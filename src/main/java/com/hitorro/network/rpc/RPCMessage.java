package com.hitorro.network.rpc;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.BaseSession;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

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

@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.RPCRequest,
		isView = false,
		isPersisted = false,
		schemaVersion = RPCMessage.SerializationVersion)
public class RPCMessage implements HTSerializable {
	/*
	  The magic number is required to ensure that the message being received is infact an RPCMessage so we dont croak
	 */
	public static final int MagicRPCNumber = 31068788;
	public static final int StatusOk = 1;
	public static final int SerializationVersion = 1;
	private transient boolean m_valid;
	// error states
	private transient RPCHandler handler;
	// on wire variables
	private boolean m_synchronousRequest = true;
	private String m_fromIpAddress;
	private int m_fromPortNumber;
	private String m_toIpAddress;
	private int m_toPortNumber;
	private String m_rpcMethod;
	private String m_cluster;
	private String m_clearArguments;
	private HTSerializable m_payload;
	private int m_status;
	private String m_statusMessage;
	private boolean m_requiresDMSSession = false;
	private long m_sequenceId = 310687;
	private boolean m_request = true;

	// not to be persisted
	private transient Socket m_socket;
	private transient OutputStream m_rawOut;
	private transient InputStream m_rawIn;


	public RPCMessage() {

	}

	public boolean createSocket() throws IOException {
		SocketAddress addr = new InetSocketAddress(m_toIpAddress, m_toPortNumber);
		m_socket = new Socket();
		m_socket.connect(addr, 1000 * 10);
		m_rawOut = m_socket.getOutputStream();
		m_rawIn = m_socket.getInputStream();
		return true;
	}

	public RPCMessage createResponseFrom(String myIp, int myPort) {
		RPCMessage response = new RPCMessage();
		response.setEndPoints(myIp,
				myPort,
				m_fromIpAddress,
				m_fromPortNumber,
				m_rpcMethod,
				m_synchronousRequest);
		response.m_sequenceId = m_sequenceId;
		response.m_request = false;
		response.m_cluster = RPCService.s_service.getClusterName();
		return response;
	}

	public RPCMessage createResponse() {
		RPCMessage response = new RPCMessage();
		response.setEndPoints(m_toIpAddress,
				m_toPortNumber,
				m_fromIpAddress,
				m_fromPortNumber,
				m_rpcMethod,
				m_synchronousRequest);
		response.m_sequenceId = m_sequenceId;
		response.m_request = false;
		response.m_cluster = RPCService.s_service.getClusterName();
		return response;
	}

	public long aquireNewSequenceNumber(RPCService service) {
		m_sequenceId = service.getSequenceNumber();
		return m_sequenceId;
	}

	public int getFromPortNumber() {
		return m_fromPortNumber;
	}

	public int getToPortNumber() {
		return m_toPortNumber;
	}

	public String getFromIPAddress() {
		return m_fromIpAddress;
	}

	public String getToIPAddress() {
		return m_toIpAddress;
	}

	public long getSequenceNumber() {
		return m_sequenceId;
	}

	/**
	 * Number used to define a rendezvous back from an async request (like an rpc session id)
	 *
	 * @param number
	 */
	public void setSequenceNumber(long number) {
		m_sequenceId = number;
	}

	/**
	 * Message that is a response to an originating message returns false.
	 *
	 * @return
	 */
	public boolean isRequest() {
		return m_request;
	}

	public String getClusterName() {
		return m_cluster;
	}

	public void setClusterName(String name) {
		m_cluster = name;
	}

	public String getMethod() {
		return m_rpcMethod;
	}

	public RPCMessage sendSyncMessage()
			throws IOException, StoreException, ClassNotFoundException {
		return sendSyncMessage(null, null, null);
	}

	public RPCMessage sendDirectoryAsStream(BaseSession session, String fileName, String topic, BaseFile directory, HTSerializable payload)
			throws IOException, ClassNotFoundException, StoreException {
		FileUpload fu = new FileUpload(fileName, topic, payload);
		setPayload(fu);
		DirectoryContentsUploadRPCOutputHandler handler = new DirectoryContentsUploadRPCOutputHandler(directory);
		return sendSyncMessage(handler, null, session);
	}

	public RPCMessage sendFileStream(BaseSession session, String fileName, String topic, File f)
			throws IOException, ClassNotFoundException, StoreException {

		InputStream is = FileUtil.getBufferedFileInputStream(f);
		long size = f.length();
		return sendFileStream(session, fileName, topic, size, is);
	}

	/**
	 * Post pushing the message out, send file content.  This works with message types of such things as fileupload
	 *
	 * @param session
	 * @param fileName
	 * @param topic
	 * @param size
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws com.hitorro.util.io.StoreException
	 */
	public RPCMessage sendFileStream(BaseSession session, String fileName, String topic, long size, InputStream is)
			throws IOException, ClassNotFoundException, StoreException {
		FileUpload fu = new FileUpload(fileName, topic, null);
		setPayload(fu);
		SingleFileUploadRPCOutputHandler handler = new SingleFileUploadRPCOutputHandler(size, is, fileName);
		return sendSyncMessage(handler, null, session);
	}

	public RPCMessage sendSyncMessage(RPCOutputHandler outHandler, RPCCallbackHandler returnHandler, BaseSession session)
			throws IOException, StoreException, ClassNotFoundException {
		RPCMessage msg = null;
		try {
			createSocket();
			HTObjectOutputStream os = com.hitorro.util.typesystem.HTObjectStreamFactory.getOutputStream(this.m_rawOut, session, false);
			os.writeVersionedObject(this);

			if (outHandler != null) {
				outHandler.processTail(this, os);
			}
			os.flush();
			HTObjectInputStream is = com.hitorro.util.typesystem.HTObjectStreamFactory.getInputStream(this.m_rawIn, session);
			msg = (RPCMessage) is.readVersionedObject();
			if (returnHandler != null) {
				returnHandler.processTail(this, is);
			}


		} finally {
			if (this.m_rawOut != null) {
				this.m_rawOut.flush();
				this.m_rawOut.close();
			}
			if (this.m_rawIn != null) {
				this.m_rawIn.close();

			}
			if (m_socket != null) {
				m_socket.close();
				m_socket = null;
			}
		}
		return msg;
	}

	public boolean sendAsyncMessage()
			throws IOException, StoreException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		HTObjectOutputStream os = com.hitorro.util.typesystem.HTObjectStreamFactory.getOutputStream(baos, null, false);
		if (os == null) {
			// not initialized yet!
		}
		os.writeVersionedObject(this);
		os.flush();
		baos.close();
		byte buf[] = baos.toByteArray();
		DatagramSocket socket = RPCService.s_service.getRPCDatagramListener().getDatagramSocket();
		if (socket == null) {
			Log.rpc.error("Unable to send datagram RPCDatagramService not initialized correctly.");
			return false;
		}
		InetAddress address = InetAddress.getByName(this.m_toIpAddress);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, this.m_toPortNumber);
		socket.send(packet);
		return true;
	}


	public void setEndPoints(String fromIp, int fromPort, String toIp, int toPort, String rpcMethod, boolean sync) {
		m_fromIpAddress = fromIp;
		m_fromPortNumber = fromPort;
		m_toIpAddress = toIp;
		m_toPortNumber = toPort;
		m_rpcMethod = rpcMethod;
		setSynchronousRequest(sync);
	}

	public boolean getRequiresDMSSession() {
		return m_requiresDMSSession;
	}

	public void setRequiresDMSSession(boolean flag) {
		m_requiresDMSSession = flag;
	}

	public boolean getSynchronousRequest() {
		return m_synchronousRequest;
	}

	/**
	 * Synchronous requests are processed immediately and the handler expects to have the ability to to respond the the
	 * request within an existing open channel.
	 * <p/>
	 * Asynchronous requests are placed in a queue and processed by worker threads as they become free. These requests
	 * cannot communicate using an open channel.  They can of course send a message back to the originator.
	 *
	 * @param flag
	 */
	public void setSynchronousRequest(boolean flag) {
		m_synchronousRequest = flag;
	}

	public HTSerializable getPayload() {
		return m_payload;
	}

	public void setPayload(HTSerializable payload) {
		m_payload = payload;
	}

	public int getStatusCode() {
		return m_status;
	}

	public void setStatusCode(int code) {
		m_status = code;
	}

	public String getStatusMessage() {
		return m_statusMessage;
	}

	public void setStatusMessage(String msg) {
		m_statusMessage = msg;
	}

	public String getClearArguments() {
		return m_clearArguments;
	}

	public void setClearArguments(String args) {
		m_clearArguments = args;
	}

	public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
		os.writeInt(getSerializationVersion());
		os.writeInt(MagicRPCNumber);
		os.writeBoolean(m_synchronousRequest);
		os.writeBoolean(m_requiresDMSSession);
		os.writeString(m_rpcMethod);
		os.writeString(m_cluster);
		os.writeString(m_fromIpAddress);
		os.writeInt(m_fromPortNumber);
		os.writeString(m_toIpAddress);
		os.writeInt(m_toPortNumber);
		os.writeString(m_clearArguments);
		os.writeVersionedObject(m_payload);
		os.writeInt(m_status);
		os.writeString(m_statusMessage);
		os.writeLong(m_sequenceId);
		os.writeBoolean(m_request);
	}

	public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
		int version = os.readInt();
		int magic = os.readInt();
		if (magic != MagicRPCNumber) {
			// dont read anymore
			m_valid = false;
			return;
		}
		m_synchronousRequest = os.readBoolean();
		m_requiresDMSSession = os.readBoolean();
		m_rpcMethod = os.readString();
		//ensure we know how to process this method.
		setHandler(RPCService.getService().getHandler(m_rpcMethod));

		if (getHandler() == null) {
			m_valid = false;
			return;
		}
		m_cluster = os.readString();
		m_fromIpAddress = os.readString();
		m_fromPortNumber = os.readInt();
		m_toIpAddress = os.readString();
		m_toPortNumber = os.readInt();
		m_clearArguments = os.readString();
		m_payload = os.readVersionedObject();
		m_status = os.readInt();
		m_statusMessage = os.readString();

		m_sequenceId = os.readLong();
		m_request = os.readBoolean();
		m_valid = true;
	}

	public boolean getIsValid() {
		return m_valid;
	}

	public int getSerializationVersion() {
		return SerializationVersion;
	}

	public boolean isPersisted() {
		return false;
	}

	public boolean hasGuid() {
		return false;
	}

	public boolean hasSoftGuid() {
		return false;
	}

	public RPCHandler getHandler() {
		return handler;
	}

	public void setHandler(RPCHandler handler) {
		this.handler = handler;
	}


	class SingleFileUploadRPCOutputHandler implements RPCOutputHandler {
		private final long m_size;
		private final InputStream m_is;
		private final String fileName;

		public SingleFileUploadRPCOutputHandler(long size, InputStream is, String fileName) {
			m_size = size;
			m_is = is;
			this.fileName = fileName;
		}

		public void processTail(RPCMessage msg, HTObjectOutputStream os) throws IOException {
			os.writeLong(m_size);
			os.writeString(fileName);
			os.writeInputStream(m_is);
			os.writeLong(-1);
			os.flush();
		}
	}

	class DirectoryContentsUploadRPCOutputHandler implements RPCOutputHandler {
		private final BaseFile directory;
		private final int rootDirLength;

		public DirectoryContentsUploadRPCOutputHandler(BaseFile directory) {
			this.directory = directory;
			rootDirLength = directory.getAbsolutePath().length();
		}

		public void processTail(RPCMessage msg, HTObjectOutputStream os) throws IOException {
			if (!directory.isDir()) {
				writeFile(directory, os);
			} else {
				recurse(directory, os);
			}
			os.writeLong(-1);
			os.flush();
		}

		private boolean recurse(BaseFile dir, HTObjectOutputStream os) throws IOException {
			BaseFile[] list = dir.listFiles();
			for (BaseFile f : list) {
				if (!f.isDir()) {
					writeFile(f, os);
				} else {
					recurse(f, os);
				}
			}
			return true;
		}

		private void writeFile(BaseFile file, HTObjectOutputStream os) throws IOException {
			String name = file.getAbsolutePath().substring(rootDirLength);
			long size = file.length();
			os.writeLong(size);
			os.writeString(name);
			InputStream is = file.getDataInputStream();
			os.writeInputStream(is);
		}
	}
}
