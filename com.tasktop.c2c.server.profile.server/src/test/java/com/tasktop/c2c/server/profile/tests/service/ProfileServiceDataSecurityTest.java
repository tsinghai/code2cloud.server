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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.service.AuthenticationException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ProjectProfile;
import com.tasktop.c2c.server.profile.service.ProfileService;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProfileFactory;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
@Transactional
public class ProfileServiceDataSecurityTest {

	@Autowired
	private ProfileService profileService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PersistenceContext
	private EntityManager entityManager;

	private Profile profile;

	private Profile profile2;

	private String password;

	private Profile profile3;

	@Before
	public void before() {
		profile = MockProfileFactory.create(entityManager);
		profile2 = MockProfileFactory.create(entityManager);
		profile3 = MockProfileFactory.create(entityManager);

		password = profile.getPassword();

		profile.setPassword(passwordEncoder.encodePassword(password, null));
		profile2.setPassword(passwordEncoder.encodePassword(password, null));
		profile3.setPassword(passwordEncoder.encodePassword(password, null));

		entityManager.flush();
		entityManager.clear();

		profile.setPassword(null);
		profile2.setPassword(null);
	}

	@After
	public void after() {
		clearCredentials();
	}

	private void clearCredentials() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	private void logon(Profile profile) {
		try {
			profileService.authenticate(profile.getUsername(), password);
		} catch (AuthenticationException e) {
			throw new IllegalStateException(e);
		}
	}

	@Test
	public void testAnyoneCanCreateProfile() throws ValidationException {
		Profile profile = MockProfileFactory.create(null);
		profileService.createProfile(profile);
	}

	@Test
	public void testSelfCanModifyProfile() throws EntityNotFoundException, ValidationException {
		logon(profile);
		profile.setFirstName(profile.getFirstName() + "2");
		profileService.updateProfile(profile);
	}

	@Test(expected = InsufficientPermissionsException.class)
	public void testSelfCannotModifyOtherProfile() throws EntityNotFoundException, ValidationException {
		logon(profile);
		profile2.setFirstName(profile2.getFirstName() + "2");

		profileService.updateProfile(profile2);
	}

	@Test
	public void testAnyoneCanCreateApplication() throws EntityNotFoundException, ValidationException {
		logon(profile);
		profileService.createProject(profile.getId(), MockProjectFactory.create(null));
	}

	@Test(expected = InsufficientPermissionsException.class)
	public void testUnauthenticatedCannotCreateApplication() throws EntityNotFoundException, ValidationException {
		profileService.createProject(profile.getId(), MockProjectFactory.create(null));
	}

	@Test
	public void testOwnerCanModifyApplication() throws EntityNotFoundException, ValidationException {
		Project application = logonCreateAndRetrieveApplication();
		profileService.updateProject(application);
	}

	private Project logonCreateAndRetrieveApplication() throws EntityNotFoundException, ValidationException {
		logon(profile);
		Long applicationId = profileService.createProject(profile.getId(), MockProjectFactory.create(null)).getId();
		logon(profile); // Re compute roles
		Project application = profileService.getProject(applicationId);
		return application;
	}

	@Test(expected = InsufficientPermissionsException.class)
	public void testNonOwnerCannotModifyApplication() throws EntityNotFoundException, ValidationException {
		Project application = logonCreateAndRetrieveApplication();
		logon(profile2);

		profileService.updateProject(application);
	}

	@Test
	public void testMemberCanSeeApplication() throws EntityNotFoundException, ValidationException {
		Project application = logonCreateAndRetrieveApplication();
		profileService.addProjectProfile(application.getId(), profile2.getId());
		logon(profile2);
		profileService.getProject(application.getId());
		profileService.getProjectByIdentifier(application.getIdentifier());
	}

	@Test
	public void testOwnerCanSeeApplication() throws EntityNotFoundException, ValidationException {
		Project application = logonCreateAndRetrieveApplication();

		profileService.getProject(application.getId());
		profileService.getProjectByIdentifier(application.getIdentifier());
	}

	@Test(expected = InsufficientPermissionsException.class)
	public void testNonMemberCannotSeeApplicationByIdentifier() throws EntityNotFoundException, ValidationException {
		Project application = logonCreateAndRetrieveApplication();
		logon(profile2);

		profileService.getProjectByIdentifier(application.getIdentifier());
	}

	@Test(expected = InsufficientPermissionsException.class)
	public void testNonMemberCannotSeeApplicationById() throws EntityNotFoundException, ValidationException {
		Project application = logonCreateAndRetrieveApplication();
		logon(profile2);

		profileService.getProject(application.getId());
	}

	@Test
	public void testOwnerCanRemoveMember() throws EntityNotFoundException, ValidationException {
		Project application = logonCreateAndRetrieveApplication();

		profileService.addProjectProfile(application.getId(), profile2.getId());
		profileService.removeProjectProfile(application.getId(), profile2.getId());
	}

	@Test(expected = InsufficientPermissionsException.class)
	public void testMemberCannotRemoveOwner() throws EntityNotFoundException, ValidationException {
		Project application = logonCreateAndRetrieveApplication();

		profileService.addProjectProfile(application.getId(), profile2.getId());

		logon(profile2);

		profileService.removeProjectProfile(application.getId(), profile.getId());
	}

	@Test(expected = InsufficientPermissionsException.class)
	public void testMemberCannotRemoveMember() throws EntityNotFoundException, ValidationException {
		Project application = logonCreateAndRetrieveApplication();

		profileService.addProjectProfile(application.getId(), profile2.getId());
		profileService.addProjectProfile(application.getId(), profile3.getId());

		logon(profile2);

		profileService.removeProjectProfile(application.getId(), profile3.getId());
	}

	@Test
	public void testMemberCanRemoveSelf() throws EntityNotFoundException, ValidationException {
		Project application = logonCreateAndRetrieveApplication();

		profileService.addProjectProfile(application.getId(), profile2.getId());

		logon(profile2);
		profileService.removeProjectProfile(application.getId(), profile2.getId());
	}

	@Test
	public void testOwnerCanAlterRoles() throws Exception {
		Project application = logonCreateAndRetrieveApplication();

		profileService.addProjectProfile(application.getId(), profile2.getId());

		ProjectProfile applicationProfile = profileService.getProjectProfile(application.getId(), profile2.getId());
		entityManager.flush();
		entityManager.clear();

		// Switch the values of all of our flags
		applicationProfile.setOwner(!applicationProfile.getOwner());
		applicationProfile.setUser(!applicationProfile.getUser());
		applicationProfile.setCommunity(!applicationProfile.getCommunity());

		profileService.updateProjectProfile(applicationProfile);
	}

	@Test(expected = InsufficientPermissionsException.class)
	public void testNonOwnerCannotAlterRoles() throws Exception {
		Project application = logonCreateAndRetrieveApplication();

		profileService.addProjectProfile(application.getId(), profile2.getId());

		ProjectProfile applicationProfile = profileService.getProjectProfile(application.getId(), profile2.getId());
		entityManager.flush();
		entityManager.clear();

		logon(profile2);

		// Switch the values of all of our flags
		applicationProfile.setOwner(!applicationProfile.getOwner());
		applicationProfile.setUser(!applicationProfile.getUser());
		applicationProfile.setCommunity(!applicationProfile.getCommunity());

		profileService.updateProjectProfile(applicationProfile);
	}

	@Test(expected = InsufficientPermissionsException.class)
	public void testUnauthenticatedCannotRetrieveProfileByUsername() throws EntityNotFoundException {
		profileService.getProfileByUsername(profile.getUsername());
	}

	@Test(expected = InsufficientPermissionsException.class)
	public void testUnauthenticatedCannotRetrieveProfileById() throws EntityNotFoundException {
		profileService.getProfile(profile.getId());
	}

	@Test(expected = InsufficientPermissionsException.class)
	public void testUnauthenticatedCannotRetrieveProfileByEmail() throws EntityNotFoundException {
		profileService.getProfileByEmail(profile.getEmail());
	}

	@Test
	public void testOtherCanRetrieveProfile() throws EntityNotFoundException {
		logon(profile2);
		profileService.getProfile(profile.getId());
		profileService.getProfileByEmail(profile.getEmail());
		profileService.getProfileByUsername(profile.getUsername());
	}

	@Test
	public void testSelfCanRetrieveProfile() throws EntityNotFoundException {
		logon(profile);
		profileService.getProfile(profile.getId());
		profileService.getProfileByEmail(profile.getEmail());
		profileService.getProfileByUsername(profile.getUsername());
	}
}
