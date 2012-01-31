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


import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.HeaderPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.HeaderView;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class HeaderActivityMapper implements ActivityMapper {

	private HeaderPresenter headerPresenter = new HeaderPresenter(HeaderView.getInstance());

	@Override
	public Activity getActivity(Place place) {
		headerPresenter.setPlace(place);
		return headerPresenter;
	}
}
