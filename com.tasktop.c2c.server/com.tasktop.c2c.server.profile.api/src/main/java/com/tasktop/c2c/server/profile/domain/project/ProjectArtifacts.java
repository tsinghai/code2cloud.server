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
package com.tasktop.c2c.server.profile.domain.project;

import java.util.Date;
import java.util.List;

/**
 * A release of project. Collects together multiple artifacts (jars, wars, etc) along with metadata about the release.
 * Some project artifacts may not have their id specified if they're transient.
 */
@SuppressWarnings("serial")
public class ProjectArtifacts extends AbstractEntity {

	public static String computeBuildIdentifier(String buildJobName, Integer buildJobNumber) {
		return buildJobName + "-" + buildJobNumber;
	}

	public enum Type {
		/**
		 * Available for indeterminate periods of time, usually provided by the build system, but removed based on
		 * space/time criteria.
		 */
		TRANSIENT,
		/**
		 * Available until explicity removed, usually corresponding to a released build.
		 */
		PERSISTED
	};

	private String buildJobName;
	private String buildJobNumber;

	/**
	 * @return the buildJobName
	 */
	public String getBuildJobName() {
		return buildJobName;
	}

	/**
	 * @param buildJobName
	 *            the buildJobName to set
	 */
	public void setBuildJobName(String buildJobName) {
		this.buildJobName = buildJobName;
	}

	/**
	 * @return the buildJobNumber
	 */
	public String getBuildJobNumber() {
		return buildJobNumber;
	}

	/**
	 * @param buildJobNumber
	 *            the buildJobNumber to set
	 */
	public void setBuildJobNumber(String buildJobNumber) {
		this.buildJobNumber = buildJobNumber;
	}

	private String buildIdentifier;
	private String name;
	private Type type;
	private String version;
	private Date creationDate;
	private String category;
	private String description;
	private List<ProjectArtifact> artifacts;

	/**
	 * The name that users use to identify a build or release set of artifacts. By convention, should default to the
	 * project name.
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * the type of the artifact that indicates its nature, for example released builds are archived, whereas build types
	 * are transient.
	 */
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * the version. For example "1.0.2" or "1.1.0.SNAPSHOT" may be null.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * A means of categorizing builds, for example "Released", "Nightly", "Weekly", "Snapshot", etc.
	 */
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * a description that describes the release/build, may be null
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * the list of artifacts, should be non-empty
	 */
	public List<ProjectArtifact> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<ProjectArtifact> artifacts) {
		this.artifacts = artifacts;
	}

	/**
	 * the date that the project artifacts were created. May be modified in the case where a release should occur on a
	 * specific date.
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date date) {
		this.creationDate = date;
	}

	/**
	 * A build identifier, used when converting a build from the build service to a release. May be null
	 */
	public String getBuildIdentifier() {
		return buildIdentifier;
	}

	public void setBuildIdentifier(String buildIdentifier) {
		this.buildIdentifier = buildIdentifier;
	}

}
