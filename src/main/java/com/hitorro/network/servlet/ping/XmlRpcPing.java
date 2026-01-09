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
package com.hitorro.network.servlet.ping;

import com.hitorro.util.core.Log;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

/**
 * This class uses XML-RPC to ping integrators using a simple interface that can ping multiple vendors at one time. It
 * requires Apache's xml-rpc-1.2 code not Sun Microsystem's XML-RPC.
 * <p/>
 * NOTE:Since SUN Microsystem's API(s) insist on using SOAP as the transmission protocol but integrators wish to have
 * XML sent instead, you have to use the xmlrpc-1.2 code from Apache Foundation instead.
 * <p/>
 * <p/>
 * <p/>
 *
 * @author Bruce B. Werner
 * @version 0.9
 */
public class XmlRpcPing {
	private String postName;
	private String postURI;
	private String pingClient;
	private String pingMethod;

	public XmlRpcPing() {
		super();
	}

	/**
	 * This method does the ping after you've called PingIntegrator()
	 * <p/>
	 * This method gathers information necessary to do an XML-RPC ping.
	 * <p/>
	 *
	 * @param name   The name of the blog or blog entry, but that could cause problems with Technorati.
	 * @param URI    The URI of the post or blog you are sending
	 * @param client This is the client RPC name, usually something like "http://rpc.technorati.com/rpc/ping"
	 * @param method Check with your integrator for this, usually "weblogUpdates.ping"
	 * @return void
	 */
	public boolean doPing(String name, String URI, String client, String method) {
		try {
			postName = name;
			postURI = URI;
			pingClient = client;
			pingMethod = method;

			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(pingClient));
			config.setEnabledForExtensions(true);
			config.setConnectionTimeout(60 * 1000);
			config.setReplyTimeout(60 * 1000);

			XmlRpcClient xmlrpc = new XmlRpcClient();

			// use Commons HttpClient as transport
			xmlrpc.setTransportFactory(
					new XmlRpcCommonsTransportFactory(xmlrpc));
			final Vector params = new Vector();
			params.addElement(this.postName);
			params.addElement(this.postURI);
			final Object result = xmlrpc.execute(this.pingMethod, params);

			return (result != null);

		} catch (XmlRpcException ex) {
			Log.util.error("Error pinging server %s %s %e", pingClient, ex, ex);
		} catch (MalformedURLException e) {
			Log.util.error("Error pinging server %s %s %e", pingClient, e, e);
		}
		return false;

	}

}
