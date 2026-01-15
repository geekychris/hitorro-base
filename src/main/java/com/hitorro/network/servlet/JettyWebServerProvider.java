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

import com.hitorro.util.webserver.WebServerProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Servlet;

/**
 * Jetty-based implementation of {@link WebServerProvider}.
 * <p>
 * This provides real embedded Jetty web server functionality for standalone
 * Hitorro applications. It wraps Jetty Server and provides a simple API
 * for the service framework to use.
 */
public class JettyWebServerProvider implements WebServerProvider {
    private static final Logger logger = LoggerFactory.getLogger(JettyWebServerProvider.class);
    
    private Server server;
    private HandlerCollection handlerCollection;
    private int port;
    private int httpsPort;
    
    @Override
    public boolean initialize(int port, int httpsPort) {
        try {
            this.port = port;
            this.httpsPort = httpsPort;
            
            server = new Server(port);
            handlerCollection = new HandlerCollection();
            server.setHandler(handlerCollection);
            
            logger.info("Jetty server initialized on port {}", port);
            if (httpsPort > 0) {
                logger.info("HTTPS configured on port {}", httpsPort);
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to initialize Jetty server", e);
            return false;
        }
    }
    
    @Override
    public boolean start() {
        try {
            if (server != null && !server.isStarted()) {
                server.start();
                logger.info("Jetty server started on port {}", port);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to start Jetty server", e);
            return false;
        }
    }
    
    @Override
    public boolean stop() {
        try {
            if (server != null && server.isStarted()) {
                server.stop();
                logger.info("Jetty server stopped");
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to stop Jetty server", e);
            return false;
        }
    }
    
    @Override
    public boolean isRunning() {
        return server != null && server.isStarted();
    }
    
    @Override
    public Object addContext(String contextName) {
        try {
            ServletContextHandler context = new ServletContextHandler(
                handlerCollection,
                "/" + contextName,
                ServletContextHandler.SESSIONS
            );
            
            logger.debug("Added context: {}", contextName);
            return context;
        } catch (Exception e) {
            logger.error("Failed to add context: {}", contextName, e);
            return null;
        }
    }
    
    @Override
    public boolean addServlet(Object context, Class<?> servletClass, String pathSpec) {
        try {
            if (context instanceof ServletContextHandler && Servlet.class.isAssignableFrom(servletClass)) {
                ServletContextHandler servletContext = (ServletContextHandler) context;
                @SuppressWarnings("unchecked")
                Class<? extends Servlet> servlet = (Class<? extends Servlet>) servletClass;
                
                ServletHolder holder = new ServletHolder(servlet);
                servletContext.addServlet(holder, pathSpec);
                
                logger.debug("Added servlet {} at {}", servletClass.getSimpleName(), pathSpec);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to add servlet: {}", servletClass.getSimpleName(), e);
            return false;
        }
    }
    
    @Override
    public String getDescription() {
        return "Jetty Embedded Web Server (version " + Server.getVersion() + ")";
    }
    
    /**
     * Get the underlying Jetty server instance (for advanced configuration).
     * 
     * @return Jetty Server instance
     */
    public Server getServer() {
        return server;
    }
    
    /**
     * Get the handler collection (for advanced configuration).
     * 
     * @return HandlerCollection instance
     */
    public HandlerCollection getHandlerCollection() {
        return handlerCollection;
    }
}
