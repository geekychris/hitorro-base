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


/*
    Copyright (c) 2006 - 2025 Chris Collins. All rights reserved.

    
    User: chris
*/

package com.hitorro.network.servlet;

import com.hitorro.util.core.Env;
import com.hitorro.util.core.string.StringUtil;
import jakarta.servlet.http.HttpServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Simple servlet that reads a file out of the build/public directory and echoes it to the response.
 *
 * @author chris
 */
public final class FileReadServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = -6313282113050658932L;

	private void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.charAt(0) != '/') {
			// can't find it
			response.sendError(404);
			return;
		}

		try {
			File root = Env.getBin();
			File readFile = new File(root, StringUtil.strcat("build", File.separator, "public", pathInfo));
			if (!readFile.exists()) {
				response.sendError(404);
				return;
			}

			int buffsize = 4044;
			byte[] mybuff = new byte[buffsize];
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(readFile), buffsize + 2);
			BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream(), buffsize + 2);
			while (true) {
				int nread = in.read(mybuff, 0, buffsize);
				if (nread > 0) {
					out.write(mybuff, 0, nread);
				}
				if (nread < buffsize) {
					break;
				}
			}
			out.flush();
			in.close();

		} catch (Exception exc) {
			response.sendError(404, "internal error");
		}
	}
}

