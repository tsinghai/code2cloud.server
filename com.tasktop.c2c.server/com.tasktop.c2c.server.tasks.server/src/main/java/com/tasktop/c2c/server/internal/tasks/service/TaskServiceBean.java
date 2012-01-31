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
package com.tasktop.c2c.server.internal.tasks.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.InsufficientPermissionsException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.Role;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.SortInfo.Order;
import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.event.domain.TaskActivityEvent;
import com.tasktop.c2c.server.event.service.EventService;
import com.tasktop.c2c.server.event.service.EventServiceClient;
import com.tasktop.c2c.server.internal.tasks.domain.AttachmentData;
import com.tasktop.c2c.server.internal.tasks.domain.Cc;
import com.tasktop.c2c.server.internal.tasks.domain.Classification;
import com.tasktop.c2c.server.internal.tasks.domain.Comment;
import com.tasktop.c2c.server.internal.tasks.domain.Dependency;
import com.tasktop.c2c.server.internal.tasks.domain.KeywordId;
import com.tasktop.c2c.server.internal.tasks.domain.Keyworddef;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.internal.tasks.domain.StatusWorkflow;
import com.tasktop.c2c.server.internal.tasks.domain.TaskType;
import com.tasktop.c2c.server.internal.tasks.domain.Version;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.DomainConversionContext;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.DomainConverter;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.ProfileConverter;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.SavedTaskQueryConverter;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.TaskDomain;
import com.tasktop.c2c.server.internal.tasks.jpa.TaskQuery;
import com.tasktop.c2c.server.internal.tasks.service.TaskSortFieldMapper.QueryParts;
import com.tasktop.c2c.server.tasks.domain.AbstractDomainObject;
import com.tasktop.c2c.server.tasks.domain.AbstractReferenceValue;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.AttachmentHandle;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.CustomFieldValue;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.QuerySpec;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.StateTransition;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskActivity;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;
import com.tasktop.c2c.server.tasks.domain.TaskHandle;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskSummary;
import com.tasktop.c2c.server.tasks.domain.TaskSummaryItem;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.Team;
import com.tasktop.c2c.server.tasks.service.TaskService;

/**
 * Main implementation of the {@link TaskService} using JPA.
 * 
 * @author David Green <david.green@tasktop.com> (Tasktop Technologies Inc.)
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * @author Lucas Panjer <lucas.panjer@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@Service("taskService")
@Transactional(rollbackFor = { Exception.class })
@Qualifier("main")
public class TaskServiceBean extends AbstractJpaServiceBean implements TaskService, TaskServiceDependencies,
		InitializingBean {

	@Autowired
	private TaskSortFieldMapper sortFieldMapper;

	@Autowired
	private TaskServiceConfiguration configuration;
	@Autowired
	private DomainConverter domainConverter;
	@Autowired
	private InternalTaskService internalTaskService;
	@Autowired
	private TaskCustomFieldService taskCustomFieldService;
	@Autowired
	private TaskActivityService activityService;
	@Autowired
	private Validator internalValidator;
	@Autowired
	private EventService eventService;

	private static final String DEFAULT_MILESTONE = "---";

	private static final String TASK_SELECT_CLAUSE = "SELECT task ";

	private static final String TASK_FROM_CLAUSE = " FROM "
			+ com.tasktop.c2c.server.internal.tasks.domain.Task.class.getSimpleName() + " task";

	@SuppressWarnings("unchecked")
	@Override
	@Secured({ Role.Observer, Role.User })
	public TaskSummary getTaskSummary() {
		TaskSummary summary = new TaskSummary();

		// We can't do an outer join here since none of these have explicit relationships (i.e., foreign keys)

		Map<String, TaskSummaryItem> itemBySeverityValue = new HashMap<String, TaskSummaryItem>();

		List<com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity> severities = entityManager.createQuery(
				"select e from " + com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity.class.getSimpleName()
						+ " e order by e.sortkey").getResultList();
		for (com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity severity : severities) {
			TaskSummaryItem summaryItem = new TaskSummaryItem();
			summaryItem.setSeverity(TaskDomain.createDomain(severity));
			itemBySeverityValue.put(severity.getValue(), summaryItem);
			summary.getItems().add(summaryItem);
		}

		// FIXME similar issue to TASK 162, this will miss invalid tasks.
		List<Object[]> results = entityManager.createQuery(
				"select sev, count(e)" + ", sum(stat.isOpen) " + " from "
						+ com.tasktop.c2c.server.internal.tasks.domain.Task.class.getSimpleName() + " e, "
						+ com.tasktop.c2c.server.internal.tasks.domain.TaskStatus.class.getSimpleName() + " stat, "
						+ com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity.class.getSimpleName() + " sev "
						+ "where sev.value = e.severity and stat.value = e.status " + "group by sev").getResultList();

		for (Object[] result : results) {
			com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity severity = (com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity) result[0];
			Number count = (Number) result[1];
			Number openCount = (Number) result[2];

			TaskSummaryItem item = itemBySeverityValue.get(severity.getValue());
			item.setClosedCount(count.intValue() - openCount.intValue());
			item.setOpenCount(openCount.intValue());
		}

		return summary;
	}

	private static final long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;

	@SuppressWarnings("unchecked")
	@Override
	@Secured({ Role.Observer, Role.User })
	public List<TaskSummary> getHistoricalSummary(int numDays) {

		List<com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity> severities = entityManager.createQuery(
				"select e from " + com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity.class.getSimpleName()
						+ " e order by e.sortkey").getResultList();

		List<TaskSummary> summaries = new ArrayList<TaskSummary>(numDays);
		Long now = System.currentTimeMillis();
		for (int i = numDays - 1; i >= 0; i--) {
			TaskSummary summary = new TaskSummary();
			summary.setDate(new Date(now - i * MILLISECONDS_PER_DAY));
			summaries.add(summary);

			for (com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity severity : severities) {
				TaskSummaryItem summaryItem = new TaskSummaryItem();
				summaryItem.setSeverity(TaskDomain.createDomain(severity));
				summary.getItems().add(summaryItem);
			}
		}

		List<Object[]> taskAndStatus = entityManager.createQuery(
				"select t, s from " + com.tasktop.c2c.server.internal.tasks.domain.Task.class.getSimpleName() + " t, "
						+ com.tasktop.c2c.server.internal.tasks.domain.TaskStatus.class.getSimpleName() + " s "
						+ "where t.status=s.value").getResultList();

		for (Object[] objects : taskAndStatus) {
			add((com.tasktop.c2c.server.internal.tasks.domain.Task) objects[0],
					(com.tasktop.c2c.server.internal.tasks.domain.TaskStatus) objects[1], summaries);
		}

		return summaries;
	}

	private void add(com.tasktop.c2c.server.internal.tasks.domain.Task task,
			com.tasktop.c2c.server.internal.tasks.domain.TaskStatus status, List<TaskSummary> summaries) {
		Iterator<TaskSummary> it = summaries.iterator();
		TaskSummary current;

		Date closedDate = null;
		if (!status.getIsOpen()) {
			closedDate = task.getDeltaTs();
		}

		while (it.hasNext()) {
			current = it.next();
			if (task.getCreationTs() == null || current.getDate().before(task.getCreationTs())) {
				continue; // Before task existed
			} else if (closedDate != null && current.getDate().after(closedDate)) {
				// Task was closed
				countClosedTask(task, current);
			} else {
				countOpenTask(task, current);
			}
		}

	}

	private void countClosedTask(com.tasktop.c2c.server.internal.tasks.domain.Task task, TaskSummary current) {
		for (TaskSummaryItem item : current.getItems()) {
			if (item.getSeverity().getValue().equals(task.getSeverity())) {
				item.setClosedCount(item.getClosedCount() + 1);
				return;
			}
		}
		throw new IllegalStateException("Missing severity: " + task.getSeverity());
	}

	private void countOpenTask(com.tasktop.c2c.server.internal.tasks.domain.Task task, TaskSummary current) {
		for (TaskSummaryItem item : current.getItems()) {
			if (item.getSeverity().getValue().equals(task.getSeverity())) {
				item.setOpenCount(item.getOpenCount() + 1);
				return;
			}
		}
		throw new IllegalStateException("Missing severity: " + task.getSeverity());
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public QueryResult<Task> findTasksWithQuery(PredefinedTaskQuery predefinedQuery, QuerySpec querySpec) {
		SortInfo specifiedSortInfo = querySpec == null ? null : querySpec.getSortInfo();
		QuerySpec impliedQuerySpec = getImpliedQuerySpec(querySpec);

		switch (predefinedQuery) {
		case ALL:
			return findAllTasks(impliedQuerySpec);
		case OPEN:
			return findAllOpenTasks(impliedQuerySpec);
		case MINE:
			return findAllCurrentUsersTasks(impliedQuerySpec);
		case RELATED:
			return findAllCurrentUsersRelatedTasks(impliedQuerySpec);
		case RECENT:
			// Just an ALL with a different default sort
			if (specifiedSortInfo == null) {
				impliedQuerySpec.setSortInfo(new SortInfo(TaskFieldConstants.LAST_UPDATE_FIELD, Order.DESCENDING));
			}
			return findAllTasks(impliedQuerySpec);
		default:
			throw new UnsupportedOperationException("Unknown pre-defined query");
		}
	}

	private QuerySpec getImpliedQuerySpec(QuerySpec querySpec) {
		if (querySpec == null) {
			return new QuerySpec(DEFAULT_PAGE_INFO, DEFAULT_SORT_INFO, true);
		}
		Region pageInfo = querySpec.getRegion() == null ? DEFAULT_PAGE_INFO : querySpec.getRegion();
		SortInfo sortInfo = querySpec.getSortInfo() == null ? DEFAULT_SORT_INFO : querySpec.getSortInfo();
		boolean thin = querySpec.getThin() == null ? true : querySpec.getThin();
		return new QuerySpec(pageInfo, sortInfo, thin);
	}

	private QueryResult<Task> findAllCurrentUsersTasks(QuerySpec querySpec) {
		Profile currentUserProfile = internalTaskService.getCurrentUserProfile();
		ColumnCriteria criteriaClause = new ColumnCriteria(TaskFieldConstants.ASSIGNEE_FIELD, Operator.EQUALS,
				currentUserProfile.getLoginName());
		return findTasksWithCriteria(criteriaClause, querySpec);
	}

	private QueryResult<Task> findAllCurrentUsersRelatedTasks(QuerySpec querySpec) {
		Profile currentUserProfile = internalTaskService.getCurrentUserProfile();

		// user is reporter OR user in watchers OR user in commentors

		NaryCriteria criteriaClause = new NaryCriteria();
		criteriaClause.setOperator(Operator.OR);
		List<Criteria> criteria = new ArrayList<Criteria>();
		criteria.add(new ColumnCriteria(TaskFieldConstants.REPORTER_FIELD, currentUserProfile.getLoginName()));
		criteria.add(new ColumnCriteria(TaskFieldConstants.WATCHER_FIELD, currentUserProfile.getLoginName()));
		criteria.add(new ColumnCriteria(TaskFieldConstants.COMMENT_AUTHOR_FIELD, currentUserProfile.getLoginName()));
		criteriaClause.setSubCriteria(criteria);

		return findTasksWithCriteria(criteriaClause, querySpec);
	}

	private List<Task> mapToDomainObjects(QuerySpec querySpec, Query query) {
		DomainConversionContext conversionContext = createTaskQueryDomainConversionContext(querySpec);

		// Can't set the page size because it generates a bad mysql query.
		query.setFirstResult(querySpec.getRegion().getOffset());
		@SuppressWarnings("unchecked")
		List<Object> queryResults = query.getResultList();

		return mapToDomainObjects(conversionContext, querySpec.getRegion(), queryResults);
	}

	private List<Task> mapToDomainObjects(DomainConversionContext conversionContext, Region region, List<?> queryResults) {
		List<Task> resultList = new ArrayList<Task>(region.getSize());
		for (int i = 0; i < region.getSize() && i < queryResults.size(); i++) {
			Object queryResult = queryResults.get(i);
			resultList.add(mapToDomainObject(queryResult, conversionContext));
		}
		return resultList;
	}

	private DomainConversionContext createTaskQueryDomainConversionContext(QuerySpec querySpec) {
		DomainConversionContext conversionContext = new DomainConversionContext(entityManager);
		conversionContext.setThin(querySpec.getThin());
		// pre-fill common attributes so that we can minimize the number of queries that we perform
		conversionContext.fill(com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity.class);
		conversionContext.fill(com.tasktop.c2c.server.internal.tasks.domain.TaskStatus.class);
		conversionContext.fill(com.tasktop.c2c.server.internal.tasks.domain.Priority.class);
		conversionContext.fill(com.tasktop.c2c.server.internal.tasks.domain.Resolution.class);
		conversionContext.fillMilestone();
		return conversionContext;
	}

	private Task mapToDomainObject(Object queryResult, DomainConversionContext context) {

		com.tasktop.c2c.server.internal.tasks.domain.Task internalTask;
		if (queryResult instanceof Object[]) {
			internalTask = (com.tasktop.c2c.server.internal.tasks.domain.Task) ((Object[]) queryResult)[0];
		} else {
			internalTask = (com.tasktop.c2c.server.internal.tasks.domain.Task) queryResult;
		}
		Task task = (Task) domainConverter.convert(internalTask, context);
		try {
			injectCustomFields(task);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException(e); // Should never happen
		}
		return task;
	}

	private Profile getLoggedInDomainProfile() {
		if (AuthenticationServiceUser.getCurrent().getUsername().equals("ROLE_ANONYMOUS")) {
			return null;
		}
		return internalTaskService.getCurrentUserProfile();
	}

	private TaskUserProfile getLoggedInTaskUserProfile() {
		Profile internalResult = internalTaskService.getCurrentUserProfile();
		return ProfileConverter.copy(internalResult);
	}

	private QueryResult<Task> findAllTasks(QuerySpec querySpec) {
		return findTasksWithCriteria(null, querySpec);
	}

	private QueryResult<Task> findAllOpenTasks(QuerySpec querySpec) {
		QueryParts sortParts = sortFieldMapper.mapSortFieldToDB(querySpec.getSortInfo());

		// Special handle this case because it puts status in query too.
		if (querySpec.getSortInfo().getSortField().equals(TaskFieldConstants.STATUS_FIELD)) {
			sortParts = new QueryParts("", "", "", sortParts.getOrderByPart());
		}

		String queryString = TASK_SELECT_CLAUSE + ", stat" + sortParts.getSelectPart() + TASK_FROM_CLAUSE + ", "
				+ com.tasktop.c2c.server.internal.tasks.domain.TaskStatus.class.getSimpleName() + " stat"
				+ sortParts.getFromPart() + " WHERE stat.isOpen = true and task.status = stat.value "
				+ (sortParts.getWherePart().isEmpty() ? "" : "and " + sortParts.getWherePart())
				+ sortParts.getOrderByPart();

		Query query = entityManager.createQuery(queryString);

		List<Task> resultPage = mapToDomainObjects(querySpec, query);
		Number totalResultSize = (Number) entityManager.createQuery(
				"select count(task) from " + com.tasktop.c2c.server.internal.tasks.domain.Task.class.getSimpleName()
						+ " task, " + com.tasktop.c2c.server.internal.tasks.domain.TaskStatus.class.getSimpleName()
						+ " stat " + " where stat.value = task.status and stat.isOpen = true").getSingleResult();

		return new QueryResult<Task>(querySpec.getRegion(), resultPage, totalResultSize.intValue());
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public QueryResult<Task> findTasks(String searchTerm, QuerySpec querySpec) {
		QuerySpec impliedQuerySpec = getImpliedQuerySpec(querySpec);

		try {
			Integer intSearchTerm = Integer.valueOf(searchTerm);
			return findTasksWithIntegerSearchTerm(intSearchTerm, impliedQuerySpec);
		} catch (NumberFormatException e) {
			return findTasksWithStringTerm(searchTerm, impliedQuerySpec);
		}

	}

	private QueryResult<Task> findTasksWithStringTerm(String searchTerm, QuerySpec querySpec) {
		QueryParts sortParts = sortFieldMapper.mapSortFieldToDB(querySpec.getSortInfo());

		String[] terms = searchTerm.split("\\s+");
		String termClause = makeTermsClause(terms);
		String queryString = TASK_SELECT_CLAUSE + sortParts.getSelectPart() + TASK_FROM_CLAUSE
				+ sortParts.getFromPart() + " WHERE (" + termClause + ") "
				+ (sortParts.getWherePart().isEmpty() ? "" : "and " + sortParts.getWherePart())
				+ sortParts.getOrderByPart();
		Query pageQuery = entityManager.createQuery(queryString);
		Query totalSizeQuery = entityManager.createQuery("select count(task) from "
				+ com.tasktop.c2c.server.internal.tasks.domain.Task.class.getSimpleName() + " task " + " where ("
				+ termClause + ")");

		for (int i = 0; i < terms.length; i++) {
			pageQuery.setParameter("term" + i, "%" + terms[i] + "%");
			totalSizeQuery.setParameter("term" + i, "%" + terms[i] + "%");
		}

		List<Task> resultPage = mapToDomainObjects(querySpec, pageQuery);
		Number totalResultSize = (Number) totalSizeQuery.getSingleResult();

		return new QueryResult<Task>(querySpec.getRegion(), resultPage, totalResultSize.intValue());
	}

	private String makeTermsClause(String[] terms) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < terms.length; i++) {
			String termParam = ":term" + i;
			if (i != 0) {
				result.append(" AND ");
			}
			result.append("(task.shortDesc LIKE ").append(termParam).append(" OR task.targetMilestone LIKE ")
					.append(termParam).append(" OR task.id in (SELECT tc").append(i).append(".task.id FROM ")
					.append(Comment.class.getSimpleName()).append(" tc").append(i).append(" WHERE tc").append(i)
					.append(".thetext LIKE ").append(termParam).append("))");
		}
		return result.toString();
	}

	private QueryResult<Task> findTasksWithIntegerSearchTerm(Integer intSearchTerm, QuerySpec querySpec) {
		try {
			Task t = retrieveTask(intSearchTerm);
			return new QueryResult<Task>(querySpec.getRegion(), Arrays.asList(t), 1);
		} catch (EntityNotFoundException e) {
			return findTasksWithStringTerm(intSearchTerm.toString(), querySpec);
		}
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public QueryResult<Task> findTasksWithCriteria(Criteria criteria, QuerySpec querySpec) {
		QuerySpec impliedQuerySpec = getImpliedQuerySpec(querySpec);

		TaskQuery taskQuery = TaskQuery.create(taskCustomFieldService, criteria, impliedQuerySpec.getSortInfo());
		int totalResultsCount = taskQuery.countResults(entityManager);
		List<com.tasktop.c2c.server.internal.tasks.domain.Task> results = taskQuery.getResults(entityManager,
				impliedQuerySpec.getRegion());
		DomainConversionContext conversionContext = createTaskQueryDomainConversionContext(impliedQuerySpec);
		List<Task> resultPage = mapToDomainObjects(conversionContext, impliedQuerySpec.getRegion(), results);

		return new QueryResult<Task>(impliedQuerySpec.getRegion(), resultPage, totalResultsCount);
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public QueryResult<Task> findTaskSummariesWithCriteria(Criteria criteria, QuerySpec querySpec) {
		QueryResult<Task> thickResult = this.findTasksWithCriteria(criteria, querySpec);

		// Now, go through and construct objects with the bare minimum that we need for our search results - throw out
		// anything that's not explicitly needed to cut down on serialized size.

		QueryResult<Task> thinResult = new QueryResult<Task>();
		thinResult.setOffset(thickResult.getOffset());
		thinResult.setPageSize(thickResult.getPageSize());
		thinResult.setTotalResultSize(thickResult.getTotalResultSize());

		List<Task> thinTaskList = new ArrayList<Task>(thickResult.getResultPage().size());
		for (Task thickTask : thickResult.getResultPage()) {
			Task thinTask = new Task();

			// Add our minimal fields now.
			thinTask.setId(thickTask.getId());
			thinTask.setShortDescription(thickTask.getShortDescription());
			thinTask.setStatus(thickTask.getStatus());
			thinTask.setMilestone(thickTask.getMilestone());
			thinTask.setComponent(thickTask.getComponent());
			thinTask.setTaskType(thickTask.getTaskType());
			thinTask.setIteration(thickTask.getIteration());

			// We only want our product's name.
			Product thinProdoct = new Product();
			thinProdoct.setName(thickTask.getProduct().getName());
			thinTask.setProduct(thinProdoct);

			// Blank out irrelevant fields in the copied objects
			if (thinTask.getComponent() != null) {
				thinTask.getComponent().setInitialOwner(null);
				thinTask.getComponent().setProduct(null);
			}

			if (thinTask.getMilestone() != null) {
				thinTask.getMilestone().setProduct(null);
			}

			if (thinTask.getProduct() != null) {
				thinTask.getProduct().setComponents(null);
			}

			thinTaskList.add(thinTask);
		}

		thinResult.setResultPage(thinTaskList);
		return thinResult;
	}

	private com.tasktop.c2c.server.internal.tasks.domain.Task prepareTaskForSave(Task task) throws ValidationException,
			EntityNotFoundException {

		setCurrentUserFields(task);

		validate(task);
		lookupProfiles(task);

		com.tasktop.c2c.server.internal.tasks.domain.Task managedTask = null;

		if (task.getId() != null) {
			// There's an ID, so search for an existing object.
			managedTask = find(com.tasktop.c2c.server.internal.tasks.domain.Task.class, task.getId());
		}

		verifyWorkflow(managedTask, task);

		com.tasktop.c2c.server.internal.tasks.domain.Task internalTask = TaskDomain.createManaged(task);

		setDefaultAssigneeIfAppropriate(internalTask);

		if (task.getCustomFields() == null) {
			task.setCustomFields(new HashMap<String, String>());
		}

		// FIXME, Is this necessary?
		// Copy our iteration into appropriate custom field now.
		task.getCustomFields().put(com.tasktop.c2c.server.internal.tasks.domain.Task.CUSTOM_ITERATION_FIELD_NAME,
				task.getIteration().getValue());

		return internalTask;
	}

	protected void validate(Task task) throws ValidationException {

		super.validate(task, validator, new Validator() {

			@Override
			public void validate(Object target, Errors errors) {
				Task task = (Task) target;
				try {
					entityManager
							.createQuery(
									"select e from " + TaskType.class.getSimpleName() + " e where e.value = :value")
							.setParameter("value", task.getTaskType()).getSingleResult();
				} catch (NoResultException e) {
					errors.rejectValue("taskType", "field.validTaskType");
				}
			}

			@Override
			public boolean supports(Class<?> clazz) {
				return clazz == Task.class;
			}
		});
	}

	private Task completeTaskSave(com.tasktop.c2c.server.internal.tasks.domain.Task managedTargetTask,
			com.tasktop.c2c.server.internal.tasks.domain.Task sourceTask, Task domainSourceTask,
			List<TaskActivity> updateActivities) throws ValidationException, EntityNotFoundException {

		TaskDomain.fillManaged(managedTargetTask, sourceTask);

		cascadePersistForUpdate(managedTargetTask);

		fillManagedObjects(managedTargetTask);

		createAttachments(domainSourceTask, managedTargetTask);

		entityManager.flush();

		updateCustomFields(managedTargetTask, domainSourceTask);
		updateProductReleases(managedTargetTask);
		validate(managedTargetTask, new TaskConstraintValidator(entityManager));

		// Must use a fresh copy of the retrieveTask because it gets thinned out.
		Task activityTask = retrieveTask(managedTargetTask.getId());

		// Set the update activities to the new task (current one has the old values)
		for (TaskActivity a : updateActivities) {
			a.setTask(activityTask);
		}
		updateActivities.addAll(activityService.deduceActivitiesFromTask(activityTask, managedTargetTask.getDeltaTs()));
		// Thin out some un-needed fields.
		activityTask.setAttachments(null);
		activityTask.setSubTasks(null);
		activityTask.setParentTask(null);
		activityTask.setBlocksTasks(null);

		sendActivityEvent(updateActivities);

		return retrieveTask(managedTargetTask.getId());
	}

	private void updateProductReleases(com.tasktop.c2c.server.internal.tasks.domain.Task managedTargetTask) {
		if (managedTargetTask.getVersion() == null || managedTargetTask.getVersion().isEmpty()) {
			return;
		}

		for (Version pr : managedTargetTask.getProduct().getVersionses()) {
			if (pr.getValue().equals(managedTargetTask.getVersion())) {
				return;
			}
		}

		Version newProductRelease = new Version();
		newProductRelease.setProducts(managedTargetTask.getProduct());
		newProductRelease.setValue(managedTargetTask.getVersion());
		entityManager.persist(newProductRelease);
		entityManager.flush();
		managedTargetTask.getProduct().getVersionses().add(newProductRelease);
	}

	@Override
	@Secured({ Role.Community, Role.User })
	public Task createTask(Task task) throws ValidationException, EntityNotFoundException {
		if (task.getId() != null) {
			throw new IllegalArgumentException("Cannot create a task with an ID specified - ID must be null");
		}

		// Since this is being created, set our user as the creator.
		task.setReporter(getLoggedInTaskUserProfile());

		// Do a bunch of our initial validation and copying.
		com.tasktop.c2c.server.internal.tasks.domain.Task internalTask = prepareTaskForSave(task);

		internalTask.setDeltaTs(new Date());
		internalTask.setCreationTs(internalTask.getDeltaTs());
		TaskDomain.makeDescriptionTheEarliestComment(internalTask);
		fillManagedObjects(internalTask);
		entityManager.persist(internalTask);
		entityManager.flush(); // This is needed to set the bugId.

		// Now that we've created an ID, detach this object from our persistence context - we need to do this so that
		// our copy between managed objects works correctly, in particular so that changes are correctly detected (if
		// both are part of the persistence context, then both are automatically updated to reflect any changes).
		entityManager.detach(internalTask);

		// Now, get a copy of this object back from the persistence context - we do this so that we can effectively map
		// in all of our changes using TaskDomain.fillManaged().
		com.tasktop.c2c.server.internal.tasks.domain.Task managedTask = find(
				com.tasktop.c2c.server.internal.tasks.domain.Task.class, internalTask.getId());

		insertDependentObjectTree(task, internalTask);
		updateKeywords(managedTask, task, internalTask);

		// Now, wrap up the save and send this back to the caller.
		return completeTaskSave(managedTask, internalTask, task, new ArrayList<TaskActivity>());
	}

	/**
	 * Find or create existing keywords, then detach any keywords that didn't exist in the incoming object.
	 * 
	 * @param managedTask
	 * 
	 * @param sourceTask
	 *            domain object with incoming keywords
	 * @param targetTask
	 *            internal task that needs keywords
	 * @throws ValidationException
	 */
	private void updateKeywords(com.tasktop.c2c.server.internal.tasks.domain.Task managedTask, Task sourceTask,
			com.tasktop.c2c.server.internal.tasks.domain.Task targetTask) throws ValidationException {

		for (Keyword keyword : sourceTask.getKeywords()) {
			// find or create a keyworddef as necessary
			Keyworddef managedKeyword = findOrCreateManaged(keyword);

			// find or create a keyword to task mapping as necessary
			com.tasktop.c2c.server.internal.tasks.domain.Keyword keywordMap;
			try {
				keywordMap = find(com.tasktop.c2c.server.internal.tasks.domain.Keyword.class,
						new KeywordId(targetTask.getId(), managedKeyword.getId()));

			} catch (EntityNotFoundException e) {
				keywordMap = new com.tasktop.c2c.server.internal.tasks.domain.Keyword();
				keywordMap.setBugs(targetTask);
				keywordMap.setKeyworddefs(managedKeyword);
				keywordMap.setId(new KeywordId(targetTask.getId(), managedKeyword.getId())); // id must be set

			}
			// add keyword mapping to the task
			targetTask.getKeywordses().add(keywordMap);
		}

		removeUnusedKeywords(managedTask, targetTask);
	}

	private void removeUnusedKeywords(com.tasktop.c2c.server.internal.tasks.domain.Task managedTask,
			com.tasktop.c2c.server.internal.tasks.domain.Task targetTask) {

		for (com.tasktop.c2c.server.internal.tasks.domain.Keyword keyword : managedTask.getKeywordses()) {
			if (!targetTask.getKeywordses().contains(keyword)) {
				entityManager.remove(keyword);
			}
		}
	}

	/**
	 * Find or create a managed Keyword definition
	 * 
	 * @param keyword
	 * @return
	 * @throws ValidationException
	 */
	private Keyworddef findOrCreateManaged(Keyword keyword) throws ValidationException {
		Keyworddef keywordDef = findKeyworddefByName(keyword);
		if (keywordDef != null) {
			return keywordDef;
		} else {
			createKeyword(keyword);
			entityManager.flush();
			return findKeyworddefByName(keyword);
		}
	}

	private Keyworddef findKeyworddefByName(Keyword keyword) {
		String queryString = "select kd from " + Keyworddef.class.getSimpleName() + " kd where kd.name = :name";
		Query query = entityManager.createQuery(queryString);
		query.setParameter("name", keyword.getName());
		if (query.getResultList().size() > 0) {
			return (Keyworddef) query.getResultList().get(0);
		}
		return null;
	}

	private void insertDependentObjectTree(Task sourceTask, com.tasktop.c2c.server.internal.tasks.domain.Task targetTask) {
		TaskDomain.insertParentAndBlocks(sourceTask, targetTask);
		TaskDomain.insertSubTasks(sourceTask, targetTask);
		TaskDomain.insertCcs(sourceTask, targetTask);
		TaskDomain.insertDuplicateOf(sourceTask, targetTask);
	}

	private void lookupProfiles(Task task) throws EntityNotFoundException {
		task.setAssignee(getProfileByLoginName(task.getAssignee()));
		if (task.getWatchers() != null) {
			for (int i = 0; i < task.getWatchers().size(); i++) {
				TaskUserProfile profile = task.getWatchers().get(i);
				task.getWatchers().set(i, getProfileByLoginName(profile));
			}
		}
	}

	private TaskUserProfile getOrCreateFullProfile(TaskUserProfile profile) throws EntityNotFoundException {
		if (profile == null) {
			return null;
		}
		if (profile.getId() != null) {
			// It might seem odd to do a find() and to then ignore the result, but find() will throw an exception if the
			// Profile is not present, and that's what we care about, so we don't actually need to do anything with the
			// result.
			find(Profile.class, profile.getId());
			return profile;
		}
		if (profile.getLoginName() == null) {
			throw new IllegalStateException("profile's login name should be set");
		}
		if (profile.getLoginName().isEmpty()) {
			return null; // Special case for DS
		}

		Profile internalProfile = internalTaskService.provisionAccount(profile);
		return ProfileConverter.copy(internalProfile);
	}

	private TaskUserProfile getProfileByLoginName(TaskUserProfile profile) throws EntityNotFoundException {
		if (profile == null) {
			return null;
		}
		if (profile.getLoginName() == null) {
			throw new IllegalStateException("profile's login name should be set");
		}
		if (profile.getLoginName().isEmpty()) {
			return null; // Special case for DS
		}
		Profile internalProfile = internalTaskService.findProfile(profile.getLoginName());
		return ProfileConverter.copy(internalProfile);
	}

	private void setCurrentUserFields(Task task) {
		TaskUserProfile loggedInUser = getLoggedInTaskUserProfile();
		if (task.getComments() != null) {
			for (com.tasktop.c2c.server.tasks.domain.Comment c : task.getComments()) {
				if (c.getId() == null) {
					c.setAuthor(loggedInUser);
				}
			}
		}
		if (task.getWorkLogs() != null) {
			for (com.tasktop.c2c.server.tasks.domain.WorkLog w : task.getWorkLogs()) {
				if (w.getId() == null) {
					w.setProfile(loggedInUser);
				}
			}
		}
	}

	@Override
	@Secured({ Role.Community, Role.User })
	public Task updateTask(Task task) throws ValidationException, EntityNotFoundException, ConcurrentUpdateException {

		// Do a bunch of our initial validation and copying.
		com.tasktop.c2c.server.internal.tasks.domain.Task toUpdate = prepareTaskForSave(task);
		fillManagedObjects(toUpdate);

		com.tasktop.c2c.server.internal.tasks.domain.Task managedTask = find(
				com.tasktop.c2c.server.internal.tasks.domain.Task.class, task.getId());
		entityManager.lock(managedTask, LockModeType.PESSIMISTIC_WRITE); // Ensure no other concurrent writes go through

		// Make sure we haven't had an optimistic locking collision
		checkForUpdateCollision(managedTask.getDeltaTs().getTime(), task.getVersion());
		toUpdate.setDeltaTs(new Date());

		insertDependentObjectTree(task, toUpdate);
		updateKeywords(managedTask, task, toUpdate);

		// Record any changes which happened to this object.
		List<TaskActivity> activities = activityService.recordActivity(managedTask,
				retrieveCustomFieldValues(managedTask.getId()), toUpdate, task.getCustomFields(),
				getLoggedInDomainProfile(), toUpdate.getDeltaTs());

		maybeUpdateDuplicatedTask(task, managedTask);

		// Now, wrap up the save and send this back to the caller.
		return completeTaskSave(managedTask, toUpdate, task, activities);
	}

	// If we have a new duplicate, copy over assignee, reporter, watcher to the new task
	private void maybeUpdateDuplicatedTask(Task newTask,
			com.tasktop.c2c.server.internal.tasks.domain.Task managedTaskBeforeUpdate) throws EntityNotFoundException,
			ValidationException, ConcurrentUpdateException {
		if (newTask.getDuplicateOf() != null
				&& (managedTaskBeforeUpdate.getDuplicatesByBugId() == null || !newTask.getDuplicateOf().getId()
						.equals(managedTaskBeforeUpdate.getDuplicatesByBugId().getBugsByDupeOf().getId()))) {
			Task duplicated = retrieveTask(newTask.getDuplicateOf().getId());
			if (newTask.getAssignee() != null) {
				duplicated.getWatchers().add(newTask.getAssignee());
			}
			if (newTask.getReporter() != null) {
				duplicated.getWatchers().add(newTask.getReporter());
			}
			if (newTask.getWatchers() != null) {
				duplicated.getWatchers().addAll(newTask.getWatchers());
			}
			updateTask(duplicated);
		}
	}

	/**
	 * @param activities
	 */
	private void sendActivityEvent(List<TaskActivity> activities) {
		TaskActivityEvent event = new TaskActivityEvent();
		event.setProjectId(TenancyContextHolder.getContext().getTenant().getIdentity().toString());
		event.setTaskActivities(activities);
		eventService.publishEvent(event);
	}

	/**
	 * Enforce workflow constraints
	 * 
	 * @param originalTask
	 *            the original version, or null if a new task is being created
	 * @param task
	 * @throws ValidationException
	 */
	private void verifyWorkflow(com.tasktop.c2c.server.internal.tasks.domain.Task originalTask, Task task)
			throws ValidationException {
		final String originalStatusValue = originalTask == null ? null : originalTask.getStatus();
		final String newStatusValue = task.getStatus().getValue();

		if (newStatusValue != null && newStatusValue.equals(originalStatusValue)) {
			return;
		}
		try {
			String queryString = "select sw.id.requireComment from " + StatusWorkflow.class.getSimpleName()
					+ " sw where ";
			if (originalStatusValue == null) {
				queryString += "sw.oldStatus is null ";
			} else {
				queryString += "sw.oldStatus.value = :old ";
			}
			queryString += "and sw.newStatus.value = :new";
			Query query = entityManager.createQuery(queryString);
			if (originalStatusValue != null) {
				query.setParameter("old", originalStatusValue);
			}
			query.setParameter("new", newStatusValue);
			Boolean requireComment = (Boolean) query.getSingleResult();
			if (originalTask != null && requireComment) {
				boolean haveNewComment = false;
				if (task.getComments() != null) {
					for (com.tasktop.c2c.server.tasks.domain.Comment comment : task.getComments()) {
						if (comment.getId() == null && comment.getCommentText() != null
								&& comment.getCommentText().length() > 0) {
							haveNewComment = true;
							break;
						}
					}
				}
				if (!haveNewComment) {
					Errors errors = createErrors(task);
					errors.reject("task.taskStatusRequiresComment", new Object[] { newStatusValue },
							"A comment is required to save a task with status " + newStatusValue);

					throw new ValidationException(errors);
				}
			}
		} catch (NoResultException e) {
			Errors errors = createErrors(task);
			if (originalTask == null) {
				errors.reject("task.invalidTaskCreationStatus", new Object[] { newStatusValue },
						"Cannot create a task with status " + newStatusValue);
			} else {
				errors.reject("task.invalidWorkflow", new Object[] { newStatusValue, originalStatusValue },
						"Invalid workflow");
			}
			throw new ValidationException(errors);
		}
	}

	/**
	 * Set the assignee to the component's default assignee if the task assignee is null or blank. Works for new or
	 * existing tasks.
	 * 
	 * @param toUpdate
	 * @throws ValidationException
	 * @throws EntityNotFoundException
	 */
	private void setDefaultAssigneeIfAppropriate(com.tasktop.c2c.server.internal.tasks.domain.Task toUpdate)
			throws ValidationException, EntityNotFoundException {

		com.tasktop.c2c.server.internal.tasks.domain.Component targetComponent = find(
				com.tasktop.c2c.server.internal.tasks.domain.Component.class, toUpdate.getComponent().getId());

		if (targetComponent.getInitialOwner() != null) {
			if (toUpdate.getAssignee() == null || toUpdate.getAssignee().getLoginName() == null
					|| toUpdate.getAssignee().getLoginName().isEmpty()) {

				toUpdate.setAssignee(targetComponent.getInitialOwner());
			}
		}
	}

	protected void updateCustomFields(com.tasktop.c2c.server.internal.tasks.domain.Task internalTask, Task task)
			throws EntityNotFoundException, ValidationException {
		Map<String, String> customFields = task.getCustomFields();

		if (customFields == null) {
			customFields = new HashMap<String, String>();
			task.setCustomFields(customFields);
		}

		Map<String, Object> fields = new HashMap<String, Object>();
		Errors errors = createErrors(task);

		// prepare values for all valid field names
		for (FieldDescriptor fieldDescriptor : taskCustomFieldService.getCustomFields()) {
			Object fieldValue = customFields.get(fieldDescriptor.getName());
			if (fieldValue != null || customFields.containsKey(fieldDescriptor.getName())) {
				if (fieldValue != null) {
					// data conversion
					if (fieldDescriptor.getFieldType() == FieldType.TIMESTAMP) {
						try {
							fieldValue = new Date(Long.parseLong(fieldValue.toString()));
						} catch (NumberFormatException e) {
							errors.reject("task.invalidCustomFieldDate",
									new Object[] { fieldDescriptor.getDescription(), fieldValue.toString() }, null);
						}
					} else if (fieldDescriptor.getFieldType() == FieldType.MULTI_SELECT) {
						fieldValue = fieldValue.toString().length() == 0 ? new String[0] : fieldValue.toString().split(
								"\\s*,\\s*");
					} else if (fieldDescriptor.getFieldType() == FieldType.CHECKBOX) {
						Boolean value = Boolean.parseBoolean(fieldValue.toString());
						fieldValue = value ? 1 : 0;
					}
				}
				if (fieldValue != null
						&& (fieldDescriptor.getFieldType() == FieldType.SINGLE_SELECT || fieldDescriptor.getFieldType() == FieldType.MULTI_SELECT)) {
					Object[] values = (Object[]) (fieldValue.getClass().isArray() ? fieldValue
							: new Object[] { fieldValue });
					for (Object value : values) {
						if (!fieldDescriptor.getValueStrings().contains(value)) {
							errors.reject("task.invalidCustomFieldValue",
									new Object[] { fieldDescriptor.getDescription(), fieldValue }, null);

						}
					}
				}
				fields.put(fieldDescriptor.getName(), fieldValue);
			}
		}

		// check for invalid field names
		if (fields.size() != customFields.size()) {
			for (String key : customFields.keySet()) {
				if (!fields.containsKey(key)) {
					errors.reject("task.invalidCustomFieldName", new Object[] { key }, null);
				}
			}
		}
		if (errors.hasErrors()) {
			throw new ValidationException(errors);
		}
		taskCustomFieldService.updateTaskCustomFields(internalTask.getId(), fields);
	}

	private void createAttachments(Task task, com.tasktop.c2c.server.internal.tasks.domain.Task managedTask)
			throws ValidationException {
		if (task.getAttachments() == null) {
			return;
		}
		TaskUserProfile curUser = getLoggedInTaskUserProfile();
		for (Attachment a : task.getAttachments()) {
			if (a.getId() == null) {
				saveAttachment(a, managedTask, curUser);
			}
		}
	}

	private void checkForUpdateCollision(long lastUpdateTimeFromDB, String versionFromToUpdate)
			throws ConcurrentUpdateException {
		if (lastUpdateTimeFromDB != Long.parseLong(versionFromToUpdate)) {
			throw new ConcurrentUpdateException();
		}
	}

	private void fillManagedObjects(com.tasktop.c2c.server.internal.tasks.domain.Task managedTask)
			throws EntityNotFoundException {
		managedTask.setProduct(find(com.tasktop.c2c.server.internal.tasks.domain.Product.class, managedTask
				.getProduct().getId()));

		managedTask.setComponent(find(com.tasktop.c2c.server.internal.tasks.domain.Component.class, managedTask
				.getComponent().getId()));

		if (managedTask.getAssignee() != null) {
			managedTask.setAssignee(find(com.tasktop.c2c.server.internal.tasks.domain.Profile.class, managedTask
					.getAssignee().getId()));
		}

		if (managedTask.getDuplicatesByBugId() != null) {
			managedTask.getDuplicatesByBugId().setBugsByDupeOf(
					find(com.tasktop.c2c.server.internal.tasks.domain.Task.class, managedTask.getDuplicatesByBugId()
							.getBugsByDupeOf().getId()));
		}

		for (int i = 0; i < managedTask.getDependenciesesForBlocked().size(); i++) {
			Dependency dep = managedTask.getDependenciesesForBlocked().get(i);
			Dependency managedDep = find(Dependency.class, dep.getId());
			managedTask.getDependenciesesForBlocked().remove(i);
			managedTask.getDependenciesesForBlocked().add(i, managedDep);
		}

		for (int i = 0; i < managedTask.getDependenciesesForDependson().size(); i++) {
			Dependency dep = managedTask.getDependenciesesForDependson().get(i);
			Dependency managedDep = find(Dependency.class, dep.getId());
			managedTask.getDependenciesesForDependson().remove(i);
			managedTask.getDependenciesesForDependson().add(i, managedDep);
		}

		if (managedTask.getCcs() != null) {
			for (int i = 0; i < managedTask.getCcs().size(); i++) {
				Cc cc = managedTask.getCcs().get(i);
				Cc managedCc = find(Cc.class, cc.getId());
				managedTask.getCcs().remove(i);
				managedTask.getCcs().add(i, managedCc);
			}
		}
	}

	private <T> T find(Class<T> clazz, Object id) throws EntityNotFoundException {
		T entity = entityManager.find(clazz, id);
		if (entity != null) {
			return entity;
		}
		throw new EntityNotFoundException(String.format("No %s with Id: %s", clazz.getSimpleName(), String.valueOf(id)));
	}

	private void cascadePersistForUpdate(com.tasktop.c2c.server.internal.tasks.domain.Task task) {

		// Persist Comments
		for (Comment c : task.getComments()) {
			if (c.getId() == null) {
				entityManager.persist(c);
			}
		}

		// Persist CCs
		if (task.getCcs() != null) {
			for (int i = 0; i < task.getCcs().size(); i++) {
				Cc cc = task.getCcs().get(i);
				if (cc.getId() == null || entityManager.find(Cc.class, cc.getId()) == null) {
					entityManager.persist(cc);
				}
			}
		}

		// Persist our dependencies
		for (Dependency dep : task.getDependenciesesForBlocked()) {
			if (dep.getId() == null || entityManager.find(Dependency.class, dep.getId()) == null) {
				associateDependency(dep);
				entityManager.persist(dep);
			}
		}

		for (Dependency dep : task.getDependenciesesForDependson()) {
			if (dep.getId() == null || entityManager.find(Dependency.class, dep.getId()) == null) {
				associateDependency(dep);
				entityManager.persist(dep);
			}
		}
	}

	private void associateDependency(Dependency dep) {
		com.tasktop.c2c.server.internal.tasks.domain.Task bugsByBlocked = dep.getBugsByBlocked();
		com.tasktop.c2c.server.internal.tasks.domain.Task bugsByDependson = dep.getBugsByDependson();
		if (!entityManager.contains(bugsByBlocked)) {
			dep.setBugsByBlocked(entityManager.find(com.tasktop.c2c.server.internal.tasks.domain.Task.class,
					bugsByBlocked.getId()));
			if (bugsByBlocked.getDeltaTs() != null) {
				dep.getBugsByBlocked().setDeltaTs(bugsByBlocked.getDeltaTs());
			}
		}
		if (!entityManager.contains(bugsByDependson)) {
			dep.setBugsByDependson(entityManager.find(com.tasktop.c2c.server.internal.tasks.domain.Task.class,
					bugsByDependson.getId()));
			if (bugsByDependson.getDeltaTs() != null) {
				dep.getBugsByDependson().setDeltaTs(bugsByDependson.getDeltaTs());
			}
		}
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public Task retrieveTask(Integer taskId) throws EntityNotFoundException {

		if (taskId == null) {
			throw new IllegalArgumentException("null is not a valid taskId");
		}

		com.tasktop.c2c.server.internal.tasks.domain.Task task = find(
				com.tasktop.c2c.server.internal.tasks.domain.Task.class, taskId);
		entityManager.refresh(task); // REVIEW, needed for new subTasks, etc

		Task result = mapToDomainObject(task, new DomainConversionContext(entityManager));
		injectAttachmentData(result);

		return result;
	}

	private void injectAttachmentData(Task task) throws EntityNotFoundException {
		for (Attachment attachment : task.getAttachments()) {
			attachment.setUrl(configuration.getWebUrlForAttachment(attachment.getId()));
			AttachmentData data = find(AttachmentData.class, attachment.getId());
			attachment.setByteSize(data.getThedata().length); // FIXME REVIEW performance issue?
		}
	}

	// FIXME TODO XXX Move to task converter
	/**
	 * Inject the custom fields for this task, but remove the mandatory custom fields.
	 * 
	 * @param result
	 * @throws EntityNotFoundException
	 */
	private void injectCustomFields(Task result) throws EntityNotFoundException {

		Map<String, String> exposedFields = retrieveCustomFieldValues(result.getId());

		// Remove fields that are stored as custom fields, but are exposed as domain object attributes.
		// NOTE: Values for these mandatory custom fields are set in the TaskConverter.

		// TaskType
		exposedFields.remove(com.tasktop.c2c.server.internal.tasks.domain.Task.CUSTOM_TYPE_FIELD_NAME);

		// Iteration
		String iterationValue = exposedFields
				.remove(com.tasktop.c2c.server.internal.tasks.domain.Task.CUSTOM_ITERATION_FIELD_NAME);
		Iteration iteration = new Iteration();
		iteration.setValue(iterationValue);
		result.setIteration(iteration);

		// External Task Relations
		exposedFields
				.remove(com.tasktop.c2c.server.internal.tasks.domain.Task.CUSTOM_EXTERNAL_TASK_RELATIONS_FIELD_NAME);

		result.setCustomFields(exposedFields);
	}

	private Map<String, String> retrieveCustomFieldValues(Integer taskId) throws EntityNotFoundException {
		Map<String, String> exposedFields = new HashMap<String, String>();
		Map<String, Object> customFields = taskCustomFieldService.retrieveTaskCustomFields(taskId);
		for (Map.Entry<String, Object> field : customFields.entrySet()) {
			exposedFields.put(field.getKey(), convertCustomFieldValue(field.getValue()));
		}
		return exposedFields;
	}

	private String convertCustomFieldValue(Object value) {
		if (value instanceof Date) {
			// should be using XML time format, but this is consistent with Jackson/JSON marshalling
			return Long.toString(((Date) value).getTime());
		}
		return value == null ? null : value.toString();
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public RepositoryConfiguration getRepositoryContext() {
		RepositoryConfiguration result = new RepositoryConfiguration();
		result.setMilestones(getMilestones());
		result.setPriorities(getPriorities());
		result.setSeverities(getSeverities());
		result.setStatuses(getStatuses());
		result.setStateTransitions(computeStateTransitions());
		result.setUrl(configuration.getExternalTaskServiceUrl());
		result.setUsers(getUsers());
		result.setProducts(getProducts());
		result.setComponents(getComponents());
		result.setResolutions(getResolutions());
		result.setKeywords(findAllKeywords());
		// Defaults
		result.setDefaultPriority(computeDefaultPriority(result.getPriorities()));
		result.setDefaultStatus(computeDefaultStatus(result.getStatuses()));
		result.setDefaultSeverity(computeDefaultSeverity(result.getSeverities()));
		result.setDefaultProduct(computeDefaultProduct(result.getProducts()));
		result.setDefaultResolution(computeDefaultResolution(result.getResolutions()));

		// Parse through our custom fields to pull out special fields now
		List<FieldDescriptor> removeList = new ArrayList<FieldDescriptor>();
		List<FieldDescriptor> descriptorList = taskCustomFieldService.getCustomFields();
		for (FieldDescriptor descriptor : descriptorList) {
			if (com.tasktop.c2c.server.internal.tasks.domain.Task.CUSTOM_TYPE_FIELD_NAME.equals(descriptor.getName())) {
				result.setTaskTypes(descriptor.getValueStrings());
				result.setDefaultType(result.getTaskTypes().get(0));

				removeList.add(descriptor);
			} else if (com.tasktop.c2c.server.internal.tasks.domain.Task.CUSTOM_ITERATION_FIELD_NAME.equals(descriptor
					.getName())) {
				List<Iteration> iterations = new ArrayList<Iteration>(descriptor.getValues().size());
				for (CustomFieldValue value : descriptor.getValues()) {
					Iteration it = new Iteration();
					it.setId(value.getId());
					it.setValue(value.getValue());
					it.setIsActive(value.getIsActive());
					it.setSortkey(value.getSortkey());
					iterations.add(it);
				}
				result.setIterations(iterations);
				result.setDefaultIteration(result.getIterations().get(0));

				removeList.add(descriptor);
			} else if (com.tasktop.c2c.server.internal.tasks.domain.Task.CUSTOM_EXTERNAL_TASK_RELATIONS_FIELD_NAME
					.equals(descriptor.getName())) {
				removeList.add(descriptor);
			}
		}

		// Drop all of the already-handled fields from
		descriptorList.removeAll(removeList);

		result.setCustomFields(descriptorList);

		result.setSavedTaskQueries(this.listSavedQueries());

		return result;
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public List<Keyword> listAllKeywords() {
		return findAllKeywords();
	}

	@SuppressWarnings("unchecked")
	private List<StateTransition> computeStateTransitions() {
		Query query = entityManager.createQuery("select e.oldStatus, e.newStatus, e.id.requireComment from "
				+ StatusWorkflow.class.getSimpleName() + " e");
		List<StateTransition> transitions = new ArrayList<StateTransition>();
		for (Object[] statusWorkflow : ((List<Object[]>) query.getResultList())) {
			com.tasktop.c2c.server.internal.tasks.domain.TaskStatus initialStatus = (com.tasktop.c2c.server.internal.tasks.domain.TaskStatus) statusWorkflow[0];
			com.tasktop.c2c.server.internal.tasks.domain.TaskStatus newStatus = (com.tasktop.c2c.server.internal.tasks.domain.TaskStatus) statusWorkflow[1];

			transitions.add(new StateTransition(initialStatus == null ? null : initialStatus.getValue(), newStatus
					.getValue(), (Boolean) statusWorkflow[2]));
		}
		return transitions;
	}

	@SuppressWarnings("unchecked")
	private List<Keyword> findAllKeywords() {
		Query query = entityManager.createQuery("select e from " + Keyworddef.class.getSimpleName() + " e");
		List<Keyword> keywords = new ArrayList<Keyword>();

		keywords = (List<Keyword>) domainConverter.convert(query.getResultList(), new DomainConversionContext(
				entityManager));
		return keywords;
	}

	private Product computeDefaultProduct(List<Product> products) {
		if (products.isEmpty()) {
			return null;
		}
		return products.get(0); // Better way?
	}

	private TaskSeverity computeDefaultSeverity(List<TaskSeverity> severities) {
		return computeDefaultValue(severities);
	}

	private TaskStatus computeDefaultStatus(List<TaskStatus> statuses) {
		if (statuses.isEmpty()) {
			return null;
		}
		return statuses.get(0);
	}

	private TaskResolution computeDefaultResolution(List<TaskResolution> resolutions) {
		if (resolutions.isEmpty()) {
			return null;
		}
		for (TaskResolution resolution : resolutions) {
			if (resolution.getValue() != null && resolution.getValue().length() > 0) {
				return resolution;
			}
		}
		return resolutions.get(0);
	}

	private <ReferenceType extends AbstractReferenceValue> ReferenceType computeDefaultValue(List<ReferenceType> values) {
		if (values.isEmpty()) {
			return null;
		}
		List<ReferenceType> candidateValues = new ArrayList<ReferenceType>(values);
		for (ReferenceType value : candidateValues) {
			if (value.getValue().equals("---")) {
				candidateValues.remove(value);
				break;
			}
		}
		int median = (candidateValues.size() / 2);
		if (median >= candidateValues.size()) {
			median = candidateValues.size() - 1;
		}
		return candidateValues.get(median);
	}

	private Priority computeDefaultPriority(List<Priority> priorities) {
		return computeDefaultValue(priorities);
	}

	private List<Milestone> getMilestones() {
		Query query = entityManager.createQuery("select milestone from "
				+ com.tasktop.c2c.server.internal.tasks.domain.Milestone.class.getSimpleName()
				+ " milestone order by milestone.sortkey");
		List<Milestone> results = new ArrayList<Milestone>();
		for (Object queryResult : query.getResultList()) {
			com.tasktop.c2c.server.internal.tasks.domain.Milestone milestone = (com.tasktop.c2c.server.internal.tasks.domain.Milestone) queryResult;
			results.add(TaskDomain.createDomain(milestone));
		}
		return results;
	}

	private List<Priority> getPriorities() {
		Query query = entityManager.createQuery("select priority from "
				+ com.tasktop.c2c.server.internal.tasks.domain.Priority.class.getSimpleName() + " priority "
				+ " order by priority.sortkey");
		List<Priority> results = new ArrayList<Priority>();
		for (Object queryResult : query.getResultList()) {
			com.tasktop.c2c.server.internal.tasks.domain.Priority priority = (com.tasktop.c2c.server.internal.tasks.domain.Priority) queryResult;
			results.add(TaskDomain.createDomain(priority));
		}
		return results;
	}

	private List<TaskSeverity> getSeverities() {
		Query query = entityManager.createQuery("select severity from "
				+ com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity.class.getSimpleName() + " severity "
				+ " order by severity.sortkey");
		List<TaskSeverity> results = new ArrayList<TaskSeverity>();
		for (Object queryResult : query.getResultList()) {
			com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity severity = (com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity) queryResult;
			results.add(TaskDomain.createDomain(severity));
		}
		return results;
	}

	private List<TaskResolution> getResolutions() {
		Query query = entityManager.createQuery("select resolution from "
				+ com.tasktop.c2c.server.internal.tasks.domain.Resolution.class.getSimpleName() + " resolution "
				+ " WHERE resolution.isactive = true order by resolution.sortkey");
		List<TaskResolution> results = new ArrayList<TaskResolution>();
		for (Object queryResult : query.getResultList()) {
			com.tasktop.c2c.server.internal.tasks.domain.Resolution resolution = (com.tasktop.c2c.server.internal.tasks.domain.Resolution) queryResult;
			results.add(TaskDomain.createDomain(resolution));
		}
		return results;
	}

	private List<TaskStatus> getStatuses() {
		Query query = entityManager.createQuery("select status from "
				+ com.tasktop.c2c.server.internal.tasks.domain.TaskStatus.class.getSimpleName() + " status "
				+ " order by status.sortkey");
		List<TaskStatus> results = new ArrayList<TaskStatus>();
		for (Object queryResult : query.getResultList()) {
			com.tasktop.c2c.server.internal.tasks.domain.TaskStatus status = (com.tasktop.c2c.server.internal.tasks.domain.TaskStatus) queryResult;
			results.add(TaskDomain.createDomain(status));
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> getAll(Class<?> internalClass, Class<T> resultClass, String orderByField) {
		String queryStr = String.format("select curTarget from %s curTarget order by curTarget.%s",
				internalClass.getSimpleName(), orderByField);
		Query query = entityManager.createQuery(queryStr);
		return (List<T>) domainConverter.convert(query.getResultList(), new DomainConversionContext(entityManager));
	}

	private List<TaskUserProfile> getUsers() {
		return getAll(com.tasktop.c2c.server.internal.tasks.domain.Profile.class, TaskUserProfile.class, "loginName");
	}

	private List<Product> getProducts() {
		return getAll(com.tasktop.c2c.server.internal.tasks.domain.Product.class, Product.class, "name");
	}

	private List<com.tasktop.c2c.server.tasks.domain.Component> getComponents() {
		return getAll(com.tasktop.c2c.server.internal.tasks.domain.Component.class, Component.class, "name");
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public Attachment retrieveAttachment(Integer attachmentId) throws EntityNotFoundException {

		com.tasktop.c2c.server.internal.tasks.domain.Attachment internalAttachment = find(
				com.tasktop.c2c.server.internal.tasks.domain.Attachment.class, attachmentId);

		AttachmentData data = find(AttachmentData.class, attachmentId);

		Attachment attachment = (Attachment) domainConverter.convert(internalAttachment, new DomainConversionContext(
				entityManager));
		attachment.setAttachmentData(data.getThedata());
		attachment.setByteSize(attachment.getAttachmentData().length);
		attachment.setUrl(configuration.getWebUrlForAttachment(attachmentId));

		return attachment;
	}

	@Override
	@Secured({ Role.Community, Role.User })
	public AttachmentHandle saveAttachment(TaskHandle taskHandle, Attachment attachment) throws ValidationException,
			EntityNotFoundException, ConcurrentUpdateException {
		return saveAttachment(taskHandle, attachment, null);
	}

	@Override
	@Secured({ Role.Community, Role.User })
	public AttachmentHandle saveAttachment(TaskHandle taskHandle, Attachment attachment,
			com.tasktop.c2c.server.tasks.domain.Comment comment) throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		com.tasktop.c2c.server.internal.tasks.domain.Task managedTask = find(
				com.tasktop.c2c.server.internal.tasks.domain.Task.class, taskHandle.getId());
		TaskUserProfile user = getLoggedInTaskUserProfile();

		checkForUpdateCollision(managedTask.getDeltaTs().getTime(), taskHandle.getVersion());

		com.tasktop.c2c.server.internal.tasks.domain.Attachment internalAttachment = saveAttachment(attachment,
				managedTask, user);

		if (comment != null) {
			comment.setAuthor(getLoggedInTaskUserProfile());
			validate(comment);
			Comment internalComment = TaskDomain.createManagedComment(comment);
			internalComment.setTask(managedTask);
			managedTask.getComments().add(internalComment);
		}
		managedTask.preUpdate();
		entityManager.flush(); // so that comment is seen in next retrieve.

		List<TaskActivity> activities = new ArrayList<TaskActivity>();
		Task task = retrieveTask(taskHandle.getId());
		activities.add(activityService.constructAttachment(task, attachment));
		if (comment != null) {
			activities.add(activityService.constructComment(task, comment));
		}
		sendActivityEvent(activities);

		return new AttachmentHandle(internalAttachment.getId(), new TaskHandle(managedTask.getId(),
				Long.toString(convertToMySQLTimeInMilliseconds(managedTask.getDeltaTs()))));
	}

	/**
	 * Mysql drop the millisecond resolution on dates.
	 * 
	 * @return
	 */
	private long convertToMySQLTimeInMilliseconds(Date date) {
		long fullTime = date.getTime();
		return fullTime - (fullTime % 1000);
	}

	private com.tasktop.c2c.server.internal.tasks.domain.Attachment saveAttachment(Attachment attachment,
			com.tasktop.c2c.server.internal.tasks.domain.Task managedTask, TaskUserProfile user)
			throws ValidationException {

		validate(attachment);
		attachment.setSubmitter(user);
		com.tasktop.c2c.server.internal.tasks.domain.Attachment internalAttachment = TaskDomain
				.createManaged(attachment);
		internalAttachment.setBugs(managedTask);
		entityManager.persist(internalAttachment);
		entityManager.flush(); // Need the id;

		AttachmentData data = new AttachmentData();
		data.setAttachment(internalAttachment);
		data.setThedata(attachment.getAttachmentData());
		entityManager.persist(data);
		return internalAttachment;
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public List<TaskActivity> getRecentActivity(Region region) {
		return activityService.getRecentActivity(region);
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public List<TaskActivity> listActivity(Date date) {
		return activityService.listActivity(date);
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public List<TaskActivity> listTaskActivity(Integer taskId) throws EntityNotFoundException {
		return activityService.listTaskActivity(taskId);
	}

	@Override
	@Secured(Role.Admin)
	public Product createProduct(Product product) throws ValidationException {
		validate(product);

		com.tasktop.c2c.server.internal.tasks.domain.Product internalProduct = TaskDomain.createManaged(product);
		internalProduct.setAllowsUnconfirmed(true);
		internalProduct.setDefaultmilestone(DEFAULT_MILESTONE);
		internalProduct.setMaxvotesperbug((short) 10000);
		internalProduct.setAllowsUnconfirmed(false);
		Classification c = entityManager.find(Classification.class, (short) 1);
		internalProduct.setClassifications(c);
		internalProduct.setIsactive(true);
		entityManager.persist(internalProduct);
		entityManager.flush(); // Need the id.

		// create associated milestone
		com.tasktop.c2c.server.internal.tasks.domain.Milestone internalMilestone = new com.tasktop.c2c.server.internal.tasks.domain.Milestone();
		internalMilestone.setProduct(internalProduct);
		internalMilestone.setValue(DEFAULT_MILESTONE);
		internalMilestone.setSortkey((short) 0);
		entityManager.persist(internalMilestone);
		entityManager.flush();

		// reload the product to get the associate milestones and components
		entityManager.refresh(internalProduct);
		return (Product) domainConverter.convert(internalProduct, new DomainConversionContext(entityManager));
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public Product retrieveProduct(Integer productId) throws EntityNotFoundException {
		if (productId == null) {
			throw new IllegalArgumentException("null is not a valid ID");
		}
		com.tasktop.c2c.server.internal.tasks.domain.Product product = find(
				com.tasktop.c2c.server.internal.tasks.domain.Product.class, productId.shortValue());
		return (Product) domainConverter.convert(product, new DomainConversionContext(entityManager));
	}

	@Override
	@Secured(Role.Admin)
	public Product updateProduct(Product product) throws ValidationException, EntityNotFoundException {
		// validate the product public DO
		validate(product);

		com.tasktop.c2c.server.internal.tasks.domain.Product managedProduct = find(
				com.tasktop.c2c.server.internal.tasks.domain.Product.class, product.getId().shortValue());
		TaskDomain.fillManaged(managedProduct, product);

		// validate the product internal DO
		internalValidate(managedProduct);
		entityManager.flush();
		entityManager.refresh(managedProduct);

		return (Product) domainConverter.convert(managedProduct, new DomainConversionContext(entityManager));
	}

	private void internalValidate(com.tasktop.c2c.server.internal.tasks.domain.Product managedProduct)
			throws ValidationException {
		super.validate(managedProduct, internalValidator);
	}

	private void doSafeValidate(Object o, List<ValidationException> validationErrors) {
		try {
			validate(o);
		} catch (ValidationException ve) {
			validationErrors.add(ve);
		}
	}

	private ValidationException combineValidationExceptions(Product product, List<ValidationException> validationErrors) {

		Errors allErrors = new MapBindingResult(new HashMap<String, String>(), String.valueOf(product.getId()));

		for (ValidationException curVe : validationErrors) {
			BeanPropertyBindingResult beanError = (BeanPropertyBindingResult) curVe.getErrors();
			// Grab the target object out, and cast as appropriate to get it.
			AbstractDomainObject targetObj = (AbstractDomainObject) beanError.getTarget();

			// Update our message string to contain our new code.

			// allErrors.addAllErrors(beanError);

			// Loop through each field, tack on our name, and add it to the final list.
			for (FieldError curError : beanError.getFieldErrors()) {
				allErrors.reject(String.format("%s.%s[%s|%s]", curError.getCode(), curError.getField(), targetObj
						.getClass().getSimpleName(), targetObj.getId()), curVe.getMessage());
			}
		}

		return new ValidationException(allErrors);
	}

	@Override
	@Secured(Role.Admin)
	public Product updateProductTree(Product product) throws ValidationException, EntityNotFoundException {

		// First, perform validation on all objects in the tree.
		ArrayList<ValidationException> validationErrors = new ArrayList<ValidationException>();

		doSafeValidate(product, validationErrors);

		int newItemId = -1;
		boolean isNew = false;

		for (Component curComponent : product.getComponents()) {
			if (curComponent.getId() == null) {
				// Add an ID to this object, for validation only - this allows validation error messages to be added to
				// the correct items in the client
				curComponent.setId(newItemId);
				isNew = true;

				// Decrement our ID so that the next one in the sequence is queued up in case there are any others.
				newItemId--;
			}

			doSafeValidate(curComponent, validationErrors);

			// Now wipe out our ID again. Only do this if we don't yet have any errors - if we do have errors, we won't
			// save these objects and we'll need the IDs later to construct validation messages.
			if (isNew && validationErrors.isEmpty()) {
				curComponent.setId(null);
			}
			isNew = false;
		}

		// Reset our ID, since we're onto a new set of objects.
		newItemId = -1;

		for (Milestone curMilestone : product.getMilestones()) {
			if (curMilestone.getId() == null) {
				// Add an ID to this object, for validation only - this allows validation error messages to be added to
				// the correct items in the client
				curMilestone.setId(newItemId);
				isNew = true;

				// Decrement our ID so that the next one in the sequence is queued up in case there are any others.
				newItemId--;
			}

			doSafeValidate(curMilestone, validationErrors);

			// Now wipe out our ID again. Only do this if we don't yet have any errors - if we do have errors, we won't
			// save these objects and we'll need the IDs later to construct validation messages.
			if (isNew && validationErrors.isEmpty()) {
				curMilestone.setId(null);
			}
			isNew = false;
		}

		if (validationErrors.size() > 0) {
			// We've got some errors, so pack them up and send them back to the user now.
			ValidationException ve = combineValidationExceptions(product, validationErrors);
			throw ve;
		}

		// Now, perform our save of our product and each component.
		com.tasktop.c2c.server.internal.tasks.domain.Product managedProduct = find(
				com.tasktop.c2c.server.internal.tasks.domain.Product.class, product.getId().shortValue());
		TaskDomain.fillManaged(managedProduct, product);

		for (Component curComponent : product.getComponents()) {

			com.tasktop.c2c.server.internal.tasks.domain.Component managedComponent = null;

			if (curComponent.getId() == null) {
				// This is a new component - create one now.
				managedComponent = TaskDomain.createManaged(curComponent);
				managedComponent.setProduct(managedProduct);
				entityManager.persist(managedComponent);
				managedProduct.getComponents().add(managedComponent);
			} else {
				managedComponent = find(com.tasktop.c2c.server.internal.tasks.domain.Component.class, curComponent
						.getId().shortValue());
			}

			TaskDomain.fillManaged(managedComponent, curComponent, entityManager);

		}

		for (Milestone curMilestone : product.getMilestones()) {

			com.tasktop.c2c.server.internal.tasks.domain.Milestone managedMilestone = null;

			if (curMilestone.getId() == null) {
				// This is a new component - create one now.
				managedMilestone = TaskDomain.createManaged(curMilestone);
				managedMilestone.setProduct(managedProduct);
				entityManager.persist(managedMilestone);
				managedProduct.getMilestones().add(managedMilestone);
			} else {
				managedMilestone = find(com.tasktop.c2c.server.internal.tasks.domain.Milestone.class,
						curMilestone.getId());
			}
			if (!curMilestone.getValue().equals(managedMilestone.getValue())) {
				handleMilestoneValueChanged(managedProduct.getId(), curMilestone.getValue(),
						managedMilestone.getValue());
			}
			TaskDomain.fillManaged(managedMilestone, curMilestone, entityManager);

		}

		// Perform our save now - if this throws an exception, then that'll prevent the return from firing.
		entityManager.flush();

		Product retProduct = (Product) domainConverter.convert(managedProduct, new DomainConversionContext(
				entityManager));

		return retProduct;
	}

	private void handleMilestoneValueChanged(Short productId, String newValue, String oldValue) {
		List<com.tasktop.c2c.server.internal.tasks.domain.Task> toUpdate = entityManager
				.createQuery(
						"SELECT t FROM " + com.tasktop.c2c.server.internal.tasks.domain.Task.class.getSimpleName()
								+ " t " + "WHERE t.product.id = :productId AND t.targetMilestone = :milestoneValue")
				.setParameter("productId", productId).setParameter("milestoneValue", oldValue).getResultList();

		for (com.tasktop.c2c.server.internal.tasks.domain.Task t : toUpdate) {
			t.setTargetMilestone(newValue);
		}

	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public List<Product> listAllProducts() {
		return getProducts();
	}

	private long numAssociatedTasks(String field, Object value) {
		String queryStr = String.format("select count(curTarget) from %s curTarget where curTarget.%s = :objValue",
				com.tasktop.c2c.server.internal.tasks.domain.Task.class.getSimpleName(), field);
		Query countQuery = entityManager.createQuery(queryStr);
		countQuery.setParameter("objValue", value);
		return (Long) countQuery.getSingleResult();
	}

	@Override
	@Secured(Role.Admin)
	public void deleteProduct(Integer productId) throws ValidationException, EntityNotFoundException {
		// Get our product now.
		com.tasktop.c2c.server.internal.tasks.domain.Product delProduct = find(
				com.tasktop.c2c.server.internal.tasks.domain.Product.class, productId.shortValue());

		// Check if there are any tasks associated with this component
		long numTasks = numAssociatedTasks("product.id", delProduct.getId());

		// if there are, throw a ValidationException.
		if (numTasks > 0) {
			String defaultErrorMsg = "Cannot delete product because tasks still refer to it";
			Errors allErrors = new MapBindingResult(new HashMap<String, String>(), String.valueOf(productId));
			allErrors.reject("product.referencedByTasks", defaultErrorMsg);
			throw new ValidationException(defaultErrorMsg, allErrors);
		}

		// If we're ok to proceed, then go ahead and delete now.
		entityManager.remove(delProduct);
		entityManager.flush();
	}

	@Override
	@Secured(Role.Admin)
	public Component createComponent(Component newComponent) throws ValidationException {
		validate(newComponent);

		com.tasktop.c2c.server.internal.tasks.domain.Component internalComponent = TaskDomain
				.createManaged(newComponent);

		// Push this out to the database, and then turn around and reload it to
		// ensure we have the latest version of the object, including
		// auto-populated values like the ID.
		entityManager.persist(internalComponent);
		entityManager.flush();
		entityManager.refresh(internalComponent);

		return (Component) domainConverter.convert(internalComponent, new DomainConversionContext(entityManager));
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public Component retrieveComponent(Integer componentId) throws EntityNotFoundException {
		if (componentId == null) {
			throw new IllegalArgumentException("null is not a valid ID");
		}
		com.tasktop.c2c.server.internal.tasks.domain.Component component = find(
				com.tasktop.c2c.server.internal.tasks.domain.Component.class, componentId.shortValue());

		return (Component) domainConverter.convert(component, new DomainConversionContext(entityManager));
	}

	@Override
	@Secured(Role.Admin)
	public Component updateComponent(Component domainComponent) throws ValidationException, EntityNotFoundException {
		validate(domainComponent);

		com.tasktop.c2c.server.internal.tasks.domain.Component managedComponent = find(
				com.tasktop.c2c.server.internal.tasks.domain.Component.class, domainComponent.getId().shortValue());

		TaskDomain.fillManaged(managedComponent, domainComponent, entityManager);

		entityManager.flush();
		entityManager.refresh(managedComponent);

		return (Component) domainConverter.convert(managedComponent, new DomainConversionContext(entityManager));
	}

	@Override
	@Secured({ Role.Observer, Role.User })
	public List<Component> listAllComponents() {
		return getComponents();
	}

	@Override
	@Secured(Role.Admin)
	public void deleteComponent(Integer componentId) throws ValidationException, EntityNotFoundException {
		// Get our component now.
		com.tasktop.c2c.server.internal.tasks.domain.Component delComponent = find(
				com.tasktop.c2c.server.internal.tasks.domain.Component.class, componentId.shortValue());

		// Check if there are any tasks associated with this component
		long numTasks = numAssociatedTasks("component.id", delComponent.getId());

		// if there are, throw a ValidationException.
		if (numTasks > 0) {
			String defaultErrorMsg = "Cannot delete component because tasks still refer to it";
			Errors allErrors = new MapBindingResult(new HashMap<String, String>(), String.valueOf(componentId));
			allErrors.reject("component.referencedByTasks", defaultErrorMsg);
			throw new ValidationException(defaultErrorMsg, allErrors);
		}

		// If we're ok to proceed, then go ahead and delete now.
		entityManager.remove(delComponent);
		entityManager.flush();
	}

	@Override
	@Secured(Role.Admin)
	public void deleteMilestone(Integer milestoneId) throws ValidationException, EntityNotFoundException {

		// Get our milestone now.
		com.tasktop.c2c.server.internal.tasks.domain.Milestone delMilestone = find(
				com.tasktop.c2c.server.internal.tasks.domain.Milestone.class, milestoneId);

		// First, check if there are any tasks associated with this milestone
		long numTasks = numAssociatedTasks("targetMilestone", delMilestone.getValue());

		// if there are, throw a ValidationException.
		if (numTasks > 0) {
			String defaultErrorMsg = "Cannot delete release because tasks still refer to it";
			// HashMap<String, String> binderMap = new HashMap<String, String>();
			// Errors allErrors = new MapBindingResult(binderMap, String.valueOf(milestoneId));
			delMilestone.setProduct(null);
			Errors allErrors = new BeanPropertyBindingResult(delMilestone, String.valueOf(milestoneId));
			allErrors.reject("milestone.referencedByTasks", defaultErrorMsg);
			ValidationException ve = new ValidationException(defaultErrorMsg, allErrors);
			throw ve;
		}

		// Then, check if we're the default milestone for the product
		if (delMilestone.getValue().equals(delMilestone.getProduct().getDefaultmilestone())) {
			String defaultErrorMsg = "Cannot delete release because it is the default release for a product";
			HashMap<String, String> binderMap = new HashMap<String, String>();
			Errors allErrors = new MapBindingResult(binderMap, String.valueOf(milestoneId));
			allErrors.reject("milestone.defaultForProduct", defaultErrorMsg);
			throw new ValidationException(defaultErrorMsg, allErrors);
		}

		// If we're ok to proceed, then go ahead and delete now.
		entityManager.remove(delMilestone);
		entityManager.flush();
	}

	@Secured(Role.System)
	@Override
	public void replicateTeam(Team team) {
		for (TaskUserProfile profile : team.getMembers()) {
			try {
				getOrCreateFullProfile(profile);
			} catch (EntityNotFoundException e) {
				// ignore
			}
		}
	}

	/**
	 * @param eventService
	 *            the eventService to set
	 */
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		((EventServiceClient) eventService).setBaseUrl(configuration.getProfileBaseUrl() + "/api/event");
	}

	@Override
	@Secured(Role.Admin)
	public Keyword createKeyword(Keyword keyword) throws ValidationException {
		validate(keyword);

		if (findKeyworddefByName(keyword) != null) {
			Errors errors = createErrors(keyword);
			errors.reject("keyword.nameExists");
			throw new ValidationException(errors);
		}

		Keyworddef keyworddef = createManaged(keyword);
		return (Keyword) domainConverter.convert(keyworddef, new DomainConversionContext(entityManager));
	}

	private Keyworddef createManaged(Keyword keyword) {
		Keyworddef keywordDef = new Keyworddef();
		keywordDef.setName(keyword.getName());
		keywordDef.setDescription(keyword.getDescription());
		entityManager.persist(keywordDef);
		entityManager.flush();
		entityManager.refresh(keywordDef); // get id
		return keywordDef;
	}

	@Override
	@Secured(Role.Admin)
	public Keyword updateKeyword(Keyword keyword) throws EntityNotFoundException, ValidationException {
		Keyworddef keyworddef = entityManager.find(Keyworddef.class, keyword.getId().shortValue());
		if (keyworddef == null) {
			throw new EntityNotFoundException();
		}
		validate(keyword);
		keyworddef.setName(keyword.getName());
		keyworddef.setDescription(keyword.getDescription());
		entityManager.persist(keyworddef);

		return (Keyword) domainConverter.convert(keyworddef, new DomainConversionContext(entityManager));
	}

	@Override
	@Secured(Role.Admin)
	public void deleteKeyword(Integer keywordId) throws EntityNotFoundException, ValidationException {
		Keyworddef keyworddef = entityManager.find(Keyworddef.class, keywordId.shortValue());
		if (keyworddef == null) {
			throw new EntityNotFoundException();
		}

		if (keyworddef.getKeywordses().size() > 0) {
			Errors errors = createErrors(keyworddef);
			errors.reject("keyword.mappedToTasks");
			throw new ValidationException(errors);
		}
		entityManager.remove(keyworddef);
	}

	@Secured(Role.User)
	@Override
	public SavedTaskQuery createQuery(SavedTaskQuery query) throws ValidationException {
		validate(query);
		verifyQueryNameUnique(query);
		com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery managedQuery = TaskDomain.createManaged(query);
		managedQuery.setProfile(getLoggedInDomainProfile());
		entityManager.persist(managedQuery);
		entityManager.flush();
		return (SavedTaskQuery) domainConverter.convert(managedQuery, new DomainConversionContext(entityManager));
	}

	private void verifyQueryNameUnique(SavedTaskQuery query) throws ValidationException {
		String where = "WHERE q.name = :name AND q.profile.id = :profileId";
		if (query.getId() != null) {
			where += " AND q.id != :existingId";
		}
		Query q = entityManager
				.createQuery(
						"SELECT q FROM "
								+ com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery.class.getSimpleName()
								+ " q " + where).setParameter("name", query.getName())
				.setParameter("profileId", getLoggedInDomainProfile().getId());
		if (query.getId() != null) {
			q.setParameter("existingId", query.getId());
		}

		if (!q.getResultList().isEmpty()) {
			Errors errors = createErrors(query);
			errors.reject("query.nameUnique");
			throw new ValidationException(errors);
		}
	}

	@Secured(Role.User)
	@Override
	public SavedTaskQuery updateQuery(SavedTaskQuery query) throws ValidationException, EntityNotFoundException {
		validate(query);
		verifyQueryNameUnique(query);

		com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery managedQuery = find(
				com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery.class, query.getId());

		if (!managedQuery.getProfile().equals(getLoggedInDomainProfile())) {
			throw new InsufficientPermissionsException("Can not update a query you do not own");
		}
		TaskDomain.fillManaged(managedQuery, query);

		return (SavedTaskQuery) domainConverter.convert(managedQuery, new DomainConversionContext(entityManager));
	}

	private List<SavedTaskQuery> listSavedQueries() {
		List<SavedTaskQuery> result = new ArrayList<SavedTaskQuery>();

		// Find the user's queries
		Profile profile = getLoggedInDomainProfile();
		if (profile != null) {
			List<com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery> userQueries = entityManager
					.createQuery(
							"SELECT query FROM "
									+ com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery.class.getSimpleName()
									+ " query WHERE query.profile.id = :profileId")
					.setParameter("profileId", profile.getId()).getResultList();
			for (com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery query : userQueries) {
				result.add(SavedTaskQueryConverter.copy(query));
			}
		}

		// TODO public queries

		return result;
	}

	@Secured(Role.User)
	@Override
	public void deleteQuery(Integer queryId) throws ValidationException, EntityNotFoundException {
		com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery query = entityManager.find(
				com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery.class, queryId);
		if (query == null) {
			throw new EntityNotFoundException();
		}

		if (!query.getProfile().equals(getLoggedInDomainProfile())) {
			throw new InsufficientPermissionsException("Can not delete a query you do not own");
		}
		entityManager.remove(query);
	}

	private FieldDescriptor getIterationCutomFieldDescriptor() {
		for (FieldDescriptor descriptor : taskCustomFieldService.getCustomFields()) {
			if (descriptor.getName().equals(
					com.tasktop.c2c.server.internal.tasks.domain.Task.CUSTOM_ITERATION_FIELD_NAME)) {
				return descriptor;
			}
		}
		return null;
	}

	@Override
	public Iteration createIteration(Iteration iteration) throws ValidationException {
		validate(iteration);
		FieldDescriptor iterationField = getIterationCutomFieldDescriptor();
		if (iteration.getSortkey() == null) {
			iteration.setSortkey((short) (iterationField.getValues().get(iterationField.getValues().size() - 1)
					.getSortkey() + 1));
		}
		validateValueUnique(iteration, iterationField.getValues());
		taskCustomFieldService.addNewValue(iterationField, iteration);
		entityManager.flush();
		// Hacky way to get the new value's id
		iterationField = getIterationCutomFieldDescriptor();
		for (CustomFieldValue it : iterationField.getValues()) {
			if (it.getValue().equals(iteration.getValue())) {
				iteration.setId(it.getId());
				break;
			}
		}
		return iteration;
	}

	private void validateValueUnique(CustomFieldValue value, List<CustomFieldValue> values) throws ValidationException {
		for (CustomFieldValue otherValue : values) {
			if (otherValue.getValue().equals(value.getValue()) && !otherValue.getId().equals(value.getId())) {
				Errors errors = createErrors(value);
				errors.reject("customField.value.nameUnique");
				throw new ValidationException(errors);
			}
		}
	}

	@Override
	public Iteration updateIteration(Iteration iteration) throws ValidationException, EntityNotFoundException {
		validate(iteration);
		FieldDescriptor iterationField = getIterationCutomFieldDescriptor();
		validateValueUnique(iteration, iterationField.getValues());
		taskCustomFieldService.udpateValue(iterationField, iteration);
		return iteration;
	}

	@Override
	public FieldDescriptor createCustomField(FieldDescriptor customField) throws ValidationException {
		return taskCustomFieldService.createCustomField(customField);
	}

	@Override
	public FieldDescriptor updateCustomField(FieldDescriptor customField) throws ValidationException,
			EntityNotFoundException {
		return taskCustomFieldService.updateCustomField(customField);
	}

	@Override
	public void deleteCustomField(Integer customFieldId) throws EntityNotFoundException {
		FieldDescriptor descriptor = null;
		for (FieldDescriptor fieldDesc : taskCustomFieldService.getCustomFields()) {
			if (fieldDesc.getId().equals(customFieldId)) {
				descriptor = fieldDesc;
				break;
			}
		}
		if (descriptor == null) {
			throw new EntityNotFoundException();
		}
		taskCustomFieldService.removeCustomField(descriptor);
	}

	@Autowired
	private CommentWikiRenderer wikiRenderer;

	@Override
	public String renderWikiMarkupAsHtml(String markup) {
		return wikiRenderer.render(markup);
	}
}
