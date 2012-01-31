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

@SuppressWarnings("serial")
public class Profile extends AbstractEntity implements Comparable<Profile> {
	private String username;
	private String password;
	private String email;
	private String firstName;
	private String lastName;
	private String gravatarHash;
	private String githubUsername;
	private NotificationSettings notificationSettings;
	private Boolean emailVerfied;
	private Boolean accountDisabled;

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

	public String getGithubUsername() {
		return githubUsername;
	}

	public void setGithubUsername(String githubUsername) {
		this.githubUsername = githubUsername;
	}

	public String toFullName() {
		return firstName + " " + lastName;
	}

	/**
	 * the gravatar hash, or null
	 */
	public String getGravatarHash() {
		return gravatarHash;
	}

	public void setGravatarHash(String gravatarHash) {
		this.gravatarHash = gravatarHash;
	}

	/**
	 * natural ordering of profiles is by name, then by email.
	 */
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

	/**
	 * @return the notificationSettings
	 */
	public NotificationSettings getNotificationSettings() {
		return notificationSettings;
	}

	/**
	 * @param notificationSettings
	 *            the notificationSettings to set
	 */
	public void setNotificationSettings(NotificationSettings notificationSettings) {
		this.notificationSettings = notificationSettings;
	}

	/**
	 * @return the emailVerfiied
	 */
	public Boolean getEmailVerfied() {
		return emailVerfied;
	}

	/**
	 * @param emailVerfiied
	 *            the emailVerfiied to set
	 */
	public void setEmailVerfied(Boolean emailVerfied) {
		this.emailVerfied = emailVerfied;
	}

	/**
	 * @return the accountDisabled
	 */
	public Boolean getAccountDisabled() {
		return accountDisabled;
	}

	/**
	 * @param accountDisabled
	 *            the accountDisabled to set
	 */
	public void setAccountDisabled(Boolean accountDisabled) {
		this.accountDisabled = accountDisabled;
	}

}
