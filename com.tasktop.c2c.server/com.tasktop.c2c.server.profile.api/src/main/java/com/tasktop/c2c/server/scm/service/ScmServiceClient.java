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
package com.tasktop.c2c.server.scm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.WrappedCheckedException;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.web.AbstractRestServiceClient;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.scm.Commit;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;
import com.tasktop.c2c.server.profile.domain.scm.ScmSummary;

/**
 * A client to communicate to the scm REST webservice.
 */
@Service
public class ScmServiceClient extends AbstractRestServiceClient implements ScmService {

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
	// All of the setters in this method are used programmatically by the JSON serializer.
	private static class ServiceCallResult {
		private List<Commit> commitList;
		private List<CommitsForAuthor> commitsForAuthorList;
		private ScmRepository scmRepository;
		private List<ScmRepository> scmRepositoryList;
		private List<ScmSummary> scmSummaryList;

		public List<Commit> getCommitList() {
			return commitList;
		}

		public void setCommitList(List<Commit> commitList) {
			this.commitList = commitList;
		}

		public List<CommitsForAuthor> getCommitsForAuthorList() {
			return commitsForAuthorList;
		}

		public void setCommitsForAuthorList(List<CommitsForAuthor> commitsForAuthorList) {
			this.commitsForAuthorList = commitsForAuthorList;
		}

		public ScmRepository getScmRepository() {
			return scmRepository;
		}

		public void setScmRepository(ScmRepository scmRepo) {
			this.scmRepository = scmRepo;
		}

		public List<ScmRepository> getScmRepositoryList() {
			return scmRepositoryList;
		}

		public void setScmRepositoryList(List<ScmRepository> scmRepoSet) {
			this.scmRepositoryList = scmRepoSet;
		}

		public List<ScmSummary> getScmSummaryList() {
			return scmSummaryList;
		}

		public void setScmSummaryList(List<ScmSummary> scmSummaryList) {
			this.scmSummaryList = scmSummaryList;
		}
	}

	private abstract class GetCall<T> {

		public abstract T getValue(ServiceCallResult result);

		public T doCall(String urlStub, Object... variables) {
			ServiceCallResult callResult = template.getForObject(computeUrl(urlStub), ServiceCallResult.class,
					variables);

			T retVal = getValue(callResult);

			if (retVal == null) {
				throw new IllegalStateException("Illegal result from call to scmService");
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
				throw new IllegalStateException("Illegal result from call to scmService");
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

	public static final String GET_SCM_REPOSITORIES_URL = "repository";

	public List<ScmRepository> getScmRepositories() throws EntityNotFoundException {

		try {
			return new GetCall<List<ScmRepository>>() {
				public List<ScmRepository> getValue(ServiceCallResult result) {
					return result.getScmRepositoryList();
				}
			}.doCall(GET_SCM_REPOSITORIES_URL);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public static final String CREATE_SCM_REPOSITORY_URL = "repository";

	public ScmRepository createScmRepository(ScmRepository scmRepository) throws EntityNotFoundException,
			ValidationException {
		try {
			return new PostCall<ScmRepository>() {
				public ScmRepository getValue(ServiceCallResult result) {
					return result.getScmRepository();
				}
			}.doCall(CREATE_SCM_REPOSITORY_URL, scmRepository);
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			convertValidationException(e);
			throw e;
		}
	}

	public static final String REPOSITORY_ID_URLPARAM = "repositoryId";

	public static final String DELETE_SCM_REPOSITORY_URL = "repository/{" + REPOSITORY_ID_URLPARAM + "}";

	public void deleteScmRepository(Long scmRepoId) throws EntityNotFoundException {
		try {
			new DeleteCall().doCall(DELETE_SCM_REPOSITORY_URL, String.valueOf(scmRepoId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public static final String OFFSET_URL_PARAM = "offset";
	public static final String PAGESIZE_URL_PARAM = "pageSize";
	public static final String NUMDAYS_URL_PARAM = "numDays";

	public static final String GET_LOG_URL = "log";

	public List<Commit> getLog(Region region) {

		// Calculate the correct URL now.
		String url = GET_LOG_URL;
		if (region != null) {
			url = String.format("%s?%s=%s&%s=%s", url, OFFSET_URL_PARAM, region.getOffset(), PAGESIZE_URL_PARAM,
					region.getSize());
		}

		return new GetCall<List<Commit>>() {
			public List<Commit> getValue(ServiceCallResult result) {
				return result.getCommitList();
			}
		}.doCall(url);
	}

	public static final String GET_NUM_COMMITS_BY_AUTHOR_URL = "commitsByAuthor";

	public Map<Profile, Integer> getNumCommitsByAuthor(int numDays) {
		String url = String.format("%s?%s=%s", GET_NUM_COMMITS_BY_AUTHOR_URL, NUMDAYS_URL_PARAM, numDays);

		return toMap(new GetCall<List<CommitsForAuthor>>() {
			public List<CommitsForAuthor> getValue(ServiceCallResult result) {
				return result.getCommitsForAuthorList();
			}
		}.doCall(url));
	}

	private Map<Profile, Integer> toMap(List<CommitsForAuthor> commitsForAuthor) {
		Map<Profile, Integer> result = new HashMap<Profile, Integer>();
		for (CommitsForAuthor c : commitsForAuthor) {
			result.put(c.getAuthor(), c.getCount());
		}
		return result;
	}

	public static final String GET_SCM_SUMMARY_URL = "summary";

	public List<ScmSummary> getScmSummary(int numDays) {
		String url = String.format("%s?%s=%s", GET_SCM_SUMMARY_URL, NUMDAYS_URL_PARAM, numDays);

		return new GetCall<List<ScmSummary>>() {
			@Override
			public List<ScmSummary> getValue(ServiceCallResult result) {
				return result.getScmSummaryList();
			}
		}.doCall(url);
	}
}
