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
package com.tasktop.c2c.server.profile.service.provider;

import com.tasktop.c2c.server.profile.service.GitService;
import com.tasktop.c2c.server.scm.service.ScmService;

public interface ScmServiceInternal extends ScmService {

	// This method is used for internal testing.
	public void setServiceProvider(ServiceProvider<GitService> testProvider);
}
