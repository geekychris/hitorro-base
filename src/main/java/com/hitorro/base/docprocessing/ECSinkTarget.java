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
package com.hitorro.base.docprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.base.docprocessing.blockqueue.QueueService;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.typesystem.HTSerializable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sink target that sends objects via the wire queue protocol to a "queue" this could be anything that understands the
 * enqueue rpc, that could be a block queue (to file or local file system, or it could be a docprocessor.
 */
public class ECSinkTarget<T extends HTSerializable> implements Sink<T> {
	public static StringProperty CapKey = new StringProperty("capability", "", QueueService.EnqueueKey);
	public static StringProperty SegmentKey = new StringProperty("segment", "", null);
	private EnqueueClient<T> ec;

	public ECSinkTarget() {
		super();

	}

	public boolean init(JsonNode node) {
		ec = new EnqueueClient(CapKey.apply(node), SegmentKey.apply(node));
		return true;
	}

	public boolean start() {
		return true;
	}

	public boolean stop() {
		return true;
	}

	@Override
	public void close() throws IOException {
		stop();
	}

	public boolean addAll(final List<T> o) throws IOException, StoreException {
		try {
			return ec.send(o, false);
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}


	public boolean add(T o) throws IOException, StoreException {
		List<T> l = new ArrayList();
		l.add(o);
		try {
			return ec.send(l, false);
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}


}
