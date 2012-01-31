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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

public class DomainConversionContext {

	@SuppressWarnings("unused")
	private final EntityManager entityManager;
	private boolean thin = false;

	public DomainConversionContext(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	private Map<Object, Object> domainObjectByEntity = new HashMap<Object, Object>();

	public void map(Object entity, Object domainObject) {
		domainObjectByEntity.put(entity, domainObject);
	}

	public Object getDomainObject(Object entity) {
		return domainObjectByEntity.get(entity);
	}

	public boolean isThin() {
		return thin;
	}

	public void setThin(boolean thin) {
		this.thin = thin;
	}

}
