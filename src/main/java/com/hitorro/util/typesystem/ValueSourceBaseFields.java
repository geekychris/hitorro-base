/*
 * Copyright (c) 2006 - 2025 Chris Collins
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

import com.hitorro.util.core.Console;
import com.hitorro.util.typesystem.valuesource.ValueSourceForClass;

/**
 *
 */
public class ValueSourceBaseFields extends BaseFields<ValueSourceForClass> {
	private String idField = "id";
	private String createdField = "created";


	public boolean set(final ValueSourceForClass v) {
		Object o = v.getValue(v, idField);
		if (!(o instanceof Long)) {

			Console.println();
		}
		id = (Long) o;
		created = (Long) v.getValue(v, createdField);
		return true;
	}

	public BaseFields getInstance() {
		ValueSourceBaseFields f = new ValueSourceBaseFields();
		f.idField = idField;
		f.createdField = createdField;
		return f;
	}

	public String getIdField() {
		return idField;
	}

	public void setIdField(final String idField) {
		this.idField = idField;
	}

	public String getCreatedField() {
		return createdField;
	}

	public void setCreatedField(final String createdField) {
		this.createdField = createdField;
	}
}
