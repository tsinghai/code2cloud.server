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
package com.tasktop.c2c.server.profile.web.client.util;

import com.tasktop.c2c.server.profile.web.client.place.Section;

/**
 * Builds window titles by parsing {@link Section} names, page titles, and/or project names. Also appends the Product
 * name to all titles
 * 
 * @author Jennifer Hickey
 * 
 */
abstract public class WindowTitleBuilder {

	public static String PRODUCT_NAME = "Code2Cloud";

	public static String createWindowTitle(Section section) {
		String feature = getSectionTitle(section);
		if (feature.isEmpty()) {
			return PRODUCT_NAME;
		}
		return feature + " - " + PRODUCT_NAME;
	}

	public static String createWindowTitle(Section section, String projectName) {
		String feature = getSectionTitle(section);
		StringBuilder titleBuilder = new StringBuilder();
		if (!feature.isEmpty()) {
			titleBuilder.append(feature).append(" - ");
		}
		titleBuilder.append(projectName).append(" - ").append(PRODUCT_NAME);
		return titleBuilder.toString();
	}

	public static String createWindowTitle(String pageTitle) {
		return pageTitle + " - " + PRODUCT_NAME;
	}

	public static String createWindowTitle(String pageTitle, String projectName) {
		return pageTitle + " - " + projectName + " - " + PRODUCT_NAME;
	}

	private static String getSectionTitle(Section section) {
		switch (section) {
		case HOME:
			return "Home";
		case DASHBOARD:
			return "Dashboard";
		case TASKS:
			return "Tasks";
		case BUILDS:
			return "Builds";
		case DEPLOYMENTS:
			return "Deployments";
		case TEAM:
			return "Team";
		case WIKI:
			return "Wiki";
		case ADMIN:
			return "Admin";
		default:
			return "";
		}
	}
}
