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
package com.hitorro.language.table;

import com.hitorro.language.Iso639Table;
import com.hitorro.language.IsoLanguage;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.io.FileUtil;
import org.tartarus.snowball.SnowballProgram;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StemmerFixup extends com.hitorro.language.tableaddons.TableFixup {
	private Map<String, String> stemmers;
	private final Map<String, Class> stemmersClass = new HashMap();

	@Override
	public boolean init() throws IOException {
		File s = Iso639Table.StemmersTable.apply();
		if (FileUtil.nullOrNotExist(s)) {
			Log.lang.error("Unable to find stemmer csv file %s", s);
			return false;
		}
		stemmers = MapUtil.getMapFromKeyValueCSV(s, "iso639code", "stemmer");
		for (Map.Entry<String, String> ent : stemmers.entrySet()) {
			Class c = ClassUtil.getClassForName(ent.getValue(), SnowballProgram.class);
			if (c == null) {
				Log.lang.error("Stemmer class %s is unknown", ent.getValue());
				return false;
			}
			stemmersClass.put(ent.getKey(), c);
		}

		return true;
	}

	public Set<String> getKeys() {
		return stemmersClass.keySet();
	}

	public void visit(IsoLanguage lang) {
		Class c = stemmersClass.get(lang.getTwo());
		if (c != null) {
			lang.setStemmer(c);
		}
	}
}
