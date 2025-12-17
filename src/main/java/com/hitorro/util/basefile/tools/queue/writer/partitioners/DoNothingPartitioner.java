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


package com.hitorro.util.basefile.tools.queue.writer.partitioners;


import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 */
public class DoNothingPartitioner<E> implements WriterPartitioner<E> {
	private E o;
	private boolean isWithinRange = false;

	@Override
	public void initForPass(final JsonNode params) {

	}

	@Override
	public long getPartitionSequenceNumber() {
		// we use the current time to order the queue by
		return System.currentTimeMillis();
	}

	@Override
	public boolean isWithinFileRange() {
		if (isWithinRange) {
			return true;
		}
		isWithinRange = true;
		return false;
	}

	@Override
	public void reset() {

	}

	@Override
	public E getCurrent() {
		return o;
	}

	public void setCurrent(E o) {
		this.o = o;
	}

	@Override
	public boolean hasCurrent() {
		return o != null;
	}

	@Override
	public void setPartitionBoundaries() {
	}
}
