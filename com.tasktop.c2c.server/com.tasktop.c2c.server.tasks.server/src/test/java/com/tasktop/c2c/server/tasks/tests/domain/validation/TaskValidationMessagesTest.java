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

import java.util.Arrays;

import org.junit.Ignore;

import com.tasktop.c2c.server.common.tests.util.AbstractValidationMessageTest;

@Ignore
// Broken
public class TaskValidationMessagesTest extends AbstractValidationMessageTest {

	public TaskValidationMessagesTest() {
		super.setValidatorJavaFilenames(Arrays
				.asList("../com.tasktop.c2c.server.tasks.api/src/main/java/com/tasktop/c2c/server/tasks/domain/validation/AttachmentValidator.java",
						"../com.tasktop.c2c.server.tasks.api/src/main/java/com/tasktop/c2c/server/tasks/domain/validation/CommentValidator.java",
						"../com.tasktop.c2c.server.tasks.api/src/main/java/com/tasktop/c2c/server/tasks/domain/validation/ComponentValidator.java",
						"../com.tasktop.c2c.server.tasks.api/src/main/java/com/tasktop/c2c/server/tasks/domain/validation/KeywordValidator.java",
						"../com.tasktop.c2c.server.tasks.api/src/main/java/com/tasktop/c2c/server/tasks/domain/validation/MilestoneValidator.java",
						"../com.tasktop.c2c.server.tasks.api/src/main/java/com/tasktop/c2c/server/tasks/domain/validation/SavedTaskQueryValidator.java",
						"../com.tasktop.c2c.server.tasks.api/src/main/java/com/tasktop/c2c/server/tasks/domain/validation/TaskValidator.java",
						"../com.tasktop.c2c.server.tasks.api/src/main/java/com/tasktop/c2c/server/tasks/domain/validation/WorkLogValidator.java"));
		super.setMessageBundleFilenames(Arrays.asList("src/main/resources/messages.properties"));
	}

}
