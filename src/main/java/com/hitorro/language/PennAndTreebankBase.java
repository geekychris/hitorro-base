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
package com.hitorro.language;

import com.hitorro.util.io.csv.CSVReader;
import com.hitorro.util.io.csv.ColumnTableMeta;
import com.hitorro.util.io.csv.csvconsumer.CSVConsumer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class PennAndTreebankBase implements CSVConsumer {
	private final String lang;
	private boolean m_firstRowProcessed = false;
	private ColumnTableMeta m_meta;
	private final Map<String, PATElem> map = new HashMap();

	PennAndTreebankBase(String lang) {
		this.lang = lang;
	}

	boolean loadFromFile(File file) throws IOException {
		CSVReader reader = new CSVReader(file);
		reader.readLines(this);

		for (PATElem elem : map.values()) {
			elem.fixup(this);
		}
		return true;
	}

	public String getLang() {
		return lang;
	}

	public PATElem getValue(String name) {
		return map.get(name);
	}

	@Override
	public void line(final int rowCount, final String[] line) {
		if (!m_firstRowProcessed) {
			m_meta = ColumnTableMeta.init(line);
			m_firstRowProcessed = true;
		} else {
			PATElem elem = new PATElem(m_meta.get("code", line), m_meta.get("description", line), m_meta.get("parent", line), m_meta.get("isphrase", line));
			map.put(elem.getName(), elem);
		}
	}
}
