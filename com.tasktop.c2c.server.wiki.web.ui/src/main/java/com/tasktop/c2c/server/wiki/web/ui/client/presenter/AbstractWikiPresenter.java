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
package com.tasktop.c2c.server.wiki.web.ui.client.presenter;

import net.customware.gwt.dispatch.client.DispatchAsync;


import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.common.web.client.presenter.AbstractPresenter;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractWikiPresenter extends AbstractPresenter {

	protected String projectIdentifier;

	/**
	 * @param view
	 */
	protected AbstractWikiPresenter(IsWidget view) {
		super(view);
	}

	/**
	 * @return the projectIdentifier
	 */
	public String getProjectIdentifier() {
		return projectIdentifier;
	}

	protected DispatchAsync getDispatchService() {
		return CommonGinjector.get.instance().getDispatchService();
	}

	/**
	 * @param projectIdentifier
	 *            the projectIdentifier to set
	 */
	public void setProjectIdentifier(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
	}
}
