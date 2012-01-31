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
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Panel;

public class PersonLabel extends Composite implements ClickHandler, HasValue<Person> {
	private Panel contents;
	private Anchor nameLabel;
	private Person person;
	private String labelText;
	private boolean asOrigin;
	private boolean asSelf;

	public PersonLabel() {
		contents = new FlowPanel();
		contents.addStyleName("personLabel");
		nameLabel = new Anchor();
		contents.add(nameLabel);
		nameLabel.addClickHandler(this);
		initWidget(contents);
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
		updateUi();
	}

	/**
	 * indicate if the person should be displayed as reporter
	 */
	public boolean isAsOrigin() {
		return asOrigin;
	}

	public void setAsOrigin(boolean asOrigin) {
		this.asOrigin = asOrigin;
		updateUi();
	}

	/**
	 * indicate if the person should be displayed as self
	 */
	public boolean isAsSelf() {
		return asSelf;
	}

	public void setAsSelf(boolean asSelf) {
		this.asSelf = asSelf;
		updateUi();
	}

	/**
	 * the label text, which if specified overrides the default label (the {@link Person#getName() person name}).
	 */
	public String getLabelText() {
		return labelText;
	}

	public void setLabelText(String labelText) {
		this.labelText = labelText;
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		updateUi();
	}

	protected void updateUi() {
		if (!isAttached()) {
			return;
		}
		if (person == null) {
			nameLabel.setText("");
		} else {
			nameLabel.setText(labelText == null ? person.getName() : labelText);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		if (person == null) {
			return;
		}
		if (event.getSource() == nameLabel) {
			new PersonDetailPopupPanel(person).showRelativeTo(nameLabel);
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Person> handler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Person getValue() {
		return getPerson();
	}

	@Override
	public void setValue(Person value) {
		setPerson(value);
	}

	@Override
	public void setValue(Person value, boolean fireEvents) {
		setPerson(value);
	}
}
