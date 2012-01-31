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
package com.tasktop.c2c.server.internal.tasks.domain.conversion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.tasktop.c2c.server.internal.tasks.domain.Dependency;
import com.tasktop.c2c.server.internal.tasks.domain.Duplicate;
import com.tasktop.c2c.server.internal.tasks.domain.Keyworddef;
import com.tasktop.c2c.server.internal.tasks.service.CommentWikiRenderer;
import com.tasktop.c2c.server.internal.tasks.service.TaskServiceConfiguration;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.ExternalTaskRelation;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.WorkLog;

@org.springframework.stereotype.Component
public class TaskConverter implements ObjectConverter<Task> {

	@Override
	public boolean supportsSource(Class<?> clazz) {
		return com.tasktop.c2c.server.internal.tasks.domain.Task.class.isAssignableFrom(clazz);
	}

	@Autowired
	private TaskServiceConfiguration configuration;

	@Autowired
	private CommentWikiRenderer renderer;

	@SuppressWarnings("unchecked")
	@Override
	public void copy(Task target, Object internalObject, DomainConverter converter, DomainConversionContext context) {
		com.tasktop.c2c.server.internal.tasks.domain.Task source = (com.tasktop.c2c.server.internal.tasks.domain.Task) internalObject;

		DomainConversionContext subcontext = context.subcontext();

		target.setId(source.getId());
		target.setFoundInRelease(source.getVersion() == null ? null : source.getVersion().isEmpty() ? null : source
				.getVersion());
		target.setCreationDate(source.getCreationTs());
		target.setModificationDate(source.getDeltaTs());
		target.setVersion(source.getDeltaTs() == null ? null : Long.toString(source.getDeltaTs().getTime()));
		target.setShortDescription(source.getShortDesc());
		target.setEstimatedTime(source.getEstimatedTime());
		target.setRemainingTime(source.getRemainingTime());
		target.setDeadline(source.getDeadline());
		target.setUrl(configuration.getWebUrlForTask(target.getId()));

		// Mandatory custom fields
		target.setTaskType(source.getTaskType());

		List<ExternalTaskRelation> externalTaskRelations = new ArrayList<ExternalTaskRelation>();
		if (source.getExternalTaskRelations() != null) {
			String[] strings = StringUtils.split(source.getExternalTaskRelations(), "\n");
			Pattern p = Pattern.compile("(.*)\\.(.*): (.*)");
			for (String string : strings) {
				Matcher matcher = p.matcher(string);
				if (matcher.matches()) {
					String type = matcher.group(1);
					String kind = matcher.group(2);
					String uri = matcher.group(3);
					externalTaskRelations.add(new ExternalTaskRelation(type, kind, uri));
				}
			}
		}
		target.setExternalTaskRelations(externalTaskRelations);

		// These must be set from query join results
		target.setSeverity(context.getTaskSeverity(source.getSeverity()));
		target.setStatus(context.getTaskStatus(source.getStatus()));
		target.setResolution(context.getTaskResolution(source.getResolution()));
		target.setPriority(context.getPriority(source.getPriority()));
		target.setMilestone(context.getMilestone(source.getProduct(), source.getTargetMilestone()));

		target.setProduct((Product) converter.convert(source.getProduct(), subcontext));
		target.setComponent((Component) converter.convert(source.getComponent(), subcontext));
		target.setReporter((TaskUserProfile) converter.convert(source.getReporter(), subcontext));
		target.setAssignee((TaskUserProfile) converter.convert(source.getAssignee(), subcontext));
		target.setWatchers((List<TaskUserProfile>) converter.convert(source.getCcs(), subcontext));

		List<Keyworddef> keyworddefs = new ArrayList<Keyworddef>();
		for (com.tasktop.c2c.server.internal.tasks.domain.Keyword keyword : source.getKeywordses()) {
			keyworddefs.add(keyword.getKeyworddefs());
		}

		target.setKeywords((List<Keyword>) converter.convert(keyworddefs, subcontext));

		// Description (first comment), Comments, and Worklog items (comment with workTime)
		List<com.tasktop.c2c.server.internal.tasks.domain.Comment> sourceComments = source.getComments();
		List<WorkLog> worklogs = new ArrayList<WorkLog>();
		List<Comment> comments = new ArrayList<Comment>();
		if (sourceComments.isEmpty()) {
			target.setDescription("");
			target.setWikiRenderedDescription("");
		} else {
			target.setDescription(sourceComments.get(0).getThetext());
			target.setWikiRenderedDescription(renderer.render(target.getDescription()));

			for (int i = 1; i < sourceComments.size(); i++) {
				com.tasktop.c2c.server.internal.tasks.domain.Comment sourceComment = sourceComments.get(i);

				if (sourceComment.getWorkTime() != null && sourceComment.getWorkTime().compareTo(BigDecimal.ZERO) > 0) {
					WorkLog worklog = new WorkLog();
					worklog.setId(sourceComment.getId());
					worklog.setProfile((TaskUserProfile) converter.convert(sourceComment.getProfile(), subcontext));
					worklog.setDateWorked(sourceComment.getCreationTs());
					worklog.setHoursWorked(sourceComment.getWorkTime());
					worklog.setComment(sourceComment.getThetext());
					worklogs.add(worklog);
				} else {
					comments.add((Comment) converter.convert(sourceComment, subcontext));
				}
			}
		}
		target.setComments(comments);
		target.setWorkLogs(worklogs);

		BigDecimal sumOfSubtasksEstimate = BigDecimal.ZERO;
		BigDecimal sumOfSubtasksTimeSpent = BigDecimal.ZERO;
		Queue<Dependency> subTaskQueue = new LinkedList<Dependency>(source.getDependenciesesForBlocked());
		while (!subTaskQueue.isEmpty()) {
			com.tasktop.c2c.server.internal.tasks.domain.Task subTask = subTaskQueue.poll().getBugsByDependson();
			subTaskQueue.addAll(subTask.getDependenciesesForBlocked());

			if (subTask.getEstimatedTime() != null) {
				sumOfSubtasksEstimate = sumOfSubtasksEstimate.add(subTask.getEstimatedTime());
			}
			for (com.tasktop.c2c.server.internal.tasks.domain.Comment c : subTask.getComments()) {
				if (c.getWorkTime() != null && c.getWorkTime().signum() > 0) {
					sumOfSubtasksTimeSpent = sumOfSubtasksTimeSpent.add(c.getWorkTime());
				}
			}
		}
		target.setSumOfSubtasksEstimatedTime(sumOfSubtasksEstimate);
		target.setSumOfSubtasksTimeSpent(sumOfSubtasksTimeSpent);

		if (!context.isThin()) {
			target.setBlocksTasks(new ArrayList<Task>(source.getDependenciesesForDependson().size()));
			for (Dependency dep : source.getDependenciesesForDependson()) {
				target.getBlocksTasks().add(shallowCopyAssociate(dep.getBugsByBlocked(), subcontext));
			}

			target.setSubTasks(new ArrayList<Task>(source.getDependenciesesForBlocked().size()));
			for (Dependency dep : source.getDependenciesesForBlocked()) {
				target.getSubTasks().add(shallowCopyAssociate(dep.getBugsByDependson(), subcontext));
			}
			if (source.getDuplicatesByBugId() != null) {
				target.setDuplicateOf(shallowCopyAssociate(source.getDuplicatesByBugId().getBugsByDupeOf(), subcontext));
			}
			target.setDuplicates(new ArrayList<Task>());
			for (Duplicate duplicate : source.getDuplicatesesForDupeOf()) {
				target.getDuplicates().add(shallowCopyAssociate(duplicate.getBugsByBugId(), subcontext));
			}

			if (source.getStatusWhiteboard() != null && !source.getStatusWhiteboard().isEmpty()) {
				// A non-empty statusWhiteboard means we store description there for backward compatibility. (See
				// discussion in Task 422)
				target.setDescription(source.getStatusWhiteboard());
				target.setWikiRenderedDescription(renderer.render(source.getStatusWhiteboard()));
			}
			target.setAttachments((List<Attachment>) converter.convert(source.getAttachments(), subcontext));
		} else {
			// THIN tasks still get their parent populated
			if (!source.getDependenciesesForDependson().isEmpty()) {
				target.setParentTask(shallowCopyAssociate(source.getDependenciesesForDependson().get(0)
						.getBugsByBlocked(), subcontext));
			}
		}
	}

	private Task shallowCopyAssociate(com.tasktop.c2c.server.internal.tasks.domain.Task source,
			DomainConversionContext context) {
		Task target = new Task();
		target.setId(source.getId());
		target.setTaskType(source.getTaskType());
		target.setShortDescription(source.getShortDesc());
		target.setSeverity(context.getTaskSeverity(source.getSeverity()));
		target.setStatus(context.getTaskStatus(source.getStatus()));
		target.setResolution(context.getTaskResolution(source.getResolution()));
		target.setPriority(context.getPriority(source.getPriority()));
		target.setUrl(configuration.getWebUrlForTask(source.getId()));
		return target;
	}

	@Override
	public Class<Task> getTargetClass() {
		return Task.class;
	}
}
