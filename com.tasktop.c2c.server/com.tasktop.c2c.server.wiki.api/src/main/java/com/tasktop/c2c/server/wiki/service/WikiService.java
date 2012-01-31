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
package com.tasktop.c2c.server.wiki.service;

import java.util.List;

import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.AttachmentHandle;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.domain.PageOutline;
import com.tasktop.c2c.server.wiki.domain.WikiActivity;

/**
 * Interface for interacting with the wiki {@link Page}s. This is a public service intended to be exposed over a REST
 * interface.
 * 
 * @author David Green <david.green@tasktop.com> (Tasktop Technologies Inc.)
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public interface WikiService {
	/**
	 * create a wiki page
	 * 
	 * @param wikiPage
	 *            the page to create
	 * @return the page as it was created
	 * @throws ValidationException
	 *             if the given page cannot be created because it is invalid
	 */
	public Page createPage(Page wikiPage) throws ValidationException;

	/**
	 * retrieve a wiki page by its id
	 * 
	 * @param pageId
	 *            the id of the page to retrieve
	 * @return the wiki page
	 * @throws EntityNotFoundException
	 *             if the given page id could not be found
	 */
	public Page retrievePage(Integer pageId) throws EntityNotFoundException;

	/**
	 * retrieve a wiki page by its path
	 * 
	 * @param path
	 *            the path of the page to retrieve
	 * @return the wiki page
	 * @throws EntityNotFoundException
	 *             if the given page could not be found
	 */
	public Page retrievePageByPath(String path) throws EntityNotFoundException;

	/**
	 * update a wiki page
	 * 
	 * @param wikiPage
	 *            the wiki page to update
	 * @return the wiki page as it was after updating
	 * @throws ValidationException
	 *             if the given page cannot be modified because it is invalid
	 * @throws EntityNotFoundException
	 *             if the given page id could not be found
	 */
	public Page updatePage(Page wikiPage) throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException;

	/**
	 * Delete a wiki page
	 * 
	 * @param pageId
	 *            the page id to update
	 * @throws EntityNotFoundException
	 *             if the given page id could not be found
	 */
	public void deletePage(Integer pageId) throws EntityNotFoundException;

	/**
	 * find pages that match the given search text
	 * 
	 * @param searchTerm
	 *            the search text, or null if all pages should be returned
	 * @param query
	 *            the query request, or null if the default query request should be used. The
	 *            {@link QueryRequest#getSortInfo() sort info} is ignored
	 * @return the query result, where returned pages are "thin", ie: they contain truncated {@link Page#getContent()
	 *         content}.
	 */
	public QueryResult<Page> findPages(String searchTerm, QueryRequest query);

	/**
	 * retrieve a wiki page by its path
	 * 
	 * @param path
	 *            the path of the page to retrieve
	 * @return the wiki page, with content rendered as HTML
	 * @throws EntityNotFoundException
	 *             if the given page could not be found
	 */
	public Page retrieveRenderedPageByPath(String path) throws EntityNotFoundException;

	/**
	 * get recent activity on the wiki
	 * 
	 * @return a list of recent activity, or an empty list if there is no activity
	 */
	public List<WikiActivity> getRecentActivity(Region region);

	/**
	 * list all attachments associated with the given page
	 * 
	 * @param pageId
	 *            the identity of the page
	 * @return a list of attachments, or an empty list if there are none
	 * @throws EntityNotFoundException
	 *             if the given page could not be found
	 */
	public List<Attachment> listAttachments(Integer pageId) throws EntityNotFoundException;

	/**
	 * retrieve a specific attachment
	 * 
	 * @param attachmentHandle
	 *            the attachment to retrieve
	 * @return the attachment
	 * @throws EntityNotFoundException
	 *             if the given attachment could not be found
	 */
	public Attachment retrieveAttachment(AttachmentHandle attachmentHandle) throws EntityNotFoundException;

	/**
	 * retrieve a specific attachment
	 * 
	 * @param pageId
	 *            the identity of the page
	 * @param name
	 *            the name of the attachment
	 * 
	 * @return the attachment
	 * @throws EntityNotFoundException
	 *             if the given attachment could not be found
	 */
	public Attachment retrieveAttachmentByName(Integer pageId, String name) throws EntityNotFoundException;

	/**
	 * retrieve a specific attachment with an ETag
	 * 
	 * @param pageId
	 *            the identity of the page
	 * @param name
	 *            the name of the attachment
	 * @param etag
	 *            the {@link Attachment#getETag() ETag}
	 * 
	 * @return the attachment. If the ETag matches, the returned attachment will have null content.
	 * @throws EntityNotFoundException
	 *             if the given attachment could not be found
	 * 
	 * @see http://en.wikipedia.org/wiki/HTTP_ETag
	 * @see Attachment#getETag()
	 */
	public Attachment retrieveAttachmentByNameWithETag(Integer pageId, String name, String etag)
			throws EntityNotFoundException;

	/**
	 * create an attachment
	 * 
	 * @param attachment
	 *            the attachment to create, must be associated with a page
	 * @return the handle to the new attachment
	 * @throws EntityNotFoundException
	 *             if the associated page could not be found
	 * @throws ValidationException
	 *             if the given attachment is not valid
	 */
	public AttachmentHandle createAttachment(Attachment attachment) throws EntityNotFoundException, ValidationException;

	/**
	 * modify an attachment
	 * 
	 * @param attachment
	 *            the attachment to modify, must be associated with a page
	 * @return the handle to the attachment
	 * @throws EntityNotFoundException
	 *             if the attachment could not be found
	 * @throws ValidationException
	 *             if the given attachment is not valid
	 */
	public AttachmentHandle updateAttachment(Attachment attachment) throws EntityNotFoundException, ValidationException;

	PageOutline retrieveOutlineByPath(String path) throws EntityNotFoundException;

	String renderPreview(String pagePath, String markup) throws EntityNotFoundException;

	void restorePage(Integer pageId) throws EntityNotFoundException;
}
