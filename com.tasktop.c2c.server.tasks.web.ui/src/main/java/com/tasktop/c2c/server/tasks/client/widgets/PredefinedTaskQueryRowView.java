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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;

/**
 * @author jhickey
 * 
 */
public class PredefinedTaskQueryRowView extends BaseTaskQueryRowView {
	interface Binder extends UiBinder<Widget, PredefinedTaskQueryRowView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	private PredefinedTaskQuery query;

	public PredefinedTaskQueryRowView(PredefinedTaskQuery query) {
		initWidget(uiBinder.createAndBindUi(this));
		this.query = query;
		queryAnchor.setText(query.getLabel());
	}

	/**
	 * @return the query
	 */
	public PredefinedTaskQuery getQuery() {
		return query;
	}

}
