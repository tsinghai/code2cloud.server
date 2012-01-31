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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.access.annotation.Secured;
import org.springframework.tenancy.context.TenancyContextHolder;

import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.query.QueryUtil;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.scm.Commit;
import com.tasktop.c2c.server.profile.domain.scm.ScmSummary;
import com.tasktop.c2c.server.profile.service.GitService;

public class GitServiceBean implements GitService, InitializingBean {

	private static final String GIT_DIR_SUFFIX = ".git";

	private String basePath;

	/** Time between fetches for a repo. */
	private long milisecondsBetweenUpdates = 5 * 60 * 1000;

	/** Time between scans for new repos to fetch. */
	private long milisecondsBetweenScans = 1000;

	private AtomicBoolean stopRequest = new AtomicBoolean(false);

	private ExecutorService threadPool = Executors.newCachedThreadPool();
	private Map<String, Long> lastUpdateTimeByRepoPath = new HashMap<String, Long>();

	private class WorkerThread extends Thread {
		@Override
		public void run() {
			while (!stopRequest.get()) {
				triggerMirroredFetches();
				try {
					Thread.sleep(milisecondsBetweenScans);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void triggerMirroredFetches() {
		Long reFetchTime = System.currentTimeMillis() - milisecondsBetweenUpdates;
		for (File projectRoot : new File(basePath).listFiles()) {
			File mirroredRepos = new File(projectRoot, GitConstants.MIRRORED_GIT_DIR);
			if (!mirroredRepos.exists()) {
				continue;
			}
			for (final File mirroredRepo : mirroredRepos.listFiles()) {
				String repoPath = mirroredRepo.getAbsolutePath();
				Long lastFetch = lastUpdateTimeByRepoPath.get(repoPath);
				if (lastFetch == null || lastFetch < reFetchTime) {
					lastUpdateTimeByRepoPath.put(repoPath, System.currentTimeMillis());
					threadPool.execute(new Runnable() {

						@Override
						public void run() {
							doMirrorFetch(mirroredRepo);

						}
					});
				}
			}
		}
	}

	private void doMirrorFetch(File mirroredRepo) {
		try {
			Git git = new Git(new FileRepository(mirroredRepo));
			git.fetch().setRefSpecs(new RefSpec("refs/heads/*:refs/heads/*")).setThin(true).call();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JGitInternalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<Commit> getLog(Repository repository, Region region) {
		List<Commit> result = new ArrayList<Commit>();

		for (RevCommit revCommit : getAllCommits(repository)) {
			Commit commit = new Commit(revCommit.getName(), fromPersonIdent(revCommit.getAuthorIdent()), revCommit
					.getAuthorIdent().getWhen(), revCommit.getFullMessage());
			commit.setRepository(repository.getDirectory().getName());
			result.add(commit);
		}

		return result;
	}

	private Profile fromPersonIdent(PersonIdent person) {
		Profile result = new Profile();
		result.setEmail(person.getEmailAddress());
		result.setUsername(person.getEmailAddress());
		int firstSpace = person.getName().indexOf(" ");
		String firstName = firstSpace == -1 ? "" : person.getName().substring(0, firstSpace);
		String lastName = firstSpace == -1 ? person.getName() : person.getName().substring(firstSpace + 1);
		result.setFirstName(firstName);
		result.setLastName(lastName);

		return result;
	}

	private static final long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;
	private static final TimeZone tz = TimeZone.getTimeZone("PST"); // FIXME

	private List<ScmSummary> createEmptySummaries(int numDays) {
		List<ScmSummary> result = new ArrayList<ScmSummary>(numDays);

		Calendar cal = Calendar.getInstance(tz);
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 1);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Long now = cal.getTimeInMillis();
		for (int i = numDays - 1; i >= 0; i--) {
			ScmSummary summary = new ScmSummary();

			summary.setDate(new Date(now - i * MILLISECONDS_PER_DAY));
			summary.setAmount(0l);
			result.add(summary);
		}
		return result;
	}

	private void addCommitsToSummary(Repository repo, List<ScmSummary> summary) {
		Date firstDate = summary.get(0).getDate();

		for (RevCommit revCommit : getAllCommits(repo)) {
			Date commitDate = revCommit.getAuthorIdent().getWhen();
			if (commitDate.before(firstDate)) {
				continue;
			}
			for (int i = 0; i < summary.size(); i++) {
				if (i == summary.size() - 1) {
					summary.get(i).setAmount(summary.get(i).getAmount() + 1);
				} else if (summary.get(i).getDate().before(commitDate)
						&& commitDate.before(summary.get(i + 1).getDate())) {
					summary.get(i).setAmount(summary.get(i).getAmount() + 1);
					break;
				}
			}

		}
	}

	private File getTenantBaseDir() {
		return new File(basePath, (String) TenancyContextHolder.getContext().getTenant().getIdentity());
	}

	private File getTenantHostedBaseDir() {
		return new File(getTenantBaseDir(), GitConstants.HOSTED_GIT_DIR);
	}

	private File getTenantMirroredBaseDir() {
		return new File(getTenantBaseDir(), GitConstants.MIRRORED_GIT_DIR);
	}

	private List<RevCommit> getAllCommits(Repository repository) {
		List<RevCommit> result = new ArrayList<RevCommit>();

		try {
			RevWalk revWal = new RevWalk(repository);
			Set<ObjectId> objectsSeen = new HashSet<ObjectId>();

			Map<String, Ref> refs = repository.getAllRefs();
			for (Entry<String, Ref> entry : refs.entrySet()) {
				if (entry.getValue().getName().startsWith(Constants.R_HEADS)) {
					revWal.markStart(revWal.parseCommit(entry.getValue().getObjectId()));
				}
			}
			for (RevCommit revCommit : revWal) {
				if (objectsSeen.contains(revCommit.getId())) {
					continue;
				}
				result.add(revCommit);

			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	private List<Repository> getAllRepositories() {
		List<Repository> result = new ArrayList<Repository>();

		File hostedDir = getTenantHostedBaseDir();
		if (hostedDir.exists()) {
			for (File repoDir : hostedDir.listFiles()) {
				try {
					result.add(new FileRepository(repoDir));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		File mirroredDir = getTenantMirroredBaseDir();
		if (mirroredDir.exists()) {
			for (File repoDir : mirroredDir.listFiles()) {
				try {
					result.add(new FileRepository(repoDir));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return result;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public List<String> getRepositoryNames() {
		List<String> result = new ArrayList<String>();

		File[] fileList = getTenantHostedBaseDir().listFiles();

		if (fileList != null) {
			for (File file : fileList) {
				if (file.isDirectory() && file.getName().endsWith(GIT_DIR_SUFFIX)) {
					result.add(file.getName().substring(0, file.getName().length() - GIT_DIR_SUFFIX.length()));
				}
			}
		}

		return result;
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	// FIXME region is not really respected but...
	public List<Commit> getLog(Region region) {
		List<Commit> result = new ArrayList<Commit>();

		for (Repository repo : getAllRepositories()) {
			result.addAll(getLog(repo, region));
		}

		Collections.sort(result, new Comparator<Commit>() {

			@Override
			public int compare(Commit o1, Commit o2) {
				return o2.getDate().compareTo(o1.getDate());
			}
		});

		QueryUtil.applyRegionToList(result, region);

		return result;
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public List<ScmSummary> getScmSummary(int numDays) {
		List<ScmSummary> result = createEmptySummaries(numDays);

		for (Repository repo : getAllRepositories()) {
			addCommitsToSummary(repo, result);
		}
		return result;
	}

	@Secured({ Role.Admin })
	@Override
	public void createEmptyRepository(String name) {
		File hostedDir = getTenantHostedBaseDir();
		File gitDir = new File(hostedDir, name);
		gitDir.mkdirs();
		try {
			new FileRepository(gitDir).create();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public Map<Profile, Integer> getNumCommitsByAuthor(int numDays) {
		Map<Profile, Integer> result = new HashMap<Profile, Integer>();
		Map<String, Profile> profilesByName = new HashMap<String, Profile>();
		for (Repository repo : getAllRepositories()) {
			addCommitsToCommitsByAuthor(result, profilesByName, repo, numDays);
		}

		return result;
	}

	private void addCommitsToCommitsByAuthor(Map<Profile, Integer> commitsByAuthor,
			Map<String, Profile> profilesByName, Repository repository, int numDays) {

		Date firstDay = new Date(System.currentTimeMillis() - MILLISECONDS_PER_DAY * numDays);

		for (RevCommit c : getAllCommits(repository)) {
			if (c.getAuthorIdent().getWhen().before(firstDay)) {
				continue;
			}

			Profile p = profilesByName.get(c.getAuthorIdent().getName());
			if (p == null) {
				p = fromPersonIdent(c.getAuthorIdent());
				p.setId((long) profilesByName.size()); // wrong id but need for eq/hash
				profilesByName.put(c.getAuthorIdent().getName(), p);
			}

			Integer count = commitsByAuthor.get(p);
			if (count == null) {
				count = 0;
			}
			count++;
			commitsByAuthor.put(p, count);

		}
	}

	static String getRepoDirNameFromExternalUrl(String url) {
		String path;
		try {
			path = new URI(url).getPath();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		int i = path.lastIndexOf("/");
		if (i == -1) {
			return path;
		}
		return path.substring(i + 1);
	}

	@Secured({ Role.Admin })
	@Override
	public void addExternalRepository(String url) {
		try {
			String repoName = getRepoDirNameFromExternalUrl(url);
			File dir = new File(getTenantMirroredBaseDir(), repoName);
			Git git = Git.init().setBare(true).setDirectory(dir).call();
			RemoteConfig config = new RemoteConfig(git.getRepository().getConfig(), Constants.DEFAULT_REMOTE_NAME);
			config.addURI(new URIish(url));
			config.update(git.getRepository().getConfig());
			git.getRepository().getConfig().save();
		} catch (JGitInternalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Secured({ Role.Admin })
	@Override
	public void removeExternalRepository(String url) {
		String repoName = getRepoDirNameFromExternalUrl(url);
		File dir = new File(getTenantMirroredBaseDir(), repoName);
		try {
			FileUtils.delete(dir, FileUtils.RECURSIVE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		new WorkerThread().start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.profile.service.GitService#removeInternalRepository(java.lang.String)
	 */
	@Override
	public void removeInternalRepository(String name) {
		File hostedDir = getTenantHostedBaseDir();
		File dir = new File(hostedDir, name);
		try {
			FileUtils.delete(dir, FileUtils.RECURSIVE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
