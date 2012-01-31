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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.internal.tasks.domain.ActivityIgnored;
import com.tasktop.c2c.server.internal.tasks.domain.Cc;
import com.tasktop.c2c.server.internal.tasks.domain.Dependency;
import com.tasktop.c2c.server.internal.tasks.domain.Fielddef;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.internal.tasks.domain.ReferenceEntity;
import com.tasktop.c2c.server.internal.tasks.domain.Task;
import com.tasktop.c2c.server.internal.tasks.domain.TaskActivity;
import com.tasktop.c2c.server.internal.tasks.domain.TaskActivityId;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.DomainConversionContext;
import com.tasktop.c2c.server.internal.tasks.domain.conversion.DomainConverter;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.WorkLog;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.FieldUpdate;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.Type;
import com.tasktop.c2c.server.tasks.service.TaskService;

/**
 * Helper class to deal with task activity.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 */
@Component
public class TaskActivityService {

	private static final String CUSTOM_FIELD_PREFIX = "cf_";

	private static final Comparator<? super com.tasktop.c2c.server.tasks.domain.TaskActivity> ACTIVITY_COMPARATOR = new Comparator<com.tasktop.c2c.server.tasks.domain.TaskActivity>() {

		@Override
		public int compare(com.tasktop.c2c.server.tasks.domain.TaskActivity o1,
				com.tasktop.c2c.server.tasks.domain.TaskActivity o2) {
			int i = o2.getActivityDate().compareTo(o1.getActivityDate()); // newest first
			if (i == 0) {
				i = o1.getTask().getId().compareTo(o2.getTask().getId()); // group by task
				if (i == 0) {
					i = o2.getActivityType().compareTo(o1.getActivityType()); // order by activity type (updated before
																				// created)
				}
			}
			return i;
		}
	};

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private TaskService taskService;

	@Autowired
	private DomainConverter domainConverter;

	/**
	 * Record the changes made between the original and newTasks.
	 * 
	 * @param originalTask
	 * @param originalCustomFieldValues
	 * @param newTask
	 * @param newCustomFieldValues
	 * @param user
	 * @param updateTime
	 * @return public version of activities recorded.
	 * @throws EntityNotFoundException
	 */
	public List<com.tasktop.c2c.server.tasks.domain.TaskActivity> recordActivity(Task originalTask,
			Map<String, String> originalCustomFieldValues, Task newTask, Map<String, String> newCustomFieldValues,
			Profile user, Date updateTime) throws EntityNotFoundException {

		List<TaskActivity> activities = new ArrayList<TaskActivity>(5);

		CriteriaQuery<Fielddef> fielddefQuery = entityManager.getCriteriaBuilder().createQuery(Fielddef.class);
		List<Fielddef> fielddefs = entityManager.createQuery(fielddefQuery).getResultList();
		Map<String, Fielddef> fielddefByName = new HashMap<String, Fielddef>();
		for (Fielddef fielddef : fielddefs) {
			fielddefByName.put(fielddef.getName(), fielddef);
		}
		PropertyDescriptor[] propertyDescriptors;
		try {
			propertyDescriptors = Introspector.getBeanInfo(Task.class).getPropertyDescriptors();
		} catch (IntrospectionException e) {
			throw new IllegalStateException(e);
		}
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			Method accessor = propertyDescriptor.getReadMethod();
			if (accessor.getAnnotation(ActivityIgnored.class) != null) {
				continue;
			}
			int modifiers = accessor.getModifiers();
			if (accessor != null && Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
				Column column = accessor.getAnnotation(Column.class);
				if (column != null) {
					String name = column.name();
					Fielddef fielddef = fielddefByName.get(name);
					if (fielddef != null) {
						maybeCreateActivity(activities, originalTask, newTask, fielddef, user, updateTime, accessor);
					}
				} else {
					ManyToOne manyToOne = accessor.getAnnotation(ManyToOne.class);
					if (manyToOne != null) {
						Fielddef fielddef = computeFielddefByJoinColumn(fielddefByName, accessor);
						if (fielddef != null) {
							maybeCreateActivity(activities, originalTask, newTask, fielddef, user, updateTime, accessor);
						}
					}
				}
			}
		}

		if (!originalTask.getDependenciesesForDependson().equals(newTask.getDependenciesesForDependson())) {
			TaskActivity activity = createActivity(fielddefByName, originalTask, user, updateTime, "dependson");
			activity.getId()
					.setAdded(
							getDepsAdded(originalTask.getDependenciesesForDependson(),
									newTask.getDependenciesesForDependson()));
			activity.getId().setRemoved(
					getDepsRemoved(originalTask.getDependenciesesForDependson(),
							newTask.getDependenciesesForDependson()));
			activities.add(activity);
		}

		if (!originalTask.getDependenciesesForBlocked().equals(newTask.getDependenciesesForBlocked())) {
			TaskActivity activity = createActivity(fielddefByName, originalTask, user, updateTime, "blocked");
			activity.getId().setAdded(
					getBlockedAdded(originalTask.getDependenciesesForBlocked(), newTask.getDependenciesesForBlocked()));
			activity.getId()
					.setRemoved(
							getBlockedRemoved(originalTask.getDependenciesesForBlocked(),
									newTask.getDependenciesesForBlocked()));
			activities.add(activity);
		}

		// Special handling for the first comment: task description
		// FIXME just seem to need this update check for comments
		if (!originalTask.getComments().isEmpty() && !newTask.getComments().isEmpty()
				&& !originalTask.getComments().get(0).getThetext().equals(newTask.getComments().get(0).getThetext())) {
			TaskActivity activity = createActivity(fielddefByName, originalTask, user, updateTime, "longdesc");

			String newComment = newTask.getComments().get(0).getThetext();
			activity.getId().setAdded(newComment);

			String oldComment = originalTask.getComments().get(0).getThetext();
			activity.getId().setRemoved(oldComment);

			activities.add(activity);
		}
		if (!originalTask.getCcs().isEmpty() || !newTask.getCcs().isEmpty()) {
			Set<Cc> oldCc = Collections.emptySet();
			Set<Cc> newCc = Collections.emptySet();
			if (originalTask.getCcs() != null && !originalTask.getCcs().isEmpty()) {
				oldCc = new HashSet<Cc>(originalTask.getCcs());
			}
			if (newTask.getCcs() != null && !newTask.getCcs().isEmpty()) {
				newCc = new HashSet<Cc>(newTask.getCcs());
			}
			String ccRemoved = computeRemoved(oldCc, newCc);
			String ccAdded = computeRemoved(newCc, oldCc);

			if (ccRemoved != null || ccAdded != null) {
				TaskActivity activity = createActivity(fielddefByName, originalTask, user, updateTime, "cc");
				activity.getId().setAdded(ccAdded == null ? "" : ccAdded);
				activity.getId().setRemoved(ccRemoved == null ? "" : ccRemoved);
				activities.add(activity);
			}
		}
		if (newCustomFieldValues != null) {
			for (Map.Entry<String, String> newValueEntry : newCustomFieldValues.entrySet()) {
				String fielddefName = CUSTOM_FIELD_PREFIX + newValueEntry.getKey();
				Fielddef fielddef = fielddefByName.get(fielddefName);
				if (fielddef != null && fielddef.getCustom()) {
					String newValue = newValueEntry.getValue();
					String originalValue = originalCustomFieldValues.get(newValueEntry.getKey());
					if (!equals(originalValue, newValue)) {
						String valueAdded = toActivityValue(newValue);
						String valueRemoved = toActivityValue(originalValue);

						TaskActivity activity = new TaskActivity();
						activity.setId(new TaskActivityId(originalTask.getId(), user.getId(), updateTime, fielddef
								.getId(), valueAdded, valueRemoved));
						activity.setProfiles(user);
						activity.setBugs(originalTask);
						activity.setFielddefs(fielddef);
						activities.add(activity);
					}
				}
			}
		}
		for (TaskActivity activity : activities) {
			persist(activity);
		}

		List<com.tasktop.c2c.server.tasks.domain.TaskActivity> result = new ArrayList<com.tasktop.c2c.server.tasks.domain.TaskActivity>();
		result.addAll(postProcessActivities(createDomainConversionContext(), activities, activities.size()));
		return result;
	}

	private void persist(TaskActivity activity) {
		// activity can only handle 255 characters for both added and removed
		// so first we truncate the data to fit.

		activity.getId().setAdded(truncateActivityData(activity.getId().getAdded()));
		activity.getId().setRemoved(truncateActivityData(activity.getId().getRemoved()));

		entityManager.persist(activity);
	}

	private String truncateActivityData(String text) {
		if (text != null && text.length() > 0) {
			final int maxLength = 252;
			String truncated = text.substring(0, computeUtf8StringLength(text, maxLength));
			if (!truncated.equals(text)) {
				truncated += "...";
				text = truncated;
			}
		}
		return text;
	}

	/**
	 * compute the length of the given text encoded in utf-8, not exceeding the provided maxLength in bytes
	 * 
	 * @param text
	 * @param maxLength
	 *            the maximum allowable length of the string in bytes
	 * @return the string length that corresponds to the string that can be utf-8 encoded in 255 bytes
	 */
	private int computeUtf8StringLength(final String text, final int maxLength) {
		final int textLength = text.length();

		CharsetEncoder encoder = Charset.forName("utf-8").newEncoder();
		char[] ch = new char[1];
		CharBuffer charBuffer = CharBuffer.wrap(ch);

		ByteBuffer buffer = ByteBuffer.allocate(12);

		int utf8StringLength = 0;
		int size = 0;

		while (utf8StringLength < textLength) {
			ch[0] = text.charAt(utf8StringLength);
			encoder.encode(charBuffer, buffer, false);
			size += buffer.position();
			if (size <= maxLength) {
				++utf8StringLength;
			}
			if (size >= maxLength) {
				break;
			}
			buffer.position(0);
			charBuffer.position(0);
		}
		return utf8StringLength;
	}

	private String computeRemoved(Set<Cc> oldCc, Set<Cc> newCc) {
		String ccRemoved = null;
		for (Cc cc : oldCc) {
			if (!newCc.contains(cc)) {
				if (ccRemoved == null) {
					ccRemoved = toActivityValue(cc.getProfiles());
				} else {
					ccRemoved += ", ";
					ccRemoved += toActivityValue(cc.getProfiles());
				}
			}
		}
		return ccRemoved;
	}

	private void maybeCreateActivity(List<TaskActivity> activities, Task oldTask, Task newTask, Fielddef fielddef,
			Profile user, Date updateTime, Method accessor) {
		Object originalValue = getValue(accessor, oldTask);
		Object newValue = getValue(accessor, newTask);
		if (!equals(originalValue, newValue)) {
			String valueAdded = toActivityValue(newValue);
			String valueRemoved = toActivityValue(originalValue);

			TaskActivity activity = new TaskActivity();
			activity.setId(new TaskActivityId(oldTask.getId(), user.getId(), updateTime, fielddef.getId(), valueAdded,
					valueRemoved));
			activity.setProfiles(user);
			activity.setBugs(oldTask);
			activity.setFielddefs(fielddef);

			activities.add(activity);
		}
	}

	private Object getValue(Method readMethod, Task task) {
		try {
			return readMethod.invoke(task);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private Fielddef computeFielddefByJoinColumn(Map<String, Fielddef> fielddefByName, Method mappedAccessor) {
		Fielddef fielddef = null;
		JoinColumn joinColumn = mappedAccessor.getAnnotation(JoinColumn.class);
		if (joinColumn != null) {
			String name = joinColumn.name();
			fielddef = fielddefByName.get(name);
			if (fielddef == null && name.endsWith("_id")) {
				fielddef = fielddefByName.get(name.substring(0, name.length() - 3));
			}
		}
		return fielddef;
	}

	private String toActivityValue(Object value) {
		if (value == null) {
			return "";
		} else if (value instanceof ReferenceEntity) {
			String name = ((ReferenceEntity) value).getName();
			return name == null ? "" : name;
		} else if (value instanceof Profile) {
			Profile profile = (Profile) value;
			return profile.getLoginName();
		}
		return value.toString();
	}

	private boolean equals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		} else if (o1 != null) {
			if (o1 instanceof BigDecimal) {
				if (o2 != null) {
					return ((BigDecimal) o1).compareTo((BigDecimal) o2) == 0;
				}
			}
			if (o1 instanceof String) {
				if (o2 == null) {
					return ((String) o1).isEmpty(); // Special case: "" = null
				}
			}
			return o1.equals(o2);
		} else if (o2 != null && o2 instanceof String) {
			return ((String) o2).isEmpty(); // Special case: "" = null
		}
		return false;
	}

	private String getDepsAdded(List<Dependency> originalDeps, List<Dependency> newDeps) {
		String result = "";
		for (Dependency newDep : newDeps) {
			if (!originalDeps.contains(newDep)) {
				if (!result.isEmpty()) {
					result = result + ", ";
				}
				result = result + newDep.getBugsByBlocked().getId();
			}
		}

		return result;

	}

	private String getDepsRemoved(List<Dependency> originalDeps, List<Dependency> newDeps) {
		String result = "";
		for (Dependency oldDep : originalDeps) {
			if (!newDeps.contains(oldDep)) {
				if (!result.isEmpty()) {
					result = result + ", ";
				}
				result = result + oldDep.getBugsByBlocked().getId();
			}
		}

		return result;

	}

	private String getBlockedAdded(List<Dependency> originalDeps, List<Dependency> newDeps) {
		String result = "";
		for (Dependency newDep : newDeps) {
			if (!originalDeps.contains(newDep)) {
				if (!result.isEmpty()) {
					result = result + ", ";
				}
				result = result + newDep.getBugsByDependson().getId();
			}
		}

		return result;

	}

	private String getBlockedRemoved(List<Dependency> originalDeps, List<Dependency> newDeps) {
		String result = "";
		for (Dependency oldDep : originalDeps) {
			if (!newDeps.contains(oldDep)) {
				if (!result.isEmpty()) {
					result = result + ", ";
				}
				result = result + oldDep.getBugsByDependson().getId();
			}
		}

		return result;

	}

	private TaskActivity createActivity(Map<String, Fielddef> fielddefByName, Task task, Profile user, Date updateTime,
			String fieldName) {
		Fielddef fielddef = fielddefByName.get(fieldName);
		if (fielddef == null) {
			throw new IllegalStateException(fieldName);
		}
		TaskActivity activity = new TaskActivity();
		activity.setId(new TaskActivityId(task.getId(), user.getId(), updateTime, fielddef.getId()));
		activity.setProfiles(user);
		activity.setBugs(task);
		activity.setFielddefs(fielddef);

		return activity;
	}

	public List<com.tasktop.c2c.server.tasks.domain.TaskActivity> getRecentActivity(Region region) {
		DomainConversionContext conversionContext = createDomainConversionContext();

		List<com.tasktop.c2c.server.tasks.domain.TaskActivity> activity = new ArrayList<com.tasktop.c2c.server.tasks.domain.TaskActivity>();

		Query taskQuery = entityManager.createQuery("SELECT t FROM " + Task.class.getSimpleName()
				+ " t ORDER BY t.deltaTs DESC");
		if (region != null) {
			taskQuery.setFirstResult(region.getOffset());
			taskQuery.setMaxResults(region.getSize());
		}

		@SuppressWarnings("unchecked")
		List<Task> tasks = taskQuery.getResultList();

		for (Task task : tasks) {
			com.tasktop.c2c.server.tasks.domain.Task domainTask = (com.tasktop.c2c.server.tasks.domain.Task) domainConverter
					.convert(task, conversionContext);
			activity.addAll(deduceActivitiesFromTask(conversionContext, domainTask, null));

			// // Now we can further thin out the task
			domainTask.setComments(null);
			domainTask.setAttachments(null);
		}

		Query activityQuery = entityManager.createQuery("SELECT a FROM " + TaskActivity.class.getSimpleName()
				+ " a ORDER BY a.id.bugWhen DESC");

		if (region != null) {
			activityQuery.setFirstResult(region.getOffset());
			activityQuery.setMaxResults(region.getSize());
		}

		@SuppressWarnings("unchecked")
		List<TaskActivity> activities = activityQuery.getResultList();

		activity.addAll(postProcessActivities(conversionContext, activities, activities.size()));

		Collections.sort(activity, ACTIVITY_COMPARATOR);

		return activity;
	}

	public List<com.tasktop.c2c.server.tasks.domain.TaskActivity> listTaskActivity(Integer taskId)
			throws EntityNotFoundException {
		DomainConversionContext conversionContext = createDomainConversionContext();

		List<com.tasktop.c2c.server.tasks.domain.TaskActivity> activity = new ArrayList<com.tasktop.c2c.server.tasks.domain.TaskActivity>(
				50);
		Task task = entityManager.find(Task.class, taskId);
		com.tasktop.c2c.server.tasks.domain.Task domainTask = (com.tasktop.c2c.server.tasks.domain.Task) domainConverter
				.convert(task, conversionContext);

		activity.addAll(deduceActivitiesFromTask(conversionContext, domainTask, null));

		Query query = entityManager.createQuery("SELECT a FROM " + TaskActivity.class.getSimpleName()
				+ " a WHERE a.id.bugId = :bugId");
		query.setParameter("bugId", taskId);
		@SuppressWarnings("unchecked")
		List<TaskActivity> activities = query.getResultList();

		activity.addAll(postProcessActivities(conversionContext, activities, Integer.MAX_VALUE));
		Collections.sort(activity, ACTIVITY_COMPARATOR);
		return activity;
	}

	public List<com.tasktop.c2c.server.tasks.domain.TaskActivity> listActivity(Date date) {
		DomainConversionContext conversionContext = createDomainConversionContext();

		List<com.tasktop.c2c.server.tasks.domain.TaskActivity> activity = new ArrayList<com.tasktop.c2c.server.tasks.domain.TaskActivity>();

		@SuppressWarnings("unchecked")
		List<Task> tasks = entityManager
				.createQuery(
						"select distinct t from " + Task.class.getSimpleName()
								+ " t where t.creationTs >= :date or t.id in (select tc.task.id from "
								+ Comment.class.getSimpleName()
								+ " tc where tc.creationTs >= :date) or t.id in (select ta.bugs.id from "
								+ com.tasktop.c2c.server.internal.tasks.domain.Attachment.class.getSimpleName()
								+ " ta where ta.creationTs >= :date)").setParameter("date", date).getResultList();
		for (Task task : tasks) {
			com.tasktop.c2c.server.tasks.domain.Task domainTask = (com.tasktop.c2c.server.tasks.domain.Task) domainConverter
					.convert(task, conversionContext);
			activity.addAll(deduceActivitiesFromTask(conversionContext, domainTask, date));
		}

		final int maxResults = 5000;
		Query query = entityManager.createQuery("SELECT a FROM " + TaskActivity.class.getSimpleName()
				+ " a WHERE a.id.bugWhen >= :date");
		query.setParameter("date", date);
		query.setMaxResults(maxResults);
		@SuppressWarnings("unchecked")
		List<TaskActivity> activities = query.getResultList();

		activity.addAll(postProcessActivities(conversionContext, activities, maxResults));

		Collections.sort(activity, ACTIVITY_COMPARATOR);

		return activity;
	}

	private DomainConversionContext createDomainConversionContext() {
		DomainConversionContext conversionContext = new DomainConversionContext(entityManager);
		conversionContext.setThin(false); // Need the task comments and attachments.
		conversionContext.fill(com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity.class);
		conversionContext.fill(com.tasktop.c2c.server.internal.tasks.domain.TaskStatus.class);
		conversionContext.fill(com.tasktop.c2c.server.internal.tasks.domain.Priority.class);
		conversionContext.fill(com.tasktop.c2c.server.internal.tasks.domain.Resolution.class);
		conversionContext.fillMilestone();
		return conversionContext;
	}

	private List<com.tasktop.c2c.server.tasks.domain.TaskActivity> postProcessActivities(
			DomainConversionContext conversionContext, List<TaskActivity> activities, int maxActivities) {
		// FIXME PERF-OPT : we could reuse the conversion context from the task query

		List<com.tasktop.c2c.server.tasks.domain.TaskActivity> result = new ArrayList<com.tasktop.c2c.server.tasks.domain.TaskActivity>();
		int numActivities = Math.min(maxActivities, activities.size());

		for (int i = 0; i < numActivities; i++) {
			com.tasktop.c2c.server.tasks.domain.TaskActivity activity = (com.tasktop.c2c.server.tasks.domain.TaskActivity) domainConverter
					.convert(activities.get(i), conversionContext);
			result.add(activity);
		}

		collapseSimultaneousUpdates(result);
		addDescription(result);
		return result;
	}

	private void addDescription(List<com.tasktop.c2c.server.tasks.domain.TaskActivity> updateActivities) {
		for (com.tasktop.c2c.server.tasks.domain.TaskActivity activity : updateActivities) {
			String result = "";
			for (FieldUpdate fieldUpdate : activity.getFieldUpdates()) {

				if (fieldUpdate.getFieldName().equals("dependson")) {
					if (!fieldUpdate.getNewValue().isEmpty()) {
						result += "added parent " + fieldUpdate.getNewValue() + "\n";
					}
					if (!fieldUpdate.getOldValue().isEmpty()) {
						result += "removed parent " + fieldUpdate.getOldValue() + "\n";
					}
				} else if (fieldUpdate.getFieldName().equals("blocked")) {
					if (!fieldUpdate.getNewValue().isEmpty()) {
						result += "added subtask " + fieldUpdate.getNewValue() + "\n";
					}
					if (!fieldUpdate.getOldValue().isEmpty()) {
						result += "removed subtask " + fieldUpdate.getOldValue() + "\n";
					}
				} else {
					result += "set " + fieldUpdate.getFieldDescription() + " to " + fieldUpdate.getNewValue() + "\n";
				}
			}
			activity.setDescription(result);
		}

	}

	private void collapseSimultaneousUpdates(
			List<com.tasktop.c2c.server.tasks.domain.TaskActivity> updateActivities) {
		com.tasktop.c2c.server.tasks.domain.TaskActivity last = null;
		Iterator<com.tasktop.c2c.server.tasks.domain.TaskActivity> it = updateActivities.iterator();

		while (it.hasNext()) {
			com.tasktop.c2c.server.tasks.domain.TaskActivity current = it.next();
			if (last == null) {
				last = current;
			} else if (canCollapse(last, current)) {
				List<FieldUpdate> updates = new ArrayList<FieldUpdate>(last.getFieldUpdates());
				updates.addAll(current.getFieldUpdates());
				last.setFieldUpdates(updates);
				it.remove();
			} else {
				last = current;
			}
		}
	}

	private boolean canCollapse(com.tasktop.c2c.server.tasks.domain.TaskActivity a1,
			com.tasktop.c2c.server.tasks.domain.TaskActivity a2) {
		return a1.getActivityDate().equals(a2.getActivityDate()) && a1.getAuthor().equals(a2.getAuthor())
				&& a1.getTask().equals(a2.getTask());
	}

	/**
	 * Create the relevant activities that we can deduce from the task.
	 * 
	 * @param task
	 * @return
	 */
	private List<com.tasktop.c2c.server.tasks.domain.TaskActivity> deduceActivitiesFromTask(
			DomainConversionContext conversionContext, com.tasktop.c2c.server.tasks.domain.Task task,
			Date referenceDate) {
		List<com.tasktop.c2c.server.tasks.domain.TaskActivity> result = new ArrayList<com.tasktop.c2c.server.tasks.domain.TaskActivity>();

		if (referenceDate == null || task.getCreationDate().compareTo(referenceDate) >= 0) {
			result.add(constructCreated(task));
		}

		for (Comment comment : task.getComments()) {
			if (referenceDate == null || comment.getCreationDate().compareTo(referenceDate) >= 0) {
				result.add(constructComment(task, comment));
			}
		}

		for (WorkLog workLog : task.getWorkLogs()) {
			if (referenceDate == null || workLog.getDateWorked().compareTo(referenceDate) >= 0) {
				result.add(constructWorkLog(task, workLog));
			}
		}

		for (Attachment attachment : task.getAttachments()) {
			if (referenceDate == null || attachment.getCreationDate().compareTo(referenceDate) >= 0) {
				result.add(constructAttachment(task, attachment));
			}
		}
		return result;
	}

	com.tasktop.c2c.server.tasks.domain.TaskActivity constructCreated(
			com.tasktop.c2c.server.tasks.domain.Task task) {
		com.tasktop.c2c.server.tasks.domain.TaskActivity result = new com.tasktop.c2c.server.tasks.domain.TaskActivity();
		result.setActivityType(Type.CREATED);
		result.setTask(task);
		result.setAuthor(task.getReporter());
		result.setActivityDate(task.getCreationDate());
		result.setDescription("");
		return result;
	}

	com.tasktop.c2c.server.tasks.domain.TaskActivity constructComment(
			com.tasktop.c2c.server.tasks.domain.Task task, Comment comment) {
		com.tasktop.c2c.server.tasks.domain.TaskActivity result = new com.tasktop.c2c.server.tasks.domain.TaskActivity();

		result.setActivityType(Type.COMMENTED);
		result.setTask(task);
		result.setComment(comment);
		result.setAuthor(comment.getAuthor());
		result.setActivityDate(comment.getCreationDate());
		result.setDescription(comment.getCommentText());
		return result;
	}

	com.tasktop.c2c.server.tasks.domain.TaskActivity constructWorkLog(
			com.tasktop.c2c.server.tasks.domain.Task task, WorkLog workLog) {
		com.tasktop.c2c.server.tasks.domain.TaskActivity result = new com.tasktop.c2c.server.tasks.domain.TaskActivity();

		result.setActivityType(Type.LOGGED_TIME);
		result.setTask(task);
		result.setWorkLog(workLog);
		result.setAuthor(workLog.getProfile());
		result.setActivityDate(workLog.getDateWorked());
		result.setDescription(workLog.getComment());
		return result;
	}

	com.tasktop.c2c.server.tasks.domain.TaskActivity constructAttachment(
			com.tasktop.c2c.server.tasks.domain.Task task, Attachment attachment) {
		com.tasktop.c2c.server.tasks.domain.TaskActivity result = new com.tasktop.c2c.server.tasks.domain.TaskActivity();
		result.setActivityType(Type.ATTACHED);
		result.setTask(task);
		result.setAttachment(attachment);
		result.setAuthor(attachment.getSubmitter());
		result.setActivityDate(attachment.getCreationDate());
		result.setDescription(attachment.getFilename() + ": " + attachment.getDescription());
		return result;
	}

	/**
	 * @param result
	 * @param deltaTs
	 * @return
	 */
	public List<com.tasktop.c2c.server.tasks.domain.TaskActivity> deduceActivitiesFromTask(
			com.tasktop.c2c.server.tasks.domain.Task task, Date referenceDate) {
		return deduceActivitiesFromTask(createDomainConversionContext(), task, referenceDate);
	}

}
