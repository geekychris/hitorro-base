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
import com.hitorro.util.typesystem.HTSerializable;

public abstract class MemberDefinition implements HTSerializable, Comparable<MemberDefinition> {
	protected String name;
	protected long lastTouched;
	private transient NodeGroupMember parent;

	public abstract MemberDefinition copy();

	/**
	 * remove self if currently enable since the NodeGroup diffNodeGroupAgainstInstanceList showed that we have gone
	 * away.
	 */
	public abstract void remove();

	public abstract void addUpdate();

	public String toString() {
		return Fmt.S("%s t:%s", name, lastTouched);
	}

	public NodeGroupMember getParent() {
		return parent;
	}

	public void setParent(final NodeGroupMember parent) {
		this.parent = parent;
	}

	public long getLastTouched() {
		return lastTouched;
	}

	/**
	 * Touch the current time for self and all its parents.
	 *
	 * @param time
	 */
	protected void touch(long time) {
		lastTouched = time;
		if (parent != null) {
			parent.touch(time);
		}
	}

	protected void touch() {
		touch(System.currentTimeMillis());
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public abstract MemberDefinitionType getMemberDefinitionType();

	public int compareTo(final MemberDefinition definition) {
		MemberDefinitionType t1 = getMemberDefinitionType();
		MemberDefinitionType t2 = definition.getMemberDefinitionType();
		if (t1.equals(t2)) {
			return name.compareTo(definition.name);
		}
		return t1.compareTo(t2);
	}

	public enum MemberDefinitionType {
		QueueDef
	}
}
