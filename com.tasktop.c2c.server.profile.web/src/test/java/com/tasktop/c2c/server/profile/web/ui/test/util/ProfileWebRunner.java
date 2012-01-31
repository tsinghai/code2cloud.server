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
package com.tasktop.c2c.server.profile.web.ui.test.util;

import com.tasktop.c2c.server.common.tests.util.JettyWebApplicationRunner;

public class ProfileWebRunner {

	public static void main(String[] args) throws Exception {
		JettyWebApplicationRunner r = new JettyWebApplicationRunner();
		r.setContextRoot("/");
		// r.setResourceBase("target/profile.web-0.1.0-SNAPSHOT");
		r.setPort(8888);
		r.start();
	}

}
