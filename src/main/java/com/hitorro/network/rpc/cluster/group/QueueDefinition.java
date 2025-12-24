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

import com.hitorro.util.core.Console;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.job.JobServiceBase;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;


@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.QueueDefinition,
		isView = false,
		isPersisted = false,
		schemaVersion = QueueDefinition.SerialVersion,
		disableFastAccessors = true)
public class QueueDefinition extends MemberDefinition {
	public static final int SerialVersion = 1;


	private int[] mods;
	private int mod;

	public QueueDefinition() {

	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Console.bprint(sb, "mod=%s mods=", mod);
		for (int i = 0; i < mods.length; i++) {
			if (i == 0) {
				Console.bprint(sb, "%s", mods[i]);
			} else {
				Console.bprint(sb, ",%s", mods[i]);
			}
		}
		return sb.toString();
	}

	public void remove() {
		JobServiceBase.disableQueue(name);
	}

	public void addUpdate() {
		JobServiceBase.enableQueue(name, mods, mod);
	}

	public MemberDefinition copy() {
		QueueDefinition q = new QueueDefinition();
		q.mod = mod;
		q.name = name;
		q.mods = new int[mods.length];
		q.lastTouched = lastTouched;

		System.arraycopy(mods, 0, q.mods, 0, mods.length);
		return q;
	}

	public MemberDefinitionType getMemberDefinitionType() {
		return MemberDefinitionType.QueueDef;
	}

	public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
		os.writeInt(getSerializationVersion());
		os.writeString(name);
		if (mods == null) {
			os.writeInt(0);
		} else {
			os.writeInt(mods.length);
			for (int i : mods) {
				os.writeInt(i);
			}
		}
		os.writeInt(mod);
	}

	public void deserialize(HTObjectInputStream os)
			throws IOException, ClassNotFoundException, StoreException {
		int version = os.readInt();
		name = os.readString();
		int size = os.readInt();
		mods = new int[size];
		for (int i = 0; i < size; i++) {
			mods[i] = os.readInt();
		}
		mod = os.readInt();
	}

	public int getSerializationVersion() {
		return SerialVersion;
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


	public int[] getMods() {
		return mods;
	}

	public void setMods(final int[] mods) {
		this.mods = mods;
		touch();
	}

	public int getMod() {
		return mod;
	}

	public void setMod(final int mod) {
		this.mod = mod;
		touch();
	}
}