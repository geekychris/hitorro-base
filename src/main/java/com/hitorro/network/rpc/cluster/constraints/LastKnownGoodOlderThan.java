package com.hitorro.network.rpc.cluster.constraints;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.network.rpc.cluster.IDef;
import com.hitorro.util.core.HTAssert;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

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
@TypeClassMetaInfo(shortTypeName = "LastKnownGoodOlderThan",
		isView = false,
		isPersisted = false,
		schemaVersion = LastKnownGoodOlderThan.SerializationVersion)
public class LastKnownGoodOlderThan implements HTPredicate<JVS>, HTSerializable {
	public static final int SerializationVersion = 1;
	protected long time;
	protected long timeToCompare;

	public LastKnownGoodOlderThan() {

	}

	public LastKnownGoodOlderThan(long timeIn) {
		time = timeIn;
	}

	public boolean initFromMap(final JsonNode map) {
		// MUST IMPLEMENT
		HTAssert.assertThat(false, "LastKnownGoodOlderThan.initFromMap not implemented");
		return false;
	}

	public void initForPass() {
		timeToCompare = System.currentTimeMillis() - time;
	}

	public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
		os.writeInt(getSerializationVersion());
		os.writeLong(time);
	}

	public void deserialize(HTObjectInputStream os)
			throws IOException, ClassNotFoundException, StoreException {
		int version = os.readInt();
		switch (version) {
			case 1:
				time = os.readLong();
		}
	}

	public int getSerializationVersion() {
		return SerializationVersion;
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

	public boolean test(JVS instanceDefinition) {
		long t = instanceDefinition.getLong(IDef.lastTouchedKey);
		if (t == 0) {
			return false;
		}
		return t < timeToCompare;
	}
}
