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
package com.hitorro.util.typesystem.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.json.keys.StaticVarProperty;
import com.hitorro.util.typesystem.valuesource.ValueMapMapper;
import com.hitorro.util.typesystem.valuesource.ValueSourceForClass;

/**
 *
 */
public class MapValueMapMapper extends ValueMapMapper {
	private static final StaticVarProperty<BaseMapper> MapperKey = new StaticVarProperty("mapper", "me class or member var", true, null, BaseMapper.class);
	protected ThreadLocal<BaseMapper> threadData = new ThreadLocal();
	private BaseMapper mapper;

	public MapValueMapMapper() {
	}

	public boolean compute(String requestedField, ValueSourceForClass vsfc) {
		Object o = vsfc.getValue(vsfc, fromField);
		if (o == null) {
			return false;
		}
		Object out = null;
		if (mapper.isThreadSafe()) {
			out = mapper.apply(o);
		} else {
			out = getMapper().apply(o);
		}
		if (out != null) {
			vsfc.setValue(vsfc, requestedField, out);
		}
		return true;
	}

	/**
	 * Depending on how the me is implemented it may not be thread safe.
	 *
	 * @return
	 */
	BaseMapper getMapper() {
		BaseMapper mapper = threadData.get();
		if (mapper == null) {
			mapper = this.mapper.getCopy();
			threadData.set(mapper);
		}
		return mapper;
	}

	public boolean init(JsonNode map) {
		super.init(map);
		mapper = MapperKey.apply(map);
		return true;
	}
}
