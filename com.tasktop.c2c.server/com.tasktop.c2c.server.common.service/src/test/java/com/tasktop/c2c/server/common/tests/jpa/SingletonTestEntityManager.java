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
package com.tasktop.c2c.server.common.tests.jpa;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import com.tasktop.c2c.server.common.tests.util.EmbeddedWebContextLoader;


public class SingletonTestEntityManager implements EntityManager {
	private static class TestTransaction implements EntityTransaction {

		private final EntityTransaction delegate;

		public TestTransaction(EntityTransaction transaction) {
			this.delegate = transaction;
		}

		@Override
		public void begin() {
			if (!delegate.isActive()) {
				delegate.begin();
			}
		}

		@Override
		public void commit() {
			if (EmbeddedWebContextLoader.hasApplicationContext()) {
				// no-op if we're running an embedded web container
			} else {
				delegate.commit();
			}
		}

		@Override
		public void rollback() {
			if (EmbeddedWebContextLoader.hasApplicationContext()) {
				// no-op if we're running an embedded web container
			} else {
				delegate.rollback();
			}
		}

		@Override
		public void setRollbackOnly() {
			delegate.setRollbackOnly();
		}

		@Override
		public boolean getRollbackOnly() {
			return delegate.getRollbackOnly();
		}

		@Override
		public boolean isActive() {
			return delegate.isActive();
		}

	}

	private final EntityManager delegate;
	private final SingletonTestEntityManagerFactory entityManagerFactory;
	private boolean open = true;

	public SingletonTestEntityManager(SingletonTestEntityManagerFactory entityManagerFactory,
			EntityManager createEntityManager) {
		this.entityManagerFactory = entityManagerFactory;
		delegate = createEntityManager;
		entityManagerFactory.reference();
	}

	@Override
	public void persist(Object entity) {
		delegate.persist(entity);
	}

	@Override
	public <T> T merge(T entity) {
		return delegate.merge(entity);
	}

	@Override
	public void remove(Object entity) {
		delegate.remove(entity);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		return delegate.find(entityClass, primaryKey);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
		return delegate.find(entityClass, primaryKey, properties);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
		return delegate.find(entityClass, primaryKey, lockMode);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
		return delegate.find(entityClass, primaryKey, lockMode, properties);
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		return delegate.getReference(entityClass, primaryKey);
	}

	@Override
	public void flush() {
		delegate.flush();
	}

	@Override
	public void setFlushMode(FlushModeType flushMode) {
		delegate.setFlushMode(flushMode);
	}

	@Override
	public FlushModeType getFlushMode() {
		return delegate.getFlushMode();
	}

	@Override
	public void lock(Object entity, LockModeType lockMode) {
		delegate.lock(entity, lockMode);
	}

	@Override
	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		delegate.lock(entity, lockMode, properties);
	}

	@Override
	public void refresh(Object entity) {
		delegate.refresh(entity);
	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) {
		delegate.refresh(entity, properties);
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode) {
		delegate.refresh(entity, lockMode);
	}

	@Override
	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		delegate.refresh(entity, lockMode, properties);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public void detach(Object entity) {
		delegate.detach(entity);
	}

	@Override
	public boolean contains(Object entity) {
		return delegate.contains(entity);
	}

	@Override
	public LockModeType getLockMode(Object entity) {
		return delegate.getLockMode(entity);
	}

	@Override
	public void setProperty(String propertyName, Object value) {
		delegate.setProperty(propertyName, value);
	}

	@Override
	public Map<String, Object> getProperties() {
		return delegate.getProperties();
	}

	@Override
	public Query createQuery(String qlString) {
		return delegate.createQuery(qlString);
	}

	@Override
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		return delegate.createQuery(criteriaQuery);
	}

	@Override
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		return delegate.createQuery(qlString, resultClass);
	}

	@Override
	public Query createNamedQuery(String name) {
		return delegate.createNamedQuery(name);
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		return delegate.createNamedQuery(name, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString) {
		return delegate.createNativeQuery(sqlString);
	}

	@Override
	public Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass) {
		return delegate.createNativeQuery(sqlString, resultClass);
	}

	@Override
	public Query createNativeQuery(String sqlString, String resultSetMapping) {
		return delegate.createNativeQuery(sqlString, resultSetMapping);
	}

	@Override
	public void joinTransaction() {
		delegate.joinTransaction();
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		return delegate.unwrap(cls);
	}

	@Override
	public EntityManager getDelegate() {
		return delegate;
	}

	@Override
	public void close() {
		if (open) {
			open = false;
			entityManagerFactory.dereference();
			if (!entityManagerFactory.isReferenced()) {
				if (delegate.getTransaction().isActive()) {
					delegate.getTransaction().rollback();
				}
				delegate.clear();
			}
		}
	}

	@Override
	public boolean isOpen() {
		return open && delegate.isOpen();
	}

	@Override
	public EntityTransaction getTransaction() {
		return new TestTransaction(delegate.getTransaction());
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return delegate.getEntityManagerFactory();
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return delegate.getCriteriaBuilder();
	}

	@Override
	public Metamodel getMetamodel() {
		return delegate.getMetamodel();
	}

}
