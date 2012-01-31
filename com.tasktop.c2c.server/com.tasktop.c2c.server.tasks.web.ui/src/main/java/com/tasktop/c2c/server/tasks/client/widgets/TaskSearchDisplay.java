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

import java.util.Map;


import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.profile.web.client.presenter.person.ProjectPersonService;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.KeywordSuggestService;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;

public interface TaskSearchDisplay {

	void setPersonService(ProjectPersonService personService);

	void setKeywordService(KeywordSuggestService keywordService);

	void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration);

	void createUi();

	Widget getWidget();

	void setSearchClickHandler(ClickHandler handler);

	void setSaveClickHandler(ClickHandler handler);

	void setCancelClickHandler(ClickHandler handler);

	void setSelf(Person self);

	Map<FieldDescriptor, Widget> getFieldDescriptorToWidget();

	void setEditQuery(SavedTaskQuery editQuery);
}
