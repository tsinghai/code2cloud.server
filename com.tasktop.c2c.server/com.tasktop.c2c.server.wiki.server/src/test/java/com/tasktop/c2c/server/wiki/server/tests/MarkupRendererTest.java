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
package com.tasktop.c2c.server.wiki.server.tests;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tasktop.c2c.server.internal.wiki.server.domain.MarkupRenderer;
import com.tasktop.c2c.server.internal.wiki.server.domain.Page;
import com.tasktop.c2c.server.internal.wiki.server.domain.PageContent;
import com.tasktop.c2c.server.wiki.server.tests.mock.MockPageFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
public class MarkupRendererTest {

	@Autowired
	public MarkupRenderer markupRenderer;

	private PageContent pageContent;

	private Page page;

	@Before
	public void before() {
		page = MockPageFactory.create(null);
		page.setId(1L);

		pageContent = page.getLastPageContent();

		// Insert a stub tenancyContext - it's required because it's inserted into in-page URLs by the renderer.
		TenancyContextHolder.setContext(TenancyContextHolder.createEmptyContext());
		TenancyContextHolder.getContext().setTenant(new DefaultTenant("someProjectId", null));
	}

	@Test
	public void escapesHtml() {
		pageContent.setContent("here's a newline<br/>and <b>bold</b>");
		markupRenderer.render(pageContent);

		Assert.assertEquals("<p>here&#8217;s a newline&lt;br/&gt;and &lt;b&gt;bold&lt;/b&gt;</p>",
				pageContent.getRenderedContent());
	}

	@Test
	public void escapesXml() {
		pageContent.setContent("here's a newline<foo id=\"abc\"/>and <bar id='none'>bold</b>");
		markupRenderer.render(pageContent);

		Assert.assertEquals(
				"<p>here&#8217;s a newline&lt;foo id=\"abc\"/&gt;and &lt;bar id=&#8216;none&#8217;&gt;bold&lt;/b&gt;</p>",
				pageContent.getRenderedContent());
	}

	@Test
	public void allowsUnescapedCharacters() {

		pageContent.setContent("some ==<br/>== content with a newline");
		markupRenderer.render(pageContent);

		Assert.assertEquals("<p>some <br/> content with a newline</p>", pageContent.getRenderedContent());
	}

	@Test
	public void disallowsUnsafeUnescapedCharacters() {

		pageContent.setContent("some ==<iframe src=\"http://google.com/\">== unsafe content");
		markupRenderer.render(pageContent);

		Assert.assertEquals("<p>some &lt;iframe src=\"http://google.com/\"&gt; unsafe content</p>",
				pageContent.getRenderedContent());
	}

	@Test
	public void disallowsUnescapedTagWithJavascript() {

		pageContent.setContent("some ==<h1 onClick=\"doSomethingEvil();\">bad stuff</h1>== more");
		markupRenderer.render(pageContent);

		Assert.assertEquals("<p>some &lt;h1 onClick=\"doSomethingEvil();\"&gt;bad stuff&lt;/h1&gt; more</p>",
				pageContent.getRenderedContent());
	}

	@Test
	public void testInPageLinksWithSlash() {
		page.setPath("One/Two");

		pageContent.setContent("h1. Table Of Contents\n\n{toc}\n\nh1. Heading1\n\ncontent");

		markupRenderer.render(pageContent);

		Assert.assertTrue(pageContent.getRenderedContent().contains(
				"<a href=\"#projects/someProjectId/wiki/p/One/Two-TableOfContents\">Table Of Contents</a>"));
	}

	@Test
	public void rendererComputesVersion() {
		pageContent.setContent("abc");

		Assert.assertFalse(markupRenderer.isUpToDate(pageContent));

		markupRenderer.render(pageContent);
		System.out.println(pageContent.getRendererVersion());

		assertNotNull(pageContent.getRendererVersion());

		Assert.assertTrue(markupRenderer.isUpToDate(pageContent));

		String[] parts = pageContent.getRendererVersion().split(";");
		Assert.assertEquals(3, parts.length);

		for (int x = 0; x < parts.length; ++x) {
			boolean looksGood = false;
			if (parts[x].matches("(\\d+\\.){3}([a-zA-Z0-9_-]+)?")) {
				looksGood = true;
			} else if (x == 0) {
				// when running tests, first part is not in a jar file thus we have no version number
				looksGood = parts[x].equals("null");
			}
			if (!looksGood) {
				Assert.fail("Version string doesn't look good for part " + x + " \"" + parts[x] + "\" of "
						+ pageContent.getRendererVersion());
			}
		}
	}
}
