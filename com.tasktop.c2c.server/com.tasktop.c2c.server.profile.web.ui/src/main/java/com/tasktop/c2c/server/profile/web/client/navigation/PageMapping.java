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
package com.tasktop.c2c.server.profile.web.client.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.common.web.client.navigation.PathMapping;
import com.tasktop.c2c.server.common.web.client.navigation.PathMapping.PathInfo;

/**
 * @author cmorgan
 * 
 */
public class PageMapping {

	private static List<PageMapping> registeredMappings = new ArrayList<PageMapping>();

	public static List<PageMapping> getRegisteredMappings() {
		return registeredMappings;
	}

	public static void register(PageMapping mapping) {
		registeredMappings.add(mapping);
	}

	private final PlaceTokenizer<?> tokenizer;
	private final PathMapping[] pathMappings;

	/** Construct a new pagemapping and register it. */
	public PageMapping(PlaceTokenizer<?> newTokenizer, String firstPath, String... extraPaths) {
		this.tokenizer = newTokenizer;

		// Fill up our PathMappings now
		PathMapping[] mapping = new PathMapping[extraPaths.length + 1];
		mapping[0] = new PathMapping(firstPath);

		for (int i = 0; i < extraPaths.length; i++) {
			mapping[i + 1] = new PathMapping(extraPaths[i]);
		}

		this.pathMappings = mapping;
		register(this);
	}

	public PlaceTokenizer<?> getTokenizer() {
		return tokenizer;
	}

	private Path getPath(PathInfo info) {
		for (PathMapping curMapping : pathMappings) {
			if (curMapping.getPath().matches(info.parts)) {
				return curMapping.getPath();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public String getUrl() {
		// Search for a URL with no arguments
		return getUrlForNamedArgs(Collections.EMPTY_MAP);
	}

	public String getUrlForNamedArgs(Map<String, String> args) {
		// Go through our PathMappings and see if any of them have the requested args.
		Set<String> namedArgs = args.keySet();
		for (PathMapping curMapping : pathMappings) {
			if (curMapping.getPath().getArgumentCount() == namedArgs.size()) {
				boolean allMatch = true;
				for (String curArg : namedArgs) {
					// Check to see if this one is in the map - if not, bail out.
					if (!curMapping.getPath().containsNamedArgument(curArg)) {
						allMatch = false;
						break;
					}
				}

				if (allMatch) {
					// We got a match - construct and return our URL.
					return curMapping.getPath().uri(args);
				}
			}
		}

		return null;
	}

	public boolean matches(PathInfo info) {
		// If we match one of our paths, then return true.
		return getPath(info) != null;
	}

	public PathMapping[] getPathMappings() {
		return this.pathMappings;
	}

	public static Place getPlaceForUrl(String url) {
		PageMapping pageMappings = getPageMappingsForUrl(url);
		if (pageMappings == null) {
			return null;
		} else {
			return pageMappings.tokenizer.getPlace(url);
		}
	}

	public static Args getPathArgsForUrl(String url) {
		PageMapping pageMappings = getPageMappingsForUrl(url);
		if (pageMappings == null) {
			return null;
		} else {
			PathInfo info = PathMapping.computePathInfo(url);
			return pageMappings.getPath(info).configureArgs(url);
		}
	}

	private static PageMapping getPageMappingsForUrl(String url) {
		PathInfo info = PathMapping.computePathInfo(url);
		for (PageMapping curMapping : PageMapping.getRegisteredMappings()) {
			if (curMapping.matches(info)) {
				return curMapping;
			}
		}
		return null;
	}
}
