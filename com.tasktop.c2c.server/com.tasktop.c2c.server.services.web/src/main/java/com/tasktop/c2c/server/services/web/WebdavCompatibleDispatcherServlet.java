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
package com.tasktop.c2c.server.services.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.DispatcherServlet;

/**
 * The existing Spring DispatcherServlet descends from HttpServlet, which only allows certain HTTP methods - WebDav uses
 * other methods which are not supported by HttpServlet, such as PROPFIND and MKCOL. This servlet implements the
 * solution contained in https://jira.springsource.org/browse/SPR-4799 and bypasses the method-type checks, allowing
 * Webdav methods to get to our Spring servlet.
 */
@SuppressWarnings("serial")
public class WebdavCompatibleDispatcherServlet extends DispatcherServlet {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// Override HttpServlet.service() so that we can pass Webdav-specific HTTP methods (such as PROPFIND) which
		// would otherwise be rejected. For more details, please see https://jira.springsource.org/browse/SPR-4799
		processRequest(req, resp);
	}
}
