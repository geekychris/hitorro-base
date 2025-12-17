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


package com.hitorro.network.rpc.test;

import com.hitorro.network.rpc.cluster.group.MemberDefinition;
import com.hitorro.network.rpc.cluster.group.NodeGroupDiffer;
import com.hitorro.network.rpc.cluster.group.NodeGroupDifferCallback;
import com.hitorro.network.rpc.cluster.group.NodeGroupMember;
import com.hitorro.util.core.Console;

class TestCallback implements NodeGroupDifferCallback {

	private final StringBuilder sb = new StringBuilder();

	public void node(NodeGroupMember a, NodeGroupDiffer.Mode mode) {
		Console.println("Node: %s is %s", a.getNodeName(), mode.name());
		Console.bprint(sb, "Node: %s is %s", a.getNodeName(), mode.name());
	}

	public void definition(NodeGroupMember a, MemberDefinition old, MemberDefinition current, NodeGroupDiffer.Mode mode) {
		Console.println("Nodes: %s member %s is %s", a.getNodeName(), old.getName(), mode.name());
		Console.bprint(sb, "Nodes: %s member %s is %s", a.getNodeName(), old.getName(), mode.name());
	}

	public void nodeDiff(String nodeName, NodeGroupDiffer.Mode mode) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void clear() {
		sb.setLength(0);
	}

	public String getBuff() {
		return sb.toString();
	}
}



