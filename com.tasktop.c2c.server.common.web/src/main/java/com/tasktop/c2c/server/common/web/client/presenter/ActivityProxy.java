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
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.AsyncProxy;

public abstract class ActivityProxy<T extends Place> {
	private Class<T> clazz;
	private SplitActivity activity;

	public interface ProxyReturn extends AsyncProxy<SplittableActivity>, SplittableActivity {
	};

	public ActivityProxy(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Activity getActivity(Place place) {
		if (activity == null) {
			activity = new SplitActivity(instantiate());
		}
		activity.setPlace(place);
		return activity;
	}

	protected abstract ProxyReturn instantiate();

	/**
	 * @return the clazz
	 */
	public Class<T> getPlaceClass() {
		return clazz;
	}
}
