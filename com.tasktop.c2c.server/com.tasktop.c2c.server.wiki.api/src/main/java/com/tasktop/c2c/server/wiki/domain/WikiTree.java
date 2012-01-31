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
package com.tasktop.c2c.server.wiki.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class WikiTree {

	// FIXME hacky one level deep.
	public static final WikiTree construtTreeModel(List<Page> allpages) {
		Map<String, WikiTree> treesByDirectory = new HashMap<String, WikiTree>();

		WikiTree root = new WikiTree("/", new ArrayList<WikiTree>());
		treesByDirectory.put("/", root);

		for (Page page : allpages) {
			int lastSlash = page.getPath().lastIndexOf("/");
			if (lastSlash == -1) {
				root.getChildren().add(new WikiTree(page));
			} else {
				String dirName = page.getPath().substring(0, lastSlash);
				WikiTree dirTree = treesByDirectory.get(dirName);
				if (dirTree == null) {
					dirTree = new WikiTree(dirName, new ArrayList<WikiTree>());
					root.getChildren().add(dirTree);
					treesByDirectory.put(dirName, dirTree);
				}
				dirTree.getChildren().add(new WikiTree(page));
			}
		}

		return root;
	}

	public enum Type {
		DIRECTORY, PAGE_HEADER, PAGE_OUTLINE_ITEM, NO_OUTLINE
	};

	private Type type;
	private String path;
	private Page page;
	private PageOutlineItem pageOutlineItem;
	private List<WikiTree> children;

	public WikiTree() {

	}

	public WikiTree(String path, List<WikiTree> children) {
		this.path = path;
		this.children = children;
		this.type = Type.DIRECTORY;
	}

	public WikiTree(Page page) {
		this.page = page;
		this.type = Type.PAGE_HEADER;
		this.children = Collections.emptyList();
	}

	public WikiTree(Page page, PageOutlineItem pageOutlineItem) {
		this.page = page;
		this.pageOutlineItem = pageOutlineItem;
		this.type = Type.PAGE_OUTLINE_ITEM;
		this.children = Collections.emptyList();
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the page
	 */
	public Page getPage() {
		return page;
	}

	/**
	 * @param page
	 *            the page to set
	 */
	public void setPage(Page page) {
		this.page = page;
	}

	/**
	 * @return the children
	 */
	public List<WikiTree> getChildren() {
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<WikiTree> children) {
		this.children = children;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the pageOutline
	 */
	public PageOutlineItem getPageOutlineItem() {
		return pageOutlineItem;
	}

}
