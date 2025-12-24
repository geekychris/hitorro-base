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

import com.hitorro.util.core.BooleanUtil;
import com.hitorro.util.core.string.StringUtil;

/**
 *
 */
public class PATElem {
	private PATElem parent;

	private final String name;
	private String parentName;

	private final String description;
	private final boolean isPhrasal;

	public PATElem(String name, String description, String parentName, String isPhrasal) {
		this.name = name;
		this.description = description;
		if (!StringUtil.nullOrEmptyStringOrSaysNull(parentName)) {
			this.parentName = parentName;
		}

		this.isPhrasal = BooleanUtil.getBoolean(isPhrasal);
	}

	public boolean isPhrasal() {
		return isPhrasal;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return getName();
	}

	public String getDescription() {
		return description;
	}

	public PATElem getRoot() {
		return parent;
	}

	public String getParentString() {
		return parentName;
	}

	void fixup(PennAndTreebankBase base) {
		if (!StringUtil.nullOrEmptyOrBlankString(parentName)) {
			parent = base.getValue(parentName);
		} else {
			parent = this;
			parentName = this.name;
		}

	}
}
