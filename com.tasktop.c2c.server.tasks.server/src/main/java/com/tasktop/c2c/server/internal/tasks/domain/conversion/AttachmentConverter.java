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
package com.tasktop.c2c.server.internal.tasks.domain.conversion;

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;


@Component
public class AttachmentConverter implements ObjectConverter<Attachment> {

	@Override
	public boolean supportsSource(Class<?> clazz) {
		return com.tasktop.c2c.server.internal.tasks.domain.Attachment.class.isAssignableFrom(clazz);
	}

	@Override
	public void copy(Attachment target, Object internalObject, DomainConverter converter,
			DomainConversionContext context) {
		com.tasktop.c2c.server.internal.tasks.domain.Attachment source = (com.tasktop.c2c.server.internal.tasks.domain.Attachment) internalObject;

		target.setCreationDate(source.getCreationTs());
		target.setDescription(source.getDescription());
		target.setFilename(source.getFilename());
		target.setId(source.getId());
		target.setMimeType(source.getMimetype());
		target.setSubmitter((TaskUserProfile) converter.convert(source.getProfiles(), context));

	}

	@Override
	public Class<Attachment> getTargetClass() {
		return Attachment.class;
	}

}
