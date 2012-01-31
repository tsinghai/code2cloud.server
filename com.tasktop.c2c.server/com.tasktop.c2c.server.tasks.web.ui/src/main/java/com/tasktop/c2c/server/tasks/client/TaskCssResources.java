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
package com.tasktop.c2c.server.tasks.client;

import com.google.gwt.resources.client.CssResource;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public interface TaskCssResources extends CssResource {
	public String inline();

	public String wikiControl();

	public String wikiEdit();

	public String small();

	public String left();

	public String right();

	public String inlineEditTrigger();

	public String editControls();

	public String editing();

	public String visibleTrigger();

	public String taskSection();

	public String taskTitle();

	public String taskSummary();

	public String taskType();

	public String completeTask();
}
