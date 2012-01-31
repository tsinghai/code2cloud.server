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
package com.tasktop.c2c.server.profile.web.client;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;

// REVIEW FIXME Why not just use TextInputCell???? 
/**
 * This class is based on EditTextCell found in GWT 2.3.0. It has been altered to be uni-state (always in edit-mode).
 */
public class TextBoxCell extends AbstractEditableCell<String, TextBoxCell.ViewData> {

	interface Template extends SafeHtmlTemplates {
		@Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\"></input>")
		SafeHtml input(String value);
	}

	static class ViewData {
	}

	private static Template template;

	private final SafeHtmlRenderer<String> renderer;

	private TemplateDelegate templateDelegate;

	public static interface TemplateDelegate {
		SafeHtml getHtml(Context context, String string);
	}

	/**
	 * Construct a new EditTextCell that will use a {@link com.google.gwt.text.shared.SimpleSafeHtmlRenderer}.
	 */
	public TextBoxCell(TemplateDelegate templateDelegate) {
		this(SimpleSafeHtmlRenderer.getInstance());
		this.templateDelegate = templateDelegate;
	}

	/**
	 * Construct a new EditTextCell that will use a given {@link SafeHtmlRenderer} to render the value when not in edit
	 * mode.
	 * 
	 * @param renderer
	 *            a {@link SafeHtmlRenderer SafeHtmlRenderer<String>} instance
	 */
	public TextBoxCell(SafeHtmlRenderer<String> renderer) {
		super("click", "keyup", "keydown", "blur");
		if (template == null) {
			template = GWT.create(Template.class);
		}
		if (renderer == null) {
			throw new IllegalArgumentException("renderer == null");
		}
		this.renderer = renderer;
	}

	@Override
	public boolean isEditing(Context context, Element parent, String value) {
		return true;
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event,
			ValueUpdater<String> valueUpdater) {
		String type = event.getType();
		boolean keyUp = "keyup".equals(type);
		boolean keyDown = "keydown".equals(type);
		if (keyUp || keyDown) {
			updateValue(parent, valueUpdater);
		}
	}

	private InputElement findFirstInputElement(Element parent) {

		NodeList<Element> inputChildren = parent.getElementsByTagName("input");

		if (inputChildren == null || inputChildren.getLength() == 0) {
			return null;
		} else {
			// Return the first tag in the list.
			return (InputElement) inputChildren.getItem(0);
		}
	}

	private void updateValue(Element parent, ValueUpdater<String> valueUpdater) {

		InputElement input = findFirstInputElement(parent);
		if (input != null) {
			String value = input.getValue();
			if (valueUpdater != null) {
				valueUpdater.update(value);
			}
		}
	}

	/* This is the only method that has been changed from the original class */
	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		if (value == null) {
			value = "";
		}
		sb.append(templateDelegate.getHtml(context, value));
	}

	@Override
	public boolean resetFocus(Context context, Element parent, String value) {
		InputElement input = getInputElement(parent);
		input.focus();
		return true;
	}

	/**
	 * Get the input element in edit mode.
	 */
	private InputElement getInputElement(Element parent) {
		return parent.getFirstChild().<InputElement> cast();
	}
}
