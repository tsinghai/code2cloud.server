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

import com.tasktop.c2c.server.internal.tasks.domain.Keyworddef;
import com.tasktop.c2c.server.tasks.domain.Keyword;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
@Component
public class KeyworddefConverter implements ObjectConverter<Keyword> {

	@Override
	public boolean supportsSource(Class<?> clazz) {
		return com.tasktop.c2c.server.internal.tasks.domain.Keyworddef.class.isAssignableFrom(clazz);
	}

	@Override
	public Class<Keyword> getTargetClass() {
		return Keyword.class;
	}

	@Override
	public void copy(Keyword target, Object sourceObject, DomainConverter converter, DomainConversionContext context) {
		Keyworddef source = (Keyworddef) sourceObject;

		target.setId(source.getId() == null ? null : source.getId().intValue());
		target.setName(source.getName());
		target.setDescription(source.getDescription());
	}
}
