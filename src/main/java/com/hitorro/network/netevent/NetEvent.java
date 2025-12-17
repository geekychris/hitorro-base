package com.hitorro.network.netevent;

import com.hitorro.util.core.Env;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

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
@TypeClassMetaInfo(shortTypeName = "NE",
		isView = false,
		isPersisted = false,
		schemaVersion = NetEvent.SerializationVersion)
public class NetEvent implements HTSerializable {
	public static final int SerializationVersion = 1;

	private String ipAddress;
	private String serverId;
	private List<NetEventElement> events = new ArrayList<NetEventElement>();

	public NetEvent() {

	}


	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public List<NetEventElement> getEvents() {
		return events;
	}

	public void setEvents(List<NetEventElement> events) {
		this.events = events;
	}

	/**
	 * Populate this event with the local info
	 */
	public void populateInfo() {
		ipAddress = Env.getHostIP();
		serverId = Env.getServerId();
	}

	/**
	 * Ensure that this message is not from the same host.
	 *
	 * @return
	 */
	public boolean getIsSameHost() {
		String ip = Env.getHostIP();
		String serverId = Env.getServerId();
		return ipAddress.equals(ip) && this.serverId.equals(serverId);
	}


	public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
		os.writeInt(SerializationVersion);
		os.writeString(ipAddress);
		os.writeString(serverId);
		os.writeListOfHTSerializable(events);
	}

	public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
		int version = os.readInt();
		ipAddress = os.readString();
		serverId = os.readString();
		os.readListOfHTSerializable(events);
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
}
