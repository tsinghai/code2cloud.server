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
package com.tasktop.c2c.server.tasks.client.widgets;

import java.util.List;


import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.profile.web.client.presenter.person.ProjectPersonService;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.KeywordSuggestService;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

public interface AbstractEditTaskDisplay extends IsWidget, HasValue<Task> {

	/** Should be set before task, used to render watchers, comments .. */
	void setSelf(Person self);

	/** Should trigger save task. */
	HasClickHandlers getSaveClickHandlers();

	/** Should trigger cancel task. */
	HasClickHandlers getCancelClickHandlers();

	void setKeywordService(KeywordSuggestService keywordService);

	void setPersonService(ProjectPersonService personService);

	void addProductChangeHandler(ValueChangeHandler<Product> handler);

	void addComponentChangeHandler(ValueChangeHandler<Component> handler);

	void addOwnerChangeHandler(ValueChangeHandler<Person> handler);

	void setComponents(List<Component> components);

	void setMilestones(List<Milestone> milestones);

	void setSelectedMilestone(Milestone milestone);

	public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration);

	public void clearFoundInReleases();

	boolean isDirty();

	void setAssignee(TaskUserProfile toSet);

	/**
	 * @param projectIdentifier
	 */
	void setProjectIdentifier(String projectIdentifier);
}
