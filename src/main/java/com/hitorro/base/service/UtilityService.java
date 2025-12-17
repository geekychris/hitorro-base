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


package com.hitorro.base.service;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.ano.ArgType;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.csv.CSVIterator;
import com.hitorro.util.io.csv.CSVSink;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

import java.io.IOException;


/**
 * Some basic utilities that get registered for things like data tools
 */

@ServiceDefinition(dependentService = {},
		shortName = "utils",
		description = "Utility service",
		debugCommands = {},
		debugCommandClasses = {},
		uiDirectories = {})
public class UtilityService {

	@CommandDef(command = "csv.tsv2csv", description = "convert a tsv file to csv")
	public static final String say(@DebugArgAno(keyName = "session",
										   description = "session",
										   defaultValue = "",
										   argType = ArgType.Session) CommandSession session,
								   @DebugArgAno(propType = StringProperty.class, keyName = "inputfile",
										   description = "source file", defaultValue = "a") String inputfile,
								   @DebugArgAno(propType = StringProperty.class, keyName = "outputfile",
										   description = "source file", defaultValue = "b") String outputfile) throws IOException {

		BaseFile bfIn = FileFileSystem.Root.getFile(inputfile);
		BaseFile outputfileBf = FileFileSystem.Root.getFile(outputfile);
		if (bfIn.exists()) {
			CSVIterator iter = BaseFileUtil.bf2tsvOrcsv.apply(bfIn);
			AbstractIterator ci = (AbstractIterator) iter;
			CSVSink sink = new CSVSink(iter.getColumnNames(), BaseFileUtil.bf2outputstream.apply(outputfileBf), '\t');
			int count = ci.sink(sink);

			return Fmt.S("converted %s rows", count);
		}
		return "could not find file";
	}

	public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {

		return null;
	}

	// CSV Tools

	public String start(boolean dbInit) {
		return null;
	}

	public String deInit() {
		return null;
	}

}
