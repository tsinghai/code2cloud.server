/*******************************************************************************
 * Copyright (c) 2010, 2012 Tasktop Technologies
 * Copyright (c) 2010, 2011 SpringSource, a division of VMware
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 ******************************************************************************/
package com.tasktop.c2c.server.common.tests.util;

import java.io.File;

import org.eclipse.jetty.ajp.Ajp13SocketConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
/** Use to run
 * 
 */
public class JettyWebApplicationRunner {

	private String webRoot = "src/main/webapp";
	private String resourceBase;
	private int port = 8080;
	private int ajpPort = -1;
	private String contextRoot = "/";
	Server server;

	public void start() throws Exception {
		if (webRoot == null) {
			throw new IllegalStateException("must set webroot");
		}

		Logger log = LoggerFactory.getLogger(getClass().getName());
		log.debug("Starting web container on http://localhost:" + port + contextRoot);
		server = new Server(port);

		WebAppContext context = new WebAppContext();
		context.setContextPath(contextRoot);
		context.setResourceBase(webRoot);
		if (resourceBase != null) {
			context.setResourceBase(resourceBase);
		}
		context.setParentLoaderPriority(true);

		server.setHandler(context);
		if (ajpPort > 0) {
			Ajp13SocketConnector ajpCon = new Ajp13SocketConnector();
			ajpCon.setPort(ajpPort);
			server.addConnector(ajpCon);
		}

		server.start();
	}

	public void join() throws InterruptedException {
		server.join();
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setWebRoot(File webRoot) {
		this.webRoot = webRoot.getAbsolutePath();
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}

	public void setAjpPort(int ajpPort) {
		this.ajpPort = ajpPort;
	}

	public int getAjpPort() {
		return ajpPort;
	}

	public String getResourceBase() {
		return resourceBase;
	}

	public void setResourceBase(String resourceBase) {
		this.resourceBase = resourceBase;
	}

}
