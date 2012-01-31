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
package com.tasktop.c2c.server.tasks.tests.domain.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tasktop.c2c.server.common.tests.util.AbstractValidatorTest;
import com.tasktop.c2c.server.common.tests.util.ValidationAssert;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockTaskFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
// FIXME this starts the DB and imports the schema. should not be needed.
public class TaskValidatorTest extends AbstractValidatorTest<Task> {

	@Override
	protected Task createMock() {
		return MockTaskFactory.createDO();
	}

	@Test
	public void testMockIsValid() {
		validator.validate(mock, result);
		Assert.assertFalse(result.toString(), result.hasErrors());
	}

	@Test
	public void testShortDescriptionNull() {
		mock.setShortDescription(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required.shortDescription");
	}

	@Test
	public void testShortDescriptionEmpty() {
		mock.setShortDescription("");
		validator.validate(mock, result);
		assertHaveValidationError("field.required.shortDescription");
	}

	@Test
	public void testShortDescriptionWhitespace() {
		mock.setShortDescription("    ");
		validator.validate(mock, result);
		assertHaveValidationError("field.required.shortDescription");
	}

	@Test
	public void testNullIteration() {
		mock.setIteration(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required.iteration");
	}

	@Test
	public void testNullType() {
		mock.setTaskType(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.required.taskType");
	}

	@Test
	public void testNegativeRemainingTime() {
		mock.setRemainingTime(new BigDecimal("-1"));
		validator.validate(mock, result);
		assertHaveValidationError("nonNegative.remainingTime");
	}

	@Test
	public void testCommentTextEmpty() {
		Comment myComment = new Comment();
		myComment.setAuthor(new TaskUserProfile()); // REQUIRED
		myComment.getAuthor().setRealname("name");
		myComment.setCreationDate(new Date());
		myComment.setCommentText("");
		mock.setComments(Arrays.asList(myComment));

		validator.validate(mock, result);
		assertHaveValidationError("field.required.commentText");
	}

	@Test
	public void testCommentTextWhitespace() {
		Comment myComment = new Comment();
		myComment.setAuthor(new TaskUserProfile()); // REQUIRED
		myComment.getAuthor().setRealname("name");
		myComment.setCreationDate(new Date());
		myComment.setCommentText("      ");
		mock.setComments(Arrays.asList(myComment));

		validator.validate(mock, result);
		assertHaveValidationError("field.required.commentText");
	}

	@Test
	public void testResolutionRequiredForResolvedStatus() {
		mock.setStatus(new TaskStatus());
		mock.getStatus().setValue("RESOLVED");
		mock.setResolution(null);

		validator.validate(mock, result);
		assertHaveValidationError("field.required.resolution");

		clearErrors();

		mock.setResolution(new TaskResolution());
		mock.getResolution().setValue("FIXED");

		validator.validate(mock, result);
		ValidationAssert.assertHaveNoValidationError(result, "field.required.resolution");

		clearErrors();

		mock.setStatus(new TaskStatus());
		mock.getStatus().setValue("ASSIGNED");
		mock.getStatus().setOpen(true);

		validator.validate(mock, result);
		assertHaveValidationError("field.prohibited.resolution");

		clearErrors();

		mock.setResolution(null);

		validator.validate(mock, result);
		ValidationAssert.assertHaveNoValidationError(result, "field.prohibited.resolution");
	}

	@Test
	public void testAssigneeLoginNameEmpty() {
		mock.getAssignee().setLoginName(null);
		validator.validate(mock, result);
		assertHaveValidationError("field.empty");
	}

	@Test
	public void testWatcherLoginNameEmpty() {
		List<TaskUserProfile> watchers = new ArrayList<TaskUserProfile>();
		watchers.add(new TaskUserProfile());
		mock.setWatchers(watchers);
		validator.validate(mock, result);
		assertHaveValidationError("field.empty");
	}
}
