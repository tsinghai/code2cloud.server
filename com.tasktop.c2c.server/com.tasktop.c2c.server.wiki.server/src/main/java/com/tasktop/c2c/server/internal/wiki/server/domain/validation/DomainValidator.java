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
package com.tasktop.c2c.server.internal.wiki.server.domain.validation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.tasktop.c2c.server.common.service.validation.AbstractDomainValidator;
import com.tasktop.c2c.server.internal.wiki.server.domain.MediaTypes;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.Page;


public class DomainValidator extends AbstractDomainValidator implements InitializingBean {
	@Autowired
	private MediaTypes mediaTypes;

	@Override
	public void afterPropertiesSet() throws Exception {
		registerValidator(Page.class, new PageValidator());
		registerValidator(Attachment.class, new AttachmentValidator(mediaTypes));
	}
}
