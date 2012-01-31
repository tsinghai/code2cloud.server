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
package com.tasktop.c2c.server.common.service.tests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import com.tasktop.c2c.server.common.service.Security;


public class SecurityTest {

	private static final String AUSER = "auser";
	private static final String ROLE1 = "role1";
	private static final String ROLE2 = "role2";
	private static final String ROLE3 = "role3";

	@Before
	public void before() {
		SecurityContextHolder.clearContext();
	}

	@After
	public void after() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testHasRole_None() {
		assertFalse(Security.hasRole(ROLE1));
		assertFalse(Security.hasRole(ROLE2));
	}

	@Test
	public void testHasRole() {
		setUpSecurityContext(AUSER, ROLE1, ROLE2);
		assertTrue(Security.hasRole(ROLE1));
		assertTrue(Security.hasRole(ROLE2));
		assertFalse(Security.hasRole(ROLE3));
	}

	@Test
	public void testHasOneOfRoles() {
		setUpSecurityContext(AUSER, ROLE1);
		assertTrue(Security.hasOneOfRoles(ROLE1, ROLE2));
		assertTrue(Security.hasOneOfRoles(ROLE2, ROLE1));
		assertTrue(Security.hasOneOfRoles(ROLE1));
		assertFalse(Security.hasOneOfRoles(ROLE3));
		assertFalse(Security.hasOneOfRoles(ROLE2));
	}

	@Test
	public void testGetCurrentUser_NoUser() {
		assertNull(Security.getCurrentUser());
	}

	@Test
	public void testGetCurrentUser() {
		setUpSecurityContext(AUSER);
		assertEquals(AUSER, Security.getCurrentUser());
	}

	private void setUpSecurityContext(String username, String... roles) {
		SecurityContextImpl context = new SecurityContextImpl();
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (String role : roles) {
			authorities.add(new SimpleGrantedAuthority(role));
		}
		Authentication authentication = new UsernamePasswordAuthenticationToken(username, UUID.randomUUID(),
				authorities);
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}
}
