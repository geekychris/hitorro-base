package com.hitorro.network.resourcecache;

import com.hitorro.network.netevent.NetEventElement;
import com.hitorro.network.netevent.NetEventUtil;
import com.hitorro.network.rpc.RPCMessage;
import com.hitorro.network.rpc.RPCService;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.io.resourcecache.ResourceDirectoryVersionNode;
import com.hitorro.util.io.resourcecache.basefile.BaseFileResourceCache;
import com.hitorro.util.io.resourcecache.basefile.BaseFileResourceDirectoryVersionNode;
import com.hitorro.util.versioning.VersionNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class ResourceCacheUtil {

	public static String fetchNewerVersion(String resource, String query) throws IOException {
		// first see if we have something to compare against.
		BaseFileResourceDirectoryVersionNode rn = BaseFileResourceCache.getCache().getResource(resource, query);
		List<String> args = new ArrayList<String>();
		args.add(query);
		if (rn != null) {
			args.add(rn.getVersion());
		}
		ResourceCacheNetEventHandler.resetList(resource);
		try {
			// XXX TODO SHOULD NOT CALL SELF
			NetEventUtil.sendSingleEventAllNow(ResourceCacheNetEventHandler.ResourceCacheKey, ResourceCacheNetEventHandler.DoYouHaveKey, resource, args, false);
		} catch (IOException e) {
			Log.util.error("Exception %s %e", e, e);
		} catch (ClassNotFoundException e) {
			Log.util.error("Exception %s %e", e, e);
		} catch (StoreException e) {
			Log.util.error("Exception %s %e", e, e);
		}

		Env.sleepNSeconds(5);
		List<GenericKeyValue<RPCMessage, NetEventElement>> list = ResourceCacheNetEventHandler.getMessagesFor(resource);
		RPCMessage highest = null;
		VersionNode highestVn = null;
		synchronized (list) {

			for (GenericKeyValue<RPCMessage, NetEventElement> curr : list) {
				NetEventElement nee = curr.getValue();
				VersionNode currVn = new VersionNode(nee.getArgs().get(0));
				if (highest == null) {
					highest = curr.getKey();
					highestVn = currVn;

					continue;
				}
				if (highestVn.compareTo(currVn) < 0) {
					highest = curr.getKey();
					highestVn = currVn;
				}

			}
		}
		if (highestVn != null) {
			// fetch it!

			ResourceDirectoryVersionNode vn = new ResourceDirectoryVersionNode(resource,
					new File("/"),
					highestVn.getMajor(),
					highestVn.getMinor(),
					highestVn.getPatch(),
					highestVn.getBuildNumber());


			RPCMessage message = RPCService.s_service.getMessageToSend(highest.getFromIPAddress(),
					highest.getFromPortNumber(),
					ResourceGetRPCHandler.ResourceGetRPCHandlerKey,
					true);
			message.setPayload(vn);
			try {
				RPCMessage rpcm = message.sendSyncMessage();
				return vn.getVersion();
			} catch (StoreException e) {
				Log.util.error("Exception %s %e", e, e);
			} catch (ClassNotFoundException e) {
				Log.util.error("Exception %s %e", e, e);
			} catch (IOException e) {
				Log.util.error("Exception %s %e", e, e);
			}

		}
		return null;
	}
}
