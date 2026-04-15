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
package com.hitorro.util.typesystem.valuesource.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.typesystem.valuesource.ValueMapMapper;
import com.hitorro.util.typesystem.valuesource.ValueSourceForClass;
import com.hitorro.util.urlparser.UrlNormalizer;

/**
 * Take the contents of a source field that should be a URL and hash it using the AB fp algorithm Note that the
 * UrlNormalizer is not thread safe so we need to new one up per thread.  They NEVER go away.
 */
public class UrlHashMapper extends ValueMapMapper {
	public static BooleanProperty Canonical = new BooleanProperty("canonical", "", false);

	public static BooleanProperty IncludeWWW = new BooleanProperty("includewww", "", false);

	public static BooleanProperty IncludeHTTP = new BooleanProperty("includehttp", "", false);
	protected ThreadLocal<UrlNormalizer> threadData = new ThreadLocal();
	private boolean canonical;
	private boolean includeWWW;
	private boolean includeHttp;

	public UrlHashMapper() {
	}

	public boolean compute(String requestedField, ValueSourceForClass vsfc) {
		Object o = vsfc.getValue(vsfc, fromField);
		if (o == null) {
			return false;
		}
		String s = o.toString();
		String newVal = getNorm().normalize(s);
		long v = FPHash64.getFP(newVal);
		vsfc.setValue(vsfc, requestedField, Long.valueOf(v));
		return true;
	}

	public boolean init(JsonNode map) {
		super.init(map);
		initNorm(map);
		return true;
	}

	private void initNorm(final JsonNode map) {
		canonical = Canonical.apply(map);
		includeWWW = IncludeWWW.apply(map);
		includeHttp = IncludeHTTP.apply(map);
	}

	private UrlNormalizer getNorm() {
		UrlNormalizer norm = threadData.get();
		if (norm == null) {
			norm = new UrlNormalizer(canonical, includeHttp, includeWWW);
			threadData.set(norm);
		}
		return norm;
	}
}
