package com.hitorro.util.typesystem;

import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.startupframework.ServiceContext;
import com.hitorro.util.startupframework.phases.ServiceDefinition;
import com.hitorro.util.typesystem.commands.DumpType;
import com.hitorro.util.typesystem.commands.DumpTypes;

import java.util.List;

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

@ServiceDefinition(dependentService = {},
		shortName = "typesystem",
		description = "Type System service",
		debugCommands = {DumpTypes.class, DumpType.class},
		typeManagedClasses = {},
		uiDirectories = {},
		dependentServiceInterfaces = {})
public class TypeManagerService {
	public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
		TypeManager tm = TypeManager.getTypeManager();
		try {
			com.hitorro.util.startupframework.ServiceContext.getSC();
			List<Class> persistedClasses = com.hitorro.util.startupframework.ServiceContext.getPersistedClasses();
			for (Class c : persistedClasses) {
				tm.addType(c);
			}
			tm.initTypeObjects();
			//XXX type not getting set?
			tm.loadTypesFromProps(JVSProperties.getProperties());
			return "";
		} catch (Exception e) {
			// its ok here that we catch Exception.  we exit anyway!
			Log.typemanager.error("Error while configuring the type system %s %e", e, e);
			return e.getMessage();
		}
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

/*

Oh Meddazzaland, Medazzaland
I have a problem they said they could solve
Soon I won't speak I have no words left in me
I dream in pictures but the sound is muted
I have no way to understand what they say into Medazzaland
People are starting to talk but I don't hear them anymore
Now I can't see but I'm still able to think
Do I have any feeling left what are they saying about me
Do they really understand what's wrong I feel their hands on my skin
The time has come for them to begin I'm sinking deeper and deeper into Medazzaland
I can feel the scalpul on my skin it's cutting in deeper and deeper
I'm in Medazzaland, oh Medazzaland

-- Duran Duran Meddazzaland
 */
