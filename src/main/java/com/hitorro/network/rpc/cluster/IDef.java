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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.JsonTypeSystem;
import com.hitorro.jsontypesystem.predicates.ForEach;
import com.hitorro.jsontypesystem.predicates.PathMatch;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.json.keys.BaseMappingProperty;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.JsonProperty;
import com.hitorro.util.json.keys.LongProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.versioning.Version;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Predicate;

public class IDef {
	public static StringProperty serverIdKey = new StringProperty("server_id", "", null);
	public static StringProperty nodeIdKey = new StringProperty("node_id", "", null);
	public static StringProperty serverTypeKey = new StringProperty("server_type", "", null);
	public static StringProperty globalIdKey = new StringProperty("global_id", "", null);
	public static StringProperty softwareVersionKey = new StringProperty("software_version", "", null);
	public static StringProperty databaseIdKey = new StringProperty("database_id", "", null);
	public static StringProperty ipAddressKey = new StringProperty("ip_address", "", null);
	public static StringProperty nodeCommentKey = new StringProperty("node_comment", "", null);
	public static StringProperty realmIdKey = new StringProperty("realm_id", "", null);
	public static JsonProperty portsKey = new JsonProperty("ports", "", null);
	public static JsonProperty capabilitiesKey = new JsonProperty("capabilities", "", null);
	public static BooleanProperty clusterServerKey = new BooleanProperty("cluster_server", "", true);

	public static BooleanProperty clusterClientKey = new BooleanProperty("cluster_client", "", true);


	public static LongProperty startedKey = new LongProperty("started", "", null);
	public static LongProperty lastTouchedKey = new LongProperty("last_touched", "", null);
	public static StringProperty capabilityKey = new StringProperty("capability", "", null);
	public static StringProperty segmentKey = new StringProperty("segment", "", null);
	public static StringProperty internalUrlKey = new StringProperty("internal_url", "", null);
	public static StringProperty externalUrlKey = new StringProperty("external_url", "", null);
	public static BooleanProperty activeKey = new BooleanProperty("active", "", true);
	public static StringProperty nameKey = new StringProperty("name", "", null);
	public static IntegerProperty portKey = new IntegerProperty("port", "", 0);
	private JVS instanceStub = null;
	private final boolean ownsSingleton = false;

	public IDef() {
		initLocal();
	}

	public static boolean isLocal(JVS jvs) {
		return ClusterService.me.getInstanceStub().getId().equals(jvs.getId());
	}

	public static int getPortFor(JVS jvs, String protocol) {
		Predicate<JsonNode> pred = getPortPredicate(protocol);
		BaseMappingProperty<Integer> fe = new ForEach(portsKey, pred).mapToProperty(portKey);
		return fe.apply(jvs);
	}

	public static void removeCapability(JVS jvs, String capability, String segment) {
		Predicate<JsonNode> pred = getCapabilityPredicate(capability, segment);
		ForEach fe = new ForEach(capabilitiesKey, pred);
		fe.remove(jvs.getJsonNode());
		touch();
	}

	public static HTPredicate<JsonNode> getCapabilityPredicate(final String capabililty, final String segment) {
		return (HTPredicate<JsonNode>) new PathMatch(capabilityKey, capabililty, PathMatch.stringEquals).
				and(new PathMatch(segmentKey, segment, PathMatch.stringEquals));
	}

	public static HTPredicate<JsonNode> getPortPredicate(final String name) {
		return (HTPredicate<JsonNode>) new PathMatch(nameKey, name, PathMatch.stringEquals);
	}

	public static void touch() {
		ClusterService.getService().touch();
	}

	public static String getHostName(JVS jvs) {
		try {
			InetAddress addr = InetAddress.getByName(jvs.getString(ipAddressKey));
			return addr.getHostName();
		} catch (UnknownHostException e) {
			Log.util.error("Exception %s %e", e, e);
			return null;
		}
	}

	public static void addCapability(final String capabililty, final String internalUrl, final String externalUrl, final String segment, final boolean active, final JVS jvs) {
		ObjectNode on = JsonNodeFactory.instance.objectNode();
		capabilityKey.setPath(on, capabililty);
		segmentKey.setPath(on, segment);
		internalUrlKey.setPath(on, internalUrl);
		externalUrlKey.setPath(on, externalUrl);
		activeKey.setPath(on, active);

		Predicate<JsonNode> pm = getCapabilityPredicate(capabililty, segment);
		ForEach fe = new ForEach(capabilitiesKey, pm);
		fe.match(jvs.getJsonNode(), on);
	}

	/**
	 * Determine if we have to concern ourselfs with electing a leader for such things as queue management, jobs, etc.
	 *
	 * @return true if this instance uses a shared set of data (such as a DB)
	 */
	public boolean usesSharedDB() {
		return Env.PRIVATE.equals(instanceStub.getString(databaseIdKey));
	}

	public IDef setID(String id) {
		instanceStub.set(JVS.idKey, id);
		return this;
	}

	public JVS getInstanceStub() {
		if (instanceStub == null) {
			Long time = System.currentTimeMillis();

			JVS is = new JVS();
			is.setType(JsonTypeSystem.getMe().getType("cme"))
					.set(serverIdKey, Env.getServerId())
					.set(realmIdKey, Env.getRealmId())
					.set(ipAddressKey, Env.getHostIP())
					.set(lastTouchedKey, time);

			instanceStub = is;
		}
		return instanceStub;
	}

	public IDef initLocal() {
		JVS jvs = getInstanceStub();
		jvs.set(nodeIdKey, Env.getNodeId().toString())
				.set(serverTypeKey, Env.getServerType())
				.set(globalIdKey, Env.getGlobalId().toString())
				.set(softwareVersionKey, Version.getFullVersionLabel())
				.set(nodeCommentKey, Env.getNodeComment())
				.set(JVS.idKey, Env.getNodeId());
		return this;
	}

	public synchronized IDef addInstanceCapability(String cap, String segment, boolean active) {
		return addInstanceCapability(cap, "", "", segment, active);
	}

	public synchronized IDef addInstanceCapability(String capabililty, String internalUrl, String externalUrl, String segment, boolean active) {
		JVS jvs = getInstanceStub();
		addCapability(capabililty, internalUrl, externalUrl, segment, active, jvs);
		//touch();
		return this;
	}

	public synchronized void addPortMap(String name, String ipAddress, int portNumber) {
		JVS jvs = getInstanceStub();
		ObjectNode on = JsonNodeFactory.instance.objectNode();
		nameKey.setPath(on, name);
		ipAddressKey.setPath(on, ipAddress);
		portKey.setPath(on, portNumber);
		jvs.append(portsKey, on);
	}
}
