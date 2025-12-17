package com.hitorro.network.servlet;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.JVSUtils;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.core.Log;
import com.hitorro.util.json.keys.EnumKey;
import com.hitorro.util.json.keys.IntegerProperty;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

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
public class ActionServlet extends HttpServlet {
	public static final IntegerProperty HtmlRefresh = new IntegerProperty("htmlrefresh", "if set respond with html rather than xml", 0);

	public static final EnumKey<OutputFormatEnum> OutputType = new EnumKey<OutputFormatEnum>("otype", "", OutputFormatEnum.values(), OutputFormatEnum.Json);

	/**
	 *
	 */
	private static final long serialVersionUID = -6313282113050658932L;
	private final boolean internal;

	public ActionServlet(boolean internal) {
		this.internal = internal;
	}

	protected long getLastModified(HttpServletRequest httpServletRequest) {
		try {
			return doRequest(httpServletRequest, null, RestOperations.LastModified);
		} catch (IOException e) {
			Log.util.error("Exception %s %e", e, e);
			return -1;
		}
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

	private long doRequest(HttpServletRequest request, HttpServletResponse response, RestOperations op) throws IOException {
		String verb = request.getPathInfo();
		Map<String, String[]> params = request.getParameterMap();
		InputStream is = null;
		String uri = request.getRequestURI();
		switch (op) {
			case Post:
			case Put:
				is = request.getInputStream();
				break;
		}
		Command dc = ServletUtil.computeArgs(verb, params, response, op);


		if (dc == null) {
			if (response != null) {
				response.setHeader("Cache-Control", "no-cache");
			}
			return -1;
		}
		if (!this.internal && dc.isInternal()) {
			Log.rpc.error("External access of internal command %s", verb);
			if (response != null) {
				response.setHeader("Cache-Control", "no-cache");
			}
			return -1;
		}
		OutputStream os;
		if (response == null) {
			// if we are a time request with no request, then all we need to do is send
			// the output to dev null and provide the time.
			os = new ByteArrayOutputStream();
		} else {
			os = response.getOutputStream();
		}

		try {
			Map<String, String> p = ServletUtil.getArgs(params);
			JVS jvs = JVSUtils.convertMapToJVS(p, new JVS());
			int refresh = HtmlRefresh.apply(jvs);
			OutputFormatEnum outputType = OutputType.apply(jvs);

			Response resp = outputType.getResponse(dc, os, refresh);
			resp.setHttpRequest(request);
			resp.setHttpResponse(response);
			CommandSession cs = new CommandSession();

			//response.addHeader("Cache-Control", "s-maxage=120, public");

			//response.setHeader("Expires", "-1");
			long time = dc.executeRestfull("", jvs, resp, cs, op, is);
			resp.end();
			os.flush();
			return time;
		} catch (Exception e) {
			Log.commands.error("%s %e", e, e);
		}
		return 0;
	}

}