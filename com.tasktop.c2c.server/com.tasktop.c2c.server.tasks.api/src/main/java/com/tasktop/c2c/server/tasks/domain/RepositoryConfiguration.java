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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Container class for Task Repository configuration
 * 
 * @author Clint Morgan (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class RepositoryConfiguration implements Serializable {
	private static final long serialVersionUID = 1L;
	private String url;
	private List<TaskStatus> statuses;
	private TaskStatus defaultStatus;
	private List<Milestone> milestones;
	private List<Priority> priorities;
	private Priority defaultPriority;
	private List<TaskSeverity> severities;
	private TaskSeverity defaultSeverity;
	private List<TaskUserProfile> users;
	private List<Product> products;
	private Product defaultProduct;
	private List<Component> components;
	private List<String> taskTypes;
	private String defaultType;
	private List<Iteration> iterations;
	private Iteration defaultIteration;

	private List<TaskResolution> resolutions;
	private TaskResolution defaultResolution;

	private List<FieldDescriptor> customFields;

	private List<Keyword> keywords;

	private List<StateTransition> stateTransitions;

	private List<SavedTaskQuery> savedTaskQueries;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<TaskStatus> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<TaskStatus> statuses) {
		this.statuses = statuses;
	}

	public List<Milestone> getMilestones() {
		return milestones;
	}

	public List<Milestone> getMilestones(Product product) {

		List<Milestone> productMilestones = new ArrayList<Milestone>();
		for (Milestone milestone : milestones) {
			if (milestone.getProduct().equals(product)) {
				productMilestones.add(milestone);
			}
		}
		return productMilestones;
	}

	public void setMilestones(List<Milestone> milestones) {
		this.milestones = milestones;
	}

	public List<Priority> getPriorities() {
		return priorities;
	}

	public void setPriorities(List<Priority> priorities) {
		this.priorities = priorities;
	}

	public List<TaskSeverity> getSeverities() {
		return severities;
	}

	public void setSeverities(List<TaskSeverity> severities) {
		this.severities = severities;
	}

	public List<TaskUserProfile> getUsers() {
		return users;
	}

	public void setUsers(List<TaskUserProfile> users) {
		this.users = users;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public List<Component> getComponents() {
		return components;
	}

	public List<Component> getComponents(Product product) {

		List<Component> productComponents = new ArrayList<Component>();
		for (Component component : components) {
			if (component.getProduct().getId().equals(product.getId())) {
				productComponents.add(component);
			}
		}
		return productComponents;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	public List<String> getTaskTypes() {
		return this.taskTypes;
	}

	public void setTaskTypes(List<String> values) {
		this.taskTypes = values;
	}

	public String getDefaultType() {
		return defaultType;
	}

	public void setDefaultType(String defaultType) {
		this.defaultType = defaultType;
	}

	public List<Iteration> getIterations() {
		return iterations;
	}

	public void setIterations(List<Iteration> iterations) {
		this.iterations = iterations;
	}

	public List<Iteration> getActiveIterations() {
		if (iterations == null) {
			return null;
		}
		ArrayList<Iteration> result = new ArrayList<Iteration>(iterations.size());
		for (Iteration it : iterations) {
			if (it.getIsActive()) {
				result.add(it);
			}
		}
		return result;
	}

	public Iteration getDefaultIteration() {
		return defaultIteration;
	}

	public void setDefaultIteration(Iteration defaultIteration) {
		this.defaultIteration = defaultIteration;
	}

	public TaskStatus getDefaultStatus() {
		return defaultStatus;
	}

	public void setDefaultStatus(TaskStatus defaultStatus) {
		this.defaultStatus = defaultStatus;
	}

	public Priority getDefaultPriority() {
		return defaultPriority;
	}

	public void setDefaultPriority(Priority defaultPriority) {
		this.defaultPriority = defaultPriority;
	}

	public TaskSeverity getDefaultSeverity() {
		return defaultSeverity;
	}

	public void setDefaultSeverity(TaskSeverity defaultSeverity) {
		this.defaultSeverity = defaultSeverity;
	}

	public Product getDefaultProduct() {
		return defaultProduct;
	}

	public void setDefaultProduct(Product defaultProduct) {
		this.defaultProduct = defaultProduct;
	}

	public List<TaskResolution> getResolutions() {
		return resolutions;
	}

	public void setResolutions(List<TaskResolution> resolutions) {
		this.resolutions = resolutions;
	}

	public TaskResolution getDefaultResolution() {
		return defaultResolution;
	}

	public void setDefaultResolution(TaskResolution defaultResolution) {
		this.defaultResolution = defaultResolution;
	}

	public List<FieldDescriptor> getCustomFields() {
		return customFields;
	}

	public void setCustomFields(List<FieldDescriptor> customFields) {
		this.customFields = customFields;
	}

	public List<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<Keyword> keywords) {
		this.keywords = keywords;
	}

	public List<StateTransition> getStateTransitions() {
		return stateTransitions;
	}

	public void setStateTransitions(List<StateTransition> stateTransitions) {
		this.stateTransitions = stateTransitions;
	}

	public List<TaskStatus> computeValidStatuses(TaskStatus originalStatus) {
		List<TaskStatus> statuses = new ArrayList<TaskStatus>(getStatuses().size());

		Set<String> newStatuses = new HashSet<String>();
		for (StateTransition transition : getStateTransitions()) {
			if (transition.getInitialStatus() == null) {
				if (originalStatus == null) {
					newStatuses.add(transition.getNewStatus());
				}
			} else if (originalStatus != null && transition.getInitialStatus().equals(originalStatus.getValue())) {
				newStatuses.add(transition.getNewStatus());
			}
		}
		if (originalStatus != null) {
			newStatuses.add(originalStatus.getValue());
		}

		for (TaskStatus status : getStatuses()) {
			if (newStatuses.contains(status.getValue())) {
				statuses.add(status);
			}
		}
		return statuses;
	}

	/**
	 * Compute the valid {@link TaskResolution} values based on a given {@link TaskStatus}
	 * 
	 * @param status
	 *            the {@link TaskStatus}
	 * @return
	 */
	public List<TaskResolution> computeValidResolutions(TaskStatus status) {
		if (!status.isOpen()) {
			List<TaskResolution> result = new ArrayList<TaskResolution>(getResolutions().size() - 1);
			for (TaskResolution r : getResolutions()) {
				if (r.getValue().length() != 0) {
					result.add(r);
				}
			}
			return result;
		}

		for (TaskResolution r : getResolutions()) {
			if (r.getValue().length() != 0) {
				return Collections.singletonList(r);
			}
		}

		return Collections.emptyList();
	}

	/**
	 * @return the savedTaskQueries
	 */
	public List<SavedTaskQuery> getSavedTaskQueries() {
		return savedTaskQueries;
	}

	/**
	 * @param savedTaskQueries
	 *            the savedTaskQueries to set
	 */
	public void setSavedTaskQueries(List<SavedTaskQuery> savedTaskQueries) {
		this.savedTaskQueries = savedTaskQueries;
	}

	public List<String> getReleaseTags(Product product) {
		for (Product p : products) {
			if (p.equals(product)) {
				return p.getReleaseTags();
			}
		}
		return Collections.emptyList();
	}

}
