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


package com.hitorro.base.docprocessing;

import com.hitorro.util.core.iterator.queue.AbstractEnqueue;

/**
 * Channel loader for a docprocessor.  That is its responsible for defining the input and the ultimate destination
 * (target) associated with a docprocessor.  inputs can be from a file queue, an rpc mechanism, db, etc.  Targets could
 * be another queue, the file system etc.
 */
public class DocProcessingChannelLoader extends ChannelLoader<DocProcessorEnqueueHandler> {
	private final DocProcessor dp;
	private final AbstractEnqueue tq;
	private final String name;

	public DocProcessingChannelLoader(DocProcessor dp, AbstractEnqueue tq, String name) {
		this.dp = dp;
		this.tq = tq;
		this.name = name;
	}

	public boolean computeEnqueueHandler() {
		eh = new DocProcessorEnqueueHandler(dp, tq, name, st);
		return true;
	}
}
