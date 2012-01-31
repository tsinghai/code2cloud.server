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
package com.tasktop.c2c.server.hudson.plugin.ui;

import hudson.Extension;
import hudson.model.PageDecorator;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
@Extension
public class Code2CloudPageDecorator extends PageDecorator {

	public Code2CloudPageDecorator() {
		super(Code2CloudPageDecorator.class);
	}

	@Override
	public String getDisplayName() {
		return "Code2Cloud UI Plugin";
	}

	public boolean getAnon() {
		if (SecurityContextHolder.getContext() == null
				|| SecurityContextHolder.getContext().getAuthentication() == null) {
			return true;
		}
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			return true;
		}
		// Currently we have an anon role too
		GrantedAuthority[] auths = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		for (GrantedAuthority auth : auths) {
			if (auth.getAuthority().equals("ROLE_ANONYMOUS")) {
				return true;
			}
		}
		return false;
	}
}
