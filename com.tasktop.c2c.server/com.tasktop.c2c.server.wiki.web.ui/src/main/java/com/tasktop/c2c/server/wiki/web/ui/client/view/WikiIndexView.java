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

import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.domain.WikiTree;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiHomePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.presenter.WikiIndexPresenter.WikiIndexDisplay;

public class WikiIndexView extends AbstractComposite implements WikiIndexDisplay {

	interface Binder extends UiBinder<Widget, WikiIndexView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Label title;
	@UiField
	SimplePanel searchPanel;
	@UiField
	Panel pagePanel;
	@UiField
	Button newPageButton;
	@UiField
	Anchor allSearchLink;

	private String projectIdentifier;

	private Boolean treeView = false;

	public WikiIndexView() {
		initWidget(uiBinder.createAndBindUi(this));

	}

	@Override
	public void setProjectIdentifier(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
		allSearchLink.setHref(ProjectWikiHomePlace.createDefaultPlace(projectIdentifier).getHref());
	}

	@Override
	public void setTreeView(Boolean treeView) {
		this.treeView = treeView;
	}

	@Override
	public Widget getWidget() {
		return this;
	}

	@Override
	public HasOneWidget getSearchPanel() {
		return searchPanel;
	}

	@Override
	public void setPages(List<Page> pages) {
		pagePanel.clear();
		if (treeView) {
			WikiTreeView treeView = new WikiTreeView();
			treeView.setWikiTree(projectIdentifier, WikiTree.construtTreeModel(pages));
			pagePanel.add(treeView);
		} else {
			if (pages != null && !pages.isEmpty()) {
				for (Page page : pages) {
					pagePanel.add(new WikiPageSnippetView(projectIdentifier, page));
				}
			}
		}
	}

	@Override
	public void setEnableEdit(boolean enableEdit) {
		newPageButton.setVisible(enableEdit);
	}

	@Override
	public void addNewPageClickHandler(ClickHandler clickHandler) {
		newPageButton.addClickHandler(clickHandler);
	}
}
