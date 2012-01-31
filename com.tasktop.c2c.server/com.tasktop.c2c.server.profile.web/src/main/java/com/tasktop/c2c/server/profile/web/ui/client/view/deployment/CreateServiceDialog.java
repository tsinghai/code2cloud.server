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
package com.tasktop.c2c.server.profile.web.ui.client.view.deployment;

import java.util.List;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.deployment.domain.CloudService;
import com.tasktop.c2c.server.deployment.domain.DeploymentServiceConfiguration;
import com.tasktop.c2c.server.profile.web.ui.client.view.deployment.ObjectListBox.Renderer;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class CreateServiceDialog extends DialogBox {

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, CreateServiceDialog> {
	}

	@UiField
	TextBox name;
	@UiField
	ObjectListBox<DeploymentServiceConfiguration> type;

	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	private ServiceEditView viewForNewService;

	public CreateServiceDialog() {
		super(false, true);
		setText("Create new Service");
		setWidget(uiBinder.createAndBindUi(this));
		type.setRenderer(new Renderer<DeploymentServiceConfiguration>() {

			@Override
			public String renderToString(DeploymentServiceConfiguration object) {
				return object.getVendor() + " (" + object.getType() + ")";
			}
		});

		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				viewForNewService.cancelCreateNewService();
				hide();
			}
		});

		saveButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				viewForNewService.addNewService(getValue());
				hide();
			}
		});
		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				if (isShowing()) {
					center();
				}
			}
		});
	}

	public void clearValue() {
		name.setValue("");
		type.setValue(null);
	}

	public CloudService getValue() {
		CloudService result = new CloudService();
		result.setName(name.getValue());
		DeploymentServiceConfiguration selectedServiceConfig = type.getValue();
		result.setType(selectedServiceConfig.getType());
		result.setTierType(selectedServiceConfig.getTiers().get(0).getType());
		result.setVendor(selectedServiceConfig.getVendor());
		result.setVersion(selectedServiceConfig.getVersion());

		return result;
	}

	/**
	 * @param availableServiceConfigurations
	 */
	public void setServiceConfigurations(List<DeploymentServiceConfiguration> availableServiceConfigurations) {
		type.setValues(availableServiceConfigurations);
	}

	/**
	 * @param changeHandler
	 */
	public void setResultView(ServiceEditView view) {
		this.viewForNewService = view;
	}

}
