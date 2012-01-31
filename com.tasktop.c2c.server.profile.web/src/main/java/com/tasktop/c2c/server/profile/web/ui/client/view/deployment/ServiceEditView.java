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
import java.util.Collections;
import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.deployment.domain.CloudService;
import com.tasktop.c2c.server.profile.web.ui.client.view.deployment.ObjectListBox.Renderer;

/**
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
public class ServiceEditView extends Composite {

	interface Binder extends UiBinder<Widget, ServiceEditView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	ObjectListBox<CloudService> name;
	@UiField
	Anchor removeButton;
	@UiField
	Anchor addButton;

	private static final String CREATE_NEW_SERVICE_TEXT = "Create New Service...";

	public ServiceEditView(CloudService serviceOrNull, final CreateServiceDialog createServiceDialog) {
		initWidget(uiBinder.createAndBindUi(this));
		name.setRenderer(new Renderer<CloudService>() {

			@Override
			public String renderToString(CloudService object) {
				if (object.getName() == null) {
					return CREATE_NEW_SERVICE_TEXT;
				}
				return object.getName() + " : " + object.getVendor() + " (" + object.getType() + ")";
			}
		});
		if (serviceOrNull != null) {
			name.setValues(Collections.singletonList(serviceOrNull));
		}
		name.setValue(serviceOrNull);

		name.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				if (name.getSelectedIndex() != -1
						&& name.getValue(name.getSelectedIndex()).equals(CREATE_NEW_SERVICE_TEXT)) {
					createServiceDialog.clearValue();
					createServiceDialog.center();
					createServiceDialog.setResultView(ServiceEditView.this);
				}
			}
		});
	}

	/**
	 * @return
	 */
	public CloudService getValue() {
		return name.getValue();
	}

	public void displayAddButton(boolean show) {
		addButton.setVisible(show);
	}

	/**
	 * @param value
	 */
	public void addNewService(CloudService value) {
		int toReplace = name.getValues().size() - 1;
		List<CloudService> existingServices = new ArrayList<CloudService>(name.getValues());
		existingServices.remove(toReplace);
		existingServices.add(value);
		name.setValues(existingServices); // re-render
		name.setValue(value);
	}

	public void cancelCreateNewService() {
		name.setValue(null);
	}

	/**
	 * @param services
	 */
	public void setServices(List<CloudService> services) {
		ArrayList<CloudService> servicesPlusNew = new ArrayList<CloudService>(services);
		servicesPlusNew.add(new CloudService());
		name.setValuesMaintainingSelection(servicesPlusNew);
	}
}
