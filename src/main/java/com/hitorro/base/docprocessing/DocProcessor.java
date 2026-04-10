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
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.thread.farm.FarmCommand;
import com.hitorro.util.core.thread.farm.FarmThreadWorkFlowCreator;
import com.hitorro.util.servicecounters.CounterService;
import com.hitorro.util.servicecounters.CounterSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Setup a chain of processing steps that do some kind of computation
 */
public class DocProcessor<E, TDT> {
	FarmThreadWorkFlowCreator flowCreator;
	private final String processorName;
	private final boolean addThreadNamesToCounters;

	private final HashMap<String, AbstractEnqueue> queueMap = new HashMap();
	private CounterSet counterSet;

	private AbstractEnqueue<E> head;
	private String sinkQueueName;
	private String sinkDescription;

	public DocProcessor(String processorName, boolean addThreadNamesToCounters) {
		this.addThreadNamesToCounters = addThreadNamesToCounters;
		if (addThreadNamesToCounters) {
			counterSet = new CounterSet(processorName);
		}
		this.processorName = processorName;
		flowCreator = new FarmThreadWorkFlowCreator(processorName);
	}

	public void add(E elem) {
		try {
			head.put(elem);
		} catch (InterruptedException e) {
			Log.docprocessor.error("Problem enqueueing element on docprocessor %s error %s %e", processorName, e, e);
		}
	}

	public String getName() {
		return processorName;
	}

	public AbstractEnqueue<E> getQueueByName(String name) {
		return queueMap.get(name);
	}

	public AbstractEnqueue<E> addStep(FarmCommand<E, E, TDT> fc,
									  int queueLengthIn,
									  String inputQueueName,
									  int threadCount,
									  String description) {

		AbstractEnqueue tq = flowCreator.addStep(fc, queueLengthIn, inputQueueName, threadCount);
		if (head == null) {
			head = tq;
		}
		queueMap.put(inputQueueName, tq);

		if (addThreadNamesToCounters) {
			// QueueLengthCounterRegister removed during cleanup
		}

		return tq;
	}

	public void addSink(Sink<E> fsc,
						int queueLengthIn,
						String inputQueueName,
						String description) {
		flowCreator.setSink(fsc, inputQueueName, queueLengthIn);
		sinkQueueName = inputQueueName;
		sinkDescription = description;
	}

	public AbstractEnqueue initialize() {
		counterSet.finishInit(CounterService.getService().getCounterContext());
		AbstractEnqueue head = flowCreator.initialize();

		AbstractEnqueue sink = flowCreator.getSinkQueue();
		if (addThreadNamesToCounters && sink != null) {
			// QueueLengthCounterRegister removed during cleanup
		}
		return head;
	}
}
