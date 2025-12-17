package com.hitorro.util.typesystem;

import java.io.InputStream;
import java.io.OutputStream;

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
public class HTObjectStreamFactory {
	private static HTObjectStreamFactory factory = new HTObjectStreamFactory();

	public static void setFactory(HTObjectStreamFactory fact) {
		factory = fact;
	}

	public static HTObjectInputStream getInputStream(InputStream is, BaseSession session) {
		return factory.getInputStreamIntl(is, session);

	}

	public static HTObjectOutputStream getOutputStream(OutputStream os, BaseSession session, boolean includeContent) {
		return factory.getOutputStreamIntl(os, session, includeContent);

	}

	protected HTObjectInputStream getInputStreamIntl(InputStream is, BaseSession session) {
		return null;
	}

	protected HTObjectOutputStream getOutputStreamIntl(OutputStream os, BaseSession session, boolean includeContent) {
		return null;
	}
}
