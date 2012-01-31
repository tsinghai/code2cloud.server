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

import com.tasktop.c2c.server.profile.web.ui.client.place.AppSectionPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.AppSectionView;

public class AppSectionPresenter extends AbstractProfilePresenter {

	public AppSectionPresenter(AppSectionView view, AppSectionPlace place) {
		super(view);
		view.setSectionToShow(place.getSectionToShow());
	}

	@Override
	protected void bind() {
	}
}
