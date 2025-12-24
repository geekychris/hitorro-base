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
package com.hitorro.base.docprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.base.docprocessing.blockqueue.QueueService;
import com.hitorro.base.docprocessing.docmap.DocToBag;
import com.hitorro.base.docprocessing.docmap.DocToBagContext;
import com.hitorro.base.docprocessing.docmap.DocToBagFactory;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.crawler.BaseFileCrawlerCallback;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.Bag;
import com.hitorro.util.typesystem.HTSerializable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class QueueLoaderBaseFileCrawlerCallback extends BaseFileCrawlerCallback {
	private final EnqueueClient client;
	private final DocToBagContext d2bcontext;
	private final String bagType;
	private int counter = 0;

	public QueueLoaderBaseFileCrawlerCallback(String queueName, String bagType) {
		d2bcontext = DocToBagFactory.d2b.getContext();
		client = new EnqueueClient(QueueService.EnqueueKey, queueName);
		this.bagType = bagType;
	}

	public int getProcessedCount() {
		return counter;
	}

	public void resetProcessedCount() {
		counter = 0;
	}

	@Override
	public boolean init(JsonNode node) {
		return true;
	}

	@Override
	public boolean start() {
		return false;
	}

	public boolean add(BaseFile bf) {
		String ext = bf.getExtension();
		DocToBag dtb = d2bcontext.get(ext);
		if (dtb == null) {
			// cant do anything, dont have a converter.

			return false;
		}
		try {
			dtb.setDocument(bf);
			Bag bag = Bag.getBagForType(bagType);
			if (d2bcontext.mapToBag(dtb, bag)) {

				List<HTSerializable> list = new ArrayList();
				list.add(bag);
				if (!client.send(list, false)) {
					Log.docmap.error("Unable to send, there may be no queue receiver");
				} else {
					counter++;
				}
			}
			return true;
		} catch (IOException e) {
			Log.docmap.error("Unable to load file %s into me:%s with error %s %s", bf, dtb.getClass().getCanonicalName(), e, e);
		} catch (StoreException e) {
			Log.docmap.error("Unable to load file %s into me:%s with error %s %s", bf, dtb.getClass().getCanonicalName(), e, e);
		} catch (ClassNotFoundException e) {
			Log.docmap.error("Unable to load file %s into me:%s with error %s %s", bf, dtb.getClass().getCanonicalName(), e, e);
		}
		return false;
	}

	@Override
	public void close() throws IOException {
		stop();
	}


}
