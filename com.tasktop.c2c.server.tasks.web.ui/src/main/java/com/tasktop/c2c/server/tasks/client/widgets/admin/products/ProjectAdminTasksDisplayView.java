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


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Milestone;

public class ProjectAdminTasksDisplayView extends Composite implements
		IProjectAdminTasksView<IProjectAdminTasksView.ProjectAdminTasksDisplayPresenter> {

	interface ProjectAdminTasksDisplayViewUiBinder extends UiBinder<HTMLPanel, ProjectAdminTasksDisplayView> {
	}

	private static ProjectAdminTasksDisplayView instance;

	public static ProjectAdminTasksDisplayView getInstance() {
		if (instance == null) {
			instance = new ProjectAdminTasksDisplayView();
		}
		return instance;
	}

	private static ProjectAdminTasksDisplayViewUiBinder ourUiBinder = GWT
			.create(ProjectAdminTasksDisplayViewUiBinder.class);

	private ProjectAdminTasksDisplayPresenter presenter;

	@UiField
	Anchor deleteProduct;
	@UiField
	Anchor editProduct;
	@UiField
	Label productName;
	@UiField
	Label productDescription;
	@UiField
	Label productDefaultRelease;
	@UiField
	HTML components;
	@UiField
	HTML releases;

	private ProjectAdminTasksTemplate template = GWT.create(ProjectAdminTasksTemplate.class);

	public void setPresenter(ProjectAdminTasksDisplayPresenter presenter) {
		this.presenter = presenter;
		productName.setText(presenter.getProduct().getName());
		productDescription.setText(presenter.getProduct().getDescription());
		productDefaultRelease.setText(presenter.getProduct().getDefaultMilestone().toString());
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		for (Component component : presenter.getProduct().getComponents()) {
			if (component.getId() < 0) {
				continue;
			}
			String name = component.getName() == null ? "" : component.getName();
			String description = component.getDescription() == null ? "" : component.getDescription();
			String owner = "";

			if (component.getInitialOwner() != null) {
				owner = component.getInitialOwner().getRealname() == null ? "" : "("
						+ component.getInitialOwner().getRealname() + ")";
			}
			sb.append(template.component(name, description, owner));
		}
		components.setHTML(sb.toSafeHtml());

		sb = new SafeHtmlBuilder();
		for (Milestone milestone : presenter.getProduct().getMilestones()) {
			if (milestone.getId() < 0) {
				continue;
			}
			sb.append(template.release(milestone.getValue()));
		}
		releases.setHTML(sb.toSafeHtml());

	}

	private ProjectAdminTasksDisplayView() {
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	interface ProjectAdminTasksTemplate extends SafeHtmlTemplates {

		@Template("<fieldset>" + "<label>{0}:</label>" + "<div>" + "{1} {2}" + "</div>" + "</fieldset>")
		SafeHtml component(String componentName, String componentDescription, String owner);

		@Template("<fieldset>" + "<label>{0}</label>" + "</fieldset>")
		SafeHtml release(String releaseIdentifier);
	}

	@UiHandler("editProduct")
	void onEdit(ClickEvent event) {
		presenter.onEditProduct();
	}

	@UiHandler("deleteProduct")
	void onDelete(ClickEvent event) {
		presenter.onDeleteProduct();
	}
}
