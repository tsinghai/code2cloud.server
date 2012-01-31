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
import java.util.List;

public class Product extends AbstractDomainObject implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String description;
	private Boolean isActive;
	private Milestone defaultMilestone;
	private Component defaultComponent;
	private List<Milestone> milestones;
	private List<Component> components;
	private List<String> releaseTags;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Milestone getDefaultMilestone() {
		return defaultMilestone;
	}

	public void setDefaultMilestone(Milestone defaultMilestone) {
		this.defaultMilestone = defaultMilestone;
	}

	public Component getDefaultComponent() {
		return defaultComponent;
	}

	public void setDefaultComponent(Component defaultComponent) {
		this.defaultComponent = defaultComponent;
	}

	public void setMilestones(List<Milestone> milestones) {
		this.milestones = milestones;
	}

	public List<Milestone> getMilestones() {
		return milestones;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	public List<Component> getComponents() {
		return components;
	}

	@Override
	public String toString() {
		String str = getName();
		if (str == null || str.trim().length() == 0) {
			// If there's no name, then still return a string - if we don't do this, then Spring's validation system
			// blows up since it does object.toString(), so even if there's a valid non-null value in a field we still
			// get a validation failure.
			return "no name, ID: " + getId();
		} else {
			return str;
		}
	}

	/**
	 * @return the releaseTags
	 */
	public List<String> getReleaseTags() {
		return releaseTags;
	}

	/**
	 * @param releaseTags
	 *            the releaseTags to set
	 */
	public void setReleaseTags(List<String> releaseTags) {
		this.releaseTags = releaseTags;
	}

}
