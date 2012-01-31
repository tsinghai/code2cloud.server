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
package com.tasktop.c2c.server.common.web.client.widgets;

import java.math.BigDecimal;
import java.text.ParseException;

import com.google.gwt.dom.client.Document;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.ui.ValueBox;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class BigDecimalBox extends ValueBox<BigDecimal> {

	/**
	 * @param element
	 * @param renderer
	 * @param parser
	 */
	public BigDecimalBox() {
		super(Document.get().createTextInputElement(), new AbstractRenderer<BigDecimal>() {

			@Override
			public String render(BigDecimal object) {
				if (object == null) {
					return "";
				}
				return NumberFormat.getDecimalFormat().format(object);
			}

		}, new Parser<BigDecimal>() {

			@Override
			public BigDecimal parse(CharSequence text) throws ParseException {
				try {
					return new BigDecimal(text.toString());
				} catch (NumberFormatException e) {
					return null;
				}
			}
		});
	}
}
