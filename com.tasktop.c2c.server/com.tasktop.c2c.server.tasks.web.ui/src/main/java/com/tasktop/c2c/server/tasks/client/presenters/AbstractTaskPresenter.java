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

import net.customware.gwt.dispatch.client.DispatchAsync;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.common.web.client.presenter.AbstractPresenter;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.profile.web.client.AppState;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.tasks.client.widgets.TaskMessages;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractTaskPresenter extends AbstractPresenter {

	protected TaskMessages taskMessages = GWT.create(TaskMessages.class);

	protected String projectIdentifier;

	/**
	 * @param view
	 */
	protected AbstractTaskPresenter(IsWidget view) {
		super(view);
	}

	protected DispatchAsync getDispatchService() {
		return CommonGinjector.get.instance().getDispatchService();
	}

	protected AppState getAppState() {
		return ProfileGinjector.get.instance().getAppState();
	}

	/**
	 * @return the projectIdentifier
	 */
	public String getProjectIdentifier() {
		return projectIdentifier;
	}

	/**
	 * @param projectIdentifier
	 *            the projectIdentifier to set
	 */
	public void setProjectIdentifier(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
	}
}
