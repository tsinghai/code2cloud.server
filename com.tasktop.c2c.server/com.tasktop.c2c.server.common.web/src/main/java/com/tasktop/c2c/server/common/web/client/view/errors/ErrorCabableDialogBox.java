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

import java.util.Collections;
import java.util.List;


import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;

/**
 * @author Clint Morgan (Tasktop Technologies Inc.)
 */
public class ErrorCabableDialogBox extends DialogBox implements ErrorCapableView {

	@UiField
	public Panel errorPanel;

	/**
	 *
	 */
	public ErrorCabableDialogBox() {
		super();
	}

	/**
	 * @param autoHide
	 */
	public ErrorCabableDialogBox(boolean autoHide) {
		super(autoHide);
	}

	/**
	 * @param captionWidget
	 */
	public ErrorCabableDialogBox(Caption captionWidget) {
		super(captionWidget);
	}

	/**
	 * @param autoHide
	 * @param modal
	 */
	public ErrorCabableDialogBox(boolean autoHide, boolean modal) {
		super(autoHide, modal);
	}

	/**
	 * @param autoHide
	 * @param modal
	 * @param captionWidget
	 */
	public ErrorCabableDialogBox(boolean autoHide, boolean modal, Caption captionWidget) {
		super(autoHide, modal, captionWidget);
	}

	@Override
	public void displayError(String message) {
		displayErrors(Collections.singletonList(message));
	}

	@Override
	public void displayErrors(List<String> messages) {
		errorPanel.clear();
		for (String message : messages) {
			addMessage(createLabel(message, "error"));
		}
	}

	@Override
	public void clearErrors() {
		errorPanel.clear();
		errorPanel.setVisible(false);
	}

	private void addMessage(Widget curMessage) {
		errorPanel.add(curMessage);
		errorPanel.setVisible(true);
	}

	private HTML createLabel(String message, String messageClass) {
		HTML retHTML = new HTML("<span></span>" + message);
		retHTML.setStyleName(messageClass + " misc-icon");
		return retHTML;
	}

	@Override
	public void show() {
		clearErrors();
		super.show();
	}

}
