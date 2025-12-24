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
package com.hitorro.network.rpc;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.CommandArgs;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.LocalEventHub;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.log.Logger;
import com.hitorro.util.startupframework.ExitReasonObject;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class ShutdownRPCHandler extends RPCHandler {
	public static final String ShutdownRPCHandlerKey = "shutdown";
	private final Map<String, FileHandleTopic> m_topics = new HashMap<String, FileHandleTopic>();

	public String getMethod() {
		return ShutdownRPCHandlerKey;
	}

	public void process(RPCMessage message, HTObjectInputStream is, HTObjectOutputStream os, Socket connection, boolean sync) throws IOException, StoreException {
		String args = message.getClearArguments();

		RPCMessage responseMessage = message.createResponse();
		responseMessage.setStatusMessage("Shutting down");


		os.writeVersionedObject(responseMessage);
		os.flush();
	}

	private void shutdown(String args) {
		JVS argsMap = new JVS();

		if (!StringUtil.nullOrEmptyOrBlankString(args)) {
			try {
				argsMap = CommandArgs.getParameters(args, true);
			} catch (ParseException | PropaccessError e) {
				Log.rpc.error("Unable to parse arguments for shutdown %s %s %e", args, e, e);
			}
		}
		if (argsMap == null) {
			argsMap = new JVS();
		}
		String reason = null;
		try {
			reason = StringUtil.ifNotNullDefault(argsMap.getString("reason"), "Reason not provided");
			int exitCode = Integer.parseInt(StringUtil.ifNotNullDefault(argsMap.getString("exitcode"), "1"));
			ExitReasonObject reasonObject = new ExitReasonObject(reason, exitCode);
			LocalEventHub.get().event(Logger.ExitEvent, "", reasonObject);
		} catch (PropaccessError e) {
			Log.rpc.error("Unable to parse arguments for shutdown %s %s %e", args, e, e);
		}
	}

	public void processAsyncResponse(RPCMessage message) {
		shutdown(message.getClearArguments());
	}

	public boolean getSupportsAsync() {
		return true;
	}

	public boolean getSupportsSync() {
		return true;
	}

	public void registerFileHandleTopic(FileHandleTopic fht) {
		m_topics.put(fht.getTopic(), fht);
	}
}
