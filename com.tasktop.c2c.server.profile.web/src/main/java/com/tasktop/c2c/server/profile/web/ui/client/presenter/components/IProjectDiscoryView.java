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
package com.tasktop.c2c.server.profile.web.ui.client.presenter.components;

import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;

public interface IProjectDiscoryView {
	void setPresenter(IProjectDiscoryView.Presenter presenter);

	public static interface Presenter {
		QueryResult<Project> getCurrentResult();

		void setProjectRelationship(ProjectRelationship projectRelationship);

		ProjectRelationship getProjectRelationship();

		QueryRequest getQueryRequest();

		void setQueryRequest(QueryRequest queryRequest);

		String getCurrentQuery();

	}
}
