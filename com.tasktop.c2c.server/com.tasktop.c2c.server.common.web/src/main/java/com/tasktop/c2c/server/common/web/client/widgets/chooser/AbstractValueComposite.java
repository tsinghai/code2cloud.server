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
package com.tasktop.c2c.server.common.web.client.widgets.chooser;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author David Green (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 * @param <T>
 */
public abstract class AbstractValueComposite<T> extends Composite {

	protected final AbstractValueChooser<T> valueChooser;

	protected T value;

	protected Anchor remove;

	protected Widget valueWidget;

	protected AbstractValueComposite(AbstractValueChooser<T> chooser) {
		this.valueChooser = chooser;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	protected boolean canRemove() {
		return valueChooser.getUnremovableValues() == null || !valueChooser.getUnremovableValues().contains(value);
	}

	public void initWidget() {
		Panel panel = new FlowPanel();
		panel.addStyleName(computeItemStyleName());

		if (valueChooser.getOriginalValues() != null && !valueChooser.getOriginalValues().contains(value)) {
			panel.addStyleName("new");
		}

		ClickHandler clickHandler = createClickHandler();
		if (clickHandler == null) {
			valueWidget = new Label(computeValueLabel());
		} else {
			valueWidget = new Anchor(computeValueLabel());
			((Anchor) valueWidget).addClickHandler(clickHandler);
		}

		panel.add(valueWidget);
		if (canRemove()) {
			remove = new Anchor("x");
			remove.setStyleName("remove");
			remove.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					valueChooser.removeValue(value);
				}
			});
			panel.add(remove);
		}

		initWidget(panel);
	}

	protected ClickHandler createClickHandler() {
		return null;
	}

	protected abstract String computeValueLabel();

	protected String computeItemStyleName() {
		return "removable-input-item";
	}

}
