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

import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.profile.web.client.presenter.person.ProjectPersonService;
import com.tasktop.c2c.server.tasks.client.presenters.TaskPresenter;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.Task;

/**
 * @author jhickey
 * 
 */
public interface TaskView extends IsWidget {

	void setPresenter(TaskPresenter presenter);

	void setPersonServices(ProjectPersonService personService, Person self);

	void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration);

	void setSelf(Person self);

	void setTask(Task task);

	void updateCommentView(Task task);

	void setProjectIdentifier(String projectIdentifier);

	void setCommentText(String text);

	boolean isDirty();

	void reEnterEditMode();

}
