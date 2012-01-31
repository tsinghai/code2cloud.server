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
package com.tasktop.c2c.server.tasks.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.common.web.client.widgets.Pager;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.tasks.client.presenters.ITaskListView;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;

public class TaskListView extends AbstractComposite implements ITaskListView {

	interface Binder extends UiBinder<Widget, TaskListView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Label queryInfoLabel;

	@UiField
	Anchor saveQueryAnchor;

	@UiField
	Anchor editQueryAnchor;

	@UiField
	public Panel tasksPanel;

	@UiField
	public Button newTask;

	@UiField
	public Pager pager;

	@UiField
	public Anchor exportCsvAnchor;

	@UiField
	public Anchor exportJsonAnchor;

	private CreateSavedTaskQueryDialog createQueryDialog = new CreateSavedTaskQueryDialog();

	private Presenter presenter;

	public TaskListView() {
		initWidget(uiBinder.createAndBindUi(this));

		if (AuthenticationHelper.isAnonymous()) {
			// If this user is anonymous, then hide options and features which are only available to logged-in users.
			newTask.setVisible(false);
		}
		createQueryDialog.saveButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.doSaveCurrentQuery(createQueryDialog.name.getValue());

			}
		});

		editQueryAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.doEditSelectedQuery();
			}
		});
	}

	public void clearSearchInfo() {
		queryInfoLabel.setVisible(false);
		saveQueryAnchor.setVisible(false);
		editQueryAnchor.setVisible(false);
	}

	public void updateLabel(String queryText) {
		queryInfoLabel.setVisible(true);
		saveQueryAnchor.setVisible(presenter.canCreateQuery());
		editQueryAnchor.setVisible(presenter.canEditQuery());
		queryInfoLabel.setText(queryText);

	}

	public void updateLabel(SavedTaskQuery query) {
		queryInfoLabel.setVisible(true);
		saveQueryAnchor.setVisible(false);
		editQueryAnchor.setVisible(presenter.canEditQuery());
		queryInfoLabel.setText(query.getName());
	}

	@UiHandler(value = "saveQueryAnchor")
	public void onSaveQuery(ClickEvent e) {
		createQueryDialog.center();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	/**
	 * 
	 */
	public void saveSuccess() {
		createQueryDialog.hide();
	}
}
