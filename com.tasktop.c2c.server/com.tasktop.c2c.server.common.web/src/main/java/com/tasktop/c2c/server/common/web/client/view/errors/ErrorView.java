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
package com.tasktop.c2c.server.common.web.client.view.errors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class ErrorView extends Composite{
	interface ErrorViewUiBinder extends UiBinder<HTMLPanel, ErrorView> {}

	private static ErrorView instance;

	public static ErrorView getInstance(ErrorType errorType) {
		if (instance == null) {
			instance = new ErrorView();
		}
		instance.setErrorType(errorType);
		return instance;
	}

	public static ErrorView getError404View() {
		return getInstance(ErrorType.ERROR404);
	}

	public static ErrorView getError500View() {
		return getInstance(ErrorType.ERROR500);
	}

	public static ErrorView getError503View() {
		return getInstance(ErrorType.ERROR503);
	}

	private static ErrorViewUiBinder ourUiBinder = GWT.create(ErrorViewUiBinder.class);
	@UiField
	Anchor mainPageAnchor;
	@UiField
	Anchor previousPageAnchor;
	@UiField
	Anchor supportTicketAnchor;
	@UiField
	Label errorDescription;
	@UiField
	DivElement errorImage;

	private ErrorType errorType;

	private ErrorView() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	private void setErrorType(ErrorType errorType) {
		if (this.errorType != null) {
			errorImage.removeClassName(this.errorType.getErrorImageClass());
		}
		errorImage.addClassName(errorType.getErrorImageClass());
		errorDescription.setText(errorType.getErrorDescription());
		this.errorType = errorType;
	}
}
