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
package com.tasktop.c2c.server.internal.profile.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.auth.service.AuthUtils;
import com.tasktop.c2c.server.auth.service.AuthenticationService;
import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.common.service.Security;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.job.JobService;
import com.tasktop.c2c.server.internal.profile.crypto.PublicKeyReader;
import com.tasktop.c2c.server.profile.domain.Email;
import com.tasktop.c2c.server.profile.domain.internal.Agreement;
import com.tasktop.c2c.server.profile.domain.internal.AgreementProfile;
import com.tasktop.c2c.server.profile.domain.internal.BaseEntity;
import com.tasktop.c2c.server.profile.domain.internal.ConfigurationProperty;
import com.tasktop.c2c.server.profile.domain.internal.EmailVerificationToken;
import com.tasktop.c2c.server.profile.domain.internal.InvitationToken;
import com.tasktop.c2c.server.profile.domain.internal.PasswordResetToken;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ProjectProfile;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.domain.internal.RandomToken;
import com.tasktop.c2c.server.profile.domain.internal.SignUpToken;
import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.SignUpTokens;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKeySpec;
import com.tasktop.c2c.server.profile.domain.validation.ProfilePasswordValidator;
import com.tasktop.c2c.server.profile.service.EmailService;
import com.tasktop.c2c.server.profile.service.NotificationService;
import com.tasktop.c2c.server.profile.service.ProfileService;
import com.tasktop.c2c.server.profile.service.ProfileServiceConfiguration;
import com.tasktop.c2c.server.profile.service.ProjectServiceService;
import com.tasktop.c2c.server.profile.service.provider.TaskServiceProvider;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.Team;

/**
 * Main implementation of the {@link ProfileService} using JPA.
 * 
 * @author David Green <david.green@tasktop.com> (Tasktop Technologies Inc.)
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * @author Lucas Panjer <lucas.panjer@tasktop.com> (Tasktop Technologies Inc.)
 * @author Ryan Slobojon <ryan.slobojan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@Service("profileService")
@Qualifier("main")
@Transactional(rollbackFor = { Exception.class })
public class ProfileServiceBean extends AbstractJpaServiceBean implements ProfileService {

	private static final int MAX_SIZE = 1000;

	@Autowired
	private EmailService emailService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private SecurityPolicy securityPolicy;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private ProjectServiceService projectServiceService;

	@Autowired
	private ProfileServiceConfiguration configuration;

	@Autowired
	private VelocityEngine velocityEngine;

	@Autowired
	private JobService jobService;

	@Autowired
	private TaskServiceProvider taskServiceProvider;

	@Autowired(required = true)
	@Qualifier("main")
	private PublicKeyReader publicKeyReader;

	@Autowired
	private WebServiceDomain webServiceDomain;

	@Autowired
	private NotificationService notificationService;

	private String profileCreatedTemplate = "com/tasktop/c2c/server/internal/profile/service/template/profileCreated.vm";
	private String passwordResetTemplate = "com/tasktop/c2c/server/internal/profile/service/template/passwordResetRequest.vm";
	private String projectInvitationTemplate = "com/tasktop/c2c/server/internal/profile/service/template/projectInvitation.vm";
	private String signUpInvitationTemplate = "com/tasktop/c2c/server/internal/profile/service/template/signUpInvitation.vm";
	private String emailVerificationTemplate = "com/tasktop/c2c/server/internal/profile/service/template/emailVerification.vm";

	public void setTaskServiceProvider(TaskServiceProvider taskServiceProvider) {
		this.taskServiceProvider = taskServiceProvider;
	}

	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}

	private final class AccountConstraintsValidator implements Validator {
		// FIXME: we could move this into a registered validator if we could
		// figure out how to inject the entity manager (request scope)
		@SuppressWarnings("unchecked")
		@Override
		public void validate(Object target, Errors errors) {
			Profile profile = (Profile) target;
			if (profile.getUsername() != null && profile.getUsername().trim().length() > 0) {
				List<Profile> usersWithUsername = entityManager
						.createQuery(
								"select e from " + Profile.class.getSimpleName() + " e where e.username = :username")
						.setParameter("username", profile.getUsername()).getResultList();
				if (!usersWithUsername.isEmpty()) {
					if (usersWithUsername.size() != 1 || !usersWithUsername.get(0).equals(profile)) {
						errors.reject("profile.usernameUnique", new Object[] { profile.getUsername() }, null);
					}
				}
			}
			if (profile.getEmail() != null && profile.getEmail().trim().length() > 0) {
				List<Profile> usersWithEmail = entityManager
						.createQuery("select e from " + Profile.class.getSimpleName() + " e where e.email = :email")
						.setParameter("email", profile.getEmail()).getResultList();
				if (!usersWithEmail.isEmpty()) {
					if (usersWithEmail.size() != 1 || !usersWithEmail.get(0).equals(profile)) {
						errors.reject("profile.emailUnique", new Object[] { profile.getEmail() }, null);
					}
				}
			}
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return Profile.class.isAssignableFrom(clazz);
		}
	}

	private final class ProjectConstraintsValidator implements Validator {

		@Override
		public boolean supports(Class<?> clazz) {
			return Project.class.isAssignableFrom(clazz);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void validate(Object target, Errors errors) {
			Project project = (Project) target;
			if (project.getName() != null && project.getName().trim().length() > 0) {
				List<Project> projectsWithName = entityManager
						.createQuery(
								"select e from " + Project.class.getSimpleName()
										+ " e where e.name = :name or e.identifier = :identifier")
						.setParameter("name", project.getName()).setParameter("identifier", project.getIdentifier())
						.getResultList();
				if (!projectsWithName.isEmpty()) {
					if (projectsWithName.size() != 1 || !projectsWithName.get(0).equals(project)) {
						errors.reject("project.nameUnique", new Object[] { project.getName() }, null);
					}
				}
			}
		}

	}

	@Override
	public Long createProfile(Profile profile) throws ValidationException {
		if (entityManager.contains(profile)) {
			throw new IllegalArgumentException();
		}
		fixupCase(profile);

		securityPolicy.create(profile);

		validate(profile, validator, new AccountConstraintsValidator(), new ProfilePasswordValidator());
		profile.setId(null);
		profile.setPassword(passwordEncoder.encodePassword(profile.getPassword(), null));
		profile.setNotificationSettings(notificationService.constructDefaultSettings());
		profile.setDisabled(false);
		entityManager.persist(profile);
		sendVerificationEmail(profile);

		return profile.getId();
	}

	private void sendWelcomEmail(Profile profile) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("profile", profile);
		model.put("AppName", configuration.getAppName());
		model.put("AppBaseUrl", configuration.getProfileBaseUrl());

		String bodyText = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, profileCreatedTemplate, model);
		Email email = new Email(profile.getEmail(), "Welcome to " + configuration.getAppName(), bodyText, "text/plain");
		emailService.schedule(email);
	}

	@Secured({ Role.User, Role.Admin })
	@Override
	public void updateProfile(Profile profile) throws ValidationException, EntityNotFoundException {
		fixupCase(profile);

		securityPolicy.modify(profile);

		boolean passwordReset = false;
		Profile managedProfile = profile;
		boolean nameChanged = true;
		boolean emailChanged = true;
		if (!entityManager.contains(profile)) {
			managedProfile = entityManager.find(Profile.class, profile.getId());
			if (managedProfile == null) {
				throw new EntityNotFoundException();
			}
			nameChanged = !managedProfile.getFirstName().equals(profile.getFirstName())
					|| !managedProfile.getLastName().equals(profile.getLastName());
			emailChanged = !managedProfile.getEmail().equals(profile.getEmail());

			managedProfile.setEmail(profile.getEmail());
			managedProfile.setFirstName(profile.getFirstName());
			managedProfile.setLastName(profile.getLastName());
			if (profile.getNotificationSettings() != null) {
				managedProfile.getNotificationSettings().setEmailTaskActivity(
						profile.getNotificationSettings().getEmailTaskActivity());
				managedProfile.getNotificationSettings().setEmailNewsAndEvents(
						profile.getNotificationSettings().getEmailNewsAndEvents());
				managedProfile.getNotificationSettings().setEmailServiceAndMaintenance(
						profile.getNotificationSettings().getEmailServiceAndMaintenance());
			}
			if (profile.getPassword() != null && profile.getPassword().trim().length() > 0) {
				passwordReset = true;
				managedProfile.setPassword(profile.getPassword());
			}
			if (getCurrentUserProfile() != null && getCurrentUserProfile().getAdmin()
					&& !getCurrentUserProfile().equals(profile)) {
				managedProfile.setDisabled(profile.getDisabled());
			}
		}
		List<Validator> validators = new ArrayList<Validator>();
		validators.add(validator);
		validators.add(new AccountConstraintsValidator());
		if (passwordReset) {
			validators.add(new ProfilePasswordValidator());
		}
		validate(managedProfile, validators);
		if (passwordReset) {
			managedProfile.setPassword(passwordEncoder.encodePassword(managedProfile.getPassword(), null));
		}
		if (nameChanged || emailChanged) {
			for (ProjectProfile projectProfile : managedProfile.getProjectProfiles()) {
				jobService.schedule(new ReplicateProjectTeamJob(projectProfile.getProject()));
			}
		}
		if (emailChanged) {
			managedProfile.setEmailVerified(false);
			sendVerificationEmail(managedProfile);
		}
	}

	@Secured(Role.User)
	@Override
	public void updateProjectProfile(ProjectProfile projectProfile) throws EntityNotFoundException, ValidationException {
		securityPolicy.modify(projectProfile);
		if (!entityManager.contains(projectProfile)) {
			ProjectProfile managedApplicationProfile = entityManager.find(ProjectProfile.class, projectProfile.getId());
			if (managedApplicationProfile == null) {
				throw new EntityNotFoundException();
			}

			managedApplicationProfile.setOwner(projectProfile.getOwner());
			managedApplicationProfile.setUser(projectProfile.getUser());
			managedApplicationProfile.setCommunity(projectProfile.getCommunity());

			// test for user removing self as owner, disallow
			if (!managedApplicationProfile.getOwner()
					&& getCurrentUserProfile().equals(managedApplicationProfile.getProfile())) {
				Errors errors = createErrors(projectProfile);
				errors.reject("project.ownerCannotRemoveSelf");
				throw new ValidationException(errors);
			}
			projectProfile = managedApplicationProfile;
		}
		validate(projectProfile, validator);
	}

	@Secured(Role.User)
	@Override
	public ProjectProfile getProjectProfile(Long projectId, Long profileId) throws EntityNotFoundException {
		ProjectProfile projectProfile;
		try {
			projectProfile = (ProjectProfile) entityManager
					.createQuery(
							"select e from " + ProjectProfile.class.getSimpleName()
									+ " e where e.profile.id = :p and e.project.id = :a").setParameter("p", profileId)
					.setParameter("a", projectId).getSingleResult();
		} catch (NoResultException e) {
			throw new EntityNotFoundException();
		}
		securityPolicy.retrieve(projectProfile);
		return projectProfile;
	}

	/**
	 * username and email should always be lower-case to avoid potential for duplicates
	 */
	private void fixupCase(Profile profile) {
		if (profile.getEmail() != null) {
			profile.setEmail(profile.getEmail().toLowerCase());
		}
		if (profile.getUsername() != null) {
			profile.setUsername(profile.getUsername().toLowerCase());
		}
	}

	@Override
	public Long authenticate(String username, String password) throws AuthenticationException {
		SecurityContextHolder.getContext().setAuthentication(null);

		AuthenticationToken token = authenticationService.authenticate(username, password);
		AuthenticationServiceUser user = AuthenticationServiceUser.fromAuthenticationToken(token, password);

		AuthUtils.insertNewAuthToken(user, password, token.getAuthorities(), token);

		try {
			return getProfileByUsername(username).getId();
		} catch (EntityNotFoundException e) {
			// should never happen
			throw new IllegalStateException(e);
		}
	}

	@Secured(Role.User)
	@Override
	public Profile getProfile(Long id) throws EntityNotFoundException {
		Profile profile = getProfileInternal(id);
		securityPolicy.retrieve(profile);
		return profile;
	}

	private Profile getProfileInternal(Long id) throws EntityNotFoundException {
		if (id != null) {
			Profile profile = entityManager.find(Profile.class, id);
			if (profile != null) {
				return profile;
			}
		}
		throw new EntityNotFoundException();
	}

	@Secured(Role.User)
	@Override
	public Profile getProfileByEmail(String emailAddress) {

		if (emailAddress != null) {
			String queryAddress = emailAddress.trim();
			try {
				return getEntityByField("email", queryAddress, Profile.class);
			} catch (EntityNotFoundException e) {
				// ignore, we'll fall through and return null
				// FIXME for consistency, shouldn't we throw an EntityNotFoundException?
			}
		}
		return null;
	}

	private Profile privateGetProfileByEmail(String emailAddress) {
		if (emailAddress != null) {
			emailAddress = emailAddress.trim();
			if (emailAddress.length() > 0) {
				CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
				CriteriaQuery<Profile> query = criteriaBuilder.createQuery(Profile.class);
				Root<Profile> root = query.from(Profile.class);
				query.select(root).where(criteriaBuilder.equal(root.get("email"), emailAddress));
				try {
					Profile profile = entityManager.createQuery(query).getSingleResult();
					return profile;
				} catch (NoResultException e) {
					// ignore
				}
			}
		}
		return null;
	}

	@Secured(Role.User)
	@Override
	public Profile getProfileByUsername(String username) throws EntityNotFoundException {
		return getEntityByField("username", username, Profile.class);
	}

	@Secured(Role.User)
	@Override
	public Project createProject(Long profileId, Project project) throws ValidationException, EntityNotFoundException {

		// Before we start this createProject call, check to see if we have room for more projects in the system right
		// now
		if (!spaceAvailableForNewProject()) {
			// Construct and throw a new ValidationException
			Errors errors = createErrors(project);
			errors.reject("project.maxNumReached");
			throw new ValidationException(errors);
		}

		project.computeIdentifier();

		securityPolicy.create(project);
		setDefaultValuesBeforeCreate(project);
		validate(project, validator, new ProjectConstraintsValidator());

		Profile profile = getProfileInternal(profileId);
		securityPolicy.modify(profile);

		project.setId(null);
		entityManager.persist(project);

		ProjectProfile projectProfile = project.addProfile(profile);

		// Mark project create as both project user and owner
		projectProfile.setUser(true);
		projectProfile.setOwner(true);

		entityManager.persist(projectProfile);

		try {
			projectServiceService.provisionDefaultServices(project.getId());
		} catch (ProvisioningException e) {
			throw new RuntimeException(e); // FIXME
		}

		return project;
	}

	@Override
	public Boolean isProjectCreateAvailable() {
		return spaceAvailableForNewProject();
	}

	private boolean spaceAvailableForNewProject() {

		try {
			// First, grab our system maxProject count to see if there's a limit present.
			ConfigurationProperty maxProjNumProp = getConfigurationProperty(ConfigurationProperty.MAXNUM_PROJECTS_NAME);

			// Convert it to an int since it's stored as a string
			int maxNum = Integer.parseInt(maxProjNumProp.getValue());

			// Now, count up the number of projects in the system
			int curNumProjects = getEntityCount(Project.class);

			// Do our check and send back our result.
			return (curNumProjects < maxNum);

		} catch (EntityNotFoundException e) {
			// No config prop present, so that means there's no limit - return true.
			return true;
		}
	}

	private <T> int getEntityCount(Class<T> entityClass) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
		Root<T> root = query.from(entityClass);
		query.select(criteriaBuilder.count(root));
		return entityManager.createQuery(query).getSingleResult().intValue();
	}

	private ConfigurationProperty getConfigurationProperty(String name) throws EntityNotFoundException {
		return getEntityByField("name", name, ConfigurationProperty.class);
	}

	private <T> T getEntityByField(String fieldName, String fieldValue, Class<T> entityClass)
			throws EntityNotFoundException {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = criteriaBuilder.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		query.select(root).where(criteriaBuilder.equal(root.get(fieldName), fieldValue));
		try {
			T retObj = entityManager.createQuery(query).getSingleResult();
			securityPolicy.retrieve(retObj);
			return retObj;
		} catch (NoResultException e) {
			throw new EntityNotFoundException();
		}
	}

	private void setDefaultValuesBeforeCreate(Project project) {
		if (project.getPublic() == null) {
			project.setPublic(false);
		}
	}

	@Secured(Role.User)
	@Override
	public Project updateProject(Project project) throws EntityNotFoundException, ValidationException {

		securityPolicy.modify(project);

		Project managedProject = project;
		managedProject = entityManager.find(Project.class, project.getId());
		if (managedProject == null) {
			throw new EntityNotFoundException();
		}

		validate(project, validator, new ProjectConstraintsValidator());

		if (!entityManager.contains(project)) {
			// we disallow change of identifier
			managedProject.setName(project.getName());
			managedProject.setDescription(project.getDescription());
			managedProject.setPublic(project.getPublic());
		}

		// Since our update is now done, return our project to the caller.
		return managedProject;
	}

	@Secured(Role.User)
	@Override
	public Project getProject(Long id) throws EntityNotFoundException {
		if (id != null) {
			Project project = entityManager.find(Project.class, id);
			if (project != null) {

				securityPolicy.retrieve(project);

				return project;
			}
		}
		throw new EntityNotFoundException();
	}

	@Override
	public Project getProjectByIdentifier(String identity) throws EntityNotFoundException {
		if (identity == null) {
			throw new IllegalArgumentException();
		}

		try {
			Project project = (Project) entityManager
					.createQuery("select a from " + Project.class.getSimpleName() + " a where a.identifier = :i")
					.setParameter("i", identity).getSingleResult();

			securityPolicy.retrieve(project);

			return project;
		} catch (NoResultException e) {
			throw new EntityNotFoundException();
		}
	}

	@Secured(Role.User)
	@Override
	public List<Project> getProfileProjects(Long profileId) throws EntityNotFoundException {
		Profile profile = getProfileInternal(profileId);
		if (profile != null) {
			securityPolicy.modify(profile);

			List<Project> projects = (List<Project>) entityManager
					.createQuery(
							"SELECT DISTINCT project FROM " + Project.class.getSimpleName()
									+ " project, IN(project.projectProfiles) pp WHERE pp.profile.id = :id "
									+ createSortClause("project", Project.class, new SortInfo("name")))
					.setParameter("id", profile.getId()).getResultList();

			for (Project project : projects) {
				securityPolicy.retrieve(project);
			}
			return projects;
		}
		return null;
	}

	@Override
	public void requestPasswordReset(String emailAddress) throws EntityNotFoundException {
		Profile profile = privateGetProfileByEmail(emailAddress);

		if (profile != null) {
			PasswordResetToken passwordResetToken = addPasswordResetToken(profile);
			String passwordResetURL = configuration.getProfilePasswordResetURL(passwordResetToken.getToken());
			emailPasswordResetMessage(profile, passwordResetURL);
		} else {
			throw new EntityNotFoundException();
		}
	}

	private void emailPasswordResetMessage(Profile profile, String passwordResetURL) {
		Map<String, String> model = new HashMap<String, String>();
		model.put("passwordResetURL", passwordResetURL);
		model.put("AppName", configuration.getAppName());
		model.put("AppBaseUrl", configuration.getProfileBaseUrl());
		model.put("username", profile.getUsername());

		String bodyText = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, passwordResetTemplate, model);
		Email email = new Email(profile.getEmail(), "Password Reset", bodyText, "text/plain");
		emailService.schedule(email);
	}

	private PasswordResetToken addPasswordResetToken(Profile profile) {
		PasswordResetToken passwordResetToken = new PasswordResetToken();
		passwordResetToken.setDateCreated(new Date());
		passwordResetToken.setProfile(profile);
		passwordResetToken.setToken(UUID.randomUUID().toString());
		passwordResetToken.setDateUsed(null);
		profile.getPasswordResetTokens().add(passwordResetToken);
		entityManager.persist(profile);
		return passwordResetToken;
	}

	private PasswordResetToken getPasswordResetToken(String token) {
		return getToken(token, PasswordResetToken.class);
	}

	private InvitationToken getInvitationToken(String token) {
		return getToken(token, InvitationToken.class);
	}

	private EmailVerificationToken getEmailVerificationToken(String token) {
		return getToken(token, EmailVerificationToken.class);
	}

	@Override
	public String resetPassword(String token, String newPassword) throws EntityNotFoundException, ValidationException {
		SecurityContextHolder.getContext().setAuthentication(null);
		// load the token
		PasswordResetToken passwordResetToken = this.getPasswordResetToken(token);
		if (passwordResetToken == null || passwordResetToken.getDateUsed() != null) {
			throw new EntityNotFoundException();
		}

		Profile profile = passwordResetToken.getProfile();
		if (profile == null) {
			// shouldn't ever have a detached token.
			throw new EntityNotFoundException();
		}

		// set the profile password, validate, and persist
		profile.setPassword(newPassword);
		validate(profile, validator, new ProfilePasswordValidator());
		profile.setPassword(passwordEncoder.encodePassword(newPassword, null));
		entityManager.persist(profile);

		// mark token as used
		passwordResetToken.setDateUsed(new Date());
		entityManager.persist(passwordResetToken);
		return profile.getUsername();
	}

	@Override
	public Boolean isPasswordResetTokenAvailable(String token) {

		PasswordResetToken dbToken = getPasswordResetToken(token);

		// We want to return true if we have a token with an empty used-date.
		return (dbToken != null) && (dbToken.getDateUsed() == null);
	}

	@Secured(Role.User)
	@Override
	public String inviteUserForProject(String email, String appIdentifier) throws EntityNotFoundException {

		// Pull in our domain objects now.
		Project project = getProjectByIdentifier(appIdentifier);
		Profile profile = getCurrentUserProfile();

		// Pre-populate a new Invitation token.
		InvitationToken token = new InvitationToken();
		token.setDateCreated(new Date());
		token.setProject(project);
		token.setIssuingProfile(profile);
		token.setToken(UUID.randomUUID().toString());
		token.setEmail(email);

		securityPolicy.create(token);

		// Do our persist before our invite, so that an issue in sending the email doesn't prevent the invitation itself
		// from existing in the system.
		entityManager.persist(token);

		// Send an email to this user now.
		emailProjectInvitationMessage(token);
		return token.getToken();
	}

	private void emailProjectInvitationMessage(InvitationToken token) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("profile", token.getIssuingProfile());
		model.put("project", token.getProject());
		model.put("invitePageUrl", configuration.getInvitationURL(token.getToken()));
		model.put("AppName", configuration.getAppName());

		String bodyText = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, projectInvitationTemplate, model);
		Email email = new Email(token.getEmail(), token.getIssuingProfile().getFirstName()
				+ " has invited you to a project on " + configuration.getAppName(), bodyText, "text/plain");
		emailService.schedule(email);
	}

	@SuppressWarnings("unchecked")
	private <T extends RandomToken> T getToken(String token, Class<T> tokenType) {

		if (token == null || token.trim().length() == 0) {
			return null;
		}

		String queryStr = String.format("select t from %s t where t.token = :tokenStr", tokenType.getSimpleName());
		try {
			return (T) entityManager.createQuery(queryStr).setParameter("tokenStr", token.trim()).getSingleResult();
		} catch (NoResultException nre) {
			// Catch this and return null.
			return null;
		}
	}

	@Override
	public Project getProjectForInvitationToken(String invitationToken) throws EntityNotFoundException {

		// Get the associated token now.
		InvitationToken token = getInvitationToken(invitationToken);

		// If this token is either missing or already consumed, throw an exception indicating that it doesn't exist.
		if (token == null || token.getDateUsed() != null) {
			throw new EntityNotFoundException();
		}

		return token.getProject();
	}

	@Secured(Role.User)
	@Override
	public void acceptInvitation(String invitationToken) throws EntityNotFoundException {

		// Get the associated token now.
		InvitationToken token = getInvitationToken(invitationToken);

		// If this token is either missing or already consumed, throw an exception indicating that it doesn't exist.
		if (token == null || token.getDateUsed() != null) {
			throw new EntityNotFoundException();
		}

		// Pull in the project, and make our link now.
		Profile profile = getCurrentUserProfile();
		Project project = token.getProject();
		addProjectProfileInternal(project, profile);

		// Mark the date and save out our changes now.
		token.setDateUsed(new Date());
		entityManager.persist(token);
	}

	@Secured(Role.User)
	@Override
	public void sendVerificationEmail() {
		Profile profile = getCurrentUserProfile();
		sendVerificationEmail(profile);
	}

	/**
	 * @param profile
	 */
	private void sendVerificationEmail(Profile profile) {
		if (profile.getEmailVerified()) {
			return;
		}

		EmailVerificationToken token = new EmailVerificationToken();
		token.setProfile(profile);
		token.setEmail(profile.getEmail());
		token.setToken(UUID.randomUUID().toString());
		token.setDateCreated(new Date());
		token.setDateUsed(null);

		entityManager.persist(token);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("token", token);
		model.put("profile", profile);
		model.put("AppName", configuration.getAppName());
		model.put("AppBaseUrl", configuration.getProfileBaseUrl());
		model.put("URL", configuration.getEmailVerificationURL(token.getToken()));
		String bodyText = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, emailVerificationTemplate, model);
		Email email = new Email(token.getEmail(), "Verify your " + configuration.getAppName() + " email", bodyText,
				"text/plain");
		emailService.schedule(email);
	}

	@Secured(Role.User)
	@Override
	public void verifyEmail(String emailToken) throws EntityNotFoundException, ValidationException {

		// Get the associated token now.
		EmailVerificationToken token = getEmailVerificationToken(emailToken);

		// If this token is either missing or already consumed, throw an exception indicating that it doesn't exist.
		if (token == null || token.getDateUsed() != null) {
			throw new EntityNotFoundException();
		}

		// Pull in the project, and make our link now.
		Profile profile = getCurrentUserProfile();

		if (!profile.equals(token.getProfile())) {
			Errors errors = createErrors(token);
			errors.reject("email.verify.wrongProfile");
			throw new ValidationException(errors);
		}

		if (!token.getProfile().getEmail().equals(token.getEmail())) {
			Errors errors = createErrors(emailToken);
			errors.reject("email.verify.oldEmail");
			throw new ValidationException(errors);
		}

		profile.setEmailVerified(true);

		if (!profile.getSentWelcomeEmail()) {
			sendWelcomEmail(profile);
			profile.setSentWelcomeEmail(true);
		}

		// Mark the date and save out our changes now.
		token.setDateUsed(new Date());
		entityManager.persist(token);
	}

	@Secured(Role.User)
	@SuppressWarnings("unchecked")
	@Override
	public QueryResult<Profile> findProfiles(String queryText, Region region, SortInfo sortInfo) {
		queryText = queryText == null ? "" : queryText.trim();
		queryText = queryText.toLowerCase();
		queryText += "%";

		String coreQuery = "from " + Profile.class.getSimpleName() + " p where " + "LOWER(p.firstName) like :q OR "
				+ "LOWER(p.lastName) like :q OR " + "LOWER(p.username) like :q";
		Query query = entityManager.createQuery("select p "
				+ coreQuery
				+ " "
				+ createSortClause("p", Profile.class, sortInfo, new SortInfo("firstName"), new SortInfo("lastName"),
						new SortInfo("username")));
		query.setParameter("q", queryText.trim());
		if (region == null) {
			region = new Region(0, 100);
		} else if (region.getSize() > MAX_SIZE) {
			region.setSize(MAX_SIZE);
		}
		query.setFirstResult(region.getOffset());
		query.setMaxResults(region.getSize());

		Long totalSize = (Long) entityManager.createQuery("select count(p) " + coreQuery).setParameter("q", queryText)
				.getSingleResult();

		List<Profile> results = query.getResultList();
		for (Profile profile : results) {
			securityPolicy.retrieve(profile);
		}
		return new QueryResult<Profile>(region, results, totalSize.intValue());
	}

	private String createSortClause(String entityAlias, Class<? extends BaseEntity> entity, SortInfo... sortInfos) {
		String sql = "order by";
		if (sortInfos != null) {
			int count = 0;
			for (SortInfo sortInfo : sortInfos) {
				if (sortInfo == null) {
					continue;
				}
				if (hasPersistentField(entity, sortInfo.getSortField())) {
					sql += (count++ > 0 ? ", " : " ") + entityAlias + "." + sortInfo.getSortField();
					if (sortInfo.getSortOrder() == SortInfo.Order.DESCENDING) {
						sql += " desc";
					}
				}
			}
		}
		return sql;
	}

	private boolean hasPersistentField(Class<? extends BaseEntity> entity, String fieldName) {
		Class<?> clazz = entity;
		while (clazz != Object.class) {
			try {
				clazz.getDeclaredField(fieldName);
				return true;
			} catch (Exception e) {
				// expected
			}
			clazz = clazz.getSuperclass();
		}
		return false;
	}

	@Secured(Role.User)
	@Override
	public void addProjectProfile(Long projectId, Long profileId) throws EntityNotFoundException {

		Project project = getProject(projectId);
		Profile profile = getProfileInternal(profileId);

		// Validate that our security constraints are satisfied
		securityPolicy.add(project, profile, "projectProfiles");

		// Do the actual add now.
		addProjectProfileInternal(project, profile);
	}

	private void addProjectProfileInternal(Project project, Profile profile) {
		List<ProjectProfile> projectProfiles = project.getProjectProfiles();
		ProjectProfile projectProfile = null;
		for (ProjectProfile curProjectProfile : projectProfiles) {
			if (curProjectProfile.getProfile().equals(profile)) {
				projectProfile = curProjectProfile;

				// If we're already marked as a user, then bail out now
				if (curProjectProfile.getUser()) {
					return;
				} else {
					break;
				}
			}
		}

		// No pre-existing project profile? Create one now.
		if (projectProfile == null) {
			projectProfile = new ProjectProfile();
			projectProfile.setProject(project);
			project.getProjectProfiles().add(projectProfile);
			projectProfile.setProfile(profile);
			profile.getProjectProfiles().add(projectProfile);

			jobService.schedule(new ReplicateProjectTeamJob(project));
		}

		// Mark this profile as a user of the project.
		projectProfile.setUser(true);

		if (projectProfile.getId() == null) {
			// If this is new, save it now.
			entityManager.persist(projectProfile);
		}

	}

	@Secured(Role.User)
	@Override
	public void watchProject(String projectIdentifier) throws EntityNotFoundException {
		Project project = getProjectByIdentifier(projectIdentifier);

		// Only allow watching of a public project - if a private project is given, then pretend it doesn't exist to
		// prevent information leakage.
		if (!project.getPublic()) {
			throw new EntityNotFoundException();
		}

		Profile currentUser = getCurrentUserProfile();

		// Check to see if we're currently in the set of users for this project.
		for (ProjectProfile projectProfile : project.getProjectProfiles()) {
			if (currentUser.equals(projectProfile.getProfile())) {
				// We have a link already, ensure it is watched.
				if (!projectProfile.getCommunity()) {
					projectProfile.setCommunity(true);
					entityManager.persist(projectProfile);
				}
				return;
			}
		}

		// No pre-existing link? Create one now.
		ProjectProfile newProjectProfile = new ProjectProfile();
		newProjectProfile.setProject(project);
		project.getProjectProfiles().add(newProjectProfile);
		newProjectProfile.setProfile(currentUser);
		currentUser.getProjectProfiles().add(newProjectProfile);

		// Mark this profile as a watcher of the project.
		newProjectProfile.setCommunity(true);
		entityManager.persist(newProjectProfile);
	}

	@Secured(Role.User)
	@Override
	public void unwatchProject(String projectIdentifier) throws EntityNotFoundException {
		Project project = getProjectByIdentifier(projectIdentifier);

		Profile currentUser = getCurrentUserProfile();

		// If there is a projectProfile with the community role set it to false.
		for (ProjectProfile projectProfile : project.getProjectProfiles()) {
			if (currentUser.equals(projectProfile.getProfile())) {
				// remove the community role
				projectProfile.setCommunity(false);

				// if no roles are left remove the projectProfile
				if (!projectProfile.hasAnyRoles()) {
					currentUser.getProjectProfiles().remove(projectProfile);
					entityManager.remove(projectProfile);
				}
			}
		}
	}

	@Secured(Role.User)
	@Override
	public void removeProjectProfile(Long projectId, Long profileId) throws EntityNotFoundException,
			ValidationException {
		Project project = getProject(projectId);
		Profile profile = getProfileInternal(profileId);

		securityPolicy.remove(project, profile, "projectProfiles");

		ProjectProfile profileToRemove = null;
		List<ProjectProfile> projectProfiles = project.getProjectProfiles();
		if (projectProfiles.size() == 1) {
			// every project must have at least one member
			Errors errors = createErrors(project);
			errors.reject("project.mustHaveMembers", null,
					"Cannot remove member: an project must have at least one member");
			throw new ValidationException(errors);
		}

		int ownerCount = 0;
		for (ProjectProfile projectProfile : projectProfiles) {
			if (projectProfile.getOwner()) {
				++ownerCount;
			}
			if (projectProfile.getProfile().equals(profile)) {
				profileToRemove = projectProfile;
			}
		}
		if (profileToRemove == null) {
			throw new EntityNotFoundException();
		}
		if (ownerCount == 1 && profileToRemove.getOwner()) {
			// every project must have at least one owner
			Errors errors = createErrors(project);
			errors.reject("project.mustHaveOwner", null, "Cannot remove owner: an project must have at least one owner");
			throw new ValidationException(errors);
		}

		project.getProjectProfiles().remove(profileToRemove);
		profile.getProjectProfiles().remove(profileToRemove);
		entityManager.remove(profileToRemove);
	}

	@Override
	public Profile getCurrentUserProfile() {
		String currentUser = Security.getCurrentUser();

		if (currentUser != null) {
			try {
				return (Profile) entityManager
						.createQuery("select e from " + Profile.class.getSimpleName() + " e where e.username = :u")
						.setParameter("u", currentUser).getSingleResult();

			} catch (NoResultException e) {
				// expected
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Secured({ Role.User, Role.UserWithPendingAgreements })
	@Override
	public List<Agreement> getPendingAgreements() throws EntityNotFoundException {

		List<Agreement> pendingAgreements = new ArrayList<Agreement>();
		Profile profile = getCurrentUserProfile();

		Query q = entityManager.createQuery(
				"select a from " + Agreement.class.getSimpleName() + " a where a.active = :active").setParameter(
				"active", true);

		List<Agreement> activeAgreements = q.getResultList();

		Query q2 = entityManager.createQuery(
				"select ap.agreement from " + AgreementProfile.class.getSimpleName()
						+ " ap where ap.profile = :profile").setParameter("profile", profile);

		List<Agreement> agreedAgreements = q2.getResultList();

		for (Agreement agreement : activeAgreements) {
			if (agreedAgreements.indexOf(agreement) == -1) {
				pendingAgreements.add(agreement);
			}
		}

		return pendingAgreements;
	}

	@Secured({ Role.User, Role.UserWithPendingAgreements })
	@Override
	public void approveAgreement(Long agreementId) throws EntityNotFoundException {

		Profile profile = getCurrentUserProfile();

		Agreement agreement = entityManager.find(Agreement.class, agreementId);

		AgreementProfile ap = new AgreementProfile();
		ap.setProfile(profile);
		ap.setAgreement(agreement);
		ap.setDateAgreed(new Date());
		// securityPolicy.create(ap);

		entityManager.persist(ap);
	}

	@Secured(Role.User)
	@Override
	public List<AgreementProfile> getApprovedAgreementProfiles() throws EntityNotFoundException {
		Profile profile = getCurrentUserProfile();

		Query q = entityManager.createQuery(
				"select ap from " + AgreementProfile.class.getSimpleName() + " ap where ap.profile = :profile")
				.setParameter("profile", profile);

		@SuppressWarnings("unchecked")
		List<AgreementProfile> approvedAgreementProfiles = q.getResultList();

		return approvedAgreementProfiles;
	}

	@SuppressWarnings("unchecked")
	@Override
	public QueryResult<Project> findProjects(String queryText, Region region, SortInfo sortInfo) {

		Profile profile;
		try {
			profile = Security.getCurrentUser() == null ? null : getProfileByUsername(Security.getCurrentUser());
		} catch (EntityNotFoundException e) {
			throw new RuntimeException(e); // Should never happen.
		}

		queryText = queryText == null ? "" : queryText.trim();
		queryText = queryText.toLowerCase();
		queryText = queryText.length() > 0 ? "%" + queryText + "%" : "%";

		String coreQuery = "FROM " + Project.class.getSimpleName() + " project ";

		if (profile != null) {
			coreQuery += ", IN(project.projectProfiles) pp ";
		}
		coreQuery += "WHERE (LOWER(project.name) LIKE :q OR LOWER(project.description) LIKE :q OR LOWER(project.identifier) LIKE :q) AND (project.public = true";

		if (profile != null) {
			coreQuery += " OR pp.profile.id = :id)";
		} else {
			coreQuery += ")";
		}

		Query query = entityManager.createQuery("SELECT distinct project " + coreQuery + " "
				+ createSortClause("project", Project.class, sortInfo, new SortInfo("name")));

		query.setParameter("q", queryText.trim());
		if (profile != null) {
			query.setParameter("id", profile.getId());
		}

		if (region == null) {
			region = new Region(0, 100);
		} else if (region.getSize() > MAX_SIZE) {
			region.setSize(MAX_SIZE);
		}
		query.setFirstResult(region.getOffset());
		query.setMaxResults(region.getSize());

		Query countQuery = entityManager.createQuery("select count(distinct project) " + coreQuery).setParameter("q",
				queryText);
		if (profile != null) {
			countQuery.setParameter("id", profile.getId());
		}
		Long totalSize = (Long) countQuery.getSingleResult();

		List<Project> results = query.getResultList();
		for (Project project : results) {
			securityPolicy.retrieve(project);
		}

		return new QueryResult<Project>(region, results, totalSize.intValue());
	}

	@Secured(Role.User)
	@Override
	public Boolean isWatchingProject(String projectIdentifier) throws EntityNotFoundException {
		Profile profile = getCurrentUserProfile();
		Project project = getProjectByIdentifier(projectIdentifier);

		Query q = entityManager.createQuery("select count(pp) from " + ProjectProfile.class.getSimpleName()
				+ " pp where pp.profile = :profile AND pp.project = :project AND pp.community = true");
		q.setParameter("profile", profile);
		q.setParameter("project", project);
		Long count = (Long) q.getSingleResult();
		if (count > 0)
			return true;
		return false;
	}

	@Override
	public SignUpToken getSignUpToken(String signUpToken) throws EntityNotFoundException {
		SignUpToken dbToken = getToken(signUpToken, SignUpToken.class);
		// We want to return true if we have a token with an empty used-date.
		if (dbToken != null && dbToken.getDateUsed() == null) {
			return dbToken;
		}
		throw new EntityNotFoundException();
	}

	@Override
	public void markSignUpTokenUsed(String token) throws EntityNotFoundException {
		SignUpToken dbToken = getToken(token, SignUpToken.class);
		if (dbToken != null && dbToken.getDateUsed() == null) {
			dbToken.setDateUsed(new Date());
			entityManager.persist(dbToken);
			return;
		}
		throw new EntityNotFoundException();
	}

	@Override
	public InvitationToken getProjectInvitationToken(String projectInvitationToken) throws EntityNotFoundException {
		InvitationToken dbToken = getToken(projectInvitationToken, InvitationToken.class);
		// We want to return true if we have a token with an empty used-date.
		if (dbToken != null && dbToken.getDateUsed() == null) {
			return dbToken;
		}
		throw new EntityNotFoundException();
	}

	@Secured(Role.Admin)
	public SignUpToken createSignUpToken(com.tasktop.c2c.server.profile.domain.project.SignUpToken token)
			throws ValidationException {
		return createSignUpToken(token.getFirstname(), token.getLastname(), token.getEmail());
	}

	@Secured(Role.Admin)
	@Override
	public SignUpToken createSignUpToken(String firstName, String lastName, String email) throws ValidationException {
		SignUpToken token = new SignUpToken();
		token.setFirstname(firstName);
		token.setLastname(lastName);
		token.setEmail(email);
		token.setToken(UUID.randomUUID().toString());
		token.setDateCreated(new Date());
		token.setDateUsed(null);

		validate(token, validator);

		entityManager.persist(token);
		return token;
	}

	@Secured(Role.Admin)
	@Override
	@SuppressWarnings("unchecked")
	public List<SignUpToken> getUnusedSignUpTokens() {
		Query query = entityManager.createQuery("select t from " + SignUpToken.class.getSimpleName()
				+ " t where t.dateUsed is null");
		return (List<SignUpToken>) query.getResultList();
	}

	@Secured(Role.Admin)
	@Override
	public void sendSignUpInvitation(String email) throws EntityNotFoundException {
		SignUpToken token = null;
		try {
			// find the first token that matches this email and is unused
			Query q = entityManager.createQuery("select t from " + SignUpToken.class.getSimpleName()
					+ " t where t.dateUsed is null and t.email = :email");
			q.setParameter("email", email);
			token = (SignUpToken) q.getSingleResult();
		} catch (NoResultException e) {
			// no token found
			throw new EntityNotFoundException();
		}

		// check for existing profiles with that email, we don't want to allow creation of another one.
		Profile profile = privateGetProfileByEmail(email);
		if (profile != null) {
			// if exists throw exception
			throw new EntityNotFoundException();
		}

		// send email
		emailSignUpInvitationMessage(token);
	}

	private void emailSignUpInvitationMessage(SignUpToken token) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("firstName", token.getFirstname());
		model.put("lastName", token.getLastname());
		model.put("URL", configuration.getSignUpInvitationURL(token.getToken()));
		model.put("AppName", configuration.getAppName());
		model.put("AppBaseUrl", configuration.getProfileBaseUrl());
		model.put("UpdateSite", configuration.getUpdateSiteUrl());
		String bodyText = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, signUpInvitationTemplate, model);
		Email email = new Email(token.getEmail(), "You have been invited to join " + configuration.getAppName(),
				bodyText, "text/plain");
		emailService.schedule(email);

		// Task 2376: send an invite to info@code.cloudfoundry.com on system invite, containing invite info
		String notificationEmailAddress = configuration.getSignupNotificationEmail();
		if (notificationEmailAddress != null && !notificationEmailAddress.isEmpty()) {
			String secondBodyText = String.format(
					"first name: %s\nlast name: %s\n email: %s\n token: %s\n\nSent by: %s", token.getFirstname(),
					token.getLastname(), token.getEmail(), token.getToken(), getCurrentUserProfile().getFullName());
			Email secondEmail = new Email(notificationEmailAddress, "System invitation to " + token.getEmail()
					+ " sent for " + configuration.getWebHost(), secondBodyText, "text/plain");
			emailService.schedule(secondEmail);
		}
	}

	@Secured(Role.System)
	@Override
	public void replicateTeam(Long projectId) throws EntityNotFoundException {
		Project project = projectId == null ? null : entityManager.find(Project.class, projectId);
		if (project == null) {
			throw new EntityNotFoundException();
		}
		if (project.getProjectServiceProfile() == null) {
			return;
		}
		for (ProjectService projectService : project.getProjectServiceProfile().getProjectServices()) {
			if (projectService.getServiceHost() != null && projectService.getServiceHost().isAvailable()
					&& projectService.getType() == ServiceType.TASKS) {
				Team team = new Team();

				for (ProjectProfile teamMember : project.getProjectProfiles()) {
					if ((teamMember.getUser() != null && teamMember.getUser())
							|| (teamMember.getOwner() != null && teamMember.getOwner())) {
						TaskUserProfile taskUserProfile = new TaskUserProfile();
						Profile profile = teamMember.getProfile();
						taskUserProfile.setLoginName(profile.getUsername());
						taskUserProfile.setRealname(profile.getFullName());
						taskUserProfile.setGravatarHash(profile.getGravatarHash());
						team.add(taskUserProfile);
					}
				}

				taskServiceProvider.getTaskService(project.getIdentifier()).replicateTeam(team);
			}
		}
	}

	@Override
	@Transactional(noRollbackFor = { EntityNotFoundException.class })
	public RandomToken getSignUpOrProjectInvitationToken(String token) throws EntityNotFoundException {

		RandomToken dbToken = null;
		try {
			// will not rollback transaction!!
			dbToken = getSignUpToken(token);
		} catch (EntityNotFoundException e) {
			// will throw EntityNotFoundException if not found.
			dbToken = getProjectInvitationToken(token);
		}
		return dbToken;
	}

	@Secured(Role.Admin)
	@Override
	public SignUpTokens createInvitations(SignUpTokens invitationTokens, boolean sendEmail) throws ValidationException {
		SignUpTokens result = new SignUpTokens();

		List<SignUpToken> newTokens = new ArrayList<SignUpToken>(invitationTokens.getTokens().size());

		for (com.tasktop.c2c.server.profile.domain.project.SignUpToken token : invitationTokens.getTokens()) {
			SignUpToken signUpToken = createSignUpToken(token);
			newTokens.add(signUpToken);
			result.add(webServiceDomain.copy(signUpToken, configuration));
		}
		if (sendEmail) {
			for (SignUpToken token : newTokens) {
				emailSignUpInvitationMessage(token);
			}
		}

		return result;
	}

	@Secured(Role.Admin)
	@Override
	@SuppressWarnings("unchecked")
	public List<Profile> listAllProfiles() {
		return entityManager.createQuery("select p from " + Profile.class.getSimpleName() + " p").getResultList();
	}

	@Secured(Role.User)
	@Override
	public SshPublicKey createSshPublicKey(SshPublicKeySpec keySpec) throws ValidationException {
		validate(keySpec);

		SshPublicKey sshPublicKey = publicKeyReader.readPublicKey(keySpec.getKeyData());
		if (sshPublicKey == null) {
			Errors errors = createErrors(keySpec);
			errors.reject("invalidKeyFormat");
			throw new ValidationException(errors);
		}
		sshPublicKey.setName(keySpec.getName());
		return createSshPublicKey(sshPublicKey);
	}

	@Secured(Role.User)
	@Override
	public SshPublicKey createSshPublicKey(SshPublicKey publicKey) throws ValidationException {
		SshPublicKey managedKey = new SshPublicKey();
		managedKey.setName(publicKey.getName());
		managedKey.setAlgorithm(publicKey.getAlgorithm());
		managedKey.setKeyData(publicKey.getKeyData());

		validate(managedKey);

		managedKey.computeFingerprint();

		Profile profile = getCurrentUserProfile();

		profile.getSshPublicKeys().add(managedKey);
		managedKey.setProfile(profile);

		entityManager.persist(managedKey);
		entityManager.flush();

		return managedKey;
	}

	@Secured(Role.User)
	@Override
	public SshPublicKey updateSshPublicKey(SshPublicKey publicKey) throws ValidationException {
		SshPublicKey managedKey = entityManager.find(SshPublicKey.class, publicKey.getId());
		managedKey.setName(publicKey.getName());
		validate(managedKey);

		managedKey.computeFingerprint();

		return managedKey;
	}

	@Secured(Role.User)
	@Override
	public List<SshPublicKey> listSshPublicKeys() {
		Profile profile = getCurrentUserProfile();
		return new ArrayList<SshPublicKey>(profile.getSshPublicKeys());
	}

	@Secured(Role.User)
	@Override
	public void removeSshPublicKey(Long publicKeyId) throws EntityNotFoundException {
		Profile profile = getCurrentUserProfile();

		SshPublicKey sshPublicKey = entityManager.find(SshPublicKey.class, publicKeyId);
		if (sshPublicKey == null || !sshPublicKey.getProfile().equals(profile)) {
			throw new EntityNotFoundException();
		}
		profile.getSshPublicKeys().remove(sshPublicKey);
		entityManager.remove(sshPublicKey);
		entityManager.flush();
	}

	@Override
	public QueryResult<Project> findProjects(ProjectRelationship projectRelationship, QueryRequest queryRequest) {

		Profile profile;
		try {
			profile = Security.getCurrentUser() == null ? null : getProfileByUsername(Security.getCurrentUser());
		} catch (EntityNotFoundException e) {
			throw new RuntimeException(e); // Should never happen.
		}

		if (profile == null && ProjectRelationship.ALL.equals(projectRelationship)) {
			projectRelationship = ProjectRelationship.PUBLIC; // All mean public for anon-user
		}

		if (profile == null && !ProjectRelationship.PUBLIC.equals(projectRelationship)) {
			throw new InsufficientPermissionsException("Need to be logged in for this query");
		}

		String fromString = "FROM " + Project.class.getSimpleName() + " project, IN(project.projectProfiles) pp ";
		String whereString;
		boolean needIdParam = true;
		switch (projectRelationship) {
		case ALL:
			whereString = "WHERE project.public = true OR pp.profile.id = :id ";
			break;
		case MEMBER:
			whereString = "WHERE pp.profile.id = :id AND pp.user = true ";
			break;
		case OWNER:
			whereString = "WHERE pp.profile.id = :id AND pp.owner = true ";
			break;
		case WATCHER:
			whereString = "WHERE pp.profile.id = :id AND pp.community = true ";
			break;
		case PUBLIC:
			needIdParam = false;
			whereString = "WHERE project.public = true ";
			break;
		default:
			throw new IllegalStateException();
		}

		Query totalResultQuery = entityManager
				.createQuery("SELECT count(DISTINCT project) " + fromString + whereString);
		if (needIdParam) {
			totalResultQuery.setParameter("id", profile.getId());
		}
		int totalResultSize = ((Long) totalResultQuery.getSingleResult()).intValue();

		Query q = entityManager.createQuery("SELECT DISTINCT project " + fromString + whereString
				+ createSortClause("project", Project.class, new SortInfo("name")));
		if (needIdParam) {
			q.setParameter("id", profile.getId());
		}

		if (queryRequest != null && queryRequest.getPageInfo() != null) {
			q.setFirstResult(queryRequest.getPageInfo().getOffset());
			q.setMaxResults(queryRequest.getPageInfo().getSize());
		}

		List<Project> projects = q.getResultList();

		for (Project project : projects) {
			securityPolicy.retrieve(project);
		}

		Region region;
		if (queryRequest == null || queryRequest.getPageInfo() == null) {
			region = new Region(0, Integer.MAX_VALUE);
		} else {
			region = queryRequest.getPageInfo();
		}
		return new QueryResult<Project>(region, projects, totalResultSize);

	}
}
