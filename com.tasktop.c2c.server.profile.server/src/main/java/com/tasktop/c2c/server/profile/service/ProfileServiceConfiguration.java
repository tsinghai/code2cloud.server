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
package com.tasktop.c2c.server.profile.service;

import com.tasktop.c2c.server.common.service.BaseProfileConfiguration;

/**
 * Simple container class for the configuration needed by profile service.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class ProfileServiceConfiguration extends BaseProfileConfiguration {

	private Boolean invitationOnlySignUp;
	private String signupNotificationEmail;
	private int sshPort = 22;

	public ProfileServiceConfiguration() {
	}

	public String getProfilePasswordResetURL(String token) {
		return getProfileBaseUrl() + "/#resetPassword/" + token;
	}

	public String getInvitationURL(String token) {
		return getProfileBaseUrl() + "/#invitation/" + token;
	}

	public String getSignUpInvitationURL(String token) {
		return getProfileBaseUrl() + "/#signup/" + token;
	}

	public String getHostedScmUrlPrefix(String projectId) {
		return getServiceUrlPrefix(projectId) + "scm/";
	}

	public int getPublicSshPort() {
		return sshPort;
	}

	public void setPublicSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

	public String getEmailVerificationURL(String token) {
		return getProfileBaseUrl() + "/#verifyEmail/" + token;
	}

	public Boolean getInvitationOnlySignUp() {
		return invitationOnlySignUp;
	}

	public void setInvitationOnlySignUp(Boolean invitationOnlySignUp) {
		this.invitationOnlySignUp = invitationOnlySignUp;
	}

	public String getSignupNotificationEmail() {
		return signupNotificationEmail;
	}

	public void setSignupNotificationEmail(String signupNotificationEmail) {
		this.signupNotificationEmail = signupNotificationEmail;
	}

	public String getAppName() {
		return "Code2Cloud";
	}
}
