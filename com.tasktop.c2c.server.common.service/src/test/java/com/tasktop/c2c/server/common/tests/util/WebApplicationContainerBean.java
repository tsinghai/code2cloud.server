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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import winstone.Launcher;

public class WebApplicationContainerBean {
	@Autowired
	private ApplicationContext applicationContext;

	private static final Random random = new Random(System.currentTimeMillis());

	private Launcher winstone;

	private int port;
	private int ajpPort;

	private File webRoot;

	private boolean enableAjp;

	@SuppressWarnings({ "unchecked" })
	public void start() {
		if (winstone == null) {
			if (webRoot == null) {
				throw new IllegalStateException("must set webroot");
			}
			if (applicationContext != null) {
				EmbeddedWebContextLoader.setContext(applicationContext);
			}

			do {
				port = 8100 + Math.abs(random.nextInt(10000));
			} while (!isLocalPortFree(port));
			ajpPort = enableAjp ? port + 1 : -1;
			Logger log = LoggerFactory.getLogger(WebApplicationContainerBean.class.getName());
			log.debug("Starting web container on http://localhost:" + port + "/");
			Map args = new HashMap();
			try {
				args.put("useJasper", "false");
				args.put("ajp13Port", Integer.toString(ajpPort));
				args.put("webroot", webRoot.getAbsolutePath());
				args.put("httpPort", String.valueOf(port));
				Launcher.initLogger(args);
				winstone = new Launcher(args);

				// wait for winstone to finish starting up
				final int maxAttempts = 100;
				for (int x = 0; x < maxAttempts; ++x) {
					if (!isLocalPortFree(port)) {
						break;
					}
					if (x == maxAttempts - 1) {
						throw new IllegalStateException("Can't connect to localhost:" + port
								+ ", which indicates that the web container probably didn't start up successfully");
					}
					// wait and then try again
					try {
						Thread.sleep(100L);
					} catch (InterruptedException e1) {
						throw new IllegalStateException(e1);
					}

				}
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	private boolean isLocalPortFree(int port) {
		try {
			Socket socket = SocketFactory.getDefault().createSocket("localhost", port);
			socket.close();
			return false;
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			return true;
		}
	}

	public void stop() {
		if (winstone != null) {
			winstone.shutdown();
			winstone = null;
			if (applicationContext != null) {
				EmbeddedWebContextLoader.clearContext();
			}
		}
	}

	public File getWebRoot() {
		return webRoot;
	}

	public void setWebRoot(File webRoot) {
		this.webRoot = webRoot;
	}

	public String getBaseUrl() {
		if (port == 0 || winstone == null) {
			throw new IllegalStateException("not started");
		}
		return String.format("http://localhost:%s/", port);
	}

	public boolean isEnableAjp() {
		return enableAjp;
	}

	public void setEnableAjp(boolean enableAjp) {
		this.enableAjp = enableAjp;
	}

	public int getAjpPort() {
		return ajpPort;
	}

	public int getPort() {
		return port;
	}
}
