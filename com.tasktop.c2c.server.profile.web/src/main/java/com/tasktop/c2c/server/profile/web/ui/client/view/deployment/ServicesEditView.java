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

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.deployment.domain.CloudService;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.deployment.domain.DeploymentServiceConfiguration;

public class ServicesEditView extends Composite {

	public interface CreateServiceHandler {
		void createService(CloudService service);
	}

	interface Binder extends UiBinder<Widget, ServicesEditView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	FlowPanel serviceListEditPanel;

	CreateServiceDialog createServiceDialog = new CreateServiceDialog();

	private List<CloudService> services = null;

	public ServicesEditView() {
		initWidget(uiBinder.createAndBindUi(this));

	}

	public void setValue(DeploymentConfiguration originalValue) {
		serviceListEditPanel.clear();
		services = null; // These should be set after set value.

		if (originalValue.getServices() != null && !originalValue.getServices().isEmpty()) {
			for (CloudService service : originalValue.getServices()) {
				addServiceRow(service);
			}
		} else {
			addServiceRow(null);
		}
		updateAddButtons();
	}

	private void addServiceRow(CloudService serviceOrNull) {

		final ServiceEditView serviceEditView = new ServiceEditView(serviceOrNull, createServiceDialog);
		serviceEditView.removeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				serviceListEditPanel.remove(serviceEditView);
				if (serviceListEditPanel.getWidgetCount() == 0) {
					addServiceRow(null);
				} else {
					updateAddButtons();
				}
			}
		});
		serviceEditView.addButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				addServiceRow(null);
			}
		});
		this.serviceListEditPanel.add(serviceEditView);

		if (this.services != null) {
			serviceEditView.setServices(services);
		}
		updateAddButtons();
	}

	private void updateAddButtons() {
		for (int i = 0; i < serviceListEditPanel.getWidgetCount(); i++) {
			Widget curWidget = serviceListEditPanel.getWidget(i);

			if (curWidget instanceof ServiceEditView) {
				// Only show the add button if this is the last item in the list.
				((ServiceEditView) curWidget).displayAddButton(i == (serviceListEditPanel.getWidgetCount() - 1));
			}
		}
	}

	/**
	 * @param value
	 */
	public void updateValue(DeploymentConfiguration value) {
		List<CloudService> services = new ArrayList<CloudService>();
		for (Widget w : serviceListEditPanel) {
			ServiceEditView serviceEditView = (ServiceEditView) w;
			CloudService service = serviceEditView.getValue();
			if (service != null) {
				services.add(service);
			}
		}
		value.setServices(services);
	}

	/**
	 * @param availableServiceConfigurations
	 */
	public void setServiceConfigurations(List<DeploymentServiceConfiguration> availableServiceConfigurations) {
		createServiceDialog.setServiceConfigurations(availableServiceConfigurations);
	}

	/**
	 * @param services2
	 */
	public void setServices(List<CloudService> services) {
		this.services = services;
		// Update the service options for existing services;
		for (Widget w : serviceListEditPanel) {
			ServiceEditView serviceEditView = (ServiceEditView) w;
			serviceEditView.setServices(services);
		}
	}
}
