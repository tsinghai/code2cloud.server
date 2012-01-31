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
package com.tasktop.c2c.server.common.tests.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class TestResourceUtil {

	/**
	 * compute a resource folder for test files. Usually this will compute the path relative to the project folder. For
	 * example, if a prefix is specified as "resources" and the test class is <code>com.tasktop.code.FooTest</code> the
	 * resulting project folder will be <tt>resources/FooTest</tt>
	 * 
	 * @param prefix
	 *            the path prefix, for example "resources"
	 * @param testClass
	 *            the class under test
	 * 
	 * @throws exceptions
	 *             if the folder doesn't exist or can't be computed
	 * @return the folder, which is guaranteed to exist and be a directory
	 */
	public static File computeResourceFolder(String prefix, Class<?> testClass) {
		URL resource = testClass.getResource(testClass.getSimpleName() + ".class");
		String resourceUri;
		try {
			resourceUri = resource.toURI().toString();
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
		if (resourceUri.startsWith("file:")) {
			String path = resourceUri.substring(5);
			path = path.replaceAll("/{2,}", "/");
			path = path.replaceAll("%20", " ");
			if (path.matches("/[A-Z]:/")) {
				path = path.substring(1);
			}
			String fqnPathPart = testClass.getName().replace('.', '/') + ".class";
			if (!path.endsWith(fqnPathPart)) {
				throw new IllegalStateException(path + " does not end with " + fqnPathPart);
			}
			path = path.substring(0, path.length() - fqnPathPart.length());
			File pathFile = new File(path);
			if (!pathFile.exists()) {
				throw new IllegalStateException("path doesn't exist: " + pathFile);
			}
			if (!pathFile.isDirectory()) {
				throw new IllegalStateException("not a directory: " + pathFile);
			}
			if (!pathFile.getName().equals("test-classes")) {
				throw new IllegalStateException("expected test-classes folder: " + pathFile);
			}
			pathFile = pathFile.getParentFile().getParentFile();
			if (prefix != null) {
				pathFile = new File(pathFile, prefix);
			}
			pathFile = new File(pathFile, testClass.getSimpleName());
			if (!pathFile.exists()) {
				throw new IllegalStateException("path doesn't exist: " + pathFile);
			}
			if (!pathFile.isDirectory()) {
				throw new IllegalStateException("not a directory: " + pathFile);
			}
			return pathFile;
		} else {
			throw new IllegalStateException("Unsupported URI format: " + resourceUri);
		}
	}
}
