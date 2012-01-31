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
package com.tasktop.c2c.server.common.web.client.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class CompositeClickHandlers implements HasClickHandlers {

	private Collection<HasClickHandlers> handlers;

	public CompositeClickHandlers(HasClickHandlers... handlers) {
		this.handlers = Arrays.asList(handlers);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		for (HasClickHandlers h : handlers) {
			h.fireEvent(event);
		}
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		final List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>(handlers.size());
		for (HasClickHandlers h : handlers) {
			registrations.add(h.addClickHandler(handler));
		}

		return new HandlerRegistration() {

			public void removeHandler() {
				for (HandlerRegistration reg : registrations) {
					reg.removeHandler();
				}
			}
		};
	}

}
