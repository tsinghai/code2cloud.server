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
package com.tasktop.c2c.server.profile.web.ui.client.navigation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;

public class PageMappingTest {

	@Test
	public void testPageMappingSimple() {
		Path mapping = new Path("one");
		assertTrue(mapping.matches("one"));
		assertFalse(mapping.matches("one/two"));
		assertFalse(mapping.matches("/one"));

		assertEquals("one", mapping.uri());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPageMappingException() {
		Path mapping = new Path("boom");

		// This should throw an IllegalArgumentException, since we're passing an argument to a path that has no
		// arguments in it.
		mapping.uri("someArg");
	}

	@Test
	public void testPageMappingOneArg() {
		Path mapping = new Path("one/{foo}");
		assertTrue(mapping.matches("one/123"));
		assertFalse(mapping.matches("one/"));
		assertFalse(mapping.matches("one"));
		assertTrue(mapping.matches("one/123/"));
		assertFalse(mapping.matches("one/123/two"));

		Args retArgs = mapping.configureArgs("one/123");
		assertNotNull(retArgs);
		assertNotNull(retArgs.getLong("foo"));
		assertNotNull(retArgs.getString("foo"));
		assertEquals(Long.valueOf(123), retArgs.getLong("foo"));

		assertEquals("one/123", mapping.uri(123L));
	}

	@Test
	public void testPageMappingOneArgTrailingSegment() {
		Path mapping = new Path("one/{foo}/bar");
		assertTrue(mapping.matches("one/123/bar"));
		assertFalse(mapping.matches("one//bar"));
		assertFalse(mapping.matches("one/bar"));
		assertTrue(mapping.matches("one/123/bar/"));
		assertFalse(mapping.matches("one/123/bar/asdf"));

		Args retArgs = mapping.configureArgs("one/123/bar");
		assertNotNull(retArgs);
		assertNotNull(retArgs.getLong("foo"));
		assertNotNull(retArgs.getString("foo"));
		assertEquals(Long.valueOf(123), retArgs.getLong("foo"));

		assertEquals("one/123/bar", mapping.uri(123L));
	}

	@Test
	public void testPageMappingTwoArgsTrailingSegment() {
		Path mapping = new Path("one/{foo}/bar/{baz}/t");
		assertTrue(mapping.matches("one/123/bar/4567/t"));
		assertFalse(mapping.matches("one//bar/456/t"));
		assertFalse(mapping.matches("one/bar/t"));

		Args retArgs = mapping.configureArgs("one/123/bar/4567/t");
		assertNotNull(retArgs);
		assertNotNull(retArgs.getLong("foo"));
		assertNotNull(retArgs.getString("foo"));
		assertEquals(Long.valueOf(123), retArgs.getLong("foo"));

		assertNotNull(retArgs.getLong("baz"));
		assertNotNull(retArgs.getString("baz"));
		assertEquals(Long.valueOf(4567), retArgs.getLong("baz"));

		assertEquals("one/123/bar/4567/t", mapping.uri(123L, 4567L));
	}

	@Test
	public void testPageMappingIntegerType() {
		Path mapping = new Path("one/{foo:Integer}");
		assertTrue(mapping.matches("one/123"));
		assertFalse(mapping.matches("one/abc"));
	}

	@Test
	public void testPageMappingLongType() {
		Path mapping = new Path("one/{foo:Long}");
		assertTrue(mapping.matches("one/123"));
		assertFalse(mapping.matches("one/abc"));
	}

}
