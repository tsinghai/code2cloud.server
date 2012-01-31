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
package com.tasktop.c2c.server.tasks.client.widgets.admin.customfields;

import java.util.List;


import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.tasktop.c2c.server.tasks.client.widgets.admin.customfields.ICustomFieldsAdminView.Presenter;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;

public class CustomFieldsMenu extends Composite {

	interface Binder extends UiBinder<HTMLPanel, CustomFieldsMenu> {
	}

	private static CustomFieldsMenu instance;;

	public static CustomFieldsMenu getInstance() {
		if (instance == null) {
			instance = new CustomFieldsMenu();
		}
		return instance;
	}

	private static Binder ourUiBinder = GWT.create(Binder.class);

	@UiField(provided = true)
	protected CellList<FieldDescriptor> fieldList;
	@UiField
	protected Anchor newField;
	protected Resources resources = GWT.create(Resources.class);
	private Presenter presenter;

	public interface Resources extends CellList.Resources {

		@Override
		@Source("field-menu.css")
		Style cellListStyle();
	}

	public interface Style extends CellList.Style {
	}

	public void setPresenterAndUpdateDisplay(Presenter presenter) {
		this.presenter = presenter;
		List<FieldDescriptor> products = presenter.getCustomFields();

		fieldList.setRowData(products);
		for (FieldDescriptor fd : products) {
			fieldList.getSelectionModel().setSelected(fd, false);
		}
	}

	private CustomFieldsMenu() {
		fieldList = new CellList<FieldDescriptor>(new FieldCell(), resources);
		final SingleSelectionModel<FieldDescriptor> selectionModel = new SingleSelectionModel<FieldDescriptor>();
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				if (selectionModel.getSelectedObject() != null) {
					presenter.edit(selectionModel.getSelectedObject());
				}
			}
		});

		fieldList.setSelectionModel(selectionModel);
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public static class FieldCell extends AbstractCell<FieldDescriptor> {
		@Override
		public void render(Context context, FieldDescriptor value, SafeHtmlBuilder sb) {
			sb.append(SafeHtmlUtils.fromString(value.getDescription()));
		}
	}

	@UiHandler("newField")
	void onNewField(ClickEvent event) {
		presenter.newCustomField();
	}
}
