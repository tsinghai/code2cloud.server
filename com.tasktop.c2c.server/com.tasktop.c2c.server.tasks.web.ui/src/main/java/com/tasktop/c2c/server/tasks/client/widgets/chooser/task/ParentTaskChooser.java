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
package com.tasktop.c2c.server.tasks.client.widgets.chooser.task;

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.user.client.ui.SuggestOracle;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.MultiValueChooser;
import com.tasktop.c2c.server.tasks.domain.Task;

/**
 * Implementation of {@link MultiValueChooser} that will display multiple parent tasks (as we allow setting of multiple
 * parents from API), but will only allow selection of a single parent via UI
 * 
 * @author Jennifer Hickey
 * 
 */
public class ParentTaskChooser extends MultiValueChooser<Task> {

	/**
	 * @param suggestOracle
	 */
	public ParentTaskChooser(SuggestOracle suggestOracle) {
		super(suggestOracle);
	}

	/**
	 * Only allow choosing of <b>one</b> new value
	 * 
	 * @see com.tasktop.c2c.server.common.web.client.widgets.chooser.MultiValueChooser#addValue(java.lang.Object)
	 */
	protected void addValue(Task value) {
		List<Task> newValues = new ArrayList<Task>();
		newValues.add(value);
		setValues(newValues);
		fireValueChanged();
	}

}
