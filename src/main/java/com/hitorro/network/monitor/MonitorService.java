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
package com.hitorro.network.monitor;

import com.hitorro.network.netevent.AlertNetEventHandler;
import com.hitorro.network.netevent.NetEventHandler;
import com.hitorro.network.netevent.NetEventService;
import com.hitorro.network.rpc.cluster.ClusterService;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.thread.RestartableService;
import com.hitorro.util.core.thread.RestartableServiceDaemon;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.mail.MailAgent;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@ServiceDefinition(dependentService = {NetEventService.class},
		shortName = "monitor",
		description = "monitor service",
		debugCommands = {},
		typeManagedClasses = {},
		uiDirectories = {})
public class MonitorService implements Runnable {
	public static IntegerProperty InvokeWaitInSeconds =
			new IntegerProperty("monitor.invokewait",
					"Invocation wait period in seconds",
					60);

	public static BooleanProperty IsNetMonitor =
			new BooleanProperty("monitor.isnetmonitor",
					"",
					false);
	public static final String MonitorServiceSingleton = "monitor";
	private static final List<GenericKeyValue<MonitorValidator, MonitorAction>> watchList =
			new ArrayList<GenericKeyValue<MonitorValidator, MonitorAction>>();
	private static final List<String> messages = new ArrayList<String>();

	public static void addAlert(String msg) {
		synchronized (messages) {
			messages.add(msg);
		}
	}

	public static synchronized void addWatch(MonitorValidator validator, MonitorAction action) {
		watchList.add(new GenericKeyValue<MonitorValidator, MonitorAction>(validator, action));
	}

	private static synchronized void invokeActions() {
		for (GenericKeyValue<MonitorValidator, MonitorAction> va : watchList) {
			MonitorValidator mv = va.getKey();
			MonitorAction ma = va.getValue();
			mv.validate(ma);
		}
	}

	public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
		NetEventHandler.addHandler(new AlertNetEventHandler());
		return null;
	}

	public String start(boolean dbInit) {
		RestartableService rs = new RestartableService("monitorservice", "monitorservice", 100, this, true);
		RestartableServiceDaemon.addService(rs);
		return null;
	}

	public String deInit() {
		return null;
	}

	public void run() {
		int seconds = InvokeWaitInSeconds.apply();
		while (true) {
			Env.sleepNSeconds(seconds);
			invokeActions();
			if (ClusterService.getService().getAmINamedSingleton(MonitorServiceSingleton)) {
				File monitorDir = new File(Env.getHome(), "monitorevents");
				FileUtil.ensureDirectoryExists(monitorDir);
				//if I am the monitor server then I need to do something with the messages.
				StringBuilder sb = new StringBuilder();
				synchronized (messages) {
					for (String msg : messages) {
						long fp = FPHash64.getFP(msg);
						File f = new File(monitorDir, Fmt.S("%s.txt", fp));
						if (!f.exists()) {
							try {
								FileUtil.writeStringToFile(f, msg);
							} catch (FileNotFoundException e) {
								Log.util.error("Exception %s %e", e, e);
							}
							Console.bprintln(sb, msg);
						}
					}
				}
				if (sb.length() > 0) {
					MailAgent.email("chris@hitorro.com", "Network alerts", sb.toString(), MailAgent.MONITOR_PROPERTY_KEY_ROOT);
				}
			} else {
				synchronized (messages) {
					messages.clear();
				}
			}
		}
	}
}
