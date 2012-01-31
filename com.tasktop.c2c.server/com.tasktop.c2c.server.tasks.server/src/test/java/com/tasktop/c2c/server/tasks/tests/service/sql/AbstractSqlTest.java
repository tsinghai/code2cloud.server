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
package com.tasktop.c2c.server.tasks.tests.service.sql;

import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.tasktop.c2c.server.internal.tasks.service.sql.SqlDialect;


/**
 * subclasses must set the sqlDialect
 */
public abstract class AbstractSqlTest {

	@PersistenceContext(unitName = "tasksDomain")
	protected EntityManager entityManager;

	protected SqlDialect sqlDialect;

	public void assertTableDoesNotExist(String tableName) {
		try {
			entityManager.createNativeQuery("select * from " + sqlDialect.quoteIdentifier(tableName)).getResultList()
					.size();
			fail("Table " + tableName + " exists");
		} catch (Exception e) {
			// expected
		}
	}

	public void assertTableExists(String tableName) {
		try {
			entityManager.createNativeQuery("select * from " + sqlDialect.quoteIdentifier(tableName)).getResultList()
					.size();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Table " + tableName + " does not exist (" + e.getMessage() + ")");
		}
	}

	public void assertColumnDoesNotExist(String tableName, String columnName) {
		try {
			entityManager
					.createNativeQuery(
							String.format("select %s from %s", sqlDialect.quoteIdentifier(columnName),
									sqlDialect.quoteIdentifier(tableName))).getResultList().size();
			fail("Column " + tableName + '.' + columnName + " exists");
		} catch (Exception e) {
			// expected
		}
	}

	public void assertColumnExists(String tableName, String columnName) {
		try {
			entityManager
					.createNativeQuery(
							String.format("select %s from %s", sqlDialect.quoteIdentifier(columnName),
									sqlDialect.quoteIdentifier(tableName))).getResultList().size();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Column " + tableName + '.' + columnName + " does not exist (" + e.getMessage() + ")");
		}
	}

	protected void executeUpdate(List<String> statements) {
		for (String statement : statements) {
			executeUpdate(statement);
		}
	}

	protected void executeUpdate(String statement) {
		entityManager.createNativeQuery(statement).executeUpdate();
	}

}
