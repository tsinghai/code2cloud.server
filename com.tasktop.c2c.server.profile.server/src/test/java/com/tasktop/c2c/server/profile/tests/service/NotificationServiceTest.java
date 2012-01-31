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
package com.tasktop.c2c.server.profile.tests.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.internal.profile.service.NotificationServiceImpl;
import com.tasktop.c2c.server.profile.domain.Email;
import com.tasktop.c2c.server.profile.domain.internal.Profile;
import com.tasktop.c2c.server.profile.service.EmailService;
import com.tasktop.c2c.server.profile.service.ProfileService;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProfileFactory;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskActivity;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.FieldUpdate;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.Type;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-testDisableSecurity.xml" })
@Transactional
public class NotificationServiceTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private NotificationServiceImpl notificationService;

	@Autowired
	private ProfileService profileService;

	private List<Email> outgoingEmails = new ArrayList<Email>();

	private TaskUserProfile taskUser;
	private TaskUserProfile taskUser2;
	private Profile profileUser;
	private Profile profileUser2;

	private Task task;
	private Comment comment;
	private Attachment attachment;

	@Before
	public void setup() throws ValidationException {
		outgoingEmails.clear();

		notificationService.setEmailService(new EmailService() {

			@Override
			public void schedule(Email email) {
				outgoingEmails.add(email);
			}

			@Override
			public void deliver(Email email) throws MailException {
				Assert.fail("Should be scheduling");

			}
		});

		taskUser = new TaskUserProfile();
		taskUser.setRealname("Real Name");
		taskUser.setLoginName("loginname");

		taskUser2 = new TaskUserProfile();
		taskUser2.setRealname("Real Name2");
		taskUser2.setLoginName("loginname2");

		profileUser = MockProfileFactory.create(null);
		profileUser.setUsername(taskUser.getLoginName());
		profileUser.setEmailVerified(true);
		profileService.createProfile(profileUser);

		profileUser2 = MockProfileFactory.create(null);
		profileUser2.setUsername(taskUser2.getLoginName());
		profileUser2.setEmailVerified(true);
		profileService.createProfile(profileUser2);

		task = new Task();
		task.setUrl("http:foo.bar/task/1");
		task.setShortDescription("Short Desc");
		task.setId(1);
		task.setWatchers(Arrays.asList(taskUser));
		task.setTaskType("Task");
		task.setReporter(taskUser);
		task.setPriority(new Priority());
		task.getPriority().setValue("P2");
		task.setSeverity(new TaskSeverity());
		task.getSeverity().setValue("enhancement");
		task.setMilestone(new Milestone());
		task.getMilestone().setValue("1.0.0");
		task.setProduct(new Product());
		task.getProduct().setName("Server");
		task.setComponent(new Component());
		task.getComponent().setName("Deployments");
		task.setIteration(new Iteration("28"));
		task.setStatus(new TaskStatus());
		task.getStatus().setValue("NEW");
		task.setDescription("Lorem ipsum dolor sit amet, ");
		task.setKeywords(Arrays.asList(new Keyword("comma", ""), new Keyword("seperated", ""), new Keyword("list", "")));

		comment = new Comment();
		comment.setAuthor(taskUser);
		comment.setCreationDate(new Date());
		comment.setCommentText("This is my\nmultiline comment");
		task.setComments(Arrays.asList(comment));

		attachment = new Attachment();
		attachment.setSubmitter(taskUser);
		attachment.setFilename("filename.txt");
		attachment.setDescription("attach desc here");
		task.setAttachments(Arrays.asList(attachment));

	}

	@Test
	public void testCreateEmail() throws EntityNotFoundException, ValidationException {
		TaskActivity createActivity = new TaskActivity();
		createActivity.setTask(task);
		createActivity.setActivityDate(new Date());
		createActivity.setActivityType(Type.CREATED);
		createActivity.setAuthor(taskUser2);

		notificationService.processTaskActivity(Arrays.asList(createActivity));

		Assert.assertEquals(1, outgoingEmails.size());
		Email mail = outgoingEmails.get(0);
		Assert.assertEquals(profileUser.getEmail(), mail.getTo());
		System.out.println(mail.getBody());

		outgoingEmails.clear();
		profileUser.getNotificationSettings().setEmailTaskActivity(false);
		profileService.updateProfile(profileUser);

		notificationService.processTaskActivity(Arrays.asList(createActivity));

		Assert.assertEquals(0, outgoingEmails.size());

	}

	@Test
	public void testUpdateEmail() {

		FieldUpdate u1 = new FieldUpdate();
		u1.setFieldName("priority");
		u1.setFieldDescription("Priority");
		u1.setOldValue("High");
		u1.setNewValue("Low");

		FieldUpdate u2 = new FieldUpdate();
		u2.setFieldName("bug_status");
		u2.setFieldDescription("Status");
		u2.setOldValue("UNCONFIRMED");
		u2.setNewValue("NEW");

		TaskActivity updateActivity = new TaskActivity();
		updateActivity.setTask(task);
		updateActivity.setActivityDate(new Date());
		updateActivity.setActivityType(Type.UPDATED);
		updateActivity.setFieldUpdates(Arrays.asList(u1, u2));
		updateActivity.setAuthor(taskUser2);

		TaskActivity commentActivity = new TaskActivity();
		commentActivity.setTask(task);
		commentActivity.setActivityDate(new Date());
		commentActivity.setActivityType(Type.COMMENTED);
		commentActivity.setComment(comment);
		commentActivity.setAuthor(taskUser2);

		TaskActivity attachmentActivity = new TaskActivity();
		attachmentActivity.setTask(task);
		attachmentActivity.setActivityDate(new Date());
		attachmentActivity.setActivityType(Type.ATTACHED);
		attachmentActivity.setAttachment(attachment);
		attachmentActivity.setAuthor(taskUser2);

		notificationService.processTaskActivity(Arrays.asList(updateActivity, commentActivity, attachmentActivity));

		Assert.assertEquals(1, outgoingEmails.size());
		Email mail = outgoingEmails.get(0);
		Assert.assertEquals(profileUser.getEmail(), mail.getTo());
		System.out.println(mail.getBody());
	}

	@Test
	public void testUpdateEmailToRemovedWather() {
		task.setWatchers(new ArrayList<TaskUserProfile>());
		task.setAssignee(null);
		task.setReporter(taskUser2);

		FieldUpdate u1 = new FieldUpdate();
		u1.setFieldName("cc");
		u1.setFieldDescription("Watcher");
		u1.setOldValue(taskUser.getLoginName());
		u1.setNewValue("");

		TaskActivity updateActivity = new TaskActivity();
		updateActivity.setTask(task);
		updateActivity.setActivityDate(new Date());
		updateActivity.setActivityType(Type.UPDATED);
		updateActivity.setFieldUpdates(Arrays.asList(u1));
		updateActivity.setAuthor(taskUser2);

		notificationService.processTaskActivity(Arrays.asList(updateActivity));

		Assert.assertEquals(1, outgoingEmails.size());
		Email mail = outgoingEmails.get(0);
		Assert.assertEquals(profileUser.getEmail(), mail.getTo());
		System.out.println(mail.getBody());
	}
}
