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

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.network.rpc.cluster.IDef;

import java.util.Iterator;
import java.util.List;

public class NodeGroupDiffer {
	private NodeGroupMember o = null;
	private NodeGroupMember c = null;
	private Iterator<NodeGroupMember> oT = null;
	private Iterator<NodeGroupMember> cT = null;
	private NodeGroupDifferCallback callback = null;
	private Iterator<MemberDefinition> oI = null;
	private Iterator<MemberDefinition> cI = null;
	private MemberDefinition oD = null;
	private MemberDefinition cD = null;

	/**
	 * Diff the nodegroup against a listFiles of nodes.  Typically used to determine if a group has gained or lost nodes.
	 * Assumes that ID's are in order
	 *
	 * @param old
	 * @param ids
	 */
	public void diffNodeGroupAgainstInstanceList(NodeGroup old, List<JVS> ids, NodeGroupDifferCallback callback) {
		this.callback = callback;
		oT = old.getMembers().iterator();
		Iterator<JVS> idc = ids.iterator();
		String currName = getNameFromIDIterator(idc);
		// prime the pump
		advanceOld();
		while (o != null && currName != null) {
			String oName = o.getNodeName();

			int i = oName.compareTo(currName);
			if (i == 0) {
				// same...diffNodeGroupAgainstInstanceList definitions
				callback.nodeDiff(o.getNodeName(), Mode.Modify);
				advanceOld();
				currName = getNameFromIDIterator(idc);
			} else if (i < 0) {
				// o is smaller than c its a node that has been removed
				callback.nodeDiff(o.getNodeName(), Mode.Remove);
				advanceOld();
			} else {
				callback.nodeDiff(currName, Mode.Add);
				currName = getNameFromIDIterator(idc);
			}
		}
		reportAllNodesAs(o, oT, Mode.Remove);
		reportAllNodesAs(idc, currName, Mode.Add);

	}

	public void diff(NodeGroup old, NodeGroup current, NodeGroupDifferCallback callback) {
		this.callback = callback;
		oT = old.getMembers().iterator();
		cT = current.getMembers().iterator();
		// prime the pump
		advanceOld();
		advanceCurrent();
		while (o != null && c != null) {
			String oName = o.getNodeName();
			String cName = c.getNodeName();
			int i = oName.compareTo(cName);
			if (i == 0) {
				// same...diffNodeGroupAgainstInstanceList definitions
				if (o.getLastTouched() != c.getLastTouched()) {
					// only call this if the node has changed in some way.
					diffLikeNodes();
				}
				advanceOld();
				advanceCurrent();
			} else if (i < 0) {
				// o is smaller than c its a node that has been removed
				callback.node(o, Mode.Remove);
				advanceOld();
			} else {
				callback.node(c, Mode.Add);
				advanceCurrent();
			}
		}
		reportAllNodesAs(o, oT, Mode.Remove);
		reportAllNodesAs(c, cT, Mode.Add);
	}

	private void diffLikeNodes() {
		oI = o.getMemberDefinitions().iterator();
		cI = c.getMemberDefinitions().iterator();
		advanceOldDef();
		advanceCurrentDef();
		while (oD != null && cD != null) {
			String oName = oD.getName();
			String cName = cD.getName();
			int i = oName.compareTo(cName);
			if (i == 0) {
				// same...diffNodeGroupAgainstInstanceList definitions
				if (oD.getLastTouched() != cD.getLastTouched()) {
					// they are different
					callback.definition(c, oD, cD, Mode.Modify);
				}
				advanceOldDef();
				advanceCurrentDef();
			} else if (i < 0) {
				// o is smaller than c its a node that has been removed
				callback.definition(o, oD, null, Mode.Remove);
				advanceOldDef();
			} else {
				callback.definition(c, cD, null, Mode.Add);
				advanceCurrentDef();
			}
		}
		reportAllMembersAs(o, oD, oI, Mode.Remove);
		reportAllMembersAs(c, cD, cI, Mode.Add);
	}

	private void reportAllNodes(NodeGroupMember a, Iterator<NodeGroupMember> iter, Mode mode) {
		while (a != null) {
			callback.nodeDiff(a.getNodeName(), mode);
			reportAllDefinitionsAs(a, mode);
			if (iter.hasNext()) {
				a = iter.next();
			} else {
				a = null;
			}
		}
	}

	private void reportAllNodesAs(NodeGroupMember a, Iterator<NodeGroupMember> iter, Mode mode) {
		while (a != null) {
			callback.node(a, mode);
			reportAllDefinitionsAs(a, mode);
			if (iter.hasNext()) {
				a = iter.next();
			} else {
				a = null;
			}
		}
	}

	private void reportAllNodesAs(Iterator<JVS> iter, String name, Mode mode) {
		while (name != null) {
			callback.nodeDiff(name, mode);

			name = getNameFromIDIterator(iter);
		}
	}

	private void reportAllMembersAs(NodeGroupMember a, MemberDefinition d, Iterator<MemberDefinition> iter, Mode mode) {
		while (d != null) {
			callback.definition(a, d, null, mode);

			if (iter.hasNext()) {
				d = iter.next();
			} else {
				d = null;
			}
		}
	}

	private void reportAllDefinitionsAs(NodeGroupMember a, Mode mode) {
		for (MemberDefinition md : a.getMemberDefinitions()) {
			callback.definition(a, md, null, mode);
		}
	}

	private boolean advanceOld() {
		if (oT.hasNext()) {
			o = oT.next();
		} else {
			o = null;
		}
		return o != null;
	}

	private boolean advanceCurrent() {
		if (cT.hasNext()) {
			c = cT.next();
		} else {
			c = null;
		}
		return c != null;
	}

	private boolean advanceOldDef() {
		if (oI.hasNext()) {
			oD = oI.next();
		} else {
			oD = null;
		}
		return oD != null;
	}

	private boolean advanceCurrentDef() {
		if (cI.hasNext()) {
			cD = cI.next();
		} else {
			cD = null;
		}
		return cD != null;
	}

	private String getNameFromIDIterator(Iterator<JVS> idc) {
		if (idc != null && idc.hasNext()) {
			idc.next();
			JVS id = idc.next();
			return id.getString(IDef.serverIdKey);
		}
		return null;
	}

	public enum Mode {
		Add, Remove, Modify
	}
}
