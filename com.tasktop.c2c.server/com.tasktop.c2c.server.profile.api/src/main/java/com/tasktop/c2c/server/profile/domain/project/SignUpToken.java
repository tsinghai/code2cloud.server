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
 * A token used to sign up for a new profile.
 * 
 */
@SuppressWarnings("serial")
public class SignUpToken extends AbstractEntity {

	private String token;
	private String firstName;
	private String lastName;
	private String email;
	private String url;

	public SignUpToken() {
	}

	public void setFirstname(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstname() {
		return firstName;
	}

	public void setLastname(String lastName) {
		this.lastName = lastName;
	}

	public String getLastname() {
		return lastName;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
