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
package com.hitorro.util.io.largedata.iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.json.keys.BasefileProperty;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTObjectStreamFactory;
import com.hitorro.util.typesystem.HTSerializable;

import java.io.IOException;
import java.io.OutputStream;


public class HTSerializableSink<T extends HTSerializable> implements Sink<T> {
	public static BasefileProperty OutFileKey = new BasefileProperty("outfile", "output file");
	private OutputStream os;
	private HTObjectOutputStream hos;
	private int count = 0;
	private boolean stopped = false;

	public HTSerializableSink() {
		// constructor for the case in configs driven instantiation.
	}

	public HTSerializableSink(OutputStream os) {
		this.os = os;
	}

	@Override
	public boolean init(JsonNode node) {
		os = BaseFileUtil.bf2outputstream.apply(OutFileKey.apply(node));
		return false;
	}

	@Override
	public boolean start() {
		hos = HTObjectStreamFactory.getOutputStream(os, null, false);
		count = 0;
		return true;
	}

	@Override
	public boolean add(final T pts) throws IOException {
		try {
			hos.clearIdTracker();
			if (pts == null) {
				Log.util.error("HTSerializable was null in iterable.");
			}
			hos.writeVersionedObject(pts);
			count++;


		} catch (StoreException e) {
			Log.util.error("%s %e", e, e);
			throw new IOException(e.getMessage());
		}
		return true;
	}

	@Override
	public boolean stop() throws IOException {
		if (stopped) {
			return true;
		}
		stopped = true;
		try {
			hos.writeVersionedObject(null);
		} catch (StoreException e) {
			// XXX log it
			return false;
		}
		hos.flush();
		os.close();
		hos = null;
		os = null;
		return true;
	}

	@Override
	public void close() throws IOException {
		stop();
	}
}
