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
package com.tasktop.c2c.server.wiki.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
@SuppressWarnings("serial")
public class PageOutline implements Serializable {

	private List<PageOutlineItem> outlineItems;

	/**
	 * @return the outlineItems
	 */
	public List<PageOutlineItem> getOutlineItems() {
		return outlineItems;
	}

	/**
	 * @param outlineItems
	 *            the outlineItems to set
	 */
	public void setOutlineItems(List<PageOutlineItem> outlineItems) {
		this.outlineItems = outlineItems;
	}

	/**
	 * @param outlineItem
	 */
	public void addOutlineItem(PageOutlineItem outlineItem) {
		if (outlineItems == null) {
			outlineItems = new ArrayList<PageOutlineItem>();
		}
		outlineItems.add(outlineItem);
	}
}
