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
package com.hitorro.network.debugcommand;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.network.rpc.FileHandleTopic;
import com.hitorro.network.rpc.RPCHandler;
import com.hitorro.network.rpc.RPCMessage;
import com.hitorro.util.commandandcontrol.ActionRequest;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandRegistry;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.serialized.HTSerializedResponse;
import com.hitorro.util.commandandcontrol.serialized.InfoRow;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionRPCHandler extends RPCHandler {
	public static final String ActionRPCKey = "actionrpc";
	private final Map<String, FileHandleTopic> m_topics = new HashMap<String, FileHandleTopic>();

	public String getMethod() {
		return ActionRPCKey;
	}

	public void process(RPCMessage message, HTObjectInputStream is,
						HTObjectOutputStream os, Socket connection,
						boolean sync)
			throws IOException, StoreException {
		ActionRequest ar = (ActionRequest) message.getPayload();
		if (ar == null) {
			throw new IOException("ActionRequest payload in RPC Message not found");
		}


		RPCMessage responseMessage = message.createResponse();

		List<InfoRow> rows = execute(ar.getMethod(), ar.getArgs());
		ar.setInfoRows(rows);
		responseMessage.setPayload(ar);
		os.writeVersionedObject(responseMessage);
		os.flush();
	}

	public void processAsyncResponse(RPCMessage message) {
		//not supported
	}

	public boolean getSupportsAsync() {
		return false;
	}

	public boolean getSupportsSync() {
		return true;
	}

	public void registerFileHandleTopic(FileHandleTopic fht) {
		m_topics.put(fht.getTopic(), fht);
	}

	private List<InfoRow> execute(String verb, JVS args) {
		List<InfoRow> list = new ArrayList<InfoRow>();
		HTSerializedResponse resp = new HTSerializedResponse(list);
		Command dc = CommandRegistry.getRegistry().get(verb);

		CommandSession cs = new CommandSession();

		long time = dc.executeRestfull("", args, resp, cs, RestOperations.Get, null);
		resp.end();
		return list;
	}
}
