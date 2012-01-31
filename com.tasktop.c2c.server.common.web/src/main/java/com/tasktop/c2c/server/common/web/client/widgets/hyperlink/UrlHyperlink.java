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
package com.tasktop.c2c.server.common.web.client.widgets.hyperlink;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

/**
 * An URL Hyperlink. Creates {@link History#newItem(String) new history events}
 * 
 * @author David Green (Tasktop Technologies Inc.)
 */
public class UrlHyperlink extends Hyperlink {

	public UrlHyperlink() {
	}

	public UrlHyperlink(String uri, int offset, int length) {
		super(uri, offset, length);
	}

	@Override
	public void open() {
		String href = Window.Location.getHref();
		String uri = getUri();

		int idxOfHrefHash = href.indexOf('#');
		if (idxOfHrefHash != -1) {
			int idxOfUriHash = uri.indexOf('#');
			if (idxOfUriHash == idxOfHrefHash) {
				if (uri.substring(0, idxOfUriHash).equals(href.substring(0, idxOfHrefHash))) {
					String newHashTag = uri.substring(idxOfUriHash + 1);
					History.newItem(newHashTag);
					return;
				}
			}
		}

		Window.open(getUri(), getUri(), "");
	}

}
