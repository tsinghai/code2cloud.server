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
package com.tasktop.c2c.server.tasks.client.presenters;

import java.util.List;


import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksSummaryListPlace;
import com.tasktop.c2c.server.tasks.client.widgets.TasksSummaryListView;
import com.tasktop.c2c.server.tasks.domain.Product;

public class TasksSummaryListPresenter extends AbstractTaskPresenter implements SplittableActivity {

	private final TasksSummaryListView tasksView;
	private Integer productId;
	private ListScope scope;
	private List<Product> productList;

	public enum ListScope {
		Product, Component, Milestone, None
	};

	public TasksSummaryListPresenter(TasksSummaryListView view) {
		super(view);
		tasksView = view;
	}

	public TasksSummaryListPresenter() {
		this(new TasksSummaryListView());
	}

	public void setPlace(Place p) {
		ProjectTasksSummaryListPlace place = (ProjectTasksSummaryListPlace) p;
		this.productId = place.getProductId();
		this.scope = place.getScope();
		this.productList = place.getProducts();
		setProjectIdentifier(place.getProjectId());
		tasksView.setAppId(place.getProjectId());
		renderSummaryList();
	}

	@Override
	protected void bind() {

	}

	private void renderSummaryList() {
		tasksView.clear();
		if (productList.isEmpty()) {
			tasksView.setHeader("No task summaries found");
			return;
		}

		Product product = findProduct(productList);
		switch (scope) {
		case None:

			tasksView.setHeader("By Product and Release");
			tasksView.renderProductList(productList);
			break;

		case Product:

			tasksView.setHeader("Product " + product.getName() + ", by Release");
			tasksView.renderProduct(product);
			break;

		case Component:

			tasksView.setHeader("Product " + product.getName() + ", by Component");
			tasksView.renderProductComponents(product);
			break;

		case Milestone:

			tasksView.setHeader("Product " + product.getName() + ", by Release");
			tasksView.renderProductMilestones(product);
			break;
		}
	}

	private Product findProduct(List<Product> productList) {
		// Select out our product
		Product renderProduct = null;

		for (Product curProduct : productList) {
			if (curProduct.getId().equals(this.productId)) {
				renderProduct = curProduct;
				break;
			}
		}

		return renderProduct;
	}
}
