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

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiEditPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiHomePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.view.WikiIndexView;
import com.tasktop.c2c.server.wiki.web.ui.client.view.WikiSearchView;

public class WikiIndexPresenter extends AbstractWikiPresenter implements SplittableActivity {
	private final WikiIndexDisplay view;

	private boolean enableEdit = true;

	private ProjectWikiHomePlace place;

	private WikiSearchPresenter wikiSearchPresenter;

	public WikiIndexPresenter(WikiIndexDisplay view) {
		super(view.getWidget());
		this.view = view;
		view.addNewPageClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doNewPage();
			}
		});
		wikiSearchPresenter = new WikiSearchPresenter(new WikiSearchView());
	}

	public WikiIndexPresenter() {
		this(new WikiIndexView());
	}

	public void setPlace(Place p) {
		ProjectWikiHomePlace place = (ProjectWikiHomePlace) p;
		if (!hasChanged(place)) {
			return;
		}
		this.place = place;
		this.projectIdentifier = place.getProjectId();

		view.setProjectIdentifier(projectIdentifier);
		view.setTreeView(place.isTreeView());
		view.setPages(place.getPages());
		wikiSearchPresenter.setProjectIdentifier(getProjectIdentifier());
		wikiSearchPresenter.setSearchTerm(place.getSearchTerm());
	}

	private boolean hasChanged(ProjectWikiHomePlace place) {
		if (this.place == null) {
			return true;
		}
		if (!this.place.equals(place)) {
			return true;
		}
		// Look for changes in pages
		if (!this.place.getPages().equals(place.getPages())) {
			return true;
		}
		for (int i = 0; i < place.getPages().size(); i++) {
			if (!this.place.getPages().get(i).getModificationDate()
					.equals(place.getPages().get(i).getModificationDate())) {
				return true;
			}
		}
		return false;
	}

	public interface WikiIndexDisplay {
		Widget getWidget();

		void addNewPageClickHandler(ClickHandler clickHandler);

		void setPages(List<Page> pages);

		void setEnableEdit(boolean enableEdit);

		HasOneWidget getSearchPanel();

		void setProjectIdentifier(String projectIdentifier);

		void setTreeView(Boolean treeView);
	}

	@Override
	public void go(AcceptsOneWidget container) {
		super.go(container);
		wikiSearchPresenter.go(view.getSearchPanel());
	}

	@Override
	protected void bind() {
	}

	public boolean isEnableEdit() {
		return enableEdit;
	}

	public void setEnableEdit(boolean enableEdit) {
		this.enableEdit = enableEdit;
		view.setEnableEdit(enableEdit);
	}

	protected void doNewPage() {
		if (enableEdit) {
			ProjectWikiEditPagePlace.createPlaceForNewPage(getProjectIdentifier()).go();
		}
	}

}
