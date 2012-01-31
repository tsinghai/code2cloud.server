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
package com.tasktop.c2c.server.common.web.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DynamicFormPanel extends Composite {

	private FlexTable flexTable;

	private int row;
	private int column = 0;

	public DynamicFormPanel() {
		flexTable = new FlexTable();
		flexTable.addStyleName("taskDetailLayout");
		initWidget(flexTable);
	}

	/**
	 * Add a label and a widget, side by side.
	 * 
	 * @param label
	 *            the label for the field
	 * @param value
	 *            the widget
	 */
	public void add(String label, Widget value) {
		add(new Label(label), value);
	}

	/**
	 * Add a label and a widget, side by side.
	 * 
	 * @param label
	 *            the label for the field
	 * @param value
	 *            the widget
	 */
	public void add(Widget label, Widget value) {
		add(label, value, 1, false);
	}

	/**
	 * Add the given label widget and value widget, spanning the given number of logical columns. Logical columns are
	 * defined by both label and value, thus a label and value together occupy a single logical column.
	 * 
	 * @param label
	 * @param value
	 * @param logicalColspan
	 */
	public void add(Widget label, Widget value, int logicalColspan, boolean singleElement) {
		if (singleElement) {
			FlowPanel fp = new FlowPanel();
			fp.add(label);
			fp.add(value);
			flexTable.setWidget(row, column, fp);
			flexTable.getFlexCellFormatter().setStyleName(row, column, "product-item left");
		} else {
			flexTable.setWidget(row, column, label);
			flexTable.getFlexCellFormatter().setStyleName(row, column, "taskFieldLabel");
			++column;
			flexTable.setWidget(row, column, value);
			flexTable.getFlexCellFormatter().setStyleName(row, column, "taskFieldValue");
			if (logicalColspan > 1) {
				int columnIncrement = (logicalColspan - 1) * 2;
				flexTable.getFlexCellFormatter().setColSpan(row, column, 1 + columnIncrement);
				column += columnIncrement;
			}
		}
		++column;
	}

	public void add(Widget label, Widget value, boolean singleElement) {
		add(label, value, 1, singleElement);
	}

	/**
	 * Add a label and String value as a Label, side by side.
	 * 
	 * @param label
	 *            the label
	 * @param value
	 *            text for a Label widget
	 */
	public void add(String label, String value) {
		add(label, new Label(value));
	}

	/**
	 * Add a Widget spanning several cells.
	 * 
	 * @param colspan
	 *            the number of cells
	 * @param value
	 *            the widget
	 */
	public void add(int colspan, Widget value) {
		flexTable.setWidget(row, column, value);
		flexTable.getFlexCellFormatter().setColSpan(row, column, colspan);
		column += colspan;
	}

	/**
	 * Add a Label spanning several cells.
	 * 
	 * @param colspan
	 *            the number of cells
	 * @param value
	 *            the widget
	 */
	public void addLabel(int colspan, Widget label) {
		flexTable.setWidget(row, column, label);
		flexTable.getFlexCellFormatter().setStyleName(row, column, "taskFieldLabel");
		flexTable.getFlexCellFormatter().setColSpan(row, column, colspan);
		column += colspan;
	}

	/**
	 * Move to the new row
	 */
	public void newLine() {
		if (column > 0) {
			column = 0;
			++row;
		}
	}
}
