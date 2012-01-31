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
package com.tasktop.c2c.server.profile.domain.internal;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ConfigurationProperty extends BaseEntity {

	public static final String MAXNUM_PROJECTS_NAME = "system.project.maxnum";

	private String name;
	private String value;

	@Column(nullable = false, length = 255)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(nullable = false, length = 4096)
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
