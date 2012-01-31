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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
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

public interface ProfileServiceAsync {

	void logon(String username, String password, boolean rememberMe, AsyncCallback<Credentials> callback);

	void logout(AsyncCallback<Boolean> callback);

	void getCurrentUserInfo(AsyncCallback<UserInfo> callback);

	void updateProfile(com.tasktop.c2c.server.profile.domain.project.Profile profile,
			AsyncCallback<Credentials> callback);

	void createProject(Credentials credentials, Project project, AsyncCallback<String> callback);

	void updateProject(Project project, AsyncCallback<Project> callback);

	void getProject(String projectIdentifier, AsyncCallback<Project> callback);

	void getRolesForProject(String projectIdentifier, AsyncCallback<String[]> callback);

	void listAllProfiles(AsyncCallback<List<Profile>> callback);

	void requestPasswordReset(String email, AsyncCallback<Boolean> callback);

	void isTokenAvailable(String token, AsyncCallback<Boolean> callback);

	void resetPassword(String token, String newPassword, AsyncCallback<Credentials> callback);

	void getProfiles(String projectIdentifier, AsyncCallback<List<Profile>> callback);

	void getProfiles(String projectIdentifier, String query, int limit, AsyncCallback<List<Profile>> callback);

	void findProfiles(String query, QueryRequest request, AsyncCallback<QueryResult<Profile>> callback);

	void addTeamMemberByEmail(String projectIdentifier, String personEmail, AsyncCallback<Boolean> callback);

	void updateTeamMemberRoles(String projectIdentifier, ProjectTeamMember member, AsyncCallback<Boolean> callback);

	void removeTeamMember(String projectIdentifier, ProjectTeamMember member, AsyncCallback<Boolean> callback);

	void getRecentActivity(String projectIdentifier, AsyncCallback<List<ProjectActivity>> callback);

	void getShortActivityList(String projectIdentifier, AsyncCallback<List<ProjectActivity>> callback);

	void getDashboard(String projectIdentifier, AsyncCallback<ProjectDashboard> callback);

	void inviteUserForProject(String email, String projectIdentifier, AsyncCallback<Void> callback);

	void acceptInvitation(String invitationToken, AsyncCallback<Void> callback);

	void getProjectForInvitationToken(String token, AsyncCallback<Project> callback);

	void findProjects(String query, QueryRequest request, AsyncCallback<QueryResult<Project>> callback);

	void watchProject(String projectIdentifier, AsyncCallback<Void> callback);

	void unwatchProject(String projectIdentifier, AsyncCallback<Void> callback);

	void isWatchingProject(String projectIdentifier, AsyncCallback<Boolean> callback);

	void deleteProjectGitRepository(String projectIdentifier, Long repositoryId,
			AsyncCallback<Void> asyncCallbackSupport);

	void createProjectGitRepository(String projectIdentifier, ScmRepository repository,
			AsyncCallback<Void> asyncCallbackSupport);

	void createProfileWithSignUpToken(com.tasktop.c2c.server.profile.domain.project.Profile profile, String token,
			AsyncCallback<UserInfo> callback);

	void createSignUpTokensFromCsv(String csv, Boolean sendEmail, AsyncCallback<SignUpTokens> callback);

	void createSshPublicKey(SshPublicKeySpec publicKey, AsyncCallback<SshPublicKey> callback);

	void updateSshPublicKey(SshPublicKey publicKey, AsyncCallback<SshPublicKey> callback);

	void removeSshPublicKey(Long publicKeyId, AsyncCallback<Void> callback);

	void verifyEmail(AsyncCallback<Void> asyncCallbackSupport);

	void verifyEmailToken(String token, AsyncCallback<Void> asyncCallbackSupport);

	void getProjects(ProjectRelationship projectRelationship, QueryRequest queryRequest,
			AsyncCallback<QueryResult<Project>> callback);

}
