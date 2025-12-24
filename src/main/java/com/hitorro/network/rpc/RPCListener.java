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

import com.hitorro.util.commandandcontrol.CommandRegistry;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.thread.EnhancedThreadFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class RPCListener implements Runnable {
	static EnhancedThreadFactory m_factory = new EnhancedThreadFactory("RPC",
			"RPC", true);
	static BlockingQueue<Runnable> m_workQueue = new PriorityBlockingQueue<Runnable>(20);
	static ThreadPoolExecutor s_exec = new ThreadPoolExecutor(20, 50, 10000, TimeUnit.SECONDS, m_workQueue, m_factory);
	public int m_portNumber;
	public CommandRegistry m_Command_registry;
	private ServerSocket m_server;
	private final boolean m_running = true;
	private final RPCService m_service;

	public RPCListener(RPCService service, int port) {
		m_Command_registry = CommandRegistry.getRegistry();
		m_service = service;
		m_portNumber = port;
	}

	/**
	 * Ip address of what this listern is bound to.  Eventually this should be configurable.
	 *
	 * @return
	 */
	public String getListeningIp() {
		return Env.getHostIP();
	}

	public int getListeningPort() {
		return m_portNumber;
	}

	public int listenSocket() {
		try {
			m_server = new ServerSocket(m_portNumber);
			Log.rpc.info("RPCListener listening on %s", m_portNumber);
			return m_portNumber;
		} catch (IOException e) {
			Log.rpc.info("Could not listen on port %s", m_portNumber);

			return -1;
		}
	}

	public void acceptLoop() {
		RPCSession session = null;
		Socket connection = null;
		while (m_running) {
			try {
				InetAddress ia = m_server.getInetAddress();
				connection = m_server.accept();

				session = new RPCSession(m_service);
				session.setSession(connection.getInputStream(), connection
						.getOutputStream(), connection);
				String name = Fmt.S("From: %s", connection.getInetAddress()
						.toString());
				Log.commands.debug("Creating RPC thread for connection %s",
						name);
				s_exec.execute(session);
			} catch (IOException e) {
				Log.commands.error("Accept failed: %s", this.m_portNumber);
			} catch (RejectedExecutionException e) {
				Log.commands.info(
						"Request got rejected due max capacity %s %e", e, e);
				try {
					connection.close();
				} catch (IOException e1) {
					Log.commands.error(
							"Got an exception closing connection , %s %e", e1,
							e1);
				}
			}
		}
	}

	public void run() {
		int port = listenSocket();
		if (port == -1) {
			Log.commands.error("Unable to configure tel");
			return;
		}
		// this thread has started now, lets start the datagram on the same port number
		Log.commands.info("Telnet listening on port %s", port);
		acceptLoop();
	}
}



