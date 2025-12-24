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
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * User: chris
 */
public class HTSelectTreeController extends BaseFileSelectTreeController {
	private boolean clearIdTracker = false;

	public HTSelectTreeController(final BaseFile dir, final BaseFile[] fList, final int maxPerIter, BaseFileAccessingObjectFactory factory,
								  final boolean deleteOnceMerged, final String finalFileExtension, boolean removeOriginalFile) throws IOException {
		super(dir, fList, maxPerIter, factory, deleteOnceMerged, finalFileExtension, removeOriginalFile);
	}

	/**
	 * Set to true if the objects contain references to other objects (this is bad from a scalablity perspective as the
	 * reference hashtable just keeps on growing.
	 *
	 * @param flag
	 */
	public void setClearIDTracker(boolean flag) {
		clearIdTracker = flag;
	}

	protected Iterator getIterator(final File fc) throws IOException {
		return new HTSerializableInputIterator(fc, null, clearIdTracker);
	}
}
