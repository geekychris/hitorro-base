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

import com.hitorro.util.core.Console;
import com.hitorro.util.core.string.StringUtil;
import opennlp.tools.util.Sequence;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper that represents access to the parts of speech from a sentence.  This is a wrapper around the tokens.  It
 * should not be held onto between calls from a Pooled PartOfSpeech object.  The PartOfSpeech object provides this
 * object as a cursor to the current parsed sentence.
 */
public class POS {
	private final PartOfSpeech p;
	private String content;
	private String[] toks;
	private List<String>[] tags;
	private double[][] probs;
	private int rowsCount;
	private int wordsCount;

	private final PennAndTreebankBase pat;

	POS(PartOfSpeech p, String content) {
		this.pat = p.getLanguage().getPennAndTreebank();
		this.p = p;
		this.content = content;
	}

	public void setContent(String content) {
		this.content = content;
		this.toks = null;
		this.tags = null;
	}

	public void setContent(String[] toks) {
		this.content = StringUtil.mergeWithJoinToken(toks, " ");
		this.toks = toks;
		this.tags = null;

	}

	public String[] getModelNames() {
		String[] names = new String[IsoLanguage.NameFinderIntent.values().length];
		int i = 0;
		for (IsoLanguage.NameFinderIntent intent : IsoLanguage.NameFinderIntent.values()) {
			names[i++] = intent.name();
		}
		return names;
	}

	public NameFinder[] getAllNameFinders() {
		NameFinder[] results = new NameFinder[IsoLanguage.NameFinderIntent.values().length];
		int i = 0;
		for (IsoLanguage.NameFinderIntent intent : IsoLanguage.NameFinderIntent.values()) {
			results[i++] = getNameFinder(intent);
		}
		return results;
	}

	/**
	 * Provides a single namefinder (we re-use it and just give it new tokens each time). We will not give you a
	 * namefinder IF we dont have a model for the language you want
	 *
	 * @param intent
	 * @return
	 */
	public NameFinder getNameFinder(IsoLanguage.NameFinderIntent intent) {
		NameFinder nf = p.getNameFinder(intent);
		nf.setToks(getTokenizedText());
		return nf;
	}

	public String[] getTokenizedText() {
		if (toks == null) {
			toks = p.tokenizerME.tokenize(content);
		}
		return toks;
	}

	public List<String>[] getTags() {
		if (tags != null) {
			return tags;
		}
		String[] t = getTokenizedText();
		if (t == null) {
			return null;
		}

		Sequence[] sequences = p.postagger.topKSequences(t);
		rowsCount = sequences.length;
		wordsCount = t.length;
		if (tags == null || tags.length < rowsCount) {
			tags = (ArrayList<String>[]) Array.newInstance(ArrayList.class, rowsCount);
			probs = new double[tags.length][];
		}

		for (int j = 0; j < sequences.length; j++) {
			Sequence s = sequences[j];
			List<String> outcomes = s.getOutcomes();
			tags[j] = new ArrayList<>(outcomes);
			probs[j] = s.getProbs();
		}
		return tags;
	}

	public int getRowCount() {
		return rowsCount;
	}

	public int getWordCount() {
		return wordsCount;
	}

	public double[][] getProbs() {
		return probs;
	}

	public String[] getTagsEnglish(int pos) {
		List<String>[] t = getTags();
		if (t == null || t.length <=pos) {
			return null;
		}
		String[] temp = new String[t[pos].size()];

		for (int i = 0; i < t[pos].size(); i++) {
			PATElem patb = pat.getValue(t[pos].get(i));
			if (patb == null) {
				temp[i] = "UNDEFINED";
			} else {
				temp[i] = patb.getName();
			}
		}
		return temp;
	}

	public void dumpInEnglish(int pos) {
		String[] toks = this.getTokenizedText();
		List<String>[] t = getTags();
		String[] tagsE = getTagsEnglish(pos);
		for (int i = 0; i < t[pos].size(); i++) {
			Console.println("%s (%s - %s)", toks[i],
					tags[pos].get(i),
					tagsE[i]);
		}
	}
}
