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
package com.tasktop.c2c.server.tasks.client.presenters;

import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;

public interface ITaskListView {
	public static interface Presenter {
		void doSaveCurrentQuery(String newQueryName);

		void doEditSelectedQuery();

		void doQuery(PredefinedTaskQuery query);

		void doQuery(SavedTaskQuery query);

		void doEditQuery(SavedTaskQuery query);

		void doDeleteQuery(SavedTaskQuery query);

		boolean canEditQuery();

		boolean canCreateQuery();
	}

	void setPresenter(Presenter presenter);
}
