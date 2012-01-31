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
package com.tasktop.c2c.server.common.service.web.cgi;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.HttpRequestHandler;

public class CGIHttpRequestHandler implements HttpRequestHandler, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(CGIHttpRequestHandler.class.getSimpleName());

	private CGIHandler handler;

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		LOG.info("Git http request: " + request.getMethod() + " " + request.getRequestURI());
		CGIHandler handler = this.handler.clone();
		handler.service(request, response);
		LOG.info("Git http response complete.");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (handler == null) {
			throw new Exception("Must set handler");
		}
		if (handler.getCommand() == null) {
			throw new Exception("Handler command must be provided");
		}
	}

	public CGIHandler getHandler() {
		return handler;
	}

	public void setHandler(CGIHandler handler) {
		this.handler = handler;
	}
}
