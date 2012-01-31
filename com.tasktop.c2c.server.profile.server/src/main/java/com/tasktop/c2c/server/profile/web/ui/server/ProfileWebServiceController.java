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
package com.tasktop.c2c.server.profile.web.ui.server;

import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.ACCEPT_INVITE_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.AGREEMENT_ID_URLPARAM;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.APPROVE_AGREEMENT_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.CREATE_PROFILE_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.CREATE_PROFILE_WITH_SIGNUP_TOKEN_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.CREATE_PROJECT_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.CREATE_SIGNUP_TOKEN_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.CREATE_SSH_PUBLIC_KEY_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.DELETE_SSH_PUBLIC_KEYS;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.EMAIL_URLPARAM;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.FIND_PROJECTS_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.GET_APPROVED_AGREEMENTS_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.GET_PENDING_AGREEMENTS_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.GET_PROFILE_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.GET_PROJECTS_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.GET_PROJECT_BY_IDENTIFIER_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.GET_PROJECT_FOR_INVITATION_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.GET_PROJECT_INVITATION_TOKEN_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.GET_ROLES_FOR_PROJECT_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.GET_SIGNUP_TOKEN_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.GET_UNUSED_SIGNUP_TOKENS_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.INVITE_USER_TO_PROJECT_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.LIST_SSH_PUBLIC_KEYS_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.PROFILE_ID_URLPARAM;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.PROJECT_IDENTIFIER_URLPARAM;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.PROJECT_UNWATCH_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.PROJECT_WATCH_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.QUERY_URLPARAM;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.SEND_SIGNUP_INVITATION_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.SSH_KEY_ID_PARAM;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.TOKEN_URLPARAM;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.UPDATE_PROFILE_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.UPDATE_PROJECT_URL;
import static com.tasktop.c2c.server.profile.service.ProfileWebServiceClient.USER_EMAIL_PARAM;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.doc.Documentation;
import com.tasktop.c2c.server.common.service.doc.Section;
import com.tasktop.c2c.server.common.service.doc.Title;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.web.AbstractRestService;
import com.tasktop.c2c.server.profile.domain.project.Agreement;
import com.tasktop.c2c.server.profile.domain.project.AgreementProfile;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectInvitationToken;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.SignUpToken;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKey;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKeySpec;
import com.tasktop.c2c.server.profile.service.ProfileWebService;

@Title("Profile Service")
@Documentation("A service for managing a users profile, consisting of identity, projects and project teams.\n"
		+ "The profile service methods are available by appending the URI to the base URL\n"
		+ "https://{hostname}/{prefix}/api + URI, for example: https://code.cloudfoundry.com/api/profile")
@Controller
public class ProfileWebServiceController extends AbstractRestService implements ProfileWebService {

	@Autowired
	@Qualifier("main")
	private ProfileWebService profileWebService;

	@Section("Account Management")
	@Title("Retrieve Profile")
	@Documentation("Retrieve the profile associated with the currently authenticated user.")
	@RequestMapping(value = GET_PROFILE_URL, method = RequestMethod.GET)
	@Override
	public Profile getCurrentProfile() {
		return profileWebService.getCurrentProfile();
	}

	@Section("Account Management")
	@Title("Update Profile")
	@Documentation("Update the profile. This must be the profile associated with the currently authenticated user.")
	@RequestMapping(value = UPDATE_PROFILE_URL, method = RequestMethod.POST)
	@Override
	public void updateProfile(Profile profile) throws ValidationException, EntityNotFoundException {
		profileWebService.updateProfile(profile);
	}

	@Section("Projects")
	@Title("Retrieve Projects")
	@Documentation("Retrieve a list of projects associated with the given profileId.")
	@Override
	@RequestMapping(value = GET_PROJECTS_URL, method = RequestMethod.GET)
	public List<Project> getProjects(@PathVariable(PROFILE_ID_URLPARAM) Long profileId) throws EntityNotFoundException {
		return profileWebService.getProjects(profileId);
	}

	@Section("Projects")
	@Title("Create Project")
	@Documentation("Create a new project.")
	@RequestMapping(value = CREATE_PROJECT_URL, method = RequestMethod.POST)
	@Override
	public Project createProject(@PathVariable(PROFILE_ID_URLPARAM) Long profileId, @RequestBody Project project)
			throws EntityNotFoundException, ValidationException {
		return profileWebService.createProject(profileId, project);
	}

	@Section("Projects")
	@Title("Retrieve Project By Token")
	@Documentation("Retrieve the project associated with the given project invitation token.")
	@Override
	@RequestMapping(value = GET_PROJECT_FOR_INVITATION_URL, method = RequestMethod.GET)
	public Project getProjectForInvitationToken(@PathVariable(TOKEN_URLPARAM) String token)
			throws EntityNotFoundException {
		return profileWebService.getProjectForInvitationToken(token);
	}

	@Section("Agreements")
	@Title("List Pending Agreements")
	@Documentation("List agreements that are pending for the current user's account. If no agreements are pending, an empty list is returned.")
	@Override
	@RequestMapping(value = GET_PENDING_AGREEMENTS_URL, method = RequestMethod.GET)
	public List<Agreement> getPendingAgreements() throws EntityNotFoundException {
		return profileWebService.getPendingAgreements();
	}

	@Section("Agreements")
	@Title("Approve Agreement")
	@Documentation("Agree to the terms of the agreement identified by its id.")
	@Override
	@RequestMapping(value = APPROVE_AGREEMENT_URL, method = RequestMethod.POST)
	public void approveAgreement(@PathVariable(AGREEMENT_ID_URLPARAM) Long agreementId) throws EntityNotFoundException {
		profileWebService.approveAgreement(agreementId);
	}

	@Section("Agreements")
	@Title("List Agreements")
	@Documentation("List agreements for the current user that have been approved.")
	@Override
	@RequestMapping(value = GET_APPROVED_AGREEMENTS_URL, method = RequestMethod.GET)
	public List<AgreementProfile> getApprovedAgreementProfiles() throws EntityNotFoundException {
		return profileWebService.getApprovedAgreementProfiles();
	}

	@Section("Projects")
	@Title("Update Project")
	@Documentation("Update the given project, setting its name and/or description.")
	@RequestMapping(value = UPDATE_PROJECT_URL, method = RequestMethod.POST)
	public Project updateProject(@PathVariable(PROJECT_IDENTIFIER_URLPARAM) String projectIdentifier,
			@RequestBody Project project) throws EntityNotFoundException, ValidationException {
		// Defer to our internal method to do this.
		return this.updateProject(project);
	}

	@Override
	public Project updateProject(Project project) throws EntityNotFoundException, ValidationException {
		return profileWebService.updateProject(project);
	}

	@Section("Projects")
	@Title("Get Project Roles")
	@Documentation("Get the current user's roles for the given project")
	@Override
	@RequestMapping(value = GET_ROLES_FOR_PROJECT_URL, method = RequestMethod.GET)
	public String[] getRolesForProject(@PathVariable(PROJECT_IDENTIFIER_URLPARAM) String projectIdentifier)
			throws EntityNotFoundException {
		return profileWebService.getRolesForProject(projectIdentifier);
	}

	@Section("Projects")
	@Title("Retrieve Project By Identifier")
	@Documentation("Get a project by its identifier.")
	@Override
	@RequestMapping(value = GET_PROJECT_BY_IDENTIFIER_URL, method = RequestMethod.GET)
	public Project getProjectByIdentifier(@PathVariable(PROJECT_IDENTIFIER_URLPARAM) String projectIdentifier)
			throws EntityNotFoundException {
		return profileWebService.getProjectByIdentifier(projectIdentifier);
	}

	@Section("Projects")
	@Title("Find Projects")
	@Documentation("Find projects matching the given query string. Projects are matched by name and description.")
	@Override
	@RequestMapping(value = FIND_PROJECTS_URL, method = RequestMethod.GET)
	public QueryResult<Project> findProjects(@PathVariable(QUERY_URLPARAM) String query, QueryRequest request) {
		return profileWebService.findProjects(query, null);
	}

	@Section("Projects")
	@Title("Find Projects")
	@Documentation("Find projects based on the relationship with the user.")
	@RequestMapping(value = FIND_PROJECTS_URL, method = RequestMethod.GET)
	@Override
	public QueryResult<Project> findProjects(ProjectRelationship projectRelationship, QueryRequest queryRequest) {
		return profileWebService.findProjects(projectRelationship, queryRequest);
	}

	@Section("Projects")
	@Title("Watch Project")
	@Documentation("Watch the given project. Watched projects are associated with the users profile.")
	@Override
	@RequestMapping(value = PROJECT_WATCH_URL, method = RequestMethod.POST)
	public void watchProject(@PathVariable(PROJECT_IDENTIFIER_URLPARAM) String projectIdentifier)
			throws EntityNotFoundException {
		profileWebService.watchProject(projectIdentifier);
	}

	@Section("Projects")
	@Title("Unwatch Project")
	@Documentation("Cease watching the given project.")
	@Override
	@RequestMapping(value = PROJECT_UNWATCH_URL, method = RequestMethod.POST)
	public void unwatchProject(@PathVariable(PROJECT_IDENTIFIER_URLPARAM) String projectIdentifier)
			throws EntityNotFoundException {
		profileWebService.unwatchProject(projectIdentifier);
	}

	@Section("Projects")
	@Title("Is Watching Project")
	@Documentation("Indicate if the current user is watching the given project.")
	@RequestMapping(value = PROJECT_WATCH_URL, method = RequestMethod.GET)
	public void isWatchingProject(@PathVariable(PROJECT_IDENTIFIER_URLPARAM) String projectIdentifier,
			HttpServletResponse response) throws EntityNotFoundException, IOException {
		Boolean isWatching = profileWebService.isWatchingProject(projectIdentifier);
		writeSimpleJsonObject(response, "isWatching", isWatching);
	}

	@Override
	public Boolean isWatchingProject(String projectIdentifier) throws EntityNotFoundException {
		return profileWebService.isWatchingProject(projectIdentifier);
	}

	@Section("Account Management")
	@Title("Create Sign-up Token")
	@Documentation("Create a new sign-up token, providing a name and email address. The returned token has a system-generated token identity that can be used when creating a new account. The system takes no action (i.e., an email is not sent), however the returned token is active and ready for use.")
	@Override
	@RequestMapping(value = CREATE_SIGNUP_TOKEN_URL, method = RequestMethod.POST)
	public SignUpToken createSignUpToken(@RequestBody SignUpToken token) throws ValidationException {
		return profileWebService.createSignUpToken(token);
	}

	@Section("Account Management")
	@Title("List Unused Sign-up Tokens")
	@Documentation("List sign-up tokens that have not yet been used to create a new account.")
	@Override
	@RequestMapping(value = GET_UNUSED_SIGNUP_TOKENS_URL, method = RequestMethod.GET)
	public List<SignUpToken> getUnusedSignUpTokens() {
		return profileWebService.getUnusedSignUpTokens();
	}

	@Section("Account Management")
	@Title("Send Sign-up Invitation")
	@Documentation("Send a system-generated invitation email for the given email address. The provided email address must have previously had a sign up token created for it.")
	@Override
	@RequestMapping(value = SEND_SIGNUP_INVITATION_URL, method = RequestMethod.POST)
	public void sendSignUpInvitation(@PathVariable(EMAIL_URLPARAM) String email) throws EntityNotFoundException {
		profileWebService.sendSignUpInvitation(email);
	}

	@Section("Account Management")
	@Title("Retrieve Sign-up Token")
	@Documentation("Retrieve a sign-up token by its identity.")
	@Override
	@RequestMapping(value = GET_SIGNUP_TOKEN_URL, method = RequestMethod.GET)
	public SignUpToken getSignUpToken(@PathVariable(TOKEN_URLPARAM) String token) throws EntityNotFoundException {
		return profileWebService.getSignUpToken(token);
	}

	@Section("Account Management")
	@Title("Create Profile With Sign-up Token")
	@Documentation("Create a new profile with the provided token. Sign-up tokens are required for creating profiles when the system is in invitation-only mode.")
	@RequestMapping(value = CREATE_PROFILE_WITH_SIGNUP_TOKEN_URL, method = RequestMethod.POST)
	public void createProfileWithSignUpToken(@RequestBody Profile profile, @PathVariable(TOKEN_URLPARAM) String token,
			HttpServletResponse response) throws ValidationException, EntityNotFoundException, IOException {

		Long profileId = this.createProfileWithSignUpToken(profile, token);
		writeSimpleJsonObject(response, "profileId", profileId);
	}

	@Section("Account Management")
	@Title("Create Proflie")
	@Documentation("Create a new profile. When the system is in invitation-only mode, a sign-up token is required.")
	@RequestMapping(value = CREATE_PROFILE_URL, method = RequestMethod.POST)
	public void createProfile(@RequestBody Profile profile, HttpServletResponse response) throws ValidationException,
			EntityNotFoundException, IOException {

		Long profileId = this.createProfileWithSignUpToken(profile, (String) null);
		super.writeSimpleJsonObject(response, "profileId", profileId);
	}

	@Override
	public Long createProfileWithSignUpToken(Profile profile, String token) throws EntityNotFoundException,
			ValidationException {
		return profileWebService.createProfileWithSignUpToken(profile, token);
	}

	@Section("Projects")
	@Title("Create Project Invitation")
	@Documentation("Invite a person to join a project team. The system sends an email inviting the person to join the project. When the invitation is accepted, the person will become a member of the project team.")
	@RequestMapping(value = INVITE_USER_TO_PROJECT_URL, method = RequestMethod.GET)
	public void inviteUserForProject(@PathVariable(USER_EMAIL_PARAM) String email,
			@PathVariable(PROJECT_IDENTIFIER_URLPARAM) String projectId, HttpServletResponse response)
			throws EntityNotFoundException, IOException {

		String token = profileWebService.inviteUserForProject(email, projectId);
		writeSimpleJsonObject(response, "token", token);
	}

	@Override
	public String inviteUserForProject(String email, String appIdentifier) throws EntityNotFoundException {
		return profileWebService.inviteUserForProject(email, appIdentifier);
	}

	@Section("Projects")
	@Title("Accept Project Invitation")
	@Documentation("Accept an invitation to join a project team. Upon success, the current user becomes a member of the project team.")
	@Override
	@RequestMapping(value = ACCEPT_INVITE_URL, method = RequestMethod.GET)
	public void acceptInvitation(@PathVariable(TOKEN_URLPARAM) String token) throws EntityNotFoundException {
		profileWebService.acceptInvitation(token);
	}

	@Section("Account Management")
	@Title("Retrieve Project Invitation Token")
	@Documentation("Retrieve a project invitation token by its identity.")
	@Override
	@RequestMapping(value = GET_PROJECT_INVITATION_TOKEN_URL, method = RequestMethod.GET)
	public ProjectInvitationToken getProjectInvitationToken(@PathVariable(TOKEN_URLPARAM) String token)
			throws EntityNotFoundException {
		return profileWebService.getProjectInvitationToken(token);
	}

	@Section("SSH Key Management")
	@Title("Create SSH Public Key")
	@Documentation("Create an SSH public key for the current account by specifying the public key content as the body of the request.  Supported public key formats include SSH2 (RFC 4716) and OpenSSH.")
	@Override
	@RequestMapping(value = CREATE_SSH_PUBLIC_KEY_URL, method = RequestMethod.POST)
	public SshPublicKey createSshPublicKey(@RequestBody SshPublicKeySpec publicKey) throws ValidationException {
		return profileWebService.createSshPublicKey(publicKey);
	}

	@Section("SSH Key Management")
	@Title("List SSH Public Keys")
	@Documentation("Retrieve SSH public keys associated with the current account.")
	@Override
	@RequestMapping(value = LIST_SSH_PUBLIC_KEYS_URL, method = RequestMethod.GET)
	public List<SshPublicKey> listSshPublicKeys() {
		return profileWebService.listSshPublicKeys();
	}

	@Section("SSH Key Management")
	@Title("Remove SSH Public Key")
	@Documentation("Remove an SSH public key from the current account.")
	@Override
	@RequestMapping(value = DELETE_SSH_PUBLIC_KEYS, method = RequestMethod.DELETE)
	public void removeSshPublicKey(@PathVariable(SSH_KEY_ID_PARAM) Long publicKeyId) throws EntityNotFoundException {
		profileWebService.removeSshPublicKey(publicKeyId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.profile.service.ProfileWebService#isProjectCreateAvailble()
	 */
	@Override
	public Boolean isProjectCreateAvailble() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.tasktop.c2c.server.profile.service.ProfileWebService#isPasswordResetTokenAvailable(java.lang.String)
	 */
	@Override
	public Boolean isPasswordResetTokenAvailable(String token) {
		// TODO Auto-generated method stub
		return null;
	}

}
