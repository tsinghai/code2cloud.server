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
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.domain.Person;

@Component
public class PageConverter implements ObjectConverter<Page> {

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
		return com.tasktop.c2c.server.internal.wiki.server.domain.Page.class.isAssignableFrom(sourceClass);
	}

	@Override
	public Class<Page> getTargetClass() {
		return Page.class;
	}

	@Override
	public void copy(Page target, Object sourceObject, DomainConverter converter, DomainConversionContext context) {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = (com.tasktop.c2c.server.internal.wiki.server.domain.Page) sourceObject;

		target.setCreationDate(page.getCreationDate());
		target.setId(page.getId() == null ? null : page.getId().intValue());
		target.setLastAuthor((Person) converter.convert(page.getLastAuthor(), context));
		target.setModificationDate(page.getModificationDate());
		target.setOriginalAuthor((Person) converter.convert(page.getOriginalAuthor(), context));
		target.setPath(page.getPath());
		target.setUrl(serviceConfiguration.computeWebUrlForPage(page.getPath()));
		target.setAttachmentsUrl(serviceConfiguration.computeAttachmentsUrlForPage(page.getId()));
		target.setEditAccess(page.getEditAccess());
		target.setDeleteAccess(page.getDeleteAccess());
		target.setDeleted(page.getDeleted());
		if (!context.isThin()) {
			target.setContent(page.getLastPageContent().getContent());
		}
	}

}
