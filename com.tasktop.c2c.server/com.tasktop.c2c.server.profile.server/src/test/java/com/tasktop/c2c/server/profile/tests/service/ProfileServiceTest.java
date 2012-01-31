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
package com.tasktop.c2c.server.profile.tests.service;

import static com.tasktop.c2c.server.common.tests.util.ValidationAssert.assertHaveValidationError;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import junit.framework.Assert;

import org.apache.commons.lang.RandomStringUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.github.api.GitHub;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.auth.service.AuthUtils;
import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.MockJobService;
import com.tasktop.c2c.server.common.service.Security;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.job.Job;
import com.tasktop.c2c.server.internal.profile.service.AuthenticationServiceInternal;
import com.tasktop.c2c.server.internal.profile.service.EmailJob;
import com.tasktop.c2c.server.internal.profile.service.ReplicateProjectTeamJob;
import com.tasktop.c2c.server.profile.domain.Email;
import com.tasktop.c2c.server.profile.domain.internal.Agreement;
import com.tasktop.c2c.server.profile.domain.internal.ConfigurationProperty;
import com.tasktop.c2c.server.profile.domain.internal.InvitationToken;
import com.tasktop.c2c.server.profile.domain.internal.PasswordResetToken;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ProjectProfile;
import com.tasktop.c2c.server.profile.domain.internal.ProjectService;
import com.tasktop.c2c.server.profile.domain.internal.ProjectServiceProfile;
import com.tasktop.c2c.server.profile.domain.internal.ServiceHost;
import com.tasktop.c2c.server.profile.domain.internal.SignUpToken;
import com.tasktop.c2c.server.profile.domain.internal.SshPublicKey;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.SignUpTokens;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKeySpec;
import com.tasktop.c2c.server.profile.service.ProfileService;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockAgreementFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProfileFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockSshPublicKeyFactory;
import com.tasktop.c2c.server.profile.tests.mock.MockTaskServiceProvider;
import com.tasktop.c2c.server.tasks.domain.Team;
import com.tasktop.c2c.server.tasks.service.TaskService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-testDisableSecurity.xml" })
@Transactional
public class ProfileServiceTest implements ApplicationContextAware {

	@Autowired
	protected ProfileService profileService;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private MockJobService jobService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Mockery context = new JUnit4Mockery();

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Before
	public void clearJobService() {
		jobService.getScheduledJobs().clear();
	}

	@Before
	public void swapInMockGithubConnector() {

		context = new JUnit4Mockery();

		final UsersConnectionRepository userConnRepo = context.mock(UsersConnectionRepository.class);
		final ConnectionRepository mockConnRepo = context.mock(ConnectionRepository.class);

		context.checking(new Expectations() {
			{
				allowing(userConnRepo).createConnectionRepository(with(any(String.class)));
				will(returnValue(mockConnRepo));
				allowing(mockConnRepo).findPrimaryConnection(with(GitHub.class));
				will(returnValue(null));
			}
		});

		applicationContext.getBean(AuthenticationServiceInternal.class).setUsersConnectionRepository(userConnRepo);
	}

	@After
	public void after() {
		jobService.getScheduledJobs().clear();
		clearCredentials();
	}

	private void clearCredentials() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	private void logon(Profile profile) {
		// NOTE: the token type and password are propagated so that they can be reused
		// elsewhere (see rest client)
		// add the default role
		Authentication authentication = new UsernamePasswordAuthenticationToken(profile.getUsername(),
				profile.getPassword(), AuthUtils.toGrantedAuthorities(Collections.singletonList(Role.User)));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private boolean hasRole(Authentication auth, String role) {
		return new SecurityExpressionRoot(auth) {
		}.hasRole(role);
	}

	@Test(expected = AuthenticationException.class)
	public void testAuthenticate() throws AuthenticationException {
		Profile profile = MockProfileFactory.create(entityManager);

		String originalPassword = profile.getPassword();
		profile.setPassword(passwordEncoder.encodePassword(originalPassword, null));

		Project project = MockProjectFactory.create(entityManager);
		ProjectProfile projectProfile = new ProjectProfile();
		projectProfile.setProject(project);
		project.getProjectProfiles().add(projectProfile);
		projectProfile.setProfile(profile);
		projectProfile.setUser(true);
		profile.getProjectProfiles().add(projectProfile);

		profileService.authenticate(profile.getUsername(), originalPassword);

		assertEquals(profile.getUsername(), Security.getCurrentUser());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		assertNotNull(authentication);

		assertTrue(hasRole(authentication, Role.User));
		assertTrue(hasRole(authentication, Role.User + "/" + project.getIdentifier()));
		assertFalse(hasRole(authentication, Role.Admin + "/" + project.getIdentifier()));

		projectProfile.setOwner(true);

		profileService.authenticate(profile.getUsername(), originalPassword);

		authentication = SecurityContextHolder.getContext().getAuthentication();

		assertNotNull(authentication);

		assertTrue(hasRole(authentication, Role.User));
		assertTrue(hasRole(authentication, Role.User + "/" + project.getIdentifier()));
		assertTrue(hasRole(authentication, Role.Admin + "/" + project.getIdentifier()));

		try {
			profileService.authenticate(profile.getUsername(), originalPassword + "BAD PASSWORD");
		} finally {
			// There should be an exception, and our existing credentials should be wiped out
			assertNull(SecurityContextHolder.getContext().getAuthentication());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateProfile() throws ValidationException {
		Profile arg = MockProfileFactory.create(null);
		Long id = profileService.createProfile(arg);

		entityManager.flush();

		assertNotNull(id);
		Profile profile = entityManager.find(Profile.class, id);
		assertEquals(id, profile.getId());
		assertNotNull(profile.getVersion());
		assertEquals(arg.getFirstName(), profile.getFirstName());
		assertEquals(arg.getLastName(), profile.getLastName());
		assertEquals(arg.getUsername(), profile.getUsername());

		profileService.createProfile(arg);
	}

	@Test(expected = ValidationException.class)
	public void testCreateProfileUsernameNonunique() throws ValidationException {
		Profile profileExists = MockProfileFactory.create(entityManager);
		Profile arg = MockProfileFactory.create(null);
		arg.setUsername(profileExists.getUsername());
		try {
			profileService.createProfile(arg);
		} catch (ValidationException e) {
			assertHaveValidationError(e, "profile.usernameUnique", profileExists.getUsername());
			throw e;
		}
	}

	@Test(expected = ValidationException.class)
	public void testCreateProfileEmailNonunique() throws ValidationException {
		Profile profileExists = MockProfileFactory.create(entityManager);
		Profile arg = MockProfileFactory.create(null);
		arg.setEmail(profileExists.getEmail());
		try {
			profileService.createProfile(arg);
		} catch (ValidationException e) {
			assertHaveValidationError(e, "profile.emailUnique", profileExists.getEmail());
			throw e;
		}
	}

	@Test(expected = ValidationException.class)
	public void testCreateProfileNonAsciiUsername() throws ValidationException {
		String[] testEscapeSequences = new String[] { "\\u2297", //
				"\u00E5", // Å
				"\u00E4", // Ä
				"\u00F6", // Ö
				"\u00FC", // Ü

		};
		for (String escapeSequence : testEscapeSequences) {
			Profile arg = MockProfileFactory.create(null);
			String username = "abc123" + escapeSequence;
			arg.setUsername(username);
			try {
				profileService.createProfile(arg);
			} catch (ValidationException e) {
				assertHaveValidationError(e, "field.validUsername.username", username);
				throw e;
			}
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testGetProfileNotFound() throws Exception {
		profileService.getProfile(0L);
	}

	@Test
	public void testGetProfile() throws ValidationException, EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);

		Long id = profile.getId();

		entityManager.flush();
		entityManager.clear();

		Profile profile2 = profileService.getProfile(id);
		assertNotNull(profile2);
		assertEquals(id, profile2.getId());
		assertEquals(profile.getUsername(), profile2.getUsername());
	}

	@Test
	public void testGetProfileByEmail() {
		Profile profile = MockProfileFactory.create(entityManager);

		entityManager.flush();
		entityManager.clear();

		Profile profile2 = profileService.getProfileByEmail(profile.getEmail());
		assertNull(profileService.getProfileByEmail(profile.getEmail() + "NOTFOUND"));
		assertEquals(profile.getId(), profile2.getId());
	}

	@Test(expected = EntityNotFoundException.class)
	public void testGetProfileByUsername() throws Exception {
		Profile profile = MockProfileFactory.create(entityManager);

		entityManager.flush();
		entityManager.clear();

		Profile profile2 = profileService.getProfileByUsername(profile.getUsername());
		assertNotNull(profile2);
		assertEquals(profile.getId(), profile2.getId());

		profileService.getProfileByUsername(profile.getUsername() + " NOTFOUND");
	}

	@Test
	public void testGetProject() throws ValidationException, EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(entityManager);
		entityManager.persist(project.addProfile(profile));

		Long id = project.getId();

		entityManager.flush();
		entityManager.clear();

		Project project2 = profileService.getProject(id);
		assertNotNull(project2);
		assertEquals(id, project2.getId());
		assertEquals(project.getName(), project2.getName());
	}

	@Test(expected = EntityNotFoundException.class)
	public void testGetProjectNotFound() throws Exception {
		profileService.getProject(0L);
	}

	@Test
	public void testGetProjectByIdentity() throws ValidationException, EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(entityManager);
		entityManager.persist(project.addProfile(profile));

		Long id = project.getId();

		entityManager.flush();
		entityManager.clear();

		Project project2 = profileService.getProjectByIdentifier(project.getIdentifier());
		assertNotNull(project2);
		assertNotSame(project, project2);
		assertEquals(id, project2.getId());
		assertEquals(project.getName(), project2.getName());
		assertEquals(project.getIdentifier(), project2.getIdentifier());
	}

	@Test(expected = EntityNotFoundException.class)
	public void testGetProjectByIdentityNotFound() throws Exception {
		profileService.getProjectByIdentifier("123");
	}

	@Test
	public void testCreateProject() throws ValidationException, EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(null);

		Long id = profileService.createProject(profile.getId(), project).getId();

		assertNotNull(id);
		Project project2 = entityManager.find(Project.class, id);
		assertNotNull(project2);
		assertEquals(id, project2.getId());
		assertEquals(project.getName(), project2.getName());

		assertEquals(1, project.getProjectProfiles().size());
		assertEquals(profile, project.getProjectProfiles().get(0).getProfile());
		assertEquals(true, project.getProjectProfiles().get(0).getOwner());
	}

	@Test(expected = ValidationException.class)
	public void testCreateProjectNameNonunique() throws ValidationException, EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project projectExists = MockProjectFactory.create(entityManager);
		Project project = MockProjectFactory.create(null);
		project.setName(projectExists.getName());

		try {
			profileService.createProject(profile.getId(), project);
		} catch (ValidationException e) {
			// expected
			assertHaveValidationError(e, "project.nameUnique", project.getName());
			throw e;
		}
	}

	@Test
	public void testCreateProjectNonAsciiCharacters() throws ValidationException, EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(null);
		project.setName("Project Name With Non-Ascii\u00E4");
		project.setDescription("a description with non-ascii\u00E4");

		Long id;
		try {
			id = profileService.createProject(profile.getId(), project).getId();
			fail("expected validation exception");
		} catch (ValidationException e) {
			// expected
		}

		project.setName("Normal Project Name");
		id = profileService.createProject(profile.getId(), project).getId();

		entityManager.flush();
		entityManager.clear();

		assertNotNull(id);
		Project project2 = entityManager.find(Project.class, id);
		assertNotNull(project2);
		assertEquals(id, project2.getId());
		assertEquals(project.getName(), project2.getName());
		assertEquals(project.getDescription(), project2.getDescription());
	}

	@Test(expected = ValidationException.class)
	public void testCreateProjectDescriptionTooLong() throws ValidationException, EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(null);

		String string256 = RandomStringUtils.randomAlphanumeric(256);
		project.setDescription(string256);
		try {
			profileService.createProject(profile.getId(), project);
		} catch (ValidationException e) {
			// expected
			assertHaveValidationError(e, "field.tooLong.project.description");
			throw e;
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testCreateProjectNotFound() throws ValidationException, EntityNotFoundException {
		Project project = MockProjectFactory.create(null);

		// should throw EntityNotFoundException
		profileService.createProject(0L, project);
	}

	private <T> int getEntityCount(Class<T> entityClass) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
		Root<T> root = query.from(entityClass);
		query.select(criteriaBuilder.count(root));
		return entityManager.createQuery(query).getSingleResult().intValue();
	}

	@Test(expected = ValidationException.class)
	public void testCreateProjectMaxProjectsReached() throws ValidationException, EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(null);

		for (ConfigurationProperty existingProperty : (List<ConfigurationProperty>) entityManager.createQuery(
				"SELECT c FROM " + ConfigurationProperty.class.getSimpleName() + " c").getResultList()) {
			entityManager.remove(existingProperty);
		}
		entityManager.flush();

		// Set our global number of projects to be the same as our current number so we trigger an exception
		int numProjects = getEntityCount(Project.class);
		ConfigurationProperty configProp = new ConfigurationProperty();
		configProp.setName(ConfigurationProperty.MAXNUM_PROJECTS_NAME);
		configProp.setValue(String.valueOf(numProjects));
		entityManager.persist(configProp);

		try {
			profileService.createProject(profile.getId(), project);
		} catch (ValidationException e) {
			// expected
			assertHaveValidationError(e, "project.maxNumReached");
			throw e;
		}
	}

	@Test
	public void testUpdateProject() throws ValidationException, EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(null);

		project = profileService.createProject(profile.getId(), project);

		entityManager.flush();
		entityManager.clear();

		String originalIdentity = project.getIdentifier();

		project.setName(project.getName() + "2");
		project.setDescription(project.getDescription() + "2");
		project.setIdentifier(project.getIdentifier() + "2");
		project.setPublic(Boolean.TRUE);
		Project updatedProject = profileService.updateProject(project);

		assertEquals(project.getName(), updatedProject.getName());
		assertEquals(project.getDescription(), updatedProject.getDescription());
		assertEquals(project.getPublic(), updatedProject.getPublic());

		// identifier should never change
		assertEquals(originalIdentity, updatedProject.getIdentifier());
	}

	// task 1824
	@Test(expected = ValidationException.class)
	public void testUpdateProject_DescriptionTooLong() throws EntityNotFoundException, ValidationException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(null);

		project = profileService.createProject(profile.getId(), project);

		entityManager.flush();
		entityManager.clear();

		project.setDescription(RandomStringUtils.random(256));
		profileService.updateProject(project);
	}

	@Test(expected = EntityNotFoundException.class)
	public void testUpdateProject_ProjectNotFound() throws ValidationException, EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(null);

		project = profileService.createProject(profile.getId(), project);

		entityManager.flush();
		entityManager.clear();

		project.setId(project.getId() + 2);

		profileService.updateProject(project);
	}

	@Test
	public void testGetProfileProjects() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		List<Project> projects = MockProjectFactory.create(entityManager, 5);
		for (Project project : projects) {
			entityManager.persist(project.addProfile(profile));
		}

		List<Project> foundProjects = profileService.getProfileProjects(profile.getId());
		assertNotNull(foundProjects);
		assertTrue(projects.containsAll(foundProjects));
		assertTrue(foundProjects.containsAll(projects));
		for (Project project : foundProjects) {
			assertNotNull(project.getId());
		}
	}

	@Test(expected = EntityNotFoundException.class)
	public void testGetProfileProjectsNotFound() throws EntityNotFoundException {
		// should throw EntityNotFoundException
		profileService.getProfileProjects(0L);
	}

	@Test(expected = EntityNotFoundException.class)
	public void testUpdateProfileNotFound() throws Exception {
		Profile profile = MockProfileFactory.create(null);
		profile.setId(123L);

		// should throw EntityNotFoundException
		profileService.updateProfile(profile);
	}

	@Test
	public void testUpdateProfile() throws ValidationException, EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		entityManager.flush();
		entityManager.clear();

		profile.setFirstName(profile.getFirstName() + "_updated");
		profile.setLastName(profile.getLastName() + "_updated");
		profile.setEmail("updated_" + profile.getEmail());

		profileService.updateProfile(profile);

		Profile profile2 = entityManager.find(Profile.class, profile.getId());
		assertNotNull(profile2);
		assertNotSame(profile, profile2);

		assertEquals(profile.getFirstName(), profile2.getFirstName());
		assertEquals(profile.getEmail(), profile2.getEmail());
	}

	@Test
	public void testCreatePasswordResetToken() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		profileService.requestPasswordReset(profile.getEmail());
		Profile profile2 = entityManager.find(Profile.class, profile.getId());
		assertEquals(1, profile2.getPasswordResetTokens().size());
	}

	@Test
	public void testPasswordReset() throws EntityNotFoundException, ValidationException {
		List<Job> scheduledJobs = jobService.getScheduledJobs();
		assertEquals(0, scheduledJobs.size());

		Profile profile = MockProfileFactory.create(entityManager);
		profileService.requestPasswordReset(profile.getEmail());
		PasswordResetToken token = profile.getPasswordResetTokens().get(0);
		profileService.resetPassword(token.getToken(), "abc123ABC)$(^");

		Profile profile2 = entityManager.find(Profile.class, profile.getId());

		assertEquals(1, profile2.getPasswordResetTokens().size());
		assertNotNull(profile2.getPasswordResetTokens().get(0).getDateUsed());

		scheduledJobs = jobService.getScheduledJobs();
		assertEquals(1, scheduledJobs.size());

		Job job = scheduledJobs.get(0);
		assertTrue(job instanceof EmailJob);
		EmailJob emailJob = (EmailJob) job;
		assertEquals(profile2.getEmail(), emailJob.getEmail().getTo());
	}

	@Test(expected = ValidationException.class)
	public void testPasswordResetWithUnsafePassword() throws EntityNotFoundException, ValidationException {
		Profile profile = MockProfileFactory.create(entityManager);
		profileService.requestPasswordReset(profile.getEmail());
		PasswordResetToken token = profile.getPasswordResetTokens().get(0);

		// username is not a safe password, this should blow up.
		profileService.resetPassword(token.getToken(), profile.getUsername());
	}

	@Test(expected = EntityNotFoundException.class)
	public void testPasswordResetTokenNotFound() throws Exception {
		// This should blow up.
		profileService.resetPassword("invalid-token", "abc123");
	}

	@Test(expected = EntityNotFoundException.class)
	public void testPasswordResetTokenAlreadyUsed() throws EntityNotFoundException, ValidationException {
		Profile profile = MockProfileFactory.create(entityManager);
		profileService.requestPasswordReset(profile.getEmail());
		PasswordResetToken token = profile.getPasswordResetTokens().get(0);
		profileService.resetPassword(token.getToken(), "abc123ABC)$(^");

		// This should blow up.
		profileService.resetPassword(token.getToken(), "def123ABC)$(^");
	}

	@Test
	public void testIsTokenAvailableTokenIsValid() throws Exception {
		Profile profile = MockProfileFactory.create(entityManager);
		profileService.requestPasswordReset(profile.getEmail());
		PasswordResetToken token = profile.getPasswordResetTokens().get(0);

		assertTrue(profileService.isPasswordResetTokenAvailable(token.getToken()));
	}

	@Test
	public void testIsTokenAvailableTokenIsUsed() throws Exception {
		Profile profile = MockProfileFactory.create(entityManager);
		profileService.requestPasswordReset(profile.getEmail());
		PasswordResetToken token = profile.getPasswordResetTokens().get(0);
		profileService.resetPassword(token.getToken(), "abc123ABC)$(^");

		assertFalse(profileService.isPasswordResetTokenAvailable(token.getToken()));
	}

	@Test
	public void testIsTokenAvailableTokenDoesNotExist() throws Exception {
		assertFalse(profileService.isPasswordResetTokenAvailable("invalid-token"));
	}

	@Test
	public void testFindProfiles() {
		int count = 20;
		int max = count * 3;
		List<Profile> profiles = MockProfileFactory.create(entityManager, max);
		int x = -1;
		for (Profile profile : profiles) {
			++x;
			if (x < count) {
				if (x % 3 == 0) {
					profile.setUsername("Test123" + x);
				} else if (x % 3 == 1) {
					profile.setFirstName("tEst123" + x + profile.getFirstName());
				} else {
					profile.setLastName("teSt123" + x + profile.getLastName());
				}
			} else {
				profile.setUsername("abc" + x);
				profile.setEmail("Test123_" + x + profile.getEmail());
				profile.setFirstName("abc" + x + profile.getFirstName());
				profile.setLastName("abc" + x + profile.getLastName());
			}
		}
		entityManager.flush();
		entityManager.clear();

		Region region = new Region(0, count / 2);
		QueryResult<Profile> result = profileService.findProfiles("Test123", region, null);
		assertNotNull(result);
		assertEquals(count, result.getTotalResultSize().intValue());
		assertEquals(region.getSize().intValue(), result.getResultPage().size());
	}

	@Test
	public void testAddProjectProfile() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(entityManager);

		profileService.addProjectProfile(project.getId(), profile.getId());

		assertEquals(1, project.getProjectProfiles().size());
		assertEquals(1, profile.getProjectProfiles().size());

		ProjectProfile projectProfile = getProjectProfile(project, profile);
		assertNotNull(projectProfile);
	}

	@Test
	public void testAddProjectProfile_existingProjectProfile() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(entityManager);

		// Before we do our add, create a projectProfile between this user and this project which doesn't have user
		// privileges to ensure that the existing object is updated rather than a new one being created
		ProjectProfile existingProjectProfile = new ProjectProfile();
		existingProjectProfile.setProject(project);
		project.getProjectProfiles().add(existingProjectProfile);
		existingProjectProfile.setProfile(profile);
		profile.getProjectProfiles().add(existingProjectProfile);
		existingProjectProfile.setCommunity(true);
		entityManager.persist(existingProjectProfile);

		// Make sure we only have one object.
		assertEquals(1, entityManager.createQuery("select a from " + ProjectProfile.class.getSimpleName() + " a")
				.getResultList().size());

		profileService.addProjectProfile(project.getId(), profile.getId());

		assertEquals(1, project.getProjectProfiles().size());
		assertEquals(1, profile.getProjectProfiles().size());

		// Make sure we still only have one object.
		assertEquals(1, entityManager.createQuery("select a from " + ProjectProfile.class.getSimpleName() + " a")
				.getResultList().size());
	}

	protected ProjectProfile getProjectProfile(Project project, Profile profile) {
		try {
			return (ProjectProfile) entityManager
					.createQuery(
							"select e from " + ProjectProfile.class.getSimpleName()
									+ " e where e.project = :a and e.profile = :p").setParameter("a", project)
					.setParameter("p", profile).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Test
	public void testRemoveProjectProfile() throws EntityNotFoundException, ValidationException {
		Profile profile = MockProfileFactory.create(entityManager);
		Profile profile2 = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(entityManager);

		ProjectProfile projectProfile = addProject(profile, project);
		projectProfile.setOwner(true);
		addProject(profile2, project);

		try {
			profileService.removeProjectProfile(project.getId(), profile.getId());
			fail("Expected exception");
		} catch (ValidationException e) {
			// expected
		}
		profileService.removeProjectProfile(project.getId(), profile2.getId());

		assertEquals(1, project.getProjectProfiles().size());
		assertEquals(1, profile.getProjectProfiles().size());

		projectProfile = getProjectProfile(project, profile2);
		assertNull(projectProfile);
	}

	protected ProjectProfile addProject(Profile profile, Project project) throws EntityNotFoundException {
		profileService.addProjectProfile(project.getId(), profile.getId());
		return profileService.getProjectProfile(project.getId(), profile.getId());
	}

	@Test
	public void testGetProjectProfile() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(entityManager);

		ProjectProfile projectProfile = addProject(profile, project);
		projectProfile.setOwner(true);

		ProjectProfile projectProfile2 = profileService.getProjectProfile(project.getId(), profile.getId());

		assertSame(projectProfile, projectProfile2);
	}

	@Test
	public void testUpdateProjectProfile() throws EntityNotFoundException, ValidationException {
		Profile profile = MockProfileFactory.create(entityManager);
		Profile profile2 = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(entityManager);

		profileService.addProjectProfile(project.getId(), profile.getId());

		ProjectProfile projectProfile = addProject(profile, project);
		projectProfile.setOwner(true);
		ProjectProfile projectProfile2 = addProject(profile2, project);
		entityManager.flush();
		entityManager.clear();

		projectProfile2.setOwner(true);
		profileService.updateProjectProfile(projectProfile2); // This blows up

		ProjectProfile projectProfile3 = entityManager.find(ProjectProfile.class, projectProfile2.getId());

		assertNotSame(projectProfile2, projectProfile3);
		assertEquals(projectProfile2, projectProfile3);
		assertEquals(projectProfile2.getOwner(), projectProfile3.getOwner());
	}

	@Test(expected = ValidationException.class)
	public void testUpdateProjectProfileOwnerCannotDisownProject() throws EntityNotFoundException, ValidationException {
		Profile profile = MockProfileFactory.create(entityManager);
		logon(profile);
		Project project = MockProjectFactory.create(entityManager);

		ProjectProfile projectProfile = addProject(profile, project);
		projectProfile.setOwner(true);
		entityManager.flush();
		entityManager.clear();

		projectProfile.setOwner(false);
		try {
			profileService.updateProjectProfile(projectProfile);
		} catch (ValidationException e) {
			assertHaveValidationError(e, "project.ownerCannotRemoveSelf");
			throw e;
		}
	}

	@Test
	public void testGetPendingAgreements() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		logon(profile);
		MockAgreementFactory.create(entityManager);

		Agreement agreement = MockAgreementFactory.create(entityManager);
		agreement.setActive(false);
		entityManager.flush();

		List<Agreement> agreements = profileService.getPendingAgreements();
		assertEquals(1, agreements.size());
	}

	@Test
	public void testApproveAgreement() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		logon(profile);
		Agreement agreement = MockAgreementFactory.create(entityManager);

		List<Agreement> agreements = null;
		agreements = profileService.getPendingAgreements();
		assertEquals(1, agreements.size());
		assertEquals(agreement, agreements.get(0));

		profileService.approveAgreement(agreement.getId());

		agreements = profileService.getPendingAgreements();
		assertEquals(0, agreements.size());
	}

	@Test
	public void testGetApprovedAgreements() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		logon(profile);
		Agreement a = MockAgreementFactory.create(entityManager);

		List<Agreement> agreements = profileService.getPendingAgreements();
		assertEquals(1, agreements.size());

		profileService.approveAgreement(a.getId());

		List<Agreement> agreementsAfter = profileService.getPendingAgreements();
		assertEquals(0, agreementsAfter.size());
	}

	@Test
	public void testProjectInvitationForEmail() throws Exception {
		List<Job> scheduledJobs = jobService.getScheduledJobs();
		assertEquals(0, scheduledJobs.size());

		// Create an project which we can invite someone to.
		Project mockProject = MockProjectFactory.create(entityManager);

		// Create a user, who will be an administrator on this project
		Profile mockProfile = MockProfileFactory.create(entityManager);
		logon(mockProfile);
		ProjectProfile adminLink = new ProjectProfile();
		adminLink.setProject(mockProject);
		adminLink.setProfile(mockProfile);
		adminLink.setOwner(true);
		entityManager.persist(adminLink);

		// Now, invite some user to our project
		String testEmail = "test@email.com";
		profileService.inviteUserForProject(testEmail, mockProject.getIdentifier());

		// Confirm that our email was sent, and that it contained the info we expected.
		scheduledJobs = jobService.getScheduledJobs();
		assertEquals(1, scheduledJobs.size());

		Job job = scheduledJobs.get(0);
		assertTrue(job instanceof EmailJob);
		EmailJob emailJob = (EmailJob) job;
		Email sentEmail = emailJob.getEmail();

		assertEquals(testEmail, sentEmail.getTo());
		assertTrue(sentEmail.getBody().contains(mockProfile.getFirstName()));
		assertTrue(sentEmail.getBody().contains(mockProfile.getLastName()));
		assertTrue(sentEmail.getBody().contains(mockProfile.getUsername()));
		assertTrue(sentEmail.getBody().contains(mockProject.getName()));
		assertTrue(sentEmail.getBody().contains(mockProject.getDescription()));
	}

	@Test
	public void testAcceptInvitation() throws Exception {
		// Create an project which we can invite someone to.
		Project mockProject = MockProjectFactory.create(entityManager);

		// Create a user, who will be an administrator on this project
		Profile mockProfile = MockProfileFactory.create(entityManager);
		logon(mockProfile);
		ProjectProfile adminLink = new ProjectProfile();
		adminLink.setProject(mockProject);
		adminLink.setProfile(mockProfile);
		adminLink.setOwner(true);
		entityManager.persist(adminLink);

		// Now, invite some user to our project
		String testEmail = "test@email.com";
		profileService.inviteUserForProject(testEmail, mockProject.getIdentifier());

		// Create a second user, who will accept this invitation
		Profile invitedProfile = MockProfileFactory.create(entityManager);
		logon(invitedProfile);

		for (ProjectProfile projectProfile : mockProject.getProjectProfiles()) {
			assertFalse(invitedProfile.equals(projectProfile.getProfile()));
		}

		// Find our one and only invitation token, and make sure it's the one we expect.
		Query query = entityManager.createQuery("select i from " + InvitationToken.class.getSimpleName() + " i");
		@SuppressWarnings("unchecked")
		List<InvitationToken> tokenList = query.getResultList();
		assertEquals(1, tokenList.size());
		InvitationToken token = tokenList.get(0);
		assertEquals(mockProject, token.getProject());
		assertEquals(mockProfile, token.getIssuingProfile());

		// Make sure that our used date is blank before we accept the invitaiton.
		assertNull(token.getDateUsed());

		// Do the actual acceptance now.
		profileService.acceptInvitation(token.getToken());

		boolean userPresent = false;
		boolean permissionsCorrect = false;

		// Confirm that our user now has the expected roles.
		for (ProjectProfile projectProfile : mockProject.getProjectProfiles()) {
			if (invitedProfile.equals(projectProfile.getProfile())) {
				userPresent = true;
				// If the owner is false, then the permissions are correct.
				permissionsCorrect = !projectProfile.getOwner();
			}
		}

		assertTrue(userPresent);
		assertTrue(permissionsCorrect);

		// Make sure that our used date was updated
		assertNotNull(token.getDateUsed());
	}

	@Test(expected = EntityNotFoundException.class)
	public void testAcceptInvitation_NotFound() throws Exception {
		// Create an project which we can invite someone to.
		Project mockProject = MockProjectFactory.create(entityManager);

		// Create a user, who will be an administrator on this project
		Profile mockProfile = MockProfileFactory.create(entityManager);
		logon(mockProfile);
		ProjectProfile adminProjectProfile = new ProjectProfile();
		adminProjectProfile.setProject(mockProject);
		adminProjectProfile.setProfile(mockProfile);
		adminProjectProfile.setOwner(true);
		entityManager.persist(adminProjectProfile);

		// Invite some user to our project
		String testEmail = "test@example.com";
		profileService.inviteUserForProject(testEmail, mockProject.getIdentifier());

		// Create a second user, who will accept this invitation
		Profile invitedProfile = MockProfileFactory.create(entityManager);
		logon(invitedProfile);

		for (ProjectProfile projectProfile : mockProject.getProjectProfiles()) {
			assertFalse(invitedProfile.equals(projectProfile.getProfile()));
		}

		// Accept the invite
		profileService.acceptInvitation(UUID.randomUUID().toString());
	}

	@Test(expected = EntityNotFoundException.class)
	public void testAcceptInvitation_AlreadyUsed() throws Exception {
		// Create an project which we can invite someone to.
		Project mockProject = MockProjectFactory.create(entityManager);

		// Create a user, who will be an administrator on this project
		Profile mockProfile = MockProfileFactory.create(entityManager);
		logon(mockProfile);
		ProjectProfile adminProjectProfile = new ProjectProfile();
		adminProjectProfile.setProject(mockProject);
		adminProjectProfile.setProfile(mockProfile);
		adminProjectProfile.setOwner(true);
		entityManager.persist(adminProjectProfile);

		// Now, invite some user to our project
		String testEmail = "test@email.com";
		profileService.inviteUserForProject(testEmail, mockProject.getIdentifier());

		// Create a second user, who will accept this invitation
		Profile invitedProfile = MockProfileFactory.create(entityManager);
		logon(invitedProfile);

		for (ProjectProfile projectProfile : mockProject.getProjectProfiles()) {
			assertFalse(invitedProfile.equals(projectProfile.getProfile()));
		}

		// Find our one and only invitation token, and make sure it's the one we expect.
		Query query = entityManager.createQuery("select i from " + InvitationToken.class.getSimpleName() + " i");
		@SuppressWarnings("unchecked")
		List<InvitationToken> tokenList = query.getResultList();
		assertEquals(1, tokenList.size());
		InvitationToken token = tokenList.get(0);
		assertEquals(mockProject, token.getProject());
		assertEquals(mockProfile, token.getIssuingProfile());

		// Do the correct acceptance now.
		profileService.acceptInvitation(token.getToken());

		// Do the acceptance a second time, will throw exception.
		profileService.acceptInvitation(token.getToken());
	}

	@Test
	public void testFindPublicProjects() {
		int count = 20;
		setupProjects(count, true, null);

		Region region = new Region(0, count / 2);
		QueryResult<Project> result = profileService.findProjects("tesT123", region, null);
		assertNotNull(result);
		assertEquals(count, result.getTotalResultSize().intValue());
		assertEquals(region.getSize().intValue(), result.getResultPage().size());
	}

	@Test
	public void testFindPrivateProjects() {
		Profile mockProfile = MockProfileFactory.create(entityManager);
		logon(mockProfile);

		int count = 20;
		setupProjects(count, false, mockProfile);

		Region region = new Region(0, count / 2);
		QueryResult<Project> result = profileService.findProjects("tesT123", region, null);
		assertNotNull(result);
		assertEquals(count, result.getTotalResultSize().intValue());
		assertEquals(region.getSize().intValue(), result.getResultPage().size());
	}

	private void setupProjects(int count, Boolean createPublic, Profile profile) {
		int max = count * 3;
		List<Project> projects = MockProjectFactory.create(entityManager, max);
		int x = -1;
		for (Project project : projects) {
			project.setPublic(createPublic);
			++x;
			if (x < count) {
				if (x % 3 == 0) {
					project.setName("Test123" + x + project.getName());
				} else if (x % 3 == 1) {
					project.setDescription("tEst123" + x + project.getDescription());
				} else {
					project.setIdentifier("teSt123" + x + project.getIdentifier());
				}
				if (profile != null) {
					ProjectProfile mock = new ProjectProfile();
					mock.setProfile(profile);
					mock.setProject(project);
					entityManager.persist(mock);
				}

			} else {
				project.setName("abc" + x + project.getName());
				project.setDescription("abc" + x + project.getDescription());
				project.setIdentifier("abc" + x + project.getIdentifier());
			}

		}
		entityManager.flush();
		entityManager.clear();
	}

	@Test
	public void testWatchProject() throws Exception {
		Profile mockProfile = MockProfileFactory.create(entityManager);
		logon(mockProfile);

		Project project = MockProjectFactory.create(entityManager);
		project.setPublic(true);
		entityManager.persist(project);

		assertEquals(0, profileService.getProfileProjects(mockProfile.getId()).size());
		profileService.watchProject(project.getIdentifier());
		assertEquals(1, profileService.getProfileProjects(mockProfile.getId()).size());
	}

	@Test
	public void testUnwatchProject() throws Exception {
		Profile mockProfile = MockProfileFactory.create(entityManager);
		logon(mockProfile);

		Project project = MockProjectFactory.create(entityManager);
		project.setPublic(true);
		entityManager.persist(project);

		assertEquals(0, profileService.getProfileProjects(mockProfile.getId()).size());
		profileService.watchProject(project.getIdentifier());
		assertEquals(1, profileService.getProfileProjects(mockProfile.getId()).size());
		profileService.unwatchProject(project.getIdentifier());
		assertEquals(0, profileService.getProfileProjects(mockProfile.getId()).size());
	}

	@Test
	public void testIsWatchingProject() throws Exception {
		Profile mockProfile = MockProfileFactory.create(entityManager);
		logon(mockProfile);

		Project project = MockProjectFactory.create(entityManager);
		project.setPublic(true);
		entityManager.persist(project);

		assertEquals(0, profileService.getProfileProjects(mockProfile.getId()).size());
		assertEquals(false, profileService.isWatchingProject(project.getIdentifier()));

		profileService.watchProject(project.getIdentifier());
		entityManager.flush();

		assertEquals(1, profileService.getProfileProjects(mockProfile.getId()).size());
		assertEquals(true, profileService.isWatchingProject(project.getIdentifier()));
	}

	@Test
	public void createSignUpToken() throws ValidationException {
		String fname = "first";
		String lname = "last";
		String email = "email@exmaple.com";
		SignUpToken signUpToken = profileService.createSignUpToken(fname, lname, email);

		assertNotNull(signUpToken.getId());
		assertEquals(signUpToken.getFirstname(), fname);
		assertEquals(signUpToken.getLastname(), lname);
		assertEquals(signUpToken.getEmail(), email);
		assertEquals(signUpToken.getDateUsed(), null);
		assertNotNull(signUpToken.getToken());
	}

	@Test
	@ExpectedException(EntityNotFoundException.class)
	public void markSignUpTokenUsed() throws EntityNotFoundException, ValidationException {
		String fname = "first";
		String lname = "last";
		String email = "email@exmaple.com";
		SignUpToken internalToken = profileService.createSignUpToken(fname, lname, email);

		SignUpToken tokenBefore = profileService.getSignUpToken(internalToken.getToken());
		assertNull(tokenBefore.getDateUsed());

		profileService.markSignUpTokenUsed(internalToken.getToken());

		// will throw EntityNotFoundException
		profileService.getSignUpToken(internalToken.getToken());
	}

	@Test
	// FIXME
	@Ignore("It seems that sending out an invitation more than once (re-sending) is actually a desired behaviour")
	public void sendSignUpInvitation() throws EntityNotFoundException, ValidationException {
		List<Job> scheduledJobs = jobService.getScheduledJobs();
		assertEquals(0, scheduledJobs.size());

		String fname = "first";
		String lname = "last";
		String email = "email@exmaple.com";
		SignUpToken internalToken = profileService.createSignUpToken(fname, lname, email);

		// try to send it once
		profileService.sendSignUpInvitation(internalToken.getEmail());

		scheduledJobs = jobService.getScheduledJobs();
		assertEquals(1, scheduledJobs.size());
		Job job = scheduledJobs.get(0);
		assertTrue(job instanceof EmailJob);
		EmailJob emailJob = (EmailJob) job;
		assertEquals(internalToken.getEmail(), emailJob.getEmail().getTo());

		// try to send it again
		try {
			profileService.sendSignUpInvitation(internalToken.getEmail());
		} catch (EntityNotFoundException e) {
			// expected
		}
		// no more jobs were added
		assertEquals(1, scheduledJobs.size());
	}

	@Test(expected = EntityNotFoundException.class)
	public void sendSignUpInvitation_ProfileAlreadyExistsWithEmail() throws EntityNotFoundException,
			ValidationException {
		List<Job> scheduledJobs = jobService.getScheduledJobs();
		assertEquals(0, scheduledJobs.size());

		Profile profile = MockProfileFactory.create(entityManager);
		SignUpToken internalToken = profileService.createSignUpToken(profile.getFirstName(), profile.getLastName(),
				profile.getEmail());

		// try to send it - should throw EntityNotFoundException
		try {
			profileService.sendSignUpInvitation(internalToken.getEmail());
		} catch (EntityNotFoundException e) {
			// expected
			assertEquals(0, scheduledJobs.size());
			throw e;
		}
	}

	@Test
	public void testCreateInvitations() throws ValidationException, MalformedURLException {
		setupProfile(true);
		jobService.getScheduledJobs().clear();
		SignUpTokens invitationTokens = new SignUpTokens();
		for (int x = 0; x < 10; ++x) {
			com.tasktop.c2c.server.profile.domain.project.SignUpToken token = new com.tasktop.c2c.server.profile.domain.project.SignUpToken();
			token.setFirstname("First" + x);
			token.setLastname("Last" + x);
			token.setEmail(token.getFirstname().toLowerCase() + x + "." + token.getLastname().toLowerCase()
					+ "@example.com");
			invitationTokens.add(token);
		}
		SignUpTokens invitations = profileService.createInvitations(invitationTokens, true);

		// Expect there to be twice as many jobs as invitations in the queue, since two emails are now sent out for
		// every system invite (one to the invitee, one to info@code.cloudfoundry.com).
		assertEquals(invitationTokens.getTokens().size() * 2, jobService.getScheduledJobs().size());

		assertEquals(invitationTokens.getTokens().size(), invitations.getTokens().size());
		for (int x = 0; x < invitationTokens.getTokens().size(); ++x) {
			com.tasktop.c2c.server.profile.domain.project.SignUpToken original = invitationTokens.getTokens()
					.get(x);
			com.tasktop.c2c.server.profile.domain.project.SignUpToken created = invitations.getTokens().get(x);
			assertEquals(original.getFirstname(), created.getFirstname());
			assertEquals(original.getLastname(), created.getLastname());
			assertEquals(original.getEmail(), created.getEmail());
			assertNotNull(created.getToken());
			assertNotNull(created.getUrl());
			assertTrue(created.getUrl().contains("/#signup"));
			assertTrue(created.getUrl().contains(created.getToken()));
			new URL(created.getUrl());
		}
	}

	@Test
	public void testReplicateUserTeam() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(entityManager);
		project.addProfile(profile);

		ServiceHost serviceHost = new ServiceHost();
		serviceHost.setAvailable(true);
		entityManager.persist(serviceHost);

		ProjectServiceProfile psp = new ProjectServiceProfile();
		psp.setProject(project);
		project.setProjectServiceProfile(psp);

		entityManager.persist(psp);

		ProjectService projectService = new ProjectService();
		projectService.setType(ServiceType.TASKS);
		projectService.setServiceHost(serviceHost);
		projectService.setUriPattern("/tasks/(.*)");
		serviceHost.add(projectService);

		psp.add(projectService);

		entityManager.persist(projectService);

		entityManager.flush();

		final TaskService taskService = context.mock(TaskService.class);

		MockTaskServiceProvider.setTaskService(taskService);

		context.checking(new Expectations() {
			{
				oneOf(taskService).replicateTeam(with(any(Team.class)));
			}
		});

		profileService.replicateTeam(project.getId());

		context.assertIsSatisfied();
	}

	@Test
	public void testReplicateTeamGetsScheduledOnTeamModification() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(entityManager);

		entityManager.flush();

		profileService.addProjectProfile(project.getId(), profile.getId());

		assertReplicateJobScheduled(project);
	}

	protected void assertReplicateJobScheduled(Project project) {
		boolean found = false;
		for (Job job : jobService.getScheduledJobs()) {
			if (job instanceof ReplicateProjectTeamJob) {
				ReplicateProjectTeamJob replicateProjectTeamJob = (ReplicateProjectTeamJob) job;
				if (replicateProjectTeamJob.getProjectId().equals(project.getId())) {
					found = true;
				}
			}
		}
		assertTrue(found);
	}

	@Test
	public void testReplicateTeamGetsScheduledOnNameChange() throws EntityNotFoundException, ValidationException {
		Profile profile = MockProfileFactory.create(entityManager);
		Project project = MockProjectFactory.create(entityManager);
		project.addProfile(profile);

		entityManager.flush();

		profile.setFirstName(profile.getFirstName() + "changed");
		profileService.updateProfile(profile);

		assertReplicateJobScheduled(project);
	}

	@Test
	public void testCreateSshPublicKey() throws ValidationException {
		Profile profile = MockProfileFactory.create(entityManager);
		entityManager.flush();
		logon(profile);

		SshPublicKey publicKey = MockSshPublicKeyFactory.create(null, null);

		SshPublicKey sshPublicKey = profileService.createSshPublicKey(publicKey);

		assertNotNull(sshPublicKey);
		assertEquals(profile, sshPublicKey.getProfile());
		assertTrue(profile.getSshPublicKeys().contains(sshPublicKey));
		assertNotNull(sshPublicKey.getId());
		assertEquals(publicKey.getAlgorithm(), sshPublicKey.getAlgorithm());
		assertArrayEquals(publicKey.getKeyData(), sshPublicKey.getKeyData());
	}

	@Test
	public void createSshPublicKeySpec_BlankKeyText() { // task 2359
		Profile profile = MockProfileFactory.create(entityManager);
		entityManager.flush();
		logon(profile);

		SshPublicKeySpec publicKeySpec = new SshPublicKeySpec("abc", null);
		try {
			profileService.createSshPublicKey(publicKeySpec);
		} catch (ValidationException e) {
			assertHaveValidationError(e, "field.required.keyData");
		}
		publicKeySpec.setKeyData("XXXX");
		try {
			profileService.createSshPublicKey(publicKeySpec);
		} catch (ValidationException e) {
			assertHaveValidationError(e, "invalidKeyFormat");
		}
	}

	@Test
	public void testListSshPublicKeys() throws ValidationException {
		Profile profile = MockProfileFactory.create(entityManager);
		SshPublicKey publicKey = MockSshPublicKeyFactory.create(entityManager, profile);
		entityManager.flush();

		logon(profile);

		List<SshPublicKey> sshPublicKeys = profileService.listSshPublicKeys();

		assertEquals(1, sshPublicKeys.size());
		assertTrue(sshPublicKeys.contains(publicKey));
		assertNotSame(profile.getSshPublicKeys(), sshPublicKeys);
	}

	private Profile setupProfile() throws ValidationException {
		return setupProfile(false);
	}

	private int nextProfileNum = 0;

	private Profile setupProfile(Boolean isAdmin) throws ValidationException {
		Profile internalProfile = new Profile();
		internalProfile.setEmail("email@profile" + nextProfileNum++ + ".clm");
		internalProfile.setFirstName("First");
		internalProfile.setLastName("Last" + nextProfileNum);
		internalProfile.setUsername(internalProfile.getEmail());
		String pwd = "abcd123$";
		internalProfile.setPassword(pwd);
		internalProfile.setAdmin(isAdmin);
		profileService.createProfile(internalProfile);

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(internalProfile.getUsername(), pwd));

		return internalProfile;
	}

	@Test
	public void testfindProjectsByRelationship() throws ValidationException, EntityNotFoundException {
		Profile profile = setupProfile(false);
		Project project = MockProjectFactory.create(null);

		Long id = profileService.createProject(profile.getId(), project).getId();

		assertNotNull(id);
		Project project2 = entityManager.find(Project.class, id);
		assertNotNull(project2);
		assertEquals(id, project2.getId());
		assertEquals(project.getName(), project2.getName());

		assertEquals(1, project.getProjectProfiles().size());
		assertEquals(profile, project.getProjectProfiles().get(0).getProfile());
		assertEquals(true, project.getProjectProfiles().get(0).getOwner());

		QueryResult<Project> projects = profileService.findProjects(ProjectRelationship.ALL, null);
		Assert.assertEquals((Integer) 1, projects.getTotalResultSize());

		// TODO more testing
	}

}
