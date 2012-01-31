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
package com.tasktop.c2c.server.common.web.client.widgets.hyperlink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineHTML;
import com.tasktop.c2c.server.common.web.client.widgets.SpanPanel;

/**
 * @author David Green (Tasktop Technologies Inc.)
 */
public class HyperlinkingLabel extends Composite implements HasText {

	private SpanPanel content;

	private String text;

	private HyperlinkClickListener listener;

	private List<HyperlinkDetector> hyperlinkDetectors = new ArrayList<HyperlinkDetector>();

	private List<Hyperlink> hyperlinks;

	/**
	 * construct with a default {@link UrlHyperlinkDetector}
	 * 
	 * @see #setText(String)
	 */
	public HyperlinkingLabel() {
		this(new UrlHyperlinkDetector());
	}

	/**
	 * construct with a default {@link UrlHyperlinkDetector}
	 * 
	 * @param text
	 *            the text to render
	 */
	public HyperlinkingLabel(String text) {
		this(text, new UrlHyperlinkDetector());
	}

	private void initContent() {
		content = new SpanPanel();
		initWidget(content);
	}

	/**
	 * 
	 * @param text
	 *            the text to render
	 * @param detectors
	 *            the hyperlink detectors to use
	 */
	public HyperlinkingLabel(String text, HyperlinkDetector... detectors) {
		this(detectors);
		setText(text);
	}

	/**
	 * @see #setText(String)
	 */
	public HyperlinkingLabel(HyperlinkDetector... detectors) {
		initContent();
		for (HyperlinkDetector detector : detectors) {
			hyperlinkDetectors.add(detector);
		}
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		updateContent();
	}

	public List<Hyperlink> getHyperlinks() {
		return hyperlinks == null ? new ArrayList<Hyperlink>() : hyperlinks;
	}

	private void updateContent() {
		content.clear();
		hyperlinks = null;
		if (text != null && text.length() > 0) {
			if (!hyperlinkDetectors.isEmpty()) {
				for (HyperlinkDetector detector : hyperlinkDetectors) {
					List<Hyperlink> detectetdHyperlinks = detector.detectHyperlinks(text);
					if (detectetdHyperlinks != null && !detectetdHyperlinks.isEmpty()) {
						if (hyperlinks == null) {
							hyperlinks = detectetdHyperlinks;
						} else {
							hyperlinks.addAll(detectetdHyperlinks);
						}
					}
				}
			}
			if (hyperlinks == null || hyperlinks.isEmpty()) {
				addTextWithNewlines(text);
			} else {
				Collections.sort(hyperlinks, new HyperlinkComparator());
				int offset = 0;
				for (final Hyperlink hyperlink : hyperlinks) {
					if (hyperlink.getLength() > 0) {
						if (hyperlink.getOffset() > offset) {
							String leadingText = text.substring(offset, hyperlink.getOffset());
							addTextWithNewlines(leadingText);
						}
						int newOffset = hyperlink.getOffset() + hyperlink.getLength();
						String hyperlinkText = text.substring(hyperlink.getOffset(), newOffset);
						Anchor anchor = new Anchor(SafeHtmlUtils.fromString(hyperlinkText));
						content.add(anchor);
						anchor.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (listener != null) {
									listener.hyperlinkClicked(event, hyperlink);
								} else {
									hyperlink.open();
								}
							}
						});
						offset = newOffset;
					}
				}
				if (offset < text.length()) {
					String remainingText = text.substring(offset, text.length());
					addTextWithNewlines(remainingText);
				}
			}
		}
	}

	private void addTextWithNewlines(String text) {
		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) {
				content.add(new InlineHTML("<br>"));
			}
			content.add(new InlineHTML(SafeHtmlUtils.fromString(lines[i])));
		}
		// Handle case where we don't generate a split.
		if (text.endsWith("\n")) {
			content.add(new InlineHTML("<br>"));
		}
	}

	public HyperlinkClickListener getListener() {
		return listener;
	}

	public void setListener(HyperlinkClickListener listener) {
		this.listener = listener;
	}

	public void addHyperlinkDetector(HyperlinkDetector detector) {
		hyperlinkDetectors.add(detector);
		updateContent();
	}

	public void removeHyperlinkDetector(HyperlinkDetector detector) {
		hyperlinkDetectors.remove(detector);
		updateContent();
	}

}
