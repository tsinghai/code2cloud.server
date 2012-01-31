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
package com.tasktop.c2c.server.wiki.server.tests.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.internal.wiki.server.domain.PageContent;
import com.tasktop.c2c.server.internal.wiki.server.domain.Person;


public class MockPageContentFactory {

	private static AtomicInteger created = new AtomicInteger(0);

	public static PageContent create(EntityManager entityManager, Person author) {
		return create(entityManager, 1, author).get(0);
	}

	public static List<PageContent> create(EntityManager entityManager, int count, Person author) {
		List<PageContent> mocks = new ArrayList<PageContent>(count);
		for (int x = 0; x < count; ++x) {
			PageContent mock = populate(new PageContent(), entityManager, author);
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private static PageContent populate(PageContent content, EntityManager entityManager, Person author) {
		int index = created.incrementAndGet();

		content.setContent("some markup " + index
				+ "\n\nh1. Heading1\n\nparagraph\n\nbc. code block\n\nh2. Heading2\n\npara 2 with more content");
		content.setRenderedContent("<p>some markup " + index
				+ "</p><h1 id=\"Heading1\">Heading1</h1><p>more content</p>");
		content.setRendererVersion("123;1234;12345");
		content.setAuthor(author);
		author.getAuthorContent().add(content);

		return content;
	}
}
