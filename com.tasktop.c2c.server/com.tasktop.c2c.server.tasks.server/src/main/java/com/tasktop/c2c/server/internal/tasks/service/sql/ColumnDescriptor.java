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

public class ColumnDescriptor {
	private String name;
	private int sqlType;
	private int precision;
	private int scale;
	private boolean nullable;
	private boolean primaryKey;
	private boolean autoIncrement;
	private boolean unique;
	private boolean indexed;
	private Object defaultValue;

	public ColumnDescriptor() {
	}

	public ColumnDescriptor(String name, int sqlType, int precision, int scale, boolean nullable, Object defaultValue) {
		this.name = name;
		this.sqlType = sqlType;
		this.precision = precision;
		this.scale = scale;
		this.nullable = nullable;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isIndexed() {
		return indexed;
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	public ColumnDescriptor indexed(boolean indexed) {
		this.indexed = indexed;
		return this;
	}

	public ColumnDescriptor primaryKey(boolean b) {
		this.primaryKey = b;
		return this;
	}

	public ColumnDescriptor unique(boolean b) {
		this.unique = b;
		return this;
	}

	public ColumnDescriptor autoIncrement(boolean b) {
		this.autoIncrement = b;
		return this;
	}

}
