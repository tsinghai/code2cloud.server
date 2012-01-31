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
package com.tasktop.c2c.server.common.service.tests.wiki;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.tenancy.context.DefaultTenancyContext;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;

import com.tasktop.c2c.server.common.service.wiki.MarkupLanguageUtil;

/**
 * @author David Green (Tasktop Technologies Inc.)
 */
public class MarkupLanguageUtilTest {

	@Before
	public void setupTenancyContext() {
		TenancyContextHolder.setContext(new DefaultTenancyContext());
		TenancyContextHolder.getContext().setTenant(new DefaultTenant("tenant-id", null));
	}

	@After
	public void tearDownTenancyContext() {
		TenancyContextHolder.clearContext();
	}

	@Test
	public void testCreateDefaultMarkupLanguage() {
		MarkupLanguageUtil.createDefaultMarkupLanguage();
	}

	@Test
	public void testCreateDefaultMarkupLanguage_Cloneable() {
		MarkupLanguageUtil.createDefaultMarkupLanguage().clone();
	}

	@Test
	public void testCreateDefaultMarkupLanguage_ImplicitHyperlinks() {
		MarkupLanguage language = MarkupLanguageUtil.createDefaultMarkupLanguage().clone();
		String html = new MarkupParser(language).parseToHtml("implicit hyperlink http://example.com text");

		System.out.println(html);

		Assert.assertTrue(html
				.contains("<body><p>implicit hyperlink <a href=\"http://example.com\">http://example.com</a> text</p></body>"));
	}

	@Test
	public void testCreateDefaultMarkupLanguage_BugReference() {
		MarkupLanguage language = MarkupLanguageUtil.createDefaultMarkupLanguage().clone();

		String html = new MarkupParser(language).parseToHtml("see bug#123 or task 3064");

		System.out.println(html);

		Assert.assertTrue(html
				.contains("<body><p>see <a href=\"#projects/tenant-id/task/123\">bug#123</a> or <a href=\"#projects/tenant-id/task/3064\">task 3064</a></p></body>"));
	}

	@Test
	public void testCreateDefaultMarkupLanguage_EmailAddressReference() {
		MarkupLanguage language = MarkupLanguageUtil.createDefaultMarkupLanguage().clone();

		String html = new MarkupParser(language).parseToHtml("contact some.one@example.com for more information");

		System.out.println(html);

		Assert.assertTrue(html
				.contains("<body><p>contact <a href=\"mailto:some.one@example.com\">some.one@example.com</a> for more information</p></body>"));
	}

}
