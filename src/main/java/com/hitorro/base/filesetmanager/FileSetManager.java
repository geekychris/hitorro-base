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
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Keep track of known sets of files including queues in the system, where they are, how to read from them and how they
 * generally behave.
 */
@ServiceDefinition(dependentService = {},
		shortName = "filesetmanager",
		description = "Maintains details of filesets",
		debugCommands = {},
		typeManagedClasses = {},
		uiDirectories = {},
		dependentServiceInterfaces = {})
public class FileSetManager {
	private static final HashMap<String, FileSet> sets = new HashMap();
	private static final HashMap<String, FileQueue> queues = new HashMap();

	private static final HashMap<String, SinkSet> sinks = new HashMap();


	public static FileQueue getQueue(String name) {
		return queues.get(name.toLowerCase());
	}

	public static FileSet getSet(String name) {
		return sets.get(name.toLowerCase());
	}

	public static SinkSet getSink(String name) {
		return sinks.get(name.toLowerCase());
	}

	/**
	 * Add a fileset, queue path and sinks to the management set
	 *
	 * @param path
	 */
	public static void addPath(String path) throws PropaccessError {
		JsonNode queues = JVSProperties.getProperties().get(path);
		addFileSets(queues.get("raw"));
		addFileSets(queues.get("processed"));

		addSink(queues.get("sinks"));
	}

	private static void addSink(JsonNode list) {
		Iterator<Map.Entry<String, JsonNode>> iter = list.fields();

		while (iter.hasNext()) {
			Map.Entry<String, JsonNode> km = iter.next();
			JsonNode map = km.getValue();
			String name = km.getKey().toLowerCase();

			SinkSet ss = new SinkSet(name, map);
			sinks.put(name, ss);
		}
	}

	private static boolean addFileSets(JsonNode list) {
		Iterator<Map.Entry<String, JsonNode>> iter = list.fields();

		while (iter.hasNext()) {
			Map.Entry<String, JsonNode> km = iter.next();
			JsonNode map = km.getValue();
			String name = km.getKey().toLowerCase();
			if (FileSet.IsQueue.apply(map)) {
				FileQueue fq = new FileQueue(name, map);
				sets.put(name, fq);
				queues.put(name, fq);
			} else {
				sets.put(name, new FileSet(name, map));
			}
		}
		return true;
	}

	public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
		try {
			addPath("filesetmanagement.default");
		} catch (PropaccessError propaccessError) {
			return null;
		}
		return null;
	}

	public String start(boolean dbInit) {
		return null;
	}

	public String run() {
		return null;
	}

	public String deInit() {
		return null;
	}
}
