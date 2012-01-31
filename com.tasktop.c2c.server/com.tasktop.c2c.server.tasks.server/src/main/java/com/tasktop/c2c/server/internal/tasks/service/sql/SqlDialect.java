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
package com.tasktop.c2c.server.internal.tasks.service.sql;

import java.util.List;

public abstract class SqlDialect {
	private boolean quoteIdentifiers = true;

	/**
	 * Add a column to the given table.
	 * 
	 * @param tableName
	 *            the name of the table to alter
	 * 
	 * @return a list of SQL statements to perform.
	 */
	public abstract List<String> addColumn(String tableName, ColumnDescriptor columnDescriptor);

	/**
	 * provide the SQL statements required to create the given table
	 * 
	 * @return a list of SQL statements to perform.
	 */
	public abstract List<String> createTable(String tableName, ColumnDescriptor... columnDescriptor);

	/**
	 * provide the SQL statements required to create an index on the given table
	 * 
	 * @param indexType
	 *            the type of index to create
	 * @param tableName
	 *            the table name
	 * @param columnNames
	 *            the column names to be included in the index
	 * 
	 * @return a list of SQL statements to perform.
	 */
	public abstract List<String> index(IndexType indexType, String tableName, String... columnNames);

	/**
	 * provide the SQL statements required to create a foreign key on the given table
	 * 
	 * @param tableName
	 *            the table for which the foreign key should be created (ie: detail in master detail)
	 * @param columnName
	 *            the column that is referencing a primary key
	 * @param referencedTableName
	 *            the table that is referenced (ie: master in master detail)
	 * @param referencedColumnName
	 *            the primary key column that is referenced
	 * @return
	 */
	public abstract List<String> foreignKeyConstraint(String tableName, String columnName, String referencedTableName,
			String referencedColumnName);

	/**
	 * get the SQL definition of the column
	 * 
	 * @param sqlType
	 *            the SQL type, must be one of {@link java.sql.Types}
	 * @param precision
	 *            the column
	 * @param scale
	 *            the scale, or 0 if a scale does not apply to this type
	 * @return
	 */
	public abstract String getColumnType(int sqlType, int precision, int scale);

	public boolean isQuoteIdentifiers() {
		return quoteIdentifiers;
	}

	public void setQuoteIdentifiers(boolean quoteIdentifiers) {
		this.quoteIdentifiers = quoteIdentifiers;
	}

	public String quoteIdentifier(String identifier) {
		if (quoteIdentifiers) {
			char quoteCharacter = getQuoteCharacter();
			return quoteCharacter + identifier + quoteCharacter;
		}
		return identifier;
	}

	protected abstract char getQuoteCharacter();

	/**
	 * provide the given value as a SQL literal suitable for including in a SQL statement
	 * 
	 * @param value
	 *            the value or null
	 * @return the SQL literal
	 */
	public String literal(Object value) {
		if (value == null) {
			return "NULL";
		}
		if (value instanceof Number) {
			return value.toString();
		} else if (value instanceof Boolean) {
			return value.toString();
		}
		value = value.toString().replace("'", getStringEscapeCharacter() + "'");
		return '\'' + value.toString() + '\'';
	}

	protected String getStringEscapeCharacter() {
		return "'";
	}

	public abstract List<String> dropTable(String tableName);

	public abstract List<String> dropColumn(String tableName, String columnName);

}
