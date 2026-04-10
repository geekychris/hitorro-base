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
package com.hitorro.util.typesystem.constraint;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.typesystem.Type;
import com.hitorro.util.typesystem.TypeIntf;


public class IsViewForType implements HTPredicate<Type> {
	private final TypeIntf owningType;
	private final String view;

	public IsViewForType(TypeIntf t, String view) {
		owningType = t;
		this.view = view;
	}

	@Override
	public void initForPass() {
	}

	@Override
	public boolean test(final Type type) {
		if (type.getViewsOwner() == owningType) {
			return (view.equals(type.getViewName()));
		}
		return false;
	}

	@Override
	public boolean initFromMap(final JsonNode map) {
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
