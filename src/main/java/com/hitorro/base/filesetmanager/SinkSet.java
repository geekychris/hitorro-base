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


package com.hitorro.base.filesetmanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.json.keys.StaticVarProperty;

/**
 * Represents how to sink an iterator of something to somewhere.  For example
 */
public class SinkSet<T> {
	public final StaticVarProperty<Sink<T>> MapperKey = new StaticVarProperty("sink", "class or static var of a ", false, null, Sink.class);

	private final JsonNode map;

	public SinkSet(String name, JsonNode map) {
		this.map = map;
	}

	public Sink<T> getSink(JsonNode node) {
		Sink<T> sink = MapperKey.apply(node);
		sink.init(node);
		return sink;
	}
}
