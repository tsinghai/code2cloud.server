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
package com.tasktop.c2c.server.profile.web.shared;

@SuppressWarnings("serial")
public class Profile extends AbstractEntity implements Comparable<Profile> {
	private String username;
	private String password;
	private String email;
	private String firstName;
	private String lastName;
	private String gravatarHash;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String toFullName() {
		return firstName + " " + lastName;
	}

	public void setGravatarHash(String gravatarHash) {
		this.gravatarHash = gravatarHash;
	}

	/**
	 * the gravatar hash, or null
	 */
	public String getGravatarHash() {
		return gravatarHash;
	}

	/**
	 * natural ordering of profiles is by name, then by email.
	 */
	@Override
	public int compareTo(Profile o) {
		if (o == this) {
			return 0;
		}
		int i = toFullName().compareToIgnoreCase(o.toFullName());
		if (i == 0) {
			i = getEmail().compareToIgnoreCase(o.getEmail());
			if (i == 0) {
				i = getId().compareTo(o.getId());
			}
		}
		return i;
	}

	@Override
	public String toString() {
		return "Profile [username=" + username + ", firstName=" + firstName + ", lastName=" + lastName + "]";
	}

}
