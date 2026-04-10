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
package com.hitorro.base.service;

import com.hitorro.base.docprocessing.commands.LoadDocsToQueue;
import com.hitorro.base.filesetmanager.FileSetManager;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.network.netevent.NetEventService;
import com.hitorro.network.rpc.RPCService;
import com.hitorro.network.rpc.cluster.ClusterService;
import com.hitorro.util.auth.AuthenticationService;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.EventListener;
import com.hitorro.util.core.events.LocalEventHub;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.thread.RestartableServiceDaemon;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.keys.FileProperty;
import com.hitorro.util.servicecounters.CounterService;
import com.hitorro.util.startupframework.CleanShutdownExitHandler;
import com.hitorro.util.startupframework.ServiceContext;
import com.hitorro.util.startupframework.ServiceWrapper;
import com.hitorro.util.startupframework.phases.ServiceDefinition;
import com.hitorro.util.startupframework.steps.InitDBEvents;
import com.hitorro.util.typesystem.HTObjectStreamFactory;
import com.hitorro.util.typesystem.HTObjectStreamFactoryBaseImpl;
import com.hitorro.util.urlhandlers.PlatformDIRUrlStreamHandler;
import org.quartz.JobExecutionException;

import java.io.File;
import java.util.List;


/**
 * Service which provides most basic functionality to the server. This is the place to put initializations common to all
 * services.
 *
 * @author chris
 */

@ServiceDefinition(dependentService = {RPCService.class, ClusterService.class, RestartableServiceDaemon.class, AuthenticationService.class,
		NetEventService.class, CounterService.class, FileSetManager.class,
		UtilityService.class},
		shortName = "basic",
		description = "Basic service",
		debugCommands = {LoadDocsToQueue.class},
		debugCommandClasses = {Env.class, ServiceContext.class, CommandSession.class},
		uiDirectories = {},
		dbInitShellScripts = {"${HT_BIN}/data/initdb/commandscripts/basicservice.scr"})
public class BasicService implements EventListener {
	public static final FileProperty RunScriptOnStartup = new FileProperty("startup.runscript", "Run script on startup of server");

	private CleanShutdownExitHandler m_shutdown = null;

	public String deInit() {
		return null;
	}

	public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
		// make our htbin protocol handler work
		PlatformDIRUrlStreamHandler.register();

		HTObjectStreamFactory.setFactory(new HTObjectStreamFactoryBaseImpl());
		Log.util.info("HTBIN: %s", Env.getBin().getAbsolutePath());
		Log.util.info("HTHOME: %s", Env.getHome().getAbsolutePath());
		// register self event handler
		LocalEventHub.get().addEventListener(this, ServiceContext.ServerUp);
		LocalEventHub.get().addEventListener(this, InitDBEvents.EventName);
		return null;
	}

	public String start(boolean dbInit) {
		// dont want to register the clean exit handler till we have an initialized system.
		m_shutdown = new CleanShutdownExitHandler();
		return null;
	}

	@Override
	public boolean event(final String topic, final String subTopic, final Object args) {
		if (topic.equals(ServiceContext.ServerUp)) {
			File scriptToRun = RunScriptOnStartup.apply();
			if (!FileUtil.nullOrNotExist(scriptToRun)) {
				try {
					CommandSession.executeToLog("run", Fmt.S("script=%s toresponse=true", scriptToRun.getAbsoluteFile()));
				} catch (JobExecutionException e) {
					Log.commands.error("Unable to execute script %s %s %e", scriptToRun, e, e);
				}
			}
		}
		if (topic.equals(InitDBEvents.EventName)) {
			List<ServiceWrapper> services = ServiceContext.getSC().getServices();
			for (ServiceWrapper sw : services) {
				String[] paths = sw.getDbInitShellScripts();
				if (!ArrayUtil.nullOrEmpty(paths)) {
					for (String path : paths) {
						String resolved = JVSProperties.getProperties().resolveJsonVariable(path);
						File f = new File(resolved);
						if (!f.exists()) {
							Log.servicecontext.fatal("Script file %s was not found during init. ", f);
							return false;
						}
						try {
							Log.servicecontext.info("Running script found in service %s file: %s", sw.getShortName(), f);
							CommandSession.executeToLog("run", Fmt.S("script=%s toresponse=true", f.getAbsoluteFile()));
						} catch (JobExecutionException e) {
							Log.commands.error("Unable to execute script %s %s %e", f, e, e);
						}
					}

				}

			}

		}

		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String eventName() {
		return "BasicServicesEvent";
	}

	@Override
	public boolean runAsync() {
		return false;
	}
}
