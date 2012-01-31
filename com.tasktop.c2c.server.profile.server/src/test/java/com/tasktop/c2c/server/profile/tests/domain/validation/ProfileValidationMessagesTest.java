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
package com.tasktop.c2c.server.profile.tests.domain.validation;

import java.util.Arrays;

import com.tasktop.c2c.server.common.tests.util.AbstractValidationMessageTest;

public class ProfileValidationMessagesTest extends AbstractValidationMessageTest {

	public ProfileValidationMessagesTest() {
		super.setValidatorJavaFilenames(Arrays.asList(
				"src/main/java/com/tasktop/c2c/server/profile/domain/validation/ProjectValidator.java",
				"src/main/java/com/tasktop/c2c/server/profile/domain/validation/ProfilePasswordValidator.java",
				"src/main/java/com/tasktop/c2c/server/profile/domain/validation/ProfileValidator.java"));
		super.setMessageBundleFilenames(Arrays.asList("src/main/resources/messages.properties"));
	}

}
