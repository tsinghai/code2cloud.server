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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.source;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.cloud.domain.ScmLocation;
import com.tasktop.c2c.server.cloud.domain.ScmType;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.common.web.client.view.errors.ErrorCabableDialogBox;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;

/**
 * @author Clint Morgan (Tasktop Technologies Inc.)
 */
public class NewExternalRepoDialog extends ErrorCabableDialogBox implements
		IProjectAdminSourceView<IProjectAdminSourceView.Presenter>, ErrorCapableView {

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, NewExternalRepoDialog> {
	}

	private static NewExternalRepoDialog instance;

	public static NewExternalRepoDialog getInstance(Presenter presenter) {
		if (instance == null) {
			instance = new NewExternalRepoDialog();
		}
		instance.setPresenter(presenter);
		return instance;
	}

	@UiField
	TextBox repositoryUrl;
	@UiField
	Button addButton;
	@UiField
	Button cancelButton;
	private Presenter presenter;

	public NewExternalRepoDialog() {
		super(false, true);
		setText("Add External Repository");
		setWidget(uiBinder.createAndBindUi(this));
		setAnimationEnabled(true); // Why not?
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
	public void show() {
		super.show();
		repositoryUrl.setText(null);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@UiHandler("cancelButton")
	void onCancel(ClickEvent event) {
		hide();
	}

	@UiHandler("addButton")
	void onAdd(ClickEvent event) {
		ScmRepository newRepo = new ScmRepository();
		newRepo.setUrl(repositoryUrl.getText());
		newRepo.setType(ScmType.GIT);
		newRepo.setScmLocation(ScmLocation.EXTERNAL);
		presenter.onCreateRepository(this, newRepo);

	}
}
