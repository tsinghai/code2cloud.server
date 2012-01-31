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
package com.tasktop.c2c.server.tasks.client.widgets.admin.iterations;

import java.util.List;


import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.tasks.domain.Iteration;

public interface IIterationsAdminView extends IsWidget {

	void setPresenterAndUpdateDisplay(Presenter presenter);

	public static interface Presenter {
		List<Iteration> getIterations(boolean hideInactive);

		void saveIteration(Iteration iteration);

		void newIteration();
	}

}
