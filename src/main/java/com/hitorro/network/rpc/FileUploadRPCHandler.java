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

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class FileUploadRPCHandler extends RPCHandler {
	public static final String FileUploadRPCHandlerKey = "fileupload";
	private final Map<String, FileHandleTopic> m_topics = new HashMap<String, FileHandleTopic>();

	public String getMethod() {
		return FileUploadRPCHandlerKey;
	}

	public void process(RPCMessage message, HTObjectInputStream is,
						HTObjectOutputStream os, Socket connection,
						boolean sync)
			throws IOException, StoreException, ClassNotFoundException {
		FileUpload fu = (FileUpload) message.getPayload();
		if (fu == null) {
			throw new IOException("FileUpload payload in RPC Message not found");
		}
		String topic = fu.getTopic();
		FileHandleTopic fht = m_topics.get(topic);
		if (fht == null) {
			throw new IOException(Fmt.S("FileUploadTopic of %s RPC Message not found", topic));
		}
		int response = 1;
		RPCMessage responseMessage = message.createResponse();
		// let the content handler decide on the message
		if (!fht.processStream(responseMessage, fu, is)) {
			response = -1;
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

	public void registerFileHandleTopic(FileHandleTopic fht) {
		m_topics.put(fht.getTopic(), fht);
	}


}
