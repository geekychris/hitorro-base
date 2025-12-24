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
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.sinks.Sink;

import java.io.IOException;

/**
 * Responsible for taking a source and a target configuration (such as file reader and file enqueue client) and creating
 * these placing them inside the appropriate enqueuehandler
 */
public abstract class ChannelLoader<E extends EnqueueHandler> {
	protected Sink st;
	protected EnqueueHandlerReader ehr;
	protected E eh;

	public boolean getEnqueueHandler(JsonNode node) throws IOException {
		JsonNode child = null;
		if (node != null) {
			child = node.get("target");
		}
		st = SinkTargetInitializer.init(child);
		if (!computeEnqueueHandler()) {
			return false;
		}
		ehr = EnqueueHandlerReaderInitializer.init(node.get("source"), eh);

		return true;
	}

	public boolean start() {
		try {
			st.start();
		} catch (IOException e) {
			Log.queue.error("Unable to start channel loader %s %e", e, e);
			return false;
		}
		ehr.start();
		return true;
	}

	public EnqueueHandlerReader getEnqueueHandlerReader() {
		return ehr;
	}

	public Sink getTarget() {
		return st;
	}

	public EnqueueHandler getEnqueueHandler() {
		return eh;
	}

	public abstract boolean computeEnqueueHandler();
}
