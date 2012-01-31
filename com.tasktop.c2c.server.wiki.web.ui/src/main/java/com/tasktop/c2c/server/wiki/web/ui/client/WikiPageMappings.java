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
package com.tasktop.c2c.server.wiki.web.ui.client;


import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiEditPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiHomePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiViewPagePlace;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class WikiPageMappings {

	public static PageMapping ProjectWikiEditPage = new PageMapping(new ProjectWikiEditPagePlace.Tokenizer(),
			Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/wiki/edit/{" + ProjectWikiViewPagePlace.PAGE + ":*}",
			Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/wiki/new");
	public static PageMapping ProjectWikiViewPage = new PageMapping(new ProjectWikiViewPagePlace.Tokenizer(),
			Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/wiki/p/{" + ProjectWikiViewPagePlace.PAGE + ":*}");
	public static PageMapping ProjectWiki = new PageMapping(new ProjectWikiHomePlace.Tokenizer(), Path.PROJECT_BASE
			+ "/{" + Path.PROJECT_ID + "}/wiki/", Path.PROJECT_BASE + "/{" + Path.PROJECT_ID + "}/wiki/q/{"
			+ ProjectWikiHomePlace.QUERY_P + "}");

}
