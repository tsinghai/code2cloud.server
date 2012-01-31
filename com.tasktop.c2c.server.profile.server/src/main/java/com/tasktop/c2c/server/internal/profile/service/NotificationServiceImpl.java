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
package com.tasktop.c2c.server.internal.profile.service;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.profile.domain.Email;
import com.tasktop.c2c.server.profile.domain.internal.NotificationSettings;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.service.EmailService;
import com.tasktop.c2c.server.profile.service.NotificationService;
import com.tasktop.c2c.server.profile.service.ProfileService;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskActivity;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.FieldUpdate;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.Type;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@Service
public class NotificationServiceImpl extends AbstractJpaServiceBean implements NotificationService {

	private static final Logger LOG = Logger.getLogger(NotificationServiceImpl.class.getName());

	@Autowired
	private ProfileService profileService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private VelocityEngine velocityEngine;

	private String taskCreateHeaderTemplate = "com/tasktop/c2c/server/internal/profile/service/template/taskCreateActivityHeader.vm";
	private String taskUpdateHeaderTemplate = "com/tasktop/c2c/server/internal/profile/service/template/taskUpdateActivityHeader.vm";
	private String taskAttachActivityTemplate = "com/tasktop/c2c/server/internal/profile/service/template/taskAttachActivity.vm";
	private String taskUpdateActivityTemplate = "com/tasktop/c2c/server/internal/profile/service/template/taskUpdateActivity.vm";
	private String taskCommentActivityTemplate = "com/tasktop/c2c/server/internal/profile/service/template/taskCommentActivity.vm";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.internal.profile.service.NotificationService#constructDefaultSettings()
	 */
	@Override
	public NotificationSettings constructDefaultSettings() {
		NotificationSettings settings = new NotificationSettings();
		settings.setEmailTaskActivity(true);
		settings.setEmailServiceAndMaintenance(true);
		settings.setEmailNewsAndEvents(false);
		return settings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.internal.profile.service.NotificationService#processTaskActivity(java.util.List)
	 */
	@Override
	public void processTaskActivity(List<TaskActivity> activities) {
		if (activities.isEmpty()) {
			return;
		}
		Task task = activities.get(0).getTask();
		TaskUserProfile changeAuthor = activities.get(0).getAuthor();
		List<String> userNamesRemoved = new ArrayList<String>();

		boolean isCreateActivity = false;
		for (TaskActivity activity : activities) {
			if (!activity.getTask().equals(task)) {
				throw new IllegalStateException("expected to be called with all activities from the same task");
			}
			if (!activity.getAuthor().equals(changeAuthor)) {
				throw new IllegalStateException("expected to be called with all activities from the same author");
			}
			if (activity.getActivityType().equals(Type.CREATED)) {
				isCreateActivity = true;
			}
			if (activity.getActivityType().equals(Type.UPDATED)) {
				for (FieldUpdate update : activity.getFieldUpdates()) {
					// FIXME ugly string ref
					if (update.getFieldName().equals("cc")) {
						// oldValue are the ones removed
						userNamesRemoved.addAll(Arrays.asList(update.getOldValue().split(", ")));
					} else if (update.getFieldName().equals("assigned_to")) {
						if (!update.getOldValue().isEmpty()) {
							userNamesRemoved.add(update.getOldValue());
						}
					}
				}
			}
		}

		Collection<Profile> toEmail = computeEmailList(task, changeAuthor, userNamesRemoved, isCreateActivity);

		if (toEmail.isEmpty()) {
			return;
		}

		String subject = String.format("[%s %s] %s%s", task.getTaskType(), task.getId(), isCreateActivity ? "New: "
				: "", task.getShortDescription());
		String body = generateEmailHeader(isCreateActivity, task);

		// guarantee the order we call these for the body email
		Collections.sort(activities, new Comparator<TaskActivity>() {

			@Override
			public int compare(TaskActivity a1, TaskActivity a2) {
				return getSortOrder(a1.getActivityType()) - getSortOrder(a2.getActivityType());
			}
		});

		for (TaskActivity activity : activities) {
			body += generateEmailPart(activity);
		}

		for (Profile p : toEmail) {
			Email e = new Email(p.getEmail(), subject, body, "text/plain");
			emailService.schedule(e);
		}
	}

	private int getSortOrder(TaskActivity.Type type) {
		switch (type) {
		case CREATED:
		case UPDATED:
			return 0;
		case COMMENTED:
			return 1;
		case ATTACHED:
			return 2;
		default:
			return 5;
		}
	}

	/**
	 * @param task
	 * @param userNamesRemoved
	 * @return
	 */
	private Collection<Profile> computeEmailList(Task task, TaskUserProfile changeAuthor,
			List<String> userNamesRemoved, boolean isCreate) {
		Set<Profile> toEmail = new HashSet<Profile>();
		for (TaskUserProfile watcher : task.getWatchers()) {
			try {
				Profile profile = profileService.getProfileByUsername(watcher.getLoginName());
				if (shouldNotifyOfTaskActivity(profile)) {
					toEmail.add(profile);
				}
			} catch (EntityNotFoundException e) {
				LOG.warning(String.format("Could not find username [%s] from task in profile.", watcher.getLoginName()));
			}
		}

		if (task.getAssignee() != null) {
			try {
				Profile profile = profileService.getProfileByUsername(task.getAssignee().getLoginName());
				if (shouldNotifyOfTaskActivity(profile)) {
					toEmail.add(profile);
				}
			} catch (EntityNotFoundException e) {
				LOG.warning(String.format("Could not find username [%s] from task in profile.", task.getAssignee()
						.getLoginName()));
			}
		}

		if (isCreate && task.getComponent() != null && task.getComponent().getInitialOwner() != null) {
			try {
				Profile profile = profileService.getProfileByUsername(task.getComponent().getInitialOwner()
						.getLoginName());
				if (shouldNotifyOfTaskActivity(profile)) {
					toEmail.add(profile);
				}
			} catch (EntityNotFoundException e) {
				LOG.warning(String.format("Could not find username [%s] from task in profile.", task.getComponent()
						.getInitialOwner().getLoginName()));
			}
		}

		for (String user : userNamesRemoved) {
			try {
				Profile profile = profileService.getProfileByUsername(user);
				if (shouldNotifyOfTaskActivity(profile)) {
					toEmail.add(profile);
				}
			} catch (EntityNotFoundException e) {
				LOG.warning(String.format("Could not find username [%s] from task in profile.", user));
			}
		}

		// Do not email the user that initiated the change.
		try {
			Profile profile = profileService.getProfileByUsername(changeAuthor.getLoginName());
			toEmail.remove(profile);
		} catch (EntityNotFoundException e) {
			LOG.warning(String.format("Could not find username [%s] from task in profile.", changeAuthor.getLoginName()));
		}

		return toEmail;
	}

	/**
	 * @param isCreateActivity
	 * @param task
	 * @return
	 */
	private String generateEmailHeader(boolean isCreateActivity, Task task) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("task", task);
		if (task.getDescription() == null) {
			task.setDescription("");
		}
		String taskTags = "";
		for (Keyword k : task.getKeywords()) {
			if (!taskTags.isEmpty()) {
				taskTags += ", ";
			}
			taskTags += k.getName();
		}
		model.put("taskTags", taskTags);
		String taskWatchers = "";
		for (TaskUserProfile watcher : task.getWatchers()) {
			if (!taskWatchers.isEmpty()) {
				taskWatchers += ", ";
			}
			taskWatchers += watcher.getLoginName();
		}
		model.put("taskWatchers", taskWatchers);
		model.put("taskOwner", task.getAssignee() == null ? "" : task.getAssignee().getLoginName());
		String template = isCreateActivity ? taskCreateHeaderTemplate : taskUpdateHeaderTemplate;
		return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template, model);
	}

	private static final int UPDATE_FIELD_NAME_COL_SPACES = 20;
	private static final int UPDATE_FROM_COL_SPACES = 28;
	private static final int UPDATE_TO_COL_SPACES = 28;

	private String generateEmailPart(TaskActivity activity) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("activity", activity);
		String template;
		switch (activity.getActivityType()) {
		case ATTACHED:
			template = taskAttachActivityTemplate;
			break;
		case COMMENTED:
			template = taskCommentActivityTemplate;
			model.put("commentNumber", computeCommentNumber(activity.getTask(), activity.getComment()));
			model.put("commentTime", DateFormat.getDateTimeInstance().format(activity.getComment().getCreationDate()));
			break;
		case CREATED:
			return ""; // This was handled in the header
		case UPDATED:
			template = taskUpdateActivityTemplate;
			// Hack to get the formatting consistent.
			for (FieldUpdate update : activity.getFieldUpdates()) {
				update.setFieldDescription(addSpacesForColumns(update.getFieldDescription(),
						UPDATE_FIELD_NAME_COL_SPACES));
				update.setNewValue(addSpacesForColumns(update.getNewValue(), UPDATE_TO_COL_SPACES));
				update.setOldValue(addSpacesForColumns(update.getOldValue(), UPDATE_FROM_COL_SPACES));
				model.put("updateHeader", computeUpdateHeader());
			}
			break;
		default:
			throw new RuntimeException("Unknown activity type: " + activity.getActivityType());
		}

		return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template, model);

	}

	/**
	 * @param task
	 * @param comment
	 * @return
	 */
	private int computeCommentNumber(Task task, Comment comment) {
		int i = 1;
		for (Comment c : task.getComments()) {
			if (c.equals(comment)) {
				return i;
			}
			i++;
		}
		throw new IllegalStateException("can not find comment in task");
	}

	/**
	 * @return
	 */
	private String computeUpdateHeader() {
		return addSpacesForColumns("Property", UPDATE_FIELD_NAME_COL_SPACES) + " | "
				+ addSpacesForColumns("Removed", UPDATE_FROM_COL_SPACES) + " | "
				+ addSpacesForColumns("Added", UPDATE_TO_COL_SPACES);
	}

	/**
	 * @param fieldName
	 * @return
	 */
	private String addSpacesForColumns(String fieldName, int desiredSpaces) {
		int spacesToAdd = desiredSpaces - fieldName.length();
		if (spacesToAdd <= 0) {
			return fieldName;
		}
		for (int i = 0; i < spacesToAdd; i++) {
			fieldName = fieldName + " ";
		}
		return fieldName;
	}

	/**
	 * @param profile
	 * @return
	 */
	private boolean shouldNotifyOfTaskActivity(Profile profile) {
		return !profile.getDisabled() && profile.getEmailVerified()
				&& profile.getNotificationSettings().getEmailTaskActivity();
	}

	/**
	 * @param emailService
	 *            the emailService to set
	 */
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

}
