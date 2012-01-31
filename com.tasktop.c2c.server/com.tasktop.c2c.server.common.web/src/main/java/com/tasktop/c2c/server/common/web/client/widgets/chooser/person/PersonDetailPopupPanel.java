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
package com.tasktop.c2c.server.common.web.client.widgets.chooser.person;


import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;

public class PersonDetailPopupPanel extends PopupPanel {

	private final Person person;

	public PersonDetailPopupPanel(Person person) {
		super(true);
		this.person = person;
		init();
	}

	private void init() {
		setGlassEnabled(false);

		addStyleName("personDetail");
		Panel popupContent = new VerticalPanel();
		final Label nameLabel = new Label(person.getName());
		nameLabel.setStylePrimaryName("personName");
		popupContent.add(nameLabel);
		final Label identityLabel = new Label(person.getIdentity());
		identityLabel.setStylePrimaryName("personIdentity");
		popupContent.add(identityLabel);
		add(popupContent);

		CommonGinjector.get.instance().getEventBus().addHandler(ScrollEvent.getType(), new ScrollHandler() {
			@Override
			public void onScroll(ScrollEvent event) {
				hide();
			}
		});
		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				hide();
			}
		});
	}
}
