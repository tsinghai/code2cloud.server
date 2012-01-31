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
package com.tasktop.c2c.server.profile.web.ui.client.view.components;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.profile.web.ui.client.place.AppSectionPlace.AppSection;

public class AppSectionView extends AbstractComposite {

	interface Binder extends UiBinder<Widget, AppSectionView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);
	private static AppSectionView instance;

	public static AppSectionView getInstance() {
		if (instance == null) {
			instance = new AppSectionView();
		}

		return instance;
	}

	@UiField
	HTMLPanel buildContentPanel;

	private AppSection sectionToShow = AppSection.HEADER;

	private AppSectionView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setSectionToShow(AppSection section) {
		sectionToShow = section;
	}

	@Override
	protected void onAttach() {
		// First, call our superclass method to make sure attachment has happened.
		super.onAttach();

		// Then, hide our other components.
		showOnly();
	}

	private void showOnly() {
		// Now, hide everything that's not a part of the header.
		Element containerWrapper = getParentWidget();

		for (Element curElem = containerWrapper.getFirstChildElement(); curElem != null; curElem = curElem
				.getNextSiblingElement()) {
			String cssClass = curElem.getAttribute("class");
			// Only display this if it's our selected class
			if (cssClass != null && !cssClass.contains(sectionToShow.getStyleName())) {
				curElem.setAttribute("style", "display: none;");
			} else {
				// in case it was hidden previously, make sure we remove the style attribute
				curElem.removeAttribute("style");

				// also update the anchors in this section to point at the root of the frameset, otherwise only the
				// iframe will change when a link is clicked (that's bad).
				updateAnchors(curElem);
			}
		}
	}

	private void updateAnchors(Element elem) {
		// Get all of our anchors.
		NodeList<Element> anchorList = elem.getElementsByTagName("a");

		if (anchorList == null || anchorList.getLength() == 0) {
			// Nothing to do, bail out.
			return;
		}

		for (int i = 0; i < anchorList.getLength(); i++) {
			Element curElem = anchorList.getItem(i);

			// Only update this element if it doesn't currently have a target specified - this lets the default
			// behaviour remain for those that specify e.g. _blank as a target
			if (!curElem.hasAttribute("target")) {
				curElem.setAttribute("target", "_top");
			}
		}
	}

	private Element getParentWidget() {
		Element curWidget = buildContentPanel.getElement();

		while (curWidget.getParentElement() != null) {
			String styleName = curWidget.getAttribute("class");
			if (styleName.contains("container")) {
				// This is our container - return its parent, since it'll also contain the footer wrapper.
				return curWidget.getParentElement();
			} else {
				// Go one level up the tree.
				curWidget = curWidget.getParentElement();
			}
		}

		// Should never happen.
		return null;
	}
}
