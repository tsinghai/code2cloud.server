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
package com.tasktop.c2c.server.wiki.server.tests.domain;

import java.util.Arrays;


import com.tasktop.c2c.server.common.tests.util.AbstractValidationMessageTest;
import com.tasktop.c2c.server.internal.wiki.server.domain.validation.DomainValidator;


public class WikiValidationMessagesTest extends AbstractValidationMessageTest {

	public WikiValidationMessagesTest() throws Exception {
		DomainValidator domainValidator = new DomainValidator();
		domainValidator.afterPropertiesSet();
		super.computeValidatorJavaFilenames("src/main/java", domainValidator.getValidatorByType().values());
		super.setMessageBundleFilenames(Arrays.asList("src/main/resources/WikiMessages.properties"));
	}

}
