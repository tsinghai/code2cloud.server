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


import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.FormSectionPanel;

public abstract class AbstractComposite extends Composite {

	protected void hookDefaultButton(final Button button) {
		FormSectionPanel focusPanel = getFormSectionPanel(button);
		if (focusPanel != null) {
			focusPanel.addKeyDownHandler(new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
						event.preventDefault();
						event.stopPropagation();
						button.click();
					}
				}
			});
		} // else fail soft
	}

	private FormSectionPanel getFormSectionPanel(Widget w) {
		// Time for some recursion. Check for our two possible end cases - either w is null, or it's an instance of the
		// class we want.
		if (w == null || w instanceof FormSectionPanel) {
			return (FormSectionPanel) w;
		}

		// If it's neither of these, then recurse.
		return getFormSectionPanel(w.getParent());
	}
}
