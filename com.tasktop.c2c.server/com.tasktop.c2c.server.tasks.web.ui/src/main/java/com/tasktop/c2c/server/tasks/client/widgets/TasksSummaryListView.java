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
package com.tasktop.c2c.server.tasks.client.widgets;

import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksSummaryListPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksSummaryPlace;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;

public class TasksSummaryListView extends AbstractComposite {

	interface Binder extends UiBinder<Widget, TasksSummaryListView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);
	private String appId;

	@UiField
	public FlowPanel taskSummaryPanel;
	@UiField
	public HeadingElement taskSummaryTitle;

	public TasksSummaryListView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setHeader(String text) {
		taskSummaryTitle.setInnerText(text);
	}

	private void renderTitle(SafeHtmlBuilder sb, String text, String targetHistoryToken) {
		Hyperlink link = new Hyperlink(text, targetHistoryToken);
		sb.appendHtmlConstant("<h3 class=\"task-summary-header\">");
		sb.appendHtmlConstant(link.toString());
		sb.appendHtmlConstant("</h3>");
	}

	private void renderRow(SafeHtmlBuilder sb, String text, String targetHistoryToken) {
		Hyperlink link = new Hyperlink(text, targetHistoryToken);

		sb.appendHtmlConstant("<div class=\"task-summary-card\">");
		sb.appendHtmlConstant(link.toString());
		sb.appendHtmlConstant("<div class=\"clearleft\"></div></div>");
	}

	private void createProductSegment(Product product) {

		createProductHeader(product);
		// createComponentSegment("Components", product.getComponents(), product.getId());
		createMilestoneSegment(null, product.getMilestones(), product.getId());
	}

	private void createProductHeader(Product product) {

		SafeHtmlBuilder sb = new SafeHtmlBuilder();

		sb.appendHtmlConstant("<h2 class=\"task-summary-product-header\">Product: ");
		sb.appendEscaped(product.getName());
		sb.appendHtmlConstant("</h2>");

		taskSummaryPanel.add(new HTML(sb.toSafeHtml()));
	}

	private void createComponentSegment(String title, List<Component> componentList, Integer productId) {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();

		if (title != null) {
			renderTitle(sb, title, ProjectTasksSummaryListPlace.createProductListPlace(appId, productId).getHref());
		}

		for (Component curComponent : componentList) {
			renderRow(sb, curComponent.getName(),
					ProjectTasksSummaryPlace.createPlaceForComponent(appId, productId, curComponent.getId())
							.getPrefix());
		}

		taskSummaryPanel.add(new HTML(sb.toSafeHtml()));
	}

	private void createMilestoneSegment(String title, List<Milestone> milestoneList, Integer productId) {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();

		if (title != null) {
			renderTitle(sb, title, ProjectTasksSummaryListPlace.createProductListPlace(appId, productId).getHref());
		}

		for (Milestone curMilestone : milestoneList) {
			renderRow(sb, curMilestone.getValue(),
					ProjectTasksSummaryPlace.createPlaceForRelease(appId, productId, curMilestone.getValue())
							.getPrefix());
		}

		taskSummaryPanel.add(new HTML(sb.toSafeHtml()));
	}

	public void renderProductList(List<Product> productList) {
		for (Product curProduct : productList) {
			createProductSegment(curProduct);
		}
	}

	public void renderProduct(Product findProduct) {
		// createComponentSegment("Components", findProduct.getComponents(), findProduct.getId());
		createMilestoneSegment(null, findProduct.getMilestones(), findProduct.getId());
	}

	public void renderProductComponents(Product findProduct) {
		createComponentSegment(null, findProduct.getComponents(), findProduct.getId());
	}

	public void renderProductMilestones(Product findProduct) {
		createMilestoneSegment(null, findProduct.getMilestones(), findProduct.getId());
	}

	/**
	 * @param appId
	 *            the appId to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}

	/**
	 * 
	 */
	public void clear() {
		taskSummaryPanel.clear();
	}
}
