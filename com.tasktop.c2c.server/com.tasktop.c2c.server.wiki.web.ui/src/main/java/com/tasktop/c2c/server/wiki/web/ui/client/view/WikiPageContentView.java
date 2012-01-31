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
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.common.web.client.widgets.Format;
import com.tasktop.c2c.server.tasks.client.widgets.wiki.WikiHTMLPanel;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiEditPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.presenter.WikiPageContentPresenter.RenderedWikiPageDisplay;

public class WikiPageContentView extends AbstractComposite implements RenderedWikiPageDisplay {

	interface Binder extends UiBinder<Widget, WikiPageContentView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	WikiHTMLPanel contentPanel;
	@UiField
	Anchor editButton;
	@UiField
	Anchor deleteButton;
	@UiField
	DivElement restoreDiv;
	@UiField
	Button restoreButton;
	@UiField
	Label pageLabel;
	@UiField
	Label createdMetadata;
	@UiField
	Label modifiedMetadata;
	@UiField
	Anchor createAnchor;

	public WikiPageContentView() {
		initWidget(uiBinder.createAndBindUi(this));

	}

	@Override
	public Widget getWidget() {
		return this;
	}

	@Override
	public void setPage(String projectId, Page page) {
		createAnchor.setVisible(false);
		createdMetadata.setText(null);
		modifiedMetadata.setText(null);
		pageLabel.setText(null);
		if (page != null) {
			pageLabel.setText(page.getPath());
			if (page.getContent() != null) {
				contentPanel.setWikiHTML(page.getContent());

				String createdDateText = Format.stringValueDateTime(page.getCreationDate());
				String modifiedDateText = Format.stringValueDateTime(page.getModificationDate());
				createdMetadata.setText("Created: " + createdDateText + " by " + page.getOriginalAuthor());
				if (!createdDateText.equals(modifiedDateText) || !page.getOriginalAuthor().equals(page.getLastAuthor())) {
					modifiedMetadata.setText("Changed: " + modifiedDateText + " by " + page.getLastAuthor());
				}
			}
			editButton.setHref(ProjectWikiEditPagePlace.createPlaceForPath(projectId, page.getPath()).getHref());
			if (page.isDeleted()) {
				setEnableRestore(false);
			} else {
				setEnableRestore(false);
			}
		} else {
			contentPanel.clear();
			setEnableRestore(false);
		}
	}

	@Override
	public void setEnableRestore(boolean enabled) {
		UIObject.setVisible(restoreDiv, enabled);
		setEnableEdit(!enabled);
		setEnableDelete(!enabled);
	}

	@Override
	public void addRestoreClickHandler(final ClickHandler clickHandler) {
		restoreButton.addClickHandler(clickHandler);
	}

	@Override
	public void setEnableEdit(boolean enableEdit) {
		editButton.setVisible(enableEdit);
	}

	@Override
	public void setEnableDelete(boolean canDelete) {
		deleteButton.setVisible(canDelete);
	}

	@Override
	public void setEnableMetadata(boolean metadata) {
		modifiedMetadata.setVisible(metadata);
		createdMetadata.setVisible(metadata);
	}

	@Override
	public void addDeleteClickHandler(final ClickHandler clickHandler) {
		deleteButton.addClickHandler(clickHandler);
	}

	@Override
	public void setEnableHeader(boolean enable) {
		pageLabel.setVisible(enable);
	}

	@Override
	public void setPageNotFound(String path) {
		pageLabel.setText("Page \"" + path + "\" not found.");
		contentPanel.clear();
		setEnableRestore(false);

	}

	@Override
	public void setCreateHref(String historyToken) {
		createAnchor.setVisible(true);
		createAnchor.setHref(historyToken);
	}
}
