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
package com.tasktop.c2c.server.tasks.web.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.CsvWriter;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.doc.Documentation;
import com.tasktop.c2c.server.common.service.doc.Exclude;
import com.tasktop.c2c.server.common.service.doc.Section;
import com.tasktop.c2c.server.common.service.doc.Title;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaParser;
import com.tasktop.c2c.server.common.service.web.AbstractRestService;
import com.tasktop.c2c.server.tasks.domain.AbstractDomainObject;
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
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.Team;
import com.tasktop.c2c.server.tasks.service.CriteriaQueryArguments;
import com.tasktop.c2c.server.tasks.service.PredefinedQueryArguments;
import com.tasktop.c2c.server.tasks.service.SaveAttachmentArguments;
import com.tasktop.c2c.server.tasks.service.SearchTermQueryArguments;
import com.tasktop.c2c.server.tasks.service.TaskService;

@Title("Task Service")
@Documentation("A task service for managing the tasks database for a Code2Cloud project.\n"
		+ "The task service methods are available by appending the URI to the base URL\n"
		+ "https://{hostname}/s/{projectIdentifier}/tasks + URI, for example: https://code.cloudfoundry.com/s/cf-code/tasks/task/1")
@Controller
@Qualifier("webservice")
public class TaskServiceController extends AbstractRestService implements TaskService {

	@Autowired
	private ObjectMapper jsonMapper;

	@Qualifier("main")
	@Autowired
	private TaskService service;

	@Title("Task Summary")
	@Documentation("Provides a summary of open and closed tasks by severity.")
	@Section(value = "Reporting", order = 20)
	@RequestMapping(value = "/summary", method = RequestMethod.GET)
	@Override
	public TaskSummary getTaskSummary() {
		return service.getTaskSummary();
	}

	@Title("Task Summary, Historical")
	@Documentation("Provides a summary of open and closed tasks by severity over the specified period.")
	@Section(value = "Reporting", order = 20)
	@RequestMapping(value = "/summary/{numDays}", method = RequestMethod.GET)
	@Override
	public List<TaskSummary> getHistoricalSummary(
			@PathVariable("numDays") @Documentation("The period, provided as the number of historical days to include.") int numDays) {
		return service.getHistoricalSummary(numDays);
	}

	@Title("Task Activity")
	@Documentation("Provides recent activity to tasks, such as changed attributes, comments, task creation, etc.  Activity is provided in reverse chronological order.")
	@Section(value = "Reporting", order = 20)
	@RequestMapping(value = "/activity/", method = RequestMethod.GET)
	public List<TaskActivity> getRecentActivity(
			@RequestParam(value = "offset", required = false) @Documentation("the zero-based offset") Integer offset,
			@RequestParam(value = "size", required = false) @Documentation("the number of activity items to provide") Integer size) {
		Region region = null;
		if (offset != null && size != null) {
			region = new Region(offset, size);
		}
		return service.getRecentActivity(region);
	}

	@Override
	public List<TaskActivity> getRecentActivity(Region region) {
		return service.getRecentActivity(region);
	}

	@Title("Task Activity By Date")
	@Documentation("Provides recent activity to tasks, such as changed attributes, comments, task creation, etc.  Provides activity that has occurred on or after the specified date.  Activity is provided in reverse chronological order.")
	@Section(value = "Reporting", order = 20)
	@RequestMapping(value = "/activity/{date}", method = RequestMethod.GET)
	public List<TaskActivity> listActivity(
			@PathVariable(value = "date") @Documentation("the date formatted as 'yyyy-MM-dd'") String dateString) {
		Date date;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
		} catch (ParseException e) {
			throw new IllegalArgumentException(dateString);
		}
		return listActivity(date);
	}

	@Override
	public List<TaskActivity> listActivity(Date date) {
		return service.listActivity(date);
	}

	@Title("Task Activity, single task")
	@Documentation("Provides all of the task activity for a specific task.  Activity is provided in reverse chronological order.")
	@Section(value = "Reporting", order = 20)
	@RequestMapping(value = "/task/{taskId}/activity", method = RequestMethod.GET)
	@Override
	public List<TaskActivity> listTaskActivity(
			@PathVariable(value = "taskId") @Documentation("the task id") Integer taskId)
			throws EntityNotFoundException {
		return service.listTaskActivity(taskId);
	}

	@Title("Find Tasks with Predefined Query")
	@Documentation("Find tasks that match a predefined query.  Predefined query must be one of:\n"
			+ "* MINE - assigned to the current user\n" //
			+ "* RELATED - created, commented, or watched by the current user\n" //
			+ "* RECENT - recently changed tasks\n" //
			+ "* OPEN - all open tasks\n" //
			+ "* ALL - all tasks\n")
	@Section(value = "Search", order = 10)
	@RequestMapping(value = "/findTasksWithQuery", method = RequestMethod.POST)
	public QueryResult<Task> findTasksWithQuery(@RequestBody PredefinedQueryArguments args) {
		return this.findTasksWithQuery(args.getPredefinedTaskQuery(), args.getQuerySpec());
	}

	@Override
	public QueryResult<Task> findTasksWithQuery(PredefinedTaskQuery predefinedQuery, QuerySpec query) {
		return service.findTasksWithQuery(predefinedQuery, query);
	}

	@Title("Find Tasks with Search Term")
	@Documentation("Find tasks that match a given search term.  The provided search term is matched against task id, summary, milestone, comment or description.")
	@Section(value = "Search", order = 10)
	@RequestMapping(value = "/findTasks", method = RequestMethod.POST)
	public QueryResult<Task> findTasks(@RequestBody SearchTermQueryArguments args) {
		return this.findTasks(args.getSearchTerm(), args.getQuerySpec());
	}

	@Override
	public QueryResult<Task> findTasks(String searchTerm, QuerySpec query) {
		return service.findTasks(searchTerm, query);
	}

	@Title("Find Tasks with Criteria")
	@Documentation("Find tasks that match a given search criteria. Criteria fields must be one of the following:\n"
			+ "* \"TaskId\"\n" //
			+ "* \"tasktype\"\n" //
			+ "* \"iteration\"\n" //
			+ "* \"Summary\"\n" //
			+ "* \"Description\"\n" //
			+ "* \"Comment\"\n" //
			+ "* \"Status\"\n" //
			+ "* \"Priority\"\n" //
			+ "* \"Severity\"\n" //
			+ "* \"Resolution\"\n" //
			+ "* \"Assignee\"\n" //
			+ "* \"Reporter\"\n" //
			+ "* \"Watcher\"\n" //
			+ "* \"CreationTime\"\n" //
			+ "* \"UpdateTime\"\n" //
			+ "* \"Product\"\n" //
			+ "* \"Component\"\n" //
			+ "* \"ProductName\"\n" //
			+ "* \"ComponentName\"\n" //
			+ "* \"Release\"\n" //
			+ "* \"CommentAuthor\"\n" //
			+ "* \"Keywords\"\n")
	@Section(value = "Search", order = 10)
	@RequestMapping(value = "/findTasksWithCriteria", method = RequestMethod.POST)
	public QueryResult<Task> findTasksWithCriteria(@RequestBody CriteriaQueryArguments args) {
		return this.findTasksWithCriteria(args.getCriteria(), args.getQuerySpec());
	}

	@Override
	public QueryResult<Task> findTasksWithCriteria(Criteria criteria, QuerySpec query) {
		return service.findTasksWithCriteria(criteria, query);
	}

	@Exclude
	@RequestMapping(value = "/findTaskSummariesWithCriteria", method = RequestMethod.POST)
	public QueryResult<Task> findTaskSummariesWithCriteria(@RequestBody CriteriaQueryArguments args) {
		return this.findTaskSummariesWithCriteria(args.getCriteria(), args.getQuerySpec());
	}

	@Override
	public QueryResult<Task> findTaskSummariesWithCriteria(Criteria criteria, QuerySpec query) {
		return service.findTaskSummariesWithCriteria(criteria, query);
	}

	@Title("Create Task")
	@Documentation("Create a new task")
	@Section(value = "Tasks", order = 1)
	@RequestMapping(value = "/task", method = RequestMethod.POST)
	@Override
	public Task createTask(@RequestBody Task task) throws ValidationException, EntityNotFoundException {
		return service.createTask(task);
	}

	@Title("Modify Task")
	@Documentation("Modify a task. The provided task must have an id.")
	@Section(value = "Tasks", order = 1)
	@RequestMapping(value = "/task/{taskId}", method = RequestMethod.POST)
	@Override
	public Task updateTask(@RequestBody Task task) throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		return service.updateTask(task);
	}

	@Title("Retrieve Task")
	@Documentation("Retrieve a task by its id.")
	@Section(value = "Tasks", order = 1)
	@RequestMapping(value = "/task/{taskId}", method = RequestMethod.GET)
	@Override
	public Task retrieveTask(@PathVariable(value = "taskId") Integer taskId) throws EntityNotFoundException {
		return service.retrieveTask(taskId);
	}

	@Title("Retrieve Repository Configuration")
	@Documentation("Retrieve the repository configuration, which provides valid values, custom fields, task types, resolutions, etc.")
	@Section("Configuration")
	@RequestMapping(value = "/repositoryContext", method = RequestMethod.GET)
	@Override
	public RepositoryConfiguration getRepositoryContext() {
		return service.getRepositoryContext();
	}

	@Title("Retrieve Attachment")
	@Documentation("Retrieve a task attachment.")
	@Section(value = "Attachments", order = 5)
	@RequestMapping(value = "/attachment/{attachmentId}/full", method = RequestMethod.GET)
	@Override
	public Attachment retrieveAttachment(@PathVariable("attachmentId") Integer attachmentId)
			throws EntityNotFoundException {
		return service.retrieveAttachment(attachmentId);
	}

	/**
	 * For GET attachment over URL.
	 */
	@Title("Retrieve Attachment Content")
	@Documentation("Retrieve a task attachment content.  Suitable for standard web access via browser, curl, etc.")
	@Section(value = "Attachments", order = 5)
	@RequestMapping(value = "/attachment/{attachmentId}", method = RequestMethod.GET)
	public void getAttachmentData(HttpServletResponse response, @PathVariable("attachmentId") Integer attachmentId)
			throws EntityNotFoundException, IOException {
		Attachment attachment = service.retrieveAttachment(attachmentId);
		// set headers
		response.setContentType(attachment.getMimeType());
		response.setHeader("Content-Disposition", "attachment; filename=" + attachment.getFilename());
		response.setContentLength(attachment.getByteSize());
		// lastly, write the content
		response.getOutputStream().write(attachment.getAttachmentData());
	}

	@Title("Create Attachment")
	@Documentation("Create a task attachment.")
	@Section(value = "Attachments", order = 5)
	@RequestMapping(value = "/attachment", method = RequestMethod.POST)
	public AttachmentHandle saveAttachment(@RequestBody SaveAttachmentArguments args) throws ValidationException,
			EntityNotFoundException, ConcurrentUpdateException {
		// Proxy this call to our internal interface implementation method - we do this to ensure consistency with the
		// TaskService interface (implementing the interface on this class ensures we capture all changes to it).
		if (args.getComment() == null) {
			return this.saveAttachment(args.getTaskHandle(), args.getAttachment());
		} else {
			return this.saveAttachment(args.getTaskHandle(), args.getAttachment(), args.getComment());
		}
	}

	@Override
	public AttachmentHandle saveAttachment(TaskHandle taskHandle, Attachment attachment) throws ValidationException,
			EntityNotFoundException, ConcurrentUpdateException {
		return service.saveAttachment(taskHandle, attachment);
	}

	@Override
	public AttachmentHandle saveAttachment(TaskHandle taskHandle, Attachment attachment, Comment comment)
			throws ValidationException, EntityNotFoundException, ConcurrentUpdateException {
		return service.saveAttachment(taskHandle, attachment, comment);
	}

	@Title("Create Product")
	@Documentation("Create a product.")
	@Section("Products")
	@RequestMapping(value = "/task/product", method = RequestMethod.POST)
	@Override
	public Product createProduct(@RequestBody Product product) throws ValidationException {
		return service.createProduct(product);
	}

	@Title("Create Product")
	@Documentation("Retrieve a product.")
	@Section("Products")
	@RequestMapping(value = "/task/product/{productId}", method = RequestMethod.GET)
	@Override
	public Product retrieveProduct(@PathVariable(value = "productId") @Documentation("The product id") Integer productId)
			throws EntityNotFoundException {
		return service.retrieveProduct(productId);
	}

	@Title("List Products")
	@Documentation("List all products in the current repository configuration.")
	@Section("Products")
	@RequestMapping(value = "/task/product", method = RequestMethod.GET)
	@Override
	public List<Product> listAllProducts() {
		return service.listAllProducts();
	}

	@Title("Delete Product")
	@Documentation("Delete a specific product by id.")
	@Section("Products")
	@RequestMapping(value = "/task/product/{productId}", method = RequestMethod.DELETE)
	@Override
	public void deleteProduct(@PathVariable(value = "productId") @Documentation("The product id") Integer productId)
			throws ValidationException, EntityNotFoundException {
		service.deleteProduct(productId);
	}

	@Title("Modify Product")
	@Documentation("Modify a product.  The given product must have an id.")
	@Section("Products")
	@RequestMapping(value = "/task/product/{productId}", method = RequestMethod.POST)
	public Product updateProduct(@PathVariable(value = "productId") Integer productId, @RequestBody Product product)
			throws EntityNotFoundException, ValidationException {

		confirmIdsMatch(productId, product);
		return this.updateProduct(product);
	}

	@Title("Modify Product Configuration")
	@Documentation("Modify a product and its associated components and milestones.")
	@Section("Products")
	@RequestMapping(value = "/task/product-tree/{productId}", method = RequestMethod.POST)
	public Product updateProductTree(@PathVariable(value = "productId") Integer productId, @RequestBody Product product)
			throws EntityNotFoundException, ValidationException {

		confirmIdsMatch(productId, product);
		return this.updateProductTree(product);
	}

	@Override
	public Product updateProductTree(Product product) throws ValidationException, EntityNotFoundException {
		return service.updateProductTree(product);
	}

	@Override
	public Product updateProduct(Product product) throws ValidationException, EntityNotFoundException {
		return service.updateProduct(product);
	}

	@Title("Create Component")
	@Documentation("Create a product component.")
	@Section("Products")
	@RequestMapping(value = "/task/component", method = RequestMethod.POST)
	@Override
	public Component createComponent(@RequestBody Component product) throws ValidationException {
		return service.createComponent(product);
	}

	@Title("Retrieve Component")
	@Documentation("Retrieve a product component by its id.")
	@Section("Products")
	@RequestMapping(value = "/task/component/{componentId}", method = RequestMethod.GET)
	@Override
	public Component retrieveComponent(@PathVariable(value = "componentId") Integer componentId)
			throws EntityNotFoundException {

		return service.retrieveComponent(componentId);
	}

	@Title("List Components")
	@Documentation("List all components.")
	@Section("Products")
	@RequestMapping(value = "/task/component", method = RequestMethod.GET)
	@Override
	public List<Component> listAllComponents() {
		return service.listAllComponents();
	}

	@Title("Delete Component")
	@Documentation("Delete a component by its id.")
	@Section("Products")
	@RequestMapping(value = "/task/component/{componentId}", method = RequestMethod.DELETE)
	@Override
	public void deleteComponent(@PathVariable(value = "componentId") Integer componentId) throws ValidationException,
			EntityNotFoundException {
		service.deleteComponent(componentId);
	};

	@Title("Modify Component")
	@Documentation("Modify a product component.  The provided component must have an id.")
	@Section("Products")
	@RequestMapping(value = "/task/component/{componentId}", method = RequestMethod.POST)
	public Component updateComponent(@PathVariable(value = "componentId") Integer componentId,
			@RequestBody Component component) throws EntityNotFoundException, ValidationException {

		confirmIdsMatch(componentId, component);
		return this.updateComponent(component);
	}

	@Override
	public Component updateComponent(Component component) throws ValidationException, EntityNotFoundException {
		return service.updateComponent(component);
	}

	@Title("Delete Milestone")
	@Documentation("Delete a product milestone by its id.")
	@Section("Products")
	@Override
	@RequestMapping(value = "/task/milestone/{milestoneId}", method = RequestMethod.DELETE)
	public void deleteMilestone(@PathVariable(value = "milestoneId") Integer milestoneId) throws ValidationException,
			EntityNotFoundException {
		service.deleteMilestone(milestoneId);
	};

	@Exclude
	@RequestMapping(value = "/team", method = RequestMethod.POST)
	@Override
	public void replicateTeam(@RequestBody Team team) {
		service.replicateTeam(team);
	}

	@Title("List Tags")
	@Documentation("List all tags.")
	@Section("Tags")
	@RequestMapping(value = "/keywords", method = RequestMethod.GET)
	@Override
	public List<Keyword> listAllKeywords() {
		return service.listAllKeywords();
	}

	@Title("Create Tag")
	@Documentation("Create a new tag.")
	@Section("Tags")
	@Override
	@RequestMapping(value = "/keywords", method = RequestMethod.POST)
	public Keyword createKeyword(@RequestBody Keyword keyword) throws ValidationException {
		return service.createKeyword(keyword);
	}

	@Title("Update Tag")
	@Documentation("Update a tag.")
	@Section("Tags")
	@RequestMapping(value = "/keywords/{keywordId}", method = RequestMethod.POST)
	public Keyword updateKeyword(@PathVariable(value = "keywordId") Integer keywordId, @RequestBody Keyword keyword)
			throws ValidationException, EntityNotFoundException {
		confirmIdsMatch(keywordId, keyword);
		return this.updateKeyword(keyword);
	}

	@Override
	public Keyword updateKeyword(Keyword keyword) throws ValidationException, EntityNotFoundException {
		return service.updateKeyword(keyword);
	}

	@Title("Delete Tag")
	@Documentation("Delete a tag if it's not in use on any tasks.")
	@Section("Tags")
	@Override
	@RequestMapping(value = "/keywords/{keywordId}", method = RequestMethod.DELETE)
	public void deleteKeyword(@PathVariable(value = "keywordId") Integer keywordId) throws EntityNotFoundException,
			ValidationException {
		service.deleteKeyword(keywordId);
	}

	interface QueryRunner {
		QueryResult<Task> getPage(QuerySpec page);
	}

	Iterator<Task> getPagingIterator(final QueryRunner runner) {
		return new Iterator<Task>() {
			int pageSize = 100;
			int currentIndex = 0;
			QuerySpec currentPage = new QuerySpec(new Region(0, pageSize), new SortInfo(
					TaskFieldConstants.TASK_ID_FIELD));
			QueryResult<Task> currentResult = runner.getPage(currentPage);

			@Override
			public boolean hasNext() {
				return currentIndex < currentResult.getTotalResultSize();
			}

			@Override
			public Task next() {
				if (currentIndex >= currentPage.getRegion().getOffset() + currentPage.getRegion().getSize()) {
					currentPage = new QuerySpec(new Region(currentPage.getRegion().getOffset() + pageSize, pageSize),
							new SortInfo(TaskFieldConstants.TASK_ID_FIELD));
					currentResult = runner.getPage(currentPage);
				}
				return currentResult.getResultPage().get(currentIndex++ - currentPage.getRegion().getOffset());
			}

			@Override
			public void remove() {
				throw new IllegalArgumentException("not supported");

			}

		};
	}

	@Title("Export a Criteria Query to CSV")
	@Documentation("")
	@Section("Export")
	@RequestMapping(value = "/export/criteria/csv/{criteriaString:.*}", method = RequestMethod.GET)
	public void exportCriteriaQueryToCSV(@PathVariable(value = "criteriaString") String criteriaString,
			HttpServletResponse response) throws Exception {
		final Criteria criteria = CriteriaParser.parse(criteriaString);

		Iterator<Task> result = getPagingIterator(new QueryRunner() {

			@Override
			public QueryResult<Task> getPage(QuerySpec page) {
				return service.findTasksWithCriteria(criteria, page);
			}
		});

		exportToCSV(response, result);
	}

	@Title("Export a Predefined Query to CSV")
	@Documentation("")
	@Section("Export")
	@RequestMapping(value = "/export/predefined/csv/{query:.*}", method = RequestMethod.GET)
	public void exportPredefinedQueryToCSV(@PathVariable(value = "query") final PredefinedTaskQuery predefinedQuery,
			HttpServletResponse response) throws Exception {

		Iterator<Task> result = getPagingIterator(new QueryRunner() {

			@Override
			public QueryResult<Task> getPage(QuerySpec page) {
				return service.findTasksWithQuery(predefinedQuery, page);
			}
		});
		exportToCSV(response, result);
	}

	@Title("Export a Text Query to CSV")
	@Documentation("")
	@Section("Export")
	@RequestMapping(value = "/export/text/csv/{query:.*}", method = RequestMethod.GET)
	public void exportTextQueryToCSV(@PathVariable(value = "query") final String queryText, HttpServletResponse response)
			throws Exception {
		Iterator<Task> result = getPagingIterator(new QueryRunner() {

			@Override
			public QueryResult<Task> getPage(QuerySpec page) {
				return service.findTasks(queryText, page);
			}
		});

		exportToCSV(response, result);
	}

	private void maybeAddSep(StringBuilder value) {
		if (value.length() != 0) {
			value.append(", ");
		}
	}

	private void exportToCSV(HttpServletResponse response, Iterator<Task> result) throws IOException {
		response.setContentType("text/csv;charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"tasks.csv\"");
		CsvWriter writer = new CsvWriter();
		writer.value("Id").value("Type").value("Created").value("Changed").value("Summary").value("Priority")
				.value("Severity").value("Product").value("Component").value("Status").value("Resolution")
				.value("Estimate").value("Release").value("Found In Release").value("Iteration").value("Owner")
				.value("CC").value("Tags").value("Parent").row();
		writer.write(response.getWriter());

		while (result.hasNext()) {
			Task task = result.next();

			StringBuilder tagString = new StringBuilder();
			for (Keyword k : task.getKeywords()) {
				maybeAddSep(tagString);
				tagString.append(k.getName());
			}

			StringBuilder ccString = new StringBuilder();
			if (task.getWatchers() != null) {
				for (TaskUserProfile cc : task.getWatchers()) {
					maybeAddSep(ccString);
					ccString.append(cc.getLoginName());
				}
			}

			writer.value(task.getId()).value(task.getTaskType()).value(task.getCreationDate())
					.value(task.getModificationDate()).value(task.getShortDescription())
					.value(task.getPriority().getValue()).value(task.getSeverity().getValue())
					.value(task.getProduct().getName()).value(task.getComponent().getName())
					.value(task.getStatus().getValue())
					.value(task.getResolution() == null ? null : task.getResolution().getValue())
					.value(task.getEstimatedTime())
					.value(task.getMilestone() == null ? null : task.getMilestone().getValue())
					.value(task.getFoundInRelease()).value(task.getIteration())
					.value(task.getAssignee() == null ? null : task.getAssignee().getLoginName()).value(ccString)
					.value(tagString).value(task.getParentTask() == null ? null : task.getParentTask().getId()).row()
					.write(response.getWriter());
		}
	}

	@Title("Export a Criteria Query to JSON")
	@Documentation("")
	@Section("Export")
	@RequestMapping(value = "/export/criteria/json/{criteriaString:.*}", method = RequestMethod.GET)
	public void exportCriteriaQueryToJson(@PathVariable(value = "criteriaString") String criteriaString,
			HttpServletResponse response) throws Exception {
		final Criteria criteria = CriteriaParser.parse(criteriaString);
		Iterator<Task> result = getPagingIterator(new QueryRunner() {

			@Override
			public QueryResult<Task> getPage(QuerySpec page) {
				return service.findTasksWithCriteria(criteria, page);
			}
		});

		exportToJson(response, result);
	}

	@Title("Export a Predefined Query to CSV")
	@Documentation("")
	@Section("Export")
	@RequestMapping(value = "/export/predefined/json/{query:.*}", method = RequestMethod.GET)
	public void exportPredefinedQueryToJson(@PathVariable(value = "query") final PredefinedTaskQuery predefinedQuery,
			HttpServletResponse response) throws Exception {
		Iterator<Task> result = getPagingIterator(new QueryRunner() {

			@Override
			public QueryResult<Task> getPage(QuerySpec page) {
				return service.findTasksWithQuery(predefinedQuery, page);
			}
		});

		exportToJson(response, result);
	}

	@Title("Export a Text Query to CSV")
	@Documentation("")
	@Section("Export")
	@RequestMapping(value = "/export/text/json/{query:.*}", method = RequestMethod.GET)
	public void exportTextQueryToJson(@PathVariable(value = "query") final String queryText,
			HttpServletResponse response) throws Exception {
		Iterator<Task> result = getPagingIterator(new QueryRunner() {

			@Override
			public QueryResult<Task> getPage(QuerySpec page) {
				return service.findTasks(queryText, page);
			}
		});

		exportToJson(response, result);
	}

	private void exportToJson(HttpServletResponse response, Iterator<Task> result) throws IOException {
		response.setContentType("application/json");
		response.setHeader("Content-Disposition", "attachment; filename=\"tasks.json\"");
		response.getWriter().print("[");
		boolean needSep = false;
		while (result.hasNext()) {
			Task task = result.next();
			if (needSep) {
				response.getWriter().print(", \n");
			} else {
				needSep = true;
			}
			response.getWriter().print(jsonMapper.writeValueAsString(task));
		}
		response.getWriter().println("]");
	}

	@Title("Create Saved Query")
	@Documentation("Create a saved query.")
	@Section("Configuration")
	@RequestMapping(value = "/task/query", method = RequestMethod.POST)
	@Override
	public SavedTaskQuery createQuery(@RequestBody SavedTaskQuery query) throws ValidationException {
		return service.createQuery(query);
	}

	@Title("Modify Saved Query")
	@Documentation("Modify a query.  The given query must have an id.")
	@Section("Configuration")
	@RequestMapping(value = "/task/query/{queryId}", method = RequestMethod.POST)
	public SavedTaskQuery updateQuery(@PathVariable(value = "queryId") Integer queryId,
			@RequestBody SavedTaskQuery query) throws EntityNotFoundException, ValidationException {

		confirmIdsMatch(queryId, query);
		return this.updateQuery(query);
	}

	@Override
	public SavedTaskQuery updateQuery(SavedTaskQuery query) throws ValidationException, EntityNotFoundException {
		return service.updateQuery(query);
	}

	@Override
	@RequestMapping(value = "/task/query/{queryId}", method = RequestMethod.DELETE)
	public void deleteQuery(@PathVariable(value = "queryId") Integer queryId) throws EntityNotFoundException,
			ValidationException {
		service.deleteQuery(queryId);
	}

	/**
	 * Check that an Integer ID and and object's ID are the same.
	 * 
	 * @param objectId
	 * @param object
	 */
	private void confirmIdsMatch(Integer objectId, AbstractDomainObject object) {

		if ((objectId == null) || object == null || object.getId() == null) {
			// Bad data - throw an exception back.
			throw new IllegalArgumentException("Invalid ID: null");
		}

		// Validate that the given ID matches the ID within the given Component.
		if (objectId.intValue() != object.getId().intValue()) {
			throw new IllegalArgumentException(String.format("Mismatched IDs: URL ID was %s, but %s ID was %s",
					objectId, object.getClass().getSimpleName(), object.getId()));
		}
	}

	@Title("Create Iteration")
	@Documentation("Create an iteration.")
	@Section("Configuration")
	@RequestMapping(value = "/task/iteration", method = RequestMethod.POST)
	@Override
	public Iteration createIteration(@RequestBody Iteration iteration) throws ValidationException {
		return service.createIteration(iteration);
	}

	@Title("Update Iteration")
	@Documentation("Update an iteration.")
	@Section("Configuration")
	@RequestMapping(value = "/task/iteration/{iterationId}", method = RequestMethod.POST)
	public Iteration updateIteration(@PathVariable Integer iterationId, @RequestBody Iteration iteration)
			throws ValidationException, EntityNotFoundException {
		confirmIdsMatch(iterationId, iteration);
		return service.updateIteration(iteration);
	}

	@Override
	public Iteration updateIteration(Iteration iteration) throws ValidationException, EntityNotFoundException {
		return service.updateIteration(iteration);
	}

	@Title("Create Custom Field")
	@Documentation("Create a custom field.")
	@Section("Configuration")
	@RequestMapping(value = "/task/customfield", method = RequestMethod.POST)
	@Override
	public FieldDescriptor createCustomField(@RequestBody FieldDescriptor customField) throws ValidationException {
		return service.createCustomField(customField);
	}

	@Title("Update Custom Field")
	@Documentation("Update an customField.")
	@Section("Configuration")
	@RequestMapping(value = "/task/customfield/{fieldName}", method = RequestMethod.POST)
	public FieldDescriptor updateCustomField(@PathVariable String fieldName, @RequestBody FieldDescriptor customField)
			throws ValidationException, EntityNotFoundException {
		// TODO confirm ids
		return service.updateCustomField(customField);
	}

	@Override
	public FieldDescriptor updateCustomField(FieldDescriptor customField) throws ValidationException,
			EntityNotFoundException {
		return service.updateCustomField(customField);
	}

	@Title("Delete Custom Field")
	@Documentation("Delete a specific custom field by id.")
	@Section("Configuration")
	@RequestMapping(value = "/task/customfield/{customFieldId}", method = RequestMethod.DELETE)
	@Override
	public void deleteCustomField(
			@PathVariable(value = "customFieldId") @Documentation("The custom field id") Integer customFieldId)
			throws EntityNotFoundException {
		service.deleteCustomField(customFieldId);
	}

	@Title("Render wiki markup")
	@Documentation("Render wiki markup to html.")
	@Section("Configuration")
	@RequestMapping(value = "/wikimarkup", method = RequestMethod.POST)
	public Map<String, String> doRenderWikiMarkupAsHtml(@RequestBody String markup) {
		return Collections.singletonMap("string", renderWikiMarkupAsHtml(markup));
	}

	@Override
	public String renderWikiMarkupAsHtml(String markup) {
		return service.renderWikiMarkupAsHtml(markup);
	}

}
