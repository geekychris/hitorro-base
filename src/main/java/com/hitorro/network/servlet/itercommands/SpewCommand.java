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


package com.hitorro.network.servlet.itercommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.network.servlet.IterCommand;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.json.keys.IntegerProperty;

import java.io.IOException;

/**
 *
 */
public class SpewCommand implements IterCommand {
	private static final IntegerProperty DELAY_IN_SECS = new IntegerProperty("delaysecs", "", 10);

	public SpewCommand() {
	}

	public String getCommand() {
		return "spew.docs";
	}

	@Override
	public void execute(final JsonNode args, final AbstractIterator<JsonNode> nodes, final Sink<JsonNode> response) {
		if (nodes != null) {
			while (nodes.hasNext()) {
				Console.println("%s", nodes.next());
			}
		}
		int delay = DELAY_IN_SECS.apply(args);
		int counter = 0;
		while (true) {
			ObjectNode on = JsonNodeFactory.instance.objectNode();
			on.put("count", counter++);
			on.put("hello", "world");
			on.put("delay", delay);
			try {
				response.add(on);
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} catch (StoreException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			Env.sleepNSeconds(delay);
		}
	}
}

