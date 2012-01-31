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
public class SingleValueChooser<T> extends AbstractValueChooser<T> implements HasValue<T>, IsEditor<Editor<T>> {
	public SingleValueChooser(SuggestOracle suggest) {
		super(suggest);
	}

	private T value;

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
		updateUi();
	}

	@Override
	protected void addValue(T value) {
		if (this.value == null || !this.value.equals(value)) {
			setValue(value);
			fireValueChanged();
		}
	}

	@Override
	public void removeValue(Object value) {
		setValue(null);
		fireValueChanged();
	}

	@Override
	protected List<T> getValues() {
		if (value == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(value);
	}

	@Override
	public void fireValueChanged() {
		ValueChangeEvent.fire(this, getValue());
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	public void setOriginalValue(T value) {
		if (value == null) {
			setOriginalValues(new ArrayList<T>());
		} else {
			setOriginalValues(Collections.singletonList(value));
		}
	}

	private Editor<T> editor = null;

	@Override
	public Editor<T> asEditor() {
		if (editor == null) {
			editor = TakesValueEditor.of(this);
		}
		return editor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
	 */
	@Override
	public void setValue(T value, boolean fireEvents) {
		setValue(value);
		if (fireEvents) {
			fireValueChanged();
		}
	}

}
