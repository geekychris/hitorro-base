package com.hitorro.network.rpc;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.network.debugcommand.ActionRPCHandler;
import com.hitorro.network.resourcecache.SendResourceCommand;
import com.hitorro.network.rpc.cluster.ClusterService;
import com.hitorro.network.rpc.cluster.IDef;
import com.hitorro.util.commandandcontrol.TerminalListener;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.core.thread.RestartableService;
import com.hitorro.util.core.thread.RestartableServiceDaemon;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

import java.util.HashMap;
import java.util.Map;

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
@ServiceDefinition(dependentService = {},
		shortName = "rpc",
		description = "rpc service",
		debugCommands = {SendResourceCommand.class},
		typeManagedClasses = {RPCMessage.class, FileUpload.class, StatusMessage.class},
		uiDirectories = {})
public class RPCService {


	// config keys.
	public static final IntegerProperty PortNumber = new IntegerProperty("rpcport", "port that the telnet service runs on", 6050);

	//TODO Update turned off multigram for now
	public static final BooleanProperty EnableMultigram = new BooleanProperty("multigram.enable", "enable multigram protocols, in dev some routers dont support it", false);


	public static final BooleanProperty EnableClusterMember =
			new BooleanProperty("cluster.enable.member", "enable this instance as a member of a cluster", false);

	public static final BooleanProperty EnableNodeIsBroker =
			new BooleanProperty("cluster.enable.broker", "This node is a discovery broker", false);

	public static final IntegerProperty NumberOfRetries =
			new IntegerProperty("cluster.enable.noackretries", "Number of retries before we consider the node not responding.", 10);

	public static final StringProperty ClusterName =
			new StringProperty("cluster.name", "Name of the cluster", "HiTorro");

	public static final StringProperty ClusterAddress =
			new StringProperty("cluster.address", "Global broadcast address used for cluster", "225.2.3.5");
	private static final String RPCGroup = "RPC Listener Group";
	public static RPCService s_service;
	private static final Map<String, RPCHandler> s_syncHandlers = new HashMap<String, RPCHandler>();
	private static final int RestartCount = 10;
	private RPCListenDatagram m_rpcDatagramListener;
	private RPCListenDatagram m_rpcMCLocalDatagramListener;
	private RPCListenDatagram m_rpcMCGlobalDatagramListener;
	// group
	private String m_localGroupAddress;
	private final String m_globalGroupAddress = ClusterAddress.apply();
	private final int m_localGroupPort = 7000;
	private final int m_globalGroupPort = 7001;
	private RPCListener m_rpcListener;
	private final String m_clusterName = ClusterName.apply();
	private long m_sequenceNumber = 0;
	private final Object m_lock = new Object();

	public static RPCService getService() {
		return s_service;
	}

	public static void addHandler(RPCHandler handler) {
		s_syncHandlers.put(handler.getMethod(), handler);
	}

	/**
	 * sequence number if the rpc message that uses datagrams wants to rendezvous with a response.  THat is:
	 * <p/>
	 * node a: pings b with message ping-1234 node a records it sent b a ping with sequence 1234 and then can reconcile
	 * a sequence number of 1234 on return
	 *
	 * @return
	 */
	public long getSequenceNumber() {
		synchronized (m_lock) {
			return m_sequenceNumber++;
		}
	}

	public String getClusterName() {
		return m_clusterName;
	}

	public RPCListener getRPCListener() {
		return m_rpcListener;
	}

	public RPCListenDatagram getRPCDatagramListener() {
		return m_rpcDatagramListener;
	}

	public RPCHandler getHandler(String method) {
		return s_syncHandlers.get(method);
	}

	public RPCMessage getMessageToSend(JVS jvs, String rpcMethod, boolean sync) {
		String ip = jvs.getString(IDef.ipAddressKey);
		int port;
		if (sync) {
			port = IDef.getPortFor(jvs, ClusterService.RPCTCPPortKey);
		} else {
			port = IDef.getPortFor(jvs, ClusterService.RPCUDPPortKey);
		}

		return getMessageToSend(ip, port, rpcMethod, sync);
	}

	public RPCMessage getMessageToSend(String targetIp, int targetPort, String rpcMethod, boolean sync) {
		RPCHandler h = getHandler(rpcMethod);
		if (h == null) {
			Log.rpc.error("RPC method %s is not known", rpcMethod);
			return null;
		} else {
			if (sync) {
				if (!h.getSupportsSync()) {
					Log.rpc.error("RPC method %s is does not support synchronous requests", rpcMethod);
					return null;
				}
			} else {
				if (!h.getSupportsAsync()) {
					Log.rpc.error("RPC method %s is does not support asynchronous requests", rpcMethod);
					return null;
				}
			}
		}
		RPCMessage msg = new RPCMessage();
		msg.setEndPoints(m_rpcListener.getListeningIp(), m_rpcListener.getListeningPort(), targetIp, targetPort,
				rpcMethod, sync);
		msg.setClusterName(m_clusterName);
		return msg;
	}

	/**
	 * Message to be sent to every node on the box
	 *
	 * @param rpcMethod
	 * @return
	 */
	public RPCMessage getLocalBroadcastMessageToSend(String rpcMethod) {
		return getMessageToSend(m_localGroupAddress, this.m_localGroupPort, rpcMethod, false);
	}

	/**
	 * Message to be sent to every node registered for this protocol.
	 *
	 * @param rpcMethod
	 * @return
	 */
	public RPCMessage getGlobalBroadcastMessageToSend(String rpcMethod) {
		return getMessageToSend(m_globalGroupAddress, m_globalGroupPort, rpcMethod, false);
	}

	public String deInit() {

		s_service = null;
		if (m_rpcDatagramListener != null) {
			this.m_rpcDatagramListener.stop();
		}
		this.m_rpcDatagramListener = null;
		this.m_rpcListener = null;
		return null;
	}

	private void addRunnable(Runnable r, String name) {
		RestartableService rs = new RestartableService(name, RPCGroup, RestartCount, r, true);
		RestartableServiceDaemon.addService(rs);
	}

	public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
		RPCService.addHandler(new StatusRPC());
		RPCService.addHandler(new FileUploadRPCHandler());
		RPCService.addHandler(new ShutdownRPCHandler());

		s_service = this;

		createListeners();
		addPortsForCluster();
		addHandler(new ActionRPCHandler());
		return null;
	}


	private void addPortsForCluster() {
		IDef id = ClusterService.getThisInstanceDefinition();
		String ip = m_rpcListener.getListeningIp();
		id.addPortMap(ClusterService.RPCTCPPortKey, ip, m_rpcListener.getListeningPort());

		id.addPortMap(ClusterService.RPCUDPPortKey, ip, m_rpcDatagramListener.getListeningPort());

		if (m_rpcMCLocalDatagramListener != null) {
			id.addPortMap(ClusterService.RPCUDPLocalMulticastPortKey, m_rpcMCLocalDatagramListener.getListeningIp(), m_rpcMCLocalDatagramListener.getListeningPort());
		}
		if (m_rpcMCGlobalDatagramListener != null) {
			id.addPortMap(ClusterService.RPCUDPGlobalMulticastPortKey, m_rpcMCGlobalDatagramListener.getListeningIp(), m_rpcMCGlobalDatagramListener.getListeningPort());

			id.addPortMap(ClusterService.TelnetPortKey, m_rpcMCGlobalDatagramListener.getListeningIp(), TerminalListener.PortNumber.apply());
		}
	}

	private void createListeners() {
		boolean enableMultigram = EnableMultigram.apply();

		m_rpcListener = new RPCListener(this, PortNumber.apply());
		addRunnable(m_rpcListener, "RPC Listener");


		m_rpcDatagramListener = new RPCListenDatagram(this, PortNumber.apply(), "RPC Datagram Listener");
		addRunnable(m_rpcDatagramListener, "RPC Datagram Listener");


		if (enableMultigram) {
			String myIpf = Env.getHostIP();
			if (StringUtil.nullOrEmptyString(myIpf)) {
				Log.rpc.error("Unable to register local multicast datagram as the ip address of this host was not retrievable");
				m_localGroupAddress = "NOTSET";
			} else {
				//  address has to be between 224.0.0.0 to 239.255.255.255 and therefor we steel this hosts ip address and morph it.
				// that way we can have a group address just for things on this box.
				String[] parts = StringUtil.splitByToken(myIpf, '.');
				String firstPart = parts[0];
				if (firstPart.length() < 3) {
					firstPart = "1";
				} else {
					firstPart = firstPart.substring(1);
				}
				m_localGroupAddress = Fmt.S("23%s.%s", firstPart, parts[1]);
				m_rpcMCLocalDatagramListener = new RPCListenDatagram(this, true, m_localGroupAddress,
						m_localGroupPort, "RPC Datagram Local MultiCast");
				addRunnable(m_rpcMCLocalDatagramListener, "RPC Datagram Local MultiCast");
			}


			m_rpcMCGlobalDatagramListener = new RPCListenDatagram(this, true, this.m_globalGroupAddress,
					m_globalGroupPort, "RPC Datagram Global MultiCast");
			addRunnable(m_rpcMCGlobalDatagramListener, "RPC Datagram Global MultiCast");
		}
	}

	public String start(boolean dbInit) {
		// dont listen till we have pretty much started up!

		return null;
	}

}
