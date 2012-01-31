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
package com.tasktop.c2c.server.internal.tasks.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;

import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.internal.tasks.domain.AbstractReferenceEntity;
import com.tasktop.c2c.server.internal.tasks.domain.Cc;
import com.tasktop.c2c.server.internal.tasks.domain.Comment;
import com.tasktop.c2c.server.internal.tasks.domain.Keyword;
import com.tasktop.c2c.server.internal.tasks.domain.Priority;
import com.tasktop.c2c.server.internal.tasks.domain.Task;
import com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.internal.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.internal.tasks.service.TaskCustomFieldService;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;

public class TaskQuery {

	private static final String TASK = "task";

	static final Map<String, Class<?>> compareTypeBySimpleField = new HashMap<String, Class<?>>();

	static {
		compareTypeBySimpleField.put(TaskFieldConstants.SUMMARY_FIELD, String.class);
		compareTypeBySimpleField.put(TaskFieldConstants.STATUS_FIELD, String.class);
		compareTypeBySimpleField.put(TaskFieldConstants.PRIORITY_FIELD, String.class);
		compareTypeBySimpleField.put(TaskFieldConstants.SEVERITY_FIELD, String.class);
		compareTypeBySimpleField.put(TaskFieldConstants.RESOLUTION_FIELD, String.class);
		compareTypeBySimpleField.put(TaskFieldConstants.MILESTONE_FIELD, String.class);
		compareTypeBySimpleField.put(TaskFieldConstants.ASSIGNEE_FIELD, String.class);
		compareTypeBySimpleField.put(TaskFieldConstants.REPORTER_FIELD, String.class);
		compareTypeBySimpleField.put(TaskFieldConstants.PRODUCT_FIELD, Number.class);
		compareTypeBySimpleField.put(TaskFieldConstants.COMPONENT_FIELD, Number.class);
		compareTypeBySimpleField.put(TaskFieldConstants.PRODUCT_NAME_FIELD, String.class);
		compareTypeBySimpleField.put(TaskFieldConstants.COMPONENT_NAME_FIELD, String.class);
		compareTypeBySimpleField.put(TaskFieldConstants.LAST_UPDATE_FIELD, Date.class);
		compareTypeBySimpleField.put(TaskFieldConstants.CREATION_TIME_FIELD, Date.class);
		compareTypeBySimpleField.put(TaskFieldConstants.TASK_ID_FIELD, Number.class);
	}

	private Expression expression;
	private Expression orderingExpression;
	private ExpressionBuilder builder = new ExpressionBuilder(Task.class);

	private Map<String, Expression> aliasedItems = new LinkedHashMap<String, Expression>();

	private TaskCustomFieldService customFieldService;

	private Map<String, FieldDescriptor> customFieldByName;

	private TaskQuery() {
		aliasedItems.put(TASK, builder);
	}

	public static TaskQuery create(TaskCustomFieldService customFieldService, Criteria criteria, SortInfo sortInfo) {
		TaskQuery taskQuery = new TaskQuery();
		taskQuery.setCustomFieldService(customFieldService);
		taskQuery.setCriteria(criteria);
		taskQuery.computeSortExpression(sortInfo);
		return taskQuery;
	}

	public void setCustomFieldService(TaskCustomFieldService customFieldService) {
		this.customFieldService = customFieldService;
	}

	private void computeSortExpression(SortInfo sortInfo) {
		if (sortInfo == null) {
			return;
		}
		Class<?> type = compareTypeBySimpleField.get(sortInfo.getSortField());
		if (type == null) {
			FieldDescriptor customField = getCustomFieldByName(sortInfo.getSortField());
			if (customField != null) {
				type = computeFieldType(customField.getFieldType());
			}
			if (type == null) {
				throw new IllegalStateException("Sorting is not supported on " + sortInfo.getSortField());
			}
		}
		Expression expression = computeFieldExpression(builder, FieldPurpose.SORT, sortInfo.getSortField());
		if (sortInfo.getSortOrder() == SortInfo.Order.DESCENDING) {
			expression = expression.descending();
		} else {
			expression = expression.ascending();
		}
		orderingExpression = expression;
	}

	private void setCriteria(Criteria criteria) {
		if (criteria == null) {
			expression = builder.get("id").equal(builder.get("id"));
		} else {
			expression = computeExpression(builder, criteria);
		}
	}

	private Expression computeExpression(Expression base, Criteria criteria) {
		if (criteria instanceof ColumnCriteria) {
			return computeExpression(base, (ColumnCriteria) criteria);
		} else if (criteria instanceof NaryCriteria) {
			return computeExpression(base, (NaryCriteria) criteria);
		} else {
			throw new IllegalStateException(criteria.getClass().getName());
		}
	}

	private Expression computeExpression(Expression base, ColumnCriteria criteria) {
		if (criteria.getOperator().isUnary()) {
			throw new IllegalStateException();
		} else {
			Class<?> type = compareTypeBySimpleField.get(criteria.getColumnName());
			if (type != null) {
				Expression fieldExpression = computeFieldExpression(base, FieldPurpose.COMPARE,
						criteria.getColumnName());
				return computeExpression(fieldExpression, criteria, type);
			} else {
				if (criteria.getColumnName().equals(TaskFieldConstants.DESCRIPTION_FIELD)
						|| criteria.getColumnName().equals(TaskFieldConstants.COMMENT_FIELD)) {
					ExpressionBuilder comments = new ExpressionBuilder();
					ReportQuery subQuery = new ReportQuery(Comment.class, comments);
					subQuery.addAttribute("task", comments.getField("bug_id"));
					Expression match = computeExpression(comments.get("thetext"), criteria, String.class);
					subQuery.setSelectionCriteria(match);
					return base.get("id").in(subQuery);

				} else if (criteria.getColumnName().equals(TaskFieldConstants.COMMENT_AUTHOR_FIELD)) {
					ExpressionBuilder comments = new ExpressionBuilder();
					ReportQuery subQuery = new ReportQuery(Comment.class, comments);
					subQuery.addAttribute("task", comments.getField("bug_id"));
					Expression match = computeExpression(comments.get("profile").get("loginName"), criteria,
							String.class);
					subQuery.setSelectionCriteria(match);
					return base.get("id").in(subQuery);

				} else if (criteria.getColumnName().equals(TaskFieldConstants.WATCHER_FIELD)) {
					ExpressionBuilder watchers = new ExpressionBuilder();
					ReportQuery subQuery = new ReportQuery(Cc.class, watchers);
					subQuery.addAttribute("bugs", watchers.getField("bug_id"));
					Expression match = computeExpression(watchers.get("profiles").get("loginName"), criteria,
							String.class);
					subQuery.setSelectionCriteria(match);
					return base.get("id").in(subQuery);

				} else if (criteria.getColumnName().equals(TaskFieldConstants.KEYWORDS_FIELD)) {
					ExpressionBuilder keywords = new ExpressionBuilder();
					ReportQuery subQuery = new ReportQuery(Keyword.class, keywords);
					subQuery.addAttribute("bugs", keywords.getField("bug_id"));
					Expression match = computeExpression(keywords.get("keyworddefs").get("name"), criteria,
							String.class);
					subQuery.setSelectionCriteria(match);
					return base.get("id").in(subQuery);

				} else {
					FieldDescriptor customField = getCustomFieldByName(criteria.getColumnName());
					if (customField != null) {
						type = computeFieldType(customField.getFieldType());
						Expression fieldExpression = computeFieldExpression(base, FieldPurpose.COMPARE,
								criteria.getColumnName());
						return computeExpression(fieldExpression, criteria, type);
					}
					throw new UnsupportedOperationException("Unknown field: " + criteria.getColumnName());
				}
			}
		}
	}

	private Class<?> computeFieldType(FieldType fieldType) {
		switch (fieldType) {
		case TIMESTAMP:
			return Date.class;
		case TASK_REFERENCE:
			return Integer.class;
		default:
			return String.class;
		}
	}

	private FieldDescriptor getCustomFieldByName(String fieldName) {
		if (customFieldByName == null) {
			customFieldByName = new HashMap<String, FieldDescriptor>();
			List<FieldDescriptor> customFields = customFieldService.getCustomFields();
			for (FieldDescriptor descriptor : customFields) {
				customFieldByName.put(descriptor.getName(), descriptor);
			}
		}
		return customFieldByName.get(fieldName);
	}

	private enum FieldPurpose {
		SORT, COMPARE
	}

	private Expression computeFieldExpression(Expression base, FieldPurpose purpose, String fieldName) {
		if (fieldName.equals(TaskFieldConstants.SUMMARY_FIELD)) {
			return base.get("shortDesc");
		} else if (fieldName.equals(TaskFieldConstants.STATUS_FIELD)) {
			if (purpose == FieldPurpose.SORT) {
				return computeReferenceSortExpression(TaskStatus.class, "status");
			}
			return base.get("status");
		} else if (fieldName.equals(TaskFieldConstants.PRIORITY_FIELD)) {
			if (purpose == FieldPurpose.SORT) {
				return computeReferenceSortExpression(Priority.class, "priority");
			}
			return base.get("priority");
		} else if (fieldName.equals(TaskFieldConstants.SEVERITY_FIELD)) {
			if (purpose == FieldPurpose.SORT) {
				return computeReferenceSortExpression(TaskSeverity.class, "severity");
			}
			return base.get("severity");
		} else if (fieldName.equals(TaskFieldConstants.RESOLUTION_FIELD)) {
			return base.get("resolution");
		} else if (fieldName.equals(TaskFieldConstants.MILESTONE_FIELD)) {
			return base.get("targetMilestone");
		} else if (fieldName.equals(TaskFieldConstants.CREATION_TIME_FIELD)) {
			return base.get("creationTs");
		} else if (fieldName.equals(TaskFieldConstants.LAST_UPDATE_FIELD)) {
			return base.get("deltaTs");
		} else if (fieldName.equals(TaskFieldConstants.TASK_ID_FIELD)) {
			return base.get("id");
		} else if (fieldName.equals(TaskFieldConstants.ASSIGNEE_FIELD)) {
			return base.getAllowingNull("assignee").get("loginName");
		} else if (fieldName.equals(TaskFieldConstants.REPORTER_FIELD)) {
			return base.get("reporter").get("loginName");
		} else if (fieldName.equals(TaskFieldConstants.PRODUCT_FIELD)) {
			base = base.get("product");
			return purpose == FieldPurpose.SORT ? base.get("name") : base.get("id");
		} else if (fieldName.equals(TaskFieldConstants.PRODUCT_NAME_FIELD)) {
			return base.get("product").get("name");
		} else if (fieldName.equals(TaskFieldConstants.COMPONENT_FIELD)) {
			base = base.get("component");
			return purpose == FieldPurpose.SORT ? base.get("name") : base.get("id");
		} else if (fieldName.equals(TaskFieldConstants.COMPONENT_NAME_FIELD)) {
			return base.get("component").get("name");
		} else if (fieldName.equals(TaskFieldConstants.KEYWORDS_FIELD)) {
			return base.get("keywords");
		} else {
			FieldDescriptor customField = getCustomFieldByName(fieldName);
			if (customField != null) {
				return base.getField(customFieldService.getColumnName(customField));
			}
			throw new IllegalStateException("Unsupported field " + fieldName);
		}
	}

	private Expression computeReferenceSortExpression(Class<? extends AbstractReferenceEntity> refClass,
			String refProperty) {
		Expression priority = new ExpressionBuilder(refClass);
		addAlias(priority);
		Expression sortComponent = priority.get("value").equal(builder.get(refProperty));
		expression = expression == null ? sortComponent : expression.and(sortComponent);
		return priority.get("sortkey");
	}

	private void addAlias(Expression expression) {
		aliasedItems.put("e" + aliasedItems.size(), expression);
	}

	private static Expression computeExpression(Expression expression, ColumnCriteria criteria, Class<?> valueClass) {
		Object value = criteria.getColumnValue();
		// Special case: dates get serialized over REST API as longs.
		if (value instanceof Long && valueClass.equals(Date.class)) {
			value = new Date((Long) value);
		}
		if (!valueClass.isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException("Criteria value for field " + criteria.getColumnName()
					+ " should be of type " + valueClass.getSimpleName() + ". Was of type "
					+ value.getClass().getSimpleName());
		}
		switch (criteria.getOperator()) {
		case EQUALS:
			return expression.equal(value);
		case NOT_EQUALS:
			return expression.notEqual(value);
		case STRING_CONTAINS:
			return expression.like("%" + value.toString() + "%");
		case LESS_THAN:
			return expression.lessThan(value);
		case GREATER_THAN:
			return expression.greaterThan(value);
		}
		throw new IllegalStateException(criteria.getOperator().name() + " is not supported for column criteria");
	}

	private Expression computeExpression(Expression base, NaryCriteria criteria) {
		Expression expression = null;
		if (criteria.getOperator().isUnary()) {
			if (criteria.getSubCriteria().size() != 1) {
				throw new IllegalArgumentException();
			}
			switch (criteria.getOperator()) {
			case NOT:
				expression = computeExpression(builder, criteria.getSubCriteria().get(0)).not();
				break;
			default:
				throw new IllegalStateException(criteria.getOperator().name());
			}
		} else {
			for (Criteria subCriteria : criteria.getSubCriteria()) {
				if (expression != null) {
					switch (criteria.getOperator()) {
					case AND:
						expression = expression.and(computeExpression(builder, subCriteria));
						break;
					case OR:
						expression = expression.or(computeExpression(builder, subCriteria));
						break;
					default:
						throw new IllegalStateException(criteria.getOperator().name());
					}
				} else {
					expression = computeExpression(builder, subCriteria);
				}
			}
		}
		return expression;
	}

	private enum QueryMode {
		COUNT, NORMAL
	}

	@SuppressWarnings("unchecked")
	public int countResults(EntityManager entityManager) {
		// must flush before query
		entityManager.flush();

		ReadAllQuery query = prepareQuery(null, QueryMode.COUNT);
		Object count = executeQuery(entityManager, query);
		return ((List<Number>) count).get(0).intValue();
	}

	public List<Task> getResults(EntityManager entityManager, Region region) {
		// must flush before query
		entityManager.flush();

		ReadAllQuery query = prepareQuery(region, QueryMode.NORMAL);

		@SuppressWarnings("unchecked")
		List<Object> results = (List<Object>) executeQuery(entityManager, query);
		List<Task> tasks = new ArrayList<Task>(results.size());
		for (Object result : results) {
			if (result.getClass().isArray()) {
				tasks.add((Task) ((Object[]) result)[0]);
			} else {
				tasks.add((Task) result);
			}
		}
		return tasks;
	}

	private Object executeQuery(EntityManager entityManager, ReadAllQuery query) {
		JpaEntityManager jpaEntityManager = entityManager.unwrap(JpaEntityManager.class);

		UnitOfWork unitOfWork = jpaEntityManager.getUnitOfWork();
		Session session = unitOfWork == null ? jpaEntityManager.getSession() : unitOfWork.getActiveSession();

		return session.executeQuery(query);
	}

	private ReadAllQuery prepareQuery(Region region, QueryMode mode) {
		ReadAllQuery query;
		if (mode == QueryMode.COUNT) {
			ReportQuery reportQuery = new ReportQuery();
			reportQuery.setReturnType(ReportQuery.ShouldReturnSingleAttribute);
			reportQuery.addItem("taskCount", builder.count());
			query = reportQuery;
		} else {
			if (aliasedItems.size() > 1) {
				ReportQuery reportQuery = new ReportQuery();
				reportQuery.setReturnType(ReportQuery.ShouldReturnWithoutReportQueryResult);
				for (Map.Entry<String, Expression> item : aliasedItems.entrySet()) {
					reportQuery.addItem(item.getKey(), item.getValue());
				}
				query = reportQuery;
			} else {
				query = new ReadAllQuery();
			}
		}
		query.setReferenceClass(Task.class);
		query.setSelectionCriteria(expression);
		query.setIsExecutionClone(true);
		if (mode != QueryMode.COUNT) {
			query.setFirstResult(region.getOffset());
			query.setMaxRows(region.getSize());
			if (orderingExpression != null) {
				query.addOrdering(orderingExpression);
			}
		}
		return query;
	}
}
