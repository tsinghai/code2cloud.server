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
package com.tasktop.c2c.server.profile.service.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.tasktop.c2c.server.cloud.domain.ScmLocation;
import com.tasktop.c2c.server.cloud.domain.ScmType;
import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.internal.profile.service.SecurityPolicy;
import com.tasktop.c2c.server.internal.profile.service.WebServiceDomain;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ScmRepository;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.scm.Commit;
import com.tasktop.c2c.server.profile.domain.scm.ScmSummary;
import com.tasktop.c2c.server.profile.service.GitService;
import com.tasktop.c2c.server.profile.service.ProfileService;
import com.tasktop.c2c.server.profile.service.ProfileServiceConfiguration;

@Service("scmService")
@Qualifier("main")
@Transactional(rollbackFor = { Exception.class })
public class ScmServiceBean extends AbstractJpaServiceBean implements ScmServiceInternal {

	@Autowired
	private SecurityPolicy securityPolicy;

	@Resource(name = "gitServiceProvider")
	private ServiceProvider<GitService> gitServiceProvider;

	@Autowired
	@Qualifier("main")
	private ProfileService profileService;

	@Autowired
	private WebServiceDomain webServiceDomain;

	@Autowired
	private ProfileServiceConfiguration profileServiceConfiguration;

	private GitService getCurrentService() {
		String projId = TenancyContextHolder.getContext().getTenant().getIdentity().toString();
		return gitServiceProvider.getService(projId);
	}

	private Project getCurrentProject() throws EntityNotFoundException {
		String projId = TenancyContextHolder.getContext().getTenant().getIdentity().toString();
		return profileService.getProjectByIdentifier(projId);
	}

	@Override
	public List<com.tasktop.c2c.server.profile.domain.scm.ScmRepository> getScmRepositories()
			throws EntityNotFoundException {

		List<ScmRepository> scmRepoSet = getCurrentProject().getRepositories();
		List<com.tasktop.c2c.server.profile.domain.scm.ScmRepository> domainRepos = new ArrayList<com.tasktop.c2c.server.profile.domain.scm.ScmRepository>();

		for (ScmRepository internalRepo : scmRepoSet) {
			domainRepos.add(webServiceDomain.copy(internalRepo));
		}

		return domainRepos;
	}

	@Override
	public com.tasktop.c2c.server.profile.domain.scm.ScmRepository createScmRepository(
			com.tasktop.c2c.server.profile.domain.scm.ScmRepository repository) throws EntityNotFoundException,
			ValidationException {
		Project project = getCurrentProject();
		String internalUrlPrefix = profileServiceConfiguration.getHostedScmUrlPrefix(project.getIdentifier());

		// Provide Defaults
		if (ScmLocation.CODE2CLOUD.equals(repository.getScmLocation()) && repository.getName() != null) {
			if (!repository.getName().endsWith(".git")) {
				Errors errors = createErrors(repository);
				throw new ValidationException("scmrepo.internal.nameMustEndWithGit", errors);
			} else if (repository.getName().equals(".git")) {
				Errors errors = createErrors(repository);
				errors.reject("scmrepo.internal.nameEmpty");
				throw new ValidationException(errors);
			}
			repository.setUrl(internalUrlPrefix + repository.getName());
		}
		// Create a new ScmRepository for this project
		ScmRepository newRepository = new ScmRepository();
		newRepository.setScmLocation(repository.getScmLocation());
		newRepository.setType(ScmType.GIT);
		newRepository.setUrl(repository.getUrl());
		newRepository.setProject(project);

		// FIXME this can be replaced with a @Secured annotation once projectIdentifier token specialization is
		// implemented
		// for this service
		securityPolicy.create(newRepository);

		// Validate the internal object.
		validate(newRepository);

		if (findRepositoryByUrl(newRepository.getUrl(), project) != null) {
			Errors errors = createErrors(repository);
			errors.reject("scmrepo.urlExists");
			throw new ValidationException(errors);
		}
		if (ScmLocation.EXTERNAL.equals(newRepository.getScmLocation())
				&& newRepository.getUrl().startsWith(internalUrlPrefix)) {
			Errors errors = createErrors(repository);
			errors.reject("scmrepo.external.url.isInternal");
			throw new ValidationException(errors);
		}
		if (ScmLocation.CODE2CLOUD.equals(newRepository.getScmLocation())
				&& !newRepository.getUrl().startsWith(internalUrlPrefix)) {
			Errors errors = createErrors(repository);
			errors.reject("scmrepo.internal.url.isExternal");
			throw new ValidationException(errors);
		}

		// Save this to the database now.
		entityManager.persist(newRepository);

		// We add the object to the set after it is persisted - because the hashcode is based upon the ID, if you add it
		// to the set and then persist it, the ID (and thus the hash) changes and lookups fail (this occurred in unit
		// testing).
		project.getRepositories().add(newRepository);
		entityManager.persist(project);

		if (newRepository.getScmLocation().equals(ScmLocation.EXTERNAL)) {
			getCurrentService().addExternalRepository(newRepository.getUrl());
		} else {
			getCurrentService().createEmptyRepository(repository.getName());
		}

		return webServiceDomain.copy(newRepository);
	}

	/**
	 * @param url
	 * @param project
	 * @return
	 */
	private ScmRepository findRepositoryByUrl(String url, Project project) {
		try {
			ScmRepository result = (ScmRepository) entityManager
					.createQuery(
							"SELECT s FROM " + ScmRepository.class.getSimpleName()
									+ " s WHERE s.url = :url AND s.project.id = :projectId").setParameter("url", url)
					.setParameter("projectId", project.getId()).getSingleResult();
			return result;

		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public void deleteScmRepository(Long deleteRepoUrl) throws EntityNotFoundException {
		Project project = getCurrentProject();

		// Find the related ScmRepository for this URL
		for (ScmRepository curRepo : project.getRepositories()) { // FIXME just do a EM.find
			if (curRepo.getId().equals(deleteRepoUrl)) {

				// FIXME this can be replaced with a @Secured annotation once app token specialization is implemented
				// for this service
				securityPolicy.delete(curRepo);

				// Remove this repository.
				project.getRepositories().remove(curRepo);
				entityManager.remove(curRepo);
				entityManager.persist(project);
				entityManager.flush();

				if (curRepo.getScmLocation().equals(ScmLocation.EXTERNAL)) {
					getCurrentService().removeExternalRepository(curRepo.getUrl());
				} else {
					String repoUrlPrefix = profileServiceConfiguration.getHostedScmUrlPrefix(project.getIdentifier());
					if (!curRepo.getUrl().startsWith(repoUrlPrefix)) {
						throw new IllegalStateException(
								"Could not remove internal repo, its url is in unexpected format");
					}
					String repoName = curRepo.getUrl().substring(repoUrlPrefix.length());
					getCurrentService().removeInternalRepository(repoName);
				}

				return;
			}
		}

		// If we got to this point, then we didn't find our repository - throw an exception.
		throw new EntityNotFoundException();
	}

	@Override
	public Map<Profile, Integer> getNumCommitsByAuthor(int numDays) {
		return getCurrentService().getNumCommitsByAuthor(numDays);
	}

	@Override
	public List<ScmSummary> getScmSummary(int numDays) {
		return getCurrentService().getScmSummary(numDays);
	}

	@Override
	public List<Commit> getLog(Region region) {
		return getCurrentService().getLog(region);
	}

	@Override
	public void setServiceProvider(ServiceProvider<GitService> testProvider) {
		this.gitServiceProvider = testProvider;
	}
}
