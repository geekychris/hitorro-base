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


package com.hitorro.base.docprocessing.blockqueue;

import com.hitorro.base.docprocessing.EnqueueHandler;
import com.hitorro.util.basefile.tools.queue.writer.BlockFileQueueWriter;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.StoreException;

import java.io.IOException;
import java.util.List;

/**
 * Write elements received from the Enqueue handler into a block queue writer.
 */
public class QueueServiceEnqueueHandler implements EnqueueHandler {
	private final BlockFileQueueWriter writer;
	private final String name;

	QueueServiceEnqueueHandler(String name, BlockFileQueueWriter writer) {
		this.writer = writer;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean enqueue(final Object o) {
		synchronized (writer) {
			try {
				writer.add(o);
				return true;
			} catch (IOException e) {
				Log.queue.error("Unable to put to queue %s %e", e, e);
			} catch (StoreException e) {
				Log.queue.error("Unable to put to queue %s %e", e, e);
			}
		}
		return false;
	}

	public boolean enqueue(final List el) {
		synchronized (writer) {
			try {
				for (Object o : el) {
					writer.add(o);
				}
				return true;
			} catch (IOException e) {
				Log.queue.error("Unable to put to queue %s %e", e, e);
			} catch (StoreException e) {
				Log.queue.error("Unable to put to queue %s %e", e, e);
			}
		}
		return false;
	}

	public void flush() {
		try {
			writer.flushAndHoldTillComplete();
		} catch (IOException e) {
			Log.queue.error("Unable to flush queue %s %e", e, e);
		}
	}
}
