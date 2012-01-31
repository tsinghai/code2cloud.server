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
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.deployment.domain.DeploymentConfiguration;
import com.tasktop.c2c.server.profile.web.ui.client.view.deployment.ObjectListBox.Renderer;

public class SettingsEditView extends Composite {
	interface Binder extends UiBinder<Widget, SettingsEditView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	FlowPanel mappedUrlEditPanel;
	@UiField
	ObjectListBox<Integer> memoryListBox;
	@UiField
	TextBox instancesTextBox;

	private static final String MEM_SUFFIX = " MB";

	public SettingsEditView() {
		initWidget(uiBinder.createAndBindUi(this));

		instancesTextBox.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				char typedChar = event.getCharCode();
				TextBox source = (TextBox) event.getSource();

				if (!Character.isDigit(typedChar)) {
					source.cancelKey();
				}
			}
		});
		memoryListBox.setRenderer(new Renderer<Integer>() {

			@Override
			public String renderToString(Integer object) {
				return object + MEM_SUFFIX;
			}
		});
	}

	private void updateAddButtons() {

		for (int i = 0; i < mappedUrlEditPanel.getWidgetCount(); i++) {
			Widget curWidget = mappedUrlEditPanel.getWidget(i);

			if (curWidget instanceof MappedUrlEditView) {
				// Only show the add button if this is the last item in the list.
				((MappedUrlEditView) curWidget).displayAddButton(i == (mappedUrlEditPanel.getWidgetCount() - 1));
			}
		}
	}

	public void setValue(DeploymentConfiguration originalValue) {
		mappedUrlEditPanel.clear();
		if (originalValue.getMappedUrls() != null && !originalValue.getMappedUrls().isEmpty()) {
			for (String mappedUrl : originalValue.getMappedUrls()) {
				addUrlRow(mappedUrl);
			}
		} else {
			addUrlRow("");
		}

		updateAddButtons();

		memoryListBox.setValue(originalValue.getMemory());
		instancesTextBox.setValue(originalValue.getNumInstances() + "");
	}

	private void addUrlRow(String url) {
		final MappedUrlEditView urlView = new MappedUrlEditView(url);
		urlView.removeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				mappedUrlEditPanel.remove(urlView);
				if (mappedUrlEditPanel.getWidgetCount() == 0) {
					addUrlRow("");
				}
				updateAddButtons();
			}
		});
		urlView.addUrlButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				addUrlRow("");
			}
		});

		mappedUrlEditPanel.add(urlView);
		updateAddButtons();
	}

	public void setMemoryValues(List<Integer> memoryValues) {
		memoryListBox.setValuesMaintainingSelection(memoryValues);
	}

	/**
	 * @param value
	 */
	public void updateValue(DeploymentConfiguration value) {
		value.setMemory(memoryListBox.getValue());

		value.setNumInstances(Integer.parseInt(instancesTextBox.getValue()));

		List<String> mappedUrls = new ArrayList<String>();
		for (Widget w : mappedUrlEditPanel) {
			MappedUrlEditView muev = (MappedUrlEditView) w;
			if (muev.url.getValue().isEmpty()) {
				continue;
			}
			mappedUrls.add(muev.url.getValue());
		}

		value.setMappedUrls(mappedUrls);
	}
}
