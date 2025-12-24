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
package com.hitorro.base.docprocessing.docmap;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.typesystem.Bag;

import java.util.Date;
import java.util.HashMap;

/**
 * Thread based compContext for getting DocToBag converters.  1 converter of each type used is held in a local cache
 */
public class DocToBagContext {
	private final HashMap<String, DocToBag> map = new HashMap();
	private final DocToBagFactory factory;

	DocToBagContext(DocToBagFactory factory) {
		this.factory = factory;
	}

	public boolean mapToBag(DocToBag d2b, Bag bag) {
		try {
			bag.setValue(bag, "body", d2b.getContent());
			bag.setValue(bag, "title", d2b.getTitle());
			bag.setValue(bag, "author", d2b.getAuthor());
			bag.setValue(bag, "url", d2b.getSourceURL());
			bag.setValue(bag, "keywords", d2b.getKeywords());
			Date d = d2b.getCreationDate();
			if (d != null) {
				bag.setValue(bag, "authoreddate", d.getTime());
			}
			bag.setValue(bag, "created", System.currentTimeMillis());
			d2b.reset();
		} catch (Exception e) {
			Log.docmap.error("Unable to apply file error %s %e", e, e);
			return false;
		}
		return true;
	}

	public DocToBag get(String extension) {
		if (StringUtil.nullOrEmptyString(extension)) {
			return null;
		}
		extension = extension.toLowerCase();
		DocToBag d2b = map.get(extension);
		if (d2b != null) {
			return d2b;
		}
		d2b = factory.getMapperInstance(extension);
		if (d2b != null) {
			map.put(extension, d2b);
		}
		return d2b;
	}
}
