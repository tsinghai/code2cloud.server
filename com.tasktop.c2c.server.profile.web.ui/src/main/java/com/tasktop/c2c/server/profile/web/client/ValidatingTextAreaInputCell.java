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

import java.util.Map;


import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class ValidatingTextAreaInputCell extends TextAreaInputCell {

	private final String fieldName;
	private final Map<String, String> errorMap;

	public ValidatingTextAreaInputCell(String newFieldName, Map<String, String> errorTable) {
		fieldName = newFieldName;
		errorMap = errorTable;
	}

	@Override
	protected void finishEditing(Element parent, String value, Object key, ValueUpdater<String> valueUpdater) {

		// We surround the input element with a <span>, so we need to do an extra level of child traversal to get at it.
		if (ValidationUtils.hasError(key, fieldName, errorMap)) {
			super.finishEditing(getInputElement(parent), value, key, valueUpdater);
		} else {
			// If there's no error, there's no surrounding block - make our normal call.
			super.finishEditing(parent, value, key, valueUpdater);
		}
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {

		if (ValidationUtils.hasError(context.getKey(), fieldName, errorMap)) {
			renderError(context, value, sb);
		} else {
			super.render(context, value, sb);
		}
	}

	private void renderError(Context context, String value, SafeHtmlBuilder sb) {

		// Tack on our surrounding highlight.
		sb.appendHtmlConstant("<div class=\"errorLabelWrapper\">");

		super.render(context, value, sb);

		// Also add in a space for our validation failure message.
		sb.appendHtmlConstant("<div class=\"errorLabel\">");
		sb.appendEscaped(ValidationUtils.getErrorMessage(context.getKey(), fieldName, errorMap));
		sb.appendHtmlConstant("</div></div>");
	}

}
