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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.tasktop.c2c.server.cloud.domain.ScmLocation;
import com.tasktop.c2c.server.cloud.domain.ScmType;


@Entity
@Table(name = "SCMREPOSITORY")
public class ScmRepository extends BaseEntity {

	private Project project;
	private String url;
	private ScmType type;
	private ScmLocation scmLocation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Column(name = "url", nullable = false, length = 4096)
	public String getUrl() {
		return url;
	}

	/**
	 * Get an SSH URI path for this SCM repository for repositories that are hosted at Code2Cloud. The URI path is the
	 * portion that follows the hostname and port in the URL.
	 * 
	 * @return the URI path, or null if it has none
	 */
	@Transient
	public String getSshUriPath() {
		String url = getUrl();
		if (getType() == ScmType.GIT && getScmLocation() == ScmLocation.CODE2CLOUD && url != null) {
			int lastIndex = url.lastIndexOf('/');
			if (lastIndex != -1 && lastIndex < url.length() - 1) {
				return '/' + getProject().getIdentifier() + '/' + url.substring(lastIndex + 1);
			}
		}
		return null;
	}

	public void setUrl(String newUrl) {
		this.url = newUrl;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 255)
	public ScmType getType() {
		return type;
	}

	public void setType(ScmType newType) {
		this.type = newType;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "location", nullable = false, length = 255)
	public ScmLocation getScmLocation() {
		return scmLocation;
	}

	public void setScmLocation(ScmLocation scmLocation) {
		this.scmLocation = scmLocation;
	}

	/**
	 * @return the SSH uri path, or null if there is none
	 * @see #getSshUriPath()
	 */
	public String computeSshUrl(String webHost, int sshPort) {
		String sshUriPath = getSshUriPath();
		if (sshUriPath != null) {
			String url = "ssh://" + webHost;
			if (sshPort != 22) {
				url += ":" + sshPort;
			}
			url += sshUriPath;
			return url;
		}
		return null;
	}
}
