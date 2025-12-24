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
import com.hitorro.base.objects.EnqueueElement;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.predicates.JVS2JsonPredicate;
import com.hitorro.network.rpc.RPCMessage;
import com.hitorro.network.rpc.RPCService;
import com.hitorro.network.rpc.cluster.ClusterService;
import com.hitorro.network.rpc.cluster.IDef;
import com.hitorro.network.rpc.cluster.constraints.CapabilityNameConstraint;
import com.hitorro.network.rpc.cluster.constraints.CapabilitySegmentConstraint;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTSerializable;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 */
public class EnqueueClient<T extends HTSerializable> {
	JVS id;
	private final int retries = 1000;
	private final int sleepBetweenRetries = 5;
	private List<JVS> list;
	private final String queue;
	private final String capability;

	public EnqueueClient(String cap, String segment) {
		queue = segment;
		capability = cap;
		init();
	}

	private boolean init() {
		if (id == null) {
			CapabilityNameConstraint cnc = new CapabilityNameConstraint(capability);
			CapabilitySegmentConstraint csc = new CapabilitySegmentConstraint(queue);

			Predicate<JsonNode> pred = IDef.getCapabilityPredicate(capability, "");
			list = ClusterService.getService().getInstancesMatchingCriteria(new JVS2JsonPredicate(pred));
			if (!ListUtil.nullOrEmpty(list)) {
				id = list.get(0);
				return true;
			}
			return false;
		}
		return true;
	}

	public boolean send(List<T> list, boolean flush) throws IOException, ClassNotFoundException, StoreException {
		boolean in = false;
		for (int i = 0; i < retries; i++) {
			in = init();
			if (in) {
				break;
			}
			Log.rpc.warn("Sleeping %s seconds as could not find server with cap:%s segment:%s", sleepBetweenRetries, capability, queue);
			Env.sleepNSeconds(sleepBetweenRetries);
		}
		if (!in) {
			Log.rpc.error("Unable to get host for enqueueclient");
			return false;
		}

		RPCMessage msg = RPCService.s_service.getMessageToSend(id, EnqueuElementHandler.DocQueueRPCMethod, true);
		if (msg != null) {
			EnqueueElement ee = new EnqueueElement();
			ee.setFlush(flush);
			ee.setElements(list);
			ee.setQueueName(queue);

			msg.setPayload(ee);
			msg.sendSyncMessage();
		}
		return true;
	}
}
