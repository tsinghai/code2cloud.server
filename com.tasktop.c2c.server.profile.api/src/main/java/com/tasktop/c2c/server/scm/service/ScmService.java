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
package com.tasktop.c2c.server.scm.service;

import java.util.List;
import java.util.Map;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.scm.Commit;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;
import com.tasktop.c2c.server.profile.domain.scm.ScmSummary;

/**
 * Interface for interacting with the {@link ScmRepository}s for a given project.
 * 
 * @author Ryan Slobojon <ryan.slobojan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public interface ScmService {

	List<ScmRepository> getScmRepositories() throws EntityNotFoundException;

	/**
	 * Create a new repository record.
	 * 
	 * @param newRepository
	 *            should be non-null and have appropriate fields filled in
	 * @return
	 * @throws EntityNotFoundException
	 * @throws ValidationException
	 */
	ScmRepository createScmRepository(ScmRepository newRepository) throws EntityNotFoundException, ValidationException;

	/**
	 * Remove a repository record.
	 * 
	 * @param scmRepositoryId
	 * @throws EntityNotFoundException
	 */
	void deleteScmRepository(Long scmRepositoryId) throws EntityNotFoundException;

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
