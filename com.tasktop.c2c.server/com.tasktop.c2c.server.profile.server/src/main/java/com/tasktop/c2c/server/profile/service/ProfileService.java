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

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.logging.NoLog;
import com.tasktop.c2c.server.profile.domain.internal.Agreement;
import com.tasktop.c2c.server.profile.domain.internal.AgreementProfile;
import com.tasktop.c2c.server.profile.domain.internal.InvitationToken;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ProjectProfile;
import com.tasktop.c2c.server.profile.domain.internal.RandomToken;
import com.tasktop.c2c.server.profile.domain.internal.SignUpToken;
import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.SignUpTokens;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKeySpec;

/**
 * Interface for interacting with {@link Profile}s and {@link Project}s. These are the internal versions of the objects,
 * and as such this service is for internal use only. See {@link ProfileWebService} for a public (external) interface.
 * 
 * @author David Green <david.green@tasktop.com> (Tasktop Technologies Inc.)
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * @author Lucas Panjer <lucas.panjer@tasktop.com> (Tasktop Technologies Inc.)
 * @author Ryan Slobojon <ryan.slobojan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public interface ProfileService {

	public Long createProfile(Profile profile) throws ValidationException;

	public void updateProfile(Profile profile) throws EntityNotFoundException, ValidationException;

	public Profile getProfile(Long id) throws EntityNotFoundException;

	public Profile getProfileByEmail(String emailAddress);

	public Profile getProfileByUsername(String username) throws EntityNotFoundException;

	public Profile getCurrentUserProfile();

	public Long authenticate(String username, @NoLog String password) throws AuthenticationException;

	public Project createProject(Long profileId, Project project) throws EntityNotFoundException, ValidationException;

	public Project getProject(Long projectId) throws EntityNotFoundException;

	public Project getProjectByIdentifier(String projectIdentifier) throws EntityNotFoundException;

	public List<Project> getProfileProjects(Long profileId) throws EntityNotFoundException;

	public QueryResult<Project> findProjects(ProjectRelationship projectRelationship, QueryRequest queryRequest);

	public void requestPasswordReset(String email) throws EntityNotFoundException;

	public String resetPassword(String token, @NoLog String newPassword) throws EntityNotFoundException,
			ValidationException;

	/**
	 * Determines whether a password reset token is available.
	 * 
	 * @param passwordResetToken
	 * @return
	 */
	public Boolean isPasswordResetTokenAvailable(String passwordResetToken);

	public QueryResult<Profile> findProfiles(String query, Region pageInfo, SortInfo sortInfo);

	public void watchProject(String projectIdentifier) throws EntityNotFoundException;

	public void unwatchProject(String projectIdentifier) throws EntityNotFoundException;

	public void addProjectProfile(Long projectId, Long profileId) throws EntityNotFoundException;

	public void removeProjectProfile(Long projectId, Long profileId) throws EntityNotFoundException,
			ValidationException;

	public ProjectProfile getProjectProfile(Long projectId, Long profileId) throws EntityNotFoundException;

	public void updateProjectProfile(ProjectProfile projectProfile) throws EntityNotFoundException, ValidationException;

	public Project updateProject(Project project) throws EntityNotFoundException, ValidationException;

	public List<Agreement> getPendingAgreements() throws EntityNotFoundException;

	public void approveAgreement(Long agreementId) throws EntityNotFoundException;

	public List<AgreementProfile> getApprovedAgreementProfiles() throws EntityNotFoundException;

	public String inviteUserForProject(String email, String projectIdentifier) throws EntityNotFoundException;

	public Project getProjectForInvitationToken(String invitationToken) throws EntityNotFoundException;

	public void acceptInvitation(String invitationToken) throws EntityNotFoundException;

	public QueryResult<Project> findProjects(String query, Region pageInfo, SortInfo sortInfo);

	public Boolean isWatchingProject(String projectIdentifier) throws EntityNotFoundException;

	public SignUpToken getSignUpToken(String signUpToken) throws EntityNotFoundException;

	public InvitationToken getProjectInvitationToken(String projectInvitationToken) throws EntityNotFoundException;

	public void markSignUpTokenUsed(String token) throws EntityNotFoundException;

	public SignUpToken createSignUpToken(String firstName, String lastName, String email) throws ValidationException;

	public List<SignUpToken> getUnusedSignUpTokens();

	public void sendSignUpInvitation(String email) throws EntityNotFoundException;

	/**
	 * replicate members of a team to the ALM service user registries.
	 * 
	 * @param projectId
	 *            the id of the project for which the users should be replicated.
	 */
	public void replicateTeam(Long projectId) throws EntityNotFoundException;

	public Boolean isProjectCreateAvailable();

	public RandomToken getSignUpOrProjectInvitationToken(String token) throws EntityNotFoundException;

	public List<Profile> listAllProfiles();

	public SignUpTokens createInvitations(SignUpTokens invitationTokens, boolean sendEmail) throws ValidationException;

	/**
	 * create a public key using the format specified in <a href="http://www.ietf.org/rfc/rfc4716.txt">RFC 4716</a>,
	 * associating it with the current profile's account
	 * 
	 * @param publicKey
	 *            the public key
	 * @return the newly created public key
	 * @throws ValidationException
	 */
	public SshPublicKey createSshPublicKey(SshPublicKeySpec keySpec) throws ValidationException;

	/**
	 * create a public key, associating it with the current profile's account
	 * 
	 * @param publicKey
	 *            the public key
	 * @return the newly created public key
	 * @throws ValidationException
	 */
	public SshPublicKey createSshPublicKey(SshPublicKey publicKey) throws ValidationException;

	/**
	 * Update a public key. Currently only changing the name is supported
	 * 
	 * @param publicKey
	 *            the public key
	 * @return the newly created public key
	 * @throws ValidationException
	 */
	public SshPublicKey updateSshPublicKey(SshPublicKey publicKey) throws ValidationException;

	/**
	 * list public keys available for the current profile's account
	 */
	public List<SshPublicKey> listSshPublicKeys();

	/**
	 * remove the identified public key from the current profile's account
	 * 
	 * @param publicKeyId
	 *            the identity of the public key to remove
	 * @throws EntityNotFoundException
	 *             if the given public key id is unknown or not associated with the current profile's account
	 */
	public void removeSshPublicKey(Long publicKeyId) throws EntityNotFoundException;

	/**
	 * @param emailToken
	 * @throws EntityNotFoundException
	 * @throws ValidationException
	 */
	void verifyEmail(String emailToken) throws EntityNotFoundException, ValidationException;

	/**
	 * 
	 */
	void sendVerificationEmail();
}
