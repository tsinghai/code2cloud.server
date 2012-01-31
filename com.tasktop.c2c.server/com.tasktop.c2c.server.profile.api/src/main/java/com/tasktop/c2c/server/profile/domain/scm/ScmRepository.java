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
package com.tasktop.c2c.server.profile.domain.scm;


import com.tasktop.c2c.server.cloud.domain.ScmLocation;
import com.tasktop.c2c.server.cloud.domain.ScmType;
import com.tasktop.c2c.server.profile.domain.project.AbstractEntity;
import com.tasktop.c2c.server.profile.domain.project.Project;

@SuppressWarnings("serial")
public class ScmRepository extends AbstractEntity {

	private String name; // CUrrently only used on create
	private Project project;
	private String url;
	private ScmType type;
	private ScmLocation scmLocation;
	private String alternateUrl;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String newUrl) {
		this.url = newUrl;
	}

	public ScmType getType() {
		return type;
	}

	public void setType(ScmType newType) {
		this.type = newType;
	}

	public ScmLocation getScmLocation() {
		return scmLocation;
	}

	public void setScmLocation(ScmLocation scmLocation) {
		this.scmLocation = scmLocation;
	}

	public void setAlternateUrl(String alternateUrl) {
		this.alternateUrl = alternateUrl;
	}

	/**
	 * some repositories have multiple URLs
	 * 
	 * @return the alternate URL, or null if there is none
	 */
	public String getAlternateUrl() {
		return alternateUrl;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
