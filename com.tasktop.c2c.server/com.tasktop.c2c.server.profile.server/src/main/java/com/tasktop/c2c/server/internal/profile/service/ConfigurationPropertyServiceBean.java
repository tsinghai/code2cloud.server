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
package com.tasktop.c2c.server.internal.profile.service;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Service;


import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.profile.domain.internal.ConfigurationProperty;
import com.tasktop.c2c.server.profile.service.ConfigurationPropertyService;

@Service("configurationPropertyService")
public class ConfigurationPropertyServiceBean extends AbstractJpaServiceBean implements ConfigurationPropertyService {

	@Override
	public String getConfigurationValue(String configurationKey) {
		ConfigurationProperty prop = getConfigurationProperty(configurationKey);
		if (prop == null) {
			return null;
		}
		return prop.getValue();
	}

	private ConfigurationProperty getConfigurationProperty(String name) {
		return getEntityByField("name", name, ConfigurationProperty.class);
	}

	private <T> T getEntityByField(String fieldName, String fieldValue, Class<T> entityClass) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = criteriaBuilder.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		query.select(root).where(criteriaBuilder.equal(root.get(fieldName), fieldValue));
		try {
			T retObj = entityManager.createQuery(query).getSingleResult();
			return retObj;
		} catch (NoResultException e) {
			return null;
		}
	}

}
