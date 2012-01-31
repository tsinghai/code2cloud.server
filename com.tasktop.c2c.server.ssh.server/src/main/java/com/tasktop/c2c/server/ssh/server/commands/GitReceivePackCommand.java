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
package com.tasktop.c2c.server.ssh.server.commands;

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.common.service.domain.Role;


/**
 * @author David Green (Tasktop Technologies Inc.)
 */
@Component
public class GitReceivePackCommand extends AbstractInteractiveProxyCommand {

	private String scmPrefix = "/scm/";

	@Override
	public String getName() {
		return "git-receive-pack";
	}

	@Override
	protected String computeRequestPath(String path) {
		return scmPrefix + path;
	}

	@Override
	protected String[] getRequiredRoles() {
		return new String[] { Role.User };
	}

}
