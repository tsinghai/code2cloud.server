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
package com.tasktop.c2c.server.profile.domain.internal;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.tasktop.c2c.server.common.service.identity.Gravatar;

/**
 * A profile identifies a user and serves as the entrypoint for all user-related data.
 */
@Entity
public class Profile extends BaseEntity {
	private String username;
	private String password;
	private String email;
	private Boolean emailVerified = false;
	private String firstName;
	private String lastName;
	private List<ProjectProfile> projectProfiles = new ArrayList<ProjectProfile>();
	private List<PasswordResetToken> passwordResetTokens = new ArrayList<PasswordResetToken>();
	private List<AgreementProfile> agreementProfiles = new ArrayList<AgreementProfile>();
	private List<SshPublicKey> sshPublicKeys = new ArrayList<SshPublicKey>();
	private Boolean admin = false;
	private NotificationSettings notificationSettings;
	private Boolean sentWelcomeEmail = false;
	private Boolean disabled = false;

	@Transient
	private String gravatarHash;

	/**
	 * the username that identifies the user
	 */
	@Basic(optional = false)
	@Column(unique = true, nullable = false, updatable = false)
	public String getUsername() {
		return username;
	}

	/**
	 * Usernames are insert-only
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * the password for authenticating the user
	 */
	@Basic(optional = false)
	@Column(nullable = false)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * the user's email address
	 */
	@Basic(optional = false)
	@Column(unique = true, nullable = false)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		gravatarHash = null;
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

	@Column(nullable = false)
	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	/**
	 * the projects in which the user participates.
	 */
	@OneToMany(cascade = { CascadeType.PERSIST }, mappedBy = "profile")
	public List<ProjectProfile> getProjectProfiles() {
		return projectProfiles;
	}

	public void setProjectProfiles(List<ProjectProfile> projectProfiles) {
		this.projectProfiles = projectProfiles;
	}

	/**
	 * the user's password reset tokens.
	 */
	@OneToMany(cascade = { CascadeType.PERSIST }, mappedBy = "profile")
	public List<PasswordResetToken> getPasswordResetTokens() {
		return passwordResetTokens;
	}

	public void setPasswordResetTokens(List<PasswordResetToken> passwordResetTokens) {
		this.passwordResetTokens = passwordResetTokens;
	}

	@OneToMany(cascade = { CascadeType.PERSIST }, mappedBy = "profile")
	public List<AgreementProfile> getAgreementProfiles() {
		return agreementProfiles;
	}

	public void setAgreementProfiles(List<AgreementProfile> agreementProfiles) {
		this.agreementProfiles = agreementProfiles;
	}

	@OneToMany(cascade = { CascadeType.PERSIST }, mappedBy = "profile")
	public List<SshPublicKey> getSshPublicKeys() {
		return sshPublicKeys;
	}

	public void setSshPublicKeys(List<SshPublicKey> sshPublicKeys) {
		this.sshPublicKeys = sshPublicKeys;
	}

	/**
	 * get the full name of the profile, that is the first name and last name.
	 */
	@Transient
	public String getFullName() {
		return getFirstName() + ' ' + getLastName();
	}

	/**
	 * Get the Gravatar hash of the profile, or null if it has none. Normally the Gravatar hash is computed from the
	 * {@link #getEmail() email address}.
	 */
	@Transient
	public String getGravatarHash() {
		// Currently we use the primary email address. This may change
		String emailAdress = getEmail();
		if (emailAdress != null && gravatarHash == null) {
			gravatarHash = Gravatar.computeHash(emailAdress);
		}
		return gravatarHash;
	}

	/**
	 * @return the notificationSettings
	 */
	@OneToOne(cascade = { CascadeType.PERSIST }, optional = true)
	@JoinColumn(nullable = true)
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
	 * @return the emailVerified
	 */
	@Column(nullable = false)
	public Boolean getEmailVerified() {
		return emailVerified;
	}

	/**
	 * @param emailVerified
	 *            the emailVerified to set
	 */
	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	/**
	 * @return the sentWelcomeEmail
	 */
	@Column(nullable = false)
	public Boolean getSentWelcomeEmail() {
		return sentWelcomeEmail;
	}

	/**
	 * @param sentWelcomeEmail
	 *            the sentWelcomeEmail to set
	 */
	public void setSentWelcomeEmail(Boolean sentWelcomeEmail) {
		this.sentWelcomeEmail = sentWelcomeEmail;
	}

	/**
	 * @return the disabled
	 */
	@Column
	public Boolean getDisabled() {
		return disabled;
	}

	/**
	 * @param disabled
	 *            the disabled to set
	 */
	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}
}
