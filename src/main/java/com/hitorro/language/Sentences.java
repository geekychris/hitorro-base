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

import com.hitorro.util.core.string.StringUtil;
import opennlp.tools.util.Span;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Sentences {
	private SentenceSegmenter ss;
	private String content;
	private List<String> sentences = null;
	private Span[] offsets = null;

	public Sentences(SentenceSegmenter ss, String content) {
		set(ss, content, null);
	}

	public Sentences(SentenceSegmenter ss, String content, Span[] offsets) {
		set(ss, content, offsets);
	}

	public void writeToPrintWriter(PrintWriter pw) {
		for (String sent : getSentences()) {
			pw.println(sent);
		}
	}

	public void set(SentenceSegmenter ss, String content, Span[] offsets) {
		this.ss = ss;
		this.content = content;
		sentences = null;
		this.offsets = offsets;
	}

	/**
	 * Visit each sentence found in the content and if the visitor returns false bail on processing any more rows.
	 */
	public void visitSentences(SentenceVisitor sv) {
		int sentenceNumber = 0;
		sv.start();
		for (Span span : getOffsets()) {
			if (!sv.visit(content, span, sentenceNumber++)) {
				return;
			}
		}
		sv.end();
	}

	public String[] getSentencesDirect() {
		return this.ss.impl.sentDetect(content);
	}

	public List<String> getSentences() {
		if (sentences != null) {
			return sentences;
		}

		Span[] offsets = getOffsets();
		if (offsets == null) {
			return null;
		}
		sentences = new ArrayList();
		for (Span i : offsets) {

			String sub = content.substring(i.getStart(), i.getEnd());
			sub = sub.trim();
			if (!StringUtil.nullOrEmptyOrBlankString(sub)) {
				sentences.add(sub);
			}
		}
		return sentences;
	}

	public Span[] getOffsets() {
		if (offsets == null) {
			offsets = ss.impl.sentPosDetect(content);
		}
		return offsets;
	}


	public double[] getProbabilities() {
		return ss.impl.getSentenceProbabilities();
	}
}
