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


package com.hitorro.base.objects;

import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.EnqueueElement,
		isView = false,
		isPersisted = false,
		schemaVersion = EnqueueElement.SerializationVersion)
public class EnqueueElement<T extends HTSerializable> implements HTSerializable {
	public static final int SerializationVersion = 1;

	private String queueName;
	private List<T> list = new ArrayList();
	private boolean flush = false;

	public boolean getFlush() {
		return flush;
	}

	public void setFlush(boolean flag) {
		flush = flag;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String name) {
		queueName = name;
	}

	public List<T> getElements() {
		return list;
	}

	public void setElements(List<T> list) {
		this.list = list;
	}

	public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
		os.writeString(queueName);
		os.writeListOfHTSerializable(list);
		os.writeBoolean(flush);
	}

	public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
		queueName = os.readString();
		os.readListOfHTSerializable(list);
		flush = os.readBoolean();
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
}
