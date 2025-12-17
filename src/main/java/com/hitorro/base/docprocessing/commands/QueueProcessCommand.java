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


package com.hitorro.base.docprocessing.commands;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.basefile.filters.FileExtension;
import com.hitorro.util.basefile.filters.IsDir;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.BaseFileSystem;
import com.hitorro.util.basefile.tools.queue.reader.DirectoryVisitorIterator;
import com.hitorro.util.basefile.tools.queue.reader.GoodFileKeyWriter;
import com.hitorro.util.basefile.tools.queue.reader.StreamingDirectoryVisitorIterator;
import com.hitorro.util.basefile.tools.queue.reader.serializer.WalkerDeserializer;
import com.hitorro.util.basefile.tools.queue.reader.serializer.WalkerDeserializerFactory;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.datefilters.DateRangeFilter;
import com.hitorro.util.json.keys.DateProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.typesystem.BaseSession;
import com.hitorro.util.typesystem.BaseSessionFactory;
import com.hitorro.util.typesystem.BaseType;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

/**
 *
 */
public abstract class QueueProcessCommand extends Command {
	@CommandArgument(required = false)
	public static final DateProperty startDateKey = new DateProperty("startdate", "", null);
	@CommandArgument(required = false)
	public static final DateProperty endDateKey = new DateProperty("enddate", "", null);
	@CommandArgument(required = true)
	public static final StringProperty hdfsPath = new StringProperty("path", "hdfs path", null);


	public abstract boolean process(Iterator<BaseType> iter, final String rawValue,
									final JVS args, final Response response,
									final CommandSession session) throws IOException;

	@Override
	public boolean execute(final String rawValue, final JVS args, final Response response, final CommandSession session, RestOperations operation) throws Exception {
		Date start = startDateKey.apply(args);
		Date end = endDateKey.apply(args);
		String p = hdfsPath.apply(args);

		BaseFile bf = BaseFileSystem.getBaseFileFromPath(p);
		if (!bf.exists()) {
			this.writeSimpleError(response, "File or directory does not exist: %s", bf);
			return false;
		}
		boolean dir = bf.isDir();
		BaseSession sess = BaseSessionFactory.getFactory().getSession();
		Iterator iter;
		if (dir) {
			if (start == null) {
				this.writeSimpleError(response, "Start date not provided");
				return false;
			}
			if (end == null) {
				this.writeSimpleError(response, "end date not provided");
				return false;
			}
			File lgkFile = new File(Fmt.S("/tmp/%s.lgk", System.currentTimeMillis()));
			lgkFile.delete();
			GoodFileKeyWriter lgk = new GoodFileKeyWriter(lgkFile);
			FileExtension filter = new FileExtension("ser", true);
			DateRangeFilter drf = new DateRangeFilter(start, end);
			DirectoryVisitorIterator dirvisiter = new DirectoryVisitorIterator(bf, -1, lgk, new LogicalOrOperator(new IsDir(), filter), drf);


			iter = new StreamingDirectoryVisitorIterator(dirvisiter, sess);

		} else {
			String path = bf.getAbsolutePath();
			if (!path.endsWith("ser")) {
				this.writeSimpleError(response, "file is not a serialized format %s", bf);
				return false;
			}
			WalkerDeserializer deserializer = WalkerDeserializerFactory.getInstance().getDeserializer(bf.getExtension());
			Log.filesystem.info("Deserializing file: %s", bf);
			try {
				iter = deserializer.getIterator(bf, sess);
			} catch (IOException e) {
				Log.filesystem.error("Unable to get iterator for %s with error %s %e", bf, e, e);
				return false;
			}
		}
		return process(iter, rawValue, args, response, session);
	}
}
