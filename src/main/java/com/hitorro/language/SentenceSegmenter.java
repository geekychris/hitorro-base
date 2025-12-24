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

import com.hitorro.util.core.events.cache.PoolContainer;
import com.hitorro.util.core.events.cache.PooledObjectIntf;
import opennlp.tools.sentdetect.SentenceDetectorME;

import java.io.IOException;

/**
 *
 */
public class SentenceSegmenter implements PooledObjectIntf<IsoLanguage> {
	public static final String SentDetectKey = "sentdetect";
	protected PoolContainer pc;
	SentenceDetectorME impl = null;
	private int generation;

	public boolean init(IsoLanguage val) throws IOException {
		impl = val.getSentenceDetector();
		return true;
	}

	public Sentences getSentenceOffsets(String content) {
		return new Sentences(this, content);
	}

	@Override
	public int getGenerationId() {
		return generation;
	}

	@Override
	public void setGenerationId(final int id) {
		generation = id;
	}

	@Override
	public void passivate() {
	}

	@Override
	public void activate() {
	}

	@Override
	public boolean reInit(final IsoLanguage key) {
		return false;
	}

	@Override
	public void setPoolContainer(final PoolContainer pc) {
		this.pc = pc;
	}

	@Override
	public void close() {
		pc.returnIt(this);
	}
}
