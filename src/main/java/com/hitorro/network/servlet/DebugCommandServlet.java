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

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.network.servlet.ServletUtil.ValueWriter;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandRegistry;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.DebugCommandArg;
import com.hitorro.util.commandandcontrol.InfoLevel;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class DebugCommandServlet extends HttpServlet {
	// the url under "internal" where we are installed
	public static final String UrlFragment = "debug";

	// parameter we use to tell what command is being executed
	private static final String CommandParameter = "x_command";
	/**
	 *
	 */
	private static final long serialVersionUID = 28873620487087789L;

	private static boolean inProcess = false;

	private static void writeCommandArgumentPanel(HttpServletRequest request, HttpServletResponse response, Command dcommand)
			throws IOException {
		// write out the arguments of the command
		List<DebugCommandArg> arguments = dcommand.getArguments();
		ServletUtil.startForm(response, StringUtil.strcat("./", UrlFragment), ServletUtil.GetFormType);
		ServletUtil.writeFormHidden(response, CommandParameter, dcommand.getCommand());
		if (arguments != null && arguments.size() > 0) {
			for (DebugCommandArg arg : arguments) {
				String value = request.getParameter(arg.getName());
				ServletUtil.writeFormInput(response, arg.getName(), arg.getName(), value);
				ServletUtil.writeText(response, StringUtil.strcat(" (", arg.getDescription(), ")"));
				ServletUtil.writeBreak(response);
			}
		}
		ServletUtil.endForm(response, "Do");
	}

	private static void writeButtonToMain(HttpServletResponse response)
			throws IOException {

		ServletUtil.startForm(response, StringUtil.strcat("./", UrlFragment), ServletUtil.GetFormType);
		ServletUtil.writeFormHidden(response, CommandParameter, "commands");
		ServletUtil.endForm(response, "Show All");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (inProcess) {
			System.out.println("running command while already running it");
		}
		inProcess = true;
		String command = request.getParameter(CommandParameter);

		if (command != null) {
			// handle generic commands
			doCommandGet(request, response, command, null);
		} else {
			// special handling for the commands command, which gets the listFiles of commands
			// it needs special formatting for the first columns
            /*
            ValueWriter[] columnWriters = new ValueWriter[2];
            columnWriters[0] = new LinkToCommandWriter();
            doCommandGet(request, response, DumpCommands.CommandName, columnWriters);
            */
			ServletUtil.startHtmlResponse(response, "Debug Commands");

			// get the command names
			ReceiveCommandList commandLister = new ReceiveCommandList();
			CommandRegistry.listCommands(commandLister, new CommandSession());

			for (String commandName : commandLister.getCommandNames()) {
				Command dcommand = CommandRegistry.getRegistry().get(commandName);
				if (dcommand != null) {
					ServletUtil.writeHorizontalLine(response);
					ServletUtil.writeHeading(response, dcommand.getDescription(), 2);
					ServletUtil.writeBreak(response);
					writeCommandArgumentPanel(request, response, dcommand);
				}
			}

			ServletUtil.finishHtmlResponse(response);
		}

		inProcess = false;

	}

	private void doCommandGet(HttpServletRequest request, HttpServletResponse response, String command, ValueWriter[] columnWriters)
			throws IOException {
		ServletUtil.startHtmlResponse(response, StringUtil.strcat("Debug Command ", command));

		writeButtonToMain(response);
		ServletUtil.writeHorizontalLine(response);

		CommandRegistry reg = CommandRegistry.getRegistry();
		Command dcommand = reg.get(command);

		if (dcommand != null) {
			ServletUtil.writeHeading(response, dcommand.getDescription(), 1);
			ServletUtil.writeHorizontalLine(response);

			writeCommandArgumentPanel(request, response, dcommand);
			// write our content

			// create a CommandSession just to hold state...
			CommandSession csession = new CommandSession();

			// get the arguments
			JVS args = new JVS();
			List<DebugCommandArg> dargs = dcommand.getArguments();
			for (DebugCommandArg argdef : dargs) {
				String key = argdef.getName();
				String argValue = request.getParameter(key);
				try {
					args.set(key, argValue);
				} catch (PropaccessError propaccessError) {
				}
			}

			// and direct output to the servlet response
			ServletResponse sresponse = new ServletResponse(response, columnWriters);
			try {
				dcommand.execute(null, args, sresponse, csession);
			} catch (Exception e) {
				Console.println("Unable to execute command %s %e", e, e);
			}
		} else {
			ServletUtil.writeText(response, "Cannot find command ");
			ServletUtil.writeText(response, command);
		}

		ServletUtil.finishHtmlResponse(response);
	}

    /*
    private static class LinkToCommandWriter extends ValueWriter
    {
        public void write (HttpServletResponse response, String value)
            throws IOException
        {
            ServletUtil.writeLink(response, StringUtil.strcat("./", UrlFragment, "?", CommandParameter, "=", value), value);
        }
        
    }
    */

	private static class ReceiveCommandList extends Response {
		private final List<String> _commandNames;

		public ReceiveCommandList() {
			_commandNames = new ArrayList<String>();
		}

		public List<String> getCommandNames() {
			return _commandNames;
		}

		@Override
		public void addBannerRow(String row) {
		}

		@Override
		public void addStatusUpdateMessage(final String info, final int percentComplete) {
			// do nothing
		}

		@Override
		public void addInfo(InfoLevel level, String info) {
		}

		@Override
		public void addRow(Object... elements) {
			addRowArray(elements);
		}

		public void addRowArray(Object[] elements) {
			if (elements != null && elements[0] != null) {
				_commandNames.add(elements[0].toString());
			}
		}

		@Override
		public void end() {
		}

	}

	private static class ServletResponse extends Response {
		private final HttpServletResponse _httpR;
		private int _nColumns;
		private boolean _tableEnded;
		private final ValueWriter[] _writers;

		public ServletResponse(HttpServletResponse httpResponse, ValueWriter[] columnWriters) {
			_httpR = httpResponse;
			_writers = columnWriters;
		}

		@Override
		public void addBannerRow(String row) {
			try {
				ServletUtil.writeHeading(_httpR, row, 2);
			} catch (IOException ioe) {
				Log.servlet.error(ioe);
			}
		}

		@Override
		public void addStatusUpdateMessage(final String info, final int percentComplete) {
			// do nothing
		}


		@Override
		public void addInfo(InfoLevel level, String info) {
			try {
				if (!_tableEnded) {
					ServletUtil.endTable(_httpR);
				}
				ServletUtil.writeBoldText(_httpR, level.name());
				ServletUtil.writeText(_httpR, info);
			} catch (IOException ioe) {
				Log.servlet.error(ioe);
			}
		}

		@Override
		public void addRow(Object... elements) {
			addRowArray(elements);
		}

		public void addRowArray(Object[] elements) {
			try {
				ServletUtil.writeTableRow(_httpR, StringUtil.objectArrayToStringArray(elements), _writers, _nColumns);
			} catch (IOException ioe) {
				Log.servlet.error(ioe);
			}
		}

		@Override
		public void end() {
			try {
				if (!_tableEnded) {
					ServletUtil.endTable(_httpR);
				}
			} catch (IOException ioe) {
				Log.servlet.error(ioe);
			}
		}
	}
}

