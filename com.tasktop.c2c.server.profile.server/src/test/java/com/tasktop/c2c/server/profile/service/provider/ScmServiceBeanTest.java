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

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.cloud.domain.ScmLocation;
import com.tasktop.c2c.server.cloud.domain.ScmType;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.profile.domain.internal.Project;
import com.tasktop.c2c.server.profile.domain.internal.ScmRepository;
import com.tasktop.c2c.server.profile.service.GitService;
import com.tasktop.c2c.server.profile.tests.domain.mock.MockProjectFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-testDisableSecurity.xml" })
@Transactional
public class ScmServiceBeanTest {

	@Resource(name = "scmService")
	private ScmServiceInternal scmService;

	@PersistenceContext
	private EntityManager entityManager;

	private Mockery context;

	private ServiceProvider<GitService> createMockProvider() {
		context = new JUnit4Mockery();
		@SuppressWarnings("unchecked")
		final ServiceProvider<GitService> mockGitServiceProvider = context.mock(ServiceProvider.class);
		final GitService mockGitService = context.mock(GitService.class);
		context.checking(new Expectations() {
			{
				allowing(mockGitServiceProvider).getService(with(any(String.class)));
				will(returnValue(mockGitService));
				allowing(mockGitService);
			}
		});

		return mockGitServiceProvider;
	}

	@Before
	public void setup() {
		TenancyContextHolder.createEmptyContext();
		scmService.setServiceProvider(createMockProvider());
	}

	private void putProjectInTenancyContext(Project proj) {
		DefaultTenant tenant = new DefaultTenant();
		tenant.setIdentity(proj.getIdentifier());
		TenancyContextHolder.getContext().setTenant(tenant);
	}

	@Test
	public void testCreateCode2CloudProjectScmConfiguration() throws Exception {
		Project proj = MockProjectFactory.create(entityManager);
		putProjectInTenancyContext(proj);
		String repositoryName = "code2cloud.git";
		String repo = "http://q.tasktop.com/alm/s/code2cloud/scm/" + repositoryName;
		String repoSsh = "ssh://localhost/" + proj.getIdentifier() + "/" + repositoryName;

		ScmRepository newRepo = new ScmRepository();
		newRepo.setProject(proj);
		newRepo.setScmLocation(ScmLocation.CODE2CLOUD);
		newRepo.setType(ScmType.GIT);
		newRepo.setUrl(repo);
		proj.getRepositories().add(newRepo);
		entityManager.persist(newRepo);

		List<com.tasktop.c2c.server.profile.domain.scm.ScmRepository> repoSet = scmService.getScmRepositories();

		assertEquals(1, repoSet.size());
		com.tasktop.c2c.server.profile.domain.scm.ScmRepository retrievedRepo = repoSet.iterator().next();
		assertEquals(newRepo.getId(), retrievedRepo.getId());
		assertEquals(newRepo.getProject().getId(), retrievedRepo.getProject().getId());
		assertEquals(newRepo.getType(), retrievedRepo.getType());
		assertEquals(newRepo.getScmLocation(), retrievedRepo.getScmLocation());
		assertEquals(newRepo.getUrl(), retrievedRepo.getUrl());
		Assert.assertNotNull(retrievedRepo.getAlternateUrl());
		assertEquals(repoSsh, retrievedRepo.getAlternateUrl());
	}

	@Test
	public void testCreateExternalScmRepository() throws Exception {
		Project project = MockProjectFactory.create(entityManager);
		putProjectInTenancyContext(project);
		String repo = "http://q.tasktop.com/alm/s/code2cloud/scm/code2cloud.git";

		List<com.tasktop.c2c.server.profile.domain.scm.ScmRepository> repoSet = scmService.getScmRepositories();
		assertEquals(0, repoSet.size());

		scmService.createScmRepository(createExternalRepo(repo));

		repoSet = scmService.getScmRepositories();
		assertEquals(1, repoSet.size());
		com.tasktop.c2c.server.profile.domain.scm.ScmRepository retrievedRepo = repoSet.iterator().next();
		assertEquals(project.getId(), retrievedRepo.getProject().getId());
		assertEquals(ScmType.GIT, retrievedRepo.getType());
		assertEquals(ScmLocation.EXTERNAL, retrievedRepo.getScmLocation());
		assertEquals(repo, retrievedRepo.getUrl());
		assertNull(retrievedRepo.getAlternateUrl());
	}

	private com.tasktop.c2c.server.profile.domain.scm.ScmRepository createExternalRepo(String externalUrl) {
		com.tasktop.c2c.server.profile.domain.scm.ScmRepository repo = new com.tasktop.c2c.server.profile.domain.scm.ScmRepository();
		repo.setType(ScmType.GIT);
		repo.setScmLocation(ScmLocation.EXTERNAL);
		repo.setUrl(externalUrl);
		return repo;
	}

	@Test(expected = ValidationException.class)
	public void testCreateExternalScmRepository_BlankURL() throws Exception {
		Project project = MockProjectFactory.create(entityManager);
		putProjectInTenancyContext(project);
		String repositoryURL = "";

		List<com.tasktop.c2c.server.profile.domain.scm.ScmRepository> repoSet = scmService.getScmRepositories();
		assertEquals(0, repoSet.size());

		scmService.createScmRepository(createExternalRepo(repositoryURL));
		// should throw exception
		Assert.fail();
	}

	@Test(expected = ValidationException.class)
	public void testCreateExternalScmRepository_NullURL() throws Exception {
		Project project = MockProjectFactory.create(entityManager);
		putProjectInTenancyContext(project);
		String repositoryURL = null;

		List<com.tasktop.c2c.server.profile.domain.scm.ScmRepository> repoSet = scmService.getScmRepositories();
		assertEquals(0, repoSet.size());

		scmService.createScmRepository(createExternalRepo(repositoryURL));
		// should throw exception
		Assert.fail();
	}

	@Test
	public void testDeleteScmRepository() throws Exception {
		Project proj = MockProjectFactory.create(entityManager);
		putProjectInTenancyContext(proj);
		String repo = "http://q.tasktop.com/alm/s/code2cloud/scm/code2cloud.git";

		List<com.tasktop.c2c.server.profile.domain.scm.ScmRepository> repoSet = scmService.getScmRepositories();
		assertEquals(0, repoSet.size());

		scmService.createScmRepository(createExternalRepo(repo));

		repoSet = scmService.getScmRepositories();
		assertEquals(1, repoSet.size());
		com.tasktop.c2c.server.profile.domain.scm.ScmRepository retrievedRepo = repoSet.iterator().next();
		assertEquals(proj.getId(), retrievedRepo.getProject().getId());
		assertEquals(ScmType.GIT, retrievedRepo.getType());
		assertEquals(ScmLocation.EXTERNAL, retrievedRepo.getScmLocation());
		assertEquals(repo, retrievedRepo.getUrl());

		scmService.deleteScmRepository(retrievedRepo.getId());

		repoSet = scmService.getScmRepositories();
		assertEquals(0, repoSet.size());
	}

	@Test(expected = EntityNotFoundException.class)
	public void testDeleteScmRepository_nonexistentProject() throws Exception {
		putProjectInTenancyContext(MockProjectFactory.create(entityManager));

		// This should blow up.
		scmService.deleteScmRepository(123L);
	}
}
