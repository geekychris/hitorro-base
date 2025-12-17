package com.hitorro.network.rpc.cluster.group;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

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
public class GroupSelector {
	private static final GroupSelector selector = new GroupSelector();

	private Predicate<JVS> masterGroupOperator;
	private NodeGroup currentNodeGroup = new NodeGroup();
	private Map<String, DefinitionRealmSelector> definitionConstraints = new HashMap<String, DefinitionRealmSelector>();
	private final NodeSpecificDifferCallback callback = new NodeSpecificDifferCallback();
	private final NodeGroupDiffer differ = new NodeGroupDiffer();

	public static GroupSelector getSelector() {
		return selector;
	}

	public Predicate<JVS> getMasterGroupOperator() {
		return masterGroupOperator;
	}

	public void setMasterGroupOperator(final Predicate<JVS> masterGroupOperator) {
		this.masterGroupOperator = masterGroupOperator;
	}

	public void setDefinitionConstraint(String name, DefinitionRealmSelector realm) {
		definitionConstraints.put(name, realm);
	}

	public Map<String, DefinitionRealmSelector> getDefinitionConstraints() {
		return definitionConstraints;
	}

	public void setDefinitionConstraints(final Map<String, DefinitionRealmSelector> definitionConstraints) {
		this.definitionConstraints = definitionConstraints;
	}

	public NodeGroup getCurrentNodeGroup() {
		return currentNodeGroup;
	}

	public void setCurrentNodeGroup(final NodeGroup currentNodeGroup) {
		this.currentNodeGroup = currentNodeGroup;
	}

	public synchronized void applyNewNG(NodeGroup ngNew) {
		differ.diff(currentNodeGroup, ngNew, callback);
		currentNodeGroup = ngNew;
	}

	/**
	 * Given a pre-filtered listFiles of instances (running servers), take a node group which may contain zero or more nodes,
	 * cross validating the nodes isInitialized in the ids listFiles.  For each Realm, consider rebalancing any node
	 * pcky.
	 *
	 * @param ng
	 * @param ids
	 * @return
	 */
	public NodeGroup computeNodeGroup(NodeGroup ng, List<JVS> ids) {
		NodeCallback callback = new NodeCallback();
		NodeGroupDiffer differ = new NodeGroupDiffer();
		Collections.sort(ids, JVS.identityComparator);
		Set<String> keys = definitionConstraints.keySet();
		List<JVS> result = new ArrayList();
		NodeGroup ngNew = null;
		for (String key : keys) {
			result.clear();
			DefinitionRealmSelector sel = definitionConstraints.get(key);
			Predicate<JVS> lo = sel.getDefinitionConstraint();
			for (JVS id : ids) {
				if (lo.test(id)) {
					result.add(id);
				}
			}
			differ.diffNodeGroupAgainstInstanceList(ng, result, callback);
			ngNew = sel.applyChanges(ng, ngNew, callback.getAdded(), callback.getRemoved(), callback.getSame());
			callback.clear();
		}
		return ngNew;
	}
}

class NodeCallback implements NodeGroupDifferCallback {

	private List<String> removed = new ArrayList<String>();
	private List<String> added = new ArrayList<String>();
	private List<String> same = new ArrayList<String>();

	public void node(NodeGroupMember a, NodeGroupDiffer.Mode mode) {

	}

	public void definition(NodeGroupMember a, MemberDefinition old, MemberDefinition current, NodeGroupDiffer.Mode mode) {
	}

	public void nodeDiff(String nodeName, NodeGroupDiffer.Mode mode) {
		switch (mode) {
			case Add:
				added.add(nodeName);
				break;
			case Remove:
				added.add(nodeName);
			case Modify:
				getSame().add(nodeName);
				break;
			default:
				Log.util.error("Unknown operation %s", mode.name());
		}
	}

	public void clear() {
		getRemoved().clear();
		getAdded().clear();
		getSame().clear();
	}

	public List<String> getRemoved() {
		return removed;
	}

	public void setRemoved(final List<String> removed) {
		this.removed = removed;
	}

	public List<String> getAdded() {
		return added;
	}

	public void setAdded(final List<String> added) {
		this.added = added;
	}

	public List<String> getSame() {
		return same;
	}

	public void setSame(final List<String> same) {
		this.same = same;
	}
}
