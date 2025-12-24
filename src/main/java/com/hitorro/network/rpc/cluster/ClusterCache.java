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
package com.hitorro.network.rpc.cluster;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.collection.BaseCollection;
import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.core.opers.HTPredicate;

public class ClusterCache {
	private final BaseCollection<String, JVS> cluster;
	private final ClusterCacheMapper mapper;
	private HashCache<HTPredicate<JsonNode>, ClusterResults> cache = null;

	public ClusterCache(BaseCollection<String, JVS> cluster) {
		this.cluster = cluster;
		this.mapper = new ClusterCacheMapper(cluster);
		cache = new HashCache<HTPredicate<JsonNode>, ClusterResults>(Constants.MillisInSecond, true, new ClusterResults(), "clustercache", mapper);
	}

}
