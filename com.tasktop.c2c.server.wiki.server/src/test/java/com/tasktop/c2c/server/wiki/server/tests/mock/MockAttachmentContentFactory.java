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

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.internal.wiki.server.domain.AttachmentContent;
import com.tasktop.c2c.server.internal.wiki.server.domain.Person;


public class MockAttachmentContentFactory {

	public static AttachmentContent create(EntityManager entityManager, Person author) {
		return create(entityManager, 1, author).get(0);
	}

	public static List<AttachmentContent> create(EntityManager entityManager, int count, Person author) {
		List<AttachmentContent> mocks = new ArrayList<AttachmentContent>(count);
		for (int x = 0; x < count; ++x) {
			AttachmentContent mock = populate(new AttachmentContent(), entityManager, author);
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private static AttachmentContent populate(AttachmentContent content, EntityManager entityManager, Person author) {
		content.setContent(new byte[] { 1, 2, 3 });
		content.setAuthor(author);
		content.setSize(content.getContent().length);

		author.getAuthorAttachmentContent().add(content);

		return content;
	}
}
