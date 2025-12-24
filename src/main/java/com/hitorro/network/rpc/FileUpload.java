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
package com.hitorro.network.rpc;

import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.FileUpload,
		isView = false,
		isPersisted = false,
		schemaVersion = FileUpload.SerializationVersion)
public class FileUpload implements HTSerializable {
	public static final int SerializationVersion = 1;
	private String m_fileName;
	private String m_topic;
	private HTSerializable fileUploadPayload;

	public FileUpload() {

	}

	public FileUpload(String filename, String topic, HTSerializable pts) {
		setFileName(filename);
		setTopic(topic);
		this.setFileUploadPayload(pts);
	}

	public String getFileName() {
		return m_fileName;
	}

	public void setFileName(String fileName) {
		m_fileName = fileName;
	}

	public String getTopic() {
		return m_topic;
	}

	public void setTopic(String topic) {
		m_topic = topic;
	}

	public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
		os.writeInt(getSerializationVersion());
		os.writeString(m_fileName);
		os.writeString(m_topic);
		os.writeVersionedObject(fileUploadPayload);
	}

	public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
		int version = os.readInt();
		m_fileName = os.readString();
		m_topic = os.readString();
		fileUploadPayload = os.readVersionedObject();
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

	public HTSerializable getFileUploadPayload() {
		return fileUploadPayload;
	}

	public void setFileUploadPayload(HTSerializable fileUploadPayload) {
		this.fileUploadPayload = fileUploadPayload;
	}
}
