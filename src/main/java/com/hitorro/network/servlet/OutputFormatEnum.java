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

import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.json.JSONResponse;
import com.hitorro.util.commandandcontrol.xml.HTMLResponse;
import com.hitorro.util.commandandcontrol.xml.XMLResponse;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 */
public enum OutputFormatEnum {
	Json() {
		public Response getResponse(final Command dc, final OutputStream os, int refreshPeriod) throws IOException {
			return new JSONResponse(dc.getCommand(), os);
		}

	},
	Html() {
		public Response getResponse(final Command dc, final OutputStream os, int refreshPeriod) throws SAXException {
			return new HTMLResponse(dc.getCommand(), os, refreshPeriod);
		}
	},
	Xml() {
		public Response getResponse(final Command dc, final OutputStream os, int refreshPeriod) throws SAXException {
			return new XMLResponse(dc.getCommand(), os);
		}
	};

	public abstract Response getResponse(final Command dc, final OutputStream os, int refreshPeriod) throws SAXException, IOException;
}
