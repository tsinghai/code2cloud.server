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
package com.tasktop.c2c.server.hudson.plugin.auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;

class FilterClassLoader extends ClassLoader {
	public static class FilterEnumeration implements Enumeration<URL> {

		private final Enumeration<URL> delegate;
		private URL next = null;

		public FilterEnumeration(Enumeration<URL> delegate) {
			this.delegate = delegate;
			advance();
		}

		public boolean hasMoreElements() {
			return next != null;
		}

		public URL nextElement() {
			if (next == null) {
				throw new NoSuchElementException();
			}
			URL value = next;
			advance();
			System.out.println("element: " + value);
			return value;
		}

		private void advance() {
			next = null;
			while (delegate.hasMoreElements()) {
				next = delegate.nextElement();
				if (!filtered(next)) {
					break;
				}
				next = null;
			}
		}

		private boolean filtered(URL item) {
			String string = item.toString();
			return string.contains("org.acegisecurity") || string.contains("acegi-security");
		}
	}

	private ClassLoader delegate;

	FilterClassLoader(ClassLoader delegate) {
		super();
		this.delegate = delegate;
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return delegate.loadClass(name);
	}

	public URL getResource(String name) {
		return delegate.getResource(name);
	}

	public Enumeration<URL> getResources(String name) throws IOException {
		return new FilterEnumeration(delegate.getResources(name));
	}

	public InputStream getResourceAsStream(String name) {
		return delegate.getResourceAsStream(name);
	}

	public void setDefaultAssertionStatus(boolean enabled) {
		delegate.setDefaultAssertionStatus(enabled);
	}

	public void setPackageAssertionStatus(String packageName, boolean enabled) {
		delegate.setPackageAssertionStatus(packageName, enabled);
	}

	public void setClassAssertionStatus(String className, boolean enabled) {
		delegate.setClassAssertionStatus(className, enabled);
	}

	public void clearAssertionStatus() {
		delegate.clearAssertionStatus();
	}

}
