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
package com.tasktop.c2c.server.services.web.scm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.web.AbstractRestService;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.scm.Commit;
import com.tasktop.c2c.server.profile.domain.scm.ScmSummary;
import com.tasktop.c2c.server.profile.service.GitService;
import com.tasktop.c2c.server.profile.service.GitServiceClient.CommitsForAuthor;

@Controller
public class GitServiceController extends AbstractRestService {

	private GitService gitService;

	@RequestMapping(value = "/log/", method = RequestMethod.GET)
	public List<Commit> getLog(@RequestParam(required = false, value = "pageSize") Integer pageSize,
			@RequestParam(required = false, value = "offset") Integer offset) {
		Region region = null;
		if (offset != null && pageSize != null) {
			region = new Region(offset, pageSize);
		}
		return gitService.getLog(region);
	}

	@RequestMapping(value = "/summary/", method = RequestMethod.GET)
	public List<ScmSummary> getSummary(@RequestParam(required = false, value = "numDays") Integer numDays) {

		if (numDays == null) {
			numDays = 60;
		}
		return gitService.getScmSummary(numDays);
	}

	@RequestMapping(value = "/commitsByAuthor/", method = RequestMethod.GET)
	public List<CommitsForAuthor> getCommitsByAuthor(@RequestParam(required = false, value = "numDays") Integer numDays) {
		if (numDays == null) {
			numDays = 60;
		}

		List<CommitsForAuthor> result = new ArrayList<CommitsForAuthor>();
		for (Entry<Profile, Integer> entry : gitService.getNumCommitsByAuthor(numDays).entrySet()) {
			CommitsForAuthor c = new CommitsForAuthor();
			c.setAuthor(entry.getKey());
			c.setCount(entry.getValue());
			result.add(c);
		}
		return result;
	}

	@RequestMapping(value = "/repository/", method = RequestMethod.GET)
	public List<String> getRepositoryNames() {
		return gitService.getRepositoryNames();
	}

	@RequestMapping(value = "/addExternal/", method = RequestMethod.POST)
	public void addExternalRepo(@RequestBody String url) {
		gitService.addExternalRepository(url);
	}

	@RequestMapping(value = "/addInternal/", method = RequestMethod.POST)
	public void createEmptyRepository(@RequestBody String repoName) {
		gitService.createEmptyRepository(repoName);
		;
	}

	@RequestMapping(value = "/removeInternal/", method = RequestMethod.POST)
	public void removeInternalRepository(@RequestBody String repoName) {
		gitService.removeInternalRepository(repoName);
	}

	@RequestMapping(value = "/removeExternal/", method = RequestMethod.POST)
	public void removeExternalRepo(@RequestBody String url) {
		gitService.removeExternalRepository(url);
	}

	public void setGitService(GitService gitService) {
		this.gitService = gitService;
	}
}
