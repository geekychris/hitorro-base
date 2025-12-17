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


package com.hitorro.base.docprocessing.docmap;

import java.util.HashMap;

/**
 * Provides a mapping from a such as a pdf file to a set of common fields that you would expect from a document.
 */
public class DocToBagFactory {
	public static DocToBagFactory d2b = new DocToBagFactory(new PDFDocToBag());

	private final HashMap<String, DocToBag> implsMap = new HashMap();

	public DocToBagFactory(DocToBag... impls) {
		for (DocToBag d2b : impls) {
			implsMap.put(d2b.getExtension().toLowerCase(), d2b);
		}
	}

	DocToBag getMapperInstance(String ext) {
		DocToBag d2b = implsMap.get(ext.toLowerCase());
		if (d2b == null) {
			return null;
		}
		return d2b.getCopy();
	}

	public DocToBagContext getContext() {
		return new DocToBagContext(this);
	}
}
