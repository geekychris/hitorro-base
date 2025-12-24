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

import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.InstanceCapability,
		isView = false,
		isPersisted = false,
		schemaVersion = InstanceCapability.SerializationVersion)
public class InstanceCapability implements HTSerializable, Comparable<InstanceCapability> {
	public static final int SerializationVersion = 1;
	private String capability;
	private String segment;
	private String versionLabel;
	private String comment;
	// if the resource has its own url
	private String internalUrl;
	// this url has to be resolved via some magic yet to be constructed.
	private String externalUrl;

	public String toString() {
		return StringUtil.strcat(capability, ":", segment);
	}

	public boolean isSingleton() {
		return capability.startsWith(ClusterService.SingletonString);
	}

	public String getCapability() {
		return capability;
	}

	public void setCapability(String capability) {
		this.capability = capability;
	}

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}

	public String getVersion() {
		return versionLabel;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getInternalUrl() {
		return internalUrl;
	}

	public void setInternalUrl(String internalUrl) {
		this.internalUrl = internalUrl;
	}

	public String getExternalUrl() {
		return externalUrl;
	}

	public void setExternalUrl(String externalUrl) {
		this.externalUrl = externalUrl;
	}

	public void setVersionLabel(String versionLabel) {
		this.versionLabel = versionLabel;
	}

	public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
		os.writeInt(getSerializationVersion());
		os.writeString(capability);
		os.writeString(segment);
		os.writeString(internalUrl);
		os.writeString(externalUrl);
		os.writeString(versionLabel);
		os.writeString(comment);
	}

	public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
		int version = os.readInt();
		capability = os.readStringInternalized();
		segment = os.readStringInternalized();
		internalUrl = os.readStringInternalized();
		externalUrl = os.readStringInternalized();
		versionLabel = os.readStringInternalized();
		comment = os.readStringInternalized();
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

	public int compareTo(final InstanceCapability instanceCapability) {
		int i = capability.compareTo(instanceCapability.capability);
		if (i == 0) {
			return segment.compareTo(instanceCapability.segment);
		}
		return i;
	}
}
