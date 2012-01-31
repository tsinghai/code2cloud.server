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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.web.server.AbstractAutowiredRemoteServiceServlet;
import com.tasktop.c2c.server.common.web.shared.AuthenticationFailedException;
import com.tasktop.c2c.server.common.web.shared.NoSuchEntityException;
import com.tasktop.c2c.server.common.web.shared.ValidationFailedException;
import com.tasktop.c2c.server.internal.profile.service.WebServiceDomain;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.internal.ProjectProfile;
import com.tasktop.c2c.server.profile.domain.project.Agreement;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.SignUpToken;
import com.tasktop.c2c.server.profile.domain.project.SignUpTokens;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKey;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKeySpec;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;
import com.tasktop.c2c.server.profile.service.ActivityService;
import com.tasktop.c2c.server.profile.service.ProfileServiceConfiguration;
import com.tasktop.c2c.server.profile.service.ProfileWebService;
import com.tasktop.c2c.server.profile.service.provider.HudsonServiceProvider;
import com.tasktop.c2c.server.profile.service.provider.TaskServiceProvider;
import com.tasktop.c2c.server.profile.web.client.ProfileService;
import com.tasktop.c2c.server.profile.web.shared.Credentials;
import com.tasktop.c2c.server.profile.web.shared.Profile;
import com.tasktop.c2c.server.profile.web.shared.ProjectDashboard;
import com.tasktop.c2c.server.profile.web.shared.ProjectRole;
import com.tasktop.c2c.server.profile.web.shared.ProjectTeamMember;
import com.tasktop.c2c.server.profile.web.shared.UserInfo;
import com.tasktop.c2c.server.scm.service.ScmService;

@SuppressWarnings("serial")
public class ProfileServiceImpl extends AbstractAutowiredRemoteServiceServlet implements ProfileService {

	@Autowired
	private ProfileServiceConfiguration configuration;

	@Autowired
	private ProfileServiceConfiguration profileServiceConfiguration;

	@Autowired
	@Qualifier("main")
	private com.tasktop.c2c.server.profile.service.ProfileService profileService;

	@Autowired
	@Qualifier("main")
	private ProfileWebService profileWebService;

	@Autowired
	@Qualifier("main")
	private ActivityService activityService;

	@Autowired
	@Qualifier("main")
	private ScmService scmService;

	@Autowired
	private TaskServiceProvider taskServiceProvider;

	@Autowired
	private HudsonServiceProvider hudsonServiceProvider;

	@Autowired
	private RememberMeServices rememberMeServices;

	@Autowired
	private LogoutHandler logoutHandler;

	@Override
	public ProjectDashboard getDashboard(String projectIdentifier) throws NoSuchEntityException {
		setTenancyContext(projectIdentifier);
		ProjectDashboard dashboard = new ProjectDashboard();
		int numDays = 60;

		dashboard.setProject(getProject(projectIdentifier));

		// Let some these services fails and still return data.
		try {
			dashboard.setTaskSummaries(taskServiceProvider.getTaskService(projectIdentifier).getHistoricalSummary(
					numDays));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			dashboard.setScmSummaries(scmService.getScmSummary(numDays));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			dashboard.setCommitsByAuthor(scmService.getNumCommitsByAuthor(numDays));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			dashboard.setBuildStatus(hudsonServiceProvider.getHudsonService(projectIdentifier).getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dashboard;
	}

	@Override
	public Credentials logon(String username, String password, boolean rememberMe) throws AuthenticationFailedException {

		try {
			Long profileId = profileService.authenticate(username, password);
			Credentials credentials = getCurrentUser();
			if (rememberMe && SecurityContextHolder.getContext().getAuthentication() != null) {
				rememberMeServices.loginSuccess(getThreadLocalRequest(), getThreadLocalResponse(),
						SecurityContextHolder.getContext().getAuthentication());
			}
			return credentials;
		} catch (AuthenticationException e) {
			throw new AuthenticationFailedException(e.getMessage());
		}
	}

	@Override
	public Boolean logout() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// If we have an anonymous token, we're not actually logged in.
		if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
			logoutHandler.logout(getThreadLocalRequest(), getThreadLocalResponse(), authentication);
			SecurityContextHolder.getContext().setAuthentication(null);

			// Only wipe out the session if we were logged in.
			HttpSession session = getThreadLocalRequest().getSession(false);
			if (session != null) {
				session.invalidate();
			}
		}
		return true;
	}

	private Credentials getCurrentUser() {
		com.tasktop.c2c.server.profile.domain.project.Profile profile = profileWebService.getCurrentProfile();
		if (profile == null) {
			return null;
		}
		List<String> roles = new ArrayList<String>();

		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
				roles.add(authority.getAuthority());
			}
		}

		return new Credentials(profile, roles);
	}

	@Override
	public UserInfo getCurrentUserInfo() {
		UserInfo ui = new UserInfo();
		ui.setCredentials(getCurrentUser());
		if (ui.getCredentials() != null) {
			List<Agreement> agreements;
			try {
				agreements = getPendingAgreements();
				ui.setHasPendingAgreements(agreements.size() > 0 ? true : false);
			} catch (NoSuchEntityException e) {
				ui.setHasPendingAgreements(false);
			}
		} else {
			ui.setHasPendingAgreements(false);
		}
		return ui;
	}

	@Override
	public Credentials updateProfile(com.tasktop.c2c.server.profile.domain.project.Profile p)
			throws ValidationFailedException, NoSuchEntityException {
		try {
			profileWebService.updateProfile(p);
			String newPassword = p.getPassword();
			if (newPassword != null && newPassword.length() > 0) {
				return logon(p.getUsername(), newPassword, false);
			}
			return getCurrentUser();
		} catch (ValidationException e) {
			handle(e);
		} catch (EntityNotFoundException e) {
			handle(e);
		} catch (AuthenticationFailedException e) {
			// should never happen
			throw new IllegalStateException(e);
		}

		// should never happen
		throw new IllegalStateException();
	}

	@Override
	public String createProject(Credentials credentials, Project project) throws ValidationFailedException,
			NoSuchEntityException {
		try {
			return profileWebService.createProject(credentials.getProfile().getId(), project).getIdentifier();
		} catch (ValidationException e) {
			handle(e);
			throw new IllegalStateException();
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Override
	public Project updateProject(Project project) throws NoSuchEntityException, ValidationFailedException {
		try {
			return profileWebService.updateProject(project);
		} catch (ValidationException e) {
			handle(e);
			throw new IllegalStateException();
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Override
	public Project getProject(String projectIdentifier) throws NoSuchEntityException {
		setTenancyContext(projectIdentifier);

		try {
			return profileWebService.getProjectByIdentifier(projectIdentifier);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Override
	public String[] getRolesForProject(String projectIdentifier) throws NoSuchEntityException {
		setTenancyContext(projectIdentifier);

		try {
			return profileWebService.getRolesForProject(projectIdentifier);
		} catch (EntityNotFoundException enfe) {
			handle(enfe);
			throw new IllegalStateException();
		}
	}

	@Override
	public Boolean updateTeamMemberRoles(String projectIdentifier, ProjectTeamMember member)
			throws NoSuchEntityException, ValidationFailedException {
		setTenancyContext(projectIdentifier);

		try {
			com.tasktop.c2c.server.profile.domain.internal.Project project = profileService
					.getProjectByIdentifier(projectIdentifier);

			ProjectProfile projectProfile = profileService.getProjectProfile(project.getId(), member.getProfile()
					.getId());

			// Wire in our updated roles now.
			projectProfile.setOwner(member.getRoles().contains(ProjectRole.OWNER));
			projectProfile.setUser(member.getRoles().contains(ProjectRole.MEMBER));

			profileService.updateProjectProfile(projectProfile);
		} catch (EntityNotFoundException e) {
			handle(e);
		} catch (ValidationException e) {
			handle(e);
		}
		return true;
	}

	@Override
	public QueryResult<Project> getProjects(ProjectRelationship projectRelationship, QueryRequest queryRequest) {
		return profileWebService.findProjects(projectRelationship, queryRequest);
	}

	@Override
	public Boolean requestPasswordReset(String email) throws NoSuchEntityException {
		try {
			profileService.requestPasswordReset(email);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
		return true;
	}

	@Override
	public Boolean isTokenAvailable(String token) {
		return profileService.isPasswordResetTokenAvailable(token);
	}

	@Override
	public Credentials resetPassword(String token, String newPassword) throws NoSuchEntityException,
			ValidationFailedException, AuthenticationFailedException {
		try {
			String username = profileService.resetPassword(token, newPassword);
			Long profileId = profileService.authenticate(username, newPassword);
			return getCurrentUser();
		} catch (ValidationException e) {
			handle(e);
			throw new IllegalStateException();
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		} catch (AuthenticationException e) {
			throw new AuthenticationFailedException();
		}
	}

	@Override
	public List<Profile> getProfiles(String projectIdentifier) throws NoSuchEntityException {
		return getProfiles(projectIdentifier, null, 20);
	}

	@Override
	public List<Profile> getProfiles(String projectIdentifier, String query, int limit) throws NoSuchEntityException {
		com.tasktop.c2c.server.profile.domain.internal.Project project;
		try {
			project = profileService.getProjectByIdentifier(projectIdentifier);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
		List<Profile> profiles = new ArrayList<Profile>();
		for (ProjectProfile profile : project.getProjectProfiles()) {
			Profile copy = WebDomain.copy(profile.getProfile());
			if (matches(query, copy)) {
				profiles.add(copy);
			}
		}
		Collections.sort(profiles);
		if (limit > 0) {
			while (profiles.size() > limit) {
				profiles.remove(profiles.size() - 1);
			}
		}
		// don't leak email addresses
		for (Profile profile : profiles) {
			profile.setEmail(null);
		}
		return profiles;
	}

	@Override
	public QueryResult<Profile> findProfiles(String query, QueryRequest request) {
		QueryResult<com.tasktop.c2c.server.profile.domain.internal.Profile> result = profileService.findProfiles(query,
				request.getPageInfo(), request.getSortInfo());
		QueryResult<Profile> queryResult = new QueryResult<Profile>(result.getOffset(), result.getPageSize(),
				WebDomain.copy(result.getResultPage()), result.getTotalResultSize());
		// don't leak email addresses
		for (Profile profile : queryResult.getResultPage()) {
			profile.setEmail(null);
		}
		return queryResult;
	}

	private Boolean matches(String query, Profile profile) {
		if (query == null) {
			return true;
		}
		query = query.toLowerCase();
		if (profile.getFirstName().toLowerCase().startsWith(query)) {
			return true;
		}
		if (profile.getLastName().toLowerCase().startsWith(query)) {
			return true;
		}
		if (profile.getUsername().toLowerCase().startsWith(query)) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean addTeamMemberByEmail(String projectIdentifier, String personEmail) throws NoSuchEntityException {
		setTenancyContext(projectIdentifier);

		com.tasktop.c2c.server.profile.domain.internal.Project project;
		try {
			project = profileService.getProjectByIdentifier(projectIdentifier);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
		com.tasktop.c2c.server.profile.domain.internal.Profile profile = profileService.getProfileByEmail(personEmail);
		if (profile == null) {
			throw new NoSuchEntityException();
		}
		try {
			profileService.addProjectProfile(project.getId(), profile.getId());
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
		return true;
	}

	@Override
	public boolean removeTeamMember(String projectIdentifier, ProjectTeamMember member) throws NoSuchEntityException,
			ValidationFailedException {
		setTenancyContext(projectIdentifier);

		com.tasktop.c2c.server.profile.domain.internal.Project project;
		try {
			project = profileService.getProjectByIdentifier(projectIdentifier);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
		try {
			profileService.removeProjectProfile(project.getId(), member.getProfile().getId());
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		} catch (ValidationException e) {
			handle(e);
			throw new IllegalStateException();
		}
		return true;
	}

	@Override
	public List<ProjectActivity> getShortActivityList(String projectIdentifier) {
		setTenancyContext(projectIdentifier);

		return activityService.getShortActivityList(projectIdentifier);
	}

	@Override
	public List<ProjectActivity> getRecentActivity(String projectIdentifier) {
		setTenancyContext(projectIdentifier);
		return activityService.getRecentActivity(projectIdentifier);
	}

	private List<com.tasktop.c2c.server.profile.domain.project.Agreement> getPendingAgreements()
			throws NoSuchEntityException {
		List<com.tasktop.c2c.server.profile.domain.project.Agreement> agreements;
		try {
			agreements = profileWebService.getPendingAgreements();
			return agreements;
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Override
	public void createProjectGitRepository(String projectIdentifier, ScmRepository scmRepository)
			throws NoSuchEntityException, ValidationFailedException {
		setTenancyContext(projectIdentifier);

		try {
			scmService.createScmRepository(scmRepository);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		} catch (ValidationException e) {
			handle(e);
			throw new IllegalStateException();
		}

	}

	@Override
	public void deleteProjectGitRepository(String projectIdentifier, Long repositoryId) throws NoSuchEntityException {
		setTenancyContext(projectIdentifier);

		try {
			scmService.deleteScmRepository(repositoryId);
		} catch (EntityNotFoundException enfe) {
			handle(enfe);
			throw new IllegalStateException();
		}
	}

	@Override
	public void inviteUserForProject(String email, String projectIdentifier) throws NoSuchEntityException {
		setTenancyContext(projectIdentifier);

		try {
			profileService.inviteUserForProject(email, projectIdentifier);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Override
	public void acceptInvitation(String invitationToken) throws NoSuchEntityException {
		try {
			profileService.acceptInvitation(invitationToken);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Override
	public Project getProjectForInvitationToken(String token) throws NoSuchEntityException {
		try {
			return profileWebService.getProjectForInvitationToken(token);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Override
	public QueryResult<Project> findProjects(String query, QueryRequest request) {
		QueryResult<Project> result = profileWebService.findProjects(query, request);
		return result;
	}

	@Override
	public void watchProject(String projectIdentifier) throws NoSuchEntityException {
		setTenancyContext(projectIdentifier);

		try {
			profileWebService.watchProject(projectIdentifier);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Override
	public void unwatchProject(String projectIdentifier) throws NoSuchEntityException {
		setTenancyContext(projectIdentifier);

		try {
			profileWebService.unwatchProject(projectIdentifier);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Override
	public Boolean isWatchingProject(String projectIdentifier) throws NoSuchEntityException {
		setTenancyContext(projectIdentifier);

		try {
			return profileWebService.isWatchingProject(projectIdentifier);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Override
	public UserInfo createProfileWithSignUpToken(com.tasktop.c2c.server.profile.domain.project.Profile profile,
			String token) throws NoSuchEntityException, ValidationFailedException {
		try {
			profileWebService.createProfileWithSignUpToken(profile, token);
			logon(profile.getUsername(), profile.getPassword(), false);
			return getCurrentUserInfo();
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		} catch (ValidationException e) {
			handle(e);
			throw new IllegalStateException();
		} catch (AuthenticationFailedException e) {
			// should never happen
			throw new IllegalStateException(e);
		}
	}

	@Override
	public List<Profile> listAllProfiles() {
		// Don't worry - this is secured at the next level, so only a system admin account can call it.
		return WebDomain.copy(profileService.listAllProfiles());
	}

	@Override
	public SignUpTokens createSignUpTokensFromCsv(String csv, Boolean sendEmail) throws ValidationFailedException {
		SignUpTokens invitationTokens = new SignUpTokens();
		try {
			BufferedReader reader = new BufferedReader(new StringReader(csv));
			String line;
			try {
				int lineNumber = 0;
				while ((line = reader.readLine()) != null) {
					++lineNumber;
					String trimmedLine = line.trim();
					if (trimmedLine.length() > 0) {
						String[] split = trimmedLine.split(",");
						if (split.length == 3) {
							SignUpToken signUpToken = new SignUpToken();
							signUpToken.setFirstname(unescapeCsvValue(split[0]));
							signUpToken.setLastname(unescapeCsvValue(split[1]));
							signUpToken.setEmail(unescapeCsvValue(split[2]));
							invitationTokens.add(signUpToken);
						} else {
							throw new IOException("Invalid format at line " + lineNumber
									+ ": expecting First Name, Last Name, Email");
						}
					}
				}
			} catch (IOException e) {
				Errors errors = new BeanPropertyBindingResult(invitationTokens, "invitations");
				errors.reject("invalidFormat", "Cannot read CSV: " + e.getMessage());
				throw new ValidationException(errors);
			}
			return profileService.createInvitations(invitationTokens, sendEmail);
		} catch (ValidationException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	private String unescapeCsvValue(String value) {
		if (value != null) {
			value = value.trim();
			if (value.length() > 1 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
				value = value.substring(1, value.length() - 1);
			}
			value = value.replace("\"\"", "\"");
		}
		return value;
	}

	@Override
	public SshPublicKey createSshPublicKey(SshPublicKeySpec publicKey) throws ValidationFailedException {
		try {
			return profileWebService.createSshPublicKey(publicKey);
		} catch (ValidationException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Autowired
	private WebServiceDomain webServiceDomain;

	@Override
	public SshPublicKey updateSshPublicKey(SshPublicKey publicKey) throws ValidationFailedException {
		try {
			return webServiceDomain.copy(profileService.updateSshPublicKey(webServiceDomain.copy(publicKey)));
		} catch (ValidationException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	@Override
	public void removeSshPublicKey(Long publicKeyId) throws NoSuchEntityException {
		try {
			profileWebService.removeSshPublicKey(publicKeyId);
		} catch (EntityNotFoundException e) {
			handle(e);
			throw new IllegalStateException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.profile.web.ui.client.ProfileService#verifyEmail()
	 */
	@Override
	public void verifyEmail() {
		profileService.sendVerificationEmail();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.profile.web.ui.client.ProfileService#verifyEmailToken(java.lang.String)
	 */
	@Override
	public void verifyEmailToken(String token) throws NoSuchEntityException, ValidationFailedException {
		try {
			profileService.verifyEmail(token);
		} catch (EntityNotFoundException e) {
			handle(e);
		} catch (ValidationException e) {
			handle(e);
		}
	}
}
