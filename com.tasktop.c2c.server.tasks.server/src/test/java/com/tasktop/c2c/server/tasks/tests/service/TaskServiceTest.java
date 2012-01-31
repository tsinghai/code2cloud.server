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
package com.tasktop.c2c.server.tasks.tests.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.ObjectError;

import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.WrappedCheckedException;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.SortInfo.Order;
import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.common.tests.util.ValidationAssert;
import com.tasktop.c2c.server.event.domain.Event;
import com.tasktop.c2c.server.event.domain.TaskActivityEvent;
import com.tasktop.c2c.server.event.service.EventService;
import com.tasktop.c2c.server.internal.tasks.domain.Component;
import com.tasktop.c2c.server.internal.tasks.domain.Milestone;
import com.tasktop.c2c.server.internal.tasks.domain.Priority;
import com.tasktop.c2c.server.internal.tasks.domain.Product;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.internal.tasks.domain.Resolution;
import com.tasktop.c2c.server.internal.tasks.domain.StatusWorkflow;
import com.tasktop.c2c.server.internal.tasks.domain.Task;
import com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.internal.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.DomainConversionContext;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.DomainConverter;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.ProductConverter;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.ProfileConverter;
import com.tasktop.c2c.server.internal.tasks.service.TaskCustomFieldService;
import com.tasktop.c2c.server.internal.tasks.service.TaskServiceDependencies;
import com.tasktop.c2c.server.tasks.domain.AbstractDomainObject;
import com.tasktop.c2c.server.tasks.domain.AbstractReferenceValue;
import com.tasktop.c2c.server.tasks.domain.AttachmentHandle;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.ExternalTaskRelation;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.QuerySpec;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.TaskActivity;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.FieldUpdate;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.Type;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSummary;
import com.tasktop.c2c.server.tasks.domain.TaskSummaryItem;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.Team;
import com.tasktop.c2c.server.tasks.domain.WorkLog;
import com.tasktop.c2c.server.tasks.service.TaskService;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockComponentFactory;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockKeyworddefFactory;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockMilestoneFactory;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockPriorityFactory;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockProductFactory;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockProfileFactory;
import com.tasktop.c2c.server.tasks.tests.domain.mock.MockTaskFactory;
import com.tasktop.c2c.server.tasks.tests.util.TestSecurity;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/applicationContext-test.xml" })
@Transactional
public class TaskServiceTest {

	private static final int NUM_TASKS_TO_CREATE = TaskService.DEFAULT_PAGE_INFO.getSize() + 2;

	@Qualifier("main")
	@Autowired
	protected TaskService taskService;

	@PersistenceContext(unitName = "tasksDomain")
	protected EntityManager entityManager;

	@Qualifier("switching")
	@Autowired
	private DataSource dataSource;

	protected Profile currentUser = null;

	private static boolean didAutoIncrement = false;

	@Autowired
	private DomainConverter domainConverter;

	@Autowired
	private ProductConverter productConverter;

	@Autowired
	protected TaskCustomFieldService customFieldService;
	private Set<FieldDescriptor> createdFields = new HashSet<FieldDescriptor>();

	private String[] taskTypeValues;
	private String[] iterationValues;

	private Event lastEvent = null;

	@Before
	public void resetAutoIncrements() throws SQLException {
		if (didAutoIncrement) {
			return;
		}
		for (String tablename : Arrays.asList("components", "bugs")) {
			dataSource.getConnection().createStatement().execute("ALTER TABLE " + tablename + " AUTO_INCREMENT=5");
		}
		didAutoIncrement = true;
	}

	protected void setupAuthContext() {
		if (currentUser == null) {
			currentUser = MockProfileFactory.create(entityManager);
			String username = "testuser" + System.currentTimeMillis();
			currentUser.setLoginName(username);
			entityManager.flush();
			TestSecurity.login(currentUser);
		}
	}

	@Before
	public void before() throws Exception {
		// for subclass to override
		setupAuthContext();

		taskTypeValues = computeCustomFieldValues("cf_tasktype");
		iterationValues = computeCustomFieldValues("cf_iteration");
		lastEvent = null;
	}

	@Before
	public void injectMockServices() {
		((TaskServiceDependencies) taskService).setEventService(new EventService() {

			@Override
			public void publishEvent(Event event) {
				lastEvent = event;
			}
		});
	}

	private String[] computeCustomFieldValues(String tableName) {
		@SuppressWarnings("unchecked")
		List<String> results = entityManager.createNativeQuery(
				String.format("select `value` from %s order by sortkey", tableName)).getResultList();
		return results.toArray(new String[results.size()]);
	}

	@After
	public void after() {
		if (!createdFields.isEmpty()) {
			// Special transaction handling is necessary here due to how MySQL handles alter table statements.
			// See http://ebergen.net/wordpress/2007/05/07/how-alter-table-locks-tables-and-handles-transactions
			entityManager.clear();
			entityManager.createNativeQuery("rollback").executeUpdate();
			for (FieldDescriptor descriptor : createdFields) {
				try {
					customFieldService.removeCustomField(descriptor);
				} catch (Throwable t) {
					// ignore
					t.printStackTrace();
				}
			}
		}
	}

	private List<TaskSeverity> severities;
	private TaskStatus openStatus = null;
	private TaskStatus closedStatus = null;
	private List<Task> tasks;

	private static final int MINIMUM_DATE_RESOLUTION = 1000;

	private int count(Class<?> objToCount) {

		Query countQ = entityManager
				.createQuery(String.format("SELECT count(o) FROM %s o", objToCount.getSimpleName()));

		return ((Number) countQ.getSingleResult()).intValue();
	}

	private void setupTestData() {
		setupTestData(NUM_TASKS_TO_CREATE);
	}

	@SuppressWarnings("unchecked")
	private void setupTestData(int numberOfTasksToCreate) {
		assertTrue(numberOfTasksToCreate > 0);

		computeSeverities();
		assertTrue(severities.size() > 3);
		List<TaskStatus> statuses = entityManager.createQuery(
				"select e from " + TaskStatus.class.getSimpleName() + " e").getResultList();
		for (TaskStatus status : statuses) {
			if (status.getIsOpen()) {
				openStatus = status;
			} else {
				closedStatus = status;
			}
		}
		assertNotNull(openStatus);
		assertNotNull(closedStatus);

		Product product = MockProductFactory.create(entityManager);
		Profile profile = MockProfileFactory.create(entityManager);
		com.tasktop.c2c.server.internal.tasks.domain.Priority priority = MockPriorityFactory.create(entityManager);

		tasks = MockTaskFactory.create(entityManager, product, profile, numberOfTasksToCreate);

		long initalTime = System.currentTimeMillis() - (MINIMUM_DATE_RESOLUTION * tasks.size());

		for (int x = 0; x < tasks.size(); ++x) {
			Task task = tasks.get(x);
			task.setSeverity(severities.get(x % severities.size()).getValue());
			task.setStatus(x % 3 == 0 ? closedStatus.getValue() : openStatus.getValue());
			task.setPriority(priority.getValue());
			task.setCreationTs(new Date(initalTime + (x * MINIMUM_DATE_RESOLUTION)));
			task.setDeltaTs(new Date(task.getCreationTs().getTime() + 100000));
		}

		entityManager.flush();
		tasks.clear();

		List<Object> results = entityManager.createQuery(
				"select task from " + com.tasktop.c2c.server.internal.tasks.domain.Task.class.getSimpleName()
						+ " task ").getResultList();
		for (Object result : results) {
			tasks.add((Task) result);
		}

	}

	@SuppressWarnings("unchecked")
	private void computeSeverities() {
		severities = entityManager.createQuery(
				"select e from " + TaskSeverity.class.getSimpleName() + " e order by e.sortkey asc").getResultList();
	}

	@Test
	public void testFindTasksWithCriteria_CustomFields() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		FieldDescriptor descriptor = new FieldDescriptor();
		descriptor.setAvailableForNewTasks(true);
		descriptor.setDescription("TestA");
		descriptor.setFieldType(FieldType.SINGLE_SELECT);
		descriptor.setName("testA" + System.currentTimeMillis());
		descriptor.setValueStrings(Arrays.asList("one", "two"));

		createdFields.add(descriptor);

		customFieldService.createCustomField(descriptor);

		setupTestData();

		// sanity
		assertTrue(tasks.size() > 20);

		Set<Integer> expectedTasks = new HashSet<Integer>();
		for (int x = 0; x < 3; ++x) {
			Task task = tasks.get(x);

			com.tasktop.c2c.server.tasks.domain.Task stask = taskService.retrieveTask(task.getId());
			stask.getCustomFields().put(descriptor.getName(), "two");
			stask = taskService.updateTask(stask);
			assertEquals("two", stask.getCustomFields().get(descriptor.getName()));

			expectedTasks.add(stask.getId());
		}

		Criteria criteria = new ColumnCriteria(descriptor.getName(), Operator.EQUALS, "two");
		QuerySpec querySpec = new QuerySpec(new Region(0, tasks.size() * 2), new SortInfo(descriptor.getName(),
				Order.ASCENDING));
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasksWithCriteria(criteria,
				querySpec);

		assertEquals(expectedTasks.size(), result.getTotalResultSize().intValue());
		assertEquals(expectedTasks.size(), result.getResultPage().size());

		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			assertTrue(expectedTasks.contains(task.getId()));
		}

		criteria = new ColumnCriteria(descriptor.getName(), Operator.NOT_EQUALS, "two");
		querySpec = new QuerySpec(new Region(0, tasks.size() * 2), new SortInfo(descriptor.getName(), Order.DESCENDING));

		result = taskService.findTasksWithCriteria(criteria, querySpec);

		assertEquals(tasks.size() - expectedTasks.size(), result.getTotalResultSize().intValue());

		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			assertFalse(expectedTasks.contains(task.getId()));
		}
	}

	@Test
	public void testFindTasksWithCriteria_Keywords() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		setupTestData();

		// sanity
		assertTrue(tasks.size() > 20);

		Keyword keyword1 = new Keyword();
		keyword1.setName("keywordX");
		Keyword keyword2 = new Keyword();
		keyword2.setName("keywordY");

		List<Keyword> keywords = new ArrayList<Keyword>();
		keywords.add(keyword1);
		keywords.add(keyword2);

		Set<Integer> expectedTasks = new HashSet<Integer>();
		for (int x = 0; x < 3; ++x) {
			Task task = tasks.get(x);

			com.tasktop.c2c.server.tasks.domain.Task serviceTask = taskService.retrieveTask(task.getId());
			serviceTask.setKeywords(keywords);
			serviceTask = taskService.updateTask(serviceTask);
			assertEquals(2, serviceTask.getKeywords().size());
			assertTrue(keyword1.getName().equals(serviceTask.getKeywords().get(0).getName())
					|| keyword1.getName().equals(serviceTask.getKeywords().get(1).getName()));

			expectedTasks.add(serviceTask.getId());
		}

		Criteria criteria = new ColumnCriteria(TaskFieldConstants.KEYWORDS_FIELD, Operator.EQUALS, keyword1.getName());
		QuerySpec querySpec = new QuerySpec(new Region(0, tasks.size() * 2), null);
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasksWithCriteria(criteria,
				querySpec);

		assertEquals(expectedTasks.size(), result.getTotalResultSize().intValue());
		assertEquals(expectedTasks.size(), result.getResultPage().size());

		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			assertTrue(expectedTasks.contains(task.getId()));
		}
	}

	@Test
	public void testFindTasksWithCriteria_CreationDateRange() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		setupTestData();

		// sanity
		assertTrue(tasks.size() > 20);

		Date createdStart = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 48);
		Date createdEnd = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);

		int numExpectedTasks = 10;
		Set<Integer> expectedTasks = new HashSet<Integer>();
		for (Task task : tasks) {
			if (expectedTasks.size() >= numExpectedTasks) {
				task.setCreationTs(new Date(createdEnd.getTime() + 4000));
				break;
			}
			task.setCreationTs(new Date(createdStart.getTime() + 1000 + expectedTasks.size() * 1000));
			expectedTasks.add(task.getId());
		}

		Criteria criteria = new NaryCriteria(Operator.AND, new ColumnCriteria(TaskFieldConstants.CREATION_TIME_FIELD,
				Operator.GREATER_THAN, createdStart), new ColumnCriteria(TaskFieldConstants.CREATION_TIME_FIELD,
				Operator.LESS_THAN, createdEnd));
		QuerySpec querySpec = new QuerySpec(new Region(0, tasks.size() * 2), null);
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasksWithCriteria(criteria,
				querySpec);

		assertEquals(expectedTasks.size(), result.getTotalResultSize().intValue());
		assertEquals(expectedTasks.size(), result.getResultPage().size());

		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			assertTrue(expectedTasks.contains(task.getId()));
		}

		// do the inverse
		criteria = new NaryCriteria(Operator.OR, new ColumnCriteria(TaskFieldConstants.CREATION_TIME_FIELD,
				Operator.LESS_THAN, createdStart), new ColumnCriteria(TaskFieldConstants.CREATION_TIME_FIELD,
				Operator.GREATER_THAN, createdEnd));
		querySpec = new QuerySpec(new Region(0, tasks.size() * 2), null);
		result = taskService.findTasksWithCriteria(criteria, querySpec);

		assertEquals(tasks.size() - expectedTasks.size(), result.getTotalResultSize().intValue());
		assertEquals(tasks.size() - expectedTasks.size(), result.getResultPage().size());

		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			assertFalse(expectedTasks.contains(task.getId()));
		}

		// And one that should not match any
		criteria = new NaryCriteria(Operator.AND, new ColumnCriteria(TaskFieldConstants.CREATION_TIME_FIELD,
				Operator.LESS_THAN, createdStart), new ColumnCriteria(TaskFieldConstants.CREATION_TIME_FIELD,
				Operator.GREATER_THAN, createdEnd));
		querySpec = new QuerySpec(new Region(0, tasks.size() * 2), null);
		result = taskService.findTasksWithCriteria(criteria, querySpec);

		assertEquals(0, result.getTotalResultSize().intValue());
		assertEquals(0, result.getResultPage().size());

	}

	@Test
	public void testFindTasksWithCriteria_UpdatedDateRange() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		setupTestData();

		// sanity
		assertTrue(tasks.size() > 20);

		Date updatedStart = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 48);
		Date updatedEnd = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);

		int numExpectedTasks = 10;
		Set<Integer> expectedTasks = new HashSet<Integer>();
		for (Task task : tasks) {
			if (expectedTasks.size() >= numExpectedTasks) {
				task.setDeltaTs(new Date(updatedEnd.getTime() + 4000));
				break;
			}
			task.setDeltaTs(new Date(updatedStart.getTime() + 1000 + expectedTasks.size() * 1000));
			expectedTasks.add(task.getId());
		}

		Criteria criteria = new NaryCriteria(Operator.AND, new ColumnCriteria(TaskFieldConstants.LAST_UPDATE_FIELD,
				Operator.GREATER_THAN, updatedStart), new ColumnCriteria(TaskFieldConstants.LAST_UPDATE_FIELD,
				Operator.LESS_THAN, updatedEnd));
		QuerySpec querySpec = new QuerySpec(new Region(0, tasks.size() * 2), null);
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasksWithCriteria(criteria,
				querySpec);

		assertEquals(expectedTasks.size(), result.getTotalResultSize().intValue());
		assertEquals(expectedTasks.size(), result.getResultPage().size());

		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			assertTrue(expectedTasks.contains(task.getId()));
		}

		// do the inverse
		criteria = new NaryCriteria(Operator.OR, new ColumnCriteria(TaskFieldConstants.LAST_UPDATE_FIELD,
				Operator.LESS_THAN, updatedStart), new ColumnCriteria(TaskFieldConstants.LAST_UPDATE_FIELD,
				Operator.GREATER_THAN, updatedEnd));
		querySpec = new QuerySpec(new Region(0, tasks.size() * 2), null);
		result = taskService.findTasksWithCriteria(criteria, querySpec);

		assertEquals(tasks.size() - expectedTasks.size(), result.getTotalResultSize().intValue());
		assertEquals(tasks.size() - expectedTasks.size(), result.getResultPage().size());

		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			assertFalse(expectedTasks.contains(task.getId()));
		}

		// And one that should not match any
		criteria = new NaryCriteria(Operator.AND, new ColumnCriteria(TaskFieldConstants.LAST_UPDATE_FIELD,
				Operator.LESS_THAN, updatedStart), new ColumnCriteria(TaskFieldConstants.LAST_UPDATE_FIELD,
				Operator.GREATER_THAN, updatedEnd));
		querySpec = new QuerySpec(new Region(0, tasks.size() * 2), null);
		result = taskService.findTasksWithCriteria(criteria, querySpec);

		assertEquals(0, result.getTotalResultSize().intValue());
		assertEquals(0, result.getResultPage().size());
	}

	@Test
	public void findAllTasksForProductName() throws Exception {
		setupTestData();

		// Get the generated name of our product
		String productName = tasks.get(0).getProduct().getName();

		Criteria criteria = new ColumnCriteria(TaskFieldConstants.PRODUCT_NAME_FIELD, Operator.EQUALS, productName);
		QuerySpec querySpec = new QuerySpec(new Region(0, 10000), new SortInfo(TaskFieldConstants.TASK_ID_FIELD,
				Order.ASCENDING));
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasksWithCriteria(criteria,
				querySpec);

		Set<Integer> resultIds = new HashSet<Integer>();
		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			resultIds.add(task.getId());
		}

		// Make sure we got back all of our items, and that the query was accurate.
		for (Task task : tasks) {
			// We want to confirm that, if the name was present, then it was in the returned set - as a result,
			// we don't use the short-circuit &&, but rather the always-evaluated &
			assertEquals(task.getProduct().getName().equals(productName), resultIds.contains(task.getId()));
		}
	}

	@Test
	public void findTasksForReporter_bug1649() throws Exception {
		// see related bug 1649
		setupTestData(1);

		// Get the first task
		Task firstTask = tasks.get(0);
		Profile reporter = firstTask.getReporter();

		final int reporterBugs = ((Long) entityManager
				.createQuery("select count(t) from " + Task.class.getSimpleName() + " t where t.reporter = :reporter")
				.setParameter("reporter", reporter).getSingleResult()).intValue();

		assertTrue(reporterBugs > 0);

		Criteria criteria = new ColumnCriteria(TaskFieldConstants.REPORTER_FIELD, Operator.EQUALS,
				reporter.getLoginName());
		QuerySpec querySpec = new QuerySpec(new Region(0, 10000), new SortInfo(TaskFieldConstants.TASK_ID_FIELD,
				Order.ASCENDING));
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasksWithCriteria(criteria,
				querySpec);

		assertEquals(reporterBugs, result.getResultPage().size());
		assertEquals(firstTask.getId(), result.getResultPage().get(0).getId());

		setupTestData(2);

		criteria = new ColumnCriteria(TaskFieldConstants.REPORTER_FIELD, Operator.NOT_EQUALS, reporter.getLoginName());
		result = taskService.findTasksWithCriteria(criteria, querySpec);

		int expectedSize = taskService.findTasksWithQuery(PredefinedTaskQuery.ALL, null).getTotalResultSize()
				- reporterBugs;

		assertTrue(expectedSize > 0);
		assertEquals(expectedSize, result.getResultPage().size());
		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			assertFalse(firstTask.getId().equals(task.getId()));
		}
	}

	@Test
	public void findAllTasksForProductNameAndComponentName() throws Exception {
		setupTestData();

		// Get the generated name of our product
		String productName = tasks.get(0).getProduct().getName();
		Criteria productCriteria = new ColumnCriteria(TaskFieldConstants.PRODUCT_NAME_FIELD, Operator.EQUALS,
				productName);

		// Get the generated name of our component
		String componentName = tasks.get(0).getComponent().getName();
		Criteria componentCriteria = new ColumnCriteria(TaskFieldConstants.COMPONENT_NAME_FIELD, Operator.EQUALS,
				componentName);

		QuerySpec querySpec = new QuerySpec(new Region(0, 10000), new SortInfo(TaskFieldConstants.TASK_ID_FIELD,
				Order.ASCENDING));
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasksWithCriteria(
				new NaryCriteria(Operator.AND, productCriteria, componentCriteria), querySpec);

		Set<Integer> resultIds = new HashSet<Integer>();
		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			resultIds.add(task.getId());
		}

		// Make sure we got back all of our items, and that the query was accurate.
		for (Task task : tasks) {
			// We want to confirm that, if the name was present, then it was in the returned set.
			assertEquals(
					task.getProduct().getName().equals(productName)
							&& task.getComponent().getName().equals(componentName), resultIds.contains(task.getId()));
		}
	}

	@Test
	public void findAllTasksForProductIdComponentIdAndMilestone() throws Exception {
		setupTestData();

		// Get the generated name of our product
		String productName = tasks.get(0).getProduct().getName();
		Criteria productCriteria = new ColumnCriteria(TaskFieldConstants.PRODUCT_NAME_FIELD, Operator.EQUALS,
				productName);

		// Get the generated name of our component
		Short componentId = tasks.get(0).getComponent().getId();
		Criteria componentCriteria = new ColumnCriteria(TaskFieldConstants.COMPONENT_FIELD, Operator.EQUALS,
				componentId);

		// Get the generated milestone
		String milestone = tasks.get(0).getTargetMilestone();
		Criteria milestoneCriteria = new ColumnCriteria(TaskFieldConstants.MILESTONE_FIELD, Operator.EQUALS, milestone);

		QuerySpec querySpec = new QuerySpec(new Region(0, 10000), new SortInfo(TaskFieldConstants.TASK_ID_FIELD,
				Order.ASCENDING));
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasksWithCriteria(
				new NaryCriteria(Operator.AND, productCriteria, componentCriteria, milestoneCriteria), querySpec);

		Set<Integer> resultIds = new HashSet<Integer>();
		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			resultIds.add(task.getId());
		}

		// Make sure we got back all of our items, and that the query was accurate.
		for (Task task : tasks) {
			// We want to confirm that, if the name was present, then it was in the returned set.
			assertEquals(
					task.getProduct().getName().equals(productName) && task.getComponent().getId().equals(componentId)
							&& task.getTargetMilestone().equals(milestone), resultIds.contains(task.getId()));
		}
	}

	@Test
	public void findAllTasksForProductIdAndMilestone() throws Exception {
		setupTestData();

		// Get the generated name of our product
		Short productId = tasks.get(0).getProduct().getId();
		Criteria productCriteria = new ColumnCriteria(TaskFieldConstants.PRODUCT_FIELD, Operator.EQUALS, productId);

		// Get the generated milestone
		String milestone = tasks.get(0).getTargetMilestone();
		Criteria milestoneCriteria = new ColumnCriteria(TaskFieldConstants.MILESTONE_FIELD, Operator.EQUALS, milestone);

		QuerySpec querySpec = new QuerySpec(new Region(0, 10000), new SortInfo(TaskFieldConstants.TASK_ID_FIELD,
				Order.ASCENDING));
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasksWithCriteria(
				new NaryCriteria(Operator.AND, productCriteria, milestoneCriteria), querySpec);

		Set<Integer> resultIds = new HashSet<Integer>();
		for (com.tasktop.c2c.server.tasks.domain.Task task : result.getResultPage()) {
			resultIds.add(task.getId());
		}

		// Make sure we got back all of our items, and that the query was accurate.
		for (Task task : tasks) {
			// We want to confirm that, if the name was present, then it was in the returned set.
			assertEquals(task.getProduct().getId().equals(productId) && task.getTargetMilestone().equals(milestone),
					resultIds.contains(task.getId()));
		}
	}

	@Test
	public void testTaskSummary() {
		setupTestData(1000);

		TaskSummary taskSummary = taskService.getTaskSummary();
		assertNotNull(taskSummary);

		assertEquals(severities.size(), taskSummary.getItems().size());

		// it all has to add up.
		long sum = 0;
		short previousSortKey = Short.MIN_VALUE;
		for (TaskSummaryItem item : taskSummary.getItems()) {
			System.out.println(String.format("%s open=%s closed=%s", item.getSeverity().getValue(),
					item.getOpenCount(), item.getClosedCount()));
			assertNotNull(item.getSeverity());
			assertNotNull(item.getSeverity().getValue());
			assertTrue(item.getSeverity().getSortkey() > previousSortKey);
			previousSortKey = item.getSeverity().getSortkey();
			assertTrue(item.getOpenCount() > 40);
			assertTrue(item.getClosedCount() > 40);

			sum += item.getOpenCount();
			sum += item.getClosedCount();
		}
		assertEquals(sum, tasks.size());
	}

	@Test
	public void testTaskSummaryHasAllSeverities() {
		computeSeverities();
		assertTrue(severities.size() > 3);

		TaskSummary taskSummary = taskService.getTaskSummary();
		assertNotNull(taskSummary);

		assertEquals(severities.size(), taskSummary.getItems().size());
	}

	@Test
	public void testFindAllTasks() {
		setupTestData();

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.ALL, null);
		assertNotNull(queryResult);
		assertEquals(tasks.size(), (int) queryResult.getTotalResultSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getOffset(), queryResult.getOffset());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), queryResult.getPageSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), (Integer) queryResult.getResultPage().size());
	}

	@Test
	public void testFindAllTasksSortedByTaskIdPagingThroughResults() {
		setupTestData(50);
		int pageSize = TaskService.DEFAULT_PAGE_INFO.getSize() + 3; // intentionally
																	// not the
																	// default
		int numPages = (int) Math.ceil((double) tasks.size() / (double) pageSize);
		Region pageInfo = new Region();
		pageInfo.setSize(pageSize);
		assertTrue(numPages > 0);
		SortInfo sortInfo = new SortInfo(TaskFieldConstants.TASK_ID_FIELD, Order.ASCENDING);

		com.tasktop.c2c.server.tasks.domain.Task lastResult = null;

		for (int pageNum = 0; pageNum < numPages; pageNum++) {
			pageInfo.setOffset(pageNum * pageSize);

			QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
					PredefinedTaskQuery.ALL, new QuerySpec(pageInfo, sortInfo));
			assertNotNull(queryResult);
			assertEquals(tasks.size(), (int) queryResult.getTotalResultSize());
			assertEquals(pageInfo.getOffset(), queryResult.getOffset());
			assertEquals(pageInfo.getSize(), queryResult.getPageSize());

			if (pageNum != numPages - 1) {
				assertEquals(Integer.valueOf(pageSize), (Integer) queryResult.getResultPage().size());
			}

			for (com.tasktop.c2c.server.tasks.domain.Task result : queryResult.getResultPage()) {
				if (lastResult != null) {
					assertTrue("bad sort", lastResult.getId().compareTo(result.getId()) <= 0);
				}
				lastResult = result;
			}
		}
	}

	@Test
	public void testFindAllTasksSortedByCreation() {
		setupTestData();

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.ALL, new QuerySpec(null, new SortInfo(TaskFieldConstants.CREATION_TIME_FIELD,
						Order.ASCENDING)));
		assertNotNull(queryResult);
		assertEquals(tasks.size(), (int) queryResult.getTotalResultSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getOffset(), queryResult.getOffset());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), queryResult.getPageSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), (Integer) queryResult.getResultPage().size());

		com.tasktop.c2c.server.tasks.domain.Task lastResult = null;
		for (com.tasktop.c2c.server.tasks.domain.Task result : queryResult.getResultPage()) {
			if (lastResult != null) {
				assertTrue("bad sort: " + lastResult.getCreationDate().getTime() + " "
						+ result.getCreationDate().getTime(),
						lastResult.getCreationDate().compareTo(result.getCreationDate()) <= 0);
			}
			lastResult = result;
		}
	}

	@Test
	public void testFindAllTasksSortedBySeverity() {
		setupTestData();

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.ALL, new QuerySpec(null, new SortInfo(TaskFieldConstants.SEVERITY_FIELD,
						Order.ASCENDING)));
		assertNotNull(queryResult);
		assertEquals(tasks.size(), (int) queryResult.getTotalResultSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getOffset(), queryResult.getOffset());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), queryResult.getPageSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), (Integer) queryResult.getResultPage().size());

		com.tasktop.c2c.server.tasks.domain.Task lastResult = null;
		for (com.tasktop.c2c.server.tasks.domain.Task result : queryResult.getResultPage()) {
			if (lastResult != null) {
				assertTrue("bad sort",
						lastResult.getSeverity().getSortkey().compareTo(result.getSeverity().getSortkey()) <= 0);
			}
			lastResult = result;
		}
	}

	@Test
	public void testFindAllTasksSortedByStatus() {
		setupTestData();

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.ALL, new QuerySpec(null, new SortInfo(TaskFieldConstants.STATUS_FIELD,
						Order.ASCENDING)));
		assertNotNull(queryResult);
		assertEquals(tasks.size(), (int) queryResult.getTotalResultSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getOffset(), queryResult.getOffset());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), queryResult.getPageSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), (Integer) queryResult.getResultPage().size());

		com.tasktop.c2c.server.tasks.domain.Task lastResult = null;
		for (com.tasktop.c2c.server.tasks.domain.Task result : queryResult.getResultPage()) {
			if (lastResult != null) {
				assertTrue("bad sort",
						((Short) lastResult.getStatus().getSortkey()).compareTo(result.getStatus().getSortkey()) <= 0);
			}
			lastResult = result;
		}
	}

	@Test
	public void testFindAllTasksSortedByPriority() {
		setupTestData();

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.ALL, new QuerySpec(null, new SortInfo(TaskFieldConstants.PRIORITY_FIELD,
						Order.ASCENDING)));
		assertNotNull(queryResult);
		assertEquals(tasks.size(), (int) queryResult.getTotalResultSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getOffset(), queryResult.getOffset());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), queryResult.getPageSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), (Integer) queryResult.getResultPage().size());

		com.tasktop.c2c.server.tasks.domain.Task lastResult = null;
		for (com.tasktop.c2c.server.tasks.domain.Task result : queryResult.getResultPage()) {
			if (lastResult != null) {
				assertTrue(
						"bad sort",
						((Short) lastResult.getPriority().getSortkey()).compareTo(result.getPriority().getSortkey()) <= 0);
			}
			lastResult = result;
		}
	}

	@Test
	public void testFindResultsAreThin() {
		setupTestData();

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.ALL, new QuerySpec(null, new SortInfo(TaskFieldConstants.CREATION_TIME_FIELD,
						Order.DESCENDING)));
		assertFalse(queryResult.getResultPage().isEmpty());
		for (com.tasktop.c2c.server.tasks.domain.Task result : queryResult.getResultPage()) {
			assertNotNull(result.getShortDescription());
			assertNotNull(result.getDescription());
			assertNotNull(result.getComments());
			assertNull(result.getBlocksTasks());
			assertNull(result.getAttachments());
			assertNull(result.getSubTasks());
		}
	}

	@Test
	public void testFindResultsExposesParent() {
		setupTestData(2);

		Task task1 = tasks.get(0);
		Task task2 = tasks.get(1);

		entityManager.persist(task1.addBlocked(task2));

		entityManager.flush();
		entityManager.clear();

		Criteria criteria = new ColumnCriteria(TaskFieldConstants.TASK_ID_FIELD, task1.getId());
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService
				.findTasksWithCriteria(criteria, null);
		assertEquals(Integer.valueOf(1), result.getTotalResultSize());
		com.tasktop.c2c.server.tasks.domain.Task task = result.getResultPage().get(0);

		assertEquals(task1.getId(), task.getId());
		assertNotNull(task.getParentTask());
		assertEquals(task2.getId(), task.getParentTask().getId());
	}

	@Test
	public void testFindAllTasksSortedByUpdate() {
		setupTestData();

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.RECENT, null);
		assertNotNull(queryResult);
		assertEquals(tasks.size(), (int) queryResult.getTotalResultSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getOffset(), queryResult.getOffset());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), queryResult.getPageSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), (Integer) queryResult.getResultPage().size());

		com.tasktop.c2c.server.tasks.domain.Task lastResult = null;
		for (com.tasktop.c2c.server.tasks.domain.Task result : queryResult.getResultPage()) {
			if (lastResult != null) {
				assertTrue("bad sort: " + lastResult.getModificationDate().getTime() + " "
						+ result.getModificationDate().getTime(),
						lastResult.getModificationDate().compareTo(result.getModificationDate()) >= 0);
			}
			lastResult = result;
		}
	}

	@Test
	public void testFindAllTasksSortedByTaskId() {
		setupTestData();

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.ALL, new QuerySpec(null, new SortInfo(TaskFieldConstants.TASK_ID_FIELD,
						Order.ASCENDING)));
		assertNotNull(queryResult);
		assertEquals(tasks.size(), (int) queryResult.getTotalResultSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getOffset(), queryResult.getOffset());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), queryResult.getPageSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), (Integer) queryResult.getResultPage().size());

		com.tasktop.c2c.server.tasks.domain.Task lastResult = null;
		for (com.tasktop.c2c.server.tasks.domain.Task result : queryResult.getResultPage()) {
			if (lastResult != null) {
				assertTrue("bad sort", lastResult.getId().compareTo(result.getId()) <= 0);
			}
			lastResult = result;
		}
	}

	@Test
	public void testFindAllOpenTasks() {
		setupTestData();

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.OPEN, new QuerySpec(null, new SortInfo(TaskFieldConstants.STATUS_FIELD,
						SortInfo.Order.ASCENDING)));
		assertNotNull(queryResult);

		for (com.tasktop.c2c.server.tasks.domain.Task t : queryResult.getResultPage()) {
			Assert.assertTrue(t.getStatus().isOpen());
		}
		int numOpenTasks = sumOpenTasks();
		assertEquals(numOpenTasks, (int) queryResult.getTotalResultSize());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getOffset(), queryResult.getOffset());
		assertEquals(TaskService.DEFAULT_PAGE_INFO.getSize(), queryResult.getPageSize());
		assertEquals(Math.min(numOpenTasks, TaskService.DEFAULT_PAGE_INFO.getSize()), queryResult.getResultPage()
				.size());
	}

	private int sumOpenTasks() {
		Number openTasks = (Number) entityManager.createQuery(
				"select count(task) from " + com.tasktop.c2c.server.internal.tasks.domain.Task.class.getSimpleName()
						+ " task, " + com.tasktop.c2c.server.internal.tasks.domain.TaskStatus.class.getSimpleName()
						+ " stat " + " where stat.value = task.status and stat.isOpen = true").getSingleResult();
		return openTasks.intValue();
	}

	@Test
	public void testFindMyTasks() throws ValidationException {
		int numNewTasks = 5;

		Product product = MockProductFactory.create(entityManager);
		ProfileConverter.copy(currentUser);
		entityManager.flush();

		for (int i = 0; i < numNewTasks; i++) {
			Task t = MockTaskFactory.create(entityManager, product, currentUser);
			t.setAssignee(currentUser);
		}
		entityManager.flush();

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.MINE, null);
		assertNotNull(queryResult);
		assertEquals((Integer) numNewTasks, queryResult.getTotalResultSize());
		assertEquals(numNewTasks, queryResult.getResultPage().size());

		for (com.tasktop.c2c.server.tasks.domain.Task task : queryResult.getResultPage()) {
			assertEquals(currentUser.getLoginName(), task.getReporter().getLoginName());
		}

	}

	@Test
	public void testFindMyRelatedTasks() throws ValidationException, EntityNotFoundException, ConcurrentUpdateException {

		Product product = MockProductFactory.create(entityManager);
		Profile otherCreatorUser = getCreatedMockProfile();
		Profile assigneeUser = getCreatedMockProfile();
		entityManager.flush();

		// 3 cases - assignee, commentor, cc
		int numNewTasks = 3;

		// create task with creator of currentUser
		Task task1 = MockTaskFactory.create(entityManager, product, currentUser);
		task1.setAssignee(assigneeUser);
		entityManager.flush();

		// create task with cc of currentUser
		Task task2 = MockTaskFactory.create(entityManager, product, otherCreatorUser);
		task2.setAssignee(assigneeUser);
		entityManager.flush();

		com.tasktop.c2c.server.tasks.domain.Task serviceTask2 = taskService.retrieveTask(task2.getId());
		serviceTask2.getWatchers().add(ProfileConverter.copy(currentUser));
		taskService.updateTask(serviceTask2);

		// create task with comment authored by currentUser
		Task task3 = MockTaskFactory.create(entityManager, product, otherCreatorUser);
		task3.setAssignee(otherCreatorUser);
		entityManager.flush();

		com.tasktop.c2c.server.tasks.domain.Task serviceTask3 = taskService.retrieveTask(task3.getId());
		serviceTask3.addComment("new comment");
		taskService.updateTask(serviceTask3);

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.RELATED, null);
		assertNotNull(queryResult);
		assertEquals((Integer) numNewTasks, queryResult.getTotalResultSize());
		assertEquals(numNewTasks, queryResult.getResultPage().size());
	}

	@Test
	public void testInvalidCreates() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setShortDescription(null); // Required

		try {
			taskService.createTask(toCreate);
			Assert.fail("Expect validation exception");
		} catch (ValidationException ex) {
			// Expected
			assertEquals("A summary is required.", ex.getMessage());
		}

		toCreate = getMockTask();
		toCreate.setComponent(null); // Required

		try {
			taskService.createTask(toCreate);
			Assert.fail("Expect validation exception");
		} catch (ValidationException ex) {
			// Expected
			assertEquals("A component is required.", ex.getMessage());
		}

		toCreate = getMockTask();
		toCreate.setProduct(null); // Required

		try {
			taskService.createTask(toCreate);
			Assert.fail("Expect validation exception");
		} catch (ValidationException ex) {
			// Expected
			assertEquals("A product is required.", ex.getMessage());
		}

		toCreate = getMockTask();
		toCreate.setAssignee(new TaskUserProfile());
		toCreate.getAssignee().setLoginName("foo");

		try {
			taskService.createTask(toCreate);
			Assert.fail("Expect entity not found exception");
		} catch (EntityNotFoundException ex) {
			// Expected
		}

		toCreate = getMockTask();
		toCreate.setAssignee(new TaskUserProfile());

		try {
			taskService.createTask(toCreate);
			Assert.fail("Expect validation exception");
		} catch (ValidationException ex) {
			assertEquals("An owner is required.", ex.getMessage());
			// Expected
		}

		toCreate = getMockTask();
		toCreate.setWatchers(Arrays.asList(new TaskUserProfile()));

		try {
			taskService.createTask(toCreate);
			Assert.fail("Expect validation exception");
		} catch (ValidationException ex) {
			assertEquals("The watcher's login name or id should be set.", ex.getMessage());
			// Expected
		}

		toCreate = getMockTask();
		toCreate.setProduct(new com.tasktop.c2c.server.tasks.domain.Product());
		toCreate.getProduct().setId(999999999);

		try {
			taskService.createTask(toCreate);
			Assert.fail("Expect entity not found exception");
		} catch (EntityNotFoundException ex) {
			// Expected
		}
	}

	@Test
	public void testLineDelimitersConsistent() throws Exception {
		final String textWithVaryingLineDelimiters = "line1\r\nline2\rline3\nline4";
		final String expectedTextConversion = "line1\nline2\nline3\nline4";

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setDescription(textWithVaryingLineDelimiters);
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		Assert.assertEquals(expectedTextConversion, created.getDescription());

		Comment comment = new Comment();
		comment.setAuthor(created.getReporter());
		comment.setCommentText(textWithVaryingLineDelimiters);
		created.getComments().add(comment);

		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);

		Assert.assertEquals(expectedTextConversion, updated.getComments().get(0).getCommentText());
	}

	@Test
	public void testTrailingWhitespaceNotLost() throws Exception {
		final String textWithVaryingLineDelimiters = "line1  \r\nline2";
		final String expectedTextConversion = "line1  \nline2";

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setDescription(textWithVaryingLineDelimiters);
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		Assert.assertEquals(expectedTextConversion, created.getDescription());

		Comment comment = new Comment();
		comment.setAuthor(created.getReporter());
		comment.setCommentText(textWithVaryingLineDelimiters);
		created.getComments().add(comment);

		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);

		Assert.assertEquals(expectedTextConversion, updated.getComments().get(0).getCommentText());
	}

	@Test
	public void testUpdateExceptions() throws Exception {

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		created.setShortDescription(null); // required

		try {
			taskService.updateTask(created);
			Assert.fail("Expect validation exception");
		} catch (ValidationException ex) {
			// Expected
			assertEquals("A summary is required.", ex.getMessage());
		}

		created.setShortDescription("sd");
		created.setId(100);

		try {
			taskService.updateTask(created);
			Assert.fail("Expect ENF exception");
		} catch (EntityNotFoundException ex) {
			// Expected
			// TODO, verify message
		}

	}

	@Test
	public void testUpdateSendsCorrectActivityEvent() throws Exception {

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		// So that we do not merge with the create activity
		Thread.sleep(1000L);

		String newDesc = "My new desc";
		created.setDescription(newDesc);

		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);
		Assert.assertEquals(newDesc, updated.getDescription());
		Assert.assertNotNull(lastEvent);
		TaskActivityEvent activityEvent = (TaskActivityEvent) lastEvent;
		Assert.assertEquals(1, activityEvent.getTaskActivities().size());
		TaskActivity activity = activityEvent.getTaskActivities().get(0);
		Assert.assertEquals(TaskActivity.Type.UPDATED, activity.getActivityType());
		Assert.assertEquals(1, activity.getFieldUpdates().size());
		Assert.assertEquals(newDesc, activity.getFieldUpdates().get(0).getNewValue());
		Assert.assertEquals(newDesc, activity.getTask().getDescription());
	}

	@Test
	public void testUpdateTaskModifiesTaskVersion() throws Exception {

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		Assert.assertNotNull(created.getVersion());

		created.setShortDescription(created.getShortDescription() + " changed"); // required

		// resolution of version is timestamp-based... so we have to wait here.
		Thread.sleep(1000L);

		com.tasktop.c2c.server.tasks.domain.Task updatedTask = taskService.updateTask(created);

		Assert.assertNotNull(updatedTask.getVersion());
		Assert.assertFalse(updatedTask.getVersion() + " = " + created.getVersion(),
				updatedTask.getVersion().equals(created.getVersion()));
	}

	@Test
	public void testUpdateAssociations() throws Exception {
		// bug 810: submitting a task can overwrite the parent association
		com.tasktop.c2c.server.tasks.domain.Task parent = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task child1 = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task child2 = getMockTask();
		parent = taskService.createTask(parent);
		child1 = taskService.createTask(child1);
		child2 = taskService.createTask(child2);

		// adding child to subtasks causes parent/child
		parent.getSubTasks().add(child1);
		parent = taskService.updateTask(parent);
		child1 = taskService.retrieveTask(child1.getId());
		assertParentChild(parent, child1);

		parent = taskService.retrieveTask(parent.getId());

		assertParentChild(parent, child1);

		// setting parent on child causes parent/child
		child2.setParentTask(parent);
		child2 = taskService.updateTask(child2);
		parent = taskService.retrieveTask(parent.getId());

		assertParentChild(parent, child2);
		// verify adding a child to parent doesn't remove existing children
		assertParentChild(parent, child1);

		// removing a child from a parent disassociates child
		parent.getSubTasks().remove(child1);
		parent = taskService.updateTask(parent);
		child1 = taskService.retrieveTask(child1.getId());

		assertParentChild(parent, child2);
		assertNotParentChild(parent, child1);

		// setting parent to null disassociates child
		child2.setParentTask(null);
		child2 = taskService.updateTask(child2);
		parent = taskService.retrieveTask(parent.getId());

		assertNotParentChild(parent, child1);
		assertNotParentChild(parent, child2);

		Thread.sleep(500L);

	}

	@Test
	public void testUpdateAssociationsDeltaTimestamp() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task parent = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task child1 = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task child2 = getMockTask();

		parent = taskService.createTask(parent);
		child1 = taskService.createTask(child1);
		child2 = taskService.createTask(child2);

		entityManager.flush();

		Date parentDeltaTimestamp = parent.getModificationDate();
		Date child1DeltaTimestamp = child1.getModificationDate();
		Date child2DeltaTimestamp = child2.getModificationDate();

		Thread.sleep(1000L);

		// adding child to subtasks causes parent/child
		parent.getSubTasks().add(child1);
		parent = taskService.updateTask(parent);
		child1 = taskService.retrieveTask(child1.getId());
		assertParentChild(parent, child1);

		assertAfter(child1DeltaTimestamp, child1.getModificationDate());
		assertAfter(parentDeltaTimestamp, parent.getModificationDate());

		Thread.sleep(1000L);

		parentDeltaTimestamp = parent.getModificationDate();
		child1DeltaTimestamp = child1.getModificationDate();
		child2DeltaTimestamp = child2.getModificationDate();

		// setting parent on child causes parent/child
		child2.setParentTask(parent);
		child2 = taskService.updateTask(child2);
		child1 = taskService.retrieveTask(child1.getId());
		parent = taskService.retrieveTask(parent.getId());

		assertParentChild(parent, child1);
		assertParentChild(parent, child2);

		assertAfter(child2DeltaTimestamp, child2.getModificationDate());
		assertAfter(parentDeltaTimestamp, parent.getModificationDate());
		assertEquals(child1DeltaTimestamp, child1.getModificationDate());

		Thread.sleep(1000L);

		parentDeltaTimestamp = parent.getModificationDate();
		child1DeltaTimestamp = child1.getModificationDate();
		child2DeltaTimestamp = child2.getModificationDate();

		// removing a child by setting parent task to null
		child2.setParentTask(null);
		child2 = taskService.updateTask(child2);
		child1 = taskService.retrieveTask(child1.getId());
		parent = taskService.retrieveTask(parent.getId());

		assertNotParentChild(parent, child2);
		assertParentChild(parent, child1);

		assertAfter(child2DeltaTimestamp, child2.getModificationDate());
		assertAfter(parentDeltaTimestamp, parent.getModificationDate());
		assertEquals(child1DeltaTimestamp, child1.getModificationDate());

		Thread.sleep(1000L);

		parentDeltaTimestamp = parent.getModificationDate();
		child1DeltaTimestamp = child1.getModificationDate();
		child2DeltaTimestamp = child2.getModificationDate();

		// removing a child by removing from parent collection
		parent.getSubTasks().clear();
		parent = taskService.updateTask(parent);
		child1 = taskService.retrieveTask(child1.getId());
		child2 = taskService.retrieveTask(child2.getId());

		assertNotParentChild(parent, child2);
		assertNotParentChild(parent, child1);

		assertAfter(child1DeltaTimestamp, child1.getModificationDate());
		assertAfter(parentDeltaTimestamp, parent.getModificationDate());
		assertEquals(child2DeltaTimestamp, child2.getModificationDate());
	}

	private void assertAfter(Date referenceTimestamp, Date timestamp) {
		DateFormat format = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
		assertTrue(
				String.format("Expected timestamp to be after %s but was %s", format.format(referenceTimestamp),
						format.format(timestamp)), timestamp.after(referenceTimestamp));
	}

	private void assertNotParentChild(com.tasktop.c2c.server.tasks.domain.Task parent,
			com.tasktop.c2c.server.tasks.domain.Task child) {
		assertFalse(parent.getSubTasks().contains(child));
		assertNull(child.getParentTask());
	}

	private void assertParentChild(com.tasktop.c2c.server.tasks.domain.Task parent,
			com.tasktop.c2c.server.tasks.domain.Task child) {
		assertTrue(parent.getSubTasks().contains(child));
		assertEquals(child.getParentTask(), parent);
	}

	@Test
	public void testUpdateWithNoOwner() throws Exception {
		// bug 276: saving a new task with no owner causes a validation error
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		created.setAssignee(null);
		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);
		assertNotNull(updated.getAssignee());
	}

	@Test
	public void testUpdateWithCustomFields() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		FieldDescriptor descriptor = new FieldDescriptor();
		descriptor.setAvailableForNewTasks(true);
		descriptor.setDescription("TestA");
		descriptor.setFieldType(FieldType.SINGLE_SELECT);
		descriptor.setName("testA" + System.currentTimeMillis());
		descriptor.setValueStrings(Arrays.asList("one", "two", "three"));

		createdFields.add(descriptor);

		customFieldService.createCustomField(descriptor);

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		assertNotNull(created.getCustomFields());
		assertTrue(!created.getCustomFields().isEmpty());
		assertEquals("---", created.getCustomFields().get(descriptor.getName()));

		Map<String, String> customFields = new HashMap<String, String>();
		customFields.put(descriptor.getName(), "abc123 4");
		created.setCustomFields(customFields);

		try {
			taskService.updateTask(created);
			fail("expected validation exception");
		} catch (ValidationException e) {
			// expected
		}
		created = taskService.retrieveTask(created.getId());
		created.getCustomFields().put(descriptor.getName(), "one");
		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);

		assertNotNull(updated.getCustomFields());
		assertEquals("one", updated.getCustomFields().get(descriptor.getName()));
	}

	@Test
	public void testUpdateWithCustomFields_MultiSelect() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		FieldDescriptor descriptor = new FieldDescriptor();
		descriptor.setAvailableForNewTasks(true);
		descriptor.setDescription("TestA");
		descriptor.setFieldType(FieldType.MULTI_SELECT);
		descriptor.setName("testA" + System.currentTimeMillis());
		descriptor.setValueStrings(Arrays.asList("one", "two", "three"));

		createdFields.add(descriptor);

		customFieldService.createCustomField(descriptor);

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		assertNotNull(created.getCustomFields());
		assertTrue(!created.getCustomFields().isEmpty());
		assertEquals("", created.getCustomFields().get(descriptor.getName()));

		Map<String, String> customFields = new HashMap<String, String>();
		customFields.put(descriptor.getName(), "abc123 4");
		created.setCustomFields(customFields);

		try {
			taskService.updateTask(created);
			fail("expected validation exception");
		} catch (ValidationException e) {
			// expected
		}
		created = taskService.retrieveTask(created.getId());

		// two values
		created.getCustomFields().put(descriptor.getName(), "one,two");
		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);

		assertNotNull(updated.getCustomFields());
		assertEquals("one,two", updated.getCustomFields().get(descriptor.getName()));

		// one value
		updated.getCustomFields().put(descriptor.getName(), "one");
		updated = taskService.updateTask(updated);

		assertNotNull(updated.getCustomFields());
		assertEquals("one", updated.getCustomFields().get(descriptor.getName()));

		// null value
		updated.getCustomFields().put(descriptor.getName(), null);
		updated = taskService.updateTask(updated);

		assertNotNull(updated.getCustomFields());
		assertEquals("", updated.getCustomFields().get(descriptor.getName()));

		// empty value
		updated.getCustomFields().put(descriptor.getName(), "");
		updated = taskService.updateTask(updated);

		assertNotNull(updated.getCustomFields());
		assertEquals("", updated.getCustomFields().get(descriptor.getName()));
	}

	@Test
	public void testUpdateWithTaskTypeCustomField() throws Exception {

		String taskType = taskTypeValues[0];
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setTaskType(taskType);

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		int numCustomFields = created.getCustomFields().size();
		assertEquals(taskType, created.getTaskType());

		// Modify and re-save
		taskType = taskTypeValues[1];
		created.setTaskType(taskType);

		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);

		assertEquals(numCustomFields, updated.getCustomFields().size());
		assertEquals(taskType, updated.getTaskType());
	}

	@Test(expected = ValidationException.class)
	public void testUpdateWithInvalidTaskTypeCustomField() throws Exception {

		String taskType = taskTypeValues[0];
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setTaskType(taskType);

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		assertEquals(taskType, created.getTaskType());

		// Modify and re-save
		taskType = taskTypeValues[1] + "crazystuff";
		created.setTaskType(taskType);

		// This should blow up.
		taskService.updateTask(created);
	}

	@Test
	public void testUpdateWithIterationCustomField() throws Exception {

		Iteration iteration = new Iteration(iterationValues[0]);
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setIteration(iteration);

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		int numCustomFields = created.getCustomFields().size();
		assertEquals(iteration.getValue(), created.getIteration().getValue());

		// Modify and re-save
		iteration = new Iteration(iterationValues[1]);
		created.setIteration(iteration);

		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);

		assertEquals(numCustomFields, updated.getCustomFields().size());
		assertEquals(iteration.getValue(), updated.getIteration().getValue());
	}

	@Test(expected = ValidationException.class)
	public void testUpdateWithInvalidIterationCustomField() throws Exception {

		Iteration iteration = new Iteration(iterationValues[0]);
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setIteration(iteration);

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		assertEquals(iteration.getValue(), created.getIteration().getValue());

		// Modify and re-save
		iteration = new Iteration("wackystuff");
		created.setIteration(iteration);

		// This should blow up.
		taskService.updateTask(created);
	}

	@Test(expected = EntityNotFoundException.class)
	public void testInvalidRetrieve() throws ValidationException, EntityNotFoundException {
		taskService.retrieveTask(1223523);
	}

	protected com.tasktop.c2c.server.tasks.domain.Task getMockTask() throws ValidationException {
		return getMockTask("some description");
	}

	private RepositoryConfiguration repositoryConfiguration = null;

	private RepositoryConfiguration getRepositoryConfiguration() {
		if (repositoryConfiguration == null) {
			repositoryConfiguration = taskService.getRepositoryContext();
		}
		return repositoryConfiguration;
	}

	private com.tasktop.c2c.server.tasks.domain.Task getMockTask(String desc) throws ValidationException {
		RepositoryConfiguration repositoryConfiguration = getRepositoryConfiguration();
		com.tasktop.c2c.server.tasks.domain.Task toCreate = MockTaskFactory.createDO();
		toCreate.setId(null);
		toCreate.setDescription(desc);
		toCreate.setProduct(repositoryConfiguration.getProducts().get(0));
		toCreate.setComponent(getMockComponent());
		toCreate.setAssignee(ProfileConverter.copy(currentUser));
		toCreate.setSeverity(repositoryConfiguration.getSeverities().get(0));
		for (com.tasktop.c2c.server.tasks.domain.Milestone milestone : repositoryConfiguration.getMilestones()) {
			if (milestone.getProduct().equals(toCreate.getProduct())) {
				toCreate.setMilestone(milestone);
				break;
			}
		}

		toCreate.setStatus(repositoryConfiguration.getStatuses().get(0));
		toCreate.setPriority(repositoryConfiguration.getPriorities().get(0));
		toCreate.setResolution(repositoryConfiguration.getResolutions().get(0));
		return toCreate;
	}

	private com.tasktop.c2c.server.tasks.domain.Component getMockComponent() throws ValidationException {
		com.tasktop.c2c.server.tasks.domain.Component testComponent = new com.tasktop.c2c.server.tasks.domain.Component();
		testComponent.setName(RandomStringUtils.randomAlphanumeric(24));
		testComponent.setDescription(RandomStringUtils.randomAlphanumeric(1024));
		testComponent.setInitialOwner(getCreatedMockTaskUserProfile());
		testComponent.setProduct(getCreatedMockDomainProduct());

		com.tasktop.c2c.server.tasks.domain.Component createdComponent = taskService.createComponent(testComponent);
		return createdComponent;
	}

	@Test
	public void testWorkflowTaskCreation() throws ValidationException {
		int creationStatusCount = 0;
		int creationTestCount = 0;
		for (com.tasktop.c2c.server.tasks.domain.TaskStatus status : getRepositoryConfiguration().getStatuses()) {
			++creationTestCount;
			Boolean requireComment = false;
			Boolean canCreate = false;
			try {
				requireComment = (Boolean) entityManager
						.createQuery(
								"select sw.id.requireComment from " + StatusWorkflow.class.getSimpleName()
										+ " sw where sw.oldStatus is null and sw.newStatus.value = '"
										+ status.getValue() + "'").getSingleResult();
				canCreate = true;
			} catch (NoResultException e) {
				// expected
			}
			if (canCreate) {
				++creationStatusCount;
			}
			com.tasktop.c2c.server.tasks.domain.Task task = getMockTask("test " + status.getValue());
			task.setId(null);
			task.setStatus(status);
			if (requireComment) {
				Comment comment = new Comment();
				comment.setAuthor(task.getReporter());
				comment.setCommentText("a comment");
				task.getComments().add(comment);
			}
			ensureValidResolutionForStatus(task);
			try {
				taskService.createTask(task);
				if (canCreate) {
					// expected
				} else {
					fail("Should not be able to create a task with status " + status);
				}
			} catch (ValidationException e) {
				if (canCreate) {
					fail("Should be able to create a task with status " + status);
				} else {
					ValidationAssert.assertHaveValidationError(e, "task.invalidTaskCreationStatus",
							new Object[] { status.getValue() });
				}
			} catch (EntityNotFoundException e) {
				throw new IllegalStateException(e);
			}

		}
		// sanity
		assertTrue(creationStatusCount > 0);
		assertTrue(creationTestCount > creationStatusCount);
	}

	@Test
	public void testWorkflowTaskUpdate() throws EntityNotFoundException, ConcurrentUpdateException, ValidationException {
		int updateTestCount = 0;
		for (com.tasktop.c2c.server.tasks.domain.TaskStatus status : getRepositoryConfiguration().getStatuses()) {
			for (com.tasktop.c2c.server.tasks.domain.TaskStatus status2 : getRepositoryConfiguration().getStatuses()) {
				System.out.println("Test " + status + " -> " + status2);
				++updateTestCount;
				boolean transitionAllowed = false;
				boolean commentRequired = false;
				if (status.equals(status2)) {
					transitionAllowed = true;
				} else {
					try {
						commentRequired = (Boolean) entityManager.createQuery(
								"select sw.id.requireComment from " + StatusWorkflow.class.getSimpleName()
										+ " sw where sw.oldStatus.value = '" + status.getValue()
										+ "' and sw.newStatus.value = '" + status2.getValue() + "'").getSingleResult();
						transitionAllowed = true;
					} catch (NoResultException e) {
						// expected
					}
				}
				com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask("test " + status.getValue());
				com.tasktop.c2c.server.tasks.domain.Task task = taskService.createTask(toCreate);

				// create the initial state, bypassing workflow
				Task itask = entityManager.find(Task.class, task.getId());
				itask.setStatus(status.getValue());

				task.setStatus(status2);
				if (commentRequired) {
					Comment comment = new Comment();
					comment.setAuthor(task.getReporter());
					comment.setCommentText("a comment");
					task.getComments().add(comment);
				}
				ensureValidResolutionForStatus(task);

				try {
					taskService.updateTask(task);
					if (transitionAllowed) {
						// expected
					} else {
						fail("Should not be able to transition a task with status from " + status + " to " + status2);
					}
				} catch (ValidationException e) {
					if (transitionAllowed) {
						fail("Should be able to transition a task with status from " + status + " to " + status2 + ": "
								+ e.getMessage());
					} else {
						ValidationAssert.assertHaveValidationError(e, "task.invalidWorkflow",
								new Object[] { status2.getValue(), status.getValue() });
					}
				}
			}
		}
		// sanity
		assertTrue(updateTestCount > 0);
	}

	private void ensureValidResolutionForStatus(com.tasktop.c2c.server.tasks.domain.Task task) {
		if (task.getStatus().getValue().equals("RESOLVED")) {
			task.setResolution(new TaskResolution());
			task.getResolution().setValue("FIXED");
		} else {
			task.setResolution(null);
		}
	}

	@Test
	public void testTaskCreateRetrieveUpdateRetrieve() throws Exception {

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();

		toCreate.setReporter(null);
		toCreate.setWatchers(null);
		toCreate.setCreationDate(null);
		toCreate.setModificationDate(null);
		toCreate.setVersion(null);
		toCreate.setDescription(null);
		toCreate.setAssignee(null);
		toCreate.setDeadline(null);
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		assertNotNull(created);
		assertNotNull(created.getShortDescription());
		assertNotNull(created.getDescription());
		assertNotNull(created.getId());
		assertNotNull(created.getCreationDate());
		assertNotNull(created.getModificationDate());
		assertNotNull(created.getProduct());
		assertNotNull(created.getComponent());
		assertNotNull(created.getStatus());
		assertNotNull(created.getPriority());
		assertNotNull(created.getMilestone());
		assertNotNull(created.getReporter());
		assertNotNull(created.getReporter().getLoginName());
		assertNotNull(created.getWatchers());
		assertNotNull(created.getSubTasks());
		assertNotNull(created.getBlocksTasks());
		assertNotNull(created.getAttachments());
		assertNotNull(created.getComments());
		assertNotNull(created.getAssignee());
		assertNotNull(created.getAssignee().getLoginName());
		assertNull(created.getDeadline());

		com.tasktop.c2c.server.tasks.domain.Task fromService = taskService.retrieveTask(created.getId());
		assertNotNull(fromService);
		assertNotNull(fromService.getCreationDate());
		assertNotNull(fromService.getModificationDate());
		assertEquals(created.getId(), fromService.getId());
		assertEquals(toCreate.getShortDescription(), fromService.getShortDescription());
		assertEquals("", fromService.getDescription());

		assertEquals(toCreate.getSeverity().getValue(), fromService.getSeverity().getValue());
		assertEquals(toCreate.getMilestone().getId(), fromService.getMilestone().getId());
		assertEquals(toCreate.getStatus().getId(), fromService.getStatus().getId());
		assertEquals(toCreate.getPriority().getId(), fromService.getPriority().getId());

		// Try create again
		try {
			taskService.createTask(created);
			Assert.fail("created existing task");
		} catch (Exception e) {
			// expecte
		}

		// Update
		com.tasktop.c2c.server.tasks.domain.Task toUpdate = fromService;
		toUpdate.setShortDescription("New short description");
		toUpdate.setDeadline(new Date(System.currentTimeMillis() + 100000));
		// FIXME toUpdate.setKeywords("NEW KEYWORD");
		toUpdate.setCreationDate(null);

		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(toUpdate);

		fromService = taskService.retrieveTask(toUpdate.getId());
		assertEquals(toUpdate.getId(), fromService.getId());

		assertNotNull(fromService);
		assertNotNull(fromService.getReporter());
		assertNotNull(fromService.getReporter().getLoginName());
		assertNotNull(fromService.getCreationDate());
		assertEquals(updated.getId(), fromService.getId());
		assertEquals(toUpdate.getShortDescription(), fromService.getShortDescription());
		assertEquals(toUpdate.getDescription(), fromService.getDescription());

		assertEquals(toUpdate.getSeverity().getValue(), fromService.getSeverity().getValue());
		assertEquals(toUpdate.getMilestone().getId(), fromService.getMilestone().getId());
		assertEquals(toUpdate.getStatus().getId(), fromService.getStatus().getId());
		assertEquals(toUpdate.getPriority().getId(), fromService.getPriority().getId());
		assertEquals(toUpdate.getKeywords(), fromService.getKeywords());
		assertNotNull(fromService.getDeadline());
	}

	@Test
	public void testUpdateRetrieveProductComponentAsigneeAndDelta() throws Exception {
		RepositoryConfiguration repo = taskService.getRepositoryContext();
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setProduct(repo.getProducts().get(0));
		toCreate.setComponent(repo.getComponents().get(
				repo.getComponents().indexOf(toCreate.getProduct().getDefaultComponent())));
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		assertEquals(repo.getProducts().get(0), created.getProduct());
		assertEquals(repo.getProducts().get(0).getDefaultComponent(), created.getComponent());

		Date createdDiff = created.getModificationDate();
		com.tasktop.c2c.server.tasks.domain.Component testComponent = new com.tasktop.c2c.server.tasks.domain.Component();
		testComponent.setName(RandomStringUtils.randomAlphanumeric(24));
		testComponent.setDescription(RandomStringUtils.randomAlphanumeric(1024));
		testComponent.setProduct(getCreatedMockDomainProduct());

		com.tasktop.c2c.server.tasks.domain.Component createComponent = taskService.createComponent(testComponent);
		created.setProduct(testComponent.getProduct());
		created.setComponent(createComponent);
		created.setAssignee(repo.getUsers().get(0));
		Assert.assertNotSame(toCreate.getAssignee(), created.getAssignee());
		Thread.sleep(1000);// To get a later delta

		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);
		assertEquals(createComponent.getProduct(), updated.getProduct());
		assertEquals(createComponent, updated.getComponent());
		assertEquals(repo.getUsers().get(0), updated.getAssignee());
		assertTrue(createdDiff.before(updated.getModificationDate()));
	}

	@Test
	public void testFindTasksWithNumbericSearchTerm() {
		setupTestData();

		Integer id = tasks.get(0).getId();
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasks(id.toString(), null);
		assertNotNull(queryResult);
		assertEquals((Integer) 1, queryResult.getTotalResultSize());
		assertEquals(1, queryResult.getResultPage().size());
		assertEquals(id, queryResult.getResultPage().get(0).getId());

		// Test no results
		queryResult = taskService.findTasks("1234567", null);
		assertNotNull(queryResult);
		assertEquals(0, queryResult.getResultPage().size());
		assertEquals((Integer) 0, queryResult.getTotalResultSize());
	}

	@Test
	public void testFindTasksWithMatchingCommentText() {
		setupTestData();

		final String word = UUID.randomUUID().toString();
		String text = "leading text " + word + " trailing text";

		Task task = tasks.get(0);
		com.tasktop.c2c.server.internal.tasks.domain.Comment comment = new com.tasktop.c2c.server.internal.tasks.domain.Comment();
		comment.setProfile(currentUser);
		comment.setThetext(text);
		comment.setTask(task);
		task.getComments().add(comment);

		entityManager.persist(comment);

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasks(word, null);
		assertNotNull(result);
		assertEquals(Integer.valueOf(1), result.getTotalResultSize());
		assertEquals(task.getId(), result.getResultPage().get(0).getId());
	}

	@Test
	public void testFindTasksWithMatchingTwoCommentText() {
		setupTestData();

		String word1 = UUID.randomUUID().toString();
		String text1 = "leading text " + word1 + " trailing text";
		String word2 = UUID.randomUUID().toString();
		String text2 = "leading text " + word2 + " trailing text";

		Task task = tasks.get(0);
		com.tasktop.c2c.server.internal.tasks.domain.Comment comment1 = new com.tasktop.c2c.server.internal.tasks.domain.Comment();
		comment1.setProfile(currentUser);
		comment1.setThetext(text1);
		comment1.setTask(task);
		com.tasktop.c2c.server.internal.tasks.domain.Comment comment2 = new com.tasktop.c2c.server.internal.tasks.domain.Comment();
		comment2.setProfile(currentUser);
		comment2.setThetext(text2);
		comment2.setTask(task);

		task.getComments().add(comment1);
		task.getComments().add(comment2);

		entityManager.persist(comment1);
		entityManager.persist(comment2);

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasks(word1 + "       " + word2,
				null);
		assertNotNull(result);
		assertEquals(Integer.valueOf(1), result.getTotalResultSize());
		assertEquals(task.getId(), result.getResultPage().get(0).getId());

		// Verify not found w/extra term.
		result = taskService.findTasks(word1 + "       " + word2 + " " + UUID.randomUUID().toString(), null);
		assertNotNull(result);
		assertEquals(Integer.valueOf(0), result.getTotalResultSize());
		assertEquals(0, result.getResultPage().size());
	}

	@Test
	public void testFindTasksWithMatchingCommentAndSummaryText() {
		setupTestData();

		String word1 = UUID.randomUUID().toString();
		String text1 = "leading text " + word1 + " trailing text";
		String word2 = UUID.randomUUID().toString();
		String text2 = "leading text " + word2 + " trailing text";

		Task task = tasks.get(0);
		com.tasktop.c2c.server.internal.tasks.domain.Comment comment1 = new com.tasktop.c2c.server.internal.tasks.domain.Comment();
		comment1.setProfile(currentUser);
		comment1.setThetext(text1);
		comment1.setTask(task);

		task.setShortDesc(text2);
		task.getComments().add(comment1);

		entityManager.persist(comment1);

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> result = taskService.findTasks(word1 + "       " + word2,
				null);
		assertNotNull(result);
		assertEquals(Integer.valueOf(1), result.getTotalResultSize());
		assertEquals(task.getId(), result.getResultPage().get(0).getId());

		// Verify not found w/extra term.
		result = taskService.findTasks(word1 + "       " + word2 + " " + UUID.randomUUID().toString(), null);
		assertNotNull(result);
		assertEquals(Integer.valueOf(0), result.getTotalResultSize());
		assertEquals(0, result.getResultPage().size());
	}

	@Test
	public void testCreateRetrieveUpdateComments() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		String c1Text = "c1Text";
		String c2Text = "c2Text";
		int numInitalComments = getNumCommentsInDB();

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		Comment myComment = new Comment();
		myComment.setCommentText(c1Text);
		created.setComments(Arrays.asList(myComment));

		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);
		assertNotNull(updated);
		assertNotNull(updated.getComments());
		assertEquals(1, updated.getComments().size());
		assertEquals(toCreate.getDescription(), updated.getDescription());
		assertEquals(c1Text, updated.getComments().get(0).getCommentText());
		assertNotNull(updated.getComments().get(0).getAuthor());
		assertNotNull(updated.getComments().get(0).getCreationDate());
		assertEquals(numInitalComments + 2, getNumCommentsInDB()); // One is
																	// description

		Comment anotherComment = new Comment();
		anotherComment.setCommentText(c2Text);
		updated.getComments().add(anotherComment);
		Thread.sleep(MINIMUM_DATE_RESOLUTION);
		updated = taskService.updateTask(updated);
		assertNotNull(updated);
		assertNotNull(updated.getComments());
		assertEquals(2, updated.getComments().size());
		assertEquals(c2Text, updated.getComments().get(1).getCommentText());
		assertNotNull(updated.getComments().get(0).getAuthor());
		assertNotNull(updated.getComments().get(0).getCreationDate());
		assertEquals(numInitalComments + 3, getNumCommentsInDB());

		com.tasktop.c2c.server.tasks.domain.Task retrieved = taskService.retrieveTask(updated.getId());
		assertNotNull(retrieved);
		assertNotNull(retrieved.getComments());
		assertEquals(2, retrieved.getComments().size());
		assertEquals(c2Text, retrieved.getComments().get(1).getCommentText());
	}

	@Test
	public void testCreateWithInitialComments() throws ValidationException, EntityNotFoundException {
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		String c1Text = "c1Text";

		Comment myComment = new Comment();
		myComment.setCommentText(c1Text);
		myComment.setAuthor(toCreate.getReporter());
		myComment.setCreationDate(new Date());

		toCreate.setComments(Arrays.asList(myComment));
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		assertEquals(1, created.getComments().size());
		assertEquals(c1Text, created.getComments().get(0).getCommentText());

	}

	private int getNumCommentsInDB() {
		Number comments = (Number) entityManager.createQuery(
				"select count(c) from " + com.tasktop.c2c.server.internal.tasks.domain.Comment.class.getSimpleName()
						+ " c").getSingleResult();
		return comments.intValue();
	}

	@Test
	public void testCreateRetrieveUpdateParentAndBlocks() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task parentTask = taskService.createTask(getMockTask("parent"));
		com.tasktop.c2c.server.tasks.domain.Task blockedTask1 = taskService.createTask(getMockTask("blocked1"));
		com.tasktop.c2c.server.tasks.domain.Task blockedTask2 = taskService.createTask(getMockTask("blocked2"));

		// FIXME ?? It is this ordering that defines which is the parent
		assertTrue(parentTask.getId() < blockedTask1.getId());
		assertTrue(parentTask.getId() < blockedTask2.getId());
		assertTrue(blockedTask1.getId() < blockedTask2.getId());

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask("task with parents and blocks");

		toCreate.setBlocksTasks(Arrays.asList(parentTask, blockedTask1, blockedTask2));

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		assertNotNull(created.getParentTask());
		assertEquals(parentTask.getId(), created.getParentTask().getId());
		assertEquals(3, created.getBlocksTasks().size());

		// task 331 updateTask deletes tasks
		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);
		assertEquals(3, updated.getBlocksTasks().size());

		try {
			com.tasktop.c2c.server.tasks.domain.Task self = new com.tasktop.c2c.server.tasks.domain.Task();
			self.setId(updated.getId());
			updated.setBlocksTasks(Arrays.asList(blockedTask1, blockedTask2, self));
			taskService.updateTask(updated);
			Assert.fail("expceted validation exception");
		} catch (ValidationException ex) {
			// Expected
		}

		try {
			updated.setBlocksTasks(Arrays.asList(blockedTask1));
			updated.setSubTasks(Arrays.asList(blockedTask1));
			taskService.updateTask(updated);
			Assert.fail("expceted validation exception");
		} catch (ValidationException ex) {
			// Expected
		}

		// Check the dep is represented in parent
		parentTask = taskService.retrieveTask(parentTask.getId());
		assertNotNull(parentTask.getSubTasks());
		assertEquals(1, parentTask.getSubTasks().size());
		assertEquals(created.getId(), parentTask.getSubTasks().get(0).getId());
		assertNotNull(parentTask.getSubTasks().get(0).getShortDescription());
		assertNotNull(parentTask.getSubTasks().get(0).getPriority());
		assertNotNull(parentTask.getSubTasks().get(0).getStatus());
		assertNotNull(parentTask.getSubTasks().get(0).getSeverity());

		// Check the dep is represented in the other blocked
		blockedTask1 = taskService.retrieveTask(blockedTask1.getId());
		assertNotNull(blockedTask1.getSubTasks());
		assertEquals(1, blockedTask1.getSubTasks().size());
		assertEquals(created.getId(), blockedTask1.getSubTasks().get(0).getId());

		blockedTask2 = taskService.retrieveTask(blockedTask2.getId());
		assertNotNull(blockedTask2.getSubTasks());
		assertEquals(1, blockedTask2.getSubTasks().size());
		assertEquals(created.getId(), blockedTask2.getSubTasks().get(0).getId());

		// Now remove the assosication
		updated.setParentTask(null);
		updated.setBlocksTasks(null);
		updated.setSubTasks(null);
		taskService.updateTask(updated);
		updated = taskService.retrieveTask(created.getId());
		assertNull(updated.getParentTask());
		assertTrue(updated.getBlocksTasks().isEmpty());

		blockedTask1 = taskService.retrieveTask(blockedTask1.getId());
		assertTrue(blockedTask1.getSubTasks().isEmpty());
		blockedTask2 = taskService.retrieveTask(blockedTask2.getId());
		assertTrue(blockedTask2.getSubTasks().isEmpty());

		// Now add it back
		updated.setBlocksTasks(Arrays.asList(parentTask, blockedTask1, blockedTask2));
		updated = taskService.updateTask(updated);
		assertNotNull(updated.getParentTask());
		assertEquals(parentTask.getId(), updated.getParentTask().getId());
		assertEquals(3, updated.getBlocksTasks().size());

	}

	@Test
	public void testCreateUpdateWithSubtaskCycle() throws Exception {
		// Create a -> b -> c
		com.tasktop.c2c.server.tasks.domain.Task a = taskService.createTask(getMockTask("a"));
		com.tasktop.c2c.server.tasks.domain.Task b = getMockTask("b");
		b.setParentTask(a);
		b = taskService.createTask(b);
		com.tasktop.c2c.server.tasks.domain.Task c = getMockTask("c");
		c.setParentTask(b);
		c = taskService.createTask(c);

		com.tasktop.c2c.server.tasks.domain.Task createsCycle = getMockTask("cycle");
		createsCycle.setParentTask(c);
		createsCycle.setSubTasks(Arrays.asList(a));

		try {
			taskService.createTask(createsCycle);
			Assert.fail("Expect validation exception");
		} catch (ValidationException e) {
			// expected
		}

		createsCycle.setParentTask(null);
		createsCycle.setSubTasks(null);
		createsCycle = taskService.createTask(createsCycle);

		createsCycle.setParentTask(a);
		createsCycle.setSubTasks(Arrays.asList(c));
		try {
			taskService.updateTask(createsCycle);
			Assert.fail("Expect validation exception");
		} catch (ValidationException e) {
			// expected
		}
	}

	@Test
	public void testCreateUpdateWithSelfParent() throws Exception {
		// Create a -> a
		com.tasktop.c2c.server.tasks.domain.Task a = getMockTask("a");
		a.setParentTask(new com.tasktop.c2c.server.tasks.domain.Task());

		try {
			taskService.createTask(a);
			Assert.fail("Expect validation exception");
		} catch (ValidationException e) {
			// expected
		}

		a.setParentTask(null);

		a = taskService.createTask(a);
		a.setParentTask(taskService.retrieveTask(a.getId()));

		try {
			taskService.updateTask(a);
			Assert.fail("Expect validation exception");
		} catch (ValidationException e) {
			// expected
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateRetrieveUpdateSubtasks() throws Exception {
		// Now try creating the relationship through the subTask association.
		com.tasktop.c2c.server.tasks.domain.Task subTask1 = taskService.createTask(getMockTask("subTask1"));
		com.tasktop.c2c.server.tasks.domain.Task subTask2 = taskService.createTask(getMockTask("subTask2"));
		com.tasktop.c2c.server.tasks.domain.Task subTask3 = taskService.createTask(getMockTask("subTask3"));
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask("task with subTasks");
		toCreate.setSubTasks(Arrays.asList(subTask1, subTask2));
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		assertNotNull(created.getSubTasks());
		assertEquals(2, created.getSubTasks().size());

		// Check the dep is represented in the subtasks
		subTask1 = taskService.retrieveTask(subTask1.getId());
		assertNotNull(subTask1.getParentTask());
		assertEquals(created.getId(), subTask1.getParentTask().getId());
		subTask2 = taskService.retrieveTask(subTask2.getId());
		assertNotNull(subTask2.getParentTask());
		assertEquals(created.getId(), subTask2.getParentTask().getId());

		// Now remove that association
		created.setSubTasks(Collections.EMPTY_LIST);
		taskService.updateTask(created);
		created = taskService.retrieveTask(created.getId());
		assertTrue(created.getSubTasks() == null || created.getSubTasks().isEmpty());
		subTask1 = taskService.retrieveTask(subTask1.getId());
		assertNull(subTask1.getParentTask());
		subTask2 = taskService.retrieveTask(subTask2.getId());
		assertNull(subTask2.getParentTask());

		created.setSubTasks(Arrays.asList(subTask2));
		created = taskService.updateTask(created);
		assertEquals(1, created.getSubTasks().size());

		created.setSubTasks(Arrays.asList(subTask1, subTask2, subTask3));
		created = taskService.updateTask(created);
		assertEquals(3, created.getSubTasks().size());
	}

	@Test
	public void testCreateUpdateWatchers() throws Exception {
		TaskUserProfile watcher1 = getCreatedMockTaskUserProfile();
		TaskUserProfile watcher2 = getCreatedMockTaskUserProfile();
		TaskUserProfile watcher3 = getCreatedMockTaskUserProfile();

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setWatchers(Arrays.asList(watcher1));
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		assertNotNull(created.getWatchers());
		assertEquals(1, created.getWatchers().size());
		assertEquals(watcher1.getId(), created.getWatchers().get(0).getId());

		// Remove wather1
		created.setWatchers(null);
		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);
		assertTrue(updated.getWatchers().isEmpty());

		// Add wather2
		updated.setWatchers(Arrays.asList(watcher2));
		updated = taskService.updateTask(updated);
		assertNotNull(updated.getWatchers());
		assertEquals(1, updated.getWatchers().size());
		assertEquals(watcher2.getId(), updated.getWatchers().get(0).getId());

		// Add all watchers
		updated.setWatchers(Arrays.asList(watcher1, watcher2, watcher3));
		updated = taskService.updateTask(updated);
		assertEquals(3, updated.getWatchers().size());

	}

	@Test
	public void testCreateUpdateDuplicates() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task dupOf1 = taskService.createTask(getMockTask());
		com.tasktop.c2c.server.tasks.domain.Task dupOf2 = taskService.createTask(getMockTask());

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setDuplicateOf(dupOf1);
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		assertNotNull(created.getDuplicateOf());
		assertEquals(dupOf1.getId(), created.getDuplicateOf().getId());

		dupOf1 = taskService.retrieveTask(dupOf1.getId());
		assertEquals(1, dupOf1.getDuplicates().size());
		assertEquals(created.getId(), dupOf1.getDuplicates().get(0).getId());

		// Do a regular update (no changes)
		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);

		// Try without an id.
		updated.setDuplicateOf(new com.tasktop.c2c.server.tasks.domain.Task());
		try {
			taskService.updateTask(updated);
			Assert.fail("expected exception");
		} catch (ValidationException e) {
			// Expected
		}

		// Try with self
		updated.setDuplicateOf(new com.tasktop.c2c.server.tasks.domain.Task());
		updated.getDuplicateOf().setId(updated.getId());
		try {
			taskService.updateTask(updated);
			Assert.fail("expected exception");
		} catch (ValidationException e) {
			// Expected
		}

		// Really change the duplicate
		updated.setDuplicateOf(new com.tasktop.c2c.server.tasks.domain.Task());
		updated.getDuplicateOf().setId(dupOf2.getId());
		updated = taskService.updateTask(updated);
		assertEquals(dupOf2.getId(), updated.getDuplicateOf().getId());

		// Remove the duplicate
		updated.setDuplicateOf(null);
		updated = taskService.updateTask(updated);
		assertTrue(updated.getDuplicateOf() == null);

		dupOf1 = taskService.retrieveTask(dupOf1.getId());
		assertEquals(0, dupOf1.getDuplicates().size());
		dupOf2 = taskService.retrieveTask(dupOf2.getId());
		assertEquals(0, dupOf2.getDuplicates().size());

		// Add it back
		updated.setDuplicateOf(dupOf1);
		updated = taskService.updateTask(updated);

		assertNotNull(updated.getDuplicateOf());
		assertEquals(dupOf1.getId(), updated.getDuplicateOf().getId());

		dupOf1 = taskService.retrieveTask(dupOf1.getId());
		assertEquals(1, dupOf1.getDuplicates().size());
		assertEquals(created.getId(), dupOf1.getDuplicates().get(0).getId());

	}

	/**
	 * If the owner is null and the component has a default, then set assignee.
	 */
	@Test
	public void testCreateWithNoOwner_ComponentWithDefaultOwner() throws ValidationException, EntityNotFoundException {
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setAssignee(null);

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		assertNotNull(created.getAssignee());
	}

	/**
	 * Explicitly allow null owner. If the owner is null and the component has a default, then set assignee.
	 */
	@Test
	public void testCreateWithNoOwner_ComponentWithNoDefaultOwner() throws ValidationException, EntityNotFoundException {

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setAssignee(null);

		Component component = MockComponentFactory.create(entityManager);
		component.setInitialOwner(null);
		entityManager.flush();
		entityManager.refresh(component);

		com.tasktop.c2c.server.tasks.domain.Component dComponent = new com.tasktop.c2c.server.tasks.domain.Component();
		dComponent.setId(component.getId().intValue());

		toCreate.setComponent(dComponent);

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		assertNull(created.getAssignee());
	}

	/**
	 * An owner with a null id or name should be handled gracefully as if it were a null object.
	 */
	@Test
	public void testCreateWithInvalidOwner() throws ValidationException, EntityNotFoundException {
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		TaskUserProfile owner = new TaskUserProfile();
		owner.setId(null);
		owner.setLoginName("");
		owner.setRealname("Some Guy");
		toCreate.setAssignee(owner);
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		// FIXME: this depends on prelaoded fixture data.
		// confirm the Name is not the fake one we set. (It's really from fixture data.)
		assertFalse(owner.getRealname().equals(created.getAssignee()));
	}

	@Test
	public void testCreateDoesntUpdateComponent() throws ValidationException, EntityNotFoundException {
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Component component = toCreate.getComponent();
		String originalName = component.getName();
		component.setName(originalName + "_changed");
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		Component component2 = entityManager.find(Component.class, component.getId().shortValue());

		assertEquals(originalName, component2.getName());
		assertEquals(originalName, created.getComponent().getName());
	}

	@Test
	public void testCreateWithCustomFields() throws ValidationException, EntityNotFoundException {
		FieldDescriptor descriptor = new FieldDescriptor();
		descriptor.setAvailableForNewTasks(true);
		descriptor.setDescription("TestA");
		descriptor.setFieldType(FieldType.TEXT);
		descriptor.setName("testA" + System.currentTimeMillis());

		createdFields.add(descriptor);

		customFieldService.createCustomField(descriptor);

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		Map<String, String> customFields = new HashMap<String, String>();
		customFields.put(descriptor.getName(), "abc123 4");
		toCreate.setCustomFields(customFields);

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		assertNotNull(created.getCustomFields());
		assertEquals("abc123 4", created.getCustomFields().get(descriptor.getName()));
	}

	@Test
	public void testCreateWithCustomFieldsValidationFails() throws ValidationException, EntityNotFoundException {
		// test for task 512: task is created even when a custom field
		// validation fails
		FieldDescriptor descriptor = new FieldDescriptor();
		descriptor.setAvailableForNewTasks(true);
		descriptor.setDescription("TestA");
		descriptor.setFieldType(FieldType.SINGLE_SELECT);
		descriptor.setName("testA" + System.currentTimeMillis());
		descriptor.setValueStrings(Arrays.asList("one", "two"));

		createdFields.add(descriptor);

		customFieldService.createCustomField(descriptor);

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		// something distinctive that will make it easy to find
		toCreate.setShortDescription(UUID.randomUUID().toString());
		Map<String, String> customFields = new HashMap<String, String>();
		customFields.put(descriptor.getName(), "three"); // intentionally
															// provide an
															// invalid value
		assertFalse(descriptor.getValues().contains(customFields.get(descriptor.getName())));
		toCreate.setCustomFields(customFields);

		try {
			taskService.createTask(toCreate);
			fail("Expected validation error");
		} catch (ValidationException e) {
			// expected
		}

		// verify that when the transaction is rolled back, the row does not
		// exist
		entityManager.flush();
		entityManager.clear();
		entityManager.createNativeQuery("rollback").executeUpdate();
		List<?> tasks = entityManager
				.createQuery("select e from " + Task.class.getSimpleName() + " e where e.shortDesc = :s")
				.setParameter("s", toCreate.getShortDescription()).getResultList();
		assertTrue(tasks.isEmpty());
	}

	@Test
	public void testCreateWithTaskTypeCustomField() throws ValidationException, EntityNotFoundException {

		String taskType = taskTypeValues[0];
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setTaskType(taskType);

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		assertEquals(taskType, created.getTaskType());
	}

	@Test(expected = ValidationException.class)
	public void testCreateWithInvalidTaskTypeCustomField() throws ValidationException, EntityNotFoundException {

		String taskType = taskTypeValues[0] + "invalidStuff";
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setTaskType(taskType);

		// This should blow up.
		taskService.createTask(toCreate);
	}

	@Test
	public void testCreateWithIterationCustomField() throws ValidationException, EntityNotFoundException {

		Iteration iteration = new Iteration(iterationValues[0]);
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setIteration(iteration);

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		assertEquals(iteration.getValue(), created.getIteration().getValue());
	}

	@Test(expected = ValidationException.class)
	public void testCreateWithInvalidIterationCustomField() throws ValidationException, EntityNotFoundException {

		Iteration iteration = new Iteration("invalidStuff");
		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setIteration(iteration);

		// This should blow up.
		taskService.createTask(toCreate);
	}

	private Profile getCreatedMockProfile() {
		Profile p = MockProfileFactory.create(entityManager);
		entityManager.flush();
		assertNotNull(p.getId());
		return p;
	}

	private TaskUserProfile getCreatedMockTaskUserProfile() {
		return ProfileConverter.copy(getCreatedMockProfile());
	}

	@Test
	public void testGetRepositoryContext() {
		MockKeyworddefFactory.create(entityManager, 5);

		RepositoryConfiguration ctx = taskService.getRepositoryContext();
		assertNotNull(ctx);
		assertEquals(2, ctx.getMilestones().size());
		assertEquals(count(Priority.class), ctx.getPriorities().size());
		assertEquals(count(TaskSeverity.class), ctx.getSeverities().size());
		assertEquals(count(TaskStatus.class), ctx.getStatuses().size());
		assertEquals(count(Resolution.class), ctx.getResolutions().size());
		assertTrue(ctx.getUsers().size() >= 2);
		assertEquals(1, ctx.getProducts().size());
		assertEquals(1, ctx.getComponents().size());
		assertNotNull(ctx.getComponents().get(0).getProduct());
		assertNotNull(ctx.getDefaultPriority());
		assertEquals("Normal", ctx.getDefaultPriority().getValue());
		assertNotNull(ctx.getDefaultProduct());
		assertNotNull(ctx.getDefaultProduct().getDefaultComponent());
		assertNotNull(ctx.getDefaultProduct().getDefaultMilestone());
		assertNotNull(ctx.getDefaultSeverity());
		assertEquals("normal", ctx.getDefaultSeverity().getValue());
		assertNotNull(ctx.getDefaultStatus());
		assertNotNull(ctx.getDefaultResolution());
		assertNotNull(ctx.getKeywords());
		assertFalse(ctx.getKeywords().isEmpty());
		for (Keyword keyword : ctx.getKeywords()) {
			assertNotNull(keyword.getName());
			assertNotNull(keyword.getDescription());
		}

		int numComponentsFound = 0;
		for (com.tasktop.c2c.server.tasks.domain.Product product : ctx.getProducts()) {
			List<com.tasktop.c2c.server.tasks.domain.Component> componentsForProduct = ctx.getComponents(product);
			numComponentsFound += componentsForProduct.size();
		}
		assertEquals(ctx.getComponents().size(), numComponentsFound);

		// Make sure our special fields aren't in the custom field list.
		for (FieldDescriptor desc : ctx.getCustomFields()) {
			assertFalse(Task.CUSTOM_TYPE_FIELD_NAME.equals(desc.getName()));
			assertFalse(Task.CUSTOM_ITERATION_FIELD_NAME.equals(desc.getName()));
		}

		for (String curTaskType : taskTypeValues) {
			assertTrue(ctx.getTaskTypes().contains(curTaskType));
		}

		Set<String> itValues = new HashSet<String>();
		for (Iteration it : ctx.getIterations()) {
			itValues.add(it.getValue());
		}

		for (String curIteration : iterationValues) {
			assertTrue(itValues.contains(curIteration));
		}

		assertSerializable(ctx);
	}

	@Test
	public void testGetRepositoryContextStateTransitions() {
		RepositoryConfiguration ctx = taskService.getRepositoryContext();
		assertNotNull(ctx);

		assertNotNull(ctx.getStateTransitions());
		assertFalse(ctx.getStateTransitions().isEmpty());

		List<com.tasktop.c2c.server.tasks.domain.TaskStatus> initialStatuses = ctx.computeValidStatuses(null);
		assertNotNull(initialStatuses);
		assertFalse(initialStatuses.isEmpty());
		for (com.tasktop.c2c.server.tasks.domain.TaskStatus status : initialStatuses) {
			List<com.tasktop.c2c.server.tasks.domain.TaskStatus> states = ctx.computeValidStatuses(status);
			assertNotNull(states);
			assertFalse(states.isEmpty());
			assertTrue(states.contains(status));
		}
	}

	private void assertSerializable(Object object) {
		assertNotNull(object);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(out);
			objectOut.writeObject(object);
			objectOut.close();
			ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
			Object deserializedObject = objectIn.readObject();
			assertNotNull(deserializedObject);
		} catch (Throwable t) {
			Assert.fail("Object is not serializable: " + object);
		}
	}

	@Test
	public void testGetRepositoryContextWithCustomField() throws Exception {
		FieldDescriptor descriptor = new FieldDescriptor();
		descriptor.setAvailableForNewTasks(true);
		descriptor.setDescription("Test1");
		descriptor.setFieldType(FieldType.TEXT);
		descriptor.setName("test" + System.currentTimeMillis());

		createdFields.add(descriptor);

		customFieldService.createCustomField(descriptor);

		FieldDescriptor descriptor2 = new FieldDescriptor();
		descriptor2.setAvailableForNewTasks(true);
		descriptor2.setDescription("Test2");
		descriptor2.setFieldType(FieldType.SINGLE_SELECT);
		descriptor2.setName("test2" + System.currentTimeMillis());
		descriptor2.setValueStrings(Arrays.asList("one", "two"));
		createdFields.add(descriptor2);

		customFieldService.createCustomField(descriptor2);

		RepositoryConfiguration ctx = taskService.getRepositoryContext();
		assertNotNull(ctx);

		assertNotNull(ctx.getCustomFields());

		FieldDescriptor[] originalDescriptors = new FieldDescriptor[] { descriptor, descriptor2 };
		int foundCount = 0;
		for (FieldDescriptor fd : ctx.getCustomFields()) {
			for (FieldDescriptor original : originalDescriptors) {
				if (original.equals(fd)) {
					++foundCount;

					assertEquals(original.getName(), fd.getName());
					assertEquals(original.getDescription(), fd.getDescription());
					assertEquals(original.getFieldType(), fd.getFieldType());
					if (fd.getValues() != null) {
						assertNotNull(original.getValues());
						assertTrue(fd.getValueStrings().containsAll(original.getValueStrings()));
					} else {
						assertNull(original.getValues());
					}

					break;
				}
			}
		}
		assertEquals(originalDescriptors.length, foundCount);
	}

	private void assertByteArrayEquals(byte[] expected, byte[] actual) {
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i]);
		}
	}

	@Test
	public void testSaveAndRetrieveAttachment() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task task = taskService.createTask(getMockTask());

		com.tasktop.c2c.server.tasks.domain.Attachment attachment1 = new com.tasktop.c2c.server.tasks.domain.Attachment();
		attachment1.setDescription("desc");
		attachment1.setFilename("filename");
		attachment1.setMimeType("mimetype");
		Thread.sleep(MINIMUM_DATE_RESOLUTION);
		try {
			taskService.saveAttachment(task.getTaskHandle(), attachment1);
			Assert.fail("expect validation exception");
		} catch (ValidationException e) {
			// expected
		}
		attachment1.setAttachmentData("DATA".getBytes());
		AttachmentHandle attachmentHandle = taskService.saveAttachment(task.getTaskHandle(), attachment1);
		int attachId = attachmentHandle.getId();

		com.tasktop.c2c.server.tasks.domain.Task retrieved = taskService.retrieveTask(task.getId());
		assertNotNull(retrieved.getAttachments());
		assertEquals(1, retrieved.getAttachments().size());
		assertEquals((Integer) attachId, retrieved.getAttachments().get(0).getId());
		assertEquals(attachment1.getMimeType(), retrieved.getAttachments().get(0).getMimeType());
		assertNotNull(retrieved.getAttachments().get(0).getCreationDate());
		assertNull(retrieved.getAttachments().get(0).getAttachmentData());
		assertEquals((Integer) attachment1.getAttachmentData().length, retrieved.getAttachments().get(0).getByteSize());
		// Make sure updating attachments updates the timestamp.
		assertTrue(task.getModificationDate().before(retrieved.getModificationDate()));

		com.tasktop.c2c.server.tasks.domain.Attachment fatAttachment = taskService.retrieveAttachment(attachId);
		assertNotNull(fatAttachment);
		assertNotNull(fatAttachment.getAttachmentData());
		assertByteArrayEquals(attachment1.getAttachmentData(), fatAttachment.getAttachmentData());

		com.tasktop.c2c.server.tasks.domain.Attachment attachment2 = new com.tasktop.c2c.server.tasks.domain.Attachment();
		attachment2.setDescription("desc2");
		attachment2.setFilename("filename2");
		attachment2.setMimeType("mimetype2");
		attachment2.setAttachmentData("DATA2ABCD".getBytes());

		try {
			taskService.saveAttachment(task.getTaskHandle(), attachment2);
			Assert.fail("Using old task handle should fail to update");
		} catch (ConcurrentUpdateException ex) {
			// Expected
		}

		task.setTaskHandle(attachmentHandle.getTaskHandle());
		attachmentHandle = taskService.saveAttachment(task.getTaskHandle(), attachment2);

		attachId = attachmentHandle.getId();

		task = taskService.retrieveTask(task.getId());
		assertEquals(2, task.getAttachments().size());
		assertEquals((Integer) attachId, task.getAttachments().get(1).getId());
		assertEquals(attachment2.getMimeType(), task.getAttachments().get(1).getMimeType());
		assertNotNull(task.getAttachments().get(1).getCreationDate());
		assertNull(task.getAttachments().get(1).getAttachmentData());
		assertEquals((Integer) attachment2.getAttachmentData().length, task.getAttachments().get(1).getByteSize());

	}

	@Test
	public void testSaveAndRetrieveAttachmentViaUpdate() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task task = taskService.createTask(getMockTask());

		com.tasktop.c2c.server.tasks.domain.Attachment attachment1 = new com.tasktop.c2c.server.tasks.domain.Attachment();
		attachment1.setDescription("desc");
		attachment1.setFilename("filename");
		attachment1.setMimeType("mimetype");
		attachment1.setAttachmentData("DATA".getBytes());
		task.getAttachments().add(attachment1);

		com.tasktop.c2c.server.tasks.domain.Task retrieved = taskService.updateTask(task);
		assertNotNull(retrieved.getAttachments());
		assertEquals(1, retrieved.getAttachments().size());
		assertEquals(attachment1.getMimeType(), retrieved.getAttachments().get(0).getMimeType());
		assertNotNull(retrieved.getAttachments().get(0).getCreationDate());
		assertNull(retrieved.getAttachments().get(0).getAttachmentData());
		assertEquals((Integer) attachment1.getAttachmentData().length, retrieved.getAttachments().get(0).getByteSize());

		com.tasktop.c2c.server.tasks.domain.Attachment fatAttachment = taskService.retrieveAttachment(retrieved
				.getAttachments().get(0).getId());
		assertNotNull(fatAttachment);
		assertNotNull(fatAttachment.getAttachmentData());
		assertByteArrayEquals(attachment1.getAttachmentData(), fatAttachment.getAttachmentData());

		com.tasktop.c2c.server.tasks.domain.Attachment attachment2 = new com.tasktop.c2c.server.tasks.domain.Attachment();
		attachment2.setDescription("desc2");
		attachment2.setFilename("filename2");
		attachment2.setMimeType("mimetype2");
		attachment2.setAttachmentData("DATA2ABCD".getBytes());
		retrieved.getAttachments().clear();
		retrieved.getAttachments().add(attachment2);

		task = taskService.updateTask(retrieved);
		assertEquals(2, task.getAttachments().size());
		assertEquals(attachment2.getMimeType(), task.getAttachments().get(1).getMimeType());
		assertNotNull(task.getAttachments().get(1).getCreationDate());
		assertNull(task.getAttachments().get(1).getAttachmentData());
		assertEquals((Integer) attachment2.getAttachmentData().length, task.getAttachments().get(1).getByteSize());

	}

	@Test
	public void testSaveAndRetrieveAttachmentAndComment() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task task = taskService.createTask(getMockTask());

		com.tasktop.c2c.server.tasks.domain.Attachment attachment1 = new com.tasktop.c2c.server.tasks.domain.Attachment();
		attachment1.setDescription("desc");
		attachment1.setFilename("filename");
		attachment1.setMimeType("mimetype");
		attachment1.setAttachmentData("DATA".getBytes());

		Comment comment = new Comment();
		comment.setCommentText("Attaching: " + attachment1.getFilename());

		AttachmentHandle attachmentHandle = taskService.saveAttachment(task.getTaskHandle(), attachment1, comment);

		task = taskService.retrieveTask(task.getId());
		assertNotNull(task.getAttachments());
		assertEquals(1, task.getAttachments().size());
		assertEquals((Integer) attachmentHandle.getId(), task.getAttachments().get(0).getId());
		assertEquals(attachment1.getMimeType(), task.getAttachments().get(0).getMimeType());
		assertNotNull(task.getAttachments().get(0).getCreationDate());
		assertNull(task.getAttachments().get(0).getAttachmentData());
		assertEquals((Integer) attachment1.getAttachmentData().length, task.getAttachments().get(0).getByteSize());
		assertEquals(1, task.getComments().size());
		assertEquals(comment.getCommentText(), task.getComments().get(0).getCommentText());

		com.tasktop.c2c.server.tasks.domain.Attachment fatAttachment = taskService.retrieveAttachment(attachmentHandle
				.getId());
		assertNotNull(fatAttachment);
		assertNotNull(fatAttachment.getAttachmentData());
		assertByteArrayEquals(attachment1.getAttachmentData(), fatAttachment.getAttachmentData());

	}

	// TASK 162
	@Test
	public void testQueryWithInvalidTasks() {
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithQuery(
				PredefinedTaskQuery.ALL, null);
		int initialPageSize = queryResult.getResultPage().size();
		int initialTotalSize = queryResult.getTotalResultSize();

		Milestone stone = entityManager.find(Milestone.class, 1);
		stone.setValue("---X");
		entityManager.flush();
		queryResult = taskService.findTasksWithQuery(PredefinedTaskQuery.ALL, null);
		int finalPageSize = queryResult.getResultPage().size();
		int finalTotalSize = queryResult.getTotalResultSize();

		assertEquals(initialTotalSize, finalTotalSize);
		assertEquals(initialPageSize, finalPageSize);
	}

	@Test
	public void testProvisionCurrentUser() throws ValidationException, EntityNotFoundException {
		entityManager.remove(currentUser);
		entityManager.flush();

		AuthenticationToken token = new AuthenticationToken();
		token.setFirstName("first");
		token.setLastName("Las");
		token.setUsername("username");
		token.setKey("foobar");

		SecurityContextHolder.getContext().setAuthentication(
				new PreAuthenticatedAuthenticationToken(AuthenticationServiceUser.fromAuthenticationToken(token),
						"ROLES"));

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		toCreate.setAssignee(null);
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		Assert.assertEquals(token.getUsername(), created.getReporter().getLoginName());

	}

	@Test
	public void testDeltaTimestampMaintainance() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(getMockTask());
		created.setDescription("description");

		Thread.sleep(MINIMUM_DATE_RESOLUTION);

		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);

		Assert.assertFalse(created.getModificationDate().equals(updated.getModificationDate()));

	}

	@Test
	public void testOptimisticLocking() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(getMockTask());
		com.tasktop.c2c.server.tasks.domain.Task created2 = taskService.retrieveTask(created.getId());

		Thread.sleep(MINIMUM_DATE_RESOLUTION);

		created.setDescription("New description");
		taskService.updateTask(created);
		created2.setDescription("Another new description");
		try {
			taskService.updateTask(created2);
			Assert.fail("mid-air collision was not detected");
		} catch (ConcurrentUpdateException e) {
			// expected
		}
	}

	@SuppressWarnings("unchecked")
	private void removeTasks() {
		for (Task task : (List<Task>) entityManager.createQuery("SELECT task FROM Task task").getResultList()) {
			entityManager.remove(task);
		}
	}

	@Test
	public void testCriteriaQueryWithSimpleFieldsPlusAndOr() throws Exception {
		removeTasks();
		com.tasktop.c2c.server.tasks.domain.Task expectedResult = getMockTask();

		Criteria summaryCrit = new ColumnCriteria(TaskFieldConstants.SUMMARY_FIELD,
				expectedResult.getShortDescription());
		Criteria descriptionCrit = new ColumnCriteria(TaskFieldConstants.DESCRIPTION_FIELD,
				expectedResult.getDescription());
		Criteria priorityCrit = new ColumnCriteria(TaskFieldConstants.PRIORITY_FIELD, expectedResult.getPriority()
				.getValue());
		Criteria statusCrit = new ColumnCriteria(TaskFieldConstants.STATUS_FIELD, expectedResult.getStatus().getValue());
		Criteria severityCrit = new ColumnCriteria(TaskFieldConstants.SEVERITY_FIELD, expectedResult.getSeverity()
				.getValue());
		Criteria resolutionCrit = new ColumnCriteria(TaskFieldConstants.RESOLUTION_FIELD, expectedResult
				.getResolution().getValue());
		Criteria milestoneCrit = new ColumnCriteria(TaskFieldConstants.MILESTONE_FIELD, expectedResult.getMilestone()
				.getValue());
		Criteria productCrit = new ColumnCriteria(TaskFieldConstants.PRODUCT_FIELD, expectedResult.getProduct().getId());
		Criteria componentCrit = new ColumnCriteria(TaskFieldConstants.COMPONENT_FIELD, expectedResult.getComponent()
				.getId());
		NaryCriteria andCrit = new NaryCriteria(Operator.AND, summaryCrit, priorityCrit, statusCrit, severityCrit,
				resolutionCrit, milestoneCrit, productCrit, componentCrit, descriptionCrit);
		NaryCriteria orCrit = new NaryCriteria(Operator.OR, summaryCrit, priorityCrit, statusCrit, severityCrit,
				resolutionCrit, milestoneCrit, productCrit, componentCrit, descriptionCrit);

		assertNoResults(summaryCrit);
		assertNoResults(descriptionCrit);
		assertNoResults(statusCrit);
		assertNoResults(priorityCrit);
		assertNoResults(severityCrit);
		assertNoResults(resolutionCrit);
		assertNoResults(milestoneCrit);
		assertNoResults(productCrit);
		assertNoResults(componentCrit);
		assertNoResults(andCrit);
		assertNoResults(orCrit);

		// Now create the result
		expectedResult = taskService.createTask(expectedResult);

		assertSingleResult(expectedResult, summaryCrit);
		assertSingleResult(expectedResult, descriptionCrit);
		assertSingleResult(expectedResult, statusCrit);
		assertSingleResult(expectedResult, priorityCrit);
		assertSingleResult(expectedResult, severityCrit);
		assertSingleResult(expectedResult, resolutionCrit);
		assertSingleResult(expectedResult, milestoneCrit);
		assertSingleResult(expectedResult, productCrit);
		assertSingleResult(expectedResult, componentCrit);
		assertSingleResult(expectedResult, andCrit);
		assertSingleResult(expectedResult, orCrit);

		Criteria neverMatchCrit = new ColumnCriteria(TaskFieldConstants.PRIORITY_FIELD, "NOSUCHPRIOIRITY");
		NaryCriteria neverMatchAnd = new NaryCriteria(Operator.AND, priorityCrit, statusCrit, severityCrit,
				resolutionCrit, milestoneCrit, productCrit, componentCrit, neverMatchCrit);
		assertNoResults(neverMatchAnd);

		NaryCriteria stillMatchOr = new NaryCriteria(Operator.OR, priorityCrit, statusCrit, severityCrit,
				resolutionCrit, milestoneCrit, productCrit, componentCrit, neverMatchCrit);
		assertSingleResult(expectedResult, stillMatchOr);
	}

	private void assertSingleResult(com.tasktop.c2c.server.tasks.domain.Task expectedResult, Criteria crit) {
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult = taskService.findTasksWithCriteria(crit,
				null);
		assertNotNull(queryResult);
		assertEquals((Integer) 1, queryResult.getTotalResultSize());
		assertEquals(1, queryResult.getResultPage().size());
		assertEquals(expectedResult, queryResult.getResultPage().get(0));
	}

	private void assertNoResults(Criteria crit) {
		assertResultCount(crit, 0);
	}

	private void assertResultCount(Criteria crit, int count) {
		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> queryResult;
		queryResult = taskService.findTasksWithCriteria(crit, null);
		assertNotNull(queryResult);
		assertEquals("Expected exactly " + count + " results", (Integer) count, queryResult.getTotalResultSize());
		assertEquals("Expected exactly " + count + " results", count, queryResult.getResultPage().size());
	}

	@Test
	public void testFindTasksByWatcherMultiple() throws Exception {
		// 709: can't search for multiple people at once
		// search with two watchers
		removeTasks();

		Product product = MockProductFactory.create(entityManager);
		Profile profile = MockProfileFactory.create(entityManager);
		Profile profile1 = MockProfileFactory.create(entityManager);
		Profile profile2 = MockProfileFactory.create(entityManager);
		Task internalTask = MockTaskFactory.create(entityManager, product, profile);
		entityManager.flush();
		entityManager.persist(internalTask.addCc(profile1));
		entityManager.persist(internalTask.addCc(profile2));

		entityManager.flush();

		ColumnCriteria criteria1 = new ColumnCriteria(TaskFieldConstants.WATCHER_FIELD,
				Criteria.Operator.STRING_CONTAINS, profile1.getLoginName());
		ColumnCriteria criteria2 = new ColumnCriteria(TaskFieldConstants.WATCHER_FIELD,
				Criteria.Operator.STRING_CONTAINS, profile2.getLoginName());

		Criteria criteria = new NaryCriteria(Operator.OR, criteria1, criteria2);

		assertResultCount(criteria, 1);

		assertNoResults(new NaryCriteria(Operator.NOT, criteria));

	}

	@Test
	public void testCriteriaQueryWithAssigneeReporterAndWatcher() throws Exception {
		removeTasks();
		com.tasktop.c2c.server.tasks.domain.Task expectedResult = getMockTask();

		ColumnCriteria exactAssigneeCrit = new ColumnCriteria(TaskFieldConstants.ASSIGNEE_FIELD, getMockTask()
				.getAssignee().getLoginName());

		ColumnCriteria approxAssigneeCrit = new ColumnCriteria(TaskFieldConstants.ASSIGNEE_FIELD,
				Criteria.Operator.STRING_CONTAINS, getMockTask().getAssignee().getLoginName().substring(2, 5));

		ColumnCriteria exactReporterCrit = new ColumnCriteria(TaskFieldConstants.REPORTER_FIELD,
				currentUser.getLoginName());

		ColumnCriteria approxReporterCrit = new ColumnCriteria(TaskFieldConstants.REPORTER_FIELD,
				Criteria.Operator.STRING_CONTAINS, currentUser.getLoginName().substring(2, 5));

		ColumnCriteria exactWatcherCrit = new ColumnCriteria(TaskFieldConstants.WATCHER_FIELD, getMockTask()
				.getAssignee().getLoginName());

		ColumnCriteria approxWatcherCrit = new ColumnCriteria(TaskFieldConstants.WATCHER_FIELD,
				Criteria.Operator.STRING_CONTAINS, getMockTask().getAssignee().getLoginName().substring(2, 5));

		assertNoResults(exactAssigneeCrit);
		assertNoResults(approxAssigneeCrit);
		assertNoResults(exactReporterCrit);
		assertNoResults(approxReporterCrit);
		assertNoResults(exactWatcherCrit);
		assertNoResults(approxWatcherCrit);

		expectedResult.setWatchers(Arrays.asList(expectedResult.getAssignee()));

		expectedResult = taskService.createTask(expectedResult);
		assertSingleResult(expectedResult, exactAssigneeCrit);
		assertSingleResult(expectedResult, approxAssigneeCrit);
		assertSingleResult(expectedResult, exactReporterCrit);
		assertSingleResult(expectedResult, approxReporterCrit);
		assertSingleResult(expectedResult, exactWatcherCrit);
		assertSingleResult(expectedResult, approxWatcherCrit);
	}

	// Think this is the shape the connector will generate.
	@Test
	public void testOrOfAndCriteria() throws Exception {
		RepositoryConfiguration repo = getRepositoryConfiguration();

		Criteria statusCriteria = new NaryCriteria(Operator.OR, new ColumnCriteria(TaskFieldConstants.STATUS_FIELD,
				repo.getStatuses().get(0).getValue()), new ColumnCriteria(TaskFieldConstants.STATUS_FIELD, repo
				.getStatuses().get(0).getValue()));

		Criteria severityCriteria = new NaryCriteria(Operator.OR, new ColumnCriteria(TaskFieldConstants.SEVERITY_FIELD,
				repo.getSeverities().get(0).getValue()), new ColumnCriteria(TaskFieldConstants.SEVERITY_FIELD, repo
				.getSeverities().get(0).getValue()));

		Criteria bigAnd = new NaryCriteria(Operator.AND, statusCriteria, severityCriteria);

		assertNoResults(bigAnd);
	}

	@Test
	public void testGetRecentActivity() throws Exception {

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		List<TaskActivity> activity = taskService.getRecentActivity(new Region(0, 10));
		int activityCount = 1;
		assertActivityForTask(activity, created, activityCount);

		// Update
		com.tasktop.c2c.server.tasks.domain.Task toUpdate = created;
		toUpdate.setShortDescription("New short description");
		taskService.updateTask(toUpdate);

		activity = taskService.getRecentActivity(new Region(0, 10));
		activityCount += 1;
		assertActivityForTask(activity, created, 2);
	}

	private void assertActivityForTask(List<TaskActivity> activity, com.tasktop.c2c.server.tasks.domain.Task task,
			int count) {
		int actual = 0;
		for (TaskActivity a : activity) {
			if (a.getTask().equals(task)) {
				++actual;
			}
		}
		assertEquals(count, actual);
	}

	@Test
	public void testGetActivitySinceDate() throws Exception {
		List<TaskActivity> activity = taskService.getRecentActivity(null);

		// Constructing this value is very complex, so to start with we just let the service method do it since it would
		// be a series of complicated EntityManager queries to get the right number.

		Thread.sleep(1200L);
		final Date referenceDate = new Date();
		Thread.sleep(1200L);

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		int newActivityCount = 1;

		// Update
		com.tasktop.c2c.server.tasks.domain.Task toUpdate = created;
		toUpdate.setShortDescription("New short description");
		taskService.updateTask(toUpdate);
		newActivityCount += 1;

		List<TaskActivity> activitySinceDate = taskService.listActivity(referenceDate);

		assertEquals(newActivityCount, activitySinceDate.size());
		for (TaskActivity a : activitySinceDate) {
			assertTrue(a.getActivityDate().compareTo(referenceDate) >= 0);
		}

		activity = taskService.getRecentActivity(null);
		assertActivityForTask(activity, created, newActivityCount);
	}

	@Test
	public void testGetTaskActivity() throws Exception {

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();

		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);
		com.tasktop.c2c.server.tasks.domain.Task toUpdate = created;
		toUpdate.setShortDescription("New short description");
		toUpdate.setDeadline(new Date(System.currentTimeMillis() + (1000L * 60L * 60L * 24L * 10L)));
		taskService.updateTask(toUpdate);

		List<TaskActivity> taskActivity = taskService.listTaskActivity(created.getId());
		assertEquals(2, taskActivity.size());
		TaskActivity createdActivity = taskActivity.get(1);
		assertEquals(TaskActivity.Type.CREATED, createdActivity.getActivityType());
		assertNotNull(createdActivity.getAuthor());
		assertNotNull(createdActivity.getTask());
		assertEquals(created.getId(), createdActivity.getTask().getId());
		assertEquals(toUpdate.getShortDescription(), createdActivity.getTask().getShortDescription());
		TaskActivity updatedActivity = taskActivity.get(0);
		assertEquals(TaskActivity.Type.UPDATED, updatedActivity.getActivityType());
		assertNotNull(createdActivity.getAuthor());
		assertEquals(2, updatedActivity.getFieldUpdates().size());
		Map<String, FieldUpdate> updateByFieldName = new HashMap<String, TaskActivity.FieldUpdate>();
		for (FieldUpdate fieldUpdate : updatedActivity.getFieldUpdates()) {
			updateByFieldName.put(fieldUpdate.getFieldName(), fieldUpdate);
		}
		assertContainsKey(updateByFieldName, "deadline");
		assertContainsKey(updateByFieldName, "short_desc");
	}

	private void assertContainsKey(Map<?, ?> map, Object key) {
		if (!map.containsKey(key)) {
			fail("Expected to find key " + key + " in map keys " + map.keySet());
		}
	}

	@Test
	public void testGetTaskActivity_CustomFields() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		FieldDescriptor descriptor = new FieldDescriptor();
		descriptor.setAvailableForNewTasks(true);
		descriptor.setDescription("TestA");
		descriptor.setFieldType(FieldType.SINGLE_SELECT);
		descriptor.setName("testA" + System.currentTimeMillis());
		descriptor.setValueStrings(Arrays.asList("one", "two", "three"));

		createdFields.add(descriptor);

		customFieldService.createCustomField(descriptor);

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		assertNotNull(created.getCustomFields());
		assertTrue(!created.getCustomFields().isEmpty());
		assertEquals("---", created.getCustomFields().get(descriptor.getName()));

		created.getCustomFields().put(descriptor.getName(), "one");
		com.tasktop.c2c.server.tasks.domain.Task updated = taskService.updateTask(created);

		assertNotNull(updated.getCustomFields());
		assertEquals("one", updated.getCustomFields().get(descriptor.getName()));

		List<TaskActivity> taskActivity = taskService.listTaskActivity(created.getId());

		assertEquals(2, taskActivity.size());
		TaskActivity createdActivity = taskActivity.get(1);
		assertEquals(TaskActivity.Type.CREATED, createdActivity.getActivityType());
		assertNotNull(createdActivity.getAuthor());
		TaskActivity updatedActivity = taskActivity.get(0);
		assertEquals(TaskActivity.Type.UPDATED, updatedActivity.getActivityType());

		assertEquals(1, updatedActivity.getFieldUpdates().size());
		FieldUpdate fieldUpdate = updatedActivity.getFieldUpdates().get(0);
		assertEquals(descriptor.getName(), fieldUpdate.getFieldName());
		assertEquals("one", fieldUpdate.getNewValue());
		assertEquals("---", fieldUpdate.getOldValue());
	}

	@Test
	public void testGetProductActivity() throws Exception {

		com.tasktop.c2c.server.tasks.domain.Task task = taskService.createTask(getMockTask());

		// Update
		task.setProduct(getCreatedMockDomainProduct());
		task.setComponent(getMockComponent());
		task = taskService.updateTask(task);

		List<TaskActivity> updates = getUpdates(taskService.getRecentActivity(null));
		assertEquals(1, updates.size());
		assertEquals(2, updates.get(0).getFieldUpdates().size());

	}

	@Test
	public void testGetDepActivity() throws Exception {

		com.tasktop.c2c.server.tasks.domain.Task task = taskService.createTask(getMockTask());
		com.tasktop.c2c.server.tasks.domain.Task parent = taskService.createTask(getMockTask());

		// Update
		task.setParentTask(parent);
		task = taskService.updateTask(task);

		List<TaskActivity> updates = getUpdates(taskService.getRecentActivity(null));
		assertEquals(1, updates.size());
		assertEquals(1, updates.get(0).getFieldUpdates().size());
		assertEquals("dependson", updates.get(0).getFieldUpdates().get(0).getFieldName());
		assertEquals("" + parent.getId(), updates.get(0).getFieldUpdates().get(0).getNewValue());
		assertEquals("", updates.get(0).getFieldUpdates().get(0).getOldValue());

		Thread.sleep(MINIMUM_DATE_RESOLUTION);
		task.setParentTask(null);
		task = taskService.updateTask(task);

		updates = getUpdates(taskService.getRecentActivity(null));
		assertEquals(2, updates.size());
		assertEquals(1, updates.get(1).getFieldUpdates().size());
		assertEquals("dependson", updates.get(1).getFieldUpdates().get(0).getFieldName());
		assertEquals("" + parent.getId(), updates.get(0).getFieldUpdates().get(0).getOldValue());
		assertEquals("", updates.get(0).getFieldUpdates().get(0).getNewValue());

	}

	@Test
	public void testGetBlocksActivity() throws Exception {

		com.tasktop.c2c.server.tasks.domain.Task task = taskService.createTask(getMockTask());
		com.tasktop.c2c.server.tasks.domain.Task subTask = taskService.createTask(getMockTask());

		// Update
		task.setSubTasks(Arrays.asList(subTask));
		task = taskService.updateTask(task);

		List<TaskActivity> updates = getUpdates(taskService.getRecentActivity(null));
		assertEquals(1, updates.size());
		assertEquals(1, updates.get(0).getFieldUpdates().size());
		assertEquals("blocked", updates.get(0).getFieldUpdates().get(0).getFieldName());
		assertEquals("" + subTask.getId(), updates.get(0).getFieldUpdates().get(0).getNewValue());
		assertEquals("", updates.get(0).getFieldUpdates().get(0).getOldValue());

		Thread.sleep(MINIMUM_DATE_RESOLUTION);
		task.setSubTasks(null);
		task = taskService.updateTask(task);

		updates = getUpdates(taskService.getRecentActivity(null));
		assertEquals(2, updates.size());
		assertEquals(1, updates.get(1).getFieldUpdates().size());
		assertEquals("blocked", updates.get(1).getFieldUpdates().get(0).getFieldName());
		assertEquals("" + subTask.getId(), updates.get(0).getFieldUpdates().get(0).getOldValue());
		assertEquals("", updates.get(0).getFieldUpdates().get(0).getNewValue());

	}

	private List<TaskActivity> getUpdates(List<TaskActivity> activity) {
		List<TaskActivity> result = new ArrayList<TaskActivity>();
		for (TaskActivity a : activity) {
			if (a.getActivityType().equals(Type.UPDATED)) {
				result.add(a);
			}
		}
		return result;
	}

	// Task-897
	@Test
	public void testUpdateWithCommentDateSet() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		com.tasktop.c2c.server.tasks.domain.Task task = getMockTask();
		task.setDescription("Desc");
		task = taskService.createTask(task);
		entityManager.flush();
		Comment c = new Comment();
		c.setCommentText("Comment");
		c.setCreationDate(new Date(task.getCreationDate().getTime() - 20000l));
		task.setComments(Arrays.asList(c));
		task = taskService.updateTask(task);

		Assert.assertEquals("Desc", task.getDescription());
		Assert.assertEquals("Comment", task.getComments().get(0).getCommentText());
		Assert.assertFalse(task.getCreationDate().after(task.getComments().get(0).getCreationDate()));
	}

	@Test
	public void testCreateProduct() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Product p = new com.tasktop.c2c.server.tasks.domain.Product();
		p.setName("Product");
		p.setDescription("Description");

		com.tasktop.c2c.server.tasks.domain.Product createdProduct = taskService.createProduct(p);
		assertNotNull(createdProduct.getId());
		assertEquals(p.getName(), createdProduct.getName());
		assertEquals(p.getDescription(), createdProduct.getDescription());
		assertEquals(true, createdProduct.getIsActive());
		assertEquals(Collections.EMPTY_LIST, createdProduct.getComponents());
		assertNotNull(createdProduct.getMilestones());
		assertEquals(1, createdProduct.getMilestones().size());
		assertEquals("---", createdProduct.getDefaultMilestone().getValue());
	}

	@Test
	public void testRetrieveProduct() throws Exception {
		Product mockProduct = MockProductFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);

		com.tasktop.c2c.server.tasks.domain.Product product = taskService.retrieveProduct(mockProduct.getId()
				.intValue());
		assertEquals(mockProduct.getId().intValue(), product.getId().intValue());
		assertEquals(mockProduct.getName(), product.getName());
	}

	@Test
	public void testRetrieveAllProducts() {
		int initialNumProducts = count(Product.class);
		int numProducts = 5;
		MockProductFactory.create(entityManager, numProducts);
		entityManager.flush();

		List<com.tasktop.c2c.server.tasks.domain.Product> productsAfter = taskService.listAllProducts();
		assertEquals(initialNumProducts + numProducts, productsAfter.size());
	}

	@Test
	public void testUpdateProduct() throws Exception {
		Product mockProduct = MockProductFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);
		com.tasktop.c2c.server.tasks.domain.Product product = taskService.retrieveProduct(mockProduct.getId()
				.intValue());

		product.setName(RandomStringUtils.randomAlphanumeric(24));
		product.setDescription(RandomStringUtils.randomAlphanumeric(1024));
		product.setIsActive(false);
		com.tasktop.c2c.server.tasks.domain.Product updatedProduct = taskService.updateProduct(product);

		assertEquals(product.getName(), updatedProduct.getName());
		assertEquals(updatedProduct.getDescription(), product.getDescription());
		assertEquals(updatedProduct.getIsActive(), product.getIsActive());
		assertEquals(updatedProduct.getDefaultMilestone(), product.getDefaultMilestone());
	}

	// Test case for task 976
	@Test(expected = ConcurrentUpdateException.class)
	public void testDependencyUpdateConflict() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task parent = taskService.createTask(getMockTask("parent"));
		com.tasktop.c2c.server.tasks.domain.Task child = taskService.createTask(getMockTask("child"));

		// We have a forced granularity of 1 second on our timestamp optimistic locking, due to existing schema
		// limitations - as a result, we need to inject a delay slightly greater than 1s to ensure that our update is
		// correctly detected.
		Thread.sleep(1001);

		parent.setSubTasks(Arrays.asList(child));
		taskService.updateTask(parent);

		child.setDescription("All I changed is description");

		child = taskService.updateTask(child);

		Assert.assertTrue("Mid-air collision resulted in overwriting of data.", child.getParentTask() != null);
	}

	// Test case for task 976
	@Test(expected = ConcurrentUpdateException.class)
	public void testWithDependencyUpdateConflictFromCreate() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task child = taskService.createTask(getMockTask("child"));

		com.tasktop.c2c.server.tasks.domain.Task parent = getMockTask("parent");
		parent.setSubTasks(Arrays.asList(child));

		// We have a forced granularity of 1 second on our timestamp optimistic locking, due to existing schema
		// limitations - as a result, we need to inject a delay slightly greater than 1s to ensure that our update is
		// correctly detected.
		Thread.sleep(1001);

		taskService.createTask(parent);

		child.setDescription("All I changed is description");

		child = taskService.updateTask(child);

		Assert.assertTrue("Mid-air collision resulted in overwriting of data.", child.getParentTask() != null);
	}

	@Test
	public void testCreateComponentWithInitialOwner() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Component testComponent = new com.tasktop.c2c.server.tasks.domain.Component();
		testComponent.setName(RandomStringUtils.randomAlphanumeric(24));
		testComponent.setDescription(RandomStringUtils.randomAlphanumeric(1024));
		testComponent.setInitialOwner(getCreatedMockTaskUserProfile());
		testComponent.setProduct(getCreatedMockDomainProduct());

		com.tasktop.c2c.server.tasks.domain.Component createdComponent = taskService.createComponent(testComponent);
		assertNotNull(createdComponent.getId());
		assertEquals(testComponent.getName(), createdComponent.getName());
		assertEquals(testComponent.getDescription(), createdComponent.getDescription());
		assertEquals(testComponent.getInitialOwner().getId(), createdComponent.getInitialOwner().getId());
		assertEquals(testComponent.getProduct().getId(), createdComponent.getProduct().getId());
	}

	/**
	 * Should successfully create a component with no intitial owner (task 1727)
	 */
	@Test
	public void testCreateComponentNoInitialOwner() throws Throwable {
		com.tasktop.c2c.server.tasks.domain.Component testComponent = new com.tasktop.c2c.server.tasks.domain.Component();
		testComponent.setName(RandomStringUtils.randomAlphanumeric(24));
		testComponent.setDescription(RandomStringUtils.randomAlphanumeric(1024));
		testComponent.setProduct(getCreatedMockDomainProduct());

		com.tasktop.c2c.server.tasks.domain.Component createComponent = taskService.createComponent(testComponent);
		assertNull(createComponent.getInitialOwner());
	}

	private Product getCreatedMockProduct() {
		Product p = MockProductFactory.create(entityManager);
		entityManager.flush();
		assertNotNull(p.getId());
		return p;
	}

	private com.tasktop.c2c.server.tasks.domain.Product getCreatedMockDomainProduct() {
		com.tasktop.c2c.server.tasks.domain.Product retProd = new com.tasktop.c2c.server.tasks.domain.Product();

		// Perform our conversion now.
		productConverter.copy(retProd, getCreatedMockProduct(), domainConverter, new DomainConversionContext(
				entityManager));

		return retProd;
	}

	@Test
	public void testRetrieveComponent() throws Exception {
		Component mockComponent = MockComponentFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockComponent);

		com.tasktop.c2c.server.tasks.domain.Component domainComponent = taskService.retrieveComponent(Integer
				.valueOf(mockComponent.getId()));
		assertEquals(mockComponent.getId().intValue(), domainComponent.getId().intValue());
		assertEquals(mockComponent.getName(), domainComponent.getName());
	}

	@Test(expected = EntityNotFoundException.class)
	public void testRetrieveComponentForBadID() throws Throwable {

		try {
			// This should blow up, since the ID of a Component is a short - an
			// ID
			// of Integer.MAX_VALUE will be impossible in the database.
			assertNotNull(taskService.retrieveComponent(new Integer(Integer.MAX_VALUE)));
		} catch (WrappedCheckedException wce) {
			// If this was wrapped due to the REST interface, throw the original
			// exception
			throw wce.getCause();
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRetrieveComponentForNullID() throws Exception {

		// This should blow up
		assertNotNull(taskService.retrieveComponent(null));
	}

	@Test
	public void testRetrieveAllComponents() {

		// First, figure out our initial number of components before this test.
		int origNumComponents = count(Component.class);

		int numComponentsToCreate = 5;
		MockComponentFactory.create(entityManager, numComponentsToCreate);
		entityManager.flush();

		List<com.tasktop.c2c.server.tasks.domain.Component> componentsAfter = taskService.listAllComponents();
		assertEquals(origNumComponents + numComponentsToCreate, componentsAfter.size());
	}

	@Test
	public void testUpdateComponent() throws Exception {

		// Save a random component before we start our test.
		Component mockComponent = MockComponentFactory.create(entityManager);
		entityManager.flush();

		// Grab our domain object through the task service.
		com.tasktop.c2c.server.tasks.domain.Component origComponent = taskService.retrieveComponent(Integer
				.valueOf(mockComponent.getId()));

		// Sanity check, to make sure we had a component to start with.
		assertNotNull(origComponent);

		// Perform a couple of updates, and push them to the Task Service
		origComponent.setName(RandomStringUtils.randomAlphanumeric(24));
		origComponent.setDescription(RandomStringUtils.randomAlphanumeric(1024));
		com.tasktop.c2c.server.tasks.domain.Component updatedComponent = taskService.updateComponent(origComponent);

		assertEquals(origComponent.getName(), updatedComponent.getName());
		assertEquals(updatedComponent.getDescription(), origComponent.getDescription());
	}

	@Test(expected = ValidationException.class)
	public void testUpdateComponentNoName() throws Throwable {

		// Save a random component before we start our test.
		Component mockComponent = MockComponentFactory.create(entityManager);
		entityManager.flush();

		// Grab our domain object through the task service.
		com.tasktop.c2c.server.tasks.domain.Component origComponent = taskService.retrieveComponent(Integer
				.valueOf(mockComponent.getId()));

		// Sanity check, to make sure we had a component to start with.
		assertNotNull(origComponent);

		// Perform a couple of updates, and push them to the Task Service
		origComponent.setName(null);
		origComponent.setDescription(RandomStringUtils.randomAlphanumeric(1024));

		// This should blow up.
		taskService.updateComponent(origComponent);
	}

	// Simple key generation method, mimics logic in TaskServiceBean.combineValidationExceptions()
	private String generateValidationKey(String fieldName, AbstractDomainObject domainObj) {
		return String.format("field.required.%s[%s|%s]", fieldName, domainObj.getClass().getSimpleName(),
				domainObj.getId());
	}

	@Test
	public void testUpdateProductTree_ValidationErrors() throws Exception {
		Product mockProduct = MockProductFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);
		com.tasktop.c2c.server.tasks.domain.Product product = taskService.retrieveProduct(mockProduct.getId()
				.intValue());

		product.setName(RandomStringUtils.randomAlphanumeric(24));
		product.setDescription(RandomStringUtils.randomAlphanumeric(1024));
		product.setIsActive(false);

		// Create a list to keep track of all of our validation errors - we'll use then to ensure that all of our
		// validation messages were present, and were correctly calculated.
		List<String> validationList = new ArrayList<String>();

		// Now, make some invalid data.
		product.setName(null);
		validationList.add(generateValidationKey("name", product));

		for (com.tasktop.c2c.server.tasks.domain.Component curComponent : product.getComponents()) {
			curComponent.setName(null);
			validationList.add(generateValidationKey("name", curComponent));
		}

		try {
			// This should blow up with an exception.
			taskService.updateProductTree(product);
		} catch (ValidationException ve) {

			ve.getErrors().getAllErrors();

			// We should have an error for every product, and 2 for every component
			assertEquals(1 + product.getComponents().size(), ve.getErrors().getErrorCount());

			for (ObjectError curErr : ve.getErrors().getAllErrors()) {
				// Now, loop through our errors and make sure that the validation codes follow the correct format
				if (!validationList.contains(curErr.getCode())) {
					fail("The key " + curErr.getCode() + " was found, but was not expected from the validation system!");
				}

				validationList.remove(curErr.getCode());
			}

			if (validationList.size() > 0) {
				fail("The following validation errors were not caught by the validator: " + validationList);
			}
		}
	}

	@Test
	public void testUpdateProductTree() throws Exception {
		Product mockProduct = MockProductFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);
		com.tasktop.c2c.server.tasks.domain.Product product = taskService.retrieveProduct(mockProduct.getId()
				.intValue());
		String appender = RandomStringUtils.randomAlphanumeric(12);

		product.setName(RandomStringUtils.randomAlphanumeric(24));
		product.setDescription(RandomStringUtils.randomAlphanumeric(1024));
		product.setIsActive(false);

		for (com.tasktop.c2c.server.tasks.domain.Component curComponent : product.getComponents()) {
			curComponent.setName(curComponent.getName() + appender);
		}

		com.tasktop.c2c.server.tasks.domain.Product updatedProduct = taskService.updateProductTree(product);

		assertEquals(product.getName(), updatedProduct.getName());
		assertEquals(updatedProduct.getDescription(), product.getDescription());
		assertEquals(updatedProduct.getIsActive(), product.getIsActive());
		assertEquals(updatedProduct.getDefaultMilestone(), product.getDefaultMilestone());

		for (com.tasktop.c2c.server.tasks.domain.Component curComponent : product.getComponents()) {
			assertTrue(curComponent.getName().endsWith(appender));
		}
	}

	@Ignore
	@Test
	public void testUpdateProductTree_task2891() throws Exception {
		Product mockProduct = MockProductFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);
		com.tasktop.c2c.server.tasks.domain.Product product = taskService.retrieveProduct(mockProduct.getId()
				.intValue());

		product.setName(RandomStringUtils.randomAlphanumeric(24));
		product.setDescription(RandomStringUtils.randomAlphanumeric(1024));
		product.setIsActive(false);

		String lastComponentName = null;
		for (com.tasktop.c2c.server.tasks.domain.Component curComponent : product.getComponents()) {
			String thisName = curComponent.getName();
			if (lastComponentName != null) {
				curComponent.setName(lastComponentName);
			}
			lastComponentName = thisName;
		}
		product.getComponents().get(0).setName(lastComponentName);

		com.tasktop.c2c.server.tasks.domain.Product updatedProduct = taskService.updateProductTree(product);
		Assert.assertEquals(lastComponentName, updatedProduct.getComponents().get(0).getName());
	}

	@Test
	public void testUpdateMilestoneNameUpdatesAssosiatedTasks_task2434() throws Exception {
		Product mockProduct = MockProductFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);
		com.tasktop.c2c.server.tasks.domain.Product product = taskService.retrieveProduct(mockProduct.getId()
				.intValue());
		com.tasktop.c2c.server.tasks.domain.Milestone newMileStone = new com.tasktop.c2c.server.tasks.domain.Milestone();
		String origMilestoneName = "origMilestone";
		newMileStone.setValue(origMilestoneName);
		newMileStone.setSortkey((short) 2);
		product.getMilestones().add(newMileStone);

		product = taskService.updateProductTree(product);

		assertEquals(2, product.getMilestones().size());
		assertEquals(origMilestoneName, product.getMilestones().get(1).getValue());

		com.tasktop.c2c.server.tasks.domain.Task newTask = getMockTask();
		newTask.setProduct(product);
		newTask.setComponent(product.getComponents().get(0));
		newTask.setMilestone(product.getMilestones().get(1));

		newTask = taskService.createTask(newTask);
		assertNotNull(newTask.getMilestone());
		assertEquals(origMilestoneName, newTask.getMilestone().getValue());

		String newMilestoneName = "newMilestone";
		product.getMilestones().get(1).setValue(newMilestoneName);

		product = taskService.updateProductTree(product);
		assertEquals(2, product.getMilestones().size());
		assertEquals(newMilestoneName, product.getMilestones().get(1).getValue());

		newTask = taskService.retrieveTask(newTask.getId());
		assertNotNull(newTask.getMilestone());
		assertEquals(newMilestoneName, newTask.getMilestone().getValue());
	}

	@Test
	public void testDeleteMilestone() throws Exception {
		// First, create a product tree and a few milestones.
		int numMilestones = 10;
		Product mockProduct = MockProductFactory.create(entityManager);
		MockMilestoneFactory.createWithProduct(entityManager, numMilestones, mockProduct);
		entityManager.flush();
		entityManager.refresh(mockProduct);

		// Confirm that we have the number of milestones that we expect, to start from a known point.
		assertEquals(numMilestones + 1, mockProduct.getMilestones().size());

		// Now, iterate through each of the non-default ones and delete them - these should all succeed.
		for (Milestone curMilestone : mockProduct.getMilestones()) {
			if (mockProduct.getDefaultmilestone().equals(curMilestone.getValue())) {
				// If this is our default milestone, skip it.
				continue;
			}

			taskService.deleteMilestone(curMilestone.getId());
		}

		// Refresh our product now.
		entityManager.refresh(mockProduct);

		// Now, make sure that we only have 1 milestone left, and that it's our default.
		assertEquals(1, mockProduct.getMilestones().size());
		assertEquals(mockProduct.getDefaultmilestone(), mockProduct.getMilestones().get(0).getValue());
	}

	@Test(expected = ValidationException.class)
	public void testDeleteDefaultMilestone() throws Exception {
		// First, create a product tree - this will also create a default milestone.
		Product mockProduct = MockProductFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);

		// Confirm that we have the number of milestones that we expect, to start from a known point - also make sure
		// that our one milestone is our default.
		assertEquals(1, mockProduct.getMilestones().size());
		assertEquals(mockProduct.getDefaultmilestone(), mockProduct.getMilestones().get(0).getValue());

		// Now, try to delete that milestone - this should blow up.
		taskService.deleteMilestone(mockProduct.getMilestones().get(0).getId());
	}

	@Test(expected = ValidationException.class)
	public void testDeleteMilestoneWithTask() throws Exception {
		// First, create a product tree and a non-default milestone.
		Product mockProduct = MockProductFactory.create(entityManager);
		MockMilestoneFactory.createWithProduct(entityManager, 1, mockProduct);
		entityManager.flush();
		entityManager.refresh(mockProduct);

		// Confirm that we have the number of milestones that we expect, to start from a known point - also make sure
		// that our one milestone is our default.
		assertEquals(2, mockProduct.getMilestones().size());

		Milestone targetMilestone = null;
		for (Milestone curMilestone : mockProduct.getMilestones()) {
			if (!mockProduct.getDefaultmilestone().equals(curMilestone.getValue())) {
				// If this is our default milestone, skip it.
				targetMilestone = curMilestone;
			}
		}

		assertNotNull(targetMilestone);
		assertFalse(targetMilestone.getValue().equals(mockProduct.getDefaultmilestone()));

		// Create a new task, and make sure it's got that milestone.
		Profile newUser = MockProfileFactory.create(entityManager);
		Task curTask = MockTaskFactory.create(entityManager, mockProduct, newUser);
		curTask.setTargetMilestone(targetMilestone.getValue());
		entityManager.flush();

		// Now, try to delete that milestone - this should blow up.
		taskService.deleteMilestone(targetMilestone.getId());
	}

	@Test
	public void testDeleteComponent() throws Exception {
		// First, create a product tree
		Product mockProduct = MockProductFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);

		// Confirm that we have at least one component, so we're starting from a known good point.
		assertTrue(mockProduct.getComponents().size() > 0);

		// Now, iterate through each of the components and delete them - these should all succeed.
		for (Component curMilestone : mockProduct.getComponents()) {
			taskService.deleteComponent(curMilestone.getId().intValue());
		}

		// Refresh our product now.
		entityManager.refresh(mockProduct);

		// Now, make sure that we have no components left.
		assertEquals(0, mockProduct.getComponents().size());
	}

	@Test(expected = ValidationException.class)
	public void testDeleteComponentWithTask() throws Exception {
		// First, create a product tree
		Product mockProduct = MockProductFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);

		// Confirm that we have at least one component, so we're starting from a known good point.
		assertTrue(mockProduct.getComponents().size() > 0);

		// Grab one of our components - any one will do.
		Component targetComponent = mockProduct.getComponents().iterator().next();

		assertNotNull(targetComponent);

		// Create a new task, and make sure it's got that milestone.
		Profile newUser = MockProfileFactory.create(entityManager);
		Task curTask = MockTaskFactory.create(entityManager, mockProduct, newUser);
		curTask.setComponent(targetComponent);
		entityManager.flush();

		// Now, try to delete that milestone - this should blow up.
		taskService.deleteComponent(targetComponent.getId().intValue());
	}

	@Test(expected = EntityNotFoundException.class)
	public void testDeleteProduct() throws Exception {
		// First, create a product tree
		Product mockProduct = MockProductFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);

		// Now, delete it.
		taskService.deleteProduct(mockProduct.getId().intValue());

		// Confirm that it's gone - this will blow up.
		taskService.retrieveProduct(mockProduct.getId().intValue());
	}

	@Test(expected = ValidationException.class)
	public void testDeleteProductWithTask() throws Exception {
		// First, create a product tree
		Product mockProduct = MockProductFactory.create(entityManager);
		entityManager.flush();
		entityManager.refresh(mockProduct);

		// Create a new task, and make sure it's got that milestone.
		Profile newUser = MockProfileFactory.create(entityManager);
		Task curTask = MockTaskFactory.create(entityManager, mockProduct, newUser);
		curTask.setProduct(mockProduct);
		entityManager.flush();

		// Now, try to delete that milestone - this should blow up.
		taskService.deleteProduct(mockProduct.getId().intValue());
	}

	@Test
	public void testHistoricalSummary() {
		removeTasks();
		int numDays = 5;
		int tasksPerDay = 2;
		int daysOpen = 10;
		long millisPerDay = 1000 * 60 * 60 * 24;
		long now = System.currentTimeMillis();
		long startDay = now - (millisPerDay * (numDays - 1));
		for (int day = 0; day < numDays; day++) {
			for (int task = 0; task < tasksPerDay; task++) {
				Task t = MockTaskFactory.create(null, getCreatedMockProduct(), getCreatedMockProfile());
				long openTime = startDay + day * millisPerDay;
				t.setCreationTs(new Date(openTime));
				long closeTime = openTime + daysOpen * millisPerDay;
				if (closeTime < now) {
					t.setDeltaTs(new Date(closeTime));
					t.setStatus("RESOLVED");
				}
				entityManager.persist(t);
			}
		}
		entityManager.flush();

		List<TaskSummary> summaries = taskService.getHistoricalSummary(numDays);
		Assert.assertEquals(numDays, summaries.size());

		Integer lastClosed = null;
		for (int i = 0; i < summaries.size(); i++) {
			TaskSummary summary = summaries.get(i);
			int open = 0;
			int closed = 0;
			for (TaskSummaryItem tsi : summary.getItems()) {
				open += tsi.getOpenCount();
				closed += tsi.getClosedCount();
			}

			if (i == 0) {
				Assert.assertEquals(0, closed);
				Assert.assertEquals(tasksPerDay, open);
			} else if (i == numDays - 1) {
				Assert.assertEquals(numDays * tasksPerDay, closed + open);
			}

			if (lastClosed != null) {
				Assert.assertTrue("closed should only grow", closed >= lastClosed);
			}

			lastClosed = closed;
		}
	}

	@Test
	public void replicateTeam() {
		Profile profile = MockProfileFactory.create(entityManager);

		Team team = new Team();

		TaskUserProfile user1 = new TaskUserProfile();
		user1.setLoginName(profile.getLoginName());
		user1.setRealname(profile.getRealname() + "changed");
		TaskUserProfile user2 = new TaskUserProfile();
		user2.setLoginName("newusernotyetcreated");
		user2.setRealname("Joe Bloe");

		team.add(user1);
		team.add(user2);

		taskService.replicateTeam(team);

		assertUserExistsWithCurrentValues(user1);
		assertUserExistsWithCurrentValues(user2);
	}

	@Test
	public void gravatarHash() throws EntityNotFoundException {
		Profile profile = MockProfileFactory.create(entityManager);

		Team team = new Team();

		String gravatarHash = UUID.randomUUID().toString().replace("-", "");

		TaskUserProfile user1 = new TaskUserProfile();
		user1.setLoginName(profile.getLoginName());
		user1.setRealname(profile.getRealname() + "changed");
		user1.setGravatarHash(gravatarHash);

		team.add(user1);

		taskService.replicateTeam(team);

		entityManager.flush();

		Product product = MockProductFactory.create(entityManager);

		Task task = MockTaskFactory.create(entityManager, product, profile);
		entityManager.flush();

		com.tasktop.c2c.server.tasks.domain.Task serviceTask = taskService.retrieveTask(task.getId());

		assertEquals(gravatarHash, serviceTask.getReporter().getGravatarHash());

		Criteria criteria = new ColumnCriteria(TaskFieldConstants.TASK_ID_FIELD, Operator.EQUALS, serviceTask.getId());

		QueryResult<com.tasktop.c2c.server.tasks.domain.Task> results = taskService.findTasksWithCriteria(criteria,
				null);
		assertEquals(1, results.getResultPage().size());
		assertEquals(gravatarHash, results.getResultPage().get(0).getReporter().getGravatarHash());
	}

	protected void assertUserExistsWithCurrentValues(TaskUserProfile user1) {
		TypedQuery<Profile> query = entityManager.createQuery(
				"select u from " + Profile.class.getSimpleName() + " u where u.loginName = :name", Profile.class)
				.setParameter("name", user1.getLoginName());
		Profile result = query.getSingleResult();

		assertEquals(user1.getLoginName(), result.getLoginName());
		assertEquals(user1.getRealname(), result.getRealname());
	}

	@Test
	public void createTask_NewKeyword() throws ValidationException, EntityNotFoundException {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask("description");
		Keyword keyword = new Keyword("tag1", "desc1");
		List<Keyword> keys = new ArrayList<Keyword>();
		keys.add(keyword);
		mockTask.setKeywords(keys);
		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);
		assertEquals(mockTask.getKeywords().size(), createdTask.getKeywords().size());
	}

	@Test
	public void updateTask_NewKeyword() throws ValidationException, EntityNotFoundException, ConcurrentUpdateException {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask("description");
		Keyword keyword = new Keyword("tag1", "desc1");
		List<Keyword> keys = new ArrayList<Keyword>();
		keys.add(keyword);
		mockTask.setKeywords(keys);
		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);
		assertEquals(mockTask.getKeywords().size(), createdTask.getKeywords().size());

		// add a second keyword and update
		Keyword keyword2 = new Keyword("tag2", "desc2");
		createdTask.getKeywords().add(keyword2);
		com.tasktop.c2c.server.tasks.domain.Task updatedTask = taskService.updateTask(createdTask);
		assertEquals(createdTask.getKeywords().size(), updatedTask.getKeywords().size());
	}

	@Test
	public void updateTask_RemoveKeyword() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask("description");
		List<Keyword> keys = new ArrayList<Keyword>();
		keys.add(new Keyword("tag1", "desc1"));
		Keyword keyword2 = new Keyword("tag2", "desc2");
		keys.add(keyword2);
		mockTask.setKeywords(keys);
		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);
		assertEquals(createdTask.getKeywords().size(), createdTask.getKeywords().size());
		for (Keyword kw : createdTask.getKeywords()) {
			assertTrue(createdTask.getKeywords().contains(kw));
		}

		// add a second keyword and update
		createdTask.getKeywords().remove(keyword2);
		com.tasktop.c2c.server.tasks.domain.Task updatedTask = taskService.updateTask(createdTask);
		// order is unimportant
		assertEquals(createdTask.getKeywords().size(), updatedTask.getKeywords().size());
		for (Keyword kw : createdTask.getKeywords()) {
			assertTrue(updatedTask.getKeywords().contains(kw));
		}
	}

	private <T extends AbstractReferenceValue> T findByName(String name, Collection<T> values) {
		for (T val : values) {
			if (val.getValue().equals(name)) {
				return val;
			}
		}
		return null;
	}

	// Task 1975
	@Test
	public void testResolveDuplicateAddsToCCOfDuplicatedTask() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task duplicate = taskService.createTask(getMockTask());
		com.tasktop.c2c.server.tasks.domain.Task duplicateed = taskService.createTask(getMockTask());

		Profile duplicatedOwner = getCreatedMockProfile();
		duplicatedOwner.setLoginName("duplicatedOwner");

		duplicate.setAssignee(ProfileConverter.copy(duplicatedOwner));

		duplicate = taskService.updateTask(duplicate);

		duplicate.setStatus(findByName("RESOLVED", getRepositoryConfiguration().getStatuses()));
		duplicate.setResolution(findByName("DUPLICATE", getRepositoryConfiguration().getResolutions()));
		duplicate.setDuplicateOf(duplicateed);

		duplicate = taskService.updateTask(duplicate);
		duplicateed = taskService.retrieveTask(duplicateed.getId());

		Assert.assertTrue("Duplicate assignee missing from watchers of duplicated",
				duplicateed.getWatchers().contains(duplicate.getAssignee()));
	}

	@Test
	public void createKeyword() throws ValidationException {
		Keyword newKeyword = new Keyword("name", "description");
		Keyword createdKeyword = taskService.createKeyword(newKeyword);
		assertEquals(newKeyword.getName(), createdKeyword.getName());
		assertEquals(newKeyword.getDescription(), createdKeyword.getDescription());
	}

	@Test
	public void updateKeyword() throws ValidationException, EntityNotFoundException {
		Keyword newKeyword = new Keyword("name", "description");
		Keyword createdKeyword = taskService.createKeyword(newKeyword);
		List<Keyword> allKeywords = taskService.listAllKeywords();
		assertTrue(allKeywords.get(allKeywords.size() - 1).equals(createdKeyword));

		createdKeyword.setName("name2");
		Keyword updateKeyword = taskService.updateKeyword(createdKeyword);
		assertTrue(updateKeyword.equals(createdKeyword));
	}

	@Test
	public void deleteKeyword() throws ValidationException, EntityNotFoundException {
		Keyword newKeyword = new Keyword("name", "description");
		Keyword createdKeyword = taskService.createKeyword(newKeyword);

		List<Keyword> allKeywords = taskService.listAllKeywords();
		int numKeywords = allKeywords.size();

		taskService.deleteKeyword(createdKeyword.getId());
		List<Keyword> afterKeywords = taskService.listAllKeywords();
		assertEquals(numKeywords - 1, afterKeywords.size());
	}

	@Test
	public void testFoundInRelease() throws ValidationException, EntityNotFoundException, ConcurrentUpdateException {
		com.tasktop.c2c.server.tasks.domain.Task task = getMockTask();
		String foundInRelease = "1.0.0#29";
		task.setFoundInRelease(foundInRelease);
		task = taskService.createTask(task);

		RepositoryConfiguration repoCtx = taskService.getRepositoryContext();
		com.tasktop.c2c.server.tasks.domain.Product updatedProduct = null;
		for (com.tasktop.c2c.server.tasks.domain.Product p : repoCtx.getProducts()) {
			if (p.equals(task.getProduct())) {
				updatedProduct = p;
				break;
			}
		}
		Assert.assertEquals(1, updatedProduct.getReleaseTags().size());
		Assert.assertEquals(foundInRelease, updatedProduct.getReleaseTags().get(0));

		task.setFoundInRelease(null);
		task = taskService.updateTask(task);

		repoCtx = taskService.getRepositoryContext();
		updatedProduct = null;
		for (com.tasktop.c2c.server.tasks.domain.Product p : repoCtx.getProducts()) {
			if (p.equals(task.getProduct())) {
				updatedProduct = p;
				break;
			}
		}
		Assert.assertEquals(1, updatedProduct.getReleaseTags().size());
		Assert.assertEquals(foundInRelease, updatedProduct.getReleaseTags().get(0));

		String foundInRelease2 = "1.0.0#30";
		task.setFoundInRelease(foundInRelease2);
		task = taskService.updateTask(task);

		repoCtx = taskService.getRepositoryContext();
		updatedProduct = null;
		for (com.tasktop.c2c.server.tasks.domain.Product p : repoCtx.getProducts()) {
			if (p.equals(task.getProduct())) {
				updatedProduct = p;
				break;
			}
		}
		Assert.assertEquals(2, updatedProduct.getReleaseTags().size());
		Assert.assertEquals(foundInRelease2, updatedProduct.getReleaseTags().get(1));

		task.setFoundInRelease(foundInRelease2);
		task = taskService.updateTask(task);

		repoCtx = taskService.getRepositoryContext();
		updatedProduct = null;
		for (com.tasktop.c2c.server.tasks.domain.Product p : repoCtx.getProducts()) {
			if (p.equals(task.getProduct())) {
				updatedProduct = p;
				break;
			}
		}
		Assert.assertEquals(2, updatedProduct.getReleaseTags().size());
		Assert.assertEquals(foundInRelease2, updatedProduct.getReleaseTags().get(1));
	}

	@Test
	public void saveQueryListCreateUpdate() throws ValidationException, EntityNotFoundException {
		Assert.assertEquals(0, taskService.getRepositoryContext().getSavedTaskQueries().size());

		SavedTaskQuery query = new SavedTaskQuery();
		query.setName("My New Query");

		try {
			taskService.createQuery(query);
			Assert.fail("Expect validation exception");
		} catch (ValidationException e) {
			ValidationAssert.assertHaveValidationError(e, "field.required.queryString");
		}

		query.setQueryString("DOES NOT PARSE");

		try {
			taskService.createQuery(query);
			Assert.fail("Expect validation exception");
		} catch (ValidationException e) {
			ValidationAssert.assertHaveValidationError(e, "invalid.queryString");
		}

		query.setQueryString("status = 'NEW'");

		query = taskService.createQuery(query);
		Assert.assertNotNull(query.getId());
		Assert.assertEquals(1, taskService.getRepositoryContext().getSavedTaskQueries().size());

		String newName = "NEWNAME";
		query.setName(newName);
		query = taskService.updateQuery(query);
		Assert.assertEquals(newName, query.getName());

		query.setName("Invalid@Name");
		try {
			taskService.updateQuery(query);
			Assert.fail("Expect validation exception");
		} catch (ValidationException e) {
			ValidationAssert.assertHaveValidationError(e, "field.validQueryName");
		}

		query = new SavedTaskQuery();
		query.setName(newName);
		query.setQueryString("status = 'NEW'");

		try {
			taskService.createQuery(query);
			Assert.fail("Expect validation exception");
		} catch (ValidationException e) {
			ValidationAssert.assertHaveValidationError(e, "query.nameUnique");
		}
	}

	// task 2728
	@Test
	public void testUpdateProductComponentActivity() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Product p = new com.tasktop.c2c.server.tasks.domain.Product();
		String newProductName = "newProduct";
		p.setName(newProductName);
		p.setDescription("Description");
		taskService.createProduct(p);

		com.tasktop.c2c.server.tasks.domain.Task toCreate = getMockTask();
		com.tasktop.c2c.server.tasks.domain.Task created = taskService.createTask(toCreate);

		com.tasktop.c2c.server.tasks.domain.Product newProduct = getRepositoryConfiguration().getProducts().get(1);
		newProduct.setName(null); // This is how the web ui sends it. Fair enough...
		Assert.assertFalse(newProduct.equals(created.getProduct()));
		created.setProduct(newProduct);
		taskService.updateTask(created);
		List<TaskActivity> activity = taskService.listTaskActivity(created.getId());
		Assert.assertEquals(2, activity.size());
		Assert.assertEquals(TaskActivity.Type.CREATED, activity.get(1).getActivityType());
		Assert.assertEquals(TaskActivity.Type.UPDATED, activity.get(0).getActivityType());
		Assert.assertEquals(1, activity.get(0).getFieldUpdates().size());
		Assert.assertEquals(newProductName, activity.get(0).getFieldUpdates().get(0).getNewValue());

	}

	@Test
	public void createTask_taskRelation() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask();
		ExternalTaskRelation relation = new ExternalTaskRelation("type", "kind", "http://example.com");
		List<ExternalTaskRelation> relations = new ArrayList<ExternalTaskRelation>();
		relations.add(relation);
		mockTask.setExternalTaskRelations(relations);
		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);
		assertEquals(mockTask.getExternalTaskRelations().size(), createdTask.getExternalTaskRelations().size());
		assertEquals(mockTask.getExternalTaskRelations().get(0), createdTask.getExternalTaskRelations().get(0));

		Integer taskId = createdTask.getId();
		com.tasktop.c2c.server.tasks.domain.Task retrievedTask = taskService.retrieveTask(taskId);
		assertEquals(1, retrievedTask.getExternalTaskRelations().size());

	}

	@Test
	public void updateTask_taskRelations() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask();
		ExternalTaskRelation relation = new ExternalTaskRelation("type", "kind", "http://abc123.com/adfasdf#1231231");
		List<ExternalTaskRelation> relations = new ArrayList<ExternalTaskRelation>();
		relations.add(relation);
		mockTask.setExternalTaskRelations(relations);
		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);

		createdTask.getExternalTaskRelations().add(
				new ExternalTaskRelation("type", "kind2", "http://example.com/otherURI"));
		com.tasktop.c2c.server.tasks.domain.Task updatedTask = taskService.updateTask(createdTask);
		assertEquals(2, updatedTask.getExternalTaskRelations().size());

		updatedTask.setExternalTaskRelations(null);
		com.tasktop.c2c.server.tasks.domain.Task updatedTask2 = taskService.updateTask(updatedTask);
		assertEquals(0, updatedTask2.getExternalTaskRelations().size());
	}

	@Test
	public void createTask_withWorkLog() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask();
		WorkLog worklog = new WorkLog();
		worklog.setProfile(ProfileConverter.copy(currentUser));
		Date now = new Date();
		worklog.setDateWorked(now);
		worklog.setHoursWorked(new BigDecimal(1.0));
		mockTask.getWorkLogs().add(worklog);
		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);

		assertEquals(1, createdTask.getWorkLogs().size());
		assertEquals(BigDecimal.ONE, createdTask.getWorkLogs().get(0).getHoursWorked());

		// the date that is stored is set by the client, not the server
		assertEquals(0, now.compareTo(createdTask.getWorkLogs().get(0).getDateWorked()));
	}

	@Test
	public void updateTask_withWorkLog() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask();
		WorkLog worklog = new WorkLog();
		worklog.setProfile(ProfileConverter.copy(currentUser));
		worklog.setDateWorked(new Date());
		worklog.setHoursWorked(new BigDecimal(1.0));
		mockTask.getWorkLogs().add(worklog);
		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);

		BigDecimal time = BigDecimal.valueOf(3.3);
		WorkLog w2 = new WorkLog();
		w2.setDateWorked(new Date());
		w2.setHoursWorked(time);
		w2.setProfile(ProfileConverter.copy(currentUser));
		createdTask.getWorkLogs().add(w2);

		com.tasktop.c2c.server.tasks.domain.Task updatedTask = taskService.updateTask(createdTask);

		assertEquals(2, updatedTask.getWorkLogs().size());
		assertEquals(time, updatedTask.getWorkLogs().get(1).getHoursWorked());
	}

	@Test
	public void updateTask_withOnlyNewWorkLog() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask();
		WorkLog worklog = new WorkLog();
		worklog.setProfile(ProfileConverter.copy(currentUser));
		worklog.setDateWorked(new Date());
		worklog.setHoursWorked(new BigDecimal(1.0));
		mockTask.getWorkLogs().add(worklog);
		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);

		BigDecimal time = BigDecimal.valueOf(4.44);
		WorkLog w2 = new WorkLog();
		w2.setDateWorked(new Date());
		w2.setHoursWorked(time);
		w2.setProfile(ProfileConverter.copy(currentUser));

		// remove all work logs, only submit the new one
		createdTask.getWorkLogs().clear();
		createdTask.getWorkLogs().add(w2);
		assertEquals(1, createdTask.getWorkLogs().size());

		Thread.sleep(1000L);
		com.tasktop.c2c.server.tasks.domain.Task updatedTask = taskService.updateTask(createdTask);

		// should still return two worklogs
		assertEquals(2, updatedTask.getWorkLogs().size());
		assertEquals(time, updatedTask.getWorkLogs().get(1).getHoursWorked());
	}

	@Test
	public void updateTask_workLogsShouldBeImmutable() throws Exception {
		BigDecimal originalHoursWorked = new BigDecimal(1.0);
		BigDecimal changedHoursWorked = new BigDecimal(2.0);

		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask();
		WorkLog worklog = new WorkLog();
		worklog.setProfile(ProfileConverter.copy(currentUser));
		worklog.setDateWorked(new Date());
		worklog.setHoursWorked(originalHoursWorked);
		mockTask.getWorkLogs().add(worklog);
		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);

		assertEquals(originalHoursWorked, createdTask.getWorkLogs().get(0).getHoursWorked());

		createdTask.getWorkLogs().get(0).setHoursWorked(changedHoursWorked);
		com.tasktop.c2c.server.tasks.domain.Task updatedTask = taskService.updateTask(createdTask);

		assertEquals(originalHoursWorked, updatedTask.getWorkLogs().get(0).getHoursWorked());
	}

	@Test
	public void updateTask_workLogsAuthoredByCurrentUserOnly() throws Exception {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask();
		WorkLog worklog = new WorkLog();

		// Not the current user. This should not validate.
		worklog.setProfile(getCreatedMockTaskUserProfile());
		worklog.setDateWorked(new Date());
		worklog.setHoursWorked(BigDecimal.valueOf(3.4));
		mockTask.getWorkLogs().add(worklog);

		Assert.assertFalse(currentUser.getLoginName().equals(mockTask.getWorkLogs().get(0).getProfile().getLoginName()));

		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);
		Assert.assertEquals(1, createdTask.getWorkLogs().size());
		Assert.assertEquals(currentUser.getLoginName(), createdTask.getWorkLogs().get(0).getProfile().getLoginName());
	}

	@Test
	public void createUpdateIteration() throws ValidationException, EntityNotFoundException {
		RepositoryConfiguration originalConfig = taskService.getRepositoryContext();

		Iteration newIteration = new Iteration("32-nextSprint");
		newIteration = taskService.createIteration(newIteration);
		Assert.assertNotNull(newIteration);
		Assert.assertNotNull(newIteration.getId());

		RepositoryConfiguration newConfig = taskService.getRepositoryContext();
		Assert.assertEquals(originalConfig.getIterations().size() + 1, newConfig.getIterations().size());

		newIteration.setIsActive(false);
		newIteration = taskService.updateIteration(newIteration);
		newConfig = taskService.getRepositoryContext();
		Assert.assertEquals(newConfig.getIterations().size() - 1, newConfig.getActiveIterations().size());
	}

	@Test
	public void testWorkLogBeforeTaskCreationOnUpdate_task2812() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask();
		String desc = "Description";
		mockTask.setDescription(desc);
		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);

		WorkLog worklog = new WorkLog();
		worklog.setDateWorked(new Date(createdTask.getCreationDate().getTime() - 20000l));
		worklog.setHoursWorked(BigDecimal.valueOf(3.4));
		worklog.setComment("worklog");
		createdTask.getWorkLogs().add(worklog);

		com.tasktop.c2c.server.tasks.domain.Task updatedTask = taskService.updateTask(createdTask);
		Assert.assertEquals(desc, updatedTask.getDescription());
	}

	@Test
	public void testWorkLogBeforeTaskCreationOnCreate_task2812() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask();
		String desc = "Description";
		mockTask.setDescription(desc);

		WorkLog worklog = new WorkLog();
		worklog.setDateWorked(new Date(System.currentTimeMillis() - 20000l));
		worklog.setHoursWorked(BigDecimal.valueOf(3.4));
		worklog.setComment("worklog");
		mockTask.getWorkLogs().add(worklog);

		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);

		com.tasktop.c2c.server.tasks.domain.Task updatedTask = taskService.updateTask(createdTask);
		Assert.assertEquals(desc, updatedTask.getDescription());
	}

	@Test
	public void testCommentDoesNotBecomeDescription_task2838() throws ValidationException, EntityNotFoundException,
			ConcurrentUpdateException, InterruptedException {
		com.tasktop.c2c.server.tasks.domain.Task mockTask = getMockTask();
		String desc = "Description";
		mockTask.setDescription(desc);
		com.tasktop.c2c.server.tasks.domain.Task createdTask = taskService.createTask(mockTask);
		com.tasktop.c2c.server.tasks.domain.Task updatedTask = taskService.updateTask(createdTask);
		Assert.assertEquals(desc, updatedTask.getDescription());

		for (int i = 1; i < 10; i++) {
			Thread.sleep(1000);
			updatedTask.addComment("Comment" + i);
			updatedTask = taskService.updateTask(updatedTask);
			Assert.assertEquals("failed on comment " + i, desc, updatedTask.getDescription());
		}
	}

	@Test
	public void testWikiRenderer() {
		String wikiText = ".pre hello";
		String html = taskService.renderWikiMarkupAsHtml(wikiText);
		Assert.assertNotNull(html);
	}
}
