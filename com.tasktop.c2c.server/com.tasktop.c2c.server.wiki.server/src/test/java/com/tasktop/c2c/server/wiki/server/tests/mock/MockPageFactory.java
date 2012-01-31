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

import com.tasktop.c2c.server.internal.wiki.server.domain.Page;
import com.tasktop.c2c.server.internal.wiki.server.domain.PageContent;
import com.tasktop.c2c.server.internal.wiki.server.domain.Person;


public class MockPageFactory {

	private static AtomicInteger created = new AtomicInteger(0);

	public static Page create(EntityManager entityManager) {
		return create(entityManager, 1).get(0);
	}

	public static List<Page> create(EntityManager entityManager, int count) {
		Person author = MockPersonFactory.create(entityManager);

		List<Page> mocks = new ArrayList<Page>(count);
		for (int x = 0; x < count; ++x) {
			Page mock = populate(new Page(), entityManager, author);
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private static Page populate(Page page, EntityManager entityManager, Person author) {
		int index = created.incrementAndGet();

		page.setOriginalAuthor(author);
		page.setPath("page" + index);

		PageContent content = MockPageContentFactory.create(entityManager, author);
		page.addPageContent(content);

		return page;
	}
}
