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
package com.tasktop.c2c.server.profile.web.ui.client.view;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

/**
 * A custom Pager that maintains a set page size and displays page numbers and total pages more elegantly. SimplePager
 * will ensure <code>pageSize</code> rows are always rendered even if the "last" page has less than
 * <code>pageSize</code> rows remaining.
 * 
 * This was derived from a solution offered on the GWT Forum: http://comments.gmane.org/gmane.org.google.gwt/62989
 */
public class PageIndexingSimplePager extends SimplePager {

	// Page size is normally derived from the visibleRange
	private int pageSize = 20;

	public PageIndexingSimplePager() {
		super();
	}

	public PageIndexingSimplePager(TextLocation location, Resources resources, boolean showFastForwardButton,
			int fastForwardRows, boolean showLastPageButton) {
		super(location, resources, showFastForwardButton, fastForwardRows, showLastPageButton);
	}

	public PageIndexingSimplePager(TextLocation location) {
		super(location);
	}

	@Override
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		super.setPageSize(pageSize);
	}

	/**
	 * @return pageSize A constant page size
	 */
	@Override
	public int getPageSize() {
		return pageSize;
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
			if (isRangeLimited() && getDisplay().isRowCountExact()) {
				displayPageSize = Math.min(getPageSize(), getDisplay().getRowCount() - index);
			}
			index = Math.max(0, index);
			if (index != range.getStart()) {
				getDisplay().setVisibleRange(index, displayPageSize);
			}
		}
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
}
