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

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class BaseTaskQueryRowView extends Composite {

	private static final String SELECTED_SYTLE = "selected";
	@UiField
	protected Anchor queryAnchor;

	/**
	 * 
	 */
	public BaseTaskQueryRowView() {
		super();
	}

	protected com.google.gwt.dom.client.Element getSelectionElement() {
		return queryAnchor.getElement().getParentElement();
	}

	public void setSelected(boolean selected) {
		com.google.gwt.dom.client.Element e = getSelectionElement();
		if (selected) {
			e.addClassName(SELECTED_SYTLE);
		} else {
			e.removeClassName(SELECTED_SYTLE);
		}
	}

}
