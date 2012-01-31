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
package com.tasktop.c2c.server.internal.wiki.server.domain.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.internal.wiki.server.WikiServiceConfiguration;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.PageHandle;
import com.tasktop.c2c.server.wiki.domain.Person;


@Component
public class AttachmentConverter implements ObjectConverter<Attachment> {

	@Autowired
	private WikiServiceConfiguration serviceConfiguration;

	public WikiServiceConfiguration getServiceConfiguration() {
		return serviceConfiguration;
	}

	public void setServiceConfiguration(WikiServiceConfiguration serviceConfiguration) {
		this.serviceConfiguration = serviceConfiguration;
	}

	@Override
	public boolean supportsSource(Class<?> sourceClass) {
		return com.tasktop.c2c.server.internal.wiki.server.domain.Attachment.class.isAssignableFrom(sourceClass);
	}

	@Override
	public Class<Attachment> getTargetClass() {
		return Attachment.class;
	}

	@Override
	public void copy(Attachment target, Object sourceObject, DomainConverter converter, DomainConversionContext context) {
		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment = (com.tasktop.c2c.server.internal.wiki.server.domain.Attachment) sourceObject;

		target.setCreationDate(attachment.getCreationDate());
		target.setId(attachment.getId() == null ? null : attachment.getId().intValue());
		target.setLastAuthor((Person) converter.convert(attachment.getLastAuthor(), context));
		target.setModificationDate(attachment.getModificationDate());
		target.setOriginalAuthor((Person) converter.convert(attachment.getOriginalAuthor(), context));
		target.setName(attachment.getName());
		target.setUrl(serviceConfiguration.computeWebUrlForAttachment(attachment.getPage().getId(),
				attachment.getName()));
		target.setMimeType(attachment.getMimeType());
		target.setSize(attachment.getLastAttachmentContent().getSize());
		target.setPage(new PageHandle(attachment.getPage().getId().intValue()));
		target.setEtag(Long.toHexString(attachment.getModificationDate().getTime()) + ':'
				+ Integer.toHexString(target.getSize()));
		if (!context.isThin()) {
			target.setContent(attachment.getLastAttachmentContent().getContent());
		}
	}

}
