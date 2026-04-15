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

import com.hitorro.base.service.BasicService;
import com.hitorro.network.rpc.cluster.ClusterService;
import com.hitorro.network.rpc.cluster.IDef;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.servicecounters.CounterSetServlet;
import com.hitorro.util.startupframework.phases.ServiceDefinition;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


/**
 * Service which provides basic HTTP servlets.
 *
 * @author chris
 */

@ServiceDefinition(dependentService = {BasicService.class},
		shortName = "servlet",
		description = "Run a basic servlet",
		debugCommands = {}
)
public final class ServletService {

	/**
	 * the port number we listen on
	 */
	public static StringProperty HttpRootPath = new StringProperty("defaulthttp.rooturl", "number for http port", null);
	// our special contexts
	private static final String InternalContext = "internal";
	private static final String ExternalContext = "external";
	public static Server _server = null;
	private static ServletService service;
	HandlerCollection handlerCollection = null;
	private int portNumber;
	private Map<String, ServletContextHandler> _contextMap = null;
	private final ActionServlet actionServletExternal = new ActionServlet(false);
	private final ActionServlet actionServletInternal = new ActionServlet(true);
	private final IterCommandServlet iterCommandServlet = new IterCommandServlet();

	public static ServletService getService() {
		return service;
	}

	private static SslContextFactory.Server configureSsl() {
		SslContextFactory.Server sslCtxFactory = new SslContextFactory.Server();
		sslCtxFactory.setKeyStorePath("C:/SoftwareDMEO/Agent/keystore.jks");
		sslCtxFactory.setKeyStoreType("JKS");
		sslCtxFactory.setKeyStorePassword("password");
		sslCtxFactory.setKeyManagerPassword("password");
		return sslCtxFactory;
	}

	@CommandDef(command = "http.portnumber", description = "port number of the http service", isInternal = false)
	public int getPortNumber() {
		return portNumber;
	}

	public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
		boolean doSsl = false;
		service = this;
		portNumber = Env.getHTTPPort();
		_server = new Server();

		SslContextFactory.Server sslCtxFactory = null;
		if (doSsl) {
			sslCtxFactory = configureSsl();
		}

		HttpConfiguration httpConfiguration = new HttpConfiguration();
		httpConfiguration.setSecurePort(portNumber);
		httpConfiguration.setSecureScheme("https");
		httpConfiguration.addCustomizer(new SecureRequestCustomizer());


		ServerConnector serverConnector = null;

		if (doSsl) {
			serverConnector = new ServerConnector(
					_server,
					new SslConnectionFactory(sslCtxFactory, "http/1.1"),
					new HttpConnectionFactory(httpConfiguration));
		} else {
			serverConnector = new ServerConnector(
					_server,
					new HttpConnectionFactory(httpConfiguration));
		}

		serverConnector.setPort(portNumber);

		_server.setConnectors(new Connector[]{serverConnector});

		_contextMap = new HashMap<String, ServletContextHandler>();

		// set up the default contexts
		setupContexts();
		setupServlets();

		addPortsForCluster();

		return null;
	}

	private void setupContexts() {
		addServletContext(InternalContext);
		addServletContext(ExternalContext);
	}

	private void addPortsForCluster() {
		IDef id = ClusterService.getThisInstanceDefinition();
		if (ServletService.getService() != null) {
			id.addPortMap(ClusterService.HttpPortKey, Env.getHostIP(), getPortNumber());
		}
	}

	public String start(boolean dbInit) {
		try {
			_server.start();
		} catch (Exception e) {
			return e.getMessage();
		}
		if (_server != null && _server.isStarted()) {
			// we'll allow multiple attempts to start, but skip all but the first
			return null;
		}

		if (_server == null) {
			String message = "starting ServletService with no server";
			Log.servlet.fatal(message);
			return message;
		}

		return null;
	}

	private void setupServlets() {
		// put the always-present internal ping servlet
		addInternalServlet(new PingServlet(), "/ping/*");
		// and the command servlet
		addInternalServlet(new DebugCommandServlet(), StringUtil.strcat("/", DebugCommandServlet.UrlFragment, "/*"));
		// file reader
		addInternalServlet(new FileReadServlet(), "/public/*");
		addInternalServlet(new DebugCommandServlet(), "/dbg/*");
		addInternalServlet(new TestServlet(), "/test/*");
		addInternalServlet(new CounterSetServlet(), "/counters/*");

		addExternalServlet(actionServletExternal, "/action/*");
		addInternalServlet(actionServletInternal, "/action/*");

		addInternalServlet(iterCommandServlet, "/iteraction/*");
		addExternalServlet(iterCommandServlet, "/iteraction/*");
	}

	/**
	 * Add a servlet to the internal compContext space. This adds a servlet that is accessed with /internal.  So, for
	 * instance, if the contextPath is /foo then the servlet will be accessed with /internal/foo.
	 * <p/>
	 * This method must be called before the server is started.
	 *
	 * @param srv         The servlet to be added
	 * @param contextPath Context path of the servlet.
	 * @return an error string, or null if no error
	 */
	public String addInternalServlet(Servlet srv, String contextPath) {
		return addServlet(srv, InternalContext, contextPath);
	}

	/**
	 * Add a new servlet compContext. This will create a servlet compContext, which must be done before calls to addServlet.
	 * This routine should be called by a service at init() time.
	 *
	 * @param name the name of the servlet compContext - should be usable in a URL
	 * @return an error message or null if everything is ok.
	 */
	public String addServletContext(String name) {
		if (_contextMap.get(name) != null) {
			return StringUtil.strcat("Duplicate servlet compContext: ", name);
		}
		ServletContextHandler ctxt = new ServletContextHandler(getHandlerCollection(), StringUtil.strcat("/", name), ServletContextHandler.SESSIONS);

		_contextMap.put(name, ctxt);
		addHandlerPrivate(ctxt);

		return null;
	}

	public String addExternalServlet(Servlet srv, String contextPath) {
		return addServlet(srv, ExternalContext, contextPath);
	}

	public String addInternalServlet(Servlet srv, String contextPath, ServletContextHandler c) {
		return addServlet(srv, InternalContext, contextPath, c);
	}

	public String addExternalFilter(Filter srv, String contextPath, EnumSet<DispatcherType> dispatches, Map initParams) {
		return addFilter(srv, ExternalContext, contextPath, dispatches, initParams);
	}

	public String addInternalFilter(Filter srv, String contextPath, EnumSet<DispatcherType> dispatches, Map initParams) {
		return addFilter(srv, InternalContext, contextPath, dispatches, initParams);
	}

	/**
	 * Add a servlet to a compContext space. This adds a servlet that is accessed with /xx where "xx" is the name of the
	 * compContext space.  So, for instance, if the contextPath is /foo then the servlet will be accessed with /xx/foo.
	 * <p/>
	 * This method must be called before the server is started.
	 *
	 * @param srv         The servlet to be added
	 * @param contextName the name of the compContext space
	 * @param contextPath Context path of the servlet.
	 * @return an error string, or null if no error
	 */
	public String addServlet(Servlet srv, String contextName, String contextPath) {
		Log.servlet.info("Adding servlet to compContext %s with contextPath %s", contextName, contextPath);
		String errmsg = insistServerStarting();
		if (errmsg == null) {
			ServletContextHandler ctxt = _contextMap.get(contextName);
			if (ctxt != null) {

				ctxt.addServlet(new ServletHolder(srv), contextPath);
			} else {
				errmsg = Fmt.S("Unrecognized compContext %s", contextName);
			}
		}

		return errmsg;
	}

	public ServletContextHandler getExternalContext() {
		return _contextMap.get(ExternalContext);
	}

	/**
	 * Add a servlet to a compContext space. This adds a servlet that is accessed with /xx where "xx" is the name of the
	 * compContext space.  So, for instance, if the contextPath is /foo then the servlet will be accessed with /xx/foo.
	 * <p/>
	 * This method must be called before the server is started.
	 *
	 * @param filter      The servlet to be added
	 * @param contextName the name of the compContext space
	 * @param contextPath Context path of the servlet.
	 * @return an error string, or null if no error
	 */
	public String addFilter(Filter filter, String contextName, String contextPath, EnumSet<DispatcherType> dispatches, Map initParams) {
		Log.servlet.info("Adding servlet to compContext %s with contextPath %s", contextName, contextPath);
		String errmsg = insistServerStarting();
		if (errmsg == null) {
			ServletContextHandler ctxt = _contextMap.get(contextName);
			//XXX What todo with 9.0???
			// ch.setInitParams(initParams);

			if (ctxt != null) {

				FilterHolder fh = new FilterHolder(filter);
				if (!MapUtil.nullOrEmpty(initParams)) {
					fh.setInitParameters(initParams);
				}
				ctxt.addFilter(fh, contextPath, dispatches);
			} else {
				errmsg = Fmt.S("Unrecognized compContext %s", contextName);
			}
		}

		return errmsg;
	}

	public String addServlet(Servlet srv, String contextName, String contextPath, ServletContextHandler ctxt) {
		Log.servlet.info("Adding servlet to compContext %s with contextPath %s", contextName, contextPath);
		String errmsg = insistServerStarting();
		if (errmsg == null) {

			if (ctxt != null) {
				ctxt.addServlet(new ServletHolder(srv), contextPath);
			} else {
				errmsg = Fmt.S("Unrecognized compContext %s", contextName);
			}
		}

		return errmsg;
	}

	private void addHandlerPrivate(final Handler wc) {
		getHandlerCollection().addHandler(wc);
	}

	private HandlerCollection getHandlerCollection() {
		if (handlerCollection == null) {
			handlerCollection = new HandlerCollection();
			_server.setHandler(handlerCollection);
		}
		return handlerCollection;
	}

	/**
	 * Generate error message to be used if server isn't in right state for adding content.
	 *
	 * @return the error message if there is a problem, or null if all is ok
	 */
	private String insistServerStarting() {
		if (_server == null) {
			return "Cannot put servlet because servlet server doesn't exist";
		} else if (_server.isRunning()) {
			return "Cannot put servlet because servlet server is running";
		} else {
			return null;
		}
	}

	public String deInit() {
		if (_server != null) {
			try {
				_server.stop();
			} catch (Exception exc) {
				String excs = exc.toString();
				Console.println("Servlet stop failed with ", excs);
				return excs;
			} finally {
				_server = null;
			}
		}
		return null;
	}
}

class BlockingServlet extends HttpServlet {

	protected void doGet(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("{ \"status\": \"ok\"}");
	}
}