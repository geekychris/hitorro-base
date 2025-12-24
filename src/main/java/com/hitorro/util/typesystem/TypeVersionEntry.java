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

import com.hitorro.util.core.HTVersionable;

import java.io.ObjectInput;
import java.io.ObjectOutput;

public class TypeVersionEntry implements HTVersionable {
	private String m_shortName;
	private String m_className;
	private int m_version;

	public TypeVersionEntry() {

	}

	public String getClassName() {
		return m_className;
	}

	public void setClassName(String name) {
		m_className = name;
	}

	public String getShortName() {
		return m_shortName;
	}

	public void setShortName(String name) {
		m_shortName = name;
	}

	public int getTypeVersion() {
		return m_version;
	}

	public void setTypeVersion(int version) {
		m_version = version;
	}

	public int getCodeVersion() {
		return 0;
	}

	public void writeExternal(ObjectOutput out) {

	}

	public void readExternal(ObjectInput in) {

	}
}
