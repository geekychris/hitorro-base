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

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.network.netevent.NetEventHandler;
import com.hitorro.network.rpc.RPCService;
import com.hitorro.util.cluster.ClusterServiceInterface;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.collection.BaseCollection;
import com.hitorro.util.core.collection.BaseCollectionChangeNotifier;
import com.hitorro.util.core.collection.BaseCollectionVisitor;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.startupframework.OpersService;
import com.hitorro.util.startupframework.ServiceContext;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@ServiceDefinition(dependentService = {OpersService.class, RPCService.class},
		shortName = "clusterservice",
		description = "cluster service",
		debugCommands = {ClusterDebug.class},
		typeManagedClasses = {},
		uiDirectories = {})
public class ClusterService implements ClusterServiceInterface, BaseCollectionChangeNotifier<JVS> {
	public static BooleanProperty DisableLocalHostIPCheck =
			new BooleanProperty("network.disablelocalhostcheck",
					"if we dont have a network connection we may end up with localhost return for an ip", false);
	// constants for the port types registered with the IDef.
	public static final String RPCTCPPortKey = "RPCTCP";
	public static final String RPCUDPPortKey = "RPCUDP";
	public static final String TelnetPortKey = "TELNET";
	public static final String HttpPortKey = "HTTP";
	public static final String RPCUDPLocalMulticastPortKey = "RPCUDPLMULT";
	public static final String RPCUDPGlobalMulticastPortKey = "RPCUDPGMULT";

	public static final String InstanceListChange = "instancelistchange";

	public static final String SingletonString = "s:";
	private static IDef me = null;
	private static ClusterService s_service;
	private String dbGroupSingletonName;
	private final Object indexUpdateLock = new Object();
	private boolean isStarted = false;

	private final BaseCollection<String, JVS> cluster = new BaseCollection();

	/**
	 * Get this instance definition, lazily initialized to avoid type system dependency during static initialization.
	 * This prevents NullPointerException when JsonTypeSystem is not yet initialized (e.g., in Docker containers).
	 */
	public static IDef getThisInstanceDefinition() {
		if (me == null) {
			synchronized (ClusterService.class) {
				if (me == null) {
					try {
						me = new IDef().initLocal();
						Log.util.info("ClusterService.getThisInstanceDefinition(): Initialized instance definition");
					} catch (Exception e) {
						Log.util.error("ClusterService.getThisInstanceDefinition(): Failed to initialize instance definition - " + e.getMessage(), e);
						// Return a minimal IDef to prevent cascading failures
						me = new IDef(); // Don't call initLocal() if it fails
					}
				}
			}
		}
		return me;
	}

	public static ClusterService getService() {
		return s_service;
	}

	public String registerHooks(boolean dbinit) {
		ServiceContext.getSC().registerInterface(ClusterServiceInterface.class, this);
		return null;
	}

	/**
	 * Number of retries we will consider before giving up on the linkup between broker and ourselfs.
	 *
	 * @return
	 */
	public int getNoAckRetries() {
		return RPCService.NumberOfRetries.apply();
	}

	public void touch() {
		JVS jvs = me.getInstanceStub();
		jvs.set(IDef.lastTouchedKey, System.currentTimeMillis());
		cluster.add(jvs);
	}

	public boolean getShouldInitDBGroup() {
		if (isMemberOfDBGroup()) {
			dbGroupSingletonName = StringUtil.strcat("DBGROUP:", Env.getDatabaseId());
			//ClusterService.getThisInstanceDefinition().addInstanceCapability(dbGroupSingletonName, "", "", "");
			return true;
		}
		return false;
	}

	public boolean replaceInstanceIfDifferent(JVS id) {
		cluster.replace(id);
		return true;
	}

	public List<JVS> getInstancesMatchingCriteria(Predicate<JVS> constraint) {
		return cgetInstancesMatchingCriteria(constraint, false);
	}

	public List<JVS> cgetInstancesMatchingCriteria(Predicate<JVS> constraint,
												   boolean removeItems) {
		List<JVS> list = new ArrayList<JVS>();
		cluster.findAll(constraint, list, removeItems);
		return list;
	}


	// ************************** SERVICE STUFF ****************************************************

	public boolean isMemberOfDBGroup() {
		return !Env.getDatabaseId().equals(Env.PRIVATE);
	}

	/**
	 * Visit the instances that are available IF they test a criteria.  Used for such things as gathering up IDs that
	 * have certain capabilities.
	 *
	 * @param constraint
	 * @param visitor
	 * @param makeWriteLock
	 */
	public void visitInstanceDefinitions(Predicate<JVS> constraint,
										 BaseCollectionVisitor<JVS> visitor,
										 boolean makeWriteLock) {
		cluster.visit(constraint, visitor, makeWriteLock);
	}

	public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
		// register cluster network calls
		RPCService.addHandler(new NodeGroupRPC());
		s_service = this;
		initMyInstance();
		NetEventHandler.addHandler(new GroupNetEvent());
		String hostIp = Env.getHostIP();
		if (StringUtil.nullOrEmptyString(hostIp)) {
			Log.util.fatal("Unable to get ip address");
			return "Unable to get ip address";
		}
		if (!DisableLocalHostIPCheck.apply()) {
			if (hostIp.equals("127.0.0.1") || hostIp.equals("127.0.1.1")) {
				Log.util.fatal("Host ip is not set.");
				return "Host ip is not set";
			}
		}
		cluster.setComparable(JVS.identityComparator);
		cluster.setKeyGenerator(JVS.keyGenerator);
		cluster.setNotifier(this);


		return null;
	}

	private void initMyInstance() {
		boolean clusterServer = RPCService.EnableClusterMember.apply();
		cluster.add(me.getInstanceStub());
	}

	public String start(boolean dbInit) {
		isStarted = true;
		return null;
	}

	public String deInit() {
		return null;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean started) {
		isStarted = started;
	}

	/**
	 * Catch additions to the clustermap container.
	 *
	 * @param jvs
	 */
	@Override
	public void added(final JVS jvs) {

	}

	/**
	 * Catch removals to the clustermap container.
	 *
	 * @param jvs
	 */
	@Override
	public void removed(final JVS jvs) {

	}

	@Override
	public boolean canRunIfDBLeader() {
		//XXX implement
		return false;
	}

	@Override
	public boolean getAmINamedSingleton(final String name) {
		//XXX implement
		return false;
	}
}