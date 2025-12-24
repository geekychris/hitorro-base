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
package com.hitorro.util.typesystem.btadapter;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.core.map.LRUHashMap;
import com.hitorro.util.keystore.KeyStore;
import com.hitorro.util.typesystem.ProxyAdapter;

import java.io.IOException;

public class BaseAdapterItemCache<T> extends HashCache<String, T> {
	private final String adapterName;
	private String eventName;
	private KeyStore<T> store = null;
	private ProxyAdapter<T> proxy = null;
	private BaseAdapterItemCacheFetcher<T> fetcher = null;

	/**
	 * Constructor
	 *
	 * @param demandBasedCacheing if set to false, then all put, updates' get recognized as operations that need to be
	 *                            performed. If set to true, then deletes delete and updates delete immediately.
	 * @param nullFlyweight       not null if you want a null result from a fetch to not perform the getAux again
	 */
	public BaseAdapterItemCache(boolean demandBasedCacheing, T nullFlyweight,
								int maxCacheSize, String adapterName,
								String eventName, KeyStore<T> store,
								ProxyAdapter<T> proxy,
								BaseAdapterItemCacheFetcher<T> fetcher) {
		super(0, demandBasedCacheing, nullFlyweight, eventName, null);
		this.m_table = new LRUHashMap<Object, T>(maxCacheSize);
		this.adapterName = adapterName;
		this.store = store;
		this.proxy = proxy;
		this.fetcher = fetcher;
	}

	protected T getAux(String guid) {
		return fetcher.getAndMap(guid, adapterName, store, proxy);
	}

	protected void removeItem(Object key) {
		super.removeItem(key);
		if (store != null) {
			try {
				store.remove(key.toString());
			} catch (IOException e) {
				Log.util.error("Exception %s %e", e, e);
			}
		}
	}

	public String eventName() {
		return eventName;
	}
}

