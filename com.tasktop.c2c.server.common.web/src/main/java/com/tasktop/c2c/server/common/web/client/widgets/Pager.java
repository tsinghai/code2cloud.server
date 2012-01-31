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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

/**
 * A custom Pager that maintains a set page size and displays page numbers and total pages more elegantly. SimplePager
 * will ensure <code>pageSize</code> rows are always rendered even if the "last" page has less than
 * <code>pageSize</code> rows remaining.
 * 
 * Also omits first and last page buttons and adds a ListBox to select page size
 * 
 * @author Jennifer Hickey
 * 
 */
public class Pager extends AbstractPager {

	interface Binder extends UiBinder<Widget, Pager> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Label label;

	@UiField
	Anchor nextPage;

	@UiField
	Anchor prevPage;

	@UiField
	ListBox pageSizeSelector;

	@UiField
	public Label itemLabel;

	public Pager() {
		initWidget(uiBinder.createAndBindUi(this));
		nextPage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (nextPage.isEnabled()) {
					nextPage();
				}
			}
		});
		prevPage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (prevPage.isEnabled()) {
					previousPage();
				}
			}
		});
		pageSizeSelector.addItem("10");
		pageSizeSelector.addItem("25");
		pageSizeSelector.addItem("50");
		pageSizeSelector.addItem("100");
		pageSizeSelector.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String pageSizeSelection = pageSizeSelector.getValue(pageSizeSelector.getSelectedIndex());
				setPageSize(Integer.parseInt(pageSizeSelection));
			}

		});

		// Disable the links by default.
		setDisplay(null);
	}

	@Override
	public void firstPage() {
		super.firstPage();
	}

	@Override
	public int getPage() {
		return super.getPage();
	}

	@Override
	public int getPageCount() {
		return super.getPageCount();
	}

	@Override
	public boolean hasNextPage() {
		return super.hasNextPage();
	}

	@Override
	public boolean hasNextPages(int pages) {
		return super.hasNextPages(pages);
	}

	@Override
	public boolean hasPage(int index) {
		return super.hasPage(index);
	}

	@Override
	public boolean hasPreviousPage() {
		return super.hasPreviousPage();
	}

	@Override
	public boolean hasPreviousPages(int pages) {
		return super.hasPreviousPages(pages);
	}

	@Override
	public void lastPage() {
		super.lastPage();
	}

	@Override
	public void lastPageStart() {
		super.lastPageStart();
	}

	/**
	 * Pages forward by an exact size rather than the number of visible rows as is in the norm in the underlying
	 * implementation
	 */
	@Override
	public void nextPage() {
		if (getDisplay() != null) {
			Range range = getDisplay().getVisibleRange();
			setPageStart(range.getStart() + getPageSize());
		}
	}

	/**
	 * Pages back by an exact size rather than the number of visible rows as is in the norm in the underlying
	 * implementation
	 */
	@Override
	public void previousPage() {
		if (getDisplay() != null) {
			Range range = getDisplay().getVisibleRange();
			setPageStart(range.getStart() - getPageSize());
		}
	}

	@Override
	public void setDisplay(HasRows display) {
		// Enable or disable all buttons.
		boolean disableButtons = (display == null);
		setNextPageButtonsDisabled(disableButtons);
		setPrevPageButtonsDisabled(disableButtons);
		super.setDisplay(display);
	}

	@Override
	public void setPageSize(int pageSize) {
		super.setPageSize(pageSize);
		updatePageSizeSelection(pageSize);
	}

	private void updatePageSizeSelection(Integer pageSize) {
		if (pageSize.equals(-1)) {
			return;
		}
		if (!(Integer.toString(pageSize).equals(pageSizeSelector.getValue(pageSizeSelector.getSelectedIndex())))) {
			select(pageSizeSelector, Integer.toString(pageSize));
		}
	}

	public static void select(ListBox listBox, String object) {
		if (object == null) {
			return;
		}
		int itemCount = listBox.getItemCount();
		for (int x = 0; x < itemCount; ++x) {
			String itemValue = listBox.getValue(x);
			if (object.equals(itemValue)) {
				if (!listBox.isItemSelected(x)) {
					listBox.setSelectedIndex(x);
				}
				return;
			}
		}
		// Plug in the String itself as the value
		listBox.addItem(object, object);

		// Recurse - this will succeed since we just added this object to our list.
		select(listBox, object);
	}

	/**
	 * Overridden so the last page is shown with a number of rows less than the pageSize rather than always showing the
	 * pageSize number of rows and possibly repeating rows on the last page
	 * 
	 * @param index
	 *            page start index
	 */
	@Override
	public void setPageStart(int index) {
		if (getDisplay() != null) {
			Range range = getDisplay().getVisibleRange();
			int displayPageSize = getPageSize();
			// Explicitly *not* adjusting page size down if range limited.
			index = Math.max(0, index);
			if (index != range.getStart()) {
				getDisplay().setVisibleRange(index, displayPageSize);
			}
		}
	}

	/**
	 * Let the page know that the table is loading. Call this method to clear all data from the table and hide the
	 * current range when new data is being loaded into the table.
	 */
	public void startLoading() {
		getDisplay().setRowCount(0, true);
		label.setText("");
	}

	/**
	 * Overridden to display "0 of 0" when there are no records(otherwise you get "1-1 of 0") and "1 of 1" when there is
	 * only one record (otherwise you get "1-1 of 1"). Not internationalized (but neither is SimplePager)
	 * 
	 * @return page index text
	 */
	protected String createText() {
		NumberFormat formatter = NumberFormat.getFormat("#,###");
		HasRows display = getDisplay();
		Range range = display.getVisibleRange();
		int pageStart = range.getStart() + 1;
		int pageSize = range.getLength();
		int dataSize = display.getRowCount();
		int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
		endIndex = Math.max(pageStart, endIndex);
		boolean exact = display.isRowCountExact();
		if (dataSize == 0) {
			return "0 of 0";
		} else if (pageStart == endIndex) {
			return formatter.format(pageStart) + " of " + formatter.format(dataSize);
		}
		return formatter.format(pageStart) + "-" + formatter.format(endIndex) + (exact ? " of " : " of over ")
				+ formatter.format(dataSize);
	}

	@Override
	protected void onRangeOrRowCountChanged() {
		HasRows display = getDisplay();
		label.setText(createText());

		updatePageSizeSelection(getPageSize());

		// Update the prev and first buttons.
		setPrevPageButtonsDisabled(!hasPreviousPage());

		// Update the next and last buttons.
		if (isRangeLimited() || !display.isRowCountExact()) {
			setNextPageButtonsDisabled(!hasNextPage());
		}
	}

	/**
	 * Check if the next button is disabled. Visible for testing.
	 */
	boolean isNextButtonDisabled() {
		return !(nextPage.isEnabled());
	}

	/**
	 * Check if the previous button is disabled. Visible for testing.
	 */
	boolean isPreviousButtonDisabled() {
		return !(prevPage.isEnabled());
	}

	/**
	 * Enable or disable the next page buttons.
	 * 
	 * @param disabled
	 *            true to disable, false to enable
	 */
	private void setNextPageButtonsDisabled(boolean disabled) {
		nextPage.setEnabled(!(disabled));
		if (disabled) {
			nextPage.addStyleName("disabled");
		} else {
			nextPage.removeStyleName("disabled");
		}
	}

	/**
	 * Enable or disable the previous page buttons.
	 * 
	 * @param disabled
	 *            true to disable, false to enable
	 */
	private void setPrevPageButtonsDisabled(boolean disabled) {
		prevPage.setEnabled(!(disabled));
		if (disabled) {
			prevPage.addStyleName("disabled");
		} else {
			prevPage.removeStyleName("disabled");
		}
	}
}
