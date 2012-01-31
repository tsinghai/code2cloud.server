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
package com.tasktop.c2c.server.tasks.tests.domain.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import com.tasktop.c2c.server.internal.tasks.domain.Comment;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.internal.tasks.domain.Task;


public class MockCommentFactory {

	private static int created = 0;

	public static Comment create(EntityManager entityManager, Task task, Profile commenter) {
		return create(entityManager, task, commenter, 1).get(0);
	}

	public static List<Comment> create(EntityManager entityManager, Task task, Profile commenter, int count) {
		List<Comment> mocks = new ArrayList<Comment>(count);
		for (int x = 0; x < count; ++x) {
			Comment mock = populate(new Comment(), task, commenter);
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private synchronized static Comment populate(Comment mock, Task task, Profile commenter) {
		int index = ++created;

		mock.setCreationTs(new Date());
		mock.setProfile(commenter);
		mock.setTask(task);
		mock.setThetext("text of comment " + index);

		task.getComments().add(mock);

		return mock;
	}

}
