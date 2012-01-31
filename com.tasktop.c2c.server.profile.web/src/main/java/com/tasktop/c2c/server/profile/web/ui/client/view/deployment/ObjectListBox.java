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

import com.google.gwt.user.client.ui.ListBox;

/**
 * Extend a listbox with some functionality to make it easier to use general objects with it. NOTE: If you interact with
 * the normal list box methods (setItem, ...), then the methods exposed here will not work correctly
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * @deprecated See gwt's ValueListBox
 */
@Deprecated
public class ObjectListBox<T> extends ListBox {

	public static interface Renderer<T> {
		String renderToString(T object);
	}

	private static class ToStringRenderer implements Renderer<Object> {
		public String renderToString(Object object) {
			return object.toString();
		}
	}

	public static ToStringRenderer DEFAULT_RENDER = new ToStringRenderer();

	private List<T> values = new ArrayList<T>();
	private Renderer<? super T> renderer;
	private String noSelectionText = null;

	public ObjectListBox(Renderer<? super T> renderer) {
		this.renderer = renderer;
	}

	public ObjectListBox() {
		this(DEFAULT_RENDER);
	}

	public void setValues(List<T> values) {
		this.values.clear();
		this.values.addAll(values);
		super.clear();
		if (noSelectionText != null) {
			super.addItem(noSelectionText);
		}

		for (T value : values) {
			super.addItem(renderer.renderToString(value));
		}
	}

	public void setValuesMaintainingSelection(List<T> values) {
		T selected = getValue();
		setValues(values);
		if (selected != null) {
			setValue(selected);
		}
	}

	public void setValuesMaintainingSelectionNoAdd(List<T> values) {
		T selected = getValue();
		setValues(values);
		if (selected != null && values.contains(selected)) {
			setValue(selected);
		}
	}

	/** Set the selected value of this listbox. If the supplied item is not in the listBox it will be added. */
	public void setValue(T value) {
		if (value == null) {
			if (noSelectionText == null) {
				clearSelection();
			} else {
				setSelectedIndex(0);
			}

			return;
		}

		int index = values.indexOf(value);
		int offset = noSelectionText == null ? 0 : 1;

		if (index != -1) {
			super.setSelectedIndex(index + offset);
		} else {
			List<T> newValues = new ArrayList<T>(values);
			newValues.add(value);
			setValues(newValues);
			super.setSelectedIndex(super.getItemCount() - 1);
		}
	}

	/**
	 * 
	 */
	public void clearSelection() {
		int selected = super.getSelectedIndex();
		if (selected != -1) {
			super.setItemSelected(selected, false);
		}
	}

	public T getValue() {
		int index = super.getSelectedIndex();
		int offset = noSelectionText == null ? 0 : 1;

		if (index == -1) {
			return null;
		} else if (index - offset == -1) {
			return null;
		}
		return values.get(index - offset);
	}

	/**
	 * @return the renderer
	 */
	public Renderer<? super T> getRenderer() {
		return renderer;
	}

	/**
	 * @param renderer
	 *            the renderer to set
	 */
	public void setRenderer(Renderer<? super T> renderer) {
		this.renderer = renderer;
	}

	/**
	 * @return the values
	 */
	public List<T> getValues() {
		return values;
	}

	/**
	 * 
	 */
	public void clearValues() {
		setValues(new ArrayList<T>());
	}

	/**
	 * @return the noSelectionText
	 */
	public String getNoSelectionText() {
		return noSelectionText;
	}

	/**
	 * @param noSelectionText
	 *            the noSelectionText to set
	 */
	public void setNoSelectionText(String noSelectionText) {
		this.noSelectionText = noSelectionText;
	}
}
