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


package com.hitorro.util.io.largedata;

import com.hitorro.util.basefile.filters.FileExtension;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.io.largedata.buckets.BucketWriter;
import com.hitorro.util.io.largedata.buckets.ContainerFlushNotification;
import com.hitorro.util.io.largedata.buckets.HTSerializableFileBucketWriter;
import com.hitorro.util.io.largedata.buckets.MaturingBucketWriter;
import com.hitorro.util.io.largedata.buckets.containers.BucketWriterContainer;
import com.hitorro.util.io.largedata.iterator.HTSelectTreeController;
import com.hitorro.util.io.largedata.iterator.HTSelectTreeIteratorConstructor;
import com.hitorro.util.io.largedata.iterator.HTSerializableInputIterator;
import com.hitorro.util.typesystem.BaseSessionFactory;
import com.hitorro.util.typesystem.HTSerializable;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Add elements to bucketization mechanism that will sort and dedupe and then forward the results onto something to
 * process them.
 * <p/>
 * User: chris
 */
public abstract class HTObjectLinearizer<T extends HTSerializable> extends MaturingBucketWriter<T> implements Runnable, ContainerFlushNotification<T> {
	public FileExtension serFilter = FileExtension.ser;

	protected String fileExtension = "ser";
	protected BaseFile directory;
	protected BaseFile tmpDirectory;
	protected int counter = 0;
	protected int sleepBetweenPolls = 60;
	protected Object notificationObject = new Object();
	protected int maxFilesBeforePreFileBasedMerge;
	protected BucketWriter<T> bw;
	protected BaseFileAccessingObjectFactory<T> fact;

	protected HTObjectLinearizer() {

	}

	public HTObjectLinearizer(final int maxLength, final int maxMaturitySecs, BaseFile directory,
							  int maxFilesBeforePreFileBasedMerge, String fileExtension, BaseFileAccessingObjectFactory fact) {
		init(maxLength, maxMaturitySecs, directory, maxFilesBeforePreFileBasedMerge, fileExtension, fact);
	}

	public void init(final int maxLength, final int maxMaturitySecs, final BaseFile directory, final int maxFilesBeforePreFileBasedMerge, final String fileExtension, final BaseFileAccessingObjectFactory fact) {
		this.fact = fact;
		bw = new HTSerializableFileBucketWriter<T>(maxLength, directory, fileExtension, fact);

		init(maxMaturitySecs, bw);
		this.directory = directory;
		this.directory.mkdir();
		tmpDirectory = directory.getChild("tmp");
		this.maxFilesBeforePreFileBasedMerge = maxFilesBeforePreFileBasedMerge;
		this.fileExtension = fileExtension;
		serFilter = new FileExtension(fileExtension, true);
		this.getContainer().addNotificationHandler(this);
	}

	private BaseFile getFile() {
		return directory.getChild("%s-%s.%s", System.currentTimeMillis(), counter++, fileExtension);
	}

	public abstract int processIterator(Iterator<T> iter) throws IOException, StoreException, ClassNotFoundException;

	public void flushNotify(BucketWriterContainer<T> o) {
		super.flushNotify(o);
		this.notifyFileAddition();
	}

	public int process() throws Exception {
		BaseFile[] list = directory.listFiles(serFilter);
		if (ArrayUtil.nullOrEmpty(list)) {
			// nothing todo
			return 0;
		}
		if (list.length == 1) {

			HTSerializableInputIterator iter = new HTSerializableInputIterator(list[0], BaseSessionFactory.getFactory().getSession());
			int rows = processIterator(iter);
			if (rows != -1) {
				Log.util.debug("Linearizer processed %s rows", rows);
				list[0].delete();
				return rows;
			}

		} else if (list.length > maxFilesBeforePreFileBasedMerge) {
			for (BaseFile f : list) {
				checkFile(f);
			}
			BaseFileAccessingObjectFactory factory = new BaseFileAccessingObjectFactory() {

				@Override
				public Object getObject() {
					return null;
				}

				@Override
				public Comparator getDefaultComparitor() {
					return null;
				}

				@Override
				public Object[] getArray(final int i) {
					return new Object[i];
				}

				@Override
				public Mapper<BaseFile, Sink> getBaseFileToSinkMapper() {
					return null;
				}

				@Override
				public Mapper<BaseFile, AbstractIterator> getBaseFileToChainingMapper() {
					return null;
				}
			};
			HTSelectTreeController controller = new HTSelectTreeController(tmpDirectory, list,
					maxFilesBeforePreFileBasedMerge, factory, true, fileExtension, true);

			BaseFile mergedFile = controller.merge();
			BaseFile fileName = getFile();
			if (mergedFile.renameTo(fileName)) {
				HTSerializableInputIterator iter = new HTSerializableInputIterator(fileName, BaseSessionFactory.getFactory().getSession());
				int rows = processIterator(iter);
				if (rows != -1) {
					Log.util.debug("Linearizer processed %s rows", rows);
					fileName.delete();
					return rows;
				}
			} else {
				return -1;
			}

		} else {
			for (BaseFile f : list) {
				checkFile(f);
			}
			// we have few enough elemenents to warrant not doing a pre-apply.
			HTSelectTreeIteratorConstructor constructor = new HTSelectTreeIteratorConstructor(BaseSessionFactory.getFactory().getSession());
			int rows = processIterator(constructor.getIterator(fact.getDefaultComparitor(), fact.getRowMerger(), list));
			if (rows != -1) {
				Log.util.debug("Linearizer processed %s rows", rows);
				for (BaseFile file : list) {
					file.delete();
				}
				return rows;
			}
		}
		return -1;
	}

	public void checkFile(BaseFile f) throws IOException {
		if (true) {
			return;
		}
		HTSerializableInputIterator iter = new HTSerializableInputIterator(f, BaseSessionFactory.getFactory().getSession());
		int i = 0;
		while (iter.hasNext()) {
			iter.next();
			i++;
		}
		Console.println("Checked out: %s %s", i, f);

	}


	public void run() {
		while (true) {
			Env.sleepNSeconds(sleepBetweenPolls, notificationObject);
			try {
				process();
			} catch (Exception e) {
				Log.util.error("Unable to process files %s %e", e, e);
			}
		}
	}

	/**
	 * Signal that there has been a new file written and that the linearizer should wakeup and check.
	 */
	public void notifyFileAddition() {
		synchronized (notificationObject) {
			notificationObject.notifyAll();
		}
	}

	public int getSleepBetweenPolls() {
		return sleepBetweenPolls;
	}

	public void setSleepBetweenPolls(final int sleepBetweenPolls) {
		this.sleepBetweenPolls = sleepBetweenPolls;
	}
}
