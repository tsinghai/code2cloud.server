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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import com.tasktop.c2c.server.internal.wiki.server.domain.MediaTypes;
import com.tasktop.c2c.server.internal.wiki.server.domain.validation.AttachmentValidator;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.PageHandle;

public class AttachmentValidatorTest {
	private AttachmentValidator attachmentValidator;
	private Attachment attachment;
	private Errors errors;

	@Before
	public void before() {
		attachmentValidator = new AttachmentValidator(new MediaTypes());
		attachment = new Attachment();
		attachment.setPage(new PageHandle(1));
		attachment.setContent("111".getBytes());
		clearErrors();
	}

	private void clearErrors() {
		errors = new BeanPropertyBindingResult(attachment, "attachment");
	}

	@Test
	public void canValidate() {
		assertTrue(attachmentValidator.supports(Attachment.class));
		assertFalse(attachmentValidator
				.supports(com.tasktop.c2c.server.internal.wiki.server.domain.Attachment.class));
	}

	@Test
	public void acceptsFilenameWithSpaces() {
		attachment.setName("Name with spaces.txt");
		attachmentValidator.validate(attachment, errors);
		assertFalse(errors.hasErrors());
	}
}
