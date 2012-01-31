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
package com.tasktop.c2c.server.internal.tasks.service;

import java.util.LinkedList;
import java.util.Queue;

import javax.persistence.EntityManager;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.internal.tasks.domain.Dependency;
import com.tasktop.c2c.server.internal.tasks.domain.Milestone;
import com.tasktop.c2c.server.internal.tasks.domain.Task;


class TaskConstraintValidator implements Validator {

	private EntityManager entityManager;

	public TaskConstraintValidator(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Task.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Task task = (Task) target;

		validateMilestoneMatchesProduct(task, errors);
		validateCycleFree(task, errors);
	}

	private void validateCycleFree(Task task, Errors errors) {
		Queue<Task> queue = new LinkedList<Task>();

		for (Dependency dep : task.getDependenciesesForBlocked()) {
			queue.add(dep.getBugsByDependson());
		}

		while (!queue.isEmpty()) {
			Task node = entityManager.find(Task.class, queue.remove().getId());
			if (node.getId().equals(task.getId())) {
				errors.reject("task.dependency.cycle");
				return;
			}

			entityManager.refresh(node);

			for (Dependency dep : node.getDependenciesesForBlocked()) {
				queue.add(dep.getBugsByDependson());
			}

		}
	}

	private void validateMilestoneMatchesProduct(Task task, Errors errors) {

		for (Milestone productMilestone : task.getProduct().getMilestones()) {
			if (productMilestone.getValue().equals(task.getTargetMilestone())) {
				return;
			}
		}
		// FIXME TASK 168
		// errors.rejectValue("targetMilestone", "milestoneDoesNotMatchProduct");
	}

}
