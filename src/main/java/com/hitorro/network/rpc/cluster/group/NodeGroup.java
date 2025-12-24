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


@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.NodeGroup,
		isView = false,
		isPersisted = false,
		schemaVersion = NodeGroup.SerialVersion)
public class NodeGroup implements HTSerializable {
	public static final int SerialVersion = 1;
	private List<NodeGroupMember> members = new ArrayList<NodeGroupMember>();
	private long lastTouch;

	public NodeGroup() {

	}

	public String toString() {
		return Fmt.S("count: %s", members.size());
	}

	/**
	 * Make an exact "deep copy" replica of this node
	 *
	 * @return copy of this NodeGroup
	 */
	public NodeGroup copy() {
		NodeGroup n = new NodeGroup();
		n.lastTouch = lastTouch;
		for (NodeGroupMember member : members) {
			n.members.add(member.copy());
		}
		return n;
	}

	/**
	 * FInd a group member by nodeId and if not create one.
	 *
	 * @param nodeId
	 * @return
	 */
	public NodeGroupMember getOrAddNode(String nodeId) {
		for (NodeGroupMember member : members) {
			if (member.getNodeName().equals(nodeId)) {
				return member;
			}
		}
		touch();
		NodeGroupMember member = new NodeGroupMember();
		member.setNodeName(nodeId);
		addMember(member);
		return member;
	}

	public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
		os.writeInt(getSerializationVersion());
		os.writeListOfHTSerializable(members);
		os.writeLong(lastTouch);
	}

	public void deserialize(HTObjectInputStream os)
			throws IOException, ClassNotFoundException, StoreException {
		int version = os.readInt();
		members = os.readListOfHTSerializable();
		lastTouch = os.readLong();
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

	public List<NodeGroupMember> getMembers() {
		return members;
	}

	protected void setMembers(final List<NodeGroupMember> members) {
		this.members = members;
	}

	void touch() {
		touch(System.currentTimeMillis());
	}

	void touch(long time) {
		lastTouch = time;
	}

	public void remove(String nodeName) {
		NodeGroupMember member = getMember(nodeName);
		if (member != null) {
			members.remove(member);
			touch();
		}
	}

	public NodeGroupMember getMember(String name) {
		for (NodeGroupMember member : members) {
			if (member.getNodeName().equals(name)) {
				return member;
			}
		}
		return null;
	}

	public void addMember(NodeGroupMember member) {
		members.add(member);
		member.setParent(this);
		touch();
		sortMembers();
	}

	public long getLastTouch() {
		return lastTouch;
	}

	protected void setLastTouch(final long lastTouch) {
		this.lastTouch = lastTouch;
	}

	private void sortMembers() {
		Collections.sort(members);
	}
}