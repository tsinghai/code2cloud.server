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
package com.tasktop.c2c.server.common.web.tests.client.navigation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;

public class PathTest {

	@Test
	public void testPathSimple() {
		assertTrue(new Path("one").matches("one"));
	}

	@Test
	public void testPathWithArg() {
		assertMatch(new Path("one/{arg}"), "one/2", "arg", "2");
		assertMatch(new Path("one/{arg}"), "one/two", "arg", "two");
		assertFalse(new Path("/one/{arg}").matches("one/"));
	}

	@Test
	public void testPathWithWildcardArg() {
		assertMatch(new Path("one/{arg:*}"), "one/2", "arg", "2");
		assertMatch(new Path("one/{arg:*}"), "one/two/three", "arg", "two/three");
		assertFalse(new Path("one/{arg:*}").matches("one/"));
	}

	public void testPathWithHashtagButNoSubpage() {
		Path path = new Path("one/{query:*}");
		String query = "one/Path/PageName" + Path.HASHTAG_DELIMITER + "hashtag/foo";
		Args args = path.configureArgs("one/" + query);
		assertEquals(query, args.getString("query"));
		assertNull(args.getString(Path.HASHTAG));
	}

	@Test
	public void testPathWithWildcardArgAndHashtag() {
		Path path = new Path(Path.SUBPAGE_DELIMITER + "one/{arg:*}");

		assertMatch(path, Path.SUBPAGE_DELIMITER + "one/Path/Page+Name" + Path.HASHTAG_DELIMITER + "hashtag/foo",
				"arg", "Path/Page Name");
		Args args = path.configureArgs(Path.SUBPAGE_DELIMITER + "one/Path/Page+Name" + Path.HASHTAG_DELIMITER
				+ "hashtag/foo");

		assertEquals("hashtag/foo", args.getString(Path.HASHTAG));

		args = path.configureArgs(Path.SUBPAGE_DELIMITER + "one/Path/Page+Name" + Path.HASHTAG_DELIMITER + "");
		assertMatch(path, Path.SUBPAGE_DELIMITER + "one/Path/Page+Name" + Path.HASHTAG_DELIMITER + "", "arg",
				"Path/Page Name");

		assertEquals("", args.getString(Path.HASHTAG));
	}

	@Test
	public void testPathWithWildcardArgAndDoubleHashtag() {
		Path path = new Path(Path.SUBPAGE_DELIMITER + "one/{arg:*}");

		assertMatch(path, Path.SUBPAGE_DELIMITER + "one/Path/Page+Name" + Path.HASHTAG_DELIMITER + "hashtag"
				+ Path.HASHTAG_DELIMITER + "foo", "arg", "Path/Page Name");
		Args args = path.configureArgs(Path.SUBPAGE_DELIMITER + "one/Path/Page+Name" + Path.HASHTAG_DELIMITER
				+ "hashtag" + Path.HASHTAG_DELIMITER + "foo");

		assertEquals("hashtag" + Path.HASHTAG_DELIMITER + "foo", args.getString(Path.HASHTAG));
	}

	@Test
	public void testHashtag() {
		Path path = new Path(Path.SUBPAGE_DELIMITER + "one" + Path.HASHTAG_DELIMITER + "two");
		assertEquals("two", path.getHashTag());
		path = new Path(Path.SUBPAGE_DELIMITER + "one" + Path.HASHTAG_DELIMITER + "");
		assertEquals("", path.getHashTag());
		path = new Path(Path.SUBPAGE_DELIMITER + "one");
		assertNull(path.getHashTag());

	}

	@Test
	public void testSamePagePath() {
		Path path = new Path(Path.SUBPAGE_DELIMITER + "one" + Path.HASHTAG_DELIMITER + "two");
		Path path2 = new Path(Path.SUBPAGE_DELIMITER + "one");
		Path path3 = new Path(Path.SUBPAGE_DELIMITER + "one");
		Path path4 = new Path(Path.SUBPAGE_DELIMITER + "two");

		assertTrue(path.sameAs(path2));
		assertTrue(path2.sameAs(path));

		assertTrue(path3.sameAs(path2));
		assertTrue(path2.sameAs(path3));

		assertFalse(path4.sameAs(path2));
		assertFalse(path2.sameAs(path4));
	}

	protected void assertMatch(Path path, String uri, String argName, String argValue) {
		Assert.assertTrue(path.matches(uri));
		Args args = path.configureArgs(uri);
		Assert.assertNotNull(args.getString(argName));
		Assert.assertEquals(argValue, args.getString(argName));
	}
}
