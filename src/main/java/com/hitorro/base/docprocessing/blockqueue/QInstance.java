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
package com.hitorro.base.docprocessing.blockqueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.base.docprocessing.EnqueueService;
import com.hitorro.base.docprocessing.ValueSourceDatePartitioner;
import com.hitorro.network.rpc.cluster.ClusterService;
import com.hitorro.network.rpc.cluster.IDef;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.tools.queue.writer.BaseFileToQueueStructureMover;
import com.hitorro.util.basefile.tools.queue.writer.BlockFileQueueWriter;
import com.hitorro.util.basefile.tools.queue.writer.DirectoryStepInterface;
import com.hitorro.util.basefile.tools.queue.writer.MergerDirectoryStep;
import com.hitorro.util.basefile.tools.queue.writer.SorterDirectoryStep;
import com.hitorro.util.basefile.tools.queue.writer.partitioners.WriterPartitioner;
import com.hitorro.util.basefile.tools.queue.writer.serializer.HTSerializableWriterWritable;
import com.hitorro.util.basefile.tools.queue.writer.serializer.WriterWritableInterface;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;
import com.hitorro.util.json.keys.BasefileProperty;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.ClassProperty;
import com.hitorro.util.json.keys.FileProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.LongProperty;
import com.hitorro.util.json.keys.StaticVarProperty;
import com.hitorro.util.typesystem.BaseSessionFactory;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class QInstance {
	public static final String Partitioner = "partitioner";
	public static ClassProperty PartitionerClass = new ClassProperty("class", "", ValueSourceDatePartitioner.class);
	public static ClassProperty WriterClass = new ClassProperty("writer.class", "", HTSerializableWriterWritable.class);

	public static BasefileProperty QueueDirectory = new BasefileProperty("queuedir", "", "/queue/twitter");
	public static IntegerProperty MaxObjectsCount = new IntegerProperty("maxobjects", "", 1000000);
	public static IntegerProperty MaxSecondsCount = new IntegerProperty("maxseconds", "", 60 * 5);
	public static LongProperty MaxBytesCount = new LongProperty("maxbytes", "", Constants.MBytes * 200);

	public static FileProperty TempDirectory = new FileProperty("tmpdir", "Queues temp directory", "${ht_home}/tmpqueuepath");
	public static BooleanProperty CompressOutput = new BooleanProperty("compress", "Compress the output using gzip", false);


	//XXX changed to BaseFileAccessingObjectFactory which moved away from sub classing....will need to figure
	//    out a better way to construct from configs.
	// Sorter
	public static StaticVarProperty<BaseFileAccessingObjectFactory> Sort_Factory = new StaticVarProperty("sorter.factory", "", false, null, BaseFileAccessingObjectFactory.class);
	public static StaticVarProperty<Mapper<BaseFile, AbstractIterator>> Sort_IterIn = new StaticVarProperty("sorter.inputmap", "me used for generating the iterator from a BF", false, null, Mapper.class);
	public static BooleanProperty Sort_RunInThread = new BooleanProperty("sorter.runthread", "", false);
	public static IntegerProperty Sort_MaxEntries = new IntegerProperty("sorter.maxentries", "", 1000000);


	// Merger
	public static StaticVarProperty<BaseFileAccessingObjectFactory> Merger_Factory = new StaticVarProperty("merger.factory", "", false, null, BaseFileAccessingObjectFactory.class);
	public static StaticVarProperty<Mapper<BaseFile, AbstractIterator>> Merger_IterIn = new StaticVarProperty("merger.inputmap", "me used for generating the iterator from a BF", false, null, Mapper.class);
	public static BooleanProperty Merger_RunInThread = new BooleanProperty("merger.runthread", "", false);
	private File tmpWriter;
	private BaseFile queueRoot;
	private BlockFileQueueWriter writer;
	private WriterPartitioner part;
	private String name;
	private int maxObjects;

	public String init(String name, JsonNode map) {
		this.setName(name);
		if (queueRoot == null) {
			// could be set already
			setQueueRoot(QueueDirectory.apply(map));
		}
		setTmpWriter(TempDirectory.apply(map));
		setMaxObjects(MaxObjectsCount.apply(map));
		FileUtil.ensureDirectoryExists(getTmpWriter());
		JsonNode partitionerMap = map.get(Partitioner);
		Class c = PartitionerClass.apply(partitionerMap);

		Class classWriter = WriterClass.apply(map);

		WriterWritableInterface writer = (WriterWritableInterface) ClassUtil.getInstanceSwallowError(classWriter, WriterWritableInterface.class);
		writer.setExtension(name);
		setPart((WriterPartitioner) ClassUtil.getInstanceSwallowError(c, WriterPartitioner.class), partitionerMap);
		if (getPart() == null) {
			return Fmt.S("Unable to to instantiate partitioner found class %s", c);
		}

		try {
			BaseFile tmpOutputBF = FileFileSystem.Root.getFile(getTmpWriter().getAbsolutePath());

			DirectoryStepInterface next = getMover(map, tmpOutputBF);

			// possibly get merger
			next = getMerger(map, tmpOutputBF, next, name);
			// possibly get sorter (note its most likely we can have a sorter without a merger, but we cant have a merger without a sorter unless we know the
			// queue writer outputs always in incrementing sort key order.
			next = getSorter(map, tmpOutputBF, next, name);
			setWriter(new BlockFileQueueWriter(BaseSessionFactory.getFactory().getSession(),
					writer,
					tmpOutputBF,
					MaxSecondsCount.apply(map),
					MaxBytesCount.apply(map),
					getMaxObjects(), getPart(),
					next));
		} catch (IOException e) {
			return e.getMessage();
		}

		QueueServiceEnqueueHandler handler = new QueueServiceEnqueueHandler(name, getWriter());
		EnqueueService.addHandler(name, handler);
		IDef id = ClusterService.getThisInstanceDefinition();
		id.addInstanceCapability(QueueService.EnqueueKey, "", "", name, true);
		return null;
	}

	private DirectoryStepInterface getMover(final JsonNode map, final BaseFile tmpOutputBF) {
		BaseFile tmpOutgoing = tmpOutputBF.getChild("out");
		DirectoryStepInterface next = new BaseFileToQueueStructureMover(CompressOutput.apply(map), tmpOutgoing, getQueueRoot(), false);
		return next;
	}

	private DirectoryStepInterface getSorter(final JsonNode map, final BaseFile tmpOutputBF, DirectoryStepInterface next, String extension) {
		if (Sort_Factory.apply(map) == null) {
			return next;
		}
		next = new SorterDirectoryStep(Sort_MaxEntries.apply(map),
				extension,
				Sort_IterIn.apply(map),
				Sort_Factory.apply(map),
				tmpOutputBF.getChild("sort"),
				next, Sort_RunInThread.apply(map));
		return next;
	}

	private DirectoryStepInterface getMerger(final JsonNode map, final BaseFile tmpOutputBF, DirectoryStepInterface next, String extension) {
		if (Merger_Factory.apply(map) == null) {
			return next;
		}

		next = new MergerDirectoryStep(extension,
				Merger_IterIn.apply(map),
				Merger_Factory.apply(map),
				tmpOutputBF.getChild("merger"),
				next,
				Merger_RunInThread.apply(map));
		return next;
	}

	public File getTmpWriter() {
		return tmpWriter;
	}

	public void setTmpWriter(final File tmpWriter) {
		this.tmpWriter = tmpWriter;
	}

	public BaseFile getQueueRoot() {
		return queueRoot;
	}

	public void setQueueRoot(final BaseFile queueRoot) {
		this.queueRoot = queueRoot;
	}

	public BlockFileQueueWriter getWriter() {
		return writer;
	}

	public void setWriter(final BlockFileQueueWriter writer) {
		this.writer = writer;
	}

	public WriterPartitioner getPart() {
		return part;
	}

	public void setPart(final WriterPartitioner part, JsonNode props) {
		this.part = part;
		part.initForPass(props);
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getMaxObjects() {
		return maxObjects;
	}

	public void setMaxObjects(final int maxObjects) {
		this.maxObjects = maxObjects;
	}
}
