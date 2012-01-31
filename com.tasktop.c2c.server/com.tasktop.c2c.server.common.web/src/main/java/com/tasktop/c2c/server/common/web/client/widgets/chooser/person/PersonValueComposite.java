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


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.AbstractValueChooser;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.AbstractValueComposite;

/**
 * @author David Green (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class PersonValueComposite extends AbstractValueComposite<Person> {

	public PersonValueComposite(AbstractValueChooser<Person> chooser) {
		super(chooser);
	}

	@Override
	protected ClickHandler createClickHandler() {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPopup();
			}
		};
	}

	protected void showPopup() {
		PopupPanel popupPanel = new PersonDetailPopupPanel(value);
		popupPanel.showRelativeTo(valueWidget);
	}

	@Override
	protected String computeValueLabel() {
		return value.getName();
	}

	@Override
	protected String computeItemStyleName() {
		return "person " + super.computeItemStyleName();
	}
}
