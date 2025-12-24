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
package com.hitorro.network.netevent;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.network.rpc.RPCMessage;
import com.hitorro.network.rpc.RPCService;
import com.hitorro.network.rpc.cluster.ClusterService;
import com.hitorro.network.rpc.cluster.IDef;
import com.hitorro.util.io.StoreException;

import java.io.IOException;
import java.util.List;

public class NetEventUtil {
	public static final void sendSingleEventNow(String subsystem,
												String topic,
												String subTopic,
												List<String> args,
												boolean notifyLocal,
												List<JVS> defs)
			throws IOException, ClassNotFoundException, StoreException {
		for (JVS id : defs) {
			sendSingleEventNow(subsystem, topic, subTopic, args, notifyLocal,
					id.getString(IDef.ipAddressKey), IDef.getPortFor(id, ClusterService.RPCUDPPortKey));
		}
	}

	public static final void sendSingleEventAllNow(String subsystem, String topic, String subTopic, List<String> args, boolean notifyLocal)
			throws IOException, ClassNotFoundException, StoreException {
		RPCMessage msg = RPCService.s_service.getGlobalBroadcastMessageToSend(EventRPC.EventRPCMethod);
		constructAndSend(msg, notifyLocal, subsystem, topic, subTopic, args);

	}

	public static final void sendSingleEventNow(String subsystem,
												String topic,
												String subTopic,
												List<String> args,
												boolean notifyLocal,
												String ip,
												int port)
			throws IOException, ClassNotFoundException, StoreException {
		RPCMessage msg = RPCService.s_service.getMessageToSend(ip, port, EventRPC.EventRPCMethod, false);
		constructAndSend(msg, notifyLocal, subsystem, topic, subTopic, args);
	}

	private static void constructAndSend(RPCMessage msg, boolean notifyLocal, String subsystem, String topic, String subTopic, List<String> args) throws IOException, StoreException {
		long lastSequencNumber = msg.aquireNewSequenceNumber(RPCService.s_service);
		NetEvent ne = new NetEvent();
		ne.populateInfo();
		NetEventElement nee = new NetEventElement();
		nee.setIncludeSelf(notifyLocal);
		nee.setSubsystem(subsystem);
		nee.setTopic(topic);
		nee.setSubTopic(subTopic);
		nee.setArgs(args);
		ne.getEvents().add(nee);
		msg.setPayload(ne);
		msg.sendAsyncMessage();
	}


	public static final void sendSingleEventBackTo(RPCMessage message, String subsystem, String topic, String subTopic, List<String> args)
			throws IOException, ClassNotFoundException, StoreException {
		RPCMessage msg = RPCService.s_service.getMessageToSend(message.getFromIPAddress(), message.getFromPortNumber(), EventRPC.EventRPCMethod, false);
		constructAndSend(msg, false, subsystem, topic, subTopic, args);
	}
}
