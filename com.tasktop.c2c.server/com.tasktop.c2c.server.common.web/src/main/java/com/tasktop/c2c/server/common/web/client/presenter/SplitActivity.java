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
package com.tasktop.c2c.server.common.web.client.presenter;


import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.tasktop.c2c.server.common.web.client.presenter.ActivityProxy.ProxyReturn;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class SplitActivity implements Activity {

	private final ProxyReturn delegate;

	public SplitActivity(ProxyReturn delegate) {
		this.delegate = delegate;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		delegate.start(panel, eventBus);
	}

	@Override
	public String mayStop() {
		SplittableActivity real = delegate.getProxiedInstance();
		if (real == null) {
			return null;
		}
		return ((Activity) real).mayStop();
	}

	@Override
	public void onCancel() {
		delegate.onCancel();

	}

	@Override
	public void onStop() {
		delegate.onStop();
	}

	public void setPlace(Place place) {
		delegate.setPlace(place);
	}

}
