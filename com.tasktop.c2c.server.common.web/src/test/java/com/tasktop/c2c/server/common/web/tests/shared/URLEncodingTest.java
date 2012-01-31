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
package com.tasktop.c2c.server.common.web.tests.shared;

import static junit.framework.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.Test;

import com.tasktop.c2c.server.common.web.shared.URLEncoding;


public class URLEncodingTest {

	@Test
	public void testBasicEncoding() {
		String s = "foo bar Baz";
		assertEncodesAndDecodes(s);
		String encoded = URLEncoding.encode(s);
		assertEquals("foo+bar+Baz", encoded);
	}

	@Test
	public void testExcludes() {
		String s = "foo/bar baz";
		String encoded = URLEncoding.encode(s, new char[] { '/' });
		assertEquals("foo/bar+baz", encoded);
		String decoded = URLEncoding.decode(encoded);
		assertEquals(s, decoded);
	}

	@Test
	public void testReservedCharacters() {
		for (int x = 32; x <= 127; ++x) {
			char c = (char) x;
			if (c == '*' || c == '~') {
				continue;
			}
			String s = "" + c;
			assertEncodesAndDecodes(s);
		}
	}

	private void assertEncodesAndDecodes(String s) {
		String encoded = URLEncoding.encode(s);
		System.out.println(s + " -> " + encoded);
		assertEncodedEquivalent(s, encoded);
		assertEquals(s, URLEncoding.decode(encoded));
	}

	private void assertEncodedEquivalent(String expected, String encoded) {
		try {
			assertEquals(URLEncoder.encode(expected, "utf-8"), encoded);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
}
