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

import com.hitorro.util.core.Console;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.valuemap.DomainValueIntf;
import com.hitorro.util.io.LengthConstrainedInputStream;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.objects.DomainValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HTObjectInputStreamImpl<S extends BaseSession> implements HTObjectInputStream {
	private ObjectInputStream m_ois;
	private TypeManager m_mgr;
	private S m_session;
	private HTObjectOutputStream.LoadType m_loadType = HTObjectOutputStream.LoadType.Replace;
	private boolean m_ignoreInlineContent = false;
	// save listFiles that doesnt put till we have fully formed graphs
	private final List<HTSerializable> m_saveList = new ArrayList<HTSerializable>();

	private final List<HTSerializable> m_deleteList = new ArrayList<HTSerializable>();
	/**
	 * References to basetype objects (persisted stuff) that have been retrieved or deserialized within the current
	 * transaction.  We dont want objects within our transaction to be fetched from the db as we will have to enable
	 * transaction level querying which could be expensive.
	 */
	private final Map<String, HTSerializable> m_references = new HashMap<String, HTSerializable>();

	private final Map<String, HTSerializable> m_nonPersistedReferences = new HashMap<String, HTSerializable>();

	/**
	 * @param dis                 object input stream used to read stream from
	 * @param mgr                 Type manager
	 * @param session             to to persist objects to.
	 * @param loadType            if true each reference will be deleted before reloaded
	 * @param ignoreInlineContent if the stream contains content, do not use the content, instead use the reference to
	 *                            point to existing content.
	 */
	public HTObjectInputStreamImpl(ObjectInputStream dis, TypeManager mgr,
								   S session,
								   HTObjectOutputStream.LoadType loadType,
								   boolean ignoreInlineContent) {
		init(dis, mgr, session, loadType, ignoreInlineContent);
	}

	public HTObjectInputStreamImpl(InputStream dis, TypeManager mgr,
								   S session) {
		init(dis, mgr, session, HTObjectOutputStream.LoadType.Replace, false);
	}

	public void close() throws IOException {
		if (m_ois != null) {
			m_ois.close();
		}
	}

	/**
	 * Clear all references to objects (used in long serialization chains of self contained objects....
	 */
	public void clearIdTracker() {
		m_references.clear();
		m_nonPersistedReferences.clear();
		m_saveList.clear();
		m_deleteList.clear();
	}

	private void init(InputStream is,
					  TypeManager mgr,
					  S session,
					  HTObjectOutputStream.LoadType loadType,
					  boolean ignoreInlineContent) {
		try {
			m_ois = new ObjectInputStream(is);
		} catch (IOException ioe) {
			Log.typemanager.error("Unable to construct HTBaseObjectInputStream %s %e", ioe, ioe);
		}
		m_mgr = mgr;
		m_session = session;
		m_loadType = loadType;
		m_ignoreInlineContent = ignoreInlineContent;
	}

	public boolean ignoreInlineContent() {
		return m_ignoreInlineContent;
	}

	public int getNonCommittedObjectCount() {
		return m_saveList.size();
	}

	public void flushRefs() {
		//m_ois.clear();
		m_nonPersistedReferences.clear();
		rollback();
	}

	public void rollback() {
		m_saveList.clear();
		m_references.clear();
		if (m_session != null) {
			m_session.getSessionFactory().rollbackCloseSession(m_session);
		}
	}

	public void commit() {
		if (m_session != null) {
			Collections.sort(m_saveList, new Comp());
			for (HTSerializable pt : m_saveList) {
				m_session.saveOrUpdate(pt);
			}
			m_session.getSessionFactory().commitAndCloseSession(m_session);
		}
		m_saveList.clear();
		m_references.clear();
	}

	public HTSerializable readVersionedObject()
			throws IOException, ClassNotFoundException, StoreException {
		byte type = m_ois.readByte();
		switch (type) {
			case HTObjectOutputStream.ObjectNull:
				return null;
			case HTObjectOutputStream.ObjectReference:
				return getBaseObject(readString());
			case HTObjectOutputStream.ObjectSerialized:
				/**
				 * read the object from the stream and put it to the current non committed
				 * cache for later use in this graph.
				 */
				String guid = readString();
				return readBaseTypeFromSerialzation(guid);

			case HTObjectOutputStream.EndOfStream:
				// end of stream.
				return null;
			case ObjectStreamConstants.TC_RESET:
				Console.println("ObjectStreamConstants.TC_RESET");
				return null;
			default:
				throw new IOException("unknown Object Type constant in stream");
		}
	}

	private void saveOrUpdate(HTSerializable ser) {
		m_saveList.add(ser);
	}

	public HTSerializable readBaseTypeFromSerialzation(String guid)
			throws IOException, ClassNotFoundException, StoreException {
		HTSerializable bt = m_mgr.createInstanceFromGUID(guid);
		if (bt == null) {
			throw new IOException(Fmt.S("Unable to create instance for guid %s, maybe there is no zero argument constructor", guid));
		}
		if (bt.isPersisted()) {
			// if we have no session we will blow up, we dont support persisted objects with no session as a target yet!
			// Start Old version merging
			HTSerializable oldVersion = null;
			switch (m_loadType) {
				case Replace:
					oldVersion = m_session.getHTSerializableFromGUID(guid);
					if (oldVersion != null) {
						bt = oldVersion;
					}
					break;
				case Merge:
					oldVersion = m_session.getHTSerializableFromGUID(guid);
					break;
			}
			// end old version merging

			if (bt.hasGuid()) {
				((VersionBaseType) bt).setGuid(guid);
				// we need to mark this object in the references before going any further before
				// we reference something that references us :-}
				m_references.put(guid, bt);
			} else {
				m_nonPersistedReferences.put(guid, bt);
			}
			saveOrUpdate(bt);

		} else {
			m_nonPersistedReferences.put(guid, bt);
		}

		bt.deserialize(this);
		return bt;
	}

	public String readStringInternalized()
			throws IOException, ClassNotFoundException {
		String s = (String) readUnshared();
		if (s != null) {
			return s.intern();
		}
		return null;
	}

	/**
	 * @return
	 */
	public String readString()
			throws IOException, ClassNotFoundException {
		return (String) readUnshared();
	}

	private Object readUnshared() throws IOException, ClassNotFoundException {
		return m_ois.readUnshared();
	}

	public Date readDate()
			throws IOException, ClassNotFoundException {
		return (Date) readUnshared();
	}

	public int readVersion()
			throws IOException {
		return m_ois.readInt();
	}

	public int readInt()
			throws IOException {
		return m_ois.readInt();
	}

	public short readShort()
			throws IOException {
		return m_ois.readShort();
	}

	public byte readByte()
			throws IOException {
		return m_ois.readByte();
	}

	public long readLong()
			throws IOException {
		return m_ois.readLong();
	}

	public boolean readBoolean() throws IOException {
		return m_ois.readBoolean();
	}

	public void skipNBytes(long bytesToSkip) throws IOException {
		m_ois.skip(bytesToSkip);
	}

	/**
	 * Get an input stream wrapper that only returns nBytes of content.
	 *
	 * @param bytes
	 * @return constrained input stream
	 */
	public InputStream getLimitedInputStream(long bytes) {
		return new LengthConstrainedInputStream(m_ois, bytes);
	}

	public HTSerializable getBaseObject(String guid) {
		// check first for non persisted objects
		HTSerializable bt = m_nonPersistedReferences.get(guid);
		if (bt != null) {
			return bt;
		}
		bt = m_references.get(guid);
		if (bt == null) {
			// dont have it, try to retrieve it from the database.
			bt = m_session.getHTSerializableFromGUID(guid);
			if (bt != null) {
				m_references.put(guid, bt);
			}
		}
		return bt;
	}

	public String[] readArrayOfStrings() throws IOException, ClassNotFoundException {
		int size = readInt();
		String[] array = new String[size];

		for (int i = 0; i < size; i++) {
			array[i] = readString();
		}

		return array;
	}

	public List<String> readListOfStrings() throws IOException, ClassNotFoundException {
		int size = readInt();
		List<String> list = new ArrayList<String>();

		for (int i = 0; i < size; i++) {
			list.add(readString());
		}

		return list;
	}

	public HTSerializable[] readArrayOfHTSerializable()
			throws IOException, ClassNotFoundException, StoreException {
		int size = readInt();
		HTSerializable[] array = new HTSerializable[size];

		for (int i = 0; i < size; i++) {
			array[i] = this.readVersionedObject();
		}

		return array;
	}

	public Map<String, HTSerializable> readStringToHTSerializable()
			throws IOException, StoreException, ClassNotFoundException {
		Map<String, HTSerializable> map = new HashMap<String, HTSerializable>();
		int size = readInt();
		for (int i = 0; i < size; i++) {
			String key = readString();
			HTSerializable pts = readVersionedObject();
			map.put(key, pts);
		}
		return map;
	}

	/**
	 * read a listFiles of base objects into an array.
	 *
	 * @param l
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readListOfHTSerializable(List l)
			throws IOException, ClassNotFoundException, StoreException {
		l.clear();
		int size = m_ois.readInt();
		for (int i = 0; i < size; i++) {
			l.add(readVersionedObject());
		}
	}

	public List readListOfHTSerializable()
			throws IOException, ClassNotFoundException, StoreException {
		List l = new ArrayList();
		int size = m_ois.readInt();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				l.add(readVersionedObject());
			}
		}
		return l;
	}

	public void readSetOfHTSerializable(Set set)
			throws IOException, ClassNotFoundException, StoreException {
		set.clear();
		int size = m_ois.readInt();
		for (int i = 0; i < size; i++) {
			set.add(readVersionedObject());
		}
	}

	public void readSetOfDomainValue(Set<DomainValueIntf> set)
			throws IOException, ClassNotFoundException {
		set.clear();
		int size = m_ois.readInt();
		for (int i = 0; i < size; i++) {
			set.add(new DomainValue((String) readUnshared(), (String) readUnshared()));
		}
	}

	public double readDouble() throws IOException {
		return m_ois.readDouble();
	}

	public float readFloat() throws IOException {
		return m_ois.readFloat();
	}

	class Comp implements Comparator<HTSerializable> {
		public Comp() {

		}

		public int compare(HTSerializable o1, HTSerializable o2) {
			if (o1.isPersisted()) {
				if (o2.isPersisted()) {
					long o1Id = ((BaseType) o1).getIdTempSerializationId();
					long o2Id = ((BaseType) o2).getIdTempSerializationId();
					if (o1Id < o2Id) {

						return -1;
					} else if (o1Id > o2Id) {
						return 1;
					} else {
						Log.typemanager.error("Should not have two objects in a stream with same id %s %s", o1, o2);
						return 0;
					}
				} else {
					return 1;
				}
			}
			return 1;

		}
	}
}

