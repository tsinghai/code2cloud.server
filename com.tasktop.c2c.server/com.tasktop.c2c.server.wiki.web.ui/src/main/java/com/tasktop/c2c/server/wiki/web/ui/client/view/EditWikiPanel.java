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

import com.google.gwt.user.client.ui.TextBox;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePagePreviewAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePagePreviewResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class EditWikiPanel extends com.tasktop.c2c.server.tasks.client.widgets.wiki.EditWikiPanel {

	private TextBox path;

	protected void renderWikiPreview(String wikiText) {
		preRender();
		ProfileGinjector.get
				.instance()
				.getDispatchService()
				.execute(new RetrievePagePreviewAction(projectId, path.getText(), wikiText),
						new AsyncCallbackSupport<RetrievePagePreviewResult>() {

							@Override
							protected void success(RetrievePagePreviewResult result) {
								displayRenderedHtml(result.get());

							}
						});
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(TextBox path) {
		this.path = path;
	}

}
