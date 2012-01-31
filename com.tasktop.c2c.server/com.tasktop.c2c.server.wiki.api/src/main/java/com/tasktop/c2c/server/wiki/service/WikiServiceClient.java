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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;

import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.WrappedCheckedException;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.web.AbstractRestServiceClient;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.AttachmentHandle;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.domain.PageOutline;
import com.tasktop.c2c.server.wiki.domain.WikiActivity;

/**
 * a client for communicating with a {@link WikiService} REST service.
 */
@Service
@Qualifier("webservice-client")
public class WikiServiceClient extends AbstractRestServiceClient implements WikiService {

	@SuppressWarnings("unused")
	private static class ServiceCallResult {
		private Page page;
		private QueryResult<Page> queryResult;
		private List<WikiActivity> wikiActivityList;
		private List<Attachment> attachmentList;
		private Attachment attachment;
		private AttachmentHandle attachmentHandle;
		private PageOutline pageOutline;
		private String string;

		public Page getPage() {
			return page;
		}

		public void setPage(Page page) {
			this.page = page;
		}

		public QueryResult<Page> getQueryResult() {
			return queryResult;
		}

		public void setQueryResult(QueryResult<Page> queryResult) {
			this.queryResult = queryResult;
		}

		public List<WikiActivity> getWikiActivityList() {
			return wikiActivityList;
		}

		public void setWikiActivityList(List<WikiActivity> wikiActivityList) {
			this.wikiActivityList = wikiActivityList;
		}

		public List<Attachment> getAttachmentList() {
			return attachmentList;
		}

		public void setAttachmentList(List<Attachment> attachmentList) {
			this.attachmentList = attachmentList;
		}

		public Attachment getAttachment() {
			return attachment;
		}

		public void setAttachment(Attachment attachment) {
			this.attachment = attachment;
		}

		public AttachmentHandle getAttachmentHandle() {
			return attachmentHandle;
		}

		public void setAttachmentHandle(AttachmentHandle attachmentHandle) {
			this.attachmentHandle = attachmentHandle;
		}

		/**
		 * @return the pageOutline
		 */
		public PageOutline getPageOutline() {
			return pageOutline;
		}

		/**
		 * @param pageOutline
		 *            the pageOutline to set
		 */
		public void setPageOutline(PageOutline pageOutline) {
			this.pageOutline = pageOutline;
		}

		/**
		 * @return the string
		 */
		public String getString() {
			return string;
		}

		/**
		 * @param string
		 *            the string to set
		 */
		public void setString(String string) {
			this.string = string;
		}

	}

	private abstract class GetCall<T> {

		public abstract T getValue(ServiceCallResult result);

		public T doCall(String urlStub, Object... variables) {
			ServiceCallResult callResult = template.getForObject(computeUrl(urlStub), ServiceCallResult.class,
					variables);

			return processResult(callResult);
		}

		private T processResult(ServiceCallResult callResult) {
			T retVal = getValue(callResult);

			if (retVal == null) {
				throw new IllegalStateException("Illegal result from call to wiki service");
			}

			return retVal;
		}
	}

	private abstract class PostCall<T> {

		public abstract T getValue(ServiceCallResult result);

		public T doCall(String urlStub, Object objToPost, Object... variables) {
			ServiceCallResult callResult = template.postForObject(computeUrl(urlStub), objToPost,
					ServiceCallResult.class, variables);

			T retVal = getValue(callResult);

			if (retVal == null) {
				throw new IllegalStateException("Illegal result from call to wiki service");
			}

			return retVal;
		}
	}

	// This ended up being much more involved than expected, due to constraints within the Spring RestTemplate - calling
	// template.delete() did not set an Accept: header, which meant that any exceptions which came back would cause
	// errors in the Spring filter pipeline and never arrive at the client. In order to set the Accept: content header,
	// it was necessary to mimic what RestTemplate does with GET and POST calls - that is why the below code was copied
	// from RestTemplate's getForObject() method and modified for our purposes.
	private class DeleteCall {

		public void doCall(String urlStub, Object... variables) {
			AcceptHeaderRequestCallback requestCallback = new AcceptHeaderRequestCallback(ServiceCallResult.class);
			HttpMessageConverterExtractor<ServiceCallResult> responseExtractor = new HttpMessageConverterExtractor<ServiceCallResult>(
					ServiceCallResult.class, template.getMessageConverters());
			template.execute(computeUrl(urlStub), HttpMethod.DELETE, requestCallback, responseExtractor, variables);
		}
	}

	// This class is copied from Spring RestTemplate, since the source class is private and thus inaccessible to this
	// code otherwise.
	private class AcceptHeaderRequestCallback implements RequestCallback {

		private final Class<?> responseType;

		private AcceptHeaderRequestCallback(Class<?> responseType) {
			this.responseType = responseType;
		}

		public void doWithRequest(ClientHttpRequest request) throws IOException {
			if (responseType != null) {
				List<MediaType> allSupportedMediaTypes = new ArrayList<MediaType>();
				for (HttpMessageConverter<?> messageConverter : template.getMessageConverters()) {
					if (messageConverter.canRead(responseType, null)) {
						List<MediaType> supportedMediaTypes = messageConverter.getSupportedMediaTypes();
						for (MediaType supportedMediaType : supportedMediaTypes) {
							if (supportedMediaType.getCharSet() != null) {
								supportedMediaType = new MediaType(supportedMediaType.getType(),
										supportedMediaType.getSubtype());
							}
							allSupportedMediaTypes.add(supportedMediaType);
						}
					}
				}
				if (!allSupportedMediaTypes.isEmpty()) {
					MediaType.sortBySpecificity(allSupportedMediaTypes);
					request.getHeaders().setAccept(allSupportedMediaTypes);
				}
			}
		}
	}

	public Page createPage(Page wikiPage) throws ValidationException {
		try {
			return new PostCall<Page>() {
				public Page getValue(ServiceCallResult result) {
					return result.getPage();
				}
			}.doCall("page", wikiPage);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			throw e;
		}
	}

	public Page retrievePage(Integer pageId) throws EntityNotFoundException {
		try {
			return new GetCall<Page>() {
				public Page getValue(ServiceCallResult result) {
					return result.getPage();
				}
			}.doCall("page/{pageId}", pageId);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public Page retrievePageByPath(String path) throws EntityNotFoundException {
		try {
			return new GetCall<Page>() {
				public Page getValue(ServiceCallResult result) {
					return result.getPage();
				}
			}.doCall("page/path/{path}", path);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public Page retrieveRenderedPageByPath(String path) throws EntityNotFoundException {
		try {
			return new GetCall<Page>() {
				public Page getValue(ServiceCallResult result) {
					return result.getPage();
				}
			}.doCall("renderedPage/path/{path}", path);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public PageOutline retrieveOutlineByPath(String path) throws EntityNotFoundException {
		try {
			return new GetCall<PageOutline>() {
				public PageOutline getValue(ServiceCallResult result) {
					return result.getPageOutline();
				}
			}.doCall("outline/path/{path}", path);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public Page updatePage(Page wikiPage) throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		try {
			return new PostCall<Page>() {
				public Page getValue(ServiceCallResult result) {
					return result.getPage();
				}
			}.doCall("page/{pageId}", wikiPage, String.valueOf(wikiPage.getId()));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);
			convertConcurrentUpdateException(e);
			throw e;
		}
	}

	public void deletePage(Integer wikiPageId) throws EntityNotFoundException {
		try {
			new DeleteCall() {

			}.doCall("page/{pageId}", String.valueOf(wikiPageId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public QueryResult<Page> findPages(String searchTerm, QueryRequest query) {
		return new PostCall<QueryResult<Page>>() {
			public QueryResult<Page> getValue(ServiceCallResult result) {
				return result.getQueryResult();
			}
		}.doCall("pages/search", new SearchTermQueryArguments(searchTerm, query));
	}

	public List<WikiActivity> getRecentActivity(Region region) {
		String urlParam = "";
		if (region != null) {
			urlParam = "?offset=" + region.getOffset() + "&size=" + region.getSize();
		}

		return new GetCall<List<WikiActivity>>() {
			public List<WikiActivity> getValue(ServiceCallResult result) {
				return result.getWikiActivityList();
			}
		}.doCall("recentActivity/" + urlParam);
	}

	public List<Attachment> listAttachments(Integer pageId) throws EntityNotFoundException {
		try {
			return new GetCall<List<Attachment>>() {

				public List<Attachment> getValue(ServiceCallResult result) {
					return result.getAttachmentList();
				}
			}.doCall("page/{pageId}/attachments", pageId);

		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public Attachment retrieveAttachment(AttachmentHandle attachmentHandle) throws EntityNotFoundException {
		try {
			return new GetCall<Attachment>() {

				public Attachment getValue(ServiceCallResult result) {
					return result.getAttachment();
				}
			}.doCall("page/{pageId}/attachment/{attId}", attachmentHandle.getPage().getId(), attachmentHandle.getId());
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public Attachment retrieveAttachmentByName(Integer pageId, String name) throws EntityNotFoundException {
		try {
			return new GetCall<Attachment>() {

				public Attachment getValue(ServiceCallResult result) {
					return result.getAttachment();
				}
			}.doCall("page/{pageId}/attachmentByName/{attName}", pageId, name);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public Attachment retrieveAttachmentByNameWithETag(Integer pageId, String name, String etag)
			throws EntityNotFoundException {
		if (etag == null || etag.trim().length() == 0) {
			return retrieveAttachmentByName(pageId, name);
		}
		try {
			return new GetCall<Attachment>() {

				public Attachment getValue(ServiceCallResult result) {
					return result.getAttachment();
				}
			}.doCall("page/{pageId}/attachmentByNameETag/{attName}/{etag}", pageId, name, etag);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public AttachmentHandle createAttachment(Attachment attachment) throws EntityNotFoundException, ValidationException {
		try {
			return new PostCall<AttachmentHandle>() {

				public AttachmentHandle getValue(ServiceCallResult result) {
					return result.getAttachmentHandle();
				}
			}.doCall("page/{pageId}/attachment", attachment, attachment.getPage().getId());
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			convertValidationException(e);
			throw e;
		}
	}

	public AttachmentHandle updateAttachment(Attachment attachment) throws EntityNotFoundException, ValidationException {
		try {
			return new PostCall<AttachmentHandle>() {

				public AttachmentHandle getValue(ServiceCallResult result) {
					return result.getAttachmentHandle();
				}
			}.doCall("page/{pageId}/attachment/{attId}", attachment, attachment.getPage().getId(), attachment.getId());
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			convertValidationException(e);
			throw e;
		}
	}

	public String renderPreview(String pagePath, String markup) throws EntityNotFoundException {
		try {
			return new PostCall<String>() {
				public String getValue(ServiceCallResult result) {
					return result.getString();
				}
			}.doCall("page/preview/{path}", markup, pagePath);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public void restorePage(Integer pageId) throws EntityNotFoundException {
		try {
			new GetCall<String>() {
				public String getValue(ServiceCallResult result) {
					return "NEVER USED";
				}
			}.doCall("page/{pageId}/restore", pageId);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}
}
