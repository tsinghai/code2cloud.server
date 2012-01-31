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
package com.tasktop.c2c.server.wiki.web.ui.client.view;

import java.util.LinkedList;
import java.util.Queue;


import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.ui.FocusPanel;
import com.tasktop.c2c.server.wiki.domain.WikiTree;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class WikiTreeView extends FocusPanel {

	private CellTree cellTree;

	public WikiTreeView() {
		intitCellTree();
	}

	private void intitCellTree() {

	}

	public void setWikiTree(String projectId, WikiTree tree) {
		WikiTreeModel viewModel = new WikiTreeModel(projectId);
		cellTree = new CellTree(viewModel, tree, WikiTreeViewResources.get.resources);
		// cellTree.setAnimation(SlideAnimation.create());
		cellTree.setAnimationEnabled(false);

		cellTree.addOpenHandler(new OpenHandler<TreeNode>() {

			@Override
			public void onOpen(OpenEvent<TreeNode> event) {
				removeUnnededStyle();

			}

		});
		setWidget(cellTree);
	}

	// Hack to avoid overflow-x: hidden; overflow-y: hidden;
	// I believe this is a bug in the 2.4 CellTree impl where overflows are set to hidden to manage animations.
	private void removeUnnededStyle() {
		Queue<Element> nodeQueue = new LinkedList<Element>();
		nodeQueue.add(cellTree.getElement());

		while (!nodeQueue.isEmpty()) {
			Element node = nodeQueue.poll();

			try {
				String style = node.getAttribute("style");
				if (style != null) {
					if (style != null && style.trim().equals("overflow-x: hidden; overflow-y: hidden;")) {
						node.removeAttribute("style");
					}
				}
			} catch (Exception e) {
				//
			}
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				Node child = node.getChildNodes().getItem(i);
				if (child instanceof Element) {
					nodeQueue.add((Element) child);
				}
			}
		}

	}

}
