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
package com.tasktop.c2c.server.tasks.client.widgets.admin.keywords;

import java.util.List;


import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.tasks.domain.Keyword;

public interface IProjectAdminKeywordsView<T extends IProjectAdminKeywordsView.Presenter> extends IsWidget {

	void setPresenter(T presenter);

	public static interface Presenter {
	}

	public static interface ProjectAdminKeywordsPresenter extends ProjectAdminKeywordsDisplayPresenter,
			ProjectAdminKeywordsEditPresenter, ProjectAdminKeywordsMenuPresenter {
	}

	public static interface ProjectAdminKeywordsDisplayPresenter extends Presenter {
		void onDeleteKeyword();

		void onEditKeyword();

		Keyword getSelectedKeyword();
	}

	public static interface ProjectAdminKeywordsEditPresenter extends Presenter {
		void onSaveKeyword(ErrorCapableView errorView);

		void onEditCancel();

		boolean isEditing();

		Keyword getSelectedKeyword();
	}

	public static interface ProjectAdminKeywordsMenuPresenter extends Presenter {
		List<Keyword> getKeywords();

		void selectKeyword(Keyword keyword);

		Keyword getSelectedKeyword();

		void addKeyword();

		boolean isEditing();
	}

}
