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


import com.hitorro.network.netevent.NetEventHandler;
import com.hitorro.network.rpc.FileUploadRPCHandler;
import com.hitorro.network.rpc.RPCService;
import com.hitorro.util.io.resourcecache.ResourceDirectoryVersionNode;
import com.hitorro.util.scheduler.SchedulerContext;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

@ServiceDefinition(dependentService = {RPCService.class},
		shortName = "resource",
		description = "resource service",
		debugCommands = {GetRemoteResource.class},
		typeManagedClasses = {ResourceDirectoryVersionNode.class},
		uiDirectories = {})
public class ResourceService {
	public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
		return null;
	}

	public String start(boolean dbInit) {
		SchedulerContext.get().scheduleJobs("resourcecache");
		FileUploadRPCHandler furh = (FileUploadRPCHandler) RPCService.s_service.getHandler(FileUploadRPCHandler.FileUploadRPCHandlerKey);
		furh.registerFileHandleTopic(new ResourceCopyFileHandleTopic());
		RPCService.addHandler(new ResourceGetRPCHandler());
		NetEventHandler.addHandler(new ResourceCacheNetEventHandler());

		return null;
	}

	public String deInit() {
		return null;
	}
}
