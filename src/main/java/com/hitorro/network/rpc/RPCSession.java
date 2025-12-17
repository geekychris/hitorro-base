package com.hitorro.network.rpc;

import com.hitorro.util.core.Log;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTObjectStreamFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class RPCSession implements Runnable, Comparable<RPCSession> {
	private InputStream m_is;
	private OutputStream m_os;
	private Socket m_socket;
	private final RPCService m_rpcService;

	public RPCSession(RPCService service) {
		m_rpcService = service;
	}

	public void run() {
		try {
			try {
				HTObjectInputStream is = HTObjectStreamFactory.getInputStream(m_is, null);
				RPCMessage msg = null;

				msg = (RPCMessage) is.readVersionedObject();
				if (!msg.getIsValid()) {
					Log.rpc.debug("Bogus message came in for RPCListener.");
					return;
				}

				HTObjectOutputStream os = HTObjectStreamFactory.getOutputStream(m_os, null, false);

				RPCHandler handler = RPCService.s_service.getHandler(msg.getMethod());
				if (handler != null) {
					handler.process(msg, is, os, m_socket, true);
				}
			} finally {
                /*if (ois != null)
                {
                   ois.close();
                } */
				if (m_socket != null) {
					m_socket.close();
				}
                /*if (oos != null)
                {
                   oos.flush();
                   oos.close();
                } */

			}

		} catch (IOException e) {
			Log.rpc.error("%s %e", e, e);
		} catch (ClassNotFoundException e) {
			Log.rpc.error("%s %e", e, e);
		} catch (StoreException e) {
			Log.rpc.error("%s %e", e, e);
		}


	}

	public void setSession(InputStream is, OutputStream os, Socket connection) {

		m_is = is;
		m_os = os;
		m_socket = connection;
	}

	@Override
	public int compareTo(final RPCSession o) {
		return 0;
	}
}
