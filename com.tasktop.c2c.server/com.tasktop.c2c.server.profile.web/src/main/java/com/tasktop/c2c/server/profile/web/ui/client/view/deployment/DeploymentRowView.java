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
package com.tasktop.c2c.server.profile.web.ui.client.view.deployment;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class DeploymentRowView extends Composite {

	private static final String SELECTED_SYTLE = "selected";

	interface Binder extends UiBinder<Widget, DeploymentRowView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Anchor anchor;

	private final DeploymentConfiguration deployment;

	public DeploymentRowView(DeploymentConfiguration deployment) {
		initWidget(uiBinder.createAndBindUi(this));
		this.deployment = deployment;
		anchor.setHTML("<span></span>" + deployment.getName());
		if (deployment.getStatus() != null && deployment.getStatus().getResult() != null) {
			switch (deployment.getStatus().getResult()) {
			case STARTED:
			case UPDATING:
				anchor.addStyleName("misc-icon running");
				break;
			case STOPPED:
				anchor.addStyleName("misc-icon stopped");
				break;
			}
		} else {
			anchor.addStyleName("misc-icon error");
		}
	}

	public void setSelected(boolean selected) {
		if (selected) {
			anchor.addStyleName(SELECTED_SYTLE);
		} else {
			anchor.removeStyleName(SELECTED_SYTLE);
		}
	}

	/**
	 * @return the deployment
	 */
	public DeploymentConfiguration getDeployment() {
		return deployment;
	}
}
