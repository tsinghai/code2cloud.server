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

import java.util.List;
import java.util.Map;

import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.scm.Commit;
import com.tasktop.c2c.server.profile.domain.scm.ScmSummary;

/**
 * Serivce for interacting with a project's git repository. Allows to create and delete new internal/external
 * repositories and to obtain summary and log information. Standard git operations (e.g., clone or push) are done using
 * standard git protocol(s) and tools.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public interface GitService {

	/**
	 * Get all the repository names.
	 * 
	 * @return
	 */
	List<String> getRepositoryNames();

	void addExternalRepository(String url);

	void removeExternalRepository(String url);

	void removeInternalRepository(String name);

	/**
	 * Create an empty repository.
	 * 
	 * @param name
	 *            of the repository
	 */
	void createEmptyRepository(String name);

	/**
	 * Get the logs from all repositories.
	 * 
	 * @param region
	 *            : can be null => all
	 * @return
	 */
	List<Commit> getLog(Region region);

	/**
	 * Get a summary of activity from all repositories.
	 * 
	 * @param numDays
	 *            back in time to go
	 * @return
	 */
	List<ScmSummary> getScmSummary(int numDays);

	Map<Profile, Integer> getNumCommitsByAuthor(int numDays);

}
