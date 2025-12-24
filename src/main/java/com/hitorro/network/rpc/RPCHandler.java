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

import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class RPCHandler {
	private final List<RPCAsyncResponseCallbackHandler> m_callbacks = new ArrayList<RPCAsyncResponseCallbackHandler>();

	public abstract String getMethod();

	public abstract void process(RPCMessage message, HTObjectInputStream is, HTObjectOutputStream os, Socket connection, boolean sync)
			throws IOException, StoreException, ClassNotFoundException;

	/**
	 * Process a udp response from a request made.
	 *
	 * @param message
	 * @throws IOException
	 * @throws com.hitorro.util.io.StoreException
	 */
	public abstract void processAsyncResponse(RPCMessage message)
			throws IOException, StoreException, ClassNotFoundException;

	public abstract boolean getSupportsAsync();

	public abstract boolean getSupportsSync();

	public void registerRPCCallbackHandler(RPCAsyncResponseCallbackHandler rpcCallbackHandler) {
		m_callbacks.add(rpcCallbackHandler);
	}

	public void unregisterCallbackHandler(RPCAsyncResponseCallbackHandler rpcCallbackHandler) {
		m_callbacks.remove(rpcCallbackHandler);
	}

	protected void callback(RPCMessage message) {
		for (RPCAsyncResponseCallbackHandler handler : m_callbacks) {
			if (handler.process(this, message)) {
				// processed
				return;
			}
		}
	}
}
