package com.hitorro.network.rpc.cluster.group;

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
public interface NodeGroupDifferCallback {
	/**
	 * Called to report the addition, modification or removal of a node from the full diff mechanism.
	 *
	 * @param a
	 * @param mode
	 */
	void node(NodeGroupMember a, NodeGroupDiffer.Mode mode);

	/**
	 * Called by normal diff when two member definitions (e.g. queue assignment) has changed.
	 *
	 * @param a
	 * @param old
	 * @param current
	 * @param mode
	 */
	void definition(NodeGroupMember a, MemberDefinition old, MemberDefinition current, NodeGroupDiffer.Mode mode);

	/**
	 * Called by  #NodeGroupDiffer.diffNodeGroupAgainstInstanceList when looking to see which nodes are new or removed.
	 *
	 * @param nodeName
	 * @param mode
	 */
	void nodeDiff(String nodeName, NodeGroupDiffer.Mode mode);

}
