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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.account.authentication;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.common.web.client.view.errors.ErrorCabableDialogBox;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKey;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.IAccountView;

public class EditSshKeyDialog extends ErrorCabableDialogBox implements Editor<SshPublicKey>,
		IAccountView<IAccountView.SshKeyPresenter>, ErrorCapableView {

	interface SshKeyDialogUiBinder extends UiBinder<HTMLPanel, EditSshKeyDialog> {
	}

	interface Driver extends SimpleBeanEditorDriver<SshPublicKey, EditSshKeyDialog> {
	}

	private static EditSshKeyDialog instance;

	public static EditSshKeyDialog getInstance() {
		if (instance == null) {
			instance = new EditSshKeyDialog();
		}
		return instance;
	}

	private static SshKeyDialogUiBinder ourUiBinder = GWT.create(SshKeyDialogUiBinder.class);
	@UiField
	@Path("name")
	TextBox sshKeyNameField;
	@UiField
	@Path("keyText")
	TextArea sshKeyField;
	@UiField
	Button cancelButton;
	@UiField
	Button saveButton;

	private Driver driver = GWT.create(Driver.class);
	private SshKeyPresenter presenter;

	private EditSshKeyDialog() {
		setWidget(ourUiBinder.createAndBindUi(this));
		driver.initialize(this);
		setAnimationEnabled(true);
		setGlassEnabled(true);
		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				if (isShowing()) {
					center();
				}
			}
		});
	}

	@Override
	public void setPresenter(SshKeyPresenter presenter) {
		this.presenter = presenter;
		driver.edit(presenter.getSelectedSshKey());
		boolean isNew = presenter.getSelectedSshKey().getId() == null;
		if (isNew) {
			getCaption().setHTML(SafeHtmlUtils.fromSafeConstant("<h2>Add SSH Key</h2>"));
		} else {
			getCaption().setHTML(SafeHtmlUtils.fromSafeConstant("<h2>Edit SSH Key</h2>"));
		}
		sshKeyField.setEnabled(isNew);
		center();
	}

	@UiHandler("cancelButton")
	void onCancel(ClickEvent event) {
		sshKeyField.setText(null);
		sshKeyNameField.setText(null);
		super.hide();
	}

	@UiHandler("saveButton")
	void onSave(ClickEvent event) {
		driver.flush();
		presenter.saveSshKey(this);
	}

}
