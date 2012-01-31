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
package com.tasktop.c2c.server.profile.tests.domain;

import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProfileFactory;

/**
 * @author David Green_2 (Tasktop Technologies Inc.)
 * 
 */
public class ProfileTest {

	private Profile profile;

	@Before
	public void before() {
		profile = MockProfileFactory.create(null);
	}

	@Test
	public void testGetGravatarHashNullEmail() {
		profile.setEmail(null);
		assertNull(profile.getGravatarHash());
	}

	@Test
	public void testGetGravatar() {
		profile.setEmail("myemailaddress@example.com ");
		Assert.assertEquals("0bc83cb571cd1c50ba6f3e8a78ef1346", profile.getGravatarHash());
	}

	@Test
	public void testGetGravatarHashEmailUppercaseWithSpaces() {
		profile.setEmail("MyEmailAddress@example.com ");
		Assert.assertEquals("0bc83cb571cd1c50ba6f3e8a78ef1346", profile.getGravatarHash());
	}
}
