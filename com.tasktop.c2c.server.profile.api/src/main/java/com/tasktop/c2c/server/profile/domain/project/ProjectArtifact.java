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
package com.tasktop.c2c.server.profile.domain.project;

/**
 * An artifact from a release. TODO: The inputStream is when creating an artifact. This must be pushed to a non-GWT DO.
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * 
 */
@SuppressWarnings("serial")
public class ProjectArtifact extends AbstractEntity {
	private String path;
	private String name;
	private String url;
	private String sha1Hash;

	// private transient InputStream inputStream;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	// /**
	// * only specified on create
	// */
	// public InputStream getInputStream() {
	// return inputStream;
	// }
	//
	// /**
	// * only specified on create
	// */
	// public void setInputStream(InputStream inputStream) {
	// this.inputStream = inputStream;
	// }

	public String getSha1Hash() {
		return sha1Hash;
	}

	public void setSha1Hash(String sha1Hash) {
		this.sha1Hash = sha1Hash;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
}
