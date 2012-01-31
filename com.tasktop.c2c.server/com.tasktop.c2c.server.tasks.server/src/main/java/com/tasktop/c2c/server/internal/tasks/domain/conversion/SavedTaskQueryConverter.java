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

import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;

@Component
public class SavedTaskQueryConverter implements ObjectConverter<SavedTaskQuery> {

	@Override
	public boolean supportsSource(Class<?> clazz) {
		return com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery.class.isAssignableFrom(clazz);
	}

	@Override
	public void copy(SavedTaskQuery target, Object internalObject, DomainConverter converter,
			DomainConversionContext context) {
		com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery source = (com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery) internalObject;

		target.setId(source.getId() == null ? null : source.getId().intValue());
		target.setName(source.getName());
		target.setQueryString(source.getQueryString());

		if (source.getSortOrder() != null && source.getSortField() != null) {
			target.setDefaultSort(new SortInfo(source.getSortField(), source.getSortOrder()));
		}
	}

	@Override
	public Class<SavedTaskQuery> getTargetClass() {
		return SavedTaskQuery.class;
	}

	public static SavedTaskQuery copy(com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery source) {
		SavedTaskQuery target = new SavedTaskQuery();
		new SavedTaskQueryConverter().copy(target, source, null, null);
		return target;
	}

}
