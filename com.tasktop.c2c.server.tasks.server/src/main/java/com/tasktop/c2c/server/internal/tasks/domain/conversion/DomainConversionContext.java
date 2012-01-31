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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;


import com.tasktop.c2c.server.internal.tasks.domain.AbstractReferenceEntity;
import com.tasktop.c2c.server.internal.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;

public class DomainConversionContext {

	private final EntityManager entityManager;
	private boolean thin = false;

	private final Map<Object, Object> domainObjectByEntity;
	private final Map<Object, Object> entityByKey;

	public DomainConversionContext(EntityManager entityManager) {
		this.entityManager = entityManager;
		domainObjectByEntity = new HashMap<Object, Object>();
		entityByKey = new HashMap<Object, Object>();
	}

	private DomainConversionContext(EntityManager entityManager, Map<Object, Object> domainObjectByEntity,
			Map<Object, Object> entityByKey, boolean thin) {
		this.entityManager = entityManager;
		this.domainObjectByEntity = domainObjectByEntity;
		this.entityByKey = entityByKey;
		this.thin = thin;
	}

	/**
	 * create a thin context that is a sub-context
	 */
	public DomainConversionContext subcontext() {
		return new DomainConversionContext(entityManager, domainObjectByEntity, entityByKey, true);
	}

	public void put(AbstractReferenceEntity entity) {
		entityByKey.put(computeKey(entity.getClass(), entity.getValue()), entity);
	}

	public void put(com.tasktop.c2c.server.internal.tasks.domain.Milestone entity) {
		entityByKey.put(computeMilestoneKey(entity.getProduct(), entity.getValue()), entity);
	}

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

	@SuppressWarnings("unchecked")
	public void fill(Class<? extends AbstractReferenceEntity> classOfValue) {
		List<AbstractReferenceEntity> entities = entityManager.createQuery(
				"select e from " + classOfValue.getSimpleName() + " e").getResultList();
		for (AbstractReferenceEntity entity : entities) {
			put(entity);
		}
	}

	@SuppressWarnings("unchecked")
	public void fillMilestone() {
		List<com.tasktop.c2c.server.internal.tasks.domain.Milestone> entities = entityManager.createQuery(
				"select e from " + com.tasktop.c2c.server.internal.tasks.domain.Milestone.class.getSimpleName()
						+ " e").getResultList();
		for (com.tasktop.c2c.server.internal.tasks.domain.Milestone entity : entities) {
			put(entity);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends AbstractReferenceEntity> T get(Class<T> entityClass, String value) {
		String key = computeKey(entityClass, value);
		T t = (T) entityByKey.get(key);
		if (t == null && !entityByKey.containsKey(key)) {
			try {
				t = (T) entityManager
						.createQuery("select e from " + entityClass.getSimpleName() + " e where e.value = :v")
						.setParameter("v", value).getSingleResult();
			} catch (NoResultException e) {
				// expected
			}
			entityByKey.put(key, t);
		}
		return t;
	}

	protected <T extends AbstractReferenceEntity> String computeKey(Class<T> entityClass, String value) {
		return entityClass.getSimpleName() + ':' + value;
	}

	public TaskStatus getTaskStatus(String value) {
		return domainValue(get(com.tasktop.c2c.server.internal.tasks.domain.TaskStatus.class, value));
	}

	private TaskStatus domainValue(com.tasktop.c2c.server.internal.tasks.domain.TaskStatus entity) {
		if (entity == null) {
			return null;
		}
		TaskStatus object = (TaskStatus) domainObjectByEntity.get(entity);
		if (object == null) {
			object = TaskDomain.createDomain(entity);
			domainObjectByEntity.put(entity, object);
		}
		return object;
	}

	public TaskSeverity getTaskSeverity(String value) {
		return domainValue(get(com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity.class, value));
	}

	private TaskSeverity domainValue(com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity entity) {
		if (entity == null) {
			return null;
		}
		TaskSeverity object = (TaskSeverity) domainObjectByEntity.get(entity);
		if (object == null) {
			object = TaskDomain.createDomain(entity);
			domainObjectByEntity.put(entity, object);
		}
		return object;
	}

	public TaskResolution getTaskResolution(String value) {
		return domainValue(get(com.tasktop.c2c.server.internal.tasks.domain.Resolution.class, value));
	}

	private TaskResolution domainValue(com.tasktop.c2c.server.internal.tasks.domain.Resolution entity) {
		if (entity == null) {
			return null;
		}
		TaskResolution object = (TaskResolution) domainObjectByEntity.get(entity);
		if (object == null) {
			object = TaskDomain.createDomain(entity);
			domainObjectByEntity.put(entity, object);
		}
		return object;
	}

	public Priority getPriority(String value) {
		return domainValue(get(com.tasktop.c2c.server.internal.tasks.domain.Priority.class, value));
	}

	private Priority domainValue(com.tasktop.c2c.server.internal.tasks.domain.Priority entity) {
		if (entity == null) {
			return null;
		}
		Priority object = (Priority) domainObjectByEntity.get(entity);
		if (object == null) {
			object = TaskDomain.createDomain(entity);
			domainObjectByEntity.put(entity, object);
		}
		return object;
	}

	public Milestone getMilestone(Product product, String value) {
		String key = computeMilestoneKey(product, value);
		com.tasktop.c2c.server.internal.tasks.domain.Milestone milestone = (com.tasktop.c2c.server.internal.tasks.domain.Milestone) entityByKey
				.get(key);
		if (milestone == null && !entityByKey.containsKey(key)) {
			try {
				milestone = (com.tasktop.c2c.server.internal.tasks.domain.Milestone) entityManager
						.createQuery(
								"select e from "
										+ com.tasktop.c2c.server.internal.tasks.domain.Milestone.class
												.getSimpleName() + " e where e.value = :v and e.product = :p")
						.setParameter("v", value).setParameter("p", product).getSingleResult();
			} catch (NoResultException e) {
				// expected
			}
			entityByKey.put(key, milestone);
		}
		return domainValue(milestone);
	}

	protected String computeMilestoneKey(Product product, String value) {
		return "Milestone:" + product.getId() + ":" + value;
	}

	private Milestone domainValue(com.tasktop.c2c.server.internal.tasks.domain.Milestone entity) {
		if (entity == null) {
			return null;
		}
		Milestone object = (Milestone) domainObjectByEntity.get(entity);
		if (object == null) {
			object = TaskDomain.createDomain(entity);
			domainObjectByEntity.put(entity, object);
		}
		return object;
	}

}
