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
import com.hitorro.network.rpc.cluster.ClusterService;
import com.hitorro.network.rpc.cluster.IDef;

public class NodeSpecificDifferCallback implements NodeGroupDifferCallback {
	private final JVS id;
	private final String name;

	public NodeSpecificDifferCallback() {
		id = ClusterService.me.getInstanceStub();
		name = id.getString(IDef.serverIdKey);
	}

	public void definition(NodeGroupMember a, MemberDefinition old, MemberDefinition current, NodeGroupDiffer.Mode mode) {
		if (a.getNodeName().equals(name)) {
			//its for this node!
			switch (mode) {
				case Add:
				case Modify:
					old.addUpdate();
					break;
				case Remove:
					old.remove();
					break;

			}
		}
	}

	public void node(NodeGroupMember a, NodeGroupDiffer.Mode mode) {
		// not implemented
	}

	public void nodeDiff(String nodeName, NodeGroupDiffer.Mode mode) {
		// not implemented.
	}
}
