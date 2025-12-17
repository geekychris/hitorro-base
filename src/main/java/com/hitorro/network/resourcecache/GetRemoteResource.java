package com.hitorro.network.resourcecache;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.network.rpc.RPCMessage;
import com.hitorro.network.rpc.RPCService;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.io.resourcecache.ResourceDirectoryVersionNode;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;

import java.io.File;

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
@CommandDef(command = "resourcecache.getremote", description = "Get a resource cache entry from a remote host")
public class GetRemoteResource extends Command {
	@CommandArgument(required = true)
	public static final StringProperty Host = new StringProperty("host", "host to send file to", null);
	@CommandArgument(required = true)
	public static final IntegerProperty Port = new IntegerProperty("port", "port to send file to", 6000);
	@CommandArgument(required = true)
	public static final StringProperty Resource = new StringProperty("resource", "Name of resource to install as", null);
	@CommandArgument(required = true)
	public static final IntegerProperty Major = new IntegerProperty("major", "Major version number", 1);
	@CommandArgument(required = true)
	public static final IntegerProperty Minor = new IntegerProperty("minor", "Minor version number", 0);
	@CommandArgument(required = true)
	public static final IntegerProperty Patch = new IntegerProperty("patch", "Patch version number", 0);
	@CommandArgument(required = true)
	public static final IntegerProperty Build = new IntegerProperty("buildnumber", "Build number", 0);

	public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
		RPCMessage message = RPCService.s_service.getMessageToSend(Host.apply(args),
				Port.apply(args),
				ResourceGetRPCHandler.ResourceGetRPCHandlerKey,
				true);
		ResourceDirectoryVersionNode vn = new ResourceDirectoryVersionNode(Resource.apply(args),
				new File(""),
				Major.apply(args),
				Minor.apply(args),
				Patch.apply(args),
				Build.apply(args));
		message.setPayload(vn);
		try {
			RPCMessage rpcm = message.sendSyncMessage();
			if (rpcm != null) {
				writeSuccess(response, "Got resource %s", Resource.apply(args));
			} else {
				writeSimpleError(response, "Unable to retrieve resource");
			}
		} catch (StoreException e) {
			Log.util.error("Exception %s %e", e, e);
		} catch (ClassNotFoundException e) {
			Log.util.error("Exception %s %e", e, e);
		}
		return false;
	}
}
