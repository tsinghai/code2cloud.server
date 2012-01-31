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

import java.util.List;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.profile.domain.project.Agreement;
import com.tasktop.c2c.server.profile.domain.project.AgreementProfile;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectInvitationToken;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.SignUpToken;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKey;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKeySpec;

/**
 * Interface for interacting with {@link Profile}s and {@link Project}s. This is a public service.
 * 
 * @author David Green <david.green@tasktop.com> (Tasktop Technologies Inc.)
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * @author Lucas Panjer <lucas.panjer@tasktop.com> (Tasktop Technologies Inc.)
 * @author Ryan Slobojon <ryan.slobojan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public interface ProfileWebService {

	public List<Project> getProjects(Long profileId) throws EntityNotFoundException;

	public Project getProjectForInvitationToken(String token) throws EntityNotFoundException;

	public Project getProjectByIdentifier(String projectIdentifier) throws EntityNotFoundException;

	public Profile getCurrentProfile();

	public void updateProfile(Profile profile) throws ValidationException, EntityNotFoundException;

	public Project createProject(Long profileId, Project project) throws EntityNotFoundException, ValidationException;

	public Project updateProject(Project project) throws EntityNotFoundException, ValidationException;

	public List<Agreement> getPendingAgreements() throws EntityNotFoundException;

	public void approveAgreement(Long agreementId) throws EntityNotFoundException;

	public List<AgreementProfile> getApprovedAgreementProfiles() throws EntityNotFoundException;

	public String[] getRolesForProject(String projectIdentifier) throws EntityNotFoundException;

	public QueryResult<Project> findProjects(String query, QueryRequest request);

	public QueryResult<Project> findProjects(ProjectRelationship projectRelationship, QueryRequest queryRequest);

	public void watchProject(String projectIdentifier) throws EntityNotFoundException;

	public void unwatchProject(String projectIdentifier) throws EntityNotFoundException;

	public Boolean isWatchingProject(String projectIdentifier) throws EntityNotFoundException;

	/**
	 * Creates a sign up token that can be emailed to potential users.
	 * 
	 * Roles: Admin
	 * 
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @throws ValidationException
	 *             if a profile exists with this email, or another unused token exists with this email.
	 */
	public SignUpToken createSignUpToken(SignUpToken token) throws ValidationException;

	/**
	 * Get a list of all unused sign up tokens.
	 * 
	 * Roles: Admin
	 * 
	 * @return a list of all unused sign up tokens.
	 */
	public List<SignUpToken> getUnusedSignUpTokens();

	/**
	 * Triggers email job to send an invitation to a potential user. Also marks the sign up token as used and marks sign
	 * up token as used (and doesn't send email job) if a profile with this email exists.
	 * 
	 * Roles: Admin
	 * 
	 * @param email
	 * @throws EntityNotFoundException
	 */
	public void sendSignUpInvitation(String email) throws EntityNotFoundException;

	/**
	 * Get a SignUpToken.
	 * 
	 * Roles: Anonymous
	 * 
	 * @param token
	 * @return the @{link SignUpToken}
	 * @throws EntityNotFoundException
	 *             if a valid token can't be found
	 */
	public SignUpToken getSignUpToken(String token) throws EntityNotFoundException;

	/**
	 * Get a ProjectInvitationToken.
	 * 
	 * Roles: Anonymous
	 * 
	 * @param token
	 * @return the {@link ProjectInvitationToken}
	 * @throws EntityNotFoundException
	 *             if a valid token can't be found
	 */
	public ProjectInvitationToken getProjectInvitationToken(String token) throws EntityNotFoundException;

	/**
	 * Creates a profile using a sign up token. Marks the token as used.
	 * 
	 * Roles: Anonymous
	 * 
	 * @param profile
	 * @param token
	 *            can pass null if token is not required
	 * @return the profileId
	 * @throws EntityNotFoundException
	 * @throws ValidationException
	 */
	public Long createProfileWithSignUpToken(Profile profile, String token) throws EntityNotFoundException,
			ValidationException;

	public String inviteUserForProject(String email, String projectIdentifier) throws EntityNotFoundException;

	public void acceptInvitation(String invitationToken) throws EntityNotFoundException;

	public SshPublicKey createSshPublicKey(SshPublicKeySpec publicKey) throws ValidationException;

	public List<SshPublicKey> listSshPublicKeys();

	public void removeSshPublicKey(Long publicKeyId) throws EntityNotFoundException;

	public Boolean isProjectCreateAvailble();

	Boolean isPasswordResetTokenAvailable(String token);

}
