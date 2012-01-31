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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.HandlerMapping;

import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.doc.Documentation;
import com.tasktop.c2c.server.common.service.doc.Section;
import com.tasktop.c2c.server.common.service.doc.Title;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.web.AbstractRestService;
import com.tasktop.c2c.server.common.service.web.Error;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.AttachmentHandle;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.domain.PageHandle;
import com.tasktop.c2c.server.wiki.domain.PageOutline;
import com.tasktop.c2c.server.wiki.domain.WikiActivity;
import com.tasktop.c2c.server.wiki.service.SearchTermQueryArguments;
import com.tasktop.c2c.server.wiki.service.WikiService;

@Title("Wiki Service")
@Documentation("A wiki service for accessing, creating and modifying a project's wiki documentation."
		+ "The Wiki service methods are available by appending the URI to the base URL\n"
		+ "https://{hostname}/s/{projectIdentifier}/wiki + URI, for example: https://code.cloudfoundry.com/s/cf-code/wiki/page/Home")
@Controller
@Qualifier("webservice")
public class WikiServiceController extends AbstractRestService implements WikiService {

	@Qualifier("main")
	@Autowired
	private WikiService service;

	@Section(value = "Wiki Pages", order = 0)
	@Title("Create Page")
	@Documentation("Create a wiki page, specifying its path (title) and content.\n"
			+ "The provided path must be unique, and the page must have content.")
	@RequestMapping(value = "/page", method = RequestMethod.POST)
	@Override
	public Page createPage(@RequestBody Page wikiPage) throws ValidationException {
		return service.createPage(wikiPage);
	}

	@Section(value = "Wiki Pages", order = 0)
	@Title("Retrieve Page")
	@Documentation("Retrieve a page by its id. The returned page content is in its original wiki markup format.")
	@RequestMapping(value = "/page/{pageId}", method = RequestMethod.GET)
	@Override
	public Page retrievePage(@PathVariable(value = "pageId") Integer pageId) throws EntityNotFoundException {
		return service.retrievePage(pageId);
	}

	@Section(value = "Wiki Pages", order = 0)
	@Title("Retrieve Page By Path")
	@Documentation("Retrieve a page by its path. The returned page content is in its original wiki markup format.")
	@RequestMapping(value = "/page/path/**", method = RequestMethod.GET)
	public Page retrievePageByPath(HttpServletRequest request) throws EntityNotFoundException {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		return retrievePageByPath(path);
	}

	@Override
	public Page retrievePageByPath(String path) throws EntityNotFoundException {
		return service.retrievePageByPath(path);
	}

	@Section(value = "Wiki Pages", order = 0)
	@Title("Retrieve Rendered Page By Path")
	@Documentation("Retrieve a rendered page by its path. The returned page content is rendered in HTML format.")
	@RequestMapping(value = "/renderedPage/path/**", method = RequestMethod.GET)
	public Page retrieveRenderedPageByPath(HttpServletRequest request) throws EntityNotFoundException {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		return retrieveRenderedPageByPath(path);
	}

	@Override
	public PageOutline retrieveOutlineByPath(String path) throws EntityNotFoundException {
		return service.retrieveOutlineByPath(path);
	}

	@Section(value = "Wiki Pages", order = 0)
	@Title("Retrieve Page outline By Path")
	@Documentation("Retrieve an outline by its path.")
	@RequestMapping(value = "/outline/path/**", method = RequestMethod.GET)
	public PageOutline retrieveOutlineByPath(HttpServletRequest request) throws EntityNotFoundException {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		return retrieveOutlineByPath(path);
	}

	@Override
	public Page retrieveRenderedPageByPath(String path) throws EntityNotFoundException {
		return service.retrieveRenderedPageByPath(path);
	}

	@Section(value = "Wiki Pages", order = 0)
	@Title("Modify Page")
	@Documentation("Modify a page. The provided page must have an id.\n"
			+ "It is possible to change the page content and its path.")
	@RequestMapping(value = "/page/{pageId}", method = RequestMethod.POST)
	public Page updatePage(@PathVariable(value = "pageId") Integer pageId, @RequestBody Page wikiPage)
			throws ValidationException, EntityNotFoundException, ConcurrentUpdateException {
		if (pageId == null || !pageId.equals(wikiPage.getId())) {
			throw new EntityNotFoundException();
		}
		return updatePage(wikiPage);
	}

	@Override
	public Page updatePage(Page wikiPage) throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		return service.updatePage(wikiPage);
	}

	@Section(value = "Wiki Pages", order = 0)
	@Title("Delete Page")
	@Documentation("Delete a page by its id. ")
	@RequestMapping(value = "/page/{pageId}", method = RequestMethod.DELETE)
	public void deletePage(@PathVariable(value = "pageId") Integer pageId) throws EntityNotFoundException {
		if (pageId == null) {
			throw new EntityNotFoundException();
		}
		service.deletePage(pageId);
	}

	@Section(value = "Wiki Pages", order = 0)
	@Title("Find Pages")
	@Documentation("Find pages matching the given search term. Pages are matched on both their path and their content.")
	@RequestMapping(value = "/pages/search", method = RequestMethod.POST)
	public QueryResult<Page> findPages(@RequestBody SearchTermQueryArguments query) {
		return findPages(query.getSearchTerm(), query.getQueryRequest());
	}

	@Override
	public QueryResult<Page> findPages(String searchTerm, QueryRequest query) {
		return service.findPages(searchTerm, query);
	}

	@Section("Activity")
	@Title("Recent Activity")
	@Documentation("Retrieve recent activity. Activity is listed from the last modification in reverse chronological order.")
	@RequestMapping(value = "/recentActivity", method = RequestMethod.GET)
	public List<WikiActivity> getRecentActivity(@RequestParam(value = "offset", required = false) Integer offset,
			@RequestParam(value = "size", required = false) Integer size) {
		Region region = null;
		if (offset != null && size != null) {
			region = new Region(offset, size);
		}

		return service.getRecentActivity(region);
	}

	@Override
	public List<WikiActivity> getRecentActivity(Region region) {
		return service.getRecentActivity(region);
	}

	@Section("Attachments")
	@Title("List Page Attachments")
	@Documentation("List attachments associated with the given page. Attachments are thin, in that the attachment data is not provided.")
	@RequestMapping(value = "/page/{pageId}/attachments", method = RequestMethod.GET)
	@Override
	public List<Attachment> listAttachments(@PathVariable(value = "pageId") Integer pageId)
			throws EntityNotFoundException {
		return service.listAttachments(pageId);
	}

	@Section("Attachments")
	@Title("Retrieve Page Attachment")
	@Documentation("Retrieve an attachment by its id.")
	@RequestMapping(value = "/page/{pageId}/attachment/{attId}", method = RequestMethod.GET)
	public Attachment retrieveAttachment(@PathVariable(value = "pageId") Integer pageId,
			@PathVariable(value = "attId") Integer attachmentId) throws EntityNotFoundException {
		return retrieveAttachment(new AttachmentHandle(attachmentId, new PageHandle(pageId)));
	}

	@Override
	public Attachment retrieveAttachment(AttachmentHandle attachmentHandle) throws EntityNotFoundException {
		return service.retrieveAttachment(attachmentHandle);
	}

	@Section("Attachments")
	@Title("Retrieve Page Attachment By Name")
	@Documentation("Retrieve an attachment by its name.")
	@RequestMapping(value = "/page/{pageId}/attachmentByName/{attName:.*}", method = RequestMethod.GET)
	@Override
	public Attachment retrieveAttachmentByName(@PathVariable(value = "pageId") Integer pageId,
			@PathVariable(value = "attName") String name) throws EntityNotFoundException {
		return service.retrieveAttachmentByName(pageId, name);
	}

	@Section("Attachments")
	@Title("Retrieve Page Attachment By Name With ETag")
	@Documentation("Retrieve an attachment by its name.\n"
			+ "The provided etag is compared, and if it matches the attachment data is not included in the response.")
	@RequestMapping(value = "/page/{pageId}/attachmentByNameETag/{attName:.*}/{etag:.*}", method = RequestMethod.GET)
	@Override
	public Attachment retrieveAttachmentByNameWithETag(@PathVariable(value = "pageId") Integer pageId,
			@PathVariable(value = "attName") String name, @PathVariable(value = "etag") String etag)
			throws EntityNotFoundException {
		return service.retrieveAttachmentByNameWithETag(pageId, name, etag);
	}

	@Section("Attachments")
	@Title("Create Page Attachment")
	@Documentation("Create a page attachment. The provided attachment must have a name, mime type and content.")
	@RequestMapping(value = "/page/{pageId}/attachment", method = RequestMethod.POST)
	public AttachmentHandle createAttachment(@PathVariable(value = "pageId") Integer pageId,
			@RequestBody Attachment attachment) throws EntityNotFoundException, ValidationException {
		if (pageId == null || attachment.getPage() == null || !pageId.equals(attachment.getPage().getId())) {
			throw new EntityNotFoundException();
		}
		return createAttachment(attachment);
	}

	@Override
	public AttachmentHandle createAttachment(Attachment attachment) throws EntityNotFoundException, ValidationException {
		return service.createAttachment(attachment);
	}

	@Section("Attachments")
	@Title("Modify Page Attachment")
	@Documentation("Modify the content of a page attachment. The name and mime type of an attachment cannot be changed.")
	@RequestMapping(value = "/page/{pageId}/attachment/{attId}", method = RequestMethod.POST)
	public AttachmentHandle updateAttachment(@PathVariable(value = "pageId") Integer pageId,
			@PathVariable(value = "attId") Integer attachmentId, @RequestBody Attachment attachment)
			throws EntityNotFoundException, ValidationException {
		if (attachmentId == null || !attachmentId.equals(attachment.getId())) {
			throw new EntityNotFoundException();
		}
		return updateAttachment(attachment);
	}

	@Override
	public AttachmentHandle updateAttachment(Attachment attachment) throws EntityNotFoundException, ValidationException {
		return service.updateAttachment(attachment);
	}

	@RequestMapping(value = "{pageId}/attachment/{name:.*}", method = RequestMethod.GET)
	public void getImage(@PathVariable(value = "pageId") Integer pageId,
			@PathVariable(value = "name") String imageName, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String etag = request.getHeader("If-None-Match");
		if (etag != null && etag.length() > 1 && etag.charAt(0) == '"' && etag.charAt(etag.length() - 1) == '"') {
			etag = etag.substring(1, etag.length() - 1);
		}
		Attachment attachment;
		try {
			attachment = service.retrieveAttachmentByNameWithETag(pageId, imageName, etag);
		} catch (EntityNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (attachment.getContent() == null) {
			// ETag match
			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		String modified = formatRFC2822(attachment.getModificationDate());
		if (modified.equals(request.getHeader("If-Modified-Since"))) {
			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentLength(attachment.getSize());
		response.setContentType(attachment.getMimeType());
		response.setHeader("ETag", "\"" + attachment.getEtag() + "\"");
		response.setHeader("Modified", modified);

		ServletOutputStream outputStream = response.getOutputStream();
		try {
			outputStream.write(attachment.getContent());
		} finally {
			outputStream.close();
		}
	}

	private String formatRFC2822(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
		return format.format(date);
	}

	// HACK to override the exception handling an put a content type of plain-text. Required by the js parsing.
	static class ExceptionWrapper extends Exception {
		ExceptionWrapper(String msg, Throwable cause) {
			super(msg, cause);
		}
	}

	@Autowired
	private ObjectMapper jsonMapper;

	@ExceptionHandler(ExceptionWrapper.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void handleExceptionWrapperWithTextHtmlContent(ExceptionWrapper ex, HttpServletResponse response)
			throws JsonGenerationException, JsonMappingException, IOException {
		Error e;

		if (ex.getCause() instanceof ValidationException) {
			e = new Error((ValidationException) ex.getCause(), messageSource);
		} else {
			e = new Error(ex.getCause());
		}
		response.setContentType("text/html");
		response.getWriter().write(jsonMapper.writeValueAsString(Collections.singletonMap("error", e)));
	}

	@Secured({ Role.Community, Role.User, Role.Admin })
	@RequestMapping(value = "{pageId}/attachment", method = RequestMethod.POST)
	public void uploadAttachment(@PathVariable(value = "pageId") Integer pageId, HttpServletRequest request,
			HttpServletResponse response) throws IOException, FileUploadException, ExceptionWrapper {

		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<Attachment> attachments = new ArrayList<Attachment>();
		Map<String, String> formValues = new HashMap<String, String>();

		PageHandle pageHandle = new PageHandle(pageId);
		try {
			// find all existing attachments
			List<Attachment> allPageAttachments = service.listAttachments(pageId);
			// map them by name
			Map<String, Attachment> attachmentByName = new HashMap<String, Attachment>();
			for (Attachment attachment : allPageAttachments) {
				attachmentByName.put(attachment.getName(), attachment);
			}

			// inspect the request, getting all posted attachments
			@SuppressWarnings("unchecked")
			List<FileItem> items = upload.parseRequest(request);
			for (FileItem item : items) {

				if (item.isFormField()) {
					formValues.put(item.getFieldName(), item.getString());
				} else {
					Attachment attachment = new Attachment();
					attachment.setPage(pageHandle);
					attachment.setContent(item.get());
					attachment.setName(item.getName());
					attachment.setMimeType(item.getContentType());
					attachments.add(attachment);
				}
			}

			// for each new attachment, either create or update
			for (Attachment attachment : attachments) {
				Attachment attach = attachmentByName.get(attachment.getName());
				if (attach != null) {
					attachment.setId(attach.getId());
					service.updateAttachment(attachment);
				} else {
					service.createAttachment(attachment);
				}
			}

			// get content for response
			allPageAttachments = service.listAttachments(pageId);
			Page page = service.retrievePage(pageId);
			page.setContent(null);

			response.setContentType("text/html");
			response.getWriter().write(
					jsonMapper.writeValueAsString(Collections.singletonMap("uploadResult", new UploadResult(page,
							allPageAttachments))));
		} catch (ValidationException e) {
			throw new ExceptionWrapper(e.getMessage(), e);
		} catch (EntityNotFoundException e) {
			throw new ExceptionWrapper(e.getMessage(), e);

		}
	}

	@RequestMapping(value = "/page/preview/**", method = RequestMethod.POST)
	public Map<String, String> doRenderPreview(HttpServletRequest request, @RequestBody String markup)
			throws EntityNotFoundException {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

		return Collections.singletonMap("string", renderPreview(path, markup));
	}

	@Override
	public String renderPreview(String pagePath, String markup) throws EntityNotFoundException {
		return service.renderPreview(pagePath, markup);
	}

	@Section(value = "Wiki Pages", order = 0)
	@Title("Restore a deleted Page")
	@Documentation("Restore a page by its id. ")
	@RequestMapping(value = "/page/{pageId}/restore", method = RequestMethod.GET)
	@Override
	public void restorePage(@PathVariable("pageId") Integer pageId) throws EntityNotFoundException {
		if (pageId == null) {
			throw new EntityNotFoundException();
		}
		service.restorePage(pageId);
	}

}
