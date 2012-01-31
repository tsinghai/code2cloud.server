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
package com.tasktop.c2c.server.wiki.web.ui.client.presenter;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiHomePlace;

/**
 * a presenter for a compact search component that is used to initiate a search
 */
public class WikiSearchPresenter extends AbstractWikiPresenter {
	private final WikiSearchDisplay view;

	public WikiSearchPresenter(WikiSearchDisplay view) {
		super(view.getWidget());
		this.view = view;
		view.addSearchClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doSearch();
			}
		});
	}

	public void setSearchTerm(String term) {
		view.searchTerm().setValue(term);
	}

	public interface WikiSearchDisplay {
		Widget getWidget();

		void addSearchClickHandler(ClickHandler clickHandler);

		TakesValue<String> searchTerm();
	}

	@Override
	protected void bind() {
		// nothing to do
	}

	protected void doSearch() {
		String searchTerm = view.searchTerm().getValue();
		if (searchTerm == null || searchTerm.trim().length() == 0) {
			ProjectWikiHomePlace.createDefaultPlace(getProjectIdentifier()).go();
		} else {
			ProjectWikiHomePlace.createQueryPlace(getProjectIdentifier(), searchTerm).go();
		}
	}
}
