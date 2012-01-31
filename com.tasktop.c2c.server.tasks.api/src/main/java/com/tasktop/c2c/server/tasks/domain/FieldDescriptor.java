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
package com.tasktop.c2c.server.tasks.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

@SuppressWarnings("serial")
public class FieldDescriptor extends AbstractDomainObject implements Serializable {
	private String name;
	private FieldType fieldType;
	private String description;
	private boolean availableForNewTasks;
	private boolean obsolete;

	private List<CustomFieldValue> values;

	public FieldDescriptor() {
	}

	public FieldDescriptor(String name) {
		this.name = name;
	}

	public FieldDescriptor(String name, String description, FieldType fieldType) {
		this.name = name;
		this.description = description;
		this.fieldType = fieldType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isAvailableForNewTasks() {
		return availableForNewTasks;
	}

	public void setAvailableForNewTasks(boolean availableForNewTasks) {
		this.availableForNewTasks = availableForNewTasks;
	}

	/**
	 * When {@link #getFieldType() type} of {@link FieldType#SINGLE_SELECT} or {@link FieldType#MULTI_SELECT}, the list
	 * of available choices.
	 */
	public List<CustomFieldValue> getValues() {
		return values;
	}

	public void setValues(List<CustomFieldValue> values) {
		this.values = values;
	}

	@JsonIgnore
	public void setValueStrings(List<String> values) {
		if (values == null) {
			values = null;
			return;
		}
		List<CustomFieldValue> result = new ArrayList<CustomFieldValue>(values.size());
		short i = 0;
		for (String value : values) {
			CustomFieldValue cfValue = new CustomFieldValue();
			cfValue.setValue(value);
			cfValue.setIsActive(true);
			cfValue.setSortkey(i++);
			result.add(cfValue);
		}
		setValues(result);
	}

	@JsonIgnore
	public List<String> getValueStrings() {
		if (values == null) {
			return null;
		}
		List<String> result = new ArrayList<String>(values.size());
		for (CustomFieldValue value : values) {
			result.add(value.getValue());
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * equality based on name
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldDescriptor other = (FieldDescriptor) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public boolean isObsolete() {
		return obsolete;
	}

	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

}
