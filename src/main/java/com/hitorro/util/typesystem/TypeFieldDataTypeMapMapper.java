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
package com.hitorro.util.typesystem;

import com.hitorro.util.core.map.MapMapCSVLoader;

/**
 * Type coercion apply.  Provide a type1,type2=>type3
 */
public class TypeFieldDataTypeMapMapper extends MapMapCSVLoader<TypeFieldDataType, TypeFieldDataType, TypeFieldDataType> {
	@Override
	public TypeFieldDataType getL1Key(final String key) {
		return TypeFieldDataType.getFromShortName(key);
	}

	@Override
	public TypeFieldDataType getL2Key(final String key) {
		return TypeFieldDataType.getFromShortName(key);
	}

	@Override
	public TypeFieldDataType getPayload(final String key) {
		return TypeFieldDataType.getFromShortName(key);
	}
}
