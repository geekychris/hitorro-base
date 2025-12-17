package com.hitorro.network.rpc.cluster;

import com.hitorro.network.rpc.RPCHandler;
import com.hitorro.network.rpc.RPCMessage;
import com.hitorro.network.rpc.cluster.group.GroupSelector;
import com.hitorro.network.rpc.cluster.group.NodeGroup;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;

import java.io.IOException;
import java.net.Socket;

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
public class NodeGroupRPC extends RPCHandler {
	public static final String NodeGroupRPCMethod = "nodegroup";

	public String getMethod() {
		return NodeGroupRPCMethod;
	}


	/**
	 * The Sync response is only used for large queries (those that can span something larger than a packet, such as
	 * query for a number of ID's
	 *
	 * @param message
	 * @param is
	 * @param os
	 * @param connection
	 * @param sync
	 * @throws IOException
	 * @throws com.hitorro.util.io.StoreException
	 */
	public void process(RPCMessage message, HTObjectInputStream is, HTObjectOutputStream os, Socket connection, boolean sync)
			throws IOException, StoreException {
		RPCMessage msg = message.createResponse();
		msg.setPayload(execPayload(message));
		msg.setStatusCode(1);
		os.writeVersionedObject(msg);
		os.flush();
	}

	/**
	 * Prcoess request/response for clusterrpc.  Note we are not using the response handler callbacks here we could of
	 * done, that would also allow people to register for knowledge of a response ontop of the cluster layer.
	 *
	 * @param message
	 */
	public void processAsyncResponse(RPCMessage message) {


	}

	private NodeGroup execPayload(RPCMessage message) {
		return GroupSelector.getSelector().getCurrentNodeGroup();
	}

	public boolean getSupportsAsync() {
		return false;
	}

	public boolean getSupportsSync() {
		return true;
	}
}

