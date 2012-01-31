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

import java.sql.Types;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.internal.tasks.service.sql.ColumnDescriptor;
import com.tasktop.c2c.server.internal.tasks.service.sql.MySQLSqlDialect;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
@Transactional
public class MySQLSqlDialectTest extends AbstractSqlTest {

	private String tableName;

	AbstractSqlTest sqlAssert = this;

	@Before
	public void before() {
		sqlDialect = new MySQLSqlDialect();
		tableName = String.format("test_" + System.currentTimeMillis());
		dropTable();
	}

	@After
	public void after() {
		dropTable();
	}

	protected void dropTable() {
		executeUpdate("DROP TABLE IF EXISTS `" + tableName + "` CASCADE");
	}

	@Test
	public void testCreateTableAddColumns() {
		ColumnDescriptor[] columnDescriptors = new ColumnDescriptor[] {
				new ColumnDescriptor("id", Types.INTEGER, 8, 0, false, null).autoIncrement(true).primaryKey(true),
				new ColumnDescriptor("testIntWithDefault", Types.INTEGER, 8, 0, false, 2),
				new ColumnDescriptor("testStringWithDefault", Types.VARCHAR, 32, 0, false, "one two"),
				new ColumnDescriptor("test_date", Types.TIMESTAMP, 0, 0, false, null), };
		List<String> statements = sqlDialect.createTable(tableName, columnDescriptors);
		System.out.println(statements);
		executeUpdate(statements);
		assertTableExists(tableName);
		String newColumnName = "added";
		statements = sqlDialect.addColumn(tableName, new ColumnDescriptor(newColumnName, Types.LONGVARCHAR, 32535, 0,
				true, null));
		System.out.println(statements);
		executeUpdate(statements);
		assertColumnExists(tableName, newColumnName);
	}

	@Test
	public void testDropTable() {
		executeUpdate("create table " + sqlDialect.quoteIdentifier(tableName) + " ( "
				+ sqlDialect.quoteIdentifier("id") + " smallint auto_increment primary key )");
		assertTableExists(tableName);
		List<String> statements = sqlDialect.dropTable(tableName);
		System.out.println(statements);
		executeUpdate(statements);
		assertTableDoesNotExist(tableName);
	}

	@Test
	public void testDropColumn() {
		String columnName = "test";
		ColumnDescriptor[] columnDescriptors = new ColumnDescriptor[] {
				new ColumnDescriptor("id", Types.INTEGER, 8, 0, false, null).autoIncrement(true).primaryKey(true),
				new ColumnDescriptor(columnName, Types.INTEGER, 8, 0, true, null) };
		List<String> statements = sqlDialect.createTable(tableName, columnDescriptors);
		System.out.println(statements);
		executeUpdate(statements);
		assertTableExists(tableName);
		statements = sqlDialect.dropColumn(tableName, columnName);
		System.out.println(statements);
		executeUpdate(statements);
		assertColumnDoesNotExist(tableName, columnName);
	}

}
