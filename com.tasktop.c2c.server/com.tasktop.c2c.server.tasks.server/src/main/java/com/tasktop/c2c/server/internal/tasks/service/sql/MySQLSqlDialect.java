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

import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MySQLSqlDialect extends SqlDialect {

	@Override
	public List<String> addColumn(String tableName, ColumnDescriptor columnDescriptor) {
		// http://dev.mysql.com/doc/refman/5.5/en/create-table.html
		List<String> statements = new ArrayList<String>();
		statements.add(String.format("ALTER TABLE %s ADD COLUMN %s", quoteIdentifier(tableName),//
				computeColumnSpec(columnDescriptor)));

		return statements;
	}

	private String computeColumnSpec(ColumnDescriptor columnDescriptor) {
		String columnSpec = String.format("%s %s", quoteIdentifier(columnDescriptor.getName()),//
				getColumnType(columnDescriptor.getSqlType(), columnDescriptor.getPrecision(),//
						columnDescriptor.getScale()));
		if (columnDescriptor.getDefaultValue() != null) {
			columnSpec += " DEFAULT ";
			columnSpec += literal(columnDescriptor.getDefaultValue());
		}
		columnSpec += columnDescriptor.isNullable() ? " NULL" : " NOT NULL";
		if (columnDescriptor.isAutoIncrement()) {
			columnSpec += " AUTO_INCREMENT";
		}
		if (columnDescriptor.isPrimaryKey()) {
			columnSpec += " PRIMARY KEY";
		} else if (columnDescriptor.isUnique()) {
			columnSpec += " UNIQUE";
		}
		return columnSpec;
	}

	@Override
	public List<String> createTable(String tableName, ColumnDescriptor... columnDescriptors) {
		// http://dev.mysql.com/doc/refman/5.5/en/create-table.html
		List<String> statements = new ArrayList<String>();
		String statement = String.format("CREATE TABLE %s (\n", quoteIdentifier(tableName));
		int columnIndex = 0;
		for (ColumnDescriptor descriptor : columnDescriptors) {
			if (++columnIndex > 1) {
				statement += ",\n";
			}
			statement += computeColumnSpec(descriptor);
		}
		statement += "\n)";
		statements.add(statement);
		return statements;
	}

	@Override
	public List<String> dropTable(String tableName) {
		return Collections.singletonList(String.format("DROP TABLE IF EXISTS %s CASCADE", quoteIdentifier(tableName)));
	}

	@Override
	public List<String> dropColumn(String tableName, String columnName) {
		return Collections.singletonList(String.format("ALTER TABLE %s DROP COLUMN %s", quoteIdentifier(tableName),
				quoteIdentifier(columnName)));
	}

	public List<String> index(IndexType indexType, String tableName, String... columnNames) {
		String columnNamesPart = "";
		for (String columnName : columnNames) {
			if (columnNamesPart.length() > 0) {
				columnNamesPart += ", ";
			}
			columnNamesPart += quoteIdentifier(columnName);
		}
		String indexPart;
		switch (indexType) {
		case GENERIC:
			indexPart = "INDEX";
			break;
		case UNIQUE:
			indexPart = "UNIQUE INDEX";
			break;
		case FULLTEXT:
			indexPart = "FULLTEXT INDEX";
			break;
		default:
			throw new UnsupportedOperationException(indexType.name());
		}
		return Collections.singletonList(String.format("ALTER TABLE %s ADD %s (%s)", quoteIdentifier(tableName),
				indexPart, columnNamesPart));
	}

	public List<String> foreignKeyConstraint(String tableName, String columnName, String referencedTableName,
			String referencedColumnName) {
		return Collections.singletonList(String.format(
				"ALTER TABLE %s ADD FOREIGN KEY (%s) REFERENCES %s (%s) ON DELETE NO ACTION ON UPDATE NO ACTION",
				quoteIdentifier(tableName), quoteIdentifier(columnName), quoteIdentifier(referencedTableName),
				quoteIdentifier(referencedColumnName)));
	}

	@Override
	protected char getQuoteCharacter() {
		return '`';
	}

	@Override
	public String getColumnType(int sqlType, int precision, int scale) {
		// http://dev.mysql.com/doc/refman/5.5/en/data-types.html
		switch (sqlType) {
		case Types.VARCHAR:
			return String.format("varchar(%s)", precision);
		case Types.BOOLEAN:
			return "boolean";
		case Types.TINYINT:
		case Types.SMALLINT:
			if (precision < 3) {
				return String.format("tinyint(%s)", precision);
			} else if (precision < 5) {
				return String.format("smallint(%s)", precision);
			} else if (precision <= 9) {
				// SMALLINT with precision <= 9 is a hint to use mediumint instead of integer
				return String.format("mediumint(%s)", precision);
			}
		case Types.INTEGER:
			if (precision < 3) {
				return String.format("tinyint(%s)", precision);
			} else if (precision < 5) {
				return String.format("smallint(%s)", precision);
			} else if (precision < 7) {
				return String.format("mediumint(%s)", precision);
			} else if (precision < 10) {
				return String.format("integer(%s)", precision);
			} else {
				return String.format("bigint(%s)", precision);
			}
		case Types.TIMESTAMP:
			return "datetime";
		case Types.DATE:
			return "date";
		case Types.LONGVARCHAR:
		case Types.CLOB:
			return "mediumtext";
		}
		throw new IllegalStateException("Unsupported type: " + sqlType);
	}

	@Override
	public String literal(Object value) {
		if (value instanceof Date) {
			// FIXME: there must be a better way to handle dates. This is lossy
			// http://dev.mysql.com/doc/refman/5.0/en/date-and-time-functions.html#function_date-format
			return String.format("STR_TO_DATE(%s,'%Y %T')",
					literal(new SimpleDateFormat("yyyy HH:mm:ss").format((Date) value)));
		}
		return super.literal(value);
	}

	protected String getStringEscapeCharacter() {
		return "\\";
	}
}
