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
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 */
public class ConfirmDeleteRepoDialog extends DialogBox implements
		IProjectAdminSourceView<IProjectAdminSourceView.Presenter> {

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, ConfirmDeleteRepoDialog> {
	}

	@UiField
	Button deleteButton;
	@UiField
	Button cancelButton;
	@UiField
	Label name;

	private Presenter presenter;
	private ScmRepository repository;

	private static ConfirmDeleteRepoDialog instance;

	public static ConfirmDeleteRepoDialog getInstance(Presenter presenter, ScmRepository repository) {
		if (instance == null) {
			instance = new ConfirmDeleteRepoDialog();
		}
		instance.setPresenter(presenter);
		instance.setRepository(repository);
		return instance;
	}

	private void setRepository(ScmRepository repository) {
		this.repository = repository;
		name.setText(repository.getName());
	}

	private ConfirmDeleteRepoDialog() {
		super(false, true);
		setText("Confirm Delete");
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@UiHandler("cancelButton")
	void onCancel(ClickEvent event) {
		hide();
	}

	@UiHandler("deleteButton")
	void onDelete(ClickEvent event) {
		presenter.onDeleteRepository(repository.getId());
		hide();
	}

}
