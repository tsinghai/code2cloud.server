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
package com.tasktop.c2c.server.profile.domain.project;

/**
 * A token used to accept a project invitation.
 * 
 */
@SuppressWarnings("serial")
public class ProjectInvitationToken extends AbstractEntity {

	private String token;
	private String email;
	private Profile issuingUser;

	public ProjectInvitationToken() {
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the issuingUser
	 */
	public Profile getIssuingUser() {
		return issuingUser;
	}

	/**
	 * @param issuingUser
	 *            the issuingUser to set
	 */
	public void setIssuingUser(Profile issuingUser) {
		this.issuingUser = issuingUser;
	}

}
