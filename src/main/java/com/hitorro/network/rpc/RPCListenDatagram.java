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
import com.hitorro.util.core.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;

public class RPCListenDatagram implements Runnable {
	public static final int BufferSize = 1024 * 16;
	public CommandRegistry m_Command_registry;
	private DatagramSocket m_server;
	private final boolean m_running = true;
	private int m_portNumber;
	private final boolean m_incrementPortIfUnavailable = true;
	private boolean m_initialized = false;
	private RPCService m_service;
	private DatagramSocket datagramSocket;
	private Socket m_socket;
	private boolean multiCastListening;
	private String multiCastGroupAddress;
	private String m_name;

	private MulticastSocket multicastDatagramSocket = null;
	private InetAddress m_ia;

	private boolean keepRunning = true;

	public RPCListenDatagram(RPCService service, int port, String name) {
		init(service, false, port, null, name);

	}

	public RPCListenDatagram(RPCService service, boolean multiCastListening, String multiCastGroupAddress, int port, String name) {
		init(service, multiCastListening, port, multiCastGroupAddress, name);
	}

	public void stop() {
		keepRunning = false;
	}

	private void init(RPCService service, boolean multiCastListening, int port, String multiCastGroupAddress, String name) {
		m_Command_registry = CommandRegistry.getRegistry();
		m_service = service;
		this.multiCastListening = multiCastListening;
		m_portNumber = port;
		this.multiCastGroupAddress = multiCastGroupAddress;
		m_name = name;
	}

	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

	public int listenDatagram() {

		if (!m_initialized) {
			if (init()) {
				return -1;
			}
		}

		while (keepRunning) {
			RPCDatagramSession session = new RPCDatagramSession(this.m_service, null);
			try {
				byte[] buff = new byte[BufferSize];
				DatagramPacket packet = new DatagramPacket(buff, buff.length);
				datagramSocket.receive(packet);
				String host = packet.getAddress().getCanonicalHostName();
				int port = packet.getPort();
				session.set(m_service, buff, host, port);
				// using an executor pool here seemed to fail misserably so we are single threaded for now
				//
				session.run();
				//m_listener.s_exec.execute(session);
			} catch (SocketException e) {
				Log.rpc.error("%s %e", e, e);
			} catch (IOException e) {
				Log.rpc.error("%s %e", e, e);
			}


		}
		if (this.multiCastListening) {
			try {
				multicastDatagramSocket.leaveGroup(m_ia);
				multicastDatagramSocket.close();
			} catch (IOException e) {
				Log.util.error("Unable to leave multicast group (%s) port: %s% address: %s %e",
						m_name, m_portNumber, multiCastGroupAddress, e, e);
			}
		} else {
			datagramSocket.close();
		}
		return -1;
	}

	private boolean init() {
		// if we are restarted by the restartable daemon we dont want to go through this again

		try {
			if (this.multiCastListening) {
				multicastDatagramSocket = new MulticastSocket(m_portNumber);
				// set the address reuse option
				multicastDatagramSocket.setReuseAddress(true);

				// join the multicast group
				m_ia = InetAddress.getByName(multiCastGroupAddress);
				if (!m_ia.isMulticastAddress()) {
					Log.rpc.info("%s receive on port %s address %s is not a group address ",
							m_name, m_portNumber, multiCastGroupAddress);
					return true;
				}
				multicastDatagramSocket.joinGroup(m_ia);
				// for purposes of the receive loop
				datagramSocket = multicastDatagramSocket;
			} else {
				datagramSocket = new DatagramSocket(m_portNumber);
			}
			if (datagramSocket == null) {
				Log.rpc.error("Unable to construct datagram socket");
			}
			Log.rpc.info("%s listening on %s", m_name, m_portNumber);

		} catch (SocketException e) {
			Log.rpc.error("Unable to aquire datagram (%s) socket %s %s %e", m_name, m_portNumber, e, e);
			return true;
		} catch (IOException e) {
			Log.rpc.error("Unable to aquire datagram (%s) socket %s %s %e", m_name, m_portNumber, e, e);
			return true;
		}
		m_initialized = true;
		return false;
	}

	public int getListeningPort() {
		return this.m_portNumber;
	}

	public String getListeningIp() {
		return multiCastGroupAddress;
	}

	public void run() {
		listenDatagram();
	}
}