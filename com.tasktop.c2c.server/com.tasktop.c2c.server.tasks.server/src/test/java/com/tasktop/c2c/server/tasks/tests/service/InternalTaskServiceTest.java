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
package com.tasktop.c2c.server.tasks.tests.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.internal.tasks.service.InternalTaskService;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockProfileFactory;
import com.tasktop.c2c.server.tasks.tests.util.TestSecurity;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
@Transactional
public class InternalTaskServiceTest {

	@Autowired
	protected InternalTaskService taskService;

	@PersistenceContext(unitName = "tasksDomain")
	private EntityManager entityManager;

	private Profile profile;

	private void login() {
		TestSecurity.login(profile);
	}

	@Before
	public void setup() {
		profile = MockProfileFactory.create(entityManager);
		profile.setLoginName(profile.getLoginName());
	}

	@Test
	public void testGetCurrentUserProfile_NoCredentials() {
		assertNull(taskService.getCurrentUserProfile());
	}

	@Test
	public void testGetCurrentUserProfile_WithCredentials() {
		login();
		assertNotNull(taskService.getCurrentUserProfile());
	}

	@Test
	public void testProvisionAccount_NewAccount() {
		String username = RandomStringUtils.randomAlphanumeric(16);
		String email = username + "@test.com";
		String firstName = "firstName";
		String lastName = "lastName";
		String displayName = firstName + " " + lastName;

		taskService.provisionAccount(createTaskUserProfile(username, displayName));

		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setFirstName(firstName);
		authenticationToken.setLastName(lastName);
		authenticationToken.setIssued(new Date());
		authenticationToken.setExpiry(new Date(authenticationToken.getIssued().getTime() + 100000000L));
		authenticationToken.setKey(UUID.randomUUID().toString());
		authenticationToken.setUsername(username);

		TestSecurity.login(authenticationToken);

		Profile currentUserProfile = taskService.getCurrentUserProfile();
		assertNotNull(currentUserProfile);
		assertEquals(displayName, currentUserProfile.getRealname());

		// Change our last name, and make sure it's reflected in the DB
		lastName = "newLastName";
		authenticationToken.setLastName(lastName);
		displayName = firstName + " " + lastName;

		taskService.provisionAccount(createTaskUserProfile(username, displayName));

		currentUserProfile = taskService.getCurrentUserProfile();
		assertNotNull(currentUserProfile);
		assertEquals(displayName, currentUserProfile.getRealname());
	}

	/**
	 * @param username
	 * @param displayName
	 * @return
	 */
	private TaskUserProfile createTaskUserProfile(String username, String displayName) {
		TaskUserProfile profile = new TaskUserProfile();
		profile.setLoginName(username);
		profile.setRealname(displayName);
		return profile;
	}

	@Test
	public void testFindProfile() throws EntityNotFoundException {
		Profile actual = taskService.findProfile(profile.getLoginName());
		assertEquals(profile, actual);
	}

	@Test(expected = EntityNotFoundException.class)
	public void testFindProfileNotThere() throws EntityNotFoundException {
		taskService.findProfile("foo");
	}
}
