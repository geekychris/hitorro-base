package com.hitorro.network.resourcecache;

import com.hitorro.network.rpc.FileUploadRPCHandler;
import com.hitorro.network.rpc.RPCHandler;
import com.hitorro.network.rpc.RPCMessage;
import com.hitorro.network.rpc.RPCService;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.io.resourcecache.basefile.BaseFileResourceCache;
import com.hitorro.util.io.resourcecache.basefile.BaseFileResourceDirectoryVersionNode;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;

import java.io.IOException;
import java.net.Socket;


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
public class ResourceGetRPCHandler extends RPCHandler {
	public static final String ResourceGetRPCHandlerKey = "resourceget";
	public static final int StatusNoResource = -200;
	public static final int StatusBadSend = -201;

	public String getMethod() {
		return ResourceGetRPCHandlerKey;
	}

	public void process(RPCMessage message, HTObjectInputStream is,
						HTObjectOutputStream os, Socket connection,
						boolean sync)
			throws IOException, StoreException, ClassNotFoundException {
		BaseFileResourceDirectoryVersionNode vn = (BaseFileResourceDirectoryVersionNode) message.getPayload();

		RPCMessage responseMessage = message.createResponse();

		RPCMessage fileMsg = RPCService.s_service.getMessageToSend(message.getFromIPAddress(),
				message.getFromPortNumber(),
				FileUploadRPCHandler.FileUploadRPCHandlerKey,
				true);
		vn = BaseFileResourceCache.getCache().getResource(vn.getResource(),
				Fmt.S("%s.%s.%s", vn.getMajor(), vn.getMinor(), vn.getPatch()));
		// let the content handler decide on the message
		RPCMessage fileMsgResponse = null;
		if (vn != null) {
			fileMsgResponse = fileMsg.sendDirectoryAsStream(null, "", ResourceCopyFileHandleTopic.ResourceCopyFileHandleTopicKey, vn.getDirectory(), vn);
			responseMessage.setStatusCode(StatusNoResource);
			Log.resourcecache.info("Sending resource %s %s to host:%s/%s",
					vn.getResource(),
					vn.getVersion(),
					message.getFromIPAddress(),
					message.getFromPortNumber());
		} else {
			if (fileMsgResponse != null) {
				responseMessage.setStatusCode(fileMsgResponse.getStatusCode());
			} else {
				responseMessage.setStatusCode(StatusBadSend);
			}
		}
		os.writeVersionedObject(responseMessage);

		os.flush();
	}

	public void processAsyncResponse(RPCMessage message) {
		//not supported
	}

	public boolean getSupportsAsync() {
		return false;
	}

	public boolean getSupportsSync() {
		return true;
	}
}
