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
package com.tasktop.c2c.server.wiki.web.ui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.CellTree.Resources;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public interface WikiTreeViewResources extends Resources {
	public static class get {
		public static WikiTreeViewResources resources = GWT.create(WikiTreeViewResources.class);
	}

	@Source("CustomCellTreeBasic.css")
	Style cellTreeStyle();

	public static interface Style extends CellTree.Style {
	}
}
