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

import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Console;
import gnu.trove.map.hash.TObjectLongHashMap;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.util.Span;

/**
 *
 */
public class NameFinder {
	private final NameFinderME nameFinder;
	private String[] toks;

	NameFinder(NameFinderME nameFinder) {
		this.nameFinder = nameFinder;
	}

	public void setToks(String[] toks) {
		this.toks = toks;
	}

	public Span[] getSpans() {
		if (toks == null) {
			Console.println();
		}
		return nameFinder.find(toks);
	}

	public String[] getNames() {
		StringBuilder sb = new StringBuilder();
		Span[] spans = getSpans();
		if (ArrayUtil.nullOrEmpty(spans)) {
			return null;
		}
		String[] s = new String[spans.length];
		for (int i = 0; i < spans.length; i++) {
			sb.setLength(0);
			for (int j = spans[i].getStart(); j < spans[i].getEnd(); j++) {
				if (sb.length() != 0) {
					sb.append(" ");
				}
				sb.append(toks[j]);
				s[i] = sb.toString();
			}
		}
		return s;
	}

	/**
	 * put names to a set, capturing the frequency of occurences.
	 *
	 * @param set
	 * @return
	 */
	public int getNames(TObjectLongHashMap<String> set) {
		StringBuilder sb = new StringBuilder();
		Span[] spans = getSpans();

		if (ArrayUtil.nullOrEmpty(spans)) {
			return 0;
		}
		int counter = 0;
		for (int i = 0; i < spans.length; i++) {
			sb.setLength(0);
			for (int j = spans[i].getStart(); j < spans[i].getEnd(); j++) {
				if (sb.length() != 0) {
					sb.append(" ");
				}
				sb.append(toks[j]);
				counter++;
				String name = sb.toString();
				if (set.contains(name)) {
					set.increment(name);
				} else {
					set.put(name, 1);
				}
			}
		}
		return counter;
	}

}
