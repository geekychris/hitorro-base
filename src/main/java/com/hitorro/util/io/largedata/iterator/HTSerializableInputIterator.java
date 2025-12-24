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
package com.hitorro.util.io.largedata.iterator;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.BaseSession;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectStreamFactory;
import com.hitorro.util.typesystem.HTSerializable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class HTSerializableInputIterator<T extends HTSerializable, S extends BaseSession> extends AbstractIterator<T> {
	private HTObjectInputStream m_is;

	private S m_session;

	private T m_curr;

	private boolean clearIdTracker = false;

	public HTSerializableInputIterator(BaseFile f, S session) throws IOException {
		init(f.getDataInputStream(), session, false);
	}

	public HTSerializableInputIterator(InputStream is, S session) {
		init(is, session, false);
	}

	public HTSerializableInputIterator(File f, S session, boolean clearIDTracker) throws IOException {
		init(FileUtil.getBufferedFileInputStream(f), session, clearIDTracker);
	}

	public HTSerializableInputIterator(InputStream is, S session, boolean clearIDTracker) {
		init(is, session, clearIDTracker);
	}

	private void init(InputStream is, S session, boolean clearIDTracker) {
		m_is = HTObjectStreamFactory.getInputStream(is, session);
		m_session = session;
		m_curr = nextAux();
		this.clearIdTracker = clearIDTracker;
	}

	public void close() throws Exception {
		m_is.rollback();
		m_is.close();
	}

	public boolean hasNext() {
		return m_curr != null;
	}

	public T next() {
		if (m_curr == null) {
			return null;
		}

		T t = m_curr;
		m_curr = nextAux();

		return t;
	}

	private T nextAux() {
		// flush because we assume these are self contained objects
		if (m_is == null) {
			return null;
		}
		try {
			T returnMe = (T) m_is.readVersionedObject();
			if (clearIdTracker) {
				// optimization for when you dont have any reference objects in the stream
				m_is.clearIdTracker();
			}
			return returnMe;
		} catch (IOException e) {
			Log.util.error("%s %e", e, e);
		} catch (ClassNotFoundException e) {
			Log.util.error("%s %e", e, e);
		} catch (StoreException e) {
			Log.util.error("%s %e", e, e);
		}
		return null;

	}

	public void remove() {
		// not implemented
	}
}
