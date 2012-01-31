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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;

public class ArtifactReadOnlyView extends Composite {
	interface Binder extends UiBinder<Widget, ArtifactReadOnlyView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Panel noDeploymentPanel;
	@UiField
	Panel hasDeploymentPanel;
	@UiField
	Label lastDeploymentDate;
	@UiField
	Label artifactTypeLabel;
	@UiField
	Label jobLabel;
	@UiField
	Label buildLabel;
	@UiField
	Label artifactLabel;

	public ArtifactReadOnlyView() {
		initWidget(uiBinder.createAndBindUi(this));

	}

	public void setValue(DeploymentConfiguration deployment) {
		if (deployment.getBuildJobName() == null) {
			noDeploymentPanel.setVisible(true);
			hasDeploymentPanel.setVisible(false);
		} else {
			noDeploymentPanel.setVisible(false);
			hasDeploymentPanel.setVisible(true);
		}

		if (deployment.getDeploymentType() != null) {
			switch (deployment.getDeploymentType()) {
			case AUTOMATED:
				artifactTypeLabel.setText("Automatic");
				break;
			case MANUAL:
				artifactTypeLabel.setText("Manual");
				break;
			}
		} else {
			artifactTypeLabel.setText("");
		}

		jobLabel.setText(deployment.getBuildJobName());
		buildLabel.setText(deployment.getBuildJobNumber());
		artifactLabel.setText(deployment.getBuildArtifactPath());
		if (deployment.getLastDeploymentDate() != null) {
			lastDeploymentDate.setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(
					deployment.getLastDeploymentDate()));
		} else {
			lastDeploymentDate.setText("Not yet");
		}
	}

}
