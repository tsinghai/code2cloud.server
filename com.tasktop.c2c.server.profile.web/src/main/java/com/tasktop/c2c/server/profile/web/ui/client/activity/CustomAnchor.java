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
package com.tasktop.c2c.server.profile.web.ui.client.activity;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.shared.DirectionEstimator;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Anchor;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class CustomAnchor extends Anchor {

	private void addClickHandler() {
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String href = getHref();
				if (StringUtils.hasText(href)) {
					int hashIndex = href.indexOf("#");
					String afterHash = href.substring(hashIndex + 1, href.length());
					Place place = AppGinjector.get.instance().getPlaceHistoryMapper().getPlace(afterHash);
					if (place != null) {
						event.preventDefault();
						AppGinjector.get.instance().getPlaceController().goTo(place);
					}
				}
			}
		});
	}

	public CustomAnchor() {
		super();
		addClickHandler();
	}

	public CustomAnchor(boolean useDefaultHref) {
		super(useDefaultHref);
		addClickHandler();
	}

	public CustomAnchor(SafeHtml html) {
		super(html);
		addClickHandler();
	}

	public CustomAnchor(SafeHtml html, Direction dir) {
		super(html, dir);
		addClickHandler();
	}

	public CustomAnchor(SafeHtml html, DirectionEstimator directionEstimator) {
		super(html, directionEstimator);
		addClickHandler();
	}

	public CustomAnchor(String text) {
		super(text);
		addClickHandler();
	}

	public CustomAnchor(String text, Direction dir) {
		super(text, dir);
		addClickHandler();
	}

	public CustomAnchor(String text, DirectionEstimator directionEstimator) {
		super(text, directionEstimator);
		addClickHandler();
	}

	public CustomAnchor(String text, boolean asHtml) {
		super(text, asHtml);
		addClickHandler();
	}

	public CustomAnchor(SafeHtml html, String href) {
		super(html, href);
		addClickHandler();
	}

	public CustomAnchor(SafeHtml html, Direction dir, String href) {
		super(html, dir, href);
		addClickHandler();
	}

	public CustomAnchor(SafeHtml html, DirectionEstimator directionEstimator, String href) {
		super(html, directionEstimator, href);
		addClickHandler();
	}

	public CustomAnchor(String text, String href) {
		super(text, href);
		addClickHandler();
	}

	public CustomAnchor(String text, Direction dir, String href) {
		super(text, dir, href);
		addClickHandler();
	}

	public CustomAnchor(String text, DirectionEstimator directionEstimator, String href) {
		super(text, directionEstimator, href);
		addClickHandler();
	}

	public CustomAnchor(String text, boolean asHTML, String href) {
		super(text, asHTML, href);
		addClickHandler();
	}

	public CustomAnchor(SafeHtml html, String href, String target) {
		super(html, href, target);
		addClickHandler();
	}

	public CustomAnchor(String text, String href, String target) {
		super(text, href, target);
		addClickHandler();
	}

	public CustomAnchor(String text, boolean asHtml, String href, String target) {
		super(text, asHtml, href, target);
		addClickHandler();
	}
}
