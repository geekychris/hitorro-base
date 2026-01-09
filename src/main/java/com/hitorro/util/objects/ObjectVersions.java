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
package com.hitorro.util.objects;

import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.BaseType;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;
import java.util.Date;

@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.ObjectVersions,
		isView = false,
		isPersisted = true,
		schemaVersion = ObjectVersions.SerializationVersion)
public class ObjectVersions extends BaseType {
	public static final int SerializationVersion = 1;
	private int m_version;
	private long objectVersion;
	private String m_name;
	private Date m_lastModified = new Date();

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public Date getLastModified() {
		return m_lastModified;
	}

	public void setLastModified(Date date) {
		m_lastModified = date;
	}

	public int getVersion() {
		return m_version;
	}

	public void setVersion(int version) {
		m_version = version;
	}

	public long getObjectVersion() {
		return objectVersion;
	}

	public void setObjectVersion(long val) {
		objectVersion = val;
	}

	public void serialize(HTObjectOutputStream os)
			throws IOException, StoreException {
		os.writeInt(getSerializationVersion());
		super.serialize(os);
		os.writeString(m_name);
		os.writeDate(m_lastModified);
	}

	public void deserialize(HTObjectInputStream os)
			throws IOException, ClassNotFoundException, StoreException {
		int version = os.readInt();
		super.deserialize(os);
		switch (version) {
			case 1:
				objectVersion = os.readLong();
				m_name = os.readString();
				m_lastModified = os.readDate();
		}
	}

	/*
		private long objectVersion;
	private int    m_version;
	private String m_name;
	private Date m_lastModified = new Date();
	 */
	public int getSerializationVersion() {
		return SerializationVersion;
	}
}
