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
package com.tasktop.c2c.server.tasks.client.widgets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.tasks.domain.AbstractDomainObject;
import com.tasktop.c2c.server.tasks.domain.AbstractReferenceValue;

public class WidgetUtil {
	public static void configureReferenceValues(ListBox listBox, List<? extends AbstractReferenceValue> items) {
		listBox.clear();

		// used to ensure no duplicates
		Set<String> itemValues = new HashSet<String>();

		for (AbstractReferenceValue item : items) {
			if ((item.getValue() != null && !(item.getValue().isEmpty())) && itemValues.add(item.getValue())) {
				listBox.addItem(item.toString(), item.getValue());
			}
		}
	}

	public static void configureStringValues(ListBox listBox, List<String> items) {
		listBox.clear();

		// used to ensure no duplicates
		Set<String> itemValues = new HashSet<String>();
		if (items != null) {
			for (String item : items) {
				if (itemValues.add(item)) {
					listBox.addItem(item, item);
				}
			}
		}
	}

	/**
	 * configure the given list box with the list of provided items. The list box is first cleared, then items are added
	 * for each item provided in the list.
	 * 
	 * @param listBox
	 *            the list box to configure
	 * @param items
	 *            the items to add
	 */
	public static void configureDomainValues(ListBox listBox, List<? extends AbstractDomainObject> items) {
		configureDomainValues(listBox, items, false);
	}

	/**
	 * configure the given list box with the list of provided items. The list box is first cleared, then items are added
	 * for each item provided in the list.
	 * 
	 * @param listBox
	 *            the list box to configure
	 * @param items
	 *            the items to add
	 * @param labelAsValue
	 *            when true, the listBox's item value is the label, otherwise it is the item's id
	 */
	public static void configureDomainValues(ListBox listBox, List<? extends AbstractDomainObject> items,
			boolean labelAsValue) {
		listBox.clear();

		// used to ensure no duplicates
		Set<String> itemValues = new HashSet<String>();

		for (AbstractDomainObject item : items) {
			String value = item.getId().toString();
			if (itemValues.add(value)) {
				listBox.addItem(item.toString(), labelAsValue ? item.toString() : value);
			}
		}
	}

	public static void select(ListBox listBox, AbstractReferenceValue object) {
		if (object == null) {
			return;
		}
		final String value = object.getValue();
		int itemCount = listBox.getItemCount();
		for (int x = 0; x < itemCount; ++x) {
			String itemValue = listBox.getValue(x);
			if (value.equals(itemValue)) {
				if (!listBox.isItemSelected(x)) {
					listBox.setSelectedIndex(x);
				}
				return;
			}
		}
		listBox.addItem(object.toString(), object.getValue());
		select(listBox, object);
	}

	public static void select(ListBox listBox, String object) {
		if (object == null) {
			return;
		}
		int itemCount = listBox.getItemCount();
		for (int x = 0; x < itemCount; ++x) {
			String itemValue = listBox.getValue(x);
			if (object.equals(itemValue)) {
				if (!listBox.isItemSelected(x)) {
					listBox.setSelectedIndex(x);
				}
				return;
			}
		}
		// Plug in the String itself as the value
		listBox.addItem(object, object);

		// Recurse - this will succeed since we just added this object to our list.
		select(listBox, object);
	}

	public static void select(ListBox listBox, AbstractDomainObject object) {
		if (object == null) {
			return;
		}
		final String value = object.getId().toString();
		int itemCount = listBox.getItemCount();
		for (int x = 0; x < itemCount; ++x) {
			String itemValue = listBox.getValue(x);
			if (value.equals(itemValue)) {
				if (!listBox.isItemSelected(x)) {
					listBox.setSelectedIndex(x);
				}
				return;
			}
		}
		listBox.addItem(object.toString(), object.getId().toString());
		select(listBox, object);
	}

	public static <T extends AbstractReferenceValue> T getSelectedReferenceValue(ListBox listBox, List<T> values) {
		int index = listBox.getSelectedIndex();
		if (index >= 0) {
			String value = listBox.getValue(index);
			for (T rV : values) {
				if (value.equals(rV.getValue())) {
					return rV;
				}
			}
		}
		return null;
	}

	public static <T extends AbstractDomainObject> T getSelectedDomainValue(ListBox listBox, List<T> values) {
		int index = listBox.getSelectedIndex();
		if (index >= 0) {
			String value = listBox.getValue(index);
			for (T rV : values) {
				if (value.equals(rV.getId().toString())) {
					return rV;
				}
			}
		}
		return null;
	}

	public static PopupPanel createPopupPanel(final UIObject relativeTo) {
		return createPopupPanel(relativeTo, true, true);
	}

	public static PopupPanel createPopupPanel(final UIObject relativeTo, final boolean hideOnScroll,
			final boolean hideOnResize) {
		final PopupPanel popupPanel = new PopupPanel(true);
		CommonGinjector.get.instance().getEventBus().addHandler(ScrollEvent.getType(), new ScrollHandler() {
			@Override
			public void onScroll(ScrollEvent event) {
				if (popupPanel.isShowing()) {
					if (relativeTo != null && !hideOnScroll) {
						popupPanel.showRelativeTo(relativeTo);
					} else {
						popupPanel.hide();
					}
				}
			}
		});
		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				if (relativeTo != null && !hideOnResize) {
					popupPanel.showRelativeTo(relativeTo);
				} else {
					popupPanel.hide();
				}
			}
		});
		return popupPanel;
	}

}
