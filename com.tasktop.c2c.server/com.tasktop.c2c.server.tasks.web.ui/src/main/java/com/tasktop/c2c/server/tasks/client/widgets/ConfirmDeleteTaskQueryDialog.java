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
import com.tasktop.c2c.server.tasks.client.presenters.ITaskListView;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 */
public class ConfirmDeleteTaskQueryDialog extends DialogBox implements ITaskListView {

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, ConfirmDeleteTaskQueryDialog> {
	}

	@UiField
	Label queryName;
	@UiField
	Button deleteButton;
	@UiField
	Button cancelButton;

	private Presenter presenter;
	private SavedTaskQuery query;

	private static ConfirmDeleteTaskQueryDialog instance;

	public static ConfirmDeleteTaskQueryDialog getInstance(Presenter presenter, SavedTaskQuery query) {
		if (instance == null) {
			instance = new ConfirmDeleteTaskQueryDialog();
		}
		instance.setPresenter(presenter);
		instance.setQuery(query);
		return instance;
	}

	private ConfirmDeleteTaskQueryDialog() {
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
		presenter.doDeleteQuery(query);
		hide();
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(SavedTaskQuery query) {
		this.query = query;
		this.queryName.setText(query.getName());
	}

}
