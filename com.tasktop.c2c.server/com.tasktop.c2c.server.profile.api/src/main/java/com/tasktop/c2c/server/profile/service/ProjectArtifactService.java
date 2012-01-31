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
package com.tasktop.c2c.server.profile.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.tasktop.c2c.server.profile.domain.project.ProjectArtifact;
import com.tasktop.c2c.server.profile.domain.project.ProjectArtifacts;

/**
 * A service that enables management and finding of artifacts associated with a project.
 * 
 * @see ProjectArtifacts
 * @see ProjectArtifact
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 */
public interface ProjectArtifactService {

	/**
	 * Get all the project artifacts for a given project.
	 * 
	 * @param projectIdentifier
	 * @return all of the project's artifacts, sorting TBD (by category, then by date, then by name ???)
	 */
	List<ProjectArtifacts> listProjectArtifacts(String projectIdentifier);

	/**
	 * Get all the project artifacts for a given project. Returning only those artifacts whose name match the provide
	 * regular expression.
	 * 
	 * @param projectIdentifier
	 * @return all of the project's artifacts, sorting TBD (by category, then by date, then by name ???)
	 */
	List<ProjectArtifacts> listProjectArtifacts(String projectIdentifier, String artifactNameRegularExpression);

	/**
	 * Find an Artifacts by its build information, or null if no such artifact exists.
	 * 
	 * @param projectId
	 * @param buildJobName
	 * @param buildJobNumber
	 * @return artifact of null
	 */
	ProjectArtifacts findBuildArtifacts(String projectId, String buildJobName, String buildJobNumber);

	void downloadProjectArtifact(String projectId, File file, ProjectArtifact artifact) throws IOException;

	// /**
	// * Create a new projectArtifacts either from an existing TRANSIENT release (i.e, hudson generated), or from a
	// * manually created release (i.e., uploaded artifacts). Artifact content must be specified. Manually created
	// * releases <em>must</em> have a null {@link ProjectArtifacts#getType() type}.
	// */
	// Long createProjectArtifacts(ProjectArtifacts release);
	//
	// /**
	// * Modify project artifacts. New artifacts may be created, however existing ones must not have their content
	// * specified. Note that only ProjectArtifacts with an {@link AbstractEntity#getId() id} may be updated}
	// */
	// void updateProjectArtifacts(ProjectArtifacts release);
	//
	// /**
	// * remove a project artifacts. Note that only ProjectArtifacts with an {@link AbstractEntity#getId() id} may be
	// * removed}
	// */
	// void removeProjectArtifacts(ProjectArtifacts release);
	//
	// /**
	// * retrieve a project artifacts. Typically this would be done before
	// * {@link #updateProjectArtifacts(ProjectArtifacts) editing/updating}.
	// */
	// void retrieveProjectArtifacts(Long artifactsId);
}
