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

import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.queue.AbstractEnqueue;
import com.hitorro.util.core.queue.ThreadedQueueCanceledException;
import com.hitorro.util.core.queue.ThreadedQueueTimeoutException;

import java.util.List;

/**
 * Responsible for receiving an element or set of elements and forwarding them to the docqueue.  Responsible in this
 * case of wrappering them in the PipelineElement and assigning them the correct TARGET location for the whoever
 * forwards a result.
 */
public class DocProcessorEnqueueHandler<ELEMENT, RESULT, TARGET> implements EnqueueHandler<ELEMENT> {
	private final DocProcessor dp;
	private final AbstractEnqueue tq;
	private final String name;
	private final TARGET target;

	public DocProcessorEnqueueHandler(DocProcessor dp, AbstractEnqueue tq, String name, TARGET target) {
		this.dp = dp;
		this.tq = tq;
		this.name = name;
		this.target = target;
	}

	public String getName() {
		return name;
	}

	public boolean enqueue(final ELEMENT o) {
		try {
			PipelineElement<ELEMENT, RESULT, TARGET> pe = new PipelineElement();
			pe.setElement(o);
			pe.setTarget(target);
			try {
				tq.put(pe);
			} catch (InterruptedException e) {
			}
			return true;
		} catch (ThreadedQueueCanceledException e) {
			Log.queue.error("DocProcessorEnqueueHandler %s was unable to put object with error %s %s", name, e, e);
		} catch (ThreadedQueueTimeoutException e) {
			Log.queue.error("DocProcessorEnqueueHandler %s was unable to put object with error %s %s", name, e, e);
		}
		return false;
	}

	public boolean enqueue(final List<ELEMENT> el) {
		for (ELEMENT e : el) {
			if (!enqueue(e)) {
				return false;
			}
		}
		return true;
	}

	public void flush() {
		// doesnt do anything
	}
}
