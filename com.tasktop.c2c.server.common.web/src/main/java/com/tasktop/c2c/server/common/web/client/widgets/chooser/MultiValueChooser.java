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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * @author David Green (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class MultiValueChooser<T> extends AbstractValueChooser<T> implements HasValue<List<T>>,
		IsEditor<Editor<List<T>>> {
	private List<T> values;

	public MultiValueChooser(SuggestOracle suggest) {
		super(suggest);
	}

	/**
	 * The values to choose
	 */
	public List<T> getValues() {
		if (values == null) {
			return Collections.emptyList();
		}
		return values;
	}

	public void setValues(List<T> values) {
		this.values = values;
		updateUi();
	}

	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<T>> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	protected void addValue(T value) {
		addValue(value, true);
	}

	private boolean addValueInternal(T value) {
		if (values == null || !values.contains(value)) {
			List<T> newValues = new ArrayList<T>();
			if (values != null) {
				newValues.addAll(values);
			}
			newValues.add(value);
			setValues(newValues);
			return true;
		}
		return false;
	}

	public void addValue(T value, boolean fireEvents) {
		if (addValueInternal(value) && fireEvents) {
			fireValueChanged();
		}
	}

	public void removeValue(Object value) {
		List<T> newValues = new ArrayList<T>();
		if (values != null) {
			newValues.addAll(values);
		}
		newValues.remove(value);
		setValues(newValues);
		fireValueChanged();
	}

	protected void fireValueChanged() {
		ValueChangeEvent.fire(this, getValues());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.HasValue#getValue()
	 */
	@Override
	public List<T> getValue() {
		return getValues();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(List<T> value) {
		setValues(value);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
	 */
	@Override
	public void setValue(List<T> value, boolean fireEvents) {
		setValue(value);
		if (fireEvents) {
			fireValueChanged();
		}

	}

	private Editor<List<T>> editor = null;

	@Override
	public Editor<List<T>> asEditor() {
		if (editor == null) {
			editor = TakesValueEditor.of(this);
		}
		return editor;
	}
}
