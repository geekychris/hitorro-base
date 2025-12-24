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
package com.hitorro.network.rpc.cluster;

import com.hitorro.util.core.opers.HTPredicate;

public class ProtocolCapabilityKey {
	private HTPredicate<IDef> oper;
	private String uniqueName;

	public ProtocolCapabilityKey(String uniqueNameIn, HTPredicate<IDef> operIn) {
		setUniqueName(uniqueNameIn);
		setOper(operIn);
	}

	public HTPredicate<IDef> getOper() {
		return oper;
	}

	public void setOper(HTPredicate<IDef> oper) {
		this.oper = oper;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public int hashCode() {
		return uniqueName.hashCode();
	}

	public boolean equals(Object o) {
		return uniqueName.equals(o);
	}

	/**
	 * Factory type mechanism.  Key is responsible for the correct type of collection. Its anticipated that collections
	 * could get complex when we want to perform such things as: - "Deliver to all" - Send to 1 of each segment.
	 *
	 * @return new instance of a collection or its subtype.
	 */
	public RPCTargetCollection getTargetCollection() {
		return new RPCTargetCollection();
	}
}
