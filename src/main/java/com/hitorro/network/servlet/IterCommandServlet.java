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
package com.hitorro.network.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.NullIterator;
import com.hitorro.util.core.iterator.sinks.JsonSink;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.params.PropertiesUtil;
import com.hitorro.util.io.FileUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

/**
 *
 */
public class IterCommandServlet extends HttpServlet {
	public IterCommandServlet() {
		Log.util.info("hello");
	}

	public void init(ServletConfig config) {
		Log.util.info("init");
	}

	public ServletConfig getServletConfig() {
		Log.util.info("getServletConfig");
		return null;
	}

	protected void doHead(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws IOException {
		doRequest(httpServletRequest, httpServletResponse, RestOperations.Head);
	}

	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws IOException {
		doRequest(httpServletRequest, httpServletResponse, RestOperations.Post);
	}

	protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws IOException {
		doRequest(httpServletRequest, httpServletResponse, RestOperations.Put);
	}

	protected void doDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws IOException {
		doRequest(httpServletRequest, httpServletResponse, RestOperations.Delete);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doRequest(request, response, RestOperations.Get);
	}

	private long doRequest(HttpServletRequest req, HttpServletResponse res, RestOperations op) throws IOException {

		AbstractIterator<JsonNode> inputIter = NullIterator.me;
		if (op == RestOperations.Post) {
			inputIter = FileUtil.inputstream2JacksonjsonReader.apply(req.getInputStream());
		}
		Map map = req.getParameterMap();
		Enumeration headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			Object o = headerNames.nextElement();

			Console.println("header: %s %s", o, req.getHeader(o.toString()));
		}

		Enumeration attributeNames = req.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			Object o = attributeNames.nextElement();

			Console.println("attr: %s %s", o, req.getAttribute(o.toString()));
		}

		JsonNode node = PropertiesUtil.getJsonNodeFromStringArraysMap(map);
		String pi = req.getPathInfo();
		pi = pi.substring(1);
		IterCommand c = IterCommandContext.get().get(pi);
		if (c == null) {
			c = IterCommandContext.get().get("dummy");
		}
		Sink<JsonNode> sink = new JsonSink(res.getOutputStream());
		sink.start();
		c.execute(node, inputIter, sink);
		sink.stop();
		res.flushBuffer();
		return 0;
	}

	public String getServletInfo() {
		Log.util.info("getServletConfig");
		return null;
	}

	public void destroy() {
	}

	public enum RestOperations {
		Head, Post, Put, Get, Delete
	}
}
