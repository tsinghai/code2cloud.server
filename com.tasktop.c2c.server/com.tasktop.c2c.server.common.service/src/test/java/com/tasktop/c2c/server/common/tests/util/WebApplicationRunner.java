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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import winstone.Launcher;

@Ignore
/** Use to run
 * 
 */
public class WebApplicationRunner {

	private Launcher winstone;
	private File webRoot = new File("src/main/webapp");
	private int port = 8080;
	private int ajpPort = -1;
	private String contextRoot = "/";

	@SuppressWarnings({ "unchecked" })
	public void start() throws IOException {
		if (webRoot == null) {
			throw new IllegalStateException("must set webroot");
		}

		Logger log = LoggerFactory.getLogger(getClass().getName());
		log.debug("Starting web container on http://localhost:" + port + "/");
		Map args = new HashMap();

		args.put("useJasper", "false");
		args.put("ajp13Port", Integer.toString(ajpPort));
		args.put("webroot", webRoot.getAbsolutePath());
		args.put("httpPort", String.valueOf(port));
		args.put("prefix", contextRoot);
		Launcher.initLogger(args);

		winstone = new Launcher(args);

	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}

	public void setAjpPort(int ajpPort) {
		this.ajpPort = ajpPort;
	}
}
