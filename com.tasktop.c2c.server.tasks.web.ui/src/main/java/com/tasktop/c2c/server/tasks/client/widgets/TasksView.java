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

import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.tasks.client.presenters.ITaskListView;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;

public class TasksView extends AbstractComposite implements ITaskListView {

	interface Binder extends UiBinder<Widget, TasksView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	public FlowPanel predefinedQueryMenuPanel;
	@UiField
	public FlowPanel savedQueryMenuPanel;

	@UiField
	public HTMLPanel taskSearchFormSectionPanel;
	@UiField
	public TextBox taskSearchTextBox;
	@UiField
	public HasOneWidget contentPanel;

	@UiField
	public Anchor advancedSearchLink;
	@UiField
	public Button searchButton;

	protected Presenter presenter;

	public TasksView() {
		initWidget(uiBinder.createAndBindUi(this));

		taskSearchTextBox.getElement().setAttribute("placeholder", "Search Tasks");
	}

	public void addQueryMenuItem(final PredefinedTaskQuery query) {
		final BaseTaskQueryRowView row = new PredefinedTaskQueryRowView(query);
		row.queryAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.doQuery(query);
			}
		});
		predefinedQueryMenuPanel.add(row);
	}

	public void setSelectedPredefinedQuery(PredefinedTaskQuery query) {
		for (Widget w : predefinedQueryMenuPanel) {
			PredefinedTaskQueryRowView row = (PredefinedTaskQueryRowView) w;
			row.setSelected(row.getQuery().equals(query));
		}
		for (Widget w : savedQueryMenuPanel) {
			SavedTaskQueryRowView row = (SavedTaskQueryRowView) w;
			row.setSelected(false);
		}
	}

	public void setSavedTaskQueries(List<SavedTaskQuery> savedQueries) {
		savedQueryMenuPanel.clear();
		for (SavedTaskQuery query : savedQueries) {
			addQueryMenuItem(query);
		}

	}

	private void addQueryMenuItem(final SavedTaskQuery query) {
		final SavedTaskQueryRowView row = new SavedTaskQueryRowView(query);
		row.queryAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.doQuery(query);
			}
		});
		row.editAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.doEditQuery(query);

			}
		});
		row.deleteAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				ConfirmDeleteTaskQueryDialog.getInstance(presenter, query).center();
			}
		});
		savedQueryMenuPanel.add(row);
	}

	public void setSelectedSavedQuery(SavedTaskQuery query) {
		for (Widget w : predefinedQueryMenuPanel) {
			PredefinedTaskQueryRowView row = (PredefinedTaskQueryRowView) w;
			row.setSelected(false);
		}

		for (Widget w : savedQueryMenuPanel) {
			SavedTaskQueryRowView row = (SavedTaskQueryRowView) w;
			row.setSelected(row.getQuery().equals(query));
		}
	}

	/**
	 * @param presenter
	 *            the presenter to set
	 */
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

}
