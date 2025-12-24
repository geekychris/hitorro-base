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
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.BaseSession;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTObjectStreamFactory;
import com.hitorro.util.typesystem.HTSerializable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class HTSerializableIteratorSink<T extends HTSerializable> implements OutputIteratorSink<T> {
	private boolean m_shouldCloseOnCompletion;
	private OutputStream m_os;
	private Iterator<T> m_iter;
	private BaseSession m_session = null;

	public void setSession(BaseSession session) {
		m_session = session;
	}

	public void setFile(BaseFile file) throws IOException {
		setOutput(file.getDataOutputStream(), true);
	}

	public void setOutput(OutputStream os, boolean shouldCloseOnCompletion) {
		m_os = os;
		m_shouldCloseOnCompletion = shouldCloseOnCompletion;
	}

	public void setIterator(Iterator<T> iter) {
		m_iter = iter;
	}

	public int sink() throws IOException {
		HTObjectOutputStream os = HTObjectStreamFactory.getOutputStream(m_os, null, false);
		int count = 0;
		try {
			while (m_iter.hasNext()) {
				os.clearIdTracker();
				HTSerializable pts = m_iter.next();
				if (pts == null) {
					Log.util.error("HTSerializable was null in iterable.");
				}
				os.writeVersionedObject(pts);
				count++;
			}
			os.writeVersionedObject(null);

		} catch (StoreException e) {
			Log.util.error("%s %e", e, e);
			throw new IOException(e.getMessage());
		}

		os.flush();
		if (m_shouldCloseOnCompletion) {
			m_os.close();
		}

		return count;
	}
}
