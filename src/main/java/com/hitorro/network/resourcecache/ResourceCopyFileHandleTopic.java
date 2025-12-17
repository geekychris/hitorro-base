package com.hitorro.network.resourcecache;

import com.hitorro.network.rpc.FileHandleTopic;
import com.hitorro.network.rpc.FileUpload;
import com.hitorro.network.rpc.RPCMessage;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.io.resourcecache.ResourceDirectoryVersionNode;
import com.hitorro.util.io.resourcecache.basefile.BaseFileResourceCache;
import com.hitorro.util.io.resourcecache.basefile.BaseFileResourceContext;
import com.hitorro.util.io.resourcecache.basefile.BaseFileResourceDirectoryVersionNode;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTSerializable;

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

public class ResourceCopyFileHandleTopic extends FileHandleTopic {
	public static final String ResourceCopyFileHandleTopicKey = "resourcecopy";

	public String getTopic() {
		return ResourceCopyFileHandleTopicKey;
	}

	public boolean processStream(RPCMessage returnMessage, FileUpload fu, HTObjectInputStream is)
			throws IOException, ClassNotFoundException {
		HTSerializable pts = fu.getFileUploadPayload();
		if (pts instanceof ResourceDirectoryVersionNode) {
			BaseFileResourceDirectoryVersionNode vn = (BaseFileResourceDirectoryVersionNode) pts;
			BaseFileResourceContext c = BaseFileResourceCache.getCache().getTempResourceContextWithBuild(vn.getResource(),
					vn.getMajor(),
					vn.getMinor(),
					vn.getPatch(),
					vn.getBuildNumber());
			BaseFile dir = c.getPath();
			copyFiles(dir, is);
			c.commit();
			return true;
		}
		return false;
	}


}