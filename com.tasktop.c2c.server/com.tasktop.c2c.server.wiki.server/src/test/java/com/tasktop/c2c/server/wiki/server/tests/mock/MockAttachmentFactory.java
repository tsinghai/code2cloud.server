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

import com.tasktop.c2c.server.internal.wiki.server.domain.Attachment;
import com.tasktop.c2c.server.internal.wiki.server.domain.AttachmentContent;
import com.tasktop.c2c.server.internal.wiki.server.domain.Page;
import com.tasktop.c2c.server.internal.wiki.server.domain.Person;


public class MockAttachmentFactory {

	private static AtomicInteger created = new AtomicInteger(0);

	public static Attachment create(EntityManager entityManager, Page page) {
		return create(entityManager, page, 1).get(0);
	}

	public static List<Attachment> create(EntityManager entityManager, Page page, int count) {
		Person author = MockPersonFactory.create(entityManager);

		List<Attachment> mocks = new ArrayList<Attachment>(count);
		for (int x = 0; x < count; ++x) {
			Attachment mock = populate(new Attachment(), entityManager, author, page);
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private static Attachment populate(Attachment mock, EntityManager entityManager, Person author, Page page) {
		int index = created.incrementAndGet();

		mock.setOriginalAuthor(author);
		mock.setName("attach" + index + ".png");
		mock.setMimeType("image/png");
		if (page != null) {
			mock.setPage(page);
			page.getAttachments().add(mock);
		}
		AttachmentContent content = MockAttachmentContentFactory.create(null, author);
		mock.addAttachmentContent(content);

		return mock;
	}
}
