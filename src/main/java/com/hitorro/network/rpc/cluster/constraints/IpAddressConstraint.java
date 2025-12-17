package com.hitorro.network.rpc.cluster.constraints;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.network.rpc.cluster.IDef;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

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
@TypeClassMetaInfo(shortTypeName = "clustIpAddrConst",
		isView = false,
		isPersisted = false,
		schemaVersion = IpAddressConstraint.SerializationVersion)
public class IpAddressConstraint extends AbstractInstanceDefinitionConstraint {
	public static final int SerializationVersion = 1;
	private String name;

	public IpAddressConstraint() {

	}

	public IpAddressConstraint(String nameIn) {
		name = nameIn;
	}

	public String toString() {
		return Fmt.S("IpAddressConstraint(%s)", name);
	}

	public boolean test(JVS id) {
		return name.equalsIgnoreCase(id.getString(IDef.ipAddressKey));
	}
}
