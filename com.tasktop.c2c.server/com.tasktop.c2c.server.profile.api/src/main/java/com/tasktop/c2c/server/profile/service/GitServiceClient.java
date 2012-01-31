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

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.web.AbstractRestServiceClient;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.scm.Commit;
import com.tasktop.c2c.server.profile.domain.scm.ScmSummary;

public class GitServiceClient extends AbstractRestServiceClient implements GitService {

	/**
	 * Essentially a map entry, but spring does not like map return results that are meant to be response data.
	 * 
	 */
	public static final class CommitsForAuthor {
		private Profile author;
		private Integer count;

		public void setAuthor(Profile author) {
			this.author = author;
		}

		public Profile getAuthor() {
			return author;
		}

		public void setCount(Integer count) {
			this.count = count;
		}

		public Integer getCount() {
			return count;
		}
	}

	@SuppressWarnings("unused")
	private static final class ServiceCallResult {
		private List<Commit> commitList;
		private List<ScmSummary> scmSummaryList;
		private List<String> stringList;
		private List<CommitsForAuthor> commitsForAuthorList;

		public List<Commit> getCommitList() {
			return commitList;
		}

		public void setCommitList(List<Commit> commitList) {
			this.commitList = commitList;
		}

		public List<ScmSummary> getScmSummaryList() {
			return scmSummaryList;
		}

		public void setScmSummaryList(List<ScmSummary> scmSummaryList) {
			this.scmSummaryList = scmSummaryList;
		}

		public List<String> getStringList() {
			return stringList;
		}

		public void setStringList(List<String> stringList) {
			this.stringList = stringList;
		}

		public void setCommitsForAuthorList(List<CommitsForAuthor> commitsForAuthorList) {
			this.commitsForAuthorList = commitsForAuthorList;
		}

		public List<CommitsForAuthor> getCommitsForAuthorList() {
			return commitsForAuthorList;
		}

	}

	public List<Commit> getLog(Region region) {
		String url = computeUrl("/log/");
		if (region != null) {
			url = url + "?offset=" + region.getOffset() + "&pageSize=" + region.getSize();
		}
		ServiceCallResult result = template.getForObject(url, ServiceCallResult.class);
		if (result.getCommitList() != null) {
			return result.getCommitList();
		}
		throw new IllegalStateException("bad response from service");
	}

	public List<ScmSummary> getScmSummary(int numDays) {
		String url = computeUrl("/summary/" + "?days=" + numDays);

		ServiceCallResult result = template.getForObject(url, ServiceCallResult.class);
		if (result.getScmSummaryList() != null) {
			return result.getScmSummaryList();
		}
		throw new IllegalStateException("bad response from service");
	}

	public List<String> getRepositoryNames() {
		ServiceCallResult result = template.getForObject(computeUrl("/repository/"), ServiceCallResult.class);
		if (result.getStringList() != null) {
			return result.getStringList();
		}
		throw new IllegalStateException("bad response from service");
	}

	public void createEmptyRepository(String name) {
		template.postForObject(computeUrl("/addInternal"), name, Void.class);
	}

	public void removeInternalRepository(String name) {
		template.postForObject(computeUrl("/removeInternal"), name, Void.class);
	}

	private Map<Profile, Integer> toMap(List<CommitsForAuthor> commitsForAuthor) {
		Map<Profile, Integer> result = new HashMap<Profile, Integer>();
		for (CommitsForAuthor c : commitsForAuthor) {
			result.put(c.getAuthor(), c.getCount());
		}
		return result;
	}

	public Map<Profile, Integer> getNumCommitsByAuthor(int numDays) {
		String url = computeUrl("/commitsByAuthor/" + "?days=" + numDays);

		ServiceCallResult result = template.getForObject(url, ServiceCallResult.class);
		if (result.getCommitsForAuthorList() != null) {
			return toMap(result.getCommitsForAuthorList());
		}
		throw new IllegalStateException("bad response from service");
	}

	public void addExternalRepository(String url) {
		template.postForObject(computeUrl("/addExternal/"), url, Void.class);
	}

	public void removeExternalRepository(String url) {
		template.postForObject(computeUrl("/removeExternal/"), url, Void.class);
	}

}
