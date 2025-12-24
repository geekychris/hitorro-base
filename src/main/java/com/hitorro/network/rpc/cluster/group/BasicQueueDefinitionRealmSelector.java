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

import java.util.List;

public class BasicQueueDefinitionRealmSelector extends DefinitionRealmSelector {
	private int modMultiplier = 3;

	/**
	 * given new and removed nodes.  What one does in this case varies.
	 * <p/>
	 * Segmented queue: In the segmented queue case, there are many approaches one could take, the one taken for now is
	 * to simply re-assign without trying to keep the same pcky per node
	 *
	 * @param ng
	 * @param ngNew
	 * @param added
	 * @param removed
	 * @return
	 */
	public NodeGroup applyChanges(NodeGroup ng, NodeGroup ngNew, List<String> added, List<String> removed, List<String> same) {
		int adSize = added.size();
		int removedSize = removed.size();
		int modM = modMultiplier;
		if (adSize > 0 || removedSize > 0) {
			int totalNodes = adSize + same.size();
			modM = ensureBalancedMod(totalNodes, modM);
			int max = totalNodes * modM;
			int counter = 0;
			if (ngNew == null) {
				ngNew = ng.copy();
			}
			for (String node : removed) {
				ngNew.remove(node);
			}
			for (String node : added) {
				counter = addSegments(ngNew, node, modM, counter, totalNodes);

			}
			for (String s : same) {
				counter = addSegments(ngNew, s, modM, counter, totalNodes);
			}
		}

		return ngNew;
	}

	private int addSegments(final NodeGroup ngNew, final String node, final int modM, int counter, final int totalNodes) {
		NodeGroupMember ngm = ngNew.getOrAddNode(node);
		int[] parts = new int[modM];
		for (int i = 0; i < modM; i++) {
			parts[i] = counter + (totalNodes * i);
		}
		counter++;
		QueueDefinition qd = (QueueDefinition) ngm.get(realm);
		if (qd == null) {
			qd = new QueueDefinition();
			qd.setName(realm);
			ngm.addMemberDefinition(qd);
		}
		qd.setMod(totalNodes * modM);
		qd.setMods(parts);
		return counter;
	}

	private int ensureBalancedMod(final int totalNodes, int modM) {
		boolean oddNodes = totalNodes % 2 != 0;

		if (modMultiplier % 2 == 0) {
			if (!oddNodes) {
				modM = modM + 1;
			}
		} else {
			if (oddNodes) {
				modM = modM + 1;
			}
		}
		return modM;
	}

	public int getModMultiplier() {
		return modMultiplier;
	}

	public void setModMultiplier(final int modMultiplier) {
		this.modMultiplier = modMultiplier;
	}
}
