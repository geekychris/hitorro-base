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


package com.hitorro.base.docprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.filters.FileExtension;
import com.hitorro.util.basefile.filters.IsDir;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.BaseFileSystem;
import com.hitorro.util.basefile.tools.queue.reader.DirectoryVisitorIterator;
import com.hitorro.util.basefile.tools.queue.reader.GoodFileKeyWriter;
import com.hitorro.util.basefile.tools.queue.reader.StreamingDirectoryVisitorIterator;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.core.thread.RestartableService;
import com.hitorro.util.core.thread.RestartableServiceDaemon;
import com.hitorro.util.json.keys.FileProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.typesystem.BaseSession;
import com.hitorro.util.typesystem.BaseSessionFactory;

import java.io.File;
import java.io.IOException;

/**
 * Responsible for enqueueing to a docprocessor from some kind of block stream, could be file, could be just a straight
 * file.
 */
public class HDFSDocprocessEnqueue<T> implements EnqueueHandlerReader<T>, Runnable {

	public static final FileProperty LgkProperty = new FileProperty("lgkname", "", "${ht_home}/queuekeys/file.lgk");
	public static final StringProperty QueueRootProperty = new StringProperty("root", "", "file://${ht_home}/queue/docs");
	private String rootString;
	private BaseFile root;
	private EnqueueHandler<T> eh;
	private StreamingDirectoryVisitorIterator<T> iter;

	public boolean init(JsonNode node, EnqueueHandler<T> handler) throws IOException {
		eh = handler;

		File lgkFile = LgkProperty.apply(node);
		rootString = QueueRootProperty.apply(node);

		root = BaseFileSystem.getBaseFileFromPath(QueueRootProperty.apply(node));

		GoodFileKeyWriter lgk = new GoodFileKeyWriter(lgkFile);
		FileExtension filter = new FileExtension("ser", true);
		root.mkdir();
		DirectoryVisitorIterator dirvisiter = new DirectoryVisitorIterator(root, 30, lgk, new LogicalOrOperator(new IsDir(), filter));

		BaseSession session = BaseSessionFactory.getFactory().getSession();
		iter = new StreamingDirectoryVisitorIterator(dirvisiter, session);
		return true;
	}

	public boolean start() {
		RestartableService rs = new RestartableService("HDFSDocprocessEnqueue", "HDFSDocprocessEnqueue", 100, this, true);
		RestartableServiceDaemon.addService(rs);
		return true;
	}

	public void run() {
		while (iter.hasNext()) {
			T t = iter.next();
			if (t != null) {
				eh.enqueue(t);
			}
		}
	}
}
