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

import com.hitorro.language.tableaddons.StemmerFixup;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.csv.CSVIterator;
import com.hitorro.util.io.csv.CSVIteratorImpl;
import com.hitorro.util.json.keys.FileProperty;
import com.hitorro.util.language.Iso639TableBase;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Table of languages.  The languages have rich methods for constructing NLP models used by such things as
 * PartOfSpeachSingleton and SentenceDetectorSingleton.
 */
public class Iso639Table extends Iso639TableBase {
	public static final FileProperty StemmersTable = new FileProperty("text.analyzer.stemmers", "location of where the ", "${ht_data}/text/stemmers/stemmers.csv");
	public static final FileProperty IsoLangTable = new FileProperty("i18n.langs", "location of where the ", "${ht_data}/iso639.psv");
	private static Iso639Table instance;
	public static IsoLanguage english = getInstance().getRow("en");
	public static IsoLanguage german = getInstance().getRow("de");
	public static IsoLanguage spanish = getInstance().getRow("es");
	private final TIntObjectHashMap<IsoLanguage> ordMap = new TIntObjectHashMap();
	private final HashMap<String, IsoLanguage> rows = new HashMap();

	public static Iso639Table getInstance() {
		if (instance == null) {
			File mimeFile = IsoLangTable.apply();
			try {
				CSVIteratorImpl iter = new CSVIteratorImpl(mimeFile, '|');
				Iso639Table i = new Iso639Table();
				i.load(iter);
				i.init();
				instance = i;
			} catch (FileNotFoundException e) {
				Log.util.error("Unable to read file %s %s %e", mimeFile, e, e);
				return null;
			}
		}
		return instance;
	}

	private boolean init() {
		StemmerFixup sf = new StemmerFixup();
		try {
			if (!sf.init()) {
				return false;
			}
		} catch (IOException e) {
			Log.lang.error("Unable to init stemmers %s %e", e, e);
			return false;
		}
		Set<String> keys = sf.getKeys();
		for (String k : keys) {
			IsoLanguage il = getRow(k);
			if (il != null) {
				sf.visit(il);
			}
		}
		return true;
	}

	public void addLangKeysToSet(Set<String> set) {
		for (String k : rows.keySet()) {
			set.add(k);
		}
	}

	public IsoLanguage getRow(String code) {
		return rows.get(code.toLowerCase());
	}

	public IsoLanguage getRow(int ord) {
		return ordMap.get(ord);
	}

	public int getOrd(String code) {
		IsoLanguage row = rows.get(code.toLowerCase());
		if (row == null) {
			return -1;
		}
		return row.getOrdinal();
	}

	public String getNorm(String code) {
		IsoLanguage row = rows.get(code.toLowerCase());
		if (row == null) {
			return null;
		}
		return row.getThree();

	}

	protected boolean load(CSVIterator iter) {
		String[] columns = iter.getColumnNames();
		int i = 0;
		while (iter.hasNext()) {
			columns = iter.next();
			IsoLanguage row = new IsoLanguage(columns);
			ordMap.put(row.getOrdinal(), row);
			rows.put(row.getThree(), row);
			if (!StringUtil.nullOrEmptyString(row.getTwo())) {
				rows.put(row.getTwo(), row);
			}
		}
		return true;
	}

	public IsoLanguage getLanguageFromContent(String content) {
		String iso = LanguageId.getInstance().getLanguage639(content);
		if (StringUtil.nullOrEmptyString(iso)) {
			return null;
		}
		return getRow(iso);
	}
}
