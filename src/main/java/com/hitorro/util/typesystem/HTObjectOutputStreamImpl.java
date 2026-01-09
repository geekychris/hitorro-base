/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.typesystem;

import com.hitorro.util.core.string.StringBuilderUtil;
import com.hitorro.util.core.valuemap.DomainValueIntf;
import com.hitorro.util.io.IOUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.objects.DomainValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HTObjectOutputStreamImpl implements HTObjectOutputStream {
	/*
		Identity apply of guid's that already exist in the stream.  If they
		exist in this apply we mark the serialization with the guid for retrieval
		rather than serializing in place
	*/
	private final Map<Object, String> m_identity = new HashMap<Object, String>();

	private ObjectOutputStream m_oos;
	private final TypeManager m_mgr;
	private final BaseSession m_session;

	/**
	 * Register for uniquely identifying non persisted objects in a stream
	 */
	private long uniqueIdCounter;

	private boolean m_includeContent = false;

	private final StringBuilder m_buff = new StringBuilder();

	public HTObjectOutputStreamImpl(BaseSession session, OutputStream os, TypeManager mgr, boolean includeContent) {
		try {
			m_oos = new ObjectOutputStream(os);
		} catch (IOException ioe) {
			Log.typemanager.error("Unable to create HTBaseObjectOutputStream %s %e", ioe, ioe);
		}
		m_mgr = mgr;
		m_includeContent = includeContent;
		m_session = session;
	}

	public void flush() throws IOException {
		this.m_oos.flush();
	}

	/**
	 * WHen the output stream is used for simple object serialization we dont need the id tracking to span longer than
	 * the single highest level writeVersionedObject, else we will blow memory.
	 */
	public void clearIdTracker() throws IOException {
		m_identity.clear();
		m_oos.reset();
	}

	/**
	 * End of stream
	 *
	 * @throws IOException
	 */
	public void writeEnd() throws IOException {
		m_oos.writeByte(EndOfStream);
	}

	public void writeVersionedObject(HTSerializable bt) throws IOException, StoreException {
		if (bt == null) {
			// nothing to record other than null
			m_oos.writeByte(ObjectNull);
			return;
		}
		if (m_session != null) {
			bt = m_session.getNonProxyObject(bt);
		}
		TypeIntf t = m_mgr.getTypeForBaseType(bt);

		if (t == null) {
			Log.typemanager.error("Object %s does not have a type", bt.getClass().getCanonicalName());
			m_oos.writeByte(ObjectNull);
			return;
		}
		Object ref;
		String refGuid = null;
		if (bt.hasGuid()) {
			refGuid = ((VersionBaseType) bt).getGuid();
			ref = refGuid;
		} else if (bt.hasSoftGuid()) {
			refGuid = ((BaseType) bt).getSoftGuid();
			ref = refGuid;
		} else {
			ref = bt;
		}
		String returnedGuid = m_identity.get(ref);
		if (returnedGuid != null) {
			// we dont have to serialize this object as we already have a reference to it.
			serializeVersionedObjectReference(returnedGuid);
		} else {
			if (refGuid == null) {
				m_buff.setLength(0);
				refGuid = StringBuilderUtil.strcat(m_buff, t.getName(), ":", Long.toString(uniqueIdCounter++)).toString();
			}
			m_identity.put(ref, refGuid);
			serializeVersionedObject(refGuid, bt);
		}
	}

	public boolean includeContent() {
		return m_includeContent;
	}

	public void serializeVersionedObjectReference(String guid) throws IOException {
		m_oos.writeByte(ObjectReference);
		writeString(guid);
	}

	public void serializeVersionedObject(String guid, HTSerializable bt) throws IOException, StoreException {
		m_oos.writeByte(ObjectSerialized);
		writeString(guid);
		bt.serialize(this);
	}

	/**
	 * Wrapper for writing a string, this may change later on if we decide string serialization isnt correct for
	 * versioning.
	 *
	 * @param s
	 * @throws IOException
	 */
	public void writeString(String s) throws IOException {
		m_oos.writeUnshared(s);
	}

	public void writeVersion(int v) throws IOException {
		m_oos.writeInt(v);
	}

	public void writeInt(int v) throws IOException {
		m_oos.writeInt(v);
	}

	public void writeShort(short v) throws IOException {
		m_oos.writeShort(v);
	}

	public void writeByte(byte v) throws IOException {
		m_oos.writeByte(v);
	}

	public void writeLong(long v) throws IOException {
		m_oos.writeLong(v);
	}

	public void writeBoolean(boolean b) throws IOException {
		m_oos.writeBoolean(b);
	}

	public void writeListOfHTSerializable(List list) throws IOException, StoreException {
		int size = 0;
		if (list != null) {
			size = list.size();
		}
		writeInt(size);
		for (int i = 0; i < size; i++) {
			writeVersionedObject((HTSerializable) list.get(i));
		}
	}

	public void writeArrayOfHTSerializable(Object[] list) throws IOException, StoreException {
		if (list == null) {
			writeInt(0);
			return;
		}
		int size = list.length;
		writeInt(size);
		for (int i = 0; i < size; i++) {
			// we will croak if you try to use an object
			if (!(list[i] instanceof HTSerializable)) {
				Log.typemanager.error("Unable to serialize an object that does not implement htserializable %s", list[i].getClass());
			}
			writeVersionedObject((HTSerializable) list[i]);
		}
	}

	public void writeSetOfBaseType(Set set) throws IOException, StoreException {
		int size = set.size();
		Iterator iter = set.iterator();
		writeInt(size);
		while (iter.hasNext()) {
			writeVersionedObject((HTSerializable) iter.next());
		}
	}

	public void writeSetOfDomainValue(Set<DomainValueIntf> set) throws IOException {
		int size = set.size();
		Iterator<DomainValueIntf> iter = set.iterator();
		writeInt(size);
		while (iter.hasNext()) {
			DomainValue dv = (DomainValue) iter.next();
			this.m_oos.writeUnshared(dv.getDomain());
			this.m_oos.writeUnshared(dv.getValue());
		}
	}

	public void writeArrayOfString(String[] list) throws IOException {
		if (list == null) {
			writeInt(0);
			return;
		}
		int size = list.length;
		writeInt(size);
		for (int i = 0; i < size; i++) {
			writeString(list[i]);
		}
	}

	public void writeListOfString(List<String> list) throws IOException {
		if (list == null) {
			writeInt(0);
			return;
		}
		int size = list.size();
		writeInt(size);
		for (int i = 0; i < size; i++) {
			writeString(list.get(i));
		}
	}

	public void writeStringToHTSerializable(Map<String, HTSerializable> map) throws IOException, StoreException {
		if (map == null) {
			writeInt(0);
			return;
		}
		writeInt(map.size());
		Set<Map.Entry<String, HTSerializable>> set = map.entrySet();
		for (Map.Entry<String, HTSerializable> entry : set) {
			writeString(entry.getKey());
			this.writeVersionedObject(entry.getValue());
		}
	}

	/**
	 * Copy the bytes of the input stream to our output stream
	 *
	 * @param is
	 * @throws IOException
	 */
	public void writeInputStream(InputStream is) throws IOException {
		IOUtil.copyStream(is, m_oos);
	}

	/**
	 * Date should ultimately be replaced with a version independent mechanism
	 *
	 * @param date
	 * @throws IOException
	 */
	public void writeDate(Date date) throws IOException {
		m_oos.writeUnshared(date);
	}

	public void writeDouble(double d) throws IOException {
		m_oos.writeDouble(d);
	}

	public void writeFloat(float f) throws IOException {
		m_oos.writeFloat(f);
	}
}