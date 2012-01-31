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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Node;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;

/**
 * utility for working with navigation
 */
public class Navigation {
	/**
	 * get request parameters that were passed on the {@link History#getToken() history token}. The history token is
	 * used for parameters so that the page doesn't have to reload.
	 */
	public static Map<String, List<String>> getNavigationParameterMap() {
		Map<String, List<String>> parameters = new HashMap<String, List<String>>();

		String currentToken = History.getToken();
		if (currentToken != null) {
			int idxOfQueryStringSeparator = currentToken.indexOf('?');
			if (idxOfQueryStringSeparator != -1 && idxOfQueryStringSeparator < (currentToken.length() - 1)) {
				String queryString = currentToken.substring(idxOfQueryStringSeparator + 1);
				String[] parts = queryString.split("&");
				for (String part : parts) {
					String[] keyValue = part.split("=");
					if (keyValue.length == 2) {
						String key = URL.decode(keyValue[0]);
						String value = URL.decode(keyValue[1]);
						List<String> values = parameters.get(key);
						if (values == null) {
							values = new ArrayList<String>(1);
							parameters.put(key, values);
						}
						values.add(value);
					}
				}
			}
		}
		return parameters;
	}

	public static String appendQueryParameterToUrl(String url, String paramName, String paramValue) {

		// Make this method null-safe - some calling paths start with a null URL.
		String retUrl = (url == null ? "" : url);

		String encodedValue = URL.encode(paramValue);
		String encodedName = URL.encode(paramName);

		// Check if we already have some parameters in this URL.
		if (retUrl.indexOf('?') > 0) {
			// Already have a parameter, so the next one will be added on with &
			retUrl += "&";
		} else {
			// No parameters yet, so indicate the first one with ?
			retUrl += "?";
		}

		// Attach the rest of our parameter now.
		retUrl += encodedName + "=" + encodedValue;

		return retUrl;
	}

	/**
	 * show the element with the given id
	 * 
	 * @return true if the element was found
	 */
	public static boolean showIdElement(Element element, String elementId) {
		Element idElement = findElementById(element, elementId);
		if (idElement != null) {
			idElement.scrollIntoView();
			return true;
		}
		return false;
	}

	/**
	 * find an element by its id
	 * 
	 * @return the element, or null if it could not be found
	 */
	private static Element findElementById(Element element, String elementId) {
		String id = element.getAttribute("id");
		if (id != null && elementId.equals(id)) {
			return element;
		}
		for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element e = findElementById((Element) child, elementId);
				if (e != null) {
					return e;
				}
			}
		}
		return null;
	}
}
