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

public class WordnetPool implements PooledObjectIntf<IsoLanguage> {
	protected net.sf.extjwnl.dictionary.Dictionary wordnet;
	protected HTJWNLDictionary wrapperDict;
	protected PoolContainer pc;
	private int id;

	public WordnetPool() {
		wrapperDict = HTJWNLDictionary.me.get();
	}

	public void setPoolContainer(PoolContainer pc) {
		this.pc = pc;
	}

	public HTJWNLDictionary getDict() {
		return wrapperDict;
	}

	@Override
	public int getGenerationId() {
		return id;
	}

	@Override
	public void setGenerationId(int id) {
		this.id = id;
	}

	@Override
	public void passivate() {

	}

	@Override
	public void activate() {
	}

	@Override
	public boolean reInit(IsoLanguage key) {
		return true;
	}

	@Override
	public void close() {

	}
}

