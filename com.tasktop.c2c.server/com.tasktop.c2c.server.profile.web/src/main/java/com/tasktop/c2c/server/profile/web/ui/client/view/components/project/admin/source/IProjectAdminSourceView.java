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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.source;


import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;

import java.util.List;

public interface IProjectAdminSourceView<P extends IProjectAdminSourceView.Presenter> {

	void setPresenter(P presenter);

	public static interface Presenter {
		void onDeleteRepository(Long repositoryId);

		void onCreateRepository(ErrorCapableView view, ScmRepository repository);

		String getRepoBaseUrl();

		List<ScmRepository> getRepositories();

		Project getProject();
	}
}
