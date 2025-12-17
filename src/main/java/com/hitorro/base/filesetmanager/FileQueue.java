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


package com.hitorro.base.filesetmanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.base.docprocessing.blockqueue.QInstance;
import com.hitorro.util.basefile.filters.IsDir;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.queue.reader.DirectoryVisitorIterator;
import com.hitorro.util.basefile.tools.queue.reader.GoodFileKeyWriter;
import com.hitorro.util.core.date.DateResolution;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.ParNestingIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.datefilters.DateRangeFilterIntf;
import com.hitorro.util.json.JSONUtil;
import com.hitorro.util.json.keys.BasefileProperty;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.DateProperty;

import java.io.IOException;
import java.util.Date;

/**
 * Extension of the FileSet that represents a queue proper.  Has all the accessors to manage iterating over a queue of
 * files. - Where it is - how to get an iterator of its values (possibly constrained queue time) - how to get that
 * iterator mapped to some other form, for example, a queue of tweets in json format mapped to bags
 */
public class FileQueue<T, MAPPED> extends FileSet<T, MAPPED> {
	public static final BasefileProperty PathKey = new BasefileProperty("path", "path of where the queue is");
	public static final BooleanProperty IsInboxManaged = new BooleanProperty("inbox", "data files initially go into the inbox directory", false);
	public static final DateProperty InceptDate = new DateProperty("inceptdate", "date that data started appearing there", DateResolution.json, new Date());
	protected HTPredicate<BaseFile> defaultLO;
	protected JsonNode queueMap;
	private final BaseFile path;
	private final Date inceptDate;
	private final boolean inboxManaged;


	public FileQueue(String name, JsonNode map) {
		super(name, map);
		path = PathKey.apply(map);
		inboxManaged = IsInboxManaged.apply(map);
		inceptDate = InceptDate.apply(map);

		defaultLO = new LogicalOrOperator(new IsDir(), fileNameFilter);
		queueMap = map.get("queuewriter");
	}

	public AbstractIterator<MAPPED> getMappedToIterator(String to) throws IOException {
		return getMappedToIterator(to, null, null, -1, null);
	}

	public AbstractIterator<MAPPED> getMappedToIteratorPar(String to, HTPredicate<MAPPED> oper, int thread, int inQSize, int outQSize, String name) throws IOException {
		return getMappedToIteratorPar(to, oper, null, null, -1, null, thread, inQSize, outQSize, name);
	}

	/**
	 * Get a queue instance that can be used for writing to this queue.  Takes the sub config that the QInstance object
	 * understands to initialize the queue.  Uses the path defined in the general queue parameters to overide the queue
	 * path.
	 *
	 * @return
	 */
	public QInstance getQueueInstance() {
		if (JSONUtil.nullOrEmpty(queueMap)) {
			return null;
		}
		QInstance instance = new QInstance();
		instance.setQueueRoot(path);
		instance.init(name, queueMap);
		return instance;
	}

	public HTPredicate<BaseFile> getDefaultLogicalOperator() {
		return defaultLO;
	}

	/**
	 * Take the underlying iterator of whatever the queue generates and apply it to some other form using the provided
	 * target me
	 *
	 * @param to
	 * @param lo
	 * @param rangeFilter
	 * @param refresh
	 * @param lgk
	 * @return
	 * @throws IOException
	 */
	public AbstractIterator<MAPPED> getMappedToIterator(String to, HTPredicate<BaseFile> lo, DateRangeFilterIntf rangeFilter,
														int refresh, GoodFileKeyWriter lgk) throws IOException {
		BaseMapper<T, MAPPED> mapper = getMapTo(to);
		if (mapper == null) {
			return null;
		}
		AbstractIterator<T> t = getInputIterator(lo, rangeFilter, refresh, lgk);
		if (t == null) {
			return null;
		}
		return t.map(mapper);
	}

	/**
	 * Take the underlying iterator of whatever the queue generates and apply it to some other form using the provided
	 * target me
	 *
	 * @param to
	 * @param lo
	 * @param rangeFilter
	 * @param refresh
	 * @param lgk
	 * @return
	 * @throws IOException
	 */
	public AbstractIterator<MAPPED> getMappedToIteratorPar(String to, HTPredicate<MAPPED> filter, HTPredicate<BaseFile> lo, DateRangeFilterIntf rangeFilter,
														   int refresh, GoodFileKeyWriter lgk,
														   int threads, int inQSize, int outQSize,
														   String name) throws IOException {
		BaseMapper<T, MAPPED> mapper = getMapTo(to);
		if (mapper == null) {
			return null;
		}
		return getInputIteratorPar(lo, filter, mapper, rangeFilter, refresh, lgk, threads, inQSize, outQSize, name);
	}

	public AbstractIterator<T> getInputIterator() throws IOException {
		return getInputIterator(null, null, -1, null);
	}

	/**
	 * Get an iterator of the underlying type T.
	 *
	 * @param lo
	 * @param rangeFilter
	 * @param refresh
	 * @param lgk
	 * @return
	 * @throws IOException
	 */
	public AbstractIterator<T> getInputIterator(HTPredicate<BaseFile> lo, DateRangeFilterIntf rangeFilter,
												int refresh, GoodFileKeyWriter lgk) throws IOException {
		AbstractIterator<BaseFile> dvi = getFileIterator(lo, rangeFilter, refresh, lgk);
		return dvi.nest(iterMapper);
	}

	/**
	 * Get an iterator of the underlying type T.
	 *
	 * @param lo
	 * @param rangeFilter
	 * @param refresh
	 * @param lgk
	 * @return
	 * @throws IOException
	 */
	public AbstractIterator<MAPPED> getInputIteratorPar(HTPredicate<BaseFile> lo, HTPredicate<MAPPED> filter,
														BaseMapper mapper,
														DateRangeFilterIntf rangeFilter,
														int refresh, GoodFileKeyWriter lgk,
														int threads, int queueSizeIn, int queueSizeOut, String name) throws IOException {
		AbstractIterator<BaseFile> dvi = getFileIterator(lo, rangeFilter, refresh, lgk);
		return new ParNestingIterator(dvi, iterMapper, mapper, filter,
				threads, queueSizeIn, queueSizeOut, name);
	}


	public AbstractIterator<T> getParFileIterator(HTPredicate<BaseFile> lo, DateRangeFilterIntf rangeFilter,
												  int refresh, GoodFileKeyWriter lgk) throws IOException {
		AbstractIterator<BaseFile> dvi = getFileIterator(lo, rangeFilter, refresh, lgk);
		return null;
	}

	public AbstractIterator<BaseFile> getFileIterator() throws IOException {
		return getFileIterator(null, null, -1, null);
	}

	/**
	 * Get an iterator of the base files possibly constrained by date range
	 *
	 * @param lo
	 * @param rangeFilter
	 * @param refresh
	 * @param lgk
	 * @return
	 * @throws IOException
	 */
	public AbstractIterator<BaseFile> getFileIterator(HTPredicate<BaseFile> lo, final DateRangeFilterIntf rangeFilter, final int refresh, final GoodFileKeyWriter lgk) throws IOException {
		if (lo == null) {
			lo = defaultLO;
		}
		AbstractIterator<BaseFile> dvi = new DirectoryVisitorIterator(path, refresh, lgk, lo, rangeFilter);
		return dvi;
	}

	public BaseFile getPath() {
		return path;
	}

	public Date getInceptDate() {
		return inceptDate;
	}

	public boolean isInboxManaged() {
		return inboxManaged;
	}
}

