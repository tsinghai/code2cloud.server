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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;


import com.tasktop.c2c.server.internal.tasks.domain.Component;
import com.tasktop.c2c.server.internal.tasks.domain.Product;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.internal.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

public class MockTaskFactory {

	private static int created = 0;

	public static Task create(EntityManager entityManager, Product product, Profile reporter) {
		return create(entityManager, product, reporter, 1).get(0);
	}

	public static List<Task> create(EntityManager entityManager, Product product, Profile reporter, int count) {
		List<Task> mocks = new ArrayList<Task>(count);
		for (int x = 0; x < count; ++x) {
			Task mock = populate(new Task(), product, reporter);
			if (entityManager != null) {
				entityManager.persist(mock);
			}
			mocks.add(mock);
		}
		return mocks;
	}

	private synchronized static Task populate(Task task, Product product, Profile reporter) {
		int index = ++created;

		task.setTaskType("Task");
		task.setProduct(product);
		if (product != null) {
			product.getTasks().add(task);
		}

		task.setVersion("---");
		task.setReporter(reporter);
		if (reporter != null) {
			reporter.getReporterTasks().add(task);
		}
		task.setProduct(product);
		if (product != null) {
			product.getTasks().add(task);
			Component component = product.getComponents().iterator().next();
			task.setComponent(component);
			component.getTasks().add(task);
			task.setAssignee(component.getInitialOwner());
			component.getInitialOwner().getAssigneeTasks().add(task);
		}

		task.setShortDesc("task summary " + index);
		task.setStatus("NEW");
		task.setSeverity("normal");
		task.setOpSys("opsys");
		task.setPriority("Normal");
		task.setRepPlatform("-");
		task.setResolution("NONE");
		task.setTargetMilestone("---");
		task.setStatusWhiteboard("");
		task.setKeywords("");
		task.setEstimatedTime(BigDecimal.ZERO);
		task.setRemainingTime(BigDecimal.ZERO);

		return task;
	}

	public static com.tasktop.c2c.server.tasks.domain.Task createDO() {
		com.tasktop.c2c.server.tasks.domain.Task mock = new com.tasktop.c2c.server.tasks.domain.Task();
		mock.setShortDescription("short desc");

		mock.setDescription("desc");

		mock.setSeverity(new TaskSeverity());
		mock.getSeverity().setSortkey((short) 1);
		mock.getSeverity().setValue("low");

		mock.setStatus(new TaskStatus());
		mock.getStatus().setSortkey((short) 1);
		mock.getStatus().setValue(("Open"));

		mock.setPriority(new Priority());
		mock.getPriority().setValue("High");
		mock.getPriority().setSortkey((short) 1);

		mock.setMilestone(new Milestone());
		mock.getMilestone().setValue("---");
		mock.getMilestone().setSortkey((short) 1);

		mock.setProduct(new com.tasktop.c2c.server.tasks.domain.Product());
		mock.setComponent(new com.tasktop.c2c.server.tasks.domain.Component());
		mock.setAssignee(new TaskUserProfile());
		mock.getAssignee().setLoginName("joe.bloe");

		mock.getComponent().setId(1);
		mock.getProduct().setId(1);
		mock.setTaskType("Task");
		mock.setIteration(new Iteration());
		mock.getIteration().setValue("---");

		return mock;
	}
}
