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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.widgets.Format;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.domain.PageOutlineItem;
import com.tasktop.c2c.server.wiki.domain.WikiTree;
import com.tasktop.c2c.server.wiki.domain.WikiTree.Type;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiViewPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageOutlineAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageOutlineResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class WikiTreeModel implements TreeViewModel {

	private class WikiTreeDataProvider extends AbstractDataProvider<WikiTree> {

		private WikiTree value;

		public WikiTreeDataProvider(WikiTree value) {
			this.value = value;
		}

		@Override
		protected void onRangeChanged(final HasData<WikiTree> display) {
			switch (value.getType()) {
			case DIRECTORY:
				display.setRowData(0, value.getChildren());
				break;
			case PAGE_HEADER:
				ProfileGinjector.get
						.instance()
						.getDispatchService()
						.execute(new RetrievePageOutlineAction(projectId, value.getPage().getPath()),
								new AsyncCallbackSupport<RetrievePageOutlineResult>() {

									@Override
									protected void success(RetrievePageOutlineResult result) {
										if (result.get().getOutlineItems() == null
												|| result.get().getOutlineItems().isEmpty()) {
											WikiTree node = new WikiTree(value.getPage());
											node.setType(Type.NO_OUTLINE);
											display.setRowData(0, Collections.singletonList(node));
										} else {

											List<WikiTree> outlineItems = new ArrayList<WikiTree>();
											for (PageOutlineItem outlineItem : result.get().getOutlineItems()) {
												outlineItems.add(new WikiTree(value.getPage(), outlineItem));
											}
											display.setRowData(0, outlineItems);
										}
									}
								});
			}

		}

	}

	private static Template template = GWT.create(Template.class);

	static interface Template extends SafeHtmlTemplates {
		@Template("<div class=\" clear\">{0} ({1} items)</div> <div style=\"border-top: 2px solid #EDEEED;\"></div>")
		SafeHtml wikiDirectory(String dirname, Integer numItems);

		@Template("<div class=\"clear\">" + //
				"<a href=\"{1}\">{0}</a>" + //
				"<div class=\"date-info right\" style=\"width: 60%\">" + //
				"<div class=\"created left\">Created: {2}</div>" + //
				"<div class=\"changed right\">Changed: {3}</div>" + //
				"</div></div><div style=\"border-top: 2px solid #EDEEED;\"></div>")
		SafeHtml wikiPageHeader(String path, String url, String createdString, String updatedString);

		@Template("<div class=\"wikiContent\">{0}</div><div class=\"clear\"></div><div style=\"border-top: 2px solid #EDEEED;\"></div>")
		SafeHtml wikiPageContent(SafeHtml content);

		@Template("<a href=\"{1}\">{0}</a>")
		SafeHtml wikiPageOutlineItem(String label, String url);

		@Template("No outline available <a href=\"{0}\">Goto page</a>")
		SafeHtml wikiPageNoOutline(String url);

	}

	private class WikiTreeCell extends AbstractCell<WikiTree> {

		public WikiTreeCell() {
			super("keydown");
		}

		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context, WikiTree value, SafeHtmlBuilder sb) {
			switch (value.getType()) {
			case DIRECTORY:
				sb.append(template.wikiDirectory(value.getPath(), value.getChildren().size()));
				break;
			case PAGE_HEADER:
				Page page = value.getPage();
				String url = ProjectWikiViewPagePlace.createPlaceForPage(projectId, page.getPath()).getHref();
				sb.append(template.wikiPageHeader(page.getPath(), url, Format.stringValueDate(page.getCreationDate())
						+ " by " + page.getOriginalAuthor().getName(),
						Format.stringValueDate(page.getModificationDate()) + " by " + page.getLastAuthor().getName()));
				break;
			case PAGE_OUTLINE_ITEM:
				String itemUrl = ProjectWikiViewPagePlace.createPlaceForPageAnchor(projectId,
						value.getPage().getPath(), value.getPageOutlineItem().getId()).getHref();
				sb.append(template.wikiPageOutlineItem(value.getPageOutlineItem().getLabel(), itemUrl));

				break;

			case NO_OUTLINE:
				url = ProjectWikiViewPagePlace.createPlaceForPage(projectId, value.getPage().getPath()).getHref();
				sb.append(template.wikiPageNoOutline(url));
				break;

			}
		}

		@Override
		protected void onEnterKeyDown(Context context, Element parent, WikiTree value, NativeEvent event,
				ValueUpdater<WikiTree> valueUpdater) {
			switch (value.getType()) {
			case DIRECTORY:

			case PAGE_HEADER:

				break;
			case PAGE_OUTLINE_ITEM:
				ProjectWikiViewPagePlace.createPlaceForPageAnchor(projectId, value.getPage().getPath(),
						value.getPageOutlineItem().getId()).go();
				break;

			case NO_OUTLINE:
				ProjectWikiViewPagePlace.createPlaceForPage(projectId, value.getPage().getPath()).go();
				break;

			}
		}

	}

	private WikiTreeCell wikiTreeCell = new WikiTreeCell();

	private final String projectId;

	public WikiTreeModel(String projectId) {
		this.projectId = projectId;
	}

	@Override
	public <T> NodeInfo<?> getNodeInfo(T value) {
		if (value instanceof WikiTree) {
			return new DefaultNodeInfo(new WikiTreeDataProvider((WikiTree) value), wikiTreeCell,
					new NoSelectionModel<WikiTree>(), null);
		}
		return null;
	}

	@Override
	public boolean isLeaf(Object value) {
		if (value instanceof WikiTree) {
			WikiTree wikiTree = (WikiTree) value;
			return wikiTree.getType().equals(Type.PAGE_OUTLINE_ITEM) || wikiTree.getType().equals(Type.NO_OUTLINE);
		}
		return true;
	}

}
