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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

public class SingletonTestEntityManagerFactory implements EntityManagerFactory {

	private EntityManagerFactory delegate;
	private EntityManager entityManager;
	private int references;

	@Override
	public void close() {
		if (delegate != null) {
			delegate.close();
			delegate = null;
		}
	}

	@Override
	public EntityManager createEntityManager() {
		synchronized (this) {
			if (entityManager == null) {
				entityManager = delegate.createEntityManager();
			}
			return new SingletonTestEntityManager(this, entityManager);
		}
	}

	@Override
	public EntityManager createEntityManager(Map arg0) {
		getLog().warn("attempt to get EntityManager with arguments");
		return createEntityManager();
	}

	private Logger getLog() {
		return LoggerFactory.getLogger(SingletonTestEntityManagerFactory.class.getName());
	}

	@Override
	public Cache getCache() {
		return delegate.getCache();
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return delegate.getCriteriaBuilder();
	}

	@Override
	public Metamodel getMetamodel() {
		return delegate.getMetamodel();
	}

	@Override
	public PersistenceUnitUtil getPersistenceUnitUtil() {
		return delegate.getPersistenceUnitUtil();
	}

	@Override
	public Map<String, Object> getProperties() {
		return delegate.getProperties();
	}

	@Override
	public boolean isOpen() {
		return delegate != null && delegate.isOpen();
	}

	public EntityManagerFactory getDelegate() {
		return delegate;
	}

	public void setDelegate(EntityManagerFactory delegate) {
		this.delegate = delegate;
	}

	public synchronized void reference() {
		++references;
	}

	public synchronized void dereference() {
		if (--references < 0) {
			throw new IllegalStateException();
		}
	}

	public synchronized boolean isReferenced() {
		return references > 0;
	}
}
