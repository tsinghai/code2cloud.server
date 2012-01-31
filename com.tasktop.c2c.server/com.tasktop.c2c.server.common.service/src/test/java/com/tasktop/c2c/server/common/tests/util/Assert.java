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
package com.tasktop.c2c.server.common.tests.util;

import java.util.regex.Pattern;

public class Assert {

	public static void assertMatches(String regex,String text) {
		assertMatches(Pattern.compile(regex),text);
	}

	private static void assertMatches(Pattern pattern, String text) {
		org.junit.Assert.assertTrue(String.format("expected text matching pattern %s but got %s",pattern,text),text==null?false:pattern.matcher(text).matches());
	}
}
