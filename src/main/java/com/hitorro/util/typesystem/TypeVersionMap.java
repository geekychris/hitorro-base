package com.hitorro.util.typesystem;

import com.hitorro.util.core.HTVersionable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * Copyright (c) 2006 - 2025 Chris Collins
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
public class TypeVersionMap implements HTVersionable {
	private static final int s_codeVersion = 1;
	private final Map<String, TypeVersionEntry> m_map = new HashMap<String, TypeVersionEntry>();

	// Serialization methods

	public int getCodeVersion() {
		return s_codeVersion;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(s_codeVersion);
		Collection<TypeVersionEntry> col = m_map.values();
		Iterator<TypeVersionEntry> iter = col.iterator();
		out.writeInt(m_map.size());
		while (iter.hasNext()) {

		}
	}

	public void readExternal(ObjectInput in) {

	}
}