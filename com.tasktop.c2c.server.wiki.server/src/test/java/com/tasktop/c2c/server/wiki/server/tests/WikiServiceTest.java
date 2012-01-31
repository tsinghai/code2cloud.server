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
package com.tasktop.c2c.server.wiki.server.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.MockJobService;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.job.Job;
import com.tasktop.c2c.server.common.tests.util.ValidationAssert;
import com.tasktop.c2c.server.internal.wiki.server.UpdatePageContentJob;
import com.tasktop.c2c.server.internal.wiki.server.domain.PageContent;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.AttachmentHandle;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.domain.PageHandle;
import com.tasktop.c2c.server.wiki.domain.PageOutline;
import com.tasktop.c2c.server.wiki.domain.Person;
import com.tasktop.c2c.server.wiki.domain.WikiActivity;
import com.tasktop.c2c.server.wiki.server.tests.mock.MockAttachmentFactory;
import com.tasktop.c2c.server.wiki.server.tests.mock.MockPageFactory;
import com.tasktop.c2c.server.wiki.server.tests.util.TestSecurity;
import com.tasktop.c2c.server.wiki.service.WikiService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml", "/applicationContext-testSecurity.xml" })
@Transactional
public class WikiServiceTest {

	@Autowired
	protected WikiService wikiService;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private MockJobService jobService;

	@Autowired
	private ApplicationContext applicationContext;

	private Person author;

	private Person person2;

	private boolean tenantSet;

	@Qualifier("rawDataSource")
	@Autowired
	private DataSource dataSource;

	@Before
	public void before() {
		if (TenancyContextHolder.getContext().getTenant() == null) {
			tenantSet = true;
			TenancyContextHolder.getContext().setTenant(new DefaultTenant("test", "test"));
		}
		author = new Person();
		author.setName("Joe Bloe");
		author.setLoginName("joe.bloe@example.com");

		person2 = new Person();
		person2.setName("Frank Smith");
		person2.setLoginName("person2@example.com");
	}

	@After
	public void after() {
		if (tenantSet) {
			TenancyContextHolder.clearContext();
		}
		clearCredentials();
		jobService.getScheduledJobs().clear();
	}

	private void clearCredentials() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	private void logon(Person person) {
		TestSecurity.login(person);
	}

	@Test
	public void testHsqlQueries() throws SQLException {
		dataSource.getConnection().createStatement().executeQuery("SELECT * FROM Page");
		dataSource.getConnection().createStatement().executeQuery("SELECT * FROM PAGE");

		try {
			dataSource.getConnection().createStatement().executeQuery("SELECT * FROM \"Page\"");
			Assert.fail("expected ex");
		} catch (SQLException e) {
			// expected
		}

		dataSource.getConnection().createStatement().executeQuery("SELECT * FROM \"PAGE\"");

		dataSource.getConnection().createStatement()
				.executeQuery("SELECT ID, CREATIONDATE, IDENTITY, MODIFICATIONDATE, NAME, VERSION FROM PERSON");

	}

	@Test
	public void testCreatePage_RequiresLogon() throws ValidationException {
		try {
			Page page = new Page();
			page.setId(12345);
			wikiService.createPage(page);
			fail("expected AuthenticationException");
		} catch (AuthenticationException e) {
			// expected
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testCreatePage() throws ValidationException, Exception {
		logon(author);

		Page wikiPage = new Page();
		wikiPage.setContent("abc 123\n456 ");
		wikiPage.setCreationDate(null);
		wikiPage.setPath("Foo Bar");
		Page created = wikiService.createPage(wikiPage);

		assertNotNull(created.getId());
		assertEquals(wikiPage.getContent(), created.getContent());
		assertNotNull(created.getOriginalAuthor());
		assertNotNull(created.getCreationDate());
		assertNotNull(created.getModificationDate());
		assertEquals(wikiPage.getPath(), created.getPath());
		assertNotNull(created.getUrl());
		assertTrue(created.getUrl().startsWith("http:"));
		assertTrue(created.getUrl().endsWith(URLEncoder.encode(wikiPage.getPath(), "utf-8").replace("%20", "+")));
	}

	@Test
	public void testDeletePageAndRestore() throws ValidationException, Exception {
		logon(author);

		Page wikiPage = new Page();
		wikiPage.setContent("abc 123\n456 ");
		wikiPage.setCreationDate(null);
		wikiPage.setPath("Foo Bar");
		Page created = wikiService.createPage(wikiPage);
		created.setContent("newC Content");
		created = wikiService.updatePage(created);

		Assert.assertEquals(1, (int) wikiService.findPages(null, null).getTotalResultSize());

		wikiService.deletePage(created.getId());

		Assert.assertEquals(0, (int) wikiService.findPages(null, null).getTotalResultSize());

		wikiService.restorePage(created.getId());

		QueryResult<Page> pages = wikiService.findPages(null, null);
		Assert.assertEquals(1, (int) pages.getTotalResultSize());
		Assert.assertEquals(created.getContent(), pages.getResultPage().get(0).getContent());
	}

	@Test
	public void testCreatePage_DuplicateName() throws ValidationException, Exception {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		logon(author);

		Page wikiPage = new Page();
		wikiPage.setContent("abc 123\n456 ");
		wikiPage.setCreationDate(null);
		wikiPage.setPath(page.getPath());
		try {
			wikiService.createPage(wikiPage);
			fail("expected failure");
		} catch (ValidationException e) {
			ValidationAssert.assertHaveValidationError(e, "nonUnique.path", wikiPage.getPath());
		}
	}

	@Test
	public void testRetrievePage_NotFound() throws EntityNotFoundException {
		logon(author);

		try {
			wikiService.retrievePage(12345);
			fail("expected exception");
		} catch (EntityNotFoundException e) {
			// expected
		}
	}

	@Test
	public void testRetrievePage() throws EntityNotFoundException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		entityManager.flush();

		Page retrieved = wikiService.retrievePage(page.getId().intValue());
		assertNotNull(retrieved);
		assertEquals(Integer.valueOf(page.getId().intValue()), retrieved.getId());
	}

	@Test
	public void testRetrievePageByPath() throws EntityNotFoundException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		entityManager.flush();

		Page retrieved = wikiService.retrievePageByPath(page.getPath());
		assertNotNull(retrieved);
		assertEquals(Integer.valueOf(page.getId().intValue()), retrieved.getId());
		assertEquals(page.getLastPageContent().getContent(), retrieved.getContent());

		try {
			wikiService.retrievePageByPath("notfound");
			fail("expected exception");
		} catch (EntityNotFoundException e) {
			// expected
		}

	}

	@Test
	public void testRetrievePageByPathWithSlash() throws EntityNotFoundException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		page.setPath("First/Second");
		entityManager.flush();

		Page retrieved = wikiService.retrievePageByPath(page.getPath());
		assertNotNull(retrieved);
		assertEquals(Integer.valueOf(page.getId().intValue()), retrieved.getId());
	}

	@Test
	public void testRetrievePageByPathWithSpaces() throws EntityNotFoundException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		page.setPath("First Second");
		entityManager.flush();

		Page retrieved = wikiService.retrievePageByPath(page.getPath());
		assertNotNull(retrieved);
		assertEquals(Integer.valueOf(page.getId().intValue()), retrieved.getId());
	}

	@Test
	public void testRetrieveRenderedPageByPath() throws EntityNotFoundException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		entityManager.flush();

		Page retrieved = wikiService.retrieveRenderedPageByPath(page.getPath());
		assertNotNull(retrieved);
		assertEquals(Integer.valueOf(page.getId().intValue()), retrieved.getId());

		assertNotNull(retrieved.getContent());
		assertTrue(retrieved.getContent().contains("<h1 id=\"Heading1\">Heading1</h1>"));
	}

	@Test
	public void testRetrieveRenderedPageByPathIsUpToDate() throws EntityNotFoundException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		page.getLastPageContent().setRendererVersion("INVALID");
		page.getLastPageContent().setContent("h1. RenderMe\n\ntest up to date");

		entityManager.flush();
		entityManager.clear();

		Page retrieved = wikiService.retrieveRenderedPageByPath(page.getPath());

		// System.out.println("retrieved: " + retrieved.getContent());

		// ensure that the page content is rendered correctly
		assertEquals("<h1 id=\"RenderMe\">RenderMe</h1><p>test up to date</p>", retrieved.getContent());

		// ensure that we've scheduled a job to render the content again
		assertEquals(1, jobService.getScheduledJobs().size());
		Job job = jobService.getScheduledJobs().get(0);
		assertTrue(job instanceof UpdatePageContentJob);

		entityManager.flush();
		entityManager.clear();

		// run the job
		job.execute(applicationContext);

		// ensure that the page content is in fact updated
		PageContent pageContent = entityManager.find(PageContent.class, page.getLastPageContent().getId());

		assertEquals(retrieved.getContent(), pageContent.getRenderedContent());
		assertFalse(pageContent.getRendererVersion().equals(page.getLastPageContent().getRenderedContent()));
	}

	@Test
	public void testFindByPath() {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		page.setPath("some path");

		QueryResult<Page> pages = wikiService.findPages("some", null);
		assertEquals(1, pages.getPageSize().intValue());
		assertEquals(new Integer(page.getId().intValue()), pages.getResultPage().get(0).getId());
		pages = wikiService.findPages("path", null);
		assertEquals(1, pages.getPageSize().intValue());
		assertEquals(new Integer(page.getId().intValue()), pages.getResultPage().get(0).getId());

		pages = wikiService.findPages("notfound", null);
		assertEquals(0, pages.getPageSize().intValue());
		assertEquals(0, pages.getResultPage().size());
	}

	@Test
	public void testFindByContent() {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		page.setPath("some path");
		page.getLastPageContent().setContent("text maybe markup");

		QueryResult<Page> pages = wikiService.findPages("text", null);
		assertEquals(1, pages.getPageSize().intValue());
		assertEquals(new Integer(page.getId().intValue()), pages.getResultPage().get(0).getId());
		pages = wikiService.findPages("maybe", null);
		assertEquals(1, pages.getPageSize().intValue());
		assertEquals(new Integer(page.getId().intValue()), pages.getResultPage().get(0).getId());

		pages = wikiService.findPages("notfound", null);
		assertEquals(0, pages.getPageSize().intValue());
		assertEquals(0, pages.getResultPage().size());
	}

	@Test
	public void testUpdatePage_NotFound() throws EntityNotFoundException, ValidationException,
			ConcurrentUpdateException {
		try {
			Page page = new Page();
			page.setId(12345);
			logon(person2);
			wikiService.updatePage(page);
			fail("expected exception");
		} catch (EntityNotFoundException e) {
			// expected
		}
	}

	@Test
	public void testUpdatePagePath() throws EntityNotFoundException, ValidationException, ConcurrentUpdateException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		entityManager.flush();

		Page retrieved = wikiService.retrievePage(page.getId().intValue());
		String originalPath = retrieved.getPath();
		String originalContent = retrieved.getContent();
		retrieved.setPath(originalPath + " 2");

		logon(person2);

		Page updated = wikiService.updatePage(retrieved);

		assertEquals(retrieved.getPath(), updated.getPath());
		assertEquals(page.getOriginalAuthor().getIdentity(), updated.getLastAuthor().getLoginName());
		assertEquals(originalContent, updated.getContent());

		updated.setContent(updated.getContent() + " 2");
	}

	@Test
	public void testUpdatePagePathRerendersContent() throws EntityNotFoundException, ValidationException,
			ConcurrentUpdateException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		page.getLastPageContent().setContent("{toc}\n\nh1. Heading");
		entityManager.flush();

		Page retrieved = wikiService.retrievePage(page.getId().intValue());
		final String originalPath = retrieved.getPath();
		retrieved.setPath(originalPath + " 2");

		logon(person2);

		Page updated = wikiService.updatePage(retrieved);

		Page rendered = wikiService.retrieveRenderedPageByPath(updated.getPath());

		assertTrue(rendered.getContent().contains("href=\"#projects/test/wiki/p/" + originalPath + "+2-Heading\""));
	}

	@Test
	public void testUpdatePageContent() throws EntityNotFoundException, ValidationException, ConcurrentUpdateException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		entityManager.flush();

		Page retrieved = wikiService.retrievePage(page.getId().intValue());

		logon(person2);

		retrieved.setContent(retrieved.getContent() + " 2");
		Page updated = wikiService.updatePage(retrieved);

		assertEquals(retrieved.getPath(), updated.getPath());
		assertEquals(person2.getLoginName(), updated.getLastAuthor().getLoginName());
		assertEquals(retrieved.getContent(), updated.getContent());
	}

	@Test
	public void testUpdatePage_ConcurrentUpdate() throws EntityNotFoundException, ValidationException,
			ConcurrentUpdateException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		entityManager.flush();

		Page retrieved = wikiService.retrievePage(page.getId().intValue());
		retrieved.setModificationDate(new Date(page.getModificationDate().getTime() + 2000L));
		logon(person2);

		try {
			wikiService.updatePage(retrieved);
			fail("expected exception");
		} catch (ConcurrentUpdateException e) {
			// expected
		}
	}

	@Test
	public void testUpdatePage_DuplicateName() throws EntityNotFoundException, ValidationException,
			ConcurrentUpdateException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page2 = MockPageFactory.create(entityManager);
		entityManager.flush();

		Page retrieved = wikiService.retrievePage(page.getId().intValue());
		retrieved.setPath(page2.getPath());
		try {
			wikiService.updatePage(retrieved);
			fail("expected exception");
		} catch (ValidationException e) {
			ValidationAssert.assertHaveValidationError(e, "nonUnique.path", retrieved.getPath());
		}
	}

	@Test
	public void testUpdatePage_RequiresLogon() throws Exception {
		try {
			Page page = new Page();
			page.setId(12345);
			wikiService.updatePage(page);
			fail("expected AuthenticationException");
		} catch (AuthenticationException e) {
			// expected
		} catch (AccessDeniedException e) {
			// expected
		}
	}

	@Test
	public void testRecentActivity() throws Exception {
		logon(author);

		Page wikiPage = new Page();
		wikiPage.setContent("abc 123\n456 ");
		wikiPage.setCreationDate(null);
		wikiPage.setPath("Foo Bar");

		Page created = wikiService.createPage(wikiPage);

		// it's evil, but we have to sleep here so that the database timestamps are different
		// resolution in the database is ~1s
		Thread.sleep(1500L);

		logon(person2);

		created.setContent(created.getContent() + " more");
		wikiService.updatePage(created);

		List<WikiActivity> recentActivity = wikiService.getRecentActivity(new Region(0, 10));
		assertNotNull(recentActivity);
		assertEquals(2, recentActivity.size());

		WikiActivity activity0 = recentActivity.get(0);
		assertEquals(WikiActivity.Type.UPDATED, activity0.getActivityType());
		assertEquals(created, activity0.getPage());
		assertEquals(person2.getLoginName(), activity0.getAuthor().getLoginName());
		assertNotNull(activity0.getActivityDate());
		assertNotNull(activity0.getPage().getUrl());

		WikiActivity activity1 = recentActivity.get(1);
		assertEquals(WikiActivity.Type.CREATED, activity1.getActivityType());
		assertEquals(created, activity1.getPage());
		assertEquals(author.getLoginName(), activity1.getAuthor().getLoginName());
		assertNotNull(activity1.getActivityDate());
		assertNotNull(activity1.getPage().getUrl());

		assertTrue(activity0.getActivityDate().after(activity1.getActivityDate()));
	}

	@Test
	public void testListAttachments() throws EntityNotFoundException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		List<com.tasktop.c2c.server.internal.wiki.server.domain.Attachment> attachments = MockAttachmentFactory.create(
				entityManager, page, 3);
		entityManager.flush();

		logon(author);

		List<Attachment> pageAttachments = wikiService.listAttachments(page.getId().intValue());
		assertEquals(attachments.size(), pageAttachments.size());
		for (Attachment attachment : pageAttachments) {
			assertNotNull(attachment.getId());
			assertNotNull(attachment.getName());
			assertNotNull(attachment.getMimeType());
			assertNotNull(attachment.getPage());
			assertNotNull(attachment.getUrl());
			assertNotNull(attachment.getCreationDate());
			assertNotNull(attachment.getOriginalAuthor());
			assertTrue(attachment.getContent() == null);
			assertTrue(attachment.getSize() > 0);
			assertEquals(page.getId().intValue(), attachment.getPage().getId().intValue());
		}
	}

	@Test
	public void testCreateAttachment() throws EntityNotFoundException, ValidationException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		entityManager.flush();

		logon(author);

		Attachment attachment = new Attachment();
		attachment.setContent(new byte[] { 1, 2, 3 });
		attachment.setMimeType("image/png");
		attachment.setName("foo.png");
		attachment.setPage(new PageHandle(page.getId().intValue()));
		wikiService.createAttachment(attachment);
	}

	@Test
	public void testCreateAttachment_DuplicateName() throws EntityNotFoundException, ValidationException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment = MockAttachmentFactory.create(
				entityManager, page);
		entityManager.flush();

		logon(author);

		Attachment attach = new Attachment();
		attach.setContent(new byte[] { 1, 2, 3 });
		attach.setMimeType("image/png");
		attach.setName(attachment.getName());
		attach.setPage(new PageHandle(page.getId().intValue()));
		try {
			wikiService.createAttachment(attach);
			fail("expected failure due to duplicate name");
		} catch (ValidationException e) {
			ValidationAssert.assertHaveValidationError(e, "nonUnique.name", attach.getName());
		}
	}

	@Test
	public void testRetrieveAttachment() throws Exception {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment = MockAttachmentFactory.create(
				entityManager, page);
		entityManager.flush();

		logon(author);

		AttachmentHandle attachmentHandle = new AttachmentHandle(attachment.getId().intValue(), new PageHandle(page
				.getId().intValue()));
		Attachment attach2 = wikiService.retrieveAttachment(attachmentHandle);

		assertEquals(attachmentHandle.getId(), attach2.getId());
		assertArrayEquals(attachment.getLastAttachmentContent().getContent(), attach2.getContent());
		assertEquals(attachment.getLastAuthor().getIdentity(), attach2.getLastAuthor().getLoginName());
	}

	@Test
	public void testRetrieveAttachmentByName() throws Exception {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment = MockAttachmentFactory.create(
				entityManager, page);
		entityManager.flush();

		logon(author);

		Attachment attach2 = wikiService.retrieveAttachmentByName(page.getId().intValue(), attachment.getName());

		assertEquals(Integer.valueOf(attachment.getId().intValue()), attach2.getId());
		assertArrayEquals(attachment.getLastAttachmentContent().getContent(), attach2.getContent());
		assertEquals(attachment.getLastAuthor().getIdentity(), attach2.getLastAuthor().getLoginName());
		assertNotNull(attach2.getEtag());
	}

	@Test
	public void testRetrieveAttachmentByNameETag() throws Exception {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment = MockAttachmentFactory.create(
				entityManager, page);
		entityManager.flush();

		logon(author);

		AttachmentHandle attachmentHandle = new AttachmentHandle(attachment.getId().intValue(), new PageHandle(page
				.getId().intValue()));
		Attachment attach = wikiService.retrieveAttachment(attachmentHandle);

		String etag = attach.getEtag();

		Attachment attach2 = wikiService.retrieveAttachmentByNameWithETag(page.getId().intValue(),
				attachment.getName(), etag);

		assertNull(attach2.getContent());

		attach2 = wikiService.retrieveAttachmentByNameWithETag(page.getId().intValue(), attachment.getName(), "asdf"
				+ etag);
		assertNotNull(attach2.getContent());

	}

	@Test
	public void testRetrieveAttachmentByNameETag_NULL() throws Exception {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment = MockAttachmentFactory.create(
				entityManager, page);
		entityManager.flush();

		logon(author);

		AttachmentHandle attachmentHandle = new AttachmentHandle(attachment.getId().intValue(), new PageHandle(page
				.getId().intValue()));
		Attachment attach = wikiService.retrieveAttachment(attachmentHandle);

		String etag = attach.getEtag();

		Attachment attach2 = wikiService.retrieveAttachmentByNameWithETag(page.getId().intValue(),
				attachment.getName(), null);

		assertNotNull(attach2.getContent());
		assertEquals(etag, attach2.getEtag());
	}

	@Test
	public void testUpdateAttachment() throws EntityNotFoundException, ValidationException, Exception {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment = MockAttachmentFactory.create(
				entityManager, page);
		entityManager.flush();

		logon(author);

		AttachmentHandle attachmentHandle = new AttachmentHandle(attachment.getId().intValue(), new PageHandle(page
				.getId().intValue()));
		Attachment attach = wikiService.retrieveAttachment(attachmentHandle);
		byte[] newContent = new byte[] { 5, 6 };
		attach.setContent(newContent);

		Thread.sleep(50L);

		wikiService.updateAttachment(attach);

		Attachment attach2 = wikiService.retrieveAttachment(attachmentHandle);

		assertEquals(attach.getId(), attach2.getId());
		assertArrayEquals(newContent, attach2.getContent());
		assertEquals(author.getLoginName(), attach2.getLastAuthor().getLoginName());
		assertTrue(attach.getModificationDate().before(attach2.getModificationDate()));
	}

	@Test
	public void testRetrieveOutlineByPath() throws EntityNotFoundException {
		logon(author);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = MockPageFactory.create(entityManager);
		page.getLastPageContent().setContent("h3. Item 1\n\n blah \n\nh3. Item 2 \n\n blah");
		entityManager.flush();

		PageOutline outline = wikiService.retrieveOutlineByPath(page.getPath());
		assertNotNull(outline);
		assertEquals(2, outline.getOutlineItems().size());
	}

	@Test
	public void testRenderPreview() throws EntityNotFoundException {
		String html = wikiService.renderPreview("foo/bar", "h1. HELLO WORLD");
		assertNotNull(html);
		assertTrue(html, html.contains("<h1"));
	}
}
