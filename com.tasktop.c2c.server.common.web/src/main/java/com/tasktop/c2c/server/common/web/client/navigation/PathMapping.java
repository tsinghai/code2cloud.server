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
package com.tasktop.c2c.server.common.web.client.navigation;


public class PathMapping {

	public static final class PathInfo {
		public String[] parts;
	}

	public static PathInfo computePathInfo(String uri) {
		int indexOfParameterMarker = uri.indexOf('?');
		PathInfo pathInfo = new PathInfo();
		if (indexOfParameterMarker != -1) {
			uri = uri.substring(0, indexOfParameterMarker);
		}
		pathInfo.parts = uri.split("/");
		return pathInfo;
	}

	protected static PathMapping computePathMappingFromUri(Iterable<PathMapping> candidates, String uri) {
		PathInfo pathInfo = PathMapping.computePathInfo(uri);
		for (PathMapping mapping : candidates) {
			if (mapping.getPath().matches(pathInfo.parts)) {
				return mapping;
			}
		}
		return null;
	}

	private final Path path;

	public PathMapping(String path) {
		this.path = new Path(path);
		if (path.indexOf(Path.HASHTAG_DELIMITER) != -1) {
			throw new IllegalArgumentException("Paths cannot contain " + Path.HASHTAG_DELIMITER);
		}
	}

	public boolean matches(String path) {
		PathInfo pathInfo = computePathInfo(path);
		// Pass this to our underlying path object to see if it matches.
		return this.path.matches(pathInfo.parts);
	}

	public Path getPath() {
		return path;
	}

	public String uri(Object... args) {
		// Get our URI from our Path object.
		return path.uri(args);
	}
}
