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

import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import opennlp.tools.namefind.TokenNameFinderModel;


/**
 *
 */
public class NameFinderModelSingletonMapper extends BaseMapper<IsoLanguage, TokenNameFinderModel> {
	public static final HashCache<IsoLanguage, TokenNameFinderModel> person = new HashCache<IsoLanguage, TokenNameFinderModel>("namefinder-person", new NameFinderModelSingletonMapper("person"));
	public static final HashCache<IsoLanguage, TokenNameFinderModel> organization = new HashCache<IsoLanguage, TokenNameFinderModel>("namefinder-organization", new NameFinderModelSingletonMapper("organization"));
	public static final HashCache<IsoLanguage, TokenNameFinderModel> location = new HashCache<IsoLanguage, TokenNameFinderModel>("namefinder-location", new NameFinderModelSingletonMapper("location"));
	public static final HashCache<IsoLanguage, TokenNameFinderModel> date = new HashCache<IsoLanguage, TokenNameFinderModel>("namefinder-date", new NameFinderModelSingletonMapper("date"));
	public static final HashCache<IsoLanguage, TokenNameFinderModel> money = new HashCache<IsoLanguage, TokenNameFinderModel>("namefinder-money", new NameFinderModelSingletonMapper("money"));

	private final String intent;

	public NameFinderModelSingletonMapper(String intent) {
		this.intent = intent;
	}

	public String eventName() {
		return null;
	}

	public TokenNameFinderModel apply(final IsoLanguage key) {
		return key.getTokenNameFinderModel(intent);
	}
}

