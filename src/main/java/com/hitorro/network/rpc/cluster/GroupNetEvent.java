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
package com.hitorro.network.rpc.cluster;

import com.hitorro.network.netevent.NetEvent;
import com.hitorro.network.netevent.NetEventElement;
import com.hitorro.network.netevent.NetEventHandler;
import com.hitorro.network.rpc.RPCMessage;
import com.hitorro.network.rpc.RPCService;
import com.hitorro.network.rpc.cluster.group.GroupSelector;
import com.hitorro.network.rpc.cluster.group.NodeGroup;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.StoreException;

import java.io.IOException;

public class GroupNetEvent extends NetEventHandler {
	public static final String LeaderNetEventKey = "groupevent";

	public static final String Version = "version";

	public static NodeGroup executeRPC(String address, int port) throws IOException {

		RPCMessage message = RPCService.s_service.getMessageToSend(address,
				port,
				NodeGroupRPC.NodeGroupRPCMethod,
				true);

		RPCMessage resp = null;
		try {
			resp = message.sendSyncMessage();
		} catch (StoreException e) {
			Log.util.error("Exception %s %e", e, e);
		} catch (ClassNotFoundException e) {
			Log.util.error("Exception %s %e", e, e);
		}
		return (NodeGroup) resp.getPayload();
	}

	public String getSubSystemName() {
		return LeaderNetEventKey;
	}

	public boolean event(RPCMessage message, NetEvent ne, NetEventElement nee) {
		String topic = nee.getTopic();

		if (topic.equals(Version)) {
			String v = nee.getSubTopic();
			long time = Long.parseLong(v);
			long lastTouch = GroupSelector.getSelector().getCurrentNodeGroup().getLastTouch();
			if (lastTouch != time) {
				// we need to fetch the new node group.
				try {
					NodeGroup ng = executeRPC(message.getFromIPAddress(), message.getFromPortNumber());
					if (ng != null) {
						GroupSelector.getSelector().applyNewNG(ng);
					} else {
						Log.util.error("Unable to retrieve nodeGroup from %s %s",
								message.getFromIPAddress(), message.getFromPortNumber());
					}
				} catch (IOException e) {
					Log.util.error("Exception %s %e", e, e);
					return false;
				}
			}
			return true;
		}

		return false;
	}

}