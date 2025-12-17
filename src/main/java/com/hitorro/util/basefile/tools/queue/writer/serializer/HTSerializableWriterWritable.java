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


package com.hitorro.util.basefile.tools.queue.writer.serializer;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTObjectStreamFactory;
import com.hitorro.util.typesystem.HTSerializable;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class HTSerializableWriterWritable implements WriterWritableInterface<HTSerializable> {
	HTObjectOutputStream os;
	OutputStream oos;
	private String extension = "ser";

	public void applyCloseToFile(BaseFile f) throws IOException {
		OutputStream os = f.getDataOutputStreamAppend();
		os.write(HTObjectOutputStream.EndOfStream);
		os.flush();
		os.close();
	}

	public boolean open(BaseFile f) throws IOException {
		oos = new BufferedOutputStream(f.getDataOutputStream());
		os = HTObjectStreamFactory.getOutputStream(oos, null, false);
		return true;
	}

	public boolean close() throws IOException {
		if (os != null) {
			os.writeEnd();
			os.flush();
			oos.close();
			os = null;
			oos = null;
		}
		return true;
	}

	public boolean write(HTSerializable sd) throws IOException, StoreException {
		os.writeVersionedObject(sd);
		os.flush();
		return true;
	}

	public int getBytes(HTSerializable sd) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		HTObjectOutputStream os = HTObjectStreamFactory.getOutputStream(baos, null, false);
		try {
			baos.reset();
			os.writeVersionedObject(sd);
		} catch (StoreException e) {
			throw new IOException(e);
		}
		os.flush();
		baos.close();

		return baos.size();
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String ext) {
		extension = ext;
	}
}
