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
package com.tasktop.c2c.server.tasks.web;

import java.io.IOException;

import org.junit.Ignore;

import com.tasktop.c2c.server.common.tests.util.WebApplicationRunner;

@Ignore
public class TaskWebRunner {

	public static void main(String[] args) throws IOException, InterruptedException {
		WebApplicationRunner r = new WebApplicationRunner();
		r.setContextRoot("/tasks");
		r.setPort(8080);
		r.start();
		Thread.sleep(Integer.MAX_VALUE);
	}
}
