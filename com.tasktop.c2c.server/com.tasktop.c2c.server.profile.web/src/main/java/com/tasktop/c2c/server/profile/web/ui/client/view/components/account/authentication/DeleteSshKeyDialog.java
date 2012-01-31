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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKey;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.IAccountView;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class DeleteSshKeyDialog extends DialogBox implements Editor<SshPublicKey>,
		IAccountView<IAccountView.SshKeyPresenter> {
	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, DeleteSshKeyDialog> {
	}

	private static DeleteSshKeyDialog instance;

	public static DeleteSshKeyDialog getInstance() {
		if (instance == null) {
			instance = new DeleteSshKeyDialog();
		}
		return instance;
	}

	@UiField
	Button okButton;
	@UiField
	Button cancelButton;
	@UiField
	Label keyLabel;
	private SshKeyPresenter presenter;

	public DeleteSshKeyDialog() {
		super(false, true);
		setWidget(uiBinder.createAndBindUi(this));
		setText("Delete SSH Key");
		setAnimationEnabled(true);
		setGlassEnabled(true);

		okButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.deleteSshKey(presenter.getSelectedSshKey().getId());
				hide();
			}
		});

		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});

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
		// driver.edit(presenter.getSelectedSshKey());
		getCaption().setHTML(SafeHtmlUtils.fromSafeConstant("<h2>Remove SSH Key</h2>"));
		keyLabel.setText(presenter.getSelectedSshKey().getName() + " ("
				+ presenter.getSelectedSshKey().getFingerprint() + ")");
		center();
	}

}
