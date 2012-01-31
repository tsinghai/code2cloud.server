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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.internal.wiki.server.InternalWikiService;
import com.tasktop.c2c.server.internal.wiki.server.domain.MarkupRenderer;
import com.tasktop.c2c.server.internal.wiki.server.domain.Page;
import com.tasktop.c2c.server.internal.wiki.server.domain.PageContent;
import com.tasktop.c2c.server.wiki.server.tests.mock.MockPageFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml", "/applicationContext-testSecurity.xml" })
@Transactional
public class InternalWikiServiceTest {
	@Autowired
	private InternalWikiService service;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private MarkupRenderer markupRenderer;

	@Before
	public void setUpRenderer() {
		// Insert a stub tenancyContext - it's required because it's inserted into in-page URLs by the renderer.
		TenancyContextHolder.setContext(TenancyContextHolder.createEmptyContext());
		TenancyContextHolder.getContext().setTenant(new DefaultTenant("someProjectId", null));
	}

	@Test
	public void updatesOutOfDatePages() throws EntityNotFoundException {
		List<Page> pages = MockPageFactory.create(entityManager, 2);
		int count = 0;
		for (Page page : pages) {
			for (PageContent content : page.getPageContent()) {
				++count;
				markupRenderer.render(content);
				if (count % 2 == 0) {
					content.setRendererVersion("INVALID");
				}
			}
		}
		Assert.assertTrue(count > 1);

		entityManager.flush();
		entityManager.clear();

		service.updatePageContent();

		TypedQuery<PageContent> query = entityManager.createQuery("select pc from " + PageContent.class.getSimpleName()
				+ " pc", PageContent.class);
		Assert.assertEquals(count, query.getResultList().size());
		for (PageContent content : query.getResultList()) {
			String expectedVersion = markupRenderer.getMarkupLanguageToVersion().get(
					content.getPage().getMarkupLanguage());
			Assert.assertEquals(expectedVersion, content.getRendererVersion());
		}
	}
}
