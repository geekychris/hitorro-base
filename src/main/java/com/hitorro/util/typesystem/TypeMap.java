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


import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.util.List;

@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.TypeMap,
		isView = false,
		isPersisted = false,
		schemaVersion = 1)
public class TypeMap implements HTSerializable {
	public static final int SerializationVersion = 1;

	private long m_hash;
	private final TObjectIntHashMap m_map = new TObjectIntHashMap();

	//private


	public long getHash() {
		return m_hash;
	}

	/**
	 * Package private (only initialized by the TypeManager in this vm.
	 */
	public void init() {
		TypeManager tm = TypeManager.getTypeManager();
		List<Type> types = tm.getTypes();
		StringBuilder b = new StringBuilder();
		for (Type t : types) {
			int i = t.getSchemaVersion();
			String sName = t.getName();
			b.append(sName);
			b.append(i);
			m_map.put(sName, i);
		}
		// computeFromSubjectArea the fingerprint of this set of types.
		m_hash = FPHash64.getFP(b.toString());
	}

	/**
	 * @param shortName
	 * @return
	 */
	public int getSchemaVersion(String shortName) {
		return m_map.get(shortName);
	}

	public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
		os.writeInt(getSerializationVersion());
		os.writeLong(m_hash);
		os.writeInt(m_map.size());
		TObjectIntIterator iter = m_map.iterator();
		while (iter.hasNext()) {
			os.writeString((String) iter.key());
			os.writeInt(iter.value());
			iter.advance();
		}
	}

	public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
		int version = os.readInt();
		m_hash = os.readLong();
		int size = os.readInt();
		for (int i = 0; i < size; i++) {
			m_map.put(os.readString(), os.readInt());
		}
	}

	public int getSerializationVersion() {
		return SerializationVersion;
	}

	public boolean isPersisted() {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public boolean hasGuid() {
		return false;
	}

	public boolean hasSoftGuid() {
		return false;
	}
}
