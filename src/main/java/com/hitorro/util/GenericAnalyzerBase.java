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
package com.hitorro.util;

import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;
import org.apache.lucene.analysis.Analyzer;

/**
 * Created by chris on 3/5/16.
 */
public abstract class GenericAnalyzerBase extends Analyzer {
	public static IntegerProperty MinLengthProperty = new IntegerProperty("index.filter.length.min", "Minimum length for a token", 2);
	public static IntegerProperty MaxLengthProperty = new IntegerProperty("index.filter.length.min", "Maximum length for a token", 40);
	public static IntegerProperty PhraseDepthProperty = new IntegerProperty("index.filter.phrasedepth", "Maximum phrase depth", 3);

	public static IntegerProperty PhraseMinDepthProperty = new IntegerProperty("index.filter.minphrasedepth", "Maximum min phrase depth", 1);

	public static StringProperty Standard = new StringProperty("index.analyzer.standard", "", "STANDARD, CASE");
	public static final String StandardSansPhrase = "STANDARD, CASE, PORTERSTEM, NUMBER, LENGTH";
	public static final String StandardWithPhraseForDF = "STANDARD, CASE, PORTERSTEM, NUMBER, LENGTH, PHRASE4DF";
	public static final String StandardWithPhrase = "STANDARD, CASE, PORTERSTEM, NUMBER, LENGTH, PHRASE";
	public static final String HTStandard = "HTSTANDARD, CASE";

	public enum Mode {
		Index, Query
	}

}
