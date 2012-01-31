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
package com.tasktop.c2c.server.internal.wiki.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.common.service.Security;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.SortInfo.Order;
import com.tasktop.c2c.server.common.service.job.JobService;
import com.tasktop.c2c.server.internal.wiki.server.domain.AttachmentContent;
import com.tasktop.c2c.server.internal.wiki.server.domain.MarkupRenderer;
import com.tasktop.c2c.server.internal.wiki.server.domain.PageContent;
import com.tasktop.c2c.server.internal.wiki.server.domain.Person;
import com.tasktop.c2c.server.internal.wiki.server.domain.conversion.DomainConversionContext;
import com.tasktop.c2c.server.internal.wiki.server.domain.conversion.DomainConverter;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.AttachmentHandle;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.domain.Page.GroupAccess;
import com.tasktop.c2c.server.wiki.domain.PageHandle;
import com.tasktop.c2c.server.wiki.domain.PageOutline;
import com.tasktop.c2c.server.wiki.domain.WikiActivity;
import com.tasktop.c2c.server.wiki.service.WikiService;

/**
 * Implementation of {@link WikiService}. Domain objects are backed by JPA, and mylyn wikiText is used to render the
 * pages.
 * 
 * @author David Green <david.green@tasktop.com> (Tasktop Technologies Inc.)
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@Service("wikiService")
@Qualifier("main")
@Transactional(rollbackFor = { Exception.class })
public class WikiServiceBean extends AbstractJpaServiceBean implements WikiService {

	private static final int MAX_RESULTS = 500;
	@Autowired
	private DomainConverter domainConverter;

	@Autowired
	private MarkupRenderer markupRenderer;

	@Autowired
	private JobService jobService;

	@Secured({ Role.Community, Role.User, Role.Admin })
	@Override
	public Page createPage(Page wikiPage) throws ValidationException {
		validate(wikiPage);
		try {
			findPageByPath(wikiPage.getPath());
			Errors errors = createErrors(wikiPage);
			errors.rejectValue("path", "nonUnique", new Object[] { wikiPage.getPath() }, "path in use");
			throw new ValidationException(errors);
		} catch (EntityNotFoundException e) {
			// expected, this is good
		}
		Person author = getCurrentPerson();
		com.tasktop.c2c.server.internal.wiki.server.domain.Page managedPage = new com.tasktop.c2c.server.internal.wiki.server.domain.Page();
		managedPage.setOriginalAuthor(author);
		managedPage.setPath(wikiPage.getPath());
		if (wikiPage.getEditAccess() != null) {
			managedPage.setEditAccess(wikiPage.getEditAccess());
		}
		if (wikiPage.getDeleteAccess() != null) {
			managedPage.setDeleteAccess(wikiPage.getDeleteAccess());
		}
		PageContent pageContent = new PageContent();
		pageContent.setContent(wikiPage.getContent());
		pageContent.setAuthor(author);
		pageContent.setPage(managedPage);

		managedPage.addPageContent(pageContent);

		entityManager.persist(managedPage);
		entityManager.flush();

		markupRenderer.render(pageContent);

		return (Page) domainConverter.convert(managedPage, new DomainConversionContext(entityManager));
	}

	public Person getCurrentPerson() {
		AuthenticationServiceUser serviceUser = AuthenticationServiceUser.getCurrent();
		return serviceUser == null ? null : doProvisionAccount(serviceUser.getToken());
	}

	private Person doProvisionAccount(AuthenticationToken token) {

		// verify that a user exists for the provided credentials
		Query query = entityManager.createQuery("select e from " + Person.class.getSimpleName()
				+ " e where e.identity = :username");
		query.setParameter("username", token.getUsername());

		Person person;
		try {
			person = (Person) query.getSingleResult();
		} catch (NoResultException e) {
			person = new Person();
		}
		// propagate changes to user data such as name and email.
		person.setIdentity(token.getUsername());
		person.setName((token.getFirstName() + ' ' + token.getLastName()).trim());
		if (!entityManager.contains(person)) {
			entityManager.persist(person);
			entityManager.flush(); // Make id available
		}
		return person;
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public Page retrievePage(Integer pageId) throws EntityNotFoundException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = findPage(pageId);
		if (page.getDeleted()) {
			throw new EntityNotFoundException(String.format("Page with id %s is deleted", pageId));
		}
		return (Page) domainConverter.convert(page, new DomainConversionContext(entityManager));
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public Page retrievePageByPath(String path) throws EntityNotFoundException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = findPageByPath(path);

		return (Page) domainConverter.convert(page, new DomainConversionContext(entityManager));
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public Page retrieveRenderedPageByPath(String path) throws EntityNotFoundException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = findPageByPath(path);

		Page returnValue = (Page) domainConverter.convert(page, new DomainConversionContext(entityManager));

		PageContent lastPageContent = page.getLastPageContent();
		returnValue.setContent(computeRenderedContent(lastPageContent));

		return returnValue;
	}

	private String computeRenderedContent(PageContent pageContent) {
		if (!markupRenderer.isUpToDate(pageContent)) {
			entityManager.detach(pageContent.getPage());
			entityManager.detach(pageContent);
			markupRenderer.render(pageContent);

			jobService.schedule(new UpdatePageContentJob());
		}
		return pageContent.getRenderedContent();
	}

	private com.tasktop.c2c.server.internal.wiki.server.domain.Page findPageByPath(String path)
			throws EntityNotFoundException {
		if (path == null) {
			throw new EntityNotFoundException();
		}
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<com.tasktop.c2c.server.internal.wiki.server.domain.Page> pageQuery = criteriaBuilder
				.createQuery(com.tasktop.c2c.server.internal.wiki.server.domain.Page.class);
		Root<com.tasktop.c2c.server.internal.wiki.server.domain.Page> root = pageQuery
				.from(com.tasktop.c2c.server.internal.wiki.server.domain.Page.class);

		pageQuery.select(root).where(criteriaBuilder.equal(root.get("path"), path));
		try {
			com.tasktop.c2c.server.internal.wiki.server.domain.Page page = entityManager.createQuery(pageQuery)
					.getSingleResult();
			return page;
		} catch (NoResultException e) {
			throw new EntityNotFoundException(String.format("Page with path %s not found", path));
		}
	}

	private com.tasktop.c2c.server.internal.wiki.server.domain.Page findPage(Integer pageId)
			throws EntityNotFoundException {
		if (pageId == null) {
			throw new EntityNotFoundException();
		}
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = entityManager.find(
				com.tasktop.c2c.server.internal.wiki.server.domain.Page.class, pageId.longValue());
		if (page == null) {
			throw new EntityNotFoundException(String.format("Page with id %s not found", pageId));
		}
		return page;
	}

	private com.tasktop.c2c.server.internal.wiki.server.domain.Attachment findAttachmentByName(Integer pageId,
			String name) throws EntityNotFoundException {
		if (name == null || name.length() == 0) {
			throw new EntityNotFoundException();
		}

		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment;
		try {
			attachment = entityManager
					.createQuery(
							"select a from "
									+ com.tasktop.c2c.server.internal.wiki.server.domain.Attachment.class.getSimpleName()
									+ " a where a.name = :name and a.page.id = :pageId",
							com.tasktop.c2c.server.internal.wiki.server.domain.Attachment.class) //
					.setParameter("name", name) //
					.setParameter("pageId", pageId).getSingleResult();
		} catch (NoResultException e) {
			throw new EntityNotFoundException();
		}
		return attachment;
	}

	private com.tasktop.c2c.server.internal.wiki.server.domain.Attachment findAttachment(
			AttachmentHandle attachmentHandle) throws EntityNotFoundException {
		if (attachmentHandle == null || attachmentHandle.getId() == null) {
			throw new IllegalArgumentException("null is not a valid ID");
		}
		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment = entityManager.find(
				com.tasktop.c2c.server.internal.wiki.server.domain.Attachment.class, attachmentHandle.getId()
						.longValue());
		if (attachment == null) {
			throw new EntityNotFoundException(
					String.format("Attachment with id %s not found", attachmentHandle.getId()));
		}
		return attachment;
	}

	@Secured({ Role.Community, Role.User, Role.Admin })
	@Override
	public Page updatePage(Page wikiPage) throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = findPage(wikiPage.getId());
		if (!page.getModificationDate().equals(wikiPage.getModificationDate())) {
			throw new ConcurrentUpdateException();
		}
		checkPermissions(page.getEditAccess());
		// If we can not delete the page, then we are not allowed to edit the delete access.
		if (!hasPermission(page.getDeleteAccess()) && !page.getDeleteAccess().equals(wikiPage.getDeleteAccess())) {
			throw new InsufficientPermissionsException();
		}
		validate(wikiPage);

		try {
			com.tasktop.c2c.server.internal.wiki.server.domain.Page pageByPath = findPageByPath(wikiPage.getPath());
			if (pageByPath != null && !pageByPath.equals(page)) {
				Errors errors = createErrors(wikiPage);
				errors.rejectValue("path", "nonUnique", new Object[] { wikiPage.getPath() }, "path in use");
				throw new ValidationException(errors);
			}
		} catch (EntityNotFoundException e) {
			// expected, this is ok
		}

		final String originalPath = page.getPath();
		page.setPath(wikiPage.getPath());
		if (wikiPage.getEditAccess() != null) {
			page.setEditAccess(wikiPage.getEditAccess());
		}
		if (wikiPage.getDeleteAccess() != null) {
			page.setDeleteAccess(wikiPage.getDeleteAccess());
		}

		String newContent = wikiPage.getContent();
		if (!page.getLastPageContent().getContent().equals(newContent)) {
			Person author = getCurrentPerson();

			PageContent pageContent = new PageContent();
			pageContent.setAuthor(author);
			pageContent.setContent(newContent);

			page.addPageContent(pageContent);
			markupRenderer.render(pageContent);
		} else if (!originalPath.equals(page.getPath())) {
			markupRenderer.render(page.getLastPageContent());
		}

		return (Page) domainConverter.convert(page, new DomainConversionContext(entityManager));
	}

	private void checkPermissions(GroupAccess access) {
		if (!hasPermission(access)) {
			throw new InsufficientPermissionsException();
		}
	}

	private boolean hasPermission(GroupAccess access) {
		switch (access) {
		case MEMBER_AND_OWNERS:
			return Security.hasOneOfRoles(Role.Admin, Role.User);
		case OWNERS:
			return Security.hasOneOfRoles(Role.Admin);
		case ALL:
		default:// Check falls back to method annotation.
			return true;
		}
	}

	private static final String DELETED_CONTENT = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.wiki.service.WikiService#deletePage(java.lang.Integer)
	 */
	@Secured({ Role.Community, Role.User, Role.Admin })
	@Override
	public void deletePage(Integer pageId) throws EntityNotFoundException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = findPage(pageId);
		checkPermissions(page.getDeleteAccess());
		page.setDeleted(true);

		String newContent = DELETED_CONTENT;
		Person author = getCurrentPerson();
		PageContent pageContent = new PageContent();
		pageContent.setAuthor(author);
		pageContent.setContent(newContent);
		page.addPageContent(pageContent);
		markupRenderer.render(pageContent);
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public QueryResult<Page> findPages(String searchTerm, QueryRequest queryRequest) {
		if (searchTerm == null) {
			searchTerm = "";
		} else {
			searchTerm = searchTerm.trim();
		}
		boolean haveSearchTerm = searchTerm.length() > 0;
		String contentParameter = "%" + searchTerm + "%";
		queryRequest = computeQueryRequest(queryRequest);

		String queryText = " from " + com.tasktop.c2c.server.internal.wiki.server.domain.Page.class.getSimpleName()
				+ " e";
		queryText += " where e.deleted='false'";
		if (haveSearchTerm) {
			queryText += " AND (e.path like :content or e.lastPageContent.content like :content)";
		}
		String sortClause = String.format(" order by e.%s %s", queryRequest.getSortInfo().getSortField(), queryRequest
				.getSortInfo().getSortOrder() == Order.DESCENDING ? "desc" : "asc");
		TypedQuery<com.tasktop.c2c.server.internal.wiki.server.domain.Page> query = entityManager.createQuery(
				"select e " + queryText + sortClause, com.tasktop.c2c.server.internal.wiki.server.domain.Page.class);
		if (haveSearchTerm) {
			query.setParameter("content", contentParameter);
		}
		Region region = queryRequest.getPageInfo();

		query.setFirstResult(region.getOffset());
		query.setMaxResults(region.getSize());

		List<com.tasktop.c2c.server.internal.wiki.server.domain.Page> results = query.getResultList();

		List<Page> value = new ArrayList<Page>(results.size());

		DomainConversionContext conversionContext = new DomainConversionContext(entityManager);
		// conversionContext.setThin(true); //task 2147 - need full results to show a snippet content

		for (com.tasktop.c2c.server.internal.wiki.server.domain.Page page : results) {
			value.add((Page) domainConverter.convert(page, conversionContext));
		}

		Query countQuery = entityManager.createQuery("select count(distinct(e)) " + queryText);
		if (haveSearchTerm) {
			countQuery.setParameter("content", contentParameter);
		}
		Long count = (Long) countQuery.getSingleResult();

		return new QueryResult<Page>(new Region(region.getOffset(), value.size()), value, count.intValue());
	}

	private QueryRequest computeQueryRequest(QueryRequest queryRequest) {
		if (queryRequest == null) {
			queryRequest = new QueryRequest();
		}
		if (queryRequest.getPageInfo() == null) {
			queryRequest.setPageInfo(new Region(0, 50));
		}
		if (queryRequest.getPageInfo().getSize() > MAX_RESULTS) {
			queryRequest.getPageInfo().setSize(MAX_RESULTS);
		}
		if (queryRequest.getSortInfo() == null) {
			queryRequest.setSortInfo(new SortInfo());
		}
		if (queryRequest.getSortInfo().getSortField() == null) {
			queryRequest.getSortInfo().setSortField("path");
		}
		if (queryRequest.getSortInfo().getSortOrder() == null) {
			queryRequest.getSortInfo().setSortOrder(Order.ASCENDING);
		}
		return queryRequest;
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public List<WikiActivity> getRecentActivity(Region region) {

		DomainConversionContext conversionContext = new DomainConversionContext(entityManager);
		conversionContext.setThin(true);

		Query query = entityManager.createQuery("select pc from " + PageContent.class.getSimpleName()
				+ " pc order by pc.creationDate DESC");
		if (region != null) {
			query.setFirstResult(region.getOffset());
			query.setMaxResults(region.getSize());
		}

		List<WikiActivity> activity = new ArrayList<WikiActivity>();

		@SuppressWarnings("unchecked")
		List<PageContent> resultList = query.getResultList();
		for (PageContent pageContent : resultList) {
			com.tasktop.c2c.server.internal.wiki.server.domain.Page page = pageContent.getPage();
			Person author = pageContent.getAuthor();
			Date modificationDate = pageContent.getModificationDate();

			WikiActivity wikiActivity = new WikiActivity();
			wikiActivity.setActivityDate(modificationDate);
			wikiActivity.setAuthor((com.tasktop.c2c.server.wiki.domain.Person) domainConverter.convert(author,
					conversionContext));
			wikiActivity.setPage((Page) domainConverter.convert(page, conversionContext));

			if (pageContent.equals(page.getPageContent().get(0))) {
				wikiActivity.setActivityType(WikiActivity.Type.CREATED);
			} else if (pageContent.getContent().equals(DELETED_CONTENT)) {
				wikiActivity.setActivityType(WikiActivity.Type.DELETED);
			} else {
				wikiActivity.setActivityType(WikiActivity.Type.UPDATED);
			}

			activity.add(wikiActivity);
		}

		return activity;
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public List<Attachment> listAttachments(Integer pageId) throws EntityNotFoundException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = findPage(pageId);
		List<Attachment> attachments = new ArrayList<Attachment>();
		if (!page.getAttachments().isEmpty()) {
			DomainConversionContext conversionContext = new DomainConversionContext(entityManager);
			conversionContext.setThin(true);

			for (com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment : page.getAttachments()) {
				attachments.add((Attachment) domainConverter.convert(attachment, conversionContext));
			}
		}
		return attachments;
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public Attachment retrieveAttachment(AttachmentHandle attachmentHandle) throws EntityNotFoundException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment = findAttachment(attachmentHandle);

		DomainConversionContext conversionContext = new DomainConversionContext(entityManager);
		return (Attachment) domainConverter.convert(attachment, conversionContext);
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public Attachment retrieveAttachmentByName(Integer pageId, String name) throws EntityNotFoundException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment attachment = findAttachmentByName(pageId, name);
		DomainConversionContext conversionContext = new DomainConversionContext(entityManager);
		return (Attachment) domainConverter.convert(attachment, conversionContext);
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public Attachment retrieveAttachmentByNameWithETag(Integer pageId, String name, String etag)
			throws EntityNotFoundException {
		Attachment attachment = retrieveAttachmentByName(pageId, name);
		if (attachment.getEtag().equals(etag)) {
			attachment.setContent(null);
		}
		return attachment;
	}

	@Secured({ Role.Community, Role.User, Role.Admin })
	@Override
	public AttachmentHandle createAttachment(Attachment attachment) throws ValidationException, EntityNotFoundException {
		validate(attachment);

		com.tasktop.c2c.server.internal.wiki.server.domain.Page managedPage = findPage(attachment.getPage().getId());

		try {
			findAttachmentByName(attachment.getPage().getId(), attachment.getName());
			Errors errors = createErrors(attachment);
			errors.rejectValue("name", "nonUnique", new Object[] { attachment.getName() }, "name in use");
			throw new ValidationException(errors);
		} catch (EntityNotFoundException e) {
			// expected, this is good
		}

		if (attachment.getContent() != null) {
			attachment.setSize(attachment.getContent().length);
		}
		Person author = getCurrentPerson();
		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment managedAttachment = new com.tasktop.c2c.server.internal.wiki.server.domain.Attachment();
		managedAttachment.setOriginalAuthor(author);
		managedAttachment.setMimeType(attachment.getMimeType());
		managedAttachment.setName(attachment.getName());

		managedAttachment.setPage(managedPage);
		managedPage.getAttachments().add(managedAttachment);
		managedPage.setModificationDate(new Date());

		saveAttachmentData(attachment, managedAttachment, author);

		entityManager.persist(managedAttachment);
		entityManager.flush();

		AttachmentHandle attachmentHandle = new AttachmentHandle(managedAttachment.getId().intValue(), new PageHandle(
				managedPage.getId().intValue()));
		return attachmentHandle;
	}

	private AttachmentContent saveAttachmentData(Attachment attachment,
			com.tasktop.c2c.server.internal.wiki.server.domain.Attachment managedAttachment, Person author) {
		AttachmentContent attachmentContent = new AttachmentContent();
		attachmentContent.setAuthor(author);
		attachmentContent.setContent(attachment.getContent());
		attachmentContent.setSize(attachment.getSize());

		managedAttachment.addAttachmentContent(attachmentContent);

		return attachmentContent;
	}

	@Secured({ Role.Community, Role.User, Role.Admin })
	@Override
	public AttachmentHandle updateAttachment(Attachment attachment) throws ValidationException, EntityNotFoundException {
		validate(attachment);

		com.tasktop.c2c.server.internal.wiki.server.domain.Attachment managedAttachment = findAttachment(attachment);
		if (attachment.getContent() != null) {
			attachment.setSize(attachment.getContent().length);
		}
		Person author = getCurrentPerson();

		saveAttachmentData(attachment, managedAttachment, author);

		entityManager.persist(managedAttachment);
		entityManager.flush();

		AttachmentHandle attachmentHandle = new AttachmentHandle(managedAttachment.getId().intValue(), new PageHandle(
				managedAttachment.getPage().getId().intValue()));
		return attachmentHandle;
	}

	@Secured({ Role.Observer, Role.User })
	@Override
	public PageOutline retrieveOutlineByPath(String path) throws EntityNotFoundException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = findPageByPath(path);
		if (page.getDeleted()) {
			throw new EntityNotFoundException(String.format("Page with path %s is deleted", path));
		}

		PageContent lastPageContent = page.getLastPageContent();
		return computeOutline(lastPageContent);
	}

	// Review should we cache this?
	private PageOutline computeOutline(PageContent pageContent) {
		return markupRenderer.renderOutline(pageContent);
	}

	@Override
	public String renderPreview(String pagePath, String markup) throws EntityNotFoundException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page;
		try {
			page = findPageByPath(pagePath);
		} catch (EntityNotFoundException e) {
			page = new com.tasktop.c2c.server.internal.wiki.server.domain.Page();
			// page.setMarkupLanguage(markupLanguage)
			page.setId(-1l);
			page.setPath(pagePath);
		}

		return markupRenderer.render(page, markup);
	}

	@Override
	public void restorePage(Integer pageId) throws EntityNotFoundException {
		com.tasktop.c2c.server.internal.wiki.server.domain.Page page = findPage(pageId);
		checkPermissions(page.getDeleteAccess());
		page.setDeleted(false);

		String newContent = page.getPageContent().get(page.getPageContent().size() - 2).getContent();
		Person author = getCurrentPerson();
		PageContent pageContent = new PageContent();
		pageContent.setAuthor(author);
		pageContent.setContent(newContent);
		page.addPageContent(pageContent);
		markupRenderer.render(pageContent);

	}

}
