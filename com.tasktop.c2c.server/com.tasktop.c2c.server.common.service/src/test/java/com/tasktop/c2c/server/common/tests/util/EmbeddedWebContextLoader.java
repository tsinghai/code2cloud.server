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

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

public class EmbeddedWebContextLoader extends ContextLoaderListener {

	private static ApplicationContext applicationContext;

	@Override
	protected ApplicationContext loadParentContext(ServletContext servletContext) {
		if (applicationContext != null) {
			return applicationContext;
		}
		return super.loadParentContext(servletContext);
	}

	public static synchronized void setContext(ApplicationContext context) {
		if (context == null) {
			throw new IllegalArgumentException();
		}
		if (applicationContext != null) {
			throw new IllegalStateException();
		}
		applicationContext = context;
	}

	public static synchronized void clearContext() {
		applicationContext = null;
	}

	public static synchronized boolean hasApplicationContext() {
		return applicationContext != null;
	}
}
