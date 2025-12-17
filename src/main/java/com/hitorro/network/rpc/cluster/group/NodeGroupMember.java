package com.hitorro.network.rpc.cluster.group;

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.NodeGroupMember,
		isView = false,
		isPersisted = false,
		schemaVersion = NodeGroupMember.SerialVersion)
public class NodeGroupMember implements HTSerializable, Comparable<NodeGroupMember> {
	public static final int SerialVersion = 1;

	private String nodeName;
	private List<MemberDefinition> memberDefinitions = new ArrayList<MemberDefinition>();
	private long lastTouched;

	private transient NodeGroup parent;

	public NodeGroupMember() {

	}

	public String toString() {
		return Fmt.S("n: %s, #: %s", nodeName, memberDefinitions.size());
	}

	public MemberDefinition get(String name) {
		for (MemberDefinition md : memberDefinitions) {
			if (md.getName().equals(name)) {
				return md;
			}
		}
		return null;
	}

	public NodeGroupMember copy() {
		NodeGroupMember m = new NodeGroupMember();
		m.nodeName = nodeName;
		m.lastTouched = lastTouched;
		for (MemberDefinition def : memberDefinitions) {
			m.getMemberDefinitions().add(def.copy());
		}
		return m;
	}

	private void sortMembers() {
		Collections.sort(memberDefinitions);
	}

	public void touch() {
		touch(System.currentTimeMillis());
	}

	void touch(long time) {
		lastTouched = time;
	}

	public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
		os.writeInt(getSerializationVersion());
		os.writeString(getNodeName());
		os.writeListOfHTSerializable(getMemberDefinitions());
		for (MemberDefinition member : memberDefinitions) {
			member.setParent(this);
		}

		os.writeLong(lastTouched);
	}

	public void deserialize(HTObjectInputStream os)
			throws IOException, ClassNotFoundException, StoreException {
		int version = os.readInt();
		setNodeName(os.readString());
		setMemberDefinitions(os.readListOfHTSerializable());
		lastTouched = os.readLong();
	}

	public int getSerializationVersion() {
		return SerialVersion;
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

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(final String nodeName) {
		this.nodeName = nodeName;
	}

	public List<MemberDefinition> getMemberDefinitions() {
		return memberDefinitions;
	}

	protected void setMemberDefinitions(final List<MemberDefinition> memberDefinitions) {
		this.memberDefinitions = memberDefinitions;
		sortMembers();
	}

	public void addMemberDefinition(final MemberDefinition def) {
		memberDefinitions.add(def);
		def.setParent(this);
		sortMembers();
	}

	public int compareTo(final NodeGroupMember nodeGroupMember) {
		return nodeName.compareTo(nodeGroupMember.nodeName);
	}

	public long getLastTouched() {
		return lastTouched;
	}

	public void setLastTouched(final long lastTouched) {
		this.lastTouched = lastTouched;
	}

	public NodeGroup getParent() {
		return parent;
	}

	public void setParent(final NodeGroup parent) {
		this.parent = parent;
	}
}