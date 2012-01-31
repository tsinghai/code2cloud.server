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
package com.tasktop.c2c.server.common.web.tests.client.widgets.hyperlink;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.tasktop.c2c.server.common.web.client.widgets.hyperlink.Hyperlink;
import com.tasktop.c2c.server.common.web.client.widgets.hyperlink.HyperlinkComparator;
import com.tasktop.c2c.server.common.web.client.widgets.hyperlink.UrlHyperlink;
import com.tasktop.c2c.server.common.web.client.widgets.hyperlink.UrlHyperlinkDetector;


/**
 * @author David Green (Tasktop Technologies Inc.)
 */
public class UrlHyperlinkDetectorTest {

	private UrlHyperlinkDetector detector = new UrlHyperlinkDetector();

	@Test
	public void testNoHyperlinks() {
		Assert.assertTrue(detector.detectHyperlinks("").isEmpty());
		Assert.assertTrue(detector.detectHyperlinks("abc123 456").isEmpty());
		Assert.assertTrue(detector.detectHyperlinks("http://").isEmpty());
		Assert.assertTrue(detector.detectHyperlinks("https://").isEmpty());
	}

	@Test
	public void testSimple() {
		String hyperlinkText = "http://foobar.com";
		assertHyperlinksFound(hyperlinkText, new UrlHyperlink(hyperlinkText, 0, hyperlinkText.length()));
		assertHyperlinksFound(" " + hyperlinkText, new UrlHyperlink(hyperlinkText, 1, hyperlinkText.length()));
		assertHyperlinksFound(hyperlinkText + " ", new UrlHyperlink(hyperlinkText, 0, hyperlinkText.length()));
	}

	@Test
	public void testMultiple() {
		String hyperlink1 = "http://foobar.com";
		String hyperlink2 = "https://www.example.com/A+B/C%20D?one=two&three=four-five";
		assertHyperlinksFound(" a " + hyperlink1 + " and\n" + hyperlink2,
				new UrlHyperlink(hyperlink1, 3, hyperlink1.length()),
				new UrlHyperlink(hyperlink2, 25, hyperlink2.length()));
	}

	@Test
	public void testNotSeparated() {
		String hyperlinkText = "http://foobar.com";
		assertHyperlinksFound("abc" + hyperlinkText, new UrlHyperlink(hyperlinkText, 3, hyperlinkText.length()));
	}

	@Test
	public void testNotSeparated2() {
		String hyperlinkText = "atp://foobar.com";
		Assert.assertTrue(detector.detectHyperlinks("abc" + hyperlinkText).isEmpty());
	}

	private void assertHyperlinksFound(String text, Hyperlink... expected) {
		List<Hyperlink> hyperlinks = detector.detectHyperlinks(text);
		Collections.sort(hyperlinks, new HyperlinkComparator());
		Assert.assertEquals(hyperlinks.size(), expected.length);
		for (int x = 0; x < hyperlinks.size(); ++x) {
			Assert.assertEquals(expected[x], hyperlinks.get(x));
		}
	}

}
