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
package com.tasktop.c2c.server.internal.tasks.domain.conversion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.tasktop.c2c.server.internal.tasks.domain.Attachment;
import com.tasktop.c2c.server.internal.tasks.domain.Cc;
import com.tasktop.c2c.server.internal.tasks.domain.CcId;
import com.tasktop.c2c.server.internal.tasks.domain.Comment;
import com.tasktop.c2c.server.internal.tasks.domain.Component;
import com.tasktop.c2c.server.internal.tasks.domain.Dependency;
import com.tasktop.c2c.server.internal.tasks.domain.DependencyId;
import com.tasktop.c2c.server.internal.tasks.domain.Duplicate;
import com.tasktop.c2c.server.internal.tasks.domain.Milestone;
import com.tasktop.c2c.server.internal.tasks.domain.Priority;
import com.tasktop.c2c.server.internal.tasks.domain.Product;
import com.tasktop.c2c.server.internal.tasks.domain.Profile;
import com.tasktop.c2c.server.internal.tasks.domain.Resolution;
import com.tasktop.c2c.server.internal.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.internal.tasks.domain.Task;
import com.tasktop.c2c.server.internal.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.internal.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.ExternalTaskRelation;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.WorkLog;

/**
 * A utility for use with the task domain
 * 
 * Public to internal domain representation transformer.
 */
public class TaskDomain {

	public static com.tasktop.c2c.server.tasks.domain.TaskSeverity createDomain(TaskSeverity source) {
		if (source == null) {
			return null;
		}
		com.tasktop.c2c.server.tasks.domain.TaskSeverity target = new com.tasktop.c2c.server.tasks.domain.TaskSeverity();
		target.setSortkey(source.getSortkey());
		target.setValue(source.getValue());
		target.setId(source.getId().intValue());
		return target;
	}

	// TODO: Consider removing this and using a PriorityConverter
	public static com.tasktop.c2c.server.tasks.domain.Priority createDomain(Priority source) {
		if (source == null) {
			return null;
		}

		com.tasktop.c2c.server.tasks.domain.Priority target = new com.tasktop.c2c.server.tasks.domain.Priority();
		target.setId(source.getId() == null ? null : source.getId().intValue());
		target.setSortkey(source.getSortkey());
		target.setValue(source.getValue());
		return target;
	}

	public static com.tasktop.c2c.server.tasks.domain.TaskStatus createDomain(TaskStatus source) {
		if (source == null) {
			return null;
		}
		com.tasktop.c2c.server.tasks.domain.TaskStatus target = new com.tasktop.c2c.server.tasks.domain.TaskStatus();
		target.setId(source.getId() == null ? null : source.getId().intValue());
		target.setSortkey(source.getSortkey());
		target.setValue(source.getValue());
		target.setActive(source.getIsactive());
		target.setOpen(source.getIsOpen());
		return target;
	}

	public static TaskResolution createDomain(Resolution source) {
		if (source == null) {
			return null;
		}
		com.tasktop.c2c.server.tasks.domain.TaskResolution target = new com.tasktop.c2c.server.tasks.domain.TaskResolution();
		target.setId(source.getId() == null ? null : source.getId().intValue());
		target.setSortkey(source.getSortkey());
		target.setValue(source.getValue());
		return target;
	}

	public static Attachment createManaged(com.tasktop.c2c.server.tasks.domain.Attachment source) {
		Attachment target = new Attachment();
		target.setCreationTs(source.getCreationDate());
		target.setDescription(source.getDescription());
		target.setFilename(source.getFilename());
		target.setId(source.getId());
		target.setMimetype(source.getMimeType());
		target.setProfiles(createManaged(source.getSubmitter()));
		return target;
	}

	public static Task createManaged(com.tasktop.c2c.server.tasks.domain.Task source) {
		if (source == null) {
			return null;
		}
		Task target = new Task();

		target.setId(source.getId());
		target.setCreationTs(source.getCreationDate());
		target.setTaskType(source.getTaskType());
		target.setShortDesc(source.getShortDescription());

		target.setComments(copyCommentsAndWorkLogs(target, source));

		target.setDeadline(source.getDeadline());

		target.setExternalTaskRelations(generateExternalTaskRelationsString(source.getExternalTaskRelations()));
		target.setEstimatedTime(source.getEstimatedTime());

		// Non-nullable
		target.setKeywords(generateKeywordString(source.getKeywords()));
		target.setStatusWhiteboard("");
		target.setOpSys("");
		target.setVersion(source.getFoundInRelease() == null ? "" : source.getFoundInRelease());
		target.setReporter(TaskDomain.createManaged(source.getReporter()));
		target.setAssignee(TaskDomain.createManaged(source.getAssignee()));
		target.setProduct(TaskDomain.createManaged(source.getProduct()));
		target.setComponent(TaskDomain.createManaged(source.getComponent()));
		target.setRepPlatform("");
		target.setResolution(source.getResolution() == null ? "" : source.getResolution().getValue());
		target.setEverconfirmed(false);
		target.setReporterAccessible(true);
		target.setCclistAccessible(true);
		target.setRemainingTime(source.getRemainingTime() == null ? BigDecimal.ZERO : source.getRemainingTime());

		target.setSeverity(source.getSeverity() == null ? "" : source.getSeverity().getValue());
		target.setStatus(source.getStatus() == null ? "" : source.getStatus().getValue());
		target.setPriority(source.getPriority() == null ? "" : source.getPriority().getValue());
		target.setTargetMilestone(source.getMilestone() == null ? "" : source.getMilestone().getValue());

		// Do this last, as some of the previous calls may trigger updates on this object - as a result, we want to make
		// certain that the timestamp of this object is the same that was intended.
		target.setDeltaTs(source.getModificationDate());

		return target;
	}

	public static void makeDescriptionTheEarliestComment(
			com.tasktop.c2c.server.internal.tasks.domain.Task internalTask) {
		Date now = new Date();
		Comment descriptionComment = internalTask.getComments().get(0);

		Date commentMinDate = descriptionComment.getCreationTs() == null ? now : descriptionComment.getCreationTs();
		for (Comment c : internalTask.getComments().subList(1, internalTask.getComments().size())) {

			if (c.getCreationTs() != null && c.getCreationTs().before(commentMinDate)) {
				commentMinDate = c.getCreationTs();
			}
		}

		// Need to add a 1 second difference, since that's the granularity of the MySQL DATETIME data type - 1ms is not
		// sufficient.
		Date descriptionDate = new Date(commentMinDate.getTime() - 1000l);
		descriptionComment.setCreationTs(descriptionDate);

	}

	/**
	 * @param externalTaskRelations
	 * @return
	 */
	private static String generateExternalTaskRelationsString(List<ExternalTaskRelation> externalTaskRelations) {
		String relString = null;
		List<String> rels = new ArrayList<String>();
		if (externalTaskRelations != null) {
			for (ExternalTaskRelation externalTaskRelation : externalTaskRelations) {
				rels.add(externalTaskRelation.getType() + "." + externalTaskRelation.getKind() + ": "
						+ externalTaskRelation.getUri());
			}
			relString = StringUtils.join(rels, "\n");
		}
		return relString;
	}

	private static String generateKeywordString(List<Keyword> keywords) {
		String keywordString = "";
		if (keywords != null) {
			for (int i = 0; i < keywords.size(); i++) {
				if (i > 0) {
					keywordString += ", ";
				}
				keywordString += keywords.get(i).getName();
			}
		}
		return keywordString;
	}

	public static Component createManaged(com.tasktop.c2c.server.tasks.domain.Component source) {

		if (source == null) {
			return null;
		}

		Component target = new Component();

		// Make sure we have an ID set before we try and copy it.
		target.setId(source.getId() == null ? null : source.getId().shortValue());
		target.setDescription(source.getDescription());
		target.setName(source.getName());
		target.setProduct(TaskDomain.createManaged(source.getProduct()));
		target.setInitialOwner(TaskDomain.createManaged(source.getInitialOwner()));
		return target;
	}

	public static Milestone createManaged(com.tasktop.c2c.server.tasks.domain.Milestone source) {

		if (source == null) {
			return null;
		}

		Milestone target = new Milestone();

		// Make sure we have an ID set before we try and copy it. target.setId(source.getId());
		target.setSortkey(source.getSortkey());
		target.setValue(source.getValue());
		target.setProduct(TaskDomain.createManaged(source.getProduct()));
		return target;
	}

	public static void insertCcs(com.tasktop.c2c.server.tasks.domain.Task source, Task target) {
		target.setCcs(copyWatchers(target, source.getWatchers()));
	}

	public static void insertDuplicateOf(com.tasktop.c2c.server.tasks.domain.Task source, Task target) {
		if (source.getDuplicateOf() != null) {
			Duplicate dup = new Duplicate();
			dup.setBugsByBugId(target);
			Task dupOf = new Task();
			dupOf.setId(source.getDuplicateOf().getId());
			dup.setBugsByDupeOf(dupOf);
			dup.setDupe(target.getId());
			target.setDuplicatesByBugId(dup);
		} else {
			target.setDuplicatesByBugId(null);
		}
	}

	private static List<Cc> copyWatchers(Task targetTask, List<TaskUserProfile> source) {
		if (source == null) {
			return null;
		}
		List<Cc> target = new ArrayList<Cc>(source.size());
		for (TaskUserProfile sourceProfile : source) {
			Cc cc = copyCc(targetTask, sourceProfile);
			target.add(cc);
		}

		return target;
	}

	private static Cc copyCc(Task targetTask, TaskUserProfile sourceProfile) {
		Cc target = new Cc();
		target.setProfiles(createManaged(sourceProfile));
		target.setBugs(targetTask);
		target.setId(new CcId(targetTask.getId(), sourceProfile.getId()));
		return target;
	}

	public static void insertParentAndBlocks(com.tasktop.c2c.server.tasks.domain.Task source, Task target) {

		if (source.getBlocksTasks() != null) {
			for (com.tasktop.c2c.server.tasks.domain.Task blocks : source.getBlocksTasks()) {
				Dependency dep = new Dependency();
				dep.setId(new DependencyId(blocks.getId(), target.getId()));
				dep.setBugsByDependson(target);
				Task depTask = new Task();
				depTask.setId(blocks.getId());
				dep.setBugsByBlocked(depTask);
				target.getDependenciesesForDependson().add(dep);
			}
		}
	}

	public static void insertSubTasks(com.tasktop.c2c.server.tasks.domain.Task source, Task target) {

		if (source.getSubTasks() != null) {
			for (com.tasktop.c2c.server.tasks.domain.Task subTask : source.getSubTasks()) {
				Dependency dep = new Dependency();
				dep.setId(new DependencyId(target.getId(), subTask.getId()));
				dep.setBugsByBlocked(target);
				Task blockedTask = new Task();
				blockedTask.setId(subTask.getId());
				dep.setBugsByDependson(blockedTask);
				target.getDependenciesesForBlocked().add(dep);
			}
		}
	}

	public static Product createManaged(com.tasktop.c2c.server.tasks.domain.Product source) {
		if (source == null) {
			return null;
		}
		Product product = new Product();
		product.setId(source.getId() == null ? null : source.getId().shortValue());
		product.setDescription(source.getDescription());
		product.setName(source.getName());
		return product;
	}

	// TODO: consider removing
	public static com.tasktop.c2c.server.tasks.domain.Milestone createDomain(Milestone source) {
		if (source == null) {
			return null;
		}
		com.tasktop.c2c.server.tasks.domain.Milestone target = new com.tasktop.c2c.server.tasks.domain.Milestone();
		target.setId(source.getId());
		target.setSortkey(source.getSortkey());
		target.setValue(source.getValue());

		// SHALLOW copy.
		com.tasktop.c2c.server.tasks.domain.Product targetProduct = new com.tasktop.c2c.server.tasks.domain.Product();
		targetProduct.setId(source.getProduct().getId().intValue());
		target.setProduct(targetProduct);

		return target;
	}

	public static Profile createManaged(TaskUserProfile source) {
		if (source == null
				|| ((source.getLoginName() == null || source.getLoginName().trim().length() == 0) && source.getId() == null)) {
			return null;
		}
		Profile target = new Profile();
		target.setId(source.getId());
		target.setLoginName(source.getLoginName());
		target.setRealname(source.getRealname());
		target.setGravatarHash(source.getGravatarHash());

		return target;
	}

	private static List<Comment> copyCommentsAndWorkLogs(Task targetTask,
			com.tasktop.c2c.server.tasks.domain.Task sourceTask) {

		final List<com.tasktop.c2c.server.tasks.domain.Comment> sourceComments = sourceTask.getComments();
		final List<com.tasktop.c2c.server.tasks.domain.WorkLog> sourceWorkLogs = sourceTask.getWorkLogs();

		List<Comment> targetComments = new ArrayList<Comment>();

		Comment descriptionComment = createDescriptionComment(sourceTask);
		descriptionComment.setTask(targetTask);
		targetComments.add(descriptionComment);
		if (sourceComments != null) {
			for (com.tasktop.c2c.server.tasks.domain.Comment comment : sourceComments) {
				Comment targetComment = createManagedComment(comment);
				targetComment.setTask(targetTask);
				targetComments.add(targetComment);
			}
		}
		if (sourceWorkLogs != null) {
			for (com.tasktop.c2c.server.tasks.domain.WorkLog workLog : sourceWorkLogs) {
				Comment targetComment = createManagedWorkLogComment(workLog);
				targetComment.setTask(targetTask);
				targetComments.add(targetComment);
			}
		}

		return targetComments;
	}

	/**
	 * Create a Bugzilla comment to represent the {@link com.tasktop.c2c.server.tasks.domain.Task} description
	 * 
	 * @param sourceTask
	 * @return
	 */
	private static Comment createDescriptionComment(com.tasktop.c2c.server.tasks.domain.Task sourceTask) {
		Comment descriptionComment = new Comment();

		descriptionComment.setThetext(sourceTask.getDescription() == null ? "" : sourceTask.getDescription());
		descriptionComment.setWorkTime(BigDecimal.ZERO);
		descriptionComment.setIsprivate(false);
		descriptionComment.setProfile(createManaged(sourceTask.getReporter()));
		descriptionComment.setExtraData("");
		descriptionComment.setAlreadyWrapped(true);

		return descriptionComment;
	}

	/**
	 * Create a Bugzilla {@link Comment} to back a domain {@link com.tasktop.c2c.server.tasks.domain.Comment}
	 * 
	 * @param sourceComment
	 * @return
	 */
	public static Comment createManagedComment(com.tasktop.c2c.server.tasks.domain.Comment sourceComment) {
		if (sourceComment == null) {
			return null;
		}
		Comment targetComment = new Comment();
		targetComment.setId(sourceComment.getId());
		targetComment.setThetext(sourceComment.getCommentText());
		// FIXME -- need to loosen the Bugzilla schema to allow nulls for worktime.
		targetComment.setWorkTime(BigDecimal.ZERO); // work time is zero for regular comments
		targetComment.setIsprivate(false);
		targetComment.setProfile(createManaged(sourceComment.getAuthor()));
		targetComment.setExtraData("");
		targetComment.setAlreadyWrapped(true);

		return targetComment;
	}

	/**
	 * Create a Bugzilla {@link Comment} to back a domain {@link WorkLog}
	 * 
	 * @param workLog
	 * @return
	 */
	private static Comment createManagedWorkLogComment(WorkLog workLog) {
		if (workLog == null) {
			return null;
		}
		Comment targetComment = new Comment();
		targetComment.setId(workLog.getId());

		// Set the date from the domain object
		if (workLog.getDateWorked() != null) {
			targetComment.setCreationTs(workLog.getDateWorked());
		}

		// Set the time, worker, and comment
		targetComment.setWorkTime(workLog.getHoursWorked());
		targetComment.setProfile(createManaged(workLog.getProfile()));
		targetComment.setThetext(workLog.getComment() == null ? "" : workLog.getComment());

		targetComment.setIsprivate(false);
		targetComment.setExtraData("");
		targetComment.setAlreadyWrapped(true);

		return targetComment;
	}

	public static void fillManaged(Task managedTarget, Task source) {

		managedTarget.setVersion(source.getVersion());
		managedTarget.setShortDesc(source.getShortDesc());
		managedTarget.setDeadline(source.getDeadline());
		// Never update the creation timestamp source.getCreationTs()
		managedTarget.setDeltaTs(source.getDeltaTs());

		// Mandatory custom fields
		managedTarget.setTaskType(source.getTaskType());
		managedTarget.setExternalTaskRelations(source.getExternalTaskRelations());

		if (!managedTarget.getStatusWhiteboard().isEmpty()) {
			// A non-empty statusWhiteboard means we store description there for backward compatibility. (See discussion
			// in Task 422)
			managedTarget.setStatusWhiteboard(source.getStatusWhiteboard());
		}

		managedTarget.setKeywords(source.getKeywords());
		managedTarget.setKeywordses(source.getKeywordses());
		fillComments(managedTarget.getComments(), source.getComments());
		makeDescriptionTheEarliestComment(managedTarget);

		updateDependencies(managedTarget, managedTarget.getDependenciesesForBlocked(),
				source.getDependenciesesForBlocked());
		updateDependencies(managedTarget, managedTarget.getDependenciesesForDependson(),
				source.getDependenciesesForDependson());
		managedTarget.setCcs(source.getCcs());
		managedTarget.setCclistAccessible(source.getCclistAccessible());

		managedTarget.setResolution(source.getResolution());
		managedTarget.setEstimatedTime(source.getEstimatedTime());
		managedTarget.setRemainingTime(source.getRemainingTime());
		managedTarget.setSeverity(source.getSeverity());
		managedTarget.setStatus(source.getStatus());
		managedTarget.setPriority(source.getPriority());
		managedTarget.setTargetMilestone(source.getTargetMilestone());

		managedTarget.setComponent(source.getComponent());
		managedTarget.setProduct(source.getProduct());
		managedTarget.setAssignee(source.getAssignee());

		if (managedTarget.getDuplicatesByBugId() != null && source.getDuplicatesByBugId() != null) {
			managedTarget.getDuplicatesByBugId().setBugsByDupeOf(source.getDuplicatesByBugId().getBugsByDupeOf());
		} else {
			managedTarget.setDuplicatesByBugId(source.getDuplicatesByBugId());
		}
	}

	private static void updateDependencies(Task task, List<Dependency> dependencies, List<Dependency> newDependencies) {
		int sizehint = Math.max(newDependencies.size(), dependencies.size());
		if (sizehint == 0) {
			return;
		}
		Date deltaTs = new Date();

		List<Dependency> removed = new ArrayList<Dependency>(sizehint);
		removed.addAll(dependencies);
		removed.removeAll(newDependencies);
		List<Dependency> added = new ArrayList<Dependency>(sizehint);
		added.addAll(newDependencies);
		added.removeAll(dependencies);

		if (removed.isEmpty() && added.isEmpty()) {
			return;
		}

		for (Dependency dependency : removed) {
			dependency.getBugsByBlocked().setDeltaTs(deltaTs);
			dependency.getBugsByDependson().setDeltaTs(deltaTs);
		}

		for (Dependency dependency : added) {
			dependency.getBugsByBlocked().setDeltaTs(deltaTs);
			dependency.getBugsByDependson().setDeltaTs(deltaTs);
		}
		dependencies.clear();
		dependencies.addAll(newDependencies);
	}

	private static void fillComments(List<Comment> target, List<Comment> source) {
		// Handle the description
		if (target.isEmpty()) {
			// Can happen when updating task created from bugzilla.
			Comment descriptionComment = source.get(0);
			descriptionComment.convertLineDelimiters();
			target.add(descriptionComment);
		} else {
			Comment descriptionComment = target.get(0);
			descriptionComment.setThetext(source.get(0).getThetext());
			descriptionComment.setCreationTs(source.get(0).getCreationTs()); // This can change to always be the
																				// earliest.
			descriptionComment.convertLineDelimiters();
		}

		for (int i = 1; i < source.size(); i++) {
			Comment sourceComment = source.get(i);
			if (sourceComment.getId() == null) {
				sourceComment.convertLineDelimiters();
				target.add(sourceComment);
			}
		}
	}

	public static void fillManaged(Product managedProduct,
			com.tasktop.c2c.server.tasks.domain.Product domainProduct) {
		managedProduct.setName(domainProduct.getName());
		managedProduct.setDescription(domainProduct.getDescription());
		managedProduct.setIsactive(domainProduct.getIsActive());
		managedProduct.setDefaultmilestone(domainProduct.getDefaultMilestone().getValue());
	}

	public static void fillManaged(Component managedComponent,
			com.tasktop.c2c.server.tasks.domain.Component domainComponent, EntityManager entityManager) {
		// Fill in the modifiable fields from this domain object.
		managedComponent.setName(domainComponent.getName());
		managedComponent.setDescription(domainComponent.getDescription());

		Profile initialOwner = null;
		if (domainComponent.getInitialOwner() != null) {
			initialOwner = entityManager.find(Profile.class, domainComponent.getInitialOwner().getId());
		}
		managedComponent.setInitialOwner(initialOwner);
	}

	public static void fillManaged(Milestone managedMilestone,
			com.tasktop.c2c.server.tasks.domain.Milestone domainMilestone, EntityManager entityManager) {
		// Fill in the modifiable fields from this domain object.
		managedMilestone.setValue(domainMilestone.getValue());
		managedMilestone.setSortkey(domainMilestone.getSortkey());
	}

	public static SavedTaskQuery createManaged(com.tasktop.c2c.server.tasks.domain.SavedTaskQuery source) {
		if (source == null) {
			return null;
		}
		SavedTaskQuery query = new SavedTaskQuery();
		query.setName(source.getName());
		query.setQueryString(source.getQueryString());
		if (source.getDefaultSort() != null) {
			query.setSortField(source.getDefaultSort().getSortField());
			query.setSortOrder(source.getDefaultSort().getSortOrder());
		}
		return query;
	}

	public static void fillManaged(SavedTaskQuery managedQuery,
			com.tasktop.c2c.server.tasks.domain.SavedTaskQuery source) {

		managedQuery.setName(source.getName());
		managedQuery.setQueryString(source.getQueryString());
		if (source.getDefaultSort() != null) {
			managedQuery.setSortField(source.getDefaultSort().getSortField());
			managedQuery.setSortOrder(source.getDefaultSort().getSortOrder());
		}
	}
}
