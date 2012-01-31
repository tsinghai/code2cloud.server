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


import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.tasktop.c2c.server.common.web.client.widgets.hyperlink.HyperlinkDetector;
import com.tasktop.c2c.server.common.web.client.widgets.hyperlink.HyperlinkingLabel;

public abstract class HasExpandingTextPanel extends Composite {

	private static final String MORE_LINES_TEXT = " \u2026";
	private static final String EXPAND_BUTTON_TEXT = "Show";
	private static final String COLLAPSE_BUTTON_TEXT = "Hide";

	@UiField
	public HyperlinkingLabel expandingTextLabel;
	@UiField
	public Anchor expandButton;

	private String fullText;
	private boolean expanded = false;

	public HasExpandingTextPanel() {

	}

	protected void setupWidgets() {
		expandButton.setVisible(false);
		expandButton.setText(EXPAND_BUTTON_TEXT);
		expandButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				toggleExpand();
			}
		});
	}

	public void setExpandingText(String texty) {
		this.fullText = texty == null ? "" : texty.trim();

		int firstLineIndex = fullText.indexOf("\n");
		if (firstLineIndex == -1) {
			expandingTextLabel.setText(fullText);
			expandButton.setVisible(false);
		} else {
			expandingTextLabel.setText(fullText.substring(0, firstLineIndex) + MORE_LINES_TEXT);
			expandButton.setVisible(true);
			expandButton.setText(EXPAND_BUTTON_TEXT);
		}
	}

	public void addHyperlinkDetector(HyperlinkDetector detector) {
		expandingTextLabel.addHyperlinkDetector(detector);
	}

	private void toggleExpand() {
		expanded = !expanded;
		Element toStyle = expandButton.getElement().getParentElement(); // UGLY STYLE HACK
		if (expanded) {
			expandButton.setText(COLLAPSE_BUTTON_TEXT);
			expandingTextLabel.setText(fullText);
			toStyle.removeClassName("oneline");
			toStyle.addClassName("open");
		} else {
			expandButton.setText(EXPAND_BUTTON_TEXT);
			int firstLineIndex = fullText.indexOf("\n");
			expandingTextLabel.setText(fullText.substring(0, firstLineIndex) + MORE_LINES_TEXT);
			toStyle.addClassName("oneline");
			toStyle.removeClassName("open");
		}
	}
}
