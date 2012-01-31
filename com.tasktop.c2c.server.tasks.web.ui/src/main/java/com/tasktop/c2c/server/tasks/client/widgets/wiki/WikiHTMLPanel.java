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
package com.tasktop.c2c.server.tasks.client.widgets.wiki;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.common.web.client.navigation.PathMapping;
import com.tasktop.c2c.server.common.web.client.navigation.PathMapping.PathInfo;
import com.tasktop.c2c.server.tasks.client.widgets.TaskAnchor;
import com.tasktop.c2c.server.tasks.client.widgets.TaskAnchorManager;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class WikiHTMLPanel extends HTMLPanel {

	public WikiHTMLPanel() {
		super(SafeHtmlUtils.EMPTY_SAFE_HTML);
	}

	// These constructors have been added to keep compatibility with the superclass (since constructors aren't inherited
	// in Java, this is necessary).
	public WikiHTMLPanel(String html) {
		super(html);
	}

	public void setWikiHTML(String wikiHtml) {
		// First, clear out previous contents.
		super.clear();

		// Add all of our HTML to our element so that it gets converted to DOM format.
		this.getElement().setInnerHTML(wikiHtml);

		// Then, find all of our anchors and see if they are task links
		NodeList<Element> anchors = this.getElement().getElementsByTagName("a");

		for (int i = 0; i < anchors.getLength(); i++) {
			Element curElem = anchors.getItem(i);
			// Grab out our href
			String href = curElem.getAttribute("href");

			if (isTaskAnchor(href)) {

				// We need to do some fancy footwork here in order to inject a TaskAnchor into the body of this page -
				// hold on tight, it's going to get a bit bumpy.

				// First, grab the text inside the anchor - we'll use this and the href to construct our TaskAnchor.
				String label = curElem.getInnerText();

				// Next, replace our existing anchor with a <span>-based HTMLPanel - we do this to ensure we are
				// preserving our location within the DOM tree (i.e. inserting our TaskAnchor at the right point in the
				// Document)
				HTMLPanel panel = new HTMLPanel("span", "") {
					@Override
					public void add(Widget widget) {
						// Very ugly hack - need to call onAttach(), but it's a protected method. Sooo, we insert that
						// call in the add() method, which we'll call one time further inside this method, to ensure
						// that the widget is attached (otherwise it won't bind, and we won't get the normal Widget
						// behaviour, which is required for our TaskAnchor hovers to work).
						// If you are reading this comment and you know of a better way to handle this, you are
						// honour-bound to implement it and remove this hack.
						onAttach();
						super.add(widget);
					}
				};
				curElem.getParentElement().replaceChild(panel.getElement(), curElem);

				// Next, wrap that span in an HTML widget - this is done so that we can then inject our TaskAnchor as a
				// widget.
				panel.add(createTaskAnchor(label, href));
			}
		}
	}

	@Override
	public void clear() {
		super.clear();
		this.getElement().setInnerHTML("");
	}

	private boolean isTaskAnchor(String href) {
		if (href.endsWith("#")) {
			// Take care of a special corner case
			return false;
		}

		// Check if we got back task anchor data - if we did, this is a task anchor
		return (getTaskAnchorData(href) != null);
	}

	private String[] getTaskAnchorData(String href) {
		String internalUrl = href.contains("#") ? href.substring(href.indexOf("#") + 1) : href;
		PathInfo info = PathMapping.computePathInfo(internalUrl);
		String[] retArray = null;

		// Now, do some checking to see if this is a Task path - task paths look like /projects/[projId]/task/[taskId]
		if (info.parts.length == 4 && (Path.PROJECT_BASE.equals(info.parts[0]) && "task".equals(info.parts[2]))) {

			// Store our project ID and our task ID in our return array.
			retArray = new String[2];
			retArray[0] = info.parts[1];
			retArray[1] = info.parts[3];
		}

		return retArray;
	}

	private TaskAnchor createTaskAnchor(String label, String href) {
		// Pull out our project identifier and task ID
		String[] taskAnchorParts = getTaskAnchorData(href);
		int taskId = Integer.parseInt(taskAnchorParts[1]);
		String projId = taskAnchorParts[0];

		// Create and return our task anchor now.
		return TaskAnchorManager.createAnchor(projId, taskId, label, href);
	}

}
