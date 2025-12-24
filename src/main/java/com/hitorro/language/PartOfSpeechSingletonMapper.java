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

import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.core.events.cache.PoolContainer;
import com.hitorro.util.core.iterator.mappers.BaseMapper;

import java.io.IOException;

/**
 *
 */
public class PartOfSpeechSingletonMapper extends BaseMapper<IsoLanguage, PartOfSpeech> {
	public static final HashCache<IsoLanguage, PoolContainer<IsoLanguage, PartOfSpeech>> singleton = HashCache.getPooledCache("POS Singleton", 20, new PartOfSpeechSingletonMapper());

	public PartOfSpeechSingletonMapper() {
	}

	public PartOfSpeech apply(IsoLanguage key) {
		PartOfSpeech ss = new PartOfSpeech(key);
		try {
			ss.init(key);
			return ss;
		} catch (IOException e) {
			Log.sentence.error("Unable to load PartOfSpeech with error %s %e", e, e);
		}
		return null;
	}
}
