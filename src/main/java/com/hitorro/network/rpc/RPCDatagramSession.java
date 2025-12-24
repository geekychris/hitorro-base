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

import com.hitorro.util.core.Log;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectStreamFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class RPCDatagramSession implements Runnable {
	private RPCService m_service;
	private byte[] m_buff;
	private String host;
	private int port;

	public RPCDatagramSession(RPCService service, byte[] buff) {
		set(service, buff, "", 0);
	}

	public void set(RPCService service, byte[] buff, String host, int port) {
		m_service = service;
		m_buff = buff;
		this.host = host;
		this.port = port;
	}

	public void run() {
		ByteArrayInputStream bais = new ByteArrayInputStream(m_buff);
		try {
			if (bais == null) {
				return;
			}
			HTObjectInputStream is = HTObjectStreamFactory.getInputStream(bais, null);
			if (is == null) {
				return;
			}
			RPCMessage msg = null;

			msg = (RPCMessage) is.readVersionedObject();

			if (!msg.getIsValid()) {
				return;
			}
			RPCHandler handler = msg.getHandler();
			if (handler != null) {
				handler.processAsyncResponse(msg);
			}

		} catch (IOException e) {
			Log.rpc.error("%s h:%s p:%s %e", host, port, e, e);
		} catch (ClassNotFoundException e) {
			Log.rpc.error("%s h:%s p:%s %e", host, port, e, e);
		} catch (StoreException e) {
			Log.rpc.error("%s h:%s p:%s %e", host, port, e, e);
		}

	}
}
