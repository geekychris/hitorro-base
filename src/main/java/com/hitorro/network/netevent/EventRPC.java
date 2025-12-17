package com.hitorro.network.netevent;

import com.hitorro.network.rpc.RPCHandler;
import com.hitorro.network.rpc.RPCMessage;
import com.hitorro.util.core.Log;
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
public class EventRPC extends RPCHandler {
	public static final String EventRPCMethod = "event";

	public String getMethod() {
		return EventRPCMethod;
	}

	public void process(RPCMessage message, HTObjectInputStream is, HTObjectOutputStream os, Socket connection, boolean sync)
			throws IOException, StoreException {
		dealWithRequest(message);
		RPCMessage msg = message.createResponse();
		msg.setStatusCode(1);
		os.writeVersionedObject(msg);
		os.flush();
	}

	public void processAsyncResponse(RPCMessage message) {
		dealWithRequest(message);
	}

	private void dealWithRequest(RPCMessage message) {
		if (message.isRequest()) {
			Log.rpc.debug("received event request %s", message.getSequenceNumber());
			// this is a request, so we send a response
			NetEvent ne = (NetEvent) message.getPayload();
			if (ne != null) {
				boolean fromSelf = ne.getIsSameHost();

				for (NetEventElement elem : ne.getEvents()) {
					if (fromSelf && !elem.isIncludeSelf()) {
						// message was from self but we dont want to include ourself.
						continue;
					}
					NetEventHandler handler = NetEventHandler.getHandler(elem.getSubsystem());
					if (handler != null) {
						handler.event(message, ne, elem);
					}
				}
			}
		}
	}

	public boolean getSupportsAsync() {
		return true;
	}

	public boolean getSupportsSync() {
		return true;
	}
}
