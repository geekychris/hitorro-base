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
package com.hitorro.base.docprocessing.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.base.docprocessing.QueueLoaderBaseFileCrawlerCallback;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.BaseFileSystem;
import com.hitorro.util.basefile.tools.crawler.BaseFileCrawler;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.json.keys.StringProperty;

/**
 * bags.loadtoqueue path=file://ht/docs/cheatsheets; (default queue is docs and that goes /hthome/queue/docs)
 */
@CommandDef(command = "bags.loadtoqueue", description = "Crawl a directory to find files to load into the provided queue.")
public class LoadDocsToQueue extends Command {
	@CommandArgument(required = true)
	public static StringProperty PathKey = new StringProperty("path", "file:// or file:// path to content", null);
	@CommandArgument(required = false)
	public static StringProperty QueueKey = new StringProperty("queue", "file:// or file:// path to content", "docs");
	@CommandArgument(required = false)
	public static StringProperty BagtypeKey = new StringProperty("bagtype", "", "webpage");

	public boolean execute(String rawValue, JsonNode args, Response response, CommandSession session) throws Exception {
		String path = PathKey.apply(args);
		String queue = QueueKey.apply(args);
		String docType = BagtypeKey.apply(args);
		BaseFile bf = BaseFileSystem.getBaseFileFromPath(path);
		QueueLoaderBaseFileCrawlerCallback callback = new QueueLoaderBaseFileCrawlerCallback(queue, docType);
		BaseFileCrawler crawler = new BaseFileCrawler();
		crawler.setRoot(bf);
		crawler.setCallback(callback);
		int i = crawler.execute();
		this.writeSuccess(response, "examined %s documents, processed: %s documents", i, callback.getProcessedCount());
		return true;
	}
}
