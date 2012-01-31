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

import java.util.Date;
import java.util.List;

import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.SortInfo.Order;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
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
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;
import com.tasktop.c2c.server.tasks.domain.TaskHandle;
import com.tasktop.c2c.server.tasks.domain.TaskSummary;
import com.tasktop.c2c.server.tasks.domain.Team;
import com.tasktop.c2c.server.tasks.domain.validation.KeywordValidator;

/**
 * Interface for interacting with the {@link Task}s.
 * 
 * @author David Green <david.green@tasktop.com> (Tasktop Technologies Inc.)
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * @author Lucas Panjer <lucas.panjer@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public interface TaskService {

	static Region DEFAULT_PAGE_INFO = new Region(0, 20);
	static SortInfo DEFAULT_SORT_INFO = new SortInfo(TaskFieldConstants.PRIORITY_FIELD, Order.DESCENDING);

	/**
	 * Create a task.
	 * 
	 * @param task
	 *            to create
	 * @return created task
	 */
	Task createTask(Task task) throws ValidationException, EntityNotFoundException;

	/**
	 * Update a task. This will create new comments and attachments, but not remove those left out of the update call.
	 * 
	 * @param task
	 *            to update
	 * @return updated
	 * @throws ValidationException
	 * @throws EntityNotFoundException
	 */
	Task updateTask(Task task) throws ValidationException, EntityNotFoundException, ConcurrentUpdateException;

	/**
	 * Retrieve a task.
	 * 
	 * @param taskId
	 * @return task with the given id
	 * @throws EntityNotFoundException
	 */
	Task retrieveTask(Integer taskId) throws EntityNotFoundException;

	/**
	 * Find tasks by a predefined query.
	 * 
	 * @param predefinedQuery
	 * @param pageInfo
	 *            can be null, defaults
	 * @param sortInfo
	 *            can be null, defaults
	 * @return
	 */
	QueryResult<Task> findTasksWithQuery(PredefinedTaskQuery predefinedQuery, QuerySpec query);

	/**
	 * Find tasks by a search term.
	 * 
	 * @param searchTerm
	 * @param pageInfo
	 *            can be null, defaults
	 * @param sortInfo
	 *            can be null, defaults
	 * @return
	 */
	QueryResult<Task> findTasks(String searchTerm, QuerySpec query);

	/**
	 * Find tasks by a user defined criteria.
	 * 
	 * @param criteria
	 * @param pageInfo
	 *            can be null, defaults
	 * @param sortInfo
	 *            can be null, defaults
	 * @return
	 */
	QueryResult<Task> findTasksWithCriteria(Criteria criteria, QuerySpec query);

	/**
	 * Find tasks by a user defined criteria. This method call returns thin Tasks, which only contain enough data to
	 * display in a summary list.
	 * 
	 * @param criteria
	 * @param pageInfo
	 *            can be null, defaults
	 * @param sortInfo
	 *            can be null, defaults
	 * @return
	 */
	QueryResult<Task> findTaskSummariesWithCriteria(Criteria criteria, QuerySpec query);

	/**
	 * Get the configuration of the task repository.
	 * 
	 * @return
	 */
	RepositoryConfiguration getRepositoryContext();

	/**
	 * Get a summary of the tasks.
	 * 
	 * @return
	 */
	TaskSummary getTaskSummary();

	/**
	 * Retrieve the full attachment w/data.
	 * 
	 * @param attachmentId
	 * @return
	 * @throws EntityNotFoundException
	 */
	Attachment retrieveAttachment(Integer attachmentId) throws EntityNotFoundException;

	/**
	 * Save an attachment to a task. This method will not perform optimistic locking checks and is therefore
	 * discouraged. Use updateTask with a new attachment instead.
	 * 
	 * @param taskHandle
	 * @param attachment
	 * @return attachmentHandle. This contains a taskHandle which should be set into the client's task after successful
	 *         save.
	 * @throws ValidationException
	 * @throws EntityNotFoundException
	 */
	AttachmentHandle saveAttachment(TaskHandle taskHandle, Attachment attachment) throws ValidationException,
			EntityNotFoundException, ConcurrentUpdateException;

	/**
	 * Save an attachment to a task. This method will not perform optimistic locking checks and is therefore
	 * discouraged. Use updateTask with a new attachment instead.
	 * 
	 * @param taskHandle
	 * @param attachment
	 * @param comment
	 * @return attachmentHandle. This contains a taskHandle which should be set into the client's task after successful
	 *         save.
	 * @throws ValidationException
	 * @throws EntityNotFoundException
	 */
	AttachmentHandle saveAttachment(TaskHandle taskHandle, Attachment attachment, Comment comment)
			throws ValidationException, EntityNotFoundException, ConcurrentUpdateException;

	List<TaskSummary> getHistoricalSummary(int numDays);

	List<TaskActivity> getRecentActivity(Region region);

	/**
	 * get activity that occurred on or after the given date
	 * 
	 * @param date
	 *            the date for which activity is
	 * @return activity on or after the given date, or an empty list if there is no activity
	 */
	List<TaskActivity> listActivity(Date date);

	/**
	 * list all activity for a given task
	 * 
	 * @param taskId
	 *            the id of the task for which activity should be listed
	 * @return activity for the task
	 * @throws EntityNotFoundException
	 */
	List<TaskActivity> listTaskActivity(Integer taskId) throws EntityNotFoundException;

	/**
	 * Create a product, with default milestone, no components.
	 * 
	 * @param product
	 * @return the product
	 * @throws ValidationException
	 */
	Product createProduct(Product product) throws ValidationException;

	/**
	 * Retrieve a product.
	 * 
	 * @param productId
	 * @return
	 * @throws EntityNotFoundException
	 */
	Product retrieveProduct(Integer productId) throws EntityNotFoundException;

	/**
	 * Retrieve all products.
	 * 
	 * @return products
	 */
	List<Product> listAllProducts();

	/**
	 * Update a product, does not update it's child components, versions, or milestones.
	 * 
	 * @param product
	 * @return
	 * @throws ValidationException
	 * @throws EntityNotFoundException
	 */
	Product updateProduct(Product product) throws ValidationException, EntityNotFoundException;

	/**
	 * Deletes the given product from the system, or throws an exception if deletion is not possible at this time.
	 * 
	 * @param productId
	 *            the ID of the product to be deleted
	 * @throws ValidationException
	 *             if it was not possible to delete this product due to a constraint in the system, e.g. it currently
	 *             has tasks assigned to it.
	 * @throws EntityNotFoundException
	 *             if productId did not refer to a valid Product
	 */
	void deleteProduct(Integer productId) throws ValidationException, EntityNotFoundException;

	/**
	 * Update a products, including child components and milestones.
	 * 
	 * @param product
	 * @return
	 * @throws ValidationException
	 * @throws EntityNotFoundException
	 */
	Product updateProductTree(Product product) throws ValidationException, EntityNotFoundException;

	/**
	 * This will create a new Component which has the requested values inside the system, and return the created
	 * Component with all fields populated (e.g. the newly-generated ID).
	 * 
	 * @param newComponent
	 *            The Component which is to be created
	 * @return the created Component with all fields populated (e.g. the newly-generated ID).
	 * @throws ValidationException
	 *             if the given Component contained some sort of invalid data which prevented its creation.
	 */
	Component createComponent(Component newComponent) throws ValidationException;

	/**
	 * This retrieves the Component with the given ID from the system, and returns it to the caller.
	 * 
	 * @param componentId
	 *            The ID of the component to be returned
	 * @return the Component with the given ID - If no component with the given ID was found or if the ID was null, an
	 *         EntityNotFoundException is thrown.
	 * @throws EntityNotFoundException
	 *             if no component with the given ID was found.
	 */
	Component retrieveComponent(Integer componentId) throws EntityNotFoundException;

	/**
	 * This updates the given Component with the given values.
	 * 
	 * @param component
	 *            the Component containing the changes to be made. The ID of the given Component is used to retrieve the
	 *            system's version of the component.
	 * @return the Component which is now present in the system after application of the changes contained within the
	 *         given Component
	 * @throws ValidationException
	 *             if the given Component contained some sort of invalid data which prevented its update.
	 * @throws EntityNotFoundException
	 *             if no component with the given ID was found.
	 */
	Component updateComponent(Component component) throws ValidationException, EntityNotFoundException;

	/**
	 * This gets a List of all of the Components in the system.
	 * 
	 * @return a List of all of the Components in the system.
	 */
	List<Component> listAllComponents();

	/**
	 * Deletes the given component from the system, or throws an exception if deletion is not possible at this time.
	 * 
	 * @param componentId
	 *            the ID of the component to be deleted
	 * @throws ValidationException
	 *             if it was not possible to delete this component due to a constraint in the system, e.g. it currently
	 *             has tasks assigned to it.
	 * @throws EntityNotFoundException
	 *             if componentId did not refer to a valid Component
	 */
	void deleteComponent(Integer componentId) throws ValidationException, EntityNotFoundException;

	/**
	 * Deletes the given milestone from the system, or throws an exception if deletion is not possible at this time.
	 * 
	 * @param milestoneId
	 *            the ID of the milestone to be deleted
	 * @throws ValidationException
	 *             if it was not possible to delete this milestone due to a constraint in the system, e.g. it currently
	 *             has tasks assigned to it.
	 * @throws EntityNotFoundException
	 *             if milestoneId did not refer to a valid Milestone
	 */
	void deleteMilestone(Integer milestoneId) throws ValidationException, EntityNotFoundException;

	/**
	 * replicate a team (ie: a set of users)
	 */
	void replicateTeam(Team team);

	/**
	 * Get a list of all keywords
	 * 
	 * @return the keywords
	 */
	List<Keyword> listAllKeywords();

	/**
	 * Create a new keyword.
	 * 
	 * @param keyword
	 *            with a null keywordId
	 * @return the created keyword
	 * @throws ValidationException
	 *             if the keyword fails to validate using {@link KeywordValidator}
	 */
	Keyword createKeyword(Keyword keyword) throws ValidationException;

	/**
	 * Update or create a keyword.
	 * 
	 * @param keyword
	 *            with a keywordId
	 * @return
	 * @throws ValidationException
	 *             if the keyword fails to validate using {@link KeywordValidator}
	 * @throws EntityNotFoundException
	 */
	Keyword updateKeyword(Keyword keyword) throws ValidationException, EntityNotFoundException;

	/**
	 * Delete a keyword.
	 * 
	 * @param keywordId
	 * @throws EntityNotFoundException
	 *             if the keyword cannot be found
	 * @throws ValidationException
	 *             if the keyword is in use on any tasks
	 */
	void deleteKeyword(Integer keywordId) throws EntityNotFoundException, ValidationException;

	/**
	 * @param query
	 * @return
	 * @throws ValidationException
	 */
	SavedTaskQuery createQuery(SavedTaskQuery query) throws ValidationException;

	/**
	 * @param query
	 * @return
	 * @throws ValidationException
	 * @throws EntityNotFoundException
	 */
	SavedTaskQuery updateQuery(SavedTaskQuery query) throws ValidationException, EntityNotFoundException;

	/**
	 * @param queryId
	 * @throws ValidationException
	 * @throws EntityNotFoundException
	 */
	void deleteQuery(Integer queryId) throws ValidationException, EntityNotFoundException;

	Iteration createIteration(Iteration iteration) throws ValidationException;

	Iteration updateIteration(Iteration iteration) throws ValidationException, EntityNotFoundException;

	FieldDescriptor createCustomField(FieldDescriptor customField) throws ValidationException;

	FieldDescriptor updateCustomField(FieldDescriptor customField) throws ValidationException, EntityNotFoundException;

	void deleteCustomField(Integer customFieldId) throws EntityNotFoundException;

	/**
	 * 
	 * @param markup
	 * @return rendered html
	 */
	String renderWikiMarkupAsHtml(String markup);
}
