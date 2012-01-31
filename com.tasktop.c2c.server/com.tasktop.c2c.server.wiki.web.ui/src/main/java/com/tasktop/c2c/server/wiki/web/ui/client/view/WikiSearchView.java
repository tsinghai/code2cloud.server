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
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.wiki.web.ui.client.presenter.WikiSearchPresenter.WikiSearchDisplay;

public class WikiSearchView extends AbstractComposite implements WikiSearchDisplay {

	interface Binder extends UiBinder<Widget, WikiSearchView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	private static final String PLACEHOLDER_TEXT = "Search Wiki...";

	@UiField
	HTMLPanel searchPanel;

	@UiField
	TextBox searchText;

	@UiField
	Button searchButton;

	public WikiSearchView() {
		initWidget(uiBinder.createAndBindUi(this));
		DOM.setElementAttribute(searchText.getElement(), "placeholder", PLACEHOLDER_TEXT);
		hookDefaultButton(searchButton);
	}

	@Override
	public Widget getWidget() {
		return this;
	}

	@Override
	public TakesValue<String> searchTerm() {
		return searchText;
	}

	@Override
	public void addSearchClickHandler(ClickHandler clickHandler) {
		searchButton.addClickHandler(clickHandler);
	}
}
