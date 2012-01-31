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
package com.tasktop.c2c.server.profile.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.web.shared.AuthenticationFailedException;
import com.tasktop.c2c.server.common.web.shared.AuthenticationRequiredException;
import com.tasktop.c2c.server.common.web.shared.NoSuchEntityException;
import com.tasktop.c2c.server.common.web.shared.ValidationFailedException;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.SignUpTokens;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKey;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKeySpec;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;
import com.tasktop.c2c.server.profile.web.shared.Credentials;
import com.tasktop.c2c.server.profile.web.shared.Profile;
import com.tasktop.c2c.server.profile.web.shared.ProjectDashboard;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamMember;
import com.tasktop.c2c.server.profile.web.shared.UserInfo;

@RemoteServiceRelativePath("profileService")
public interface ProfileService extends RemoteService {
	/**
	 * @see #getCurrentUser()
	 */
	public Credentials logon(String username, String password, boolean rememberMe) throws AuthenticationFailedException;

	public Boolean logout();

	public UserInfo getCurrentUserInfo();

	public Boolean requestPasswordReset(String email) throws NoSuchEntityException;

	public Boolean isTokenAvailable(String token);

	public Credentials resetPassword(String token, String newPassword) throws NoSuchEntityException,
			ValidationFailedException, AuthenticationFailedException;

	/**
	 * update the given profile.
	 */
	public Credentials updateProfile(com.tasktop.c2c.server.profile.domain.project.Profile profile)
			throws ValidationFailedException, NoSuchEntityException, AuthenticationRequiredException;

	String createProject(Credentials credentials, Project project) throws ValidationFailedException,
			AuthenticationRequiredException, NoSuchEntityException;

	public Project updateProject(Project project) throws NoSuchEntityException, ValidationFailedException;

	public Project getProject(String projectIdentifier) throws NoSuchEntityException, AuthenticationRequiredException;

	public QueryResult<Project> getProjects(ProjectRelationship projectRelationship, QueryRequest queryRequest);

	public String[] getRolesForProject(String projectIdentifier) throws NoSuchEntityException;

	public List<Profile> getProfiles(String projectIdentifier) throws NoSuchEntityException;

	public List<Profile> getProfiles(String projectIdentifier, String query, int limit) throws NoSuchEntityException;

	public QueryResult<Profile> findProfiles(String query, QueryRequest request);

	public Boolean addTeamMemberByEmail(String projectIdentifier, String personEmail) throws NoSuchEntityException;

	public Boolean updateTeamMemberRoles(String projectIdentifier, ProjectTeamMember member)
			throws NoSuchEntityException, ValidationFailedException;

	boolean removeTeamMember(String projectIdentifier, ProjectTeamMember member) throws NoSuchEntityException,
			ValidationFailedException;

	public List<ProjectActivity> getRecentActivity(String projectIdentifier);

	public List<ProjectActivity> getShortActivityList(String projectIdentifier);

	public ProjectDashboard getDashboard(String projectIdentifier) throws NoSuchEntityException;

	void createProjectGitRepository(String projectIdentifier, ScmRepository repository)
			throws ValidationFailedException, NoSuchEntityException;

	public void deleteProjectGitRepository(String projectIdentifier, Long repositoryId) throws NoSuchEntityException;

	public void inviteUserForProject(String email, String projectIdentifier) throws NoSuchEntityException;

	public void acceptInvitation(String invitationToken) throws NoSuchEntityException;

	public Project getProjectForInvitationToken(String token) throws NoSuchEntityException;

	public QueryResult<Project> findProjects(String query, QueryRequest request);

	public void watchProject(String projectIdentifier) throws NoSuchEntityException;

	public void unwatchProject(String projectIdentifier) throws NoSuchEntityException;

	public Boolean isWatchingProject(String projectIdentifier) throws NoSuchEntityException;

	/**
	 * Sign up with an optional token. If sign up with tokens is configured this will fail unless a token is provided.
	 * 
	 * @param profile
	 * @param token
	 *            optional
	 * @return
	 * @throws NoSuchEntityException
	 * @throws ValidationFailedException
	 */
	public UserInfo createProfileWithSignUpToken(com.tasktop.c2c.server.profile.domain.project.Profile profile,
			String token) throws NoSuchEntityException, ValidationFailedException;

	public List<Profile> listAllProfiles();

	public SignUpTokens createSignUpTokensFromCsv(String csv, Boolean sendEmail) throws ValidationFailedException;

	public SshPublicKey createSshPublicKey(SshPublicKeySpec publicKey) throws ValidationFailedException;

	public SshPublicKey updateSshPublicKey(SshPublicKey publicKey) throws ValidationFailedException;

	public void removeSshPublicKey(Long publicKeyId) throws NoSuchEntityException;

	void verifyEmail() throws ValidationFailedException;

	void verifyEmailToken(String token) throws NoSuchEntityException, ValidationFailedException;
}
