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
package com.tasktop.c2c.server.tasks.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.web.AbstractRestServiceClient;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.AttachmentHandle;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.QuerySpec;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskActivity;
import com.tasktop.c2c.server.tasks.domain.TaskHandle;
import com.tasktop.c2c.server.tasks.domain.TaskSummary;
import com.tasktop.c2c.server.tasks.domain.Team;

/**
 * A client to communicate to the task REST webservice.
 */
@Service
@Qualifier("webservice-client")
public class TaskServiceClient extends AbstractRestServiceClient implements TaskService {

	@SuppressWarnings("unused")
	// All of the setters in this method are used programmatically by the JSON serializer.
	private static class ServiceCallResult {
		private TaskSummary taskSummary;
		private QueryResult<Task> queryResult;
		private Task task;
		private Attachment attachment;
		private List<TaskSummary> taskSummaryList;
		private List<TaskActivity> taskActivityList;

		private RepositoryConfiguration repositoryConfiguration;
		private AttachmentHandle attachmentHandle;

		private Product product;
		private List<Product> productList;

		private Component component;
		private List<Component> componentList;

		private List<Keyword> keywordList;
		private Keyword keyword;
		private SavedTaskQuery savedTaskQuery;
		private List<SavedTaskQuery> savedTaskQueryList;
		private Iteration iteration;
		private FieldDescriptor fieldDescriptor;
		private String htmlString;

		public TaskSummary getTaskSummary() {
			return taskSummary;
		}

		public void setTaskSummary(TaskSummary taskSummary) {
			this.taskSummary = taskSummary;
		}

		public QueryResult<Task> getQueryResult() {
			return queryResult;
		}

		public void setQueryResult(QueryResult<Task> queryResult) {
			this.queryResult = queryResult;
		}

		public Task getTask() {
			return task;
		}

		public void setTask(Task task) {
			this.task = task;
		}

		public RepositoryConfiguration getRepositoryConfiguration() {
			return repositoryConfiguration;
		}

		public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
			this.repositoryConfiguration = repositoryConfiguration;
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

		public void setTaskSummaryList(List<TaskSummary> taskSummaryList) {
			this.taskSummaryList = taskSummaryList;
		}

		public List<TaskSummary> getTaskSummaryList() {
			return this.taskSummaryList;
		}

		public List<TaskActivity> getTaskActivityList() {
			return taskActivityList;
		}

		public void setTaskActivityList(List<TaskActivity> taskActivityList) {
			this.taskActivityList = taskActivityList;
		}

		public void setProduct(Product product) {
			this.product = product;
		}

		public Product getProduct() {
			return product;
		}

		public void setProductList(List<Product> productList) {
			this.productList = productList;
		}

		public List<Product> getProductList() {
			return productList;
		}

		public Component getComponent() {
			return component;
		}

		public void setComponent(Component component) {
			this.component = component;
		}

		public List<Component> getComponentList() {
			return componentList;
		}

		public void setComponentList(List<Component> componentList) {
			this.componentList = componentList;
		}

		public void setKeywordList(List<Keyword> keywordList) {
			this.keywordList = keywordList;
		}

		public List<Keyword> getKeywordList() {
			return keywordList;
		}

		public Keyword getKeyword() {
			return keyword;
		}

		public void setKeyword(Keyword keyword) {
			this.keyword = keyword;
		}

		/**
		 * @return the savedTaskQuery
		 */
		public SavedTaskQuery getSavedTaskQuery() {
			return savedTaskQuery;
		}

		/**
		 * @param savedTaskQuery
		 *            the savedTaskQuery to set
		 */
		public void setSavedTaskQuery(SavedTaskQuery savedTaskQuery) {
			this.savedTaskQuery = savedTaskQuery;
		}

		/**
		 * @return the savedTaskQueryList
		 */
		public List<SavedTaskQuery> getSavedTaskQueryList() {
			return savedTaskQueryList;
		}

		/**
		 * @param savedTaskQueryList
		 *            the savedTaskQueryList to set
		 */
		public void setSavedTaskQueryList(List<SavedTaskQuery> savedTaskQueryList) {
			this.savedTaskQueryList = savedTaskQueryList;
		}

		/**
		 * @return the iteration
		 */
		public Iteration getIteration() {
			return iteration;
		}

		/**
		 * @param iteration
		 *            the iteration to set
		 */
		public void setIteration(Iteration iteration) {
			this.iteration = iteration;
		}

		/**
		 * @return the fieldDescriptor
		 */
		public FieldDescriptor getFieldDescriptor() {
			return fieldDescriptor;
		}

		/**
		 * @param fieldDescriptor
		 *            the fieldDescriptor to set
		 */
		public void setFieldDescriptor(FieldDescriptor fieldDescriptor) {
			this.fieldDescriptor = fieldDescriptor;
		}

		/**
		 * @return the htmlString
		 */
		public String getString() {
			return htmlString;
		}

		/**
		 * @param htmlString
		 *            the htmlString to set
		 */
		public void setString(String htmlString) {
			this.htmlString = htmlString;
		}
	}

	private abstract class GetCall<T> {

		public abstract T getValue(ServiceCallResult result);

		public T doCall(String urlStub, Object... variables) {
			ServiceCallResult callResult = template.getForObject(computeUrl(urlStub), ServiceCallResult.class,
					variables);

			T retVal = getValue(callResult);

			if (retVal == null) {
				throw new IllegalStateException("Illegal result from call to taskService");
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
				throw new IllegalStateException("Illegal result from call to taskService");
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

	public TaskSummary getTaskSummary() {

		return new GetCall<TaskSummary>() {

			public TaskSummary getValue(ServiceCallResult result) {
				return result.getTaskSummary();
			}
		}.doCall("summary");
	}

	public Task createTask(Task task) throws ValidationException, EntityNotFoundException {

		try {
			return new PostCall<Task>() {

				public Task getValue(ServiceCallResult result) {
					return result.getTask();
				}
			}.doCall("task", task);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public QueryResult<Task> findTasksWithQuery(PredefinedTaskQuery predefinedQuery, QuerySpec querySpec) {
		PredefinedQueryArguments args = new PredefinedQueryArguments(predefinedQuery, querySpec);

		return new PostCall<QueryResult<Task>>() {

			public QueryResult<Task> getValue(ServiceCallResult result) {
				return result.getQueryResult();
			}
		}.doCall("findTasksWithQuery", args);
	}

	public QueryResult<Task> findTasks(String searchTerm, QuerySpec querySpec) {
		SearchTermQueryArguments args = new SearchTermQueryArguments(searchTerm, querySpec);

		return new PostCall<QueryResult<Task>>() {

			public QueryResult<Task> getValue(ServiceCallResult result) {
				return result.getQueryResult();
			}
		}.doCall("findTasks", args);
	}

	public QueryResult<Task> findTasksWithCriteria(Criteria criteria, QuerySpec querySpec) {
		CriteriaQueryArguments args = new CriteriaQueryArguments(criteria, querySpec);

		return new PostCall<QueryResult<Task>>() {

			public QueryResult<Task> getValue(ServiceCallResult result) {
				return result.getQueryResult();
			}
		}.doCall("findTasksWithCriteria", args);
	}

	public QueryResult<Task> findTaskSummariesWithCriteria(Criteria criteria, QuerySpec querySpec) {
		CriteriaQueryArguments args = new CriteriaQueryArguments(criteria, querySpec);

		return new PostCall<QueryResult<Task>>() {

			public QueryResult<Task> getValue(ServiceCallResult result) {
				return result.getQueryResult();
			}
		}.doCall("findTaskSummariesWithCriteria", args);
	}

	public Task updateTask(Task task) throws ValidationException, EntityNotFoundException, ConcurrentUpdateException {

		try {
			return new PostCall<Task>() {

				public Task getValue(ServiceCallResult result) {
					return result.getTask();
				}
			}.doCall("task/{taskId}", task, String.valueOf(task.getId()));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);
			convertConcurrentUpdateException(e);

			throw e;
		}
	}

	public Task retrieveTask(Integer taskId) throws EntityNotFoundException {
		try {
			return new GetCall<Task>() {

				public Task getValue(ServiceCallResult result) {
					return result.getTask();
				}
			}.doCall("task/{taskId}", String.valueOf(taskId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public RepositoryConfiguration getRepositoryContext() {
		return new GetCall<RepositoryConfiguration>() {

			public RepositoryConfiguration getValue(ServiceCallResult result) {
				return result.getRepositoryConfiguration();
			}
		}.doCall("repositoryContext");
	}

	public Attachment retrieveAttachment(Integer attachmentId) throws EntityNotFoundException {
		try {
			return new GetCall<Attachment>() {

				public Attachment getValue(ServiceCallResult result) {
					return result.getAttachment();
				}
			}.doCall("attachment/{attachmentId}/full", String.valueOf(attachmentId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public AttachmentHandle saveAttachment(TaskHandle taskHandle, Attachment attachment) throws ValidationException,
			EntityNotFoundException, ConcurrentUpdateException {

		try {
			return this.saveAttachment(taskHandle, attachment, null);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);
			convertConcurrentUpdateException(e);

			throw e;
		}
	}

	public AttachmentHandle saveAttachment(TaskHandle taskHandle, Attachment attachment, Comment comment)
			throws ValidationException, EntityNotFoundException, ConcurrentUpdateException {
		SaveAttachmentArguments args = new SaveAttachmentArguments(taskHandle, attachment, comment);

		try {
			return new PostCall<AttachmentHandle>() {

				public AttachmentHandle getValue(ServiceCallResult result) {
					return result.getAttachmentHandle();
				}
			}.doCall("attachment", args);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);
			convertConcurrentUpdateException(e);

			throw e;
		}
	}

	public List<TaskSummary> getHistoricalSummary(int numDays) {
		return new GetCall<List<TaskSummary>>() {

			public List<TaskSummary> getValue(ServiceCallResult result) {
				return result.getTaskSummaryList();
			}
		}.doCall("summary/{numDays}", String.valueOf(numDays));
	}

	public List<TaskActivity> getRecentActivity(Region region) {
		String urlParam = "";
		if (region != null) {
			urlParam = "?offset=" + region.getOffset() + "&size=" + region.getSize();
		}
		return new GetCall<List<TaskActivity>>() {

			public List<TaskActivity> getValue(ServiceCallResult result) {
				return result.getTaskActivityList();
			}
		}.doCall("activity/" + urlParam);
	}

	public java.util.List<TaskActivity> listActivity(java.util.Date date) {
		return new GetCall<List<TaskActivity>>() {

			public List<TaskActivity> getValue(ServiceCallResult result) {
				return result.getTaskActivityList();
			}
		}.doCall("activity/{date}", formatDate(date));
	}

	private String formatDate(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

	public List<TaskActivity> listTaskActivity(Integer taskId) throws EntityNotFoundException {
		try {
			return new GetCall<List<TaskActivity>>() {

				public List<TaskActivity> getValue(ServiceCallResult result) {
					return result.getTaskActivityList();
				}
			}.doCall("task/{taskId}/activity", String.valueOf(taskId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public Product createProduct(Product product) throws ValidationException {
		try {
			return new PostCall<Product>() {

				public Product getValue(ServiceCallResult result) {
					return result.getProduct();
				}
			}.doCall("task/product", product);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);

			throw e;
		}
	}

	public Product retrieveProduct(Integer productId) throws EntityNotFoundException {
		try {
			return new GetCall<Product>() {

				public Product getValue(ServiceCallResult result) {
					return result.getProduct();
				}
			}.doCall("task/product/{productId}", String.valueOf(productId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public List<Product> listAllProducts() {
		return new GetCall<List<Product>>() {

			public List<Product> getValue(ServiceCallResult result) {
				return result.getProductList();
			}
		}.doCall("task/product");
	}

	public void deleteProduct(Integer productId) throws ValidationException, EntityNotFoundException {
		try {
			new DeleteCall().doCall("task/product/{productId}", String.valueOf(productId));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public Product updateProduct(Product product) throws ValidationException, EntityNotFoundException {
		try {
			return new PostCall<Product>() {

				public Product getValue(ServiceCallResult result) {
					return result.getProduct();
				}
			}.doCall("task/product/{productId}", product, String.valueOf(product.getId()));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public Product updateProductTree(Product product) throws ValidationException, EntityNotFoundException {
		try {
			return new PostCall<Product>() {

				public Product getValue(ServiceCallResult result) {
					return result.getProduct();
				}
			}.doCall("task/product-tree/{productId}", product, String.valueOf(product.getId()));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public Component createComponent(Component newComponent) throws ValidationException {
		try {
			return new PostCall<Component>() {

				public Component getValue(ServiceCallResult result) {
					return result.getComponent();
				}
			}.doCall("task/component", newComponent);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);

			throw e;
		}
	}

	public Component retrieveComponent(Integer componentId) throws EntityNotFoundException {
		try {
			return new GetCall<Component>() {

				public Component getValue(ServiceCallResult result) {
					return result.getComponent();
				}
			}.doCall("task/component/{componentId}", String.valueOf(componentId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public List<Component> listAllComponents() {
		return new GetCall<List<Component>>() {

			public List<Component> getValue(ServiceCallResult result) {
				return result.getComponentList();
			}
		}.doCall("task/component");
	}

	public void deleteComponent(Integer componentId) throws ValidationException, EntityNotFoundException {
		try {
			new DeleteCall().doCall("task/component/{componentId}", String.valueOf(componentId));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public Component updateComponent(Component component) throws ValidationException, EntityNotFoundException {
		try {
			return new PostCall<Component>() {

				public Component getValue(ServiceCallResult result) {
					return result.getComponent();
				}
			}.doCall("task/component/{componentId}", component, String.valueOf(component.getId()));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public void deleteMilestone(Integer milestoneId) throws ValidationException, EntityNotFoundException {

		try {
			new DeleteCall().doCall("task/milestone/{milestoneId}", String.valueOf(milestoneId));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public void replicateTeam(Team team) {
		new PostCall<Object>() {
			public Object getValue(ServiceCallResult result) {
				return new Object();
			}
		}.doCall("team", team);
	}

	public List<Keyword> listAllKeywords() {
		return new GetCall<List<Keyword>>() {

			public List<Keyword> getValue(ServiceCallResult result) {
				return result.getKeywordList();
			}
		}.doCall("keywords");
	}

	public Keyword createKeyword(Keyword keyword) throws ValidationException {
		try {
			return new PostCall<Keyword>() {

				@Override
				public Keyword getValue(ServiceCallResult result) {
					return result.getKeyword();
				}

			}.doCall("keywords", keyword);

		} catch (WrappedCheckedException e) {
			convertValidationException(e);

			throw e;
		}
	}

	public Keyword updateKeyword(Keyword keyword) throws ValidationException, EntityNotFoundException {
		try {
			return new PostCall<Keyword>() {
				@Override
				public Keyword getValue(ServiceCallResult result) {
					return result.getKeyword();
				}
			}.doCall("keywords/{keywordId}", keyword, keyword.getId());

		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public void deleteKeyword(Integer keywordId) throws EntityNotFoundException, ValidationException {
		try {
			new DeleteCall().doCall("keywords/{keywordId}", String.valueOf(keywordId));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);

			throw e;
		}
	}

	public SavedTaskQuery createQuery(SavedTaskQuery query) throws ValidationException {
		try {
			return new PostCall<SavedTaskQuery>() {

				public SavedTaskQuery getValue(ServiceCallResult result) {
					return result.getSavedTaskQuery();
				}
			}.doCall("task/query", query);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			throw e;
		}
	}

	public SavedTaskQuery updateQuery(SavedTaskQuery query) throws ValidationException, EntityNotFoundException {
		try {
			return new PostCall<SavedTaskQuery>() {

				public SavedTaskQuery getValue(ServiceCallResult result) {
					return result.getSavedTaskQuery();
				}
			}.doCall("task/query/{queryId}", query, String.valueOf(query.getId()));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public void deleteQuery(Integer queryId) throws EntityNotFoundException, ValidationException {
		try {
			new DeleteCall().doCall("task/query/{queryId}", String.valueOf(queryId));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public Iteration createIteration(Iteration iteration) throws ValidationException {
		try {
			return new PostCall<Iteration>() {

				public Iteration getValue(ServiceCallResult result) {
					return result.getIteration();
				}
			}.doCall("task/iteration", iteration);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			throw e;
		}
	}

	public Iteration updateIteration(Iteration iteration) throws EntityNotFoundException, ValidationException {
		try {
			return new PostCall<Iteration>() {

				public Iteration getValue(ServiceCallResult result) {
					return result.getIteration();
				}
			}.doCall("task/iteration/{iterationId}", iteration, String.valueOf(iteration.getId()));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public FieldDescriptor createCustomField(FieldDescriptor customField) throws ValidationException {
		try {
			return new PostCall<FieldDescriptor>() {

				public FieldDescriptor getValue(ServiceCallResult result) {
					return result.getFieldDescriptor();
				}
			}.doCall("task/customfield", customField);
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			throw e;
		}
	}

	public FieldDescriptor updateCustomField(FieldDescriptor customField) throws ValidationException {
		try {
			return new PostCall<FieldDescriptor>() {

				public FieldDescriptor getValue(ServiceCallResult result) {
					return result.getFieldDescriptor();
				}
			}.doCall("task/customfield/{fieldName}", customField, String.valueOf(customField.getName()));
		} catch (WrappedCheckedException e) {
			convertValidationException(e);
			throw e;
		}
	}

	public void deleteCustomField(Integer customFieldId) throws EntityNotFoundException {
		try {
			new DeleteCall().doCall("task/customfield/{fieldId}", String.valueOf(customFieldId));
		} catch (WrappedCheckedException e) {
			convertEntityNotFoundException(e);
			throw e;
		}
	}

	public String renderWikiMarkupAsHtml(String markup) {
		// Map s = template.postForObject(computeUrl("wikimarkup"), markup, Map.class);
		// return null;
		return new PostCall<String>() {

			@Override
			public String getValue(ServiceCallResult result) {
				return result.getString();
			}
		}.doCall("wikimarkup", markup);

	}
}
