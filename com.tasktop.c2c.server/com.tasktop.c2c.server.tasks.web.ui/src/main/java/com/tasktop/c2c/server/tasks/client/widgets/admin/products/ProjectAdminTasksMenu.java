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
package com.tasktop.c2c.server.tasks.client.widgets.admin.products;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.tasktop.c2c.server.tasks.domain.Product;

public class ProjectAdminTasksMenu extends Composite implements
		IProjectAdminTasksView<IProjectAdminTasksView.ProjectAdminTasksMenuPresenter> {
	interface ProjectAdminTasksMenuUiBinder extends UiBinder<HTMLPanel, ProjectAdminTasksMenu> {
	}

	private static ProjectAdminTasksMenu instance;
	private static final ProvidesKey<Product> KEY_PROVIDER = new ProvidesKey<Product>() {
		public Object getKey(Product item) {
			return item == null ? null : item.getId();
		}
	};

	public static ProjectAdminTasksMenu getInstance() {
		if (instance == null) {
			instance = new ProjectAdminTasksMenu();
		}
		return instance;
	}

	private static ProjectAdminTasksMenuUiBinder ourUiBinder = GWT.create(ProjectAdminTasksMenuUiBinder.class);
	private IProjectAdminTasksView.ProjectAdminTasksMenuPresenter presenter;

	@UiField(provided = true)
	CellList<Product> productList;
	@UiField
	Anchor addProduct;

	Resources resources = GWT.create(Resources.class);

	public interface Resources extends CellList.Resources {

		@Override
		@Source("project-menu.css")
		Style cellListStyle();
	}

	public interface Style extends CellList.Style {
	}

	public void setPresenter(IProjectAdminTasksView.ProjectAdminTasksMenuPresenter presenter) {
		this.presenter = presenter;
		List<Product> products = new ArrayList<Product>(presenter.getProducts());
		Collections.sort(products, new Comparator<Product>() {

			@Override
			public int compare(Product o1, Product o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		productList.setRowData(products);
		productList.getSelectionModel().setSelected(presenter.getProduct(), true);
	}

	private boolean silent = false;

	private ProjectAdminTasksMenu() {
		productList = new CellList<Product>(new ProjectCell(), resources, KEY_PROVIDER);
		final SingleSelectionModel<Product> selectionModel = new SingleSelectionModel<Product>();
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				Product selected = selectionModel.getSelectedObject();
				if (silent) {
					silent = false;
					return;
				}
				presenter.selectProduct(selected.getId());
			}
		});
		productList.setSelectionModel(selectionModel);
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	public static class ProjectCell extends AbstractCell<Product> {
		@Override
		public void render(Context context, Product value, SafeHtmlBuilder sb) {
			sb.append(SafeHtmlUtils.fromString(value.getName()));
		}
	}

	@UiHandler("addProduct")
	void onAddProduct(ClickEvent event) {
		if (presenter != null) {
			presenter.addProduct();
		}
	}
}
