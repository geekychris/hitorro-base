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
package com.hitorro.network.resourcecache;

import com.hitorro.network.netevent.NetEvent;
import com.hitorro.network.netevent.NetEventElement;
import com.hitorro.network.netevent.NetEventHandler;
import com.hitorro.network.netevent.NetEventUtil;
import com.hitorro.network.rpc.RPCMessage;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.io.resourcecache.basefile.BaseFileResourceCache;
import com.hitorro.util.io.resourcecache.basefile.BaseFileResourceDirectoryVersionNode;
import com.hitorro.util.versioning.VersionNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ResourceCacheNetEventHandler extends NetEventHandler {
	public static final String ResourceCacheKey = "resourcecache";
	public static final String DoYouHaveKey = "query";
	public static final String IHaveKey = "ihave";
	private static final Map<String, List<GenericKeyValue<RPCMessage, NetEventElement>>> messages =
			new HashMap<String, List<GenericKeyValue<RPCMessage, NetEventElement>>>();

	private static synchronized void addMessageForResource(RPCMessage message, NetEventElement nee, String resource) {
		List<GenericKeyValue<RPCMessage, NetEventElement>> list = getMessagesFor(resource);
		synchronized (list) {
			list.add(new GenericKeyValue<RPCMessage, NetEventElement>(message, nee));
		}
	}

	public synchronized static void resetList(String resource) {
		List<GenericKeyValue<RPCMessage, NetEventElement>> list = getMessagesFor(resource);
		synchronized (list) {
			list.clear();
		}
	}

	/**
	 * The resultant apply you MUST take a lock on it since further messages could be arriving.
	 *
	 * @param resource
	 * @return
	 */
	public synchronized static List<GenericKeyValue<RPCMessage, NetEventElement>> getMessagesFor(String resource) {
		List<GenericKeyValue<RPCMessage, NetEventElement>> list = messages.get(resource);
		if (list == null) {
			list = new LinkedList<GenericKeyValue<RPCMessage, NetEventElement>>();
			messages.put(resource, list);
		}
		return list;
	}

	public String getSubSystemName() {
		return ResourceCacheKey;
	}

	public boolean event(RPCMessage message, NetEvent ne, NetEventElement nee) {
		String topic = nee.getTopic();
		String subtopic = nee.getSubTopic();
		List<String> args = nee.getArgs();

		if (DoYouHaveKey.equals(topic)) {
			try {
				doYouHave(args, subtopic, message);
			} catch (IOException e) {
				Log.util.error("Unable to check for resource in resource cache %s %e", e, e);
			}
			return true;
		}

		// collect all messages back for nodes that have resources greater than what we asked for.

		if (IHaveKey.equals(topic)) {
			addMessageForResource(message, nee, subtopic);
			return true;
		}

		return false;
	}

	private void doYouHave(List<String> args, String subtopic, RPCMessage message) throws IOException {// this is a query to see if we have something better than
		// subtopic is the resourcename
		// arg 0 of the args is the version query
		// arg 1 of the args is the version to currently beat!
		String versionQuery = args.get(0);

		BaseFileResourceDirectoryVersionNode rn = BaseFileResourceCache.getCache().getResource(subtopic, versionQuery);
		if (rn != null) {
			// we have something, but is it better than what we have
			if (args.size() > 1) {
				String currentVersion = args.get(1);
				VersionNode vn = new VersionNode(currentVersion);
				if (rn.compareTo(vn) > 0) {
					// only send if we have a better version.
					sendIHave(args, rn, message, subtopic);
				}
			} else {
				// the requesting node didnt even have a copy
				sendIHave(args, rn, message, subtopic);
			}
		}
	}

	private void sendIHave(List<String> args, BaseFileResourceDirectoryVersionNode rn, RPCMessage message, String subtopic) {
		// we have a version greater than the one you currently have.
		args.clear();
		args.add(rn.getVersion());
		try {
			NetEventUtil.sendSingleEventBackTo(message, ResourceCacheKey, IHaveKey, subtopic, args);
		} catch (IOException e) {
			Log.util.error("Exception %s %e", e, e);
		} catch (ClassNotFoundException e) {
			Log.util.error("Exception %s %e", e, e);
		} catch (StoreException e) {
			Log.util.error("Exception %s %e", e, e);
		}
	}
}
