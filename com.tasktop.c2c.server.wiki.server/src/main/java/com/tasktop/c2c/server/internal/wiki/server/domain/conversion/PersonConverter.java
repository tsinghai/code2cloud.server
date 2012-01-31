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

import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.wiki.domain.Person;


@Component
public class PersonConverter implements ObjectConverter<Person> {

	@Override
	public boolean supportsSource(Class<?> sourceClass) {
		return com.tasktop.c2c.server.internal.wiki.server.domain.Person.class.isAssignableFrom(sourceClass);
	}

	@Override
	public Class<Person> getTargetClass() {
		return Person.class;
	}

	@Override
	public void copy(Person target, Object sourceObject, DomainConverter converter, DomainConversionContext context) {
		com.tasktop.c2c.server.internal.wiki.server.domain.Person person = (com.tasktop.c2c.server.internal.wiki.server.domain.Person) sourceObject;

		target.setId(person.getId() == null ? null : person.getId().intValue());
		target.setLoginName(person.getIdentity());
		target.setName(person.getName());
	}

}
