package com.hitorro.network.rpc;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.io.IOUtil;
import com.hitorro.util.typesystem.HTObjectInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
public abstract class FileHandleTopic {
	public abstract String getTopic();

	public abstract boolean processStream(RPCMessage returnMessage,
										  FileUpload fu,
										  HTObjectInputStream is)
			throws IOException, ClassNotFoundException;


	/**
	 * Read all the files into the target directory.
	 * <p/>
	 * Format is: Long - file size (-1 if terminating stream) String - name of file as it falls under the target dir.
	 * Stream - file content.
	 *
	 * @param targetDir
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	protected int copyFiles(BaseFile targetDir, HTObjectInputStream is) throws IOException, ClassNotFoundException {
		long size = is.readLong();
		int count = 0;

		while (size >= 0) {
			String name = is.readString();
			BaseFile targetFile = targetDir.getChild(name);
			InputStream cis = is.getLimitedInputStream(size);
			targetFile.mkdir();
			OutputStream fos = targetFile.getDataOutputStream();
			IOUtil.copyStream(cis, fos);
			fos.flush();
			fos.close();
			count++;
			size = is.readLong();

		}
		return count;


	}
}
