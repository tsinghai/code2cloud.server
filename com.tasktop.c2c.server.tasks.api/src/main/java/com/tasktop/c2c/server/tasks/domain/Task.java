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
package com.tasktop.c2c.server.tasks.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.tasktop.c2c.server.common.service.domain.ToStringCreator;

public class Task extends TaskHandle implements Comparable<Task> {
	private static final long serialVersionUID = 1L;
	private String foundInRelease;
	private Date creationDate;
	private Date modificationDate;
	private String shortDescription;
	private String description;
	private String wikiRenderedDescription;
	private List<Keyword> keywords = new ArrayList<Keyword>();
	private BigDecimal estimatedTime;
	private BigDecimal sumOfSubtasksEstimatedTime;
	private BigDecimal remainingTime;
	private Date deadline;
	private String url;
	private String taskType;
	private Iteration iteration;
	private List<ExternalTaskRelation> externalTaskRelations = new ArrayList<ExternalTaskRelation>();

	private TaskUserProfile reporter;
	private TaskUserProfile assignee;
	private List<TaskUserProfile> watchers;
	private TaskSeverity severity;
	private TaskStatus status;
	private TaskResolution resolution;
	private Priority priority;
	private Milestone milestone;
	private Product product;
	private Component component;

	private List<Task> blocksTasks;
	private List<Task> subTasks;

	private List<WorkLog> workLogs = new ArrayList<WorkLog>();
	private BigDecimal sumOfSubtasksTimeSpent;

	private List<Attachment> attachments;

	private Task duplicateOf;
	private List<Task> duplicates;

	private Map<String, String> customFields;

	@JsonIgnore
	public TaskHandle getTaskHandle() {
		return new TaskHandle(getId(), getVersion());
	}

	@JsonIgnore
	public void setTaskHandle(TaskHandle taskHandle) {
		if (!taskHandle.getId().equals(getId())) {
			throw new IllegalArgumentException("Task handle is not for this task");
		}
		setVersion(taskHandle.getVersion());
		setModificationDate(new Date(Long.parseLong(taskHandle.getVersion())));
	}

	public Milestone getMilestone() {
		return milestone;
	}

	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}

	private List<Comment> comments;

	public TaskSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(TaskSeverity severity) {
		this.severity = severity;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public TaskResolution getResolution() {
		return resolution;
	}

	public void setResolution(TaskResolution resolution) {
		this.resolution = resolution;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDesc) {
		this.shortDescription = shortDesc;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<Keyword> keywords) {
		this.keywords = keywords;
	}

	public BigDecimal getEstimatedTime() {
		return estimatedTime;
	}

	public void setEstimatedTime(BigDecimal estimatedTime) {
		this.estimatedTime = estimatedTime;
	}

	public BigDecimal getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(BigDecimal remainingTime) {
		this.remainingTime = remainingTime;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public TaskUserProfile getReporter() {
		return reporter;
	}

	public void setReporter(TaskUserProfile reporter) {
		this.reporter = reporter;
	}

	public TaskUserProfile getAssignee() {
		return assignee;
	}

	public void setAssignee(TaskUserProfile assignee) {
		this.assignee = assignee;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Component getComponent() {
		return component;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	@JsonIgnore
	public Task getParentTask() {
		if (blocksTasks != null && !blocksTasks.isEmpty()) {
			return blocksTasks.get(0);
		}
		return null;
	}

	@JsonIgnore
	public void setParentTask(Task parentTask) {
		blocksTasks = new ArrayList<Task>();
		if (parentTask != null) {
			blocksTasks.add(parentTask);
		}
	}

	public List<Task> getSubTasks() {
		return subTasks;
	}

	public void setSubTasks(List<Task> subTasks) {
		this.subTasks = subTasks;
	}

	public List<TaskUserProfile> getWatchers() {
		return watchers;
	}

	public void setWatchers(List<TaskUserProfile> watchers) {
		this.watchers = watchers;
	}

	public List<Task> getBlocksTasks() {
		return blocksTasks;
	}

	public void setBlocksTasks(List<Task> blocksTasks) {
		this.blocksTasks = blocksTasks;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public Task getDuplicateOf() {
		return duplicateOf;
	}

	public void setDuplicateOf(Task duplicateOf) {
		this.duplicateOf = duplicateOf;
	}

	public List<Task> getDuplicates() {
		return duplicates;
	}

	public void setDuplicates(List<Task> duplicates) {
		this.duplicates = duplicates;
	}

	public void addComment(String text) {
		Comment comment = new Comment();
		comment.setCommentText(text);
		List<Comment> comments = getComments();
		if (comments == null)
			comments = new ArrayList<Comment>();
		comments.add(comment);
		setComments(comments);
	}

	/**
	 * Tasks must be comparable to be used with GWT MultiSelectionModel. Compares by {@link #getId()}.
	 */
	public int compareTo(Task other) {
		if (other == this) {
			return 0;
		}
		return getId().compareTo(other.getId());
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getCustomFields() {
		return customFields;
	}

	public void setCustomFields(Map<String, String> customFields) {
		this.customFields = customFields;
	}

	public String getTaskType() {
		return this.taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public Iteration getIteration() {
		return this.iteration;
	}

	public void setIteration(Iteration iteration) {
		this.iteration = iteration;
	}

	public List<ExternalTaskRelation> getExternalTaskRelations() {
		return externalTaskRelations;
	}

	public void setExternalTaskRelations(List<ExternalTaskRelation> externalTaskRelations) {
		this.externalTaskRelations = externalTaskRelations;
	}

	@Override
	public String toString() {
		ToStringCreator result = new ToStringCreator(this);
		result.append("id", super.getId());
		result.append("version", super.getVersion());
		return result.toString();
	}

	public void setWikiRenderedDescription(String wikiRenderedDescription) {
		this.wikiRenderedDescription = wikiRenderedDescription;
	}

	public String getWikiRenderedDescription() {
		return wikiRenderedDescription;
	}

	public String getFoundInRelease() {
		return foundInRelease;
	}

	public void setFoundInRelease(String foundInRelease) {
		this.foundInRelease = foundInRelease;
	}

	public List<WorkLog> getWorkLogs() {
		return workLogs;
	}

	public void setWorkLogs(List<WorkLog> workLogs) {
		this.workLogs = workLogs;
	}

	/**
	 * @return the sumOfSubtasksEstimatedTime
	 */
	public BigDecimal getSumOfSubtasksEstimatedTime() {
		return sumOfSubtasksEstimatedTime;
	}

	/**
	 * @param sumOfSubtasksEstimatedTime
	 *            the sumOfSubtasksEstimatedTime to set
	 */
	public void setSumOfSubtasksEstimatedTime(BigDecimal sumOfSubtasksEstimatedTime) {
		this.sumOfSubtasksEstimatedTime = sumOfSubtasksEstimatedTime;
	}

	/**
	 * @return the sumOfSubtasksTimeSpent
	 */
	public BigDecimal getSumOfSubtasksTimeSpent() {
		return sumOfSubtasksTimeSpent;
	}

	/**
	 * @param sumOfSubtasksTimeSpent
	 *            the sumOfSubtasksTimeSpent to set
	 */
	public void setSumOfSubtasksTimeSpent(BigDecimal sumOfSubtasksTimeSpent) {
		this.sumOfSubtasksTimeSpent = sumOfSubtasksTimeSpent;
	}

}
