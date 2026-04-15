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
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.typesystem.valuesource.ValueMapMapper;
import com.hitorro.util.typesystem.valuesource.ValueSourceForClass;

/**
 * com.hitorro.util.typesystem.valuesource.me.PrefixCombiner
 */
public class PrefixCombiner extends ValueMapMapper {
	public static StringProperty Left = new StringProperty("srcfield", "", null);
	public static StringProperty Prefix = new StringProperty("prefix", "", null);

	private String field;
	private String prefix;

	public PrefixCombiner() {
	}

	public boolean compute(String requestedField, ValueSourceForClass vsfc) {
		Object l = vsfc.getValue(vsfc, field);
		if (l == null) {
			return false;
		}

		vsfc.setValue(vsfc, requestedField, StringUtil.strcat(prefix, l));
		return true;
	}

	public boolean init(JsonNode map) {
		super.init(map);
		field = Left.apply(map);
		prefix = Prefix.apply(map);
		return true;
	}
}
