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
package com.tasktop.c2c.server.tasks.client.widgets.admin.keywords;

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.tasktop.c2c.server.tasks.domain.Keyword;

public class ProjectAdminKeywordsMenu extends Composite implements
		IProjectAdminKeywordsView<IProjectAdminKeywordsView.ProjectAdminKeywordsMenuPresenter> {
	interface ProjectAdminKeywordsMenuUiBinder extends UiBinder<HTMLPanel, ProjectAdminKeywordsMenu> {
	}

	private static ProjectAdminKeywordsMenu instance;
	private static final ProvidesKey<Keyword> KEY_PROVIDER = new ProvidesKey<Keyword>() {
		public Object getKey(Keyword item) {
			return item == null ? null : item.getName();
		}
	};

	public static ProjectAdminKeywordsMenu getInstance() {
		if (instance == null) {
			instance = new ProjectAdminKeywordsMenu();
		}
		return instance;
	}

	private static ProjectAdminKeywordsMenuUiBinder ourUiBinder = GWT.create(ProjectAdminKeywordsMenuUiBinder.class);
	private IProjectAdminKeywordsView.ProjectAdminKeywordsMenuPresenter presenter;

	@UiField(provided = true)
	CellList<Keyword> keywordList;
	@UiField
	Anchor addKeyword;

	Resources resources = GWT.create(Resources.class);

	public interface Resources extends CellList.Resources {

		@Override
		@Source("keywords-menu.css")
		Style cellListStyle();
	}

	public interface Style extends CellList.Style {
	}

	public void setPresenter(IProjectAdminKeywordsView.ProjectAdminKeywordsMenuPresenter presenter) {
		this.presenter = presenter;
		List<Keyword> keywords = new ArrayList<Keyword>(presenter.getKeywords());
		keywordList.setRowData(keywords);
		keywordList.getSelectionModel().setSelected(presenter.getSelectedKeyword(), true);
	}

	private boolean silent = false;

	private ProjectAdminKeywordsMenu() {
		keywordList = new CellList<Keyword>(new KeywordCell(), resources, KEY_PROVIDER);
		final SingleSelectionModel<Keyword> selectionModel = new SingleSelectionModel<Keyword>();
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				Keyword selected = selectionModel.getSelectedObject();
				if (silent) {
					silent = false;
					return;
				}
				presenter.selectKeyword(selected);
			}
		});
		keywordList.setSelectionModel(selectionModel);
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public static class KeywordCell extends AbstractCell<Keyword> {
		@Override
		public void render(Context context, Keyword value, SafeHtmlBuilder sb) {
			sb.append(SafeHtmlUtils.fromString(value.getName()));
		}
	}

	@UiHandler("addKeyword")
	void onAddKeyword(ClickEvent event) {
		if (presenter != null) {
			presenter.addKeyword();
		}
	}
}
