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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.github.api.GitHub;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.internal.profile.service.ProfileWebServiceInternal;
import com.tasktop.c2c.server.profile.domain.internal.InvitationToken;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.domain.internal.RandomToken;
import com.tasktop.c2c.server.profile.domain.internal.SignUpToken;
import com.tasktop.c2c.server.profile.domain.project.Agreement;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKey;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKeySpec;
import com.tasktop.c2c.server.profile.service.ProfileService;
import com.tasktop.c2c.server.profile.service.ProfileServiceConfiguration;
import com.tasktop.c2c.server.profile.service.ProfileWebService;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockAgreementFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProfileFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectInvitationTokenFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockSignUpTokenFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockSshPublicKeyFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-testDisableSecurity.xml" })
@Transactional
public class ProfileWebServiceTest implements ApplicationContextAware {

	private static final String PASSWORD = "password1234@#%F";

	@Autowired
	protected ProfileService profileService;

	@PersistenceContext
	private EntityManager entityManager;

	@Qualifier("main")
	@Autowired
	protected ProfileWebService profileWebService;

	private Mockery context;

	private ApplicationContext applicationContext;

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

		applicationContext.getBean(ProfileWebServiceInternal.class).setUsersConnectionRepository(userConnRepo);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Test
	public void testGetProfile() throws ValidationException, EntityNotFoundException, AuthenticationException {
		Profile internalProfile = setupProfile();

		com.tasktop.c2c.server.profile.domain.project.Profile webProfile = profileWebService.getCurrentProfile();

		assertEquals(internalProfile.getEmail(), webProfile.getEmail());
		assertEquals(internalProfile.getFirstName(), webProfile.getFirstName());
		assertEquals(internalProfile.getLastName(), webProfile.getLastName());
		assertEquals(internalProfile.getUsername(), webProfile.getUsername());
		assertEquals(internalProfile.getId(), webProfile.getId());
		assertNull(webProfile.getPassword());
	}

	@Test
	public void testGetProjects() throws ValidationException, EntityNotFoundException, AuthenticationException {
		Long profileId = setupProfile().getId();

		com.tasktop.c2c.server.profile.domain.internal.Project internalProject = new com.tasktop.c2c.server.profile.domain.internal.Project();
		internalProject.setDescription("My app");
		internalProject.setIdentifier("myApp");
		internalProject.setName("Appracadapra");
		internalProject.setPublic(Boolean.FALSE);

		profileService.createProject(profileId, internalProject);

		List<Project> projects = profileWebService.getProjects(profileId);
		assertEquals(1, projects.size());
	}

	@Test
	public void testCreateProject() throws ValidationException, EntityNotFoundException, AuthenticationException {
		Long profileId = setupProfile().getId();

		Project project = new Project();
		project.setName("New Application");
		project.setIdentifier("abccccapplication");
		project.setDescription("Application description; blah blah blah.");
		project.setPublic(Boolean.FALSE);

		Project created = profileWebService.createProject(profileId, project);
		assertNotNull(created);

		assertEquals(project.getName(), created.getName());
		// The identifier is created by the service:
		// assertEquals(application.getIdentifier(), internalApp.getIdentifier());
		assertEquals(project.getDescription(), created.getDescription());
	}

	@Test
	public void testGetPendingAgreements() throws EntityNotFoundException, ValidationException {
		setupProfile();
		MockAgreementFactory.create(entityManager);
		List<Agreement> agreements = profileWebService.getPendingAgreements();
		assertEquals(1, agreements.size());
	}

	@Test
	public void testApproveAgreement() throws EntityNotFoundException, ValidationException {
		setupProfile();

		com.tasktop.c2c.server.profile.domain.internal.Agreement a = MockAgreementFactory.create(entityManager);
		List<Agreement> agreements = profileWebService.getPendingAgreements();
		assertEquals(1, agreements.size());

		profileWebService.approveAgreement(a.getId());

		List<Agreement> agreementsAfter = profileWebService.getPendingAgreements();
		assertEquals(0, agreementsAfter.size());
	}

	@Test
	public void testGetApprovedAgreements() throws EntityNotFoundException, ValidationException {
		setupProfile();
		com.tasktop.c2c.server.profile.domain.internal.Agreement a = MockAgreementFactory.create(entityManager);
		List<Agreement> agreements = profileWebService.getPendingAgreements();
		assertEquals(1, agreements.size());

		profileWebService.approveAgreement(a.getId());

		List<Agreement> agreementsAfter = profileWebService.getPendingAgreements();
		assertEquals(0, agreementsAfter.size());
	}

	@Test
	public void watchProject() throws Exception {
		Long profileId = setupProfile().getId();

		com.tasktop.c2c.server.profile.domain.internal.Project project = MockProjectFactory.create(entityManager);
		project.setPublic(true);
		entityManager.persist(project);

		assertEquals(0, profileService.getProfileProjects(profileId).size());
		profileWebService.watchProject(project.getIdentifier());
		assertEquals(1, profileService.getProfileProjects(profileId).size());
	}

	@Test
	public void unwatchProject() throws Exception {
		Long profileId = setupProfile().getId();

		com.tasktop.c2c.server.profile.domain.internal.Project project = MockProjectFactory.create(entityManager);
		project.setPublic(true);
		entityManager.persist(project);

		assertEquals(0, profileService.getProfileProjects(profileId).size());
		profileWebService.watchProject(project.getIdentifier());
		assertEquals(1, profileService.getProfileProjects(profileId).size());
		profileWebService.unwatchProject(project.getIdentifier());
		assertEquals(0, profileService.getProfileProjects(profileId).size());
	}

	@Test
	public void isWatchingProject() throws Exception {
		Long profileId = setupProfile().getId();

		com.tasktop.c2c.server.profile.domain.internal.Project project = MockProjectFactory.create(entityManager);
		project.setPublic(true);
		entityManager.persist(project);

		assertEquals(0, profileService.getProfileProjects(profileId).size());
		assertEquals(false, profileWebService.isWatchingProject(project.getIdentifier()));

		profileWebService.watchProject(project.getIdentifier());
		entityManager.flush();

		assertEquals(1, profileService.getProfileProjects(profileId).size());
		assertEquals(true, profileWebService.isWatchingProject(project.getIdentifier()));
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
		String pwd = PASSWORD;
		internalProfile.setPassword(pwd);
		internalProfile.setAdmin(isAdmin);
		profileService.createProfile(internalProfile);

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(internalProfile.getUsername(), pwd));

		return internalProfile;
	}

	@Test
	public void createSignUpToken() throws ValidationException, EntityNotFoundException {
		setupProfile(true); // Admin user
		String fname = "first";
		String lname = "last";
		String email = "email@exmaple.com";
		com.tasktop.c2c.server.profile.domain.project.SignUpToken token = new com.tasktop.c2c.server.profile.domain.project.SignUpToken();
		token.setFirstname(fname);
		token.setLastname(lname);
		token.setEmail(email);
		com.tasktop.c2c.server.profile.domain.project.SignUpToken createdToken = profileWebService
				.createSignUpToken(token);
		com.tasktop.c2c.server.profile.domain.project.SignUpToken retrievedToken = profileWebService
				.getSignUpToken(createdToken.getToken());
		assertEquals(retrievedToken.getToken(), createdToken.getToken());
		assertEquals(retrievedToken.getFirstname(), fname);
		assertEquals(retrievedToken.getLastname(), lname);
		assertEquals(retrievedToken.getEmail(), email);
	}

	@Test
	@Ignore
	public void sendSignUpInvitation() {
		// TODO
	}

	@Test
	public void getSignUpToken() throws EntityNotFoundException, ValidationException {
		setupProfile(true); // Admin user
		String fname = "first";
		String lname = "last";
		String email = "email@exmaple.com";
		SignUpToken internalToken = profileService.createSignUpToken(fname, lname, email);
		com.tasktop.c2c.server.profile.domain.project.SignUpToken signUpToken = profileWebService
				.getSignUpToken(internalToken.getToken());
		assertEquals(signUpToken.getFirstname(), fname);
		assertEquals(signUpToken.getLastname(), lname);
		assertEquals(signUpToken.getEmail(), email);
	}

	@Test
	public void createProfileWithoutToken() throws ValidationException, EntityNotFoundException,
			AuthenticationException {

		// explicitly go to token-optional mode
		applicationContext.getBean(ProfileServiceConfiguration.class).setInvitationOnlySignUp(false);

		com.tasktop.c2c.server.profile.domain.project.Profile profile = new com.tasktop.c2c.server.profile.domain.project.Profile();
		profile.setEmail("email@profile.clm");
		profile.setFirstName("First");
		profile.setLastName("Last");
		profile.setUsername(profile.getEmail());
		String pwd = PASSWORD;
		profile.setPassword(pwd);

		Long profileId = profileWebService.createProfileWithSignUpToken(profile, null);

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(profile.getUsername(), pwd));

		com.tasktop.c2c.server.profile.domain.project.Profile webProfile = profileWebService.getCurrentProfile();
		assertEquals(profile.getEmail(), webProfile.getEmail());
		assertEquals(profile.getFirstName(), webProfile.getFirstName());
		assertEquals(profile.getLastName(), webProfile.getLastName());
		assertEquals(profile.getUsername(), webProfile.getUsername());
		assertEquals(profileId, webProfile.getId());
		assertNull(webProfile.getPassword());
	}

	@Test
	public void createProfileWithSignUpToken() throws EntityNotFoundException, ValidationException {

		// explicitly go to token-required mode
		applicationContext.getBean(ProfileServiceConfiguration.class).setInvitationOnlySignUp(true);

		setupProfile(true); // Admin user
		String fname = "first";
		String lname = "last";
		String email = "email@exmaple.com";
		SignUpToken internalToken = profileService.createSignUpToken(fname, lname, email);

		// Create a profile with a different email address... Since they don't need to match the token.
		com.tasktop.c2c.server.profile.domain.project.Profile profile = new com.tasktop.c2c.server.profile.domain.project.Profile();
		profile.setFirstName("joe");
		profile.setLastName("Schmoe");
		profile.setPassword("password(*@&L1");
		profile.setUsername("user");
		profile.setEmail("joe.schmoe@example.com");
		Long profileId = profileWebService.createProfileWithSignUpToken(profile, internalToken.getToken());

		assertNotNull(profileId);
	}

	@Test
	public void getUnusedTokens() throws ValidationException, EntityNotFoundException {
		setupProfile(true); // Admin user

		List<com.tasktop.c2c.server.profile.domain.project.SignUpToken> unusedSignUpTokens = profileWebService
				.getUnusedSignUpTokens();
		assertEquals(unusedSignUpTokens.size(), 0);

		String fname = "first";
		String lname = "last";
		String email = "email@exmaple.com";
		SignUpToken internalToken = profileService.createSignUpToken(fname, lname, email);
		// entityManager.flush();

		List<com.tasktop.c2c.server.profile.domain.project.SignUpToken> unusedSignUpTokensAfterCreate = profileWebService
				.getUnusedSignUpTokens();
		assertEquals(unusedSignUpTokensAfterCreate.size(), 1);

		// Create a profile with a different email address... Since they don't need to match the token.
		com.tasktop.c2c.server.profile.domain.project.Profile profile = new com.tasktop.c2c.server.profile.domain.project.Profile();
		profile.setFirstName("joe");
		profile.setLastName("Schmoe");
		profile.setPassword("password@#*&L:2A");
		profile.setUsername("user");
		profile.setEmail("joe.schmoe@example.com");
		Long profileId = profileWebService.createProfileWithSignUpToken(profile, internalToken.getToken());

		assertNotNull(profileId);

		List<com.tasktop.c2c.server.profile.domain.project.SignUpToken> unusedSignUpTokensAfterUsed = profileWebService
				.getUnusedSignUpTokens();
		assertEquals(unusedSignUpTokensAfterUsed.size(), 0);

	}

	@Test
	public void testInviteUserToProject() throws ValidationException, EntityNotFoundException {
		Profile owner = setupProfile();
		Project project = new Project();
		project.setName("new-app");
		project.setIdentifier("new-app");
		project.setDescription("Application description; blah blah blah.");
		project.setPublic(Boolean.FALSE);
		profileWebService.createProject(owner.getId(), project);
		Profile newMember = setupProfile();
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(owner.getUsername(), PASSWORD));
		String token = profileWebService.inviteUserForProject(newMember.getEmail(), project.getIdentifier());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(newMember.getUsername(), PASSWORD));
		profileWebService.acceptInvitation(token);
		List<Project> projects = profileWebService.getProjects(newMember.getId());
		assertEquals(1, projects.size());
	}

	@Test
	public void findSignUpOrProjectInvitationToken() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);
		com.tasktop.c2c.server.profile.domain.internal.Project project = MockProjectFactory.create(entityManager);
		InvitationToken invitationToken = MockProjectInvitationTokenFactory.create(entityManager);
		invitationToken.setProject(project);
		invitationToken.setIssuingProfile(profile);
		SignUpToken signUpToken = MockSignUpTokenFactory.create(entityManager);
		entityManager.flush();

		RandomToken returnedToken = null;

		returnedToken = profileService.getSignUpOrProjectInvitationToken(invitationToken.getToken());
		assertNotNull(returnedToken);
		assertEquals(invitationToken.getToken(), returnedToken.getToken());

		returnedToken = profileService.getSignUpOrProjectInvitationToken(signUpToken.getToken());
		assertNotNull(returnedToken);
		assertEquals(signUpToken.getToken(), returnedToken.getToken());
	}

	@Test(expected = EntityNotFoundException.class)
	public void findSignUpOrProjectInvitationToken_NonExistentToken() throws Exception {

		profileService.getSignUpOrProjectInvitationToken("non-existent-token-foo");
	}

	@Test
	public void testGithubUsername_noGithubConnection() throws Exception {
		// our default test setup currently pretends no connection exists, so no extra config is needed.
		setupProfile();

		com.tasktop.c2c.server.profile.domain.project.Profile webProfile = profileWebService.getCurrentProfile();

		assertNull(webProfile.getGithubUsername());
	}

	@Test
	public void testGithubUsername_notSet() throws Exception {
		// First, re-config our default Github mock so that it actually returns a valid username.
		context = new JUnit4Mockery();

		final UsersConnectionRepository userConnRepo = context.mock(UsersConnectionRepository.class);
		final ConnectionRepository mockConnRepo = context.mock(ConnectionRepository.class);
		@SuppressWarnings("unchecked")
		final Connection<GitHub> mockConn = context.mock(Connection.class);
		final String githubUsername = "someTestUsername";

		context.checking(new Expectations() {
			{
				allowing(userConnRepo).createConnectionRepository(with(any(String.class)));
				will(returnValue(mockConnRepo));
				allowing(mockConnRepo).findPrimaryConnection(with(GitHub.class));
				will(returnValue(mockConn));
				allowing(mockConn).test();
				will(returnValue(true));
				allowing(mockConn).getDisplayName();
				will(returnValue(githubUsername));
			}
		});

		applicationContext.getBean(ProfileWebServiceInternal.class).setUsersConnectionRepository(userConnRepo);

		// Now that setup is complete, get to the actual testing part.
		setupProfile();

		com.tasktop.c2c.server.profile.domain.project.Profile webProfile = profileWebService.getCurrentProfile();

		assertEquals(githubUsername, webProfile.getGithubUsername());
	}

	@Test
	public void testGithubConnectionDead_connRemoved() throws Exception {
		// First, re-config our default Github mock to return the correct values and expect the correct calls
		context = new JUnit4Mockery();

		final UsersConnectionRepository userConnRepo = context.mock(UsersConnectionRepository.class);
		final ConnectionRepository mockConnRepo = context.mock(ConnectionRepository.class);
		@SuppressWarnings("unchecked")
		final Connection<GitHub> mockConn = context.mock(Connection.class);

		context.checking(new Expectations() {
			{
				allowing(userConnRepo).createConnectionRepository(with(any(String.class)));
				will(returnValue(mockConnRepo));
				allowing(mockConnRepo).findPrimaryConnection(with(GitHub.class));
				will(returnValue(mockConn));

				oneOf(mockConn).test();
				will(returnValue(false));
				oneOf(mockConn).hasExpired();
				will(returnValue(false));

				ConnectionKey key = new ConnectionKey("foo", "bar");
				oneOf(mockConn).getKey();
				will(returnValue(key));

				oneOf(mockConnRepo).removeConnection(with(key));
			}
		});

		applicationContext.getBean(ProfileWebServiceInternal.class).setUsersConnectionRepository(userConnRepo);

		// Now that setup is complete, get to the actual testing part.
		setupProfile();

		// Get our profile - this will trigger our GitHub connection test.
		profileWebService.getCurrentProfile();

		// Make sure all of our expected methods (particularly the remove method) were called
		context.assertIsSatisfied();
	}

	@Test
	public void testCreateSshPublicKey() throws ValidationException {
		Profile profile = setupProfile(false);
		entityManager.flush();

		String key = "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAvcVV9k7OMpIhg3+dqv93wUNgMwdfK3FEltUXxiRo7ziGJiHwGHXeGFDaoie2Gw6vbpJy5Fo/x+Iw9oVdAFtdQDWjNaOIiBaG3OUsjzHXv0UWWb+LqmtzAmXc3zLhCjCYDB3WWle2FS2ulg7O0cl2l2uvx6b4SHOD6x05TTQnGRxS3s6Bx9uy6IkCcMW1PZ+ddTjIZ7VuXra9bkqyJynNWBWh5m8kNYtN9N58b2zJ7t81TA63vVIsZoX699TJ17ADMtPAuoodsOJEubE9YrAy07DwqvM/bK02TMz0wttkfDk82TDyS2us7EzJsIwN2UfGnGpW6lPpbyWsDtGjKcYm7Q== David Green@DAVIDGREENT410S\n";

		SshPublicKey publicKey = profileWebService.createSshPublicKey(new SshPublicKeySpec("test", key));

		entityManager.flush();
		entityManager.clear();

		profile = entityManager.find(Profile.class, profile.getId());

		Assert.assertEquals(1, profile.getSshPublicKeys().size());

		com.tasktop.c2c.server.profile.domain.internal.SshPublicKey domainPublicKey = profile.getSshPublicKeys()
				.get(0);

		Assert.assertEquals(domainPublicKey.getId(), publicKey.getId());
		Assert.assertEquals(domainPublicKey.getFingerprint(), publicKey.getFingerprint());
	}

	@Test
	public void testListSshPublicKey() throws ValidationException {
		Profile profile = setupProfile(false);
		List<com.tasktop.c2c.server.profile.domain.internal.SshPublicKey> keys = MockSshPublicKeyFactory.create(
				entityManager, profile, 2);
		entityManager.flush();

		Assert.assertTrue(keys.size() > 0);

		List<SshPublicKey> publicKeys = profileWebService.listSshPublicKeys();

		Assert.assertEquals(keys.size(), publicKeys.size());

		for (com.tasktop.c2c.server.profile.domain.internal.SshPublicKey domainKey : keys) {
			boolean found = false;
			for (SshPublicKey publicKey : publicKeys) {
				if (publicKey.getId().equals(domainKey.getId())) {
					found = true;
					Assert.assertEquals(domainKey.getFingerprint(), publicKey.getFingerprint());
				}
			}
			Assert.assertTrue(found);
		}
	}

	@Test
	public void testRemoveSshPublicKey() throws EntityNotFoundException, ValidationException {
		Profile profile = setupProfile(false);
		com.tasktop.c2c.server.profile.domain.internal.SshPublicKey key = MockSshPublicKeyFactory.create(
				entityManager, profile);
		entityManager.flush();
		entityManager.refresh(profile);

		Assert.assertEquals(1, profile.getSshPublicKeys().size());

		profileWebService.removeSshPublicKey(key.getId());

		entityManager.refresh(profile);
		Assert.assertEquals(0, profile.getSshPublicKeys().size());
	}
}
