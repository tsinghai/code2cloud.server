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
package com.tasktop.c2c.server.profile.web.ui.client.view.components;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.Format;
import com.tasktop.c2c.server.profile.domain.activity.BuildActivity;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails.BuildResult;
import com.tasktop.c2c.server.profile.web.ui.client.widgets.build.BuildResources;

public class BuildActivityRow extends Composite {
	interface Binder extends UiBinder<Widget, BuildActivityRow> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);
	public static BuildResources buildResources = GWT.create(BuildResources.class);

	public BuildActivityRow(BuildActivity activity) {
		initWidget(uiBinder.createAndBindUi(this));
		render(activity);
	}

	@UiField
	Anchor buildAnchor;
	@UiField
	Anchor jobAnchor;
	@UiField
	Label dateLabel;
	@UiField
	Label resultLabel;
	@UiField
	Label descriptionLabel;

	private void render(BuildActivity buildActivity) {
		dateLabel.setText(Format.stringValueTime(buildActivity.getActivityDate()));
		resultLabel.setText(getBuildText(buildActivity.getBuildDetails()));
		buildAnchor.setText("Build " + buildActivity.getBuildDetails().getNumber());
		buildAnchor.setHref(buildActivity.getBuildDetails().getUrl());
		jobAnchor.setText(buildActivity.getJobSummary().getName());
		jobAnchor.setHref(buildActivity.getJobSummary().getUrl());
		String descriptionText = buildActivity.getBuildDetails().getCause() == null ? "" : buildActivity
				.getBuildDetails().getCause() + ".";
		if (!buildActivity.getBuildDetails().getBuilding() && buildActivity.getBuildDetails().getDuration() != null
				&& buildActivity.getBuildDetails().getResult() != null) {
			descriptionText = descriptionText + " Build took "
					+ millisecondsToString(buildActivity.getBuildDetails().getDuration()) + ".";
		}
		descriptionLabel.setText(descriptionText);
		setParentStyleFromResult(buildActivity.getBuildDetails().getResult());
	}

	private static final double MILLISECONDS_IN_MINUTE = 1000 * 60;
	private static final double MILLISECONDS_IN_SECOND = 1000;

	private String millisecondsToString(Long duration) {
		if (duration < MILLISECONDS_IN_MINUTE) {
			return Math.round((double) duration / MILLISECONDS_IN_SECOND) + " seconds";
		}
		return Math.round((double) duration / MILLISECONDS_IN_MINUTE) + " minutes";
	}

	// FIXME UGLY STYELING CONSTANTS
	private void setParentStyleFromResult(BuildResult result) {
		Element toStyle = buildAnchor.getElement().getParentElement().getParentElement();
		if (result == null) { // FIXME pending
			toStyle.addClassName("build-cancelled"); // Misspell intentional to match styling
			return;
		}

		switch (result) {
		case SUCCESS:
			toStyle.addClassName("build-successes");
			return;
		case FAILURE:
			toStyle.addClassName("build-failures");
			return;
		case UNSTABLE:
			toStyle.addClassName("build-unstable");
			return;
		case ABORTED:
		default:
			toStyle.addClassName("build-cancelled"); // Misspell intentional to match styling
			return;
		}
	}

	private String getBuildText(BuildDetails details) {
		if (details.getBuilding() || details.getResult() == null) {
			return "is pending.";
		}
		return "resulted in " + details.getResult().name().toLowerCase() + ".";
	}

}
