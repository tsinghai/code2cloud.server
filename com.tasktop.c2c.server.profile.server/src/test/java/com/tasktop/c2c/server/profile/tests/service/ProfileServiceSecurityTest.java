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

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.profile.service.ProfileService;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml", "/applicationContext-testSecurity.xml" })
@Transactional
public class ProfileServiceSecurityTest {

	@Autowired
	private ProfileService profileService;

	@After
	public void after() {
		clearCredentials();
	}

	private void clearCredentials() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	@Test
	public void testCreateApplicationRequiresAuthentication() throws Exception {
		try {
			profileService.createProject(1L, null);
			fail("Expected authentication failure");
		} catch (AuthenticationCredentialsNotFoundException e) {
			// expected
		}
	}

	@Test
	public void testGetApplicationRequiresAuthentication() throws Exception {
		try {
			profileService.getProject(null);
			fail("Expected authentication failure");
		} catch (AuthenticationCredentialsNotFoundException e) {
			// expected
		}
	}

	@Test
	public void testGetProfileRequiresAuthentication() throws Exception {
		try {
			profileService.getProfile(null);
			fail("Expected authentication failure");
		} catch (AuthenticationCredentialsNotFoundException e) {
			// expected
		}
	}

	@Test
	public void testGetProfileApplicationsRequiresAuthentication() throws Exception {
		try {
			profileService.getProfileProjects(null);
			fail("Expected authentication failure");
		} catch (AuthenticationCredentialsNotFoundException e) {
			// expected
		}
	}

	@Test
	public void testGetProfileByEmailApplicationsRequiresAuthentication() throws Exception {
		try {
			profileService.getProfileByEmail(null);
			fail("Expected authentication failure");
		} catch (AuthenticationCredentialsNotFoundException e) {
			// expected
		}
	}

	@Test
	public void testUpdateProfileByEmailApplicationsRequiresAuthentication() throws Exception {
		try {
			profileService.updateProfile(null);
			fail("Expected authentication failure");
		} catch (AuthenticationCredentialsNotFoundException e) {
			// expected
		}
	}
}
