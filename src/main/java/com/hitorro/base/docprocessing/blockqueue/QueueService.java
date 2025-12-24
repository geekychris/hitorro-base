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

import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.core.params.JsonKeyMap;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

import java.util.ArrayList;
import java.util.List;


/**
 * Block queue service.  Provides a set of handlers to buffer and then persist within a queue a sequence of objects
 */
@ServiceDefinition(dependentService = {},
		shortName = "queue",
		description = "Queue service",
		debugCommands = {},
		typeManagedClasses = {},
		uiDirectories = {})
public class QueueService {
	public static final String EnqueueKey = "queue";
	public static final String ShortName = "QueueService";

	private static QueueService service;

	private final List<QInstance> queues = new ArrayList();

	public static QueueService getService() {
		return service;
	}

	public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
		service = this;
		List<JsonKeyMap> types = null;
		try {
			types = JVSProperties.getProperties().getSubMaps("docqueue");
		} catch (PropaccessError propaccessError) {
			return "Unable to init QueueService";
		}

		for (JsonKeyMap km : types) {
			QInstance q = new QInstance();
			String err = q.init(km.getKey(), km.getValue());
			if (!StringUtil.nullOrEmptyString(err)) {
				return err;
			}
			queues.add(q);
		}
		return null;
	}

	public String start(boolean dbInit) {
		return null;
	}

	public String deInit() {
		return null;
	}

}

