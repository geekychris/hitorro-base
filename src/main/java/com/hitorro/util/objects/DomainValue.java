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

import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.core.valuemap.DomainValueIntf;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.BaseType;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.DomainValue,
		isView = false,
		isPersisted = true,
		schemaVersion = DomainValue.SerializationVersion)
public class DomainValue extends BaseType implements DomainValueIntf {
	public static final String Seperator = "?";

	public static final int SerializationVersion = 1;
	private String m_domain;
	private String m_value;

	public DomainValue() {

	}

	public DomainValue(String domain, String value) {
		setDomain(domain);
		setValue(value);
	}

	public static final String getConcatinated(String domain, String value) {
		return StringUtil.strcat(domain, Seperator, value);
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public String getValue() {
		return m_value;
	}

	public void setValue(String value) {
		m_value = value;
	}

	public void serialize(HTObjectOutputStream os)
			throws IOException, StoreException {
		// dont serialize
	}

	public void deserialize(HTObjectInputStream os, int version)
			throws IOException, ClassNotFoundException, StoreException {
		//dont deserialize
	}

	public int getSerializationVersion() {
		return SerializationVersion;
	}

	public boolean hasGuid() {
		return false;
	}

	public String getAsIndexTuple() {
		return StringUtil.strcat(m_domain, Seperator, m_value);
	}
}
