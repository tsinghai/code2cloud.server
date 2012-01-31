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
package com.tasktop.c2c.server.profile.web.ui.client.presenter;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.common.web.client.presenter.AbstractPresenter;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.profile.web.client.AppState;
import com.tasktop.c2c.server.profile.web.client.ProfileServiceAsync;
import com.tasktop.c2c.server.profile.web.ui.client.BuildServiceAsync;
import com.tasktop.c2c.server.profile.web.ui.client.DeploymentServiceAsync;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;

public abstract class AbstractProfilePresenter extends AbstractPresenter {

	protected AbstractProfilePresenter(IsWidget view) {
		super(view);
	}

	public AppState getAppState() {
		return ProfileEntryPoint.getInstance().getAppState();
	}

	public ProfileServiceAsync getProfileService() {
		return ProfileEntryPoint.getInstance().getProfileService();
	}

	public BuildServiceAsync getBuildService() {
		return ProfileEntryPoint.getInstance().getBuildService();
	}

	public DeploymentServiceAsync getDeploymentService() {
		return ProfileEntryPoint.getInstance().getDeploymentService();
	}

	protected DispatchAsync getDispatchService() {
		return CommonGinjector.get.instance().getDispatchService();
	}

}
