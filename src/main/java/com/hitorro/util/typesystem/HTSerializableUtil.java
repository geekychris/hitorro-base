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
package com.hitorro.util.typesystem;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.io.largedata.iterator.HTSerializableInputIterator;
import com.hitorro.util.io.largedata.iterator.HTSerializableSink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;



public class HTSerializableUtil {

	public static BaseMapper<InputStream, AbstractIterator<? extends HTSerializable>> is2htser = new BaseMapper<java.io.InputStream, AbstractIterator<? extends HTSerializable>>() {
		public AbstractIterator<? extends HTSerializable> apply(java.io.InputStream is) {
			BaseSession sess = BaseSessionFactory.getFactory().getSession();
			boolean clearIdTracker = true;
			return new HTSerializableInputIterator(is, sess, clearIdTracker);
		}
	};
	public static BaseMapper<java.io.OutputStream, Sink<? extends HTSerializable>> os2htser = new BaseMapper<java.io.OutputStream, Sink<? extends HTSerializable>>() {
		public Sink<? extends HTSerializable> apply(java.io.OutputStream os) {
			return new HTSerializableSink(os);
		}
	};
	public static BaseMapper<File, AbstractIterator<? extends HTSerializable>> fs2htser = FileUtil.fsInputStream.combine(is2htser);
	public static BaseMapper<BaseFile, Sink<? extends HTSerializable>> bf2htser = BaseFileUtil.bf2outputstream.combine(os2htser);

	public static final byte[] getHTSerializableBuffer(HTSerializable pts, ByteArrayOutputStream baos) {
		try {
			baos.reset();
			HTObjectOutputStream os = new HTObjectOutputStreamImpl(null, baos,
					TypeManager.getTypeManager(),
					false);
			os.writeVersionedObject(pts);
			os.flush();

			return baos.toByteArray();
		} catch (IOException e) {
			Log.util.error("%s %e", e, e);
		} catch (StoreException se) {
			Log.util.error("%s %e", se, se);
		}
		return null;
	}

	public static final byte[] getHTSerializableBuffer(HTSerializable pts) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		return getHTSerializableBuffer(pts, baos);
	}

	public static final boolean writeHTSerializableToOutputStream(HTSerializable pts,
																  OutputStream os) {
		ObjectOutputStream oos = null;
		try {
			HTObjectOutputStream pos = new HTObjectOutputStreamImpl(null, os,
					TypeManager.getTypeManager(),
					false);
			pos.writeVersionedObject(pts);
			oos.flush();

			return true;
		} catch (IOException e) {
			Log.util.error("%s %e", e, e);
		} catch (StoreException se) {
			Log.util.error("%s %e", se, se);
		}
		return false;
	}

	public static HTSerializable readHTSerializableFromBuffer(InputStream isIn, BaseSession session) {
		try {

			HTObjectInputStream is = new HTObjectInputStreamImpl(isIn,
					TypeManager.getTypeManager(),
					session);


			return is.readVersionedObject();

		} catch (IOException e) {
			com.hitorro.util.core.Log.rpc.error("%s %e", e, e);
		} catch (ClassNotFoundException e) {
			Log.rpc.error("%s %e", e, e);
		} catch (StoreException e) {
			Log.rpc.error("%s %e", e, e);
		}
		return null;
	}

	public static HTSerializable readHTSerializableFromBuffer(byte[] buff, BaseSession session) {
		ByteArrayInputStream bais = new ByteArrayInputStream(buff);
		ObjectInputStream ois = null;
		try {
			HTObjectInputStream is = new HTObjectInputStreamImpl(bais,
					TypeManager.getTypeManager(),
					session);


			return is.readVersionedObject();

		} catch (IOException e) {
			com.hitorro.util.core.Log.rpc.error("%s %e", e, e);
		} catch (ClassNotFoundException e) {
			Log.rpc.error("%s %e", e, e);
		} catch (StoreException e) {
			Log.rpc.error("%s %e", e, e);
		}
		return null;
	}

}
