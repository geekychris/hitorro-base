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


package com.hitorro.network.servlet;

import com.hitorro.network.servlet.itercommands.SpewCommand;

import java.util.HashMap;

/**
 *
 */
public class IterCommandContext {

	private static final IterCommandContext context = new IterCommandContext();
	private final HashMap<String, IterCommand> map = new HashMap<String, IterCommand>();

	public IterCommandContext() {
		this.add(new SpewCommand());
	}

	public static IterCommandContext get() {
		return context;
	}

	public void add(IterCommand c) {
		map.put(c.getCommand().toLowerCase(), c);
	}

	public IterCommand get(String commandString) {
		return map.get(commandString.toLowerCase());
	}
}
