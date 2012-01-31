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
package com.tasktop.c2c.server.tasks.client.widgets.admin.iterations;


import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.tasktop.c2c.server.tasks.domain.Iteration;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class IterationsAdminView extends Composite implements IIterationsAdminView {

	private static IterationsAdminView instance;

	public static IterationsAdminView getInstance() {
		if (instance == null) {
			instance = new IterationsAdminView();
		}
		return instance;
	}

	protected interface Binder extends UiBinder<HTMLPanel, IterationsAdminView> {
	};

	@UiField
	protected CheckBox hideInActive;
	@UiField
	protected Button newIteration;
	@UiField(provided = true)
	protected CellTable<Iteration> cellTable = new CellTable<Iteration>();
	private ListDataProvider<Iteration> dataProvider = new ListDataProvider<Iteration>();
	protected Binder binder = GWT.create(Binder.class);
	private Presenter presenter;

	private IterationsAdminView() {
		initWidget(binder.createAndBindUi(this));
		initTableColumns();
		cellTable.setPageSize(1000);
		dataProvider.addDataDisplay(cellTable);
		hideInActive.setValue(true);
	}

	private void initTableColumns() {
		Column<Iteration, String> nameColumn = new Column<Iteration, String>(new EditTextCell()) {

			@Override
			public String getValue(Iteration iteration) {
				return iteration.getValue();
			}
		};
		nameColumn.setFieldUpdater(new FieldUpdater<Iteration, String>() {

			@Override
			public void update(int index, Iteration iteration, String value) {
				if (iteration.getValue().equals(value)) {
					return;
				}
				iteration.setValue(value);
				presenter.saveIteration(iteration);

			}
		});
		cellTable.addColumn(nameColumn, "Name");

		Column<Iteration, Boolean> activeColumn = new Column<Iteration, Boolean>(new CheckboxCell()) {

			@Override
			public Boolean getValue(Iteration iteration) {
				return iteration.getIsActive();
			}
		};
		activeColumn.setFieldUpdater(new FieldUpdater<Iteration, Boolean>() {

			@Override
			public void update(int index, Iteration iteration, Boolean value) {
				if (iteration.getIsActive().equals(value)) {
					return;
				}
				iteration.setIsActive(value);
				presenter.saveIteration(iteration);
			}
		});
		cellTable.addColumn(activeColumn, "Active");
	}

	@Override
	public void setPresenterAndUpdateDisplay(Presenter presenter) {
		this.presenter = presenter;
		updateView();
	}

	private void updateView() {
		dataProvider.setList(presenter.getIterations(hideInActive.getValue()));
		refreshDisplays();
	}

	private void refreshDisplays() {
		dataProvider.refresh();
	}

	@UiHandler("newIteration")
	protected void onNewIteration(ClickEvent ce) {
		presenter.newIteration();
	}

	@UiHandler("hideInActive")
	protected void onHideActiveToggle(ClickEvent ce) {
		updateView();
	}

}
