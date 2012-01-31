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
package com.tasktop.c2c.server.tasks.domain.validation;

import org.apache.commons.validator.UrlValidator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.ExternalTaskRelation;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.WorkLog;

public class TaskValidator implements Validator {

	// FIXME No spring dep here, so just manual instantiation for now
	private CommentValidator commentValidator = new CommentValidator();
	private WorkLogValidator workLogValidator = new WorkLogValidator();
	private UrlValidator externalAssociationUrlValidator = new UrlValidator(new String[] { "http", "https" });

	public boolean supports(Class<?> clazz) {
		return Task.class.isAssignableFrom(clazz);
	}

	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "shortDescription", "field.required");

		Task task = (Task) target;
		if (task.getSeverity() == null || isEmpty(task.getSeverity().getValue())) {
			errors.rejectValue("severity", "field.required");
		}
		if (task.getTaskType() == null || isEmpty(task.getTaskType())) {
			errors.rejectValue("taskType", "field.required");
		}
		if (task.getIteration() == null || isEmpty(task.getIteration().getValue())) {
			errors.rejectValue("iteration", "field.required");
		}
		if (task.getStatus() == null || isEmpty(task.getStatus().getValue())) {
			errors.rejectValue("status", "field.required");
		} else {
			// when resolved, a resolution is required
			if ("RESOLVED".equalsIgnoreCase(task.getStatus().getValue())) {
				if (task.getResolution() == null || isEmpty(task.getResolution().getValue())) {
					errors.rejectValue("resolution", "field.required");
				}
			} else {
				if (task.getStatus().isOpen() && task.getResolution() != null
						&& !isEmpty(task.getResolution().getValue())) {
					errors.rejectValue("resolution", "field.prohibited");
				}
			}
		}
		if (task.getPriority() == null || isEmpty(task.getPriority().getValue())) {
			errors.rejectValue("priority", "field.required");
		}
		if (task.getMilestone() == null || isEmpty(task.getMilestone().getValue())) {
			errors.rejectValue("milestone", "field.required");
		}
		if (task.getComponent() == null || task.getComponent().getId() == null) {
			errors.rejectValue("component", "field.required");
		}
		if (task.getProduct() == null || task.getProduct().getId() == null) {
			errors.rejectValue("product", "field.required");
		}

		// allow null assignee as we set this to the component default
		if (task.getAssignee() != null && task.getAssignee().getLoginName() == null) {
			errors.rejectValue("assignee", "field.empty");
		}

		if (task.getEstimatedTime() != null && task.getEstimatedTime().signum() < 0) {
			errors.rejectValue("estimatedTime", "nonNegative");
		}
		if (task.getRemainingTime() != null && task.getRemainingTime().signum() < 0) {
			errors.rejectValue("remainingTime", "nonNegative");
		}

		// Validate the sub objects.

		// Validate Comments
		Comment curComment = null;
		for (int i = 0; i < (task.getComments() == null ? 0 : task.getComments().size()); i++) {
			curComment = task.getComments().get(i);
			if (curComment.getId() != null) {
				continue; // Only validate comments that we save
			}
			errors.pushNestedPath("comments[" + i + "]");
			commentValidator.validate(curComment, errors);
			errors.popNestedPath();
		}

		// Validate WorkLogs
		WorkLog workLog = null;
		for (int i = 0; i < (task.getWorkLogs() == null ? 0 : task.getWorkLogs().size()); i++) {
			workLog = task.getWorkLogs().get(i);
			if (workLog.getId() != null) {
				continue; // Only validate worklogs that we save
			}
			errors.pushNestedPath("workLogs[" + i + "]");
			workLogValidator.validate(workLog, errors);
			errors.popNestedPath();
		}

		validateTaskReference(task, task.getDuplicateOf(), "duplicateOf", errors);
		validateTaskReference(task, task.getParentTask(), "parentTask", errors);

		if (task.getSubTasks() != null) {
			for (int i = 0; i < task.getSubTasks().size(); i++) {
				Task subTask = task.getSubTasks().get(i);
				validateTaskReference(task, subTask, "subTasks[" + i + "]", errors);
				if (task.getParentTask() != null && task.getParentTask().equals(subTask)) {
					errors.reject("task.depAndBlock");
				} else if (task.getBlocksTasks() != null && task.getBlocksTasks().contains(subTask)) {
					errors.reject("task.depAndBlock");
				}
			}
		}

		if (task.getBlocksTasks() != null) {
			for (int i = 0; i < task.getBlocksTasks().size(); i++) {
				validateTaskReference(task, task.getBlocksTasks().get(i), "blocksTasks[" + i + "]", errors);
			}
		}

		if (task.getWatchers() != null) {
			for (int i = 0; i < task.getWatchers().size(); i++) {
				TaskUserProfile profile = task.getWatchers().get(i);
				if (profile.getLoginName() == null) {
					errors.rejectValue("watchers[" + i + "]", "field.empty");
				}
			}
		}

		if (task.getExternalTaskRelations() != null) {
			for (int i = 0; i < task.getExternalTaskRelations().size(); i++) {
				ExternalTaskRelation relation = task.getExternalTaskRelations().get(i);
				if (!externalAssociationUrlValidator.isValid(relation.getUri())) {
					errors.rejectValue("externalTaskRelations[" + i + "].uri", "invalid");
				}
			}
		}
	}

	// Put common task validation code in here
	private void validateTaskReference(Task sourceTask, Task relatedTask, String taskField, Errors errors) {
		if (relatedTask != null) {
			if (relatedTask.getId() == null) {
				errors.rejectValue(taskField, "field.id.required");
			} else if (relatedTask.getId().equals(sourceTask.getId())) {
				errors.rejectValue(taskField, "self.reference");
			}
		}
	}

	private boolean isEmpty(String value) {
		return value == null || value.trim().length() == 0;
	}
}
