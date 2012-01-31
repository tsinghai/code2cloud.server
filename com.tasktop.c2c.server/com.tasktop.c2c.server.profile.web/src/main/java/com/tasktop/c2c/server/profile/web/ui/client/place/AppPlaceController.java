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
package com.tasktop.c2c.server.profile.web.ui.client.place;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceChangeRequestEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.History;
import com.tasktop.c2c.server.profile.web.client.place.DefaultPlace;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * @author cmorgan
 * 
 */
public class AppPlaceController extends PlaceController {

	private AppGinjector injector = AppGinjector.get.instance();
	private final EventBus eventBus;
	private final Delegate delegate;
	private Place where = Place.NOWHERE;
	private Place goingTo = null;

	private PlaceHistoryMapper placeHistoryMapper;

	public AppPlaceController(EventBus eventBus) {
		this(eventBus, (Delegate) GWT.create(DefaultDelegate.class));
	}

	public AppPlaceController(EventBus eventBus, Delegate delegate) {
		super(eventBus, delegate);
		this.eventBus = eventBus;
		this.delegate = delegate;
		placeHistoryMapper = AppGinjector.get.instance().getPlaceHistoryMapper();
	}

	/**
	 * Returns the current place.
	 * 
	 * @return a {@link Place} instance
	 */
	@Override
	public Place getWhere() {
		return where;
	}

	@Override
	public void goTo(Place newPlace) {

		// NOTE : In contrast to the super impl, we will go to places we are already at. This is because places fetch
		// data so the same place may be different on subsequent views.

		boolean willContinue = maybeGoTo(newPlace);

		if (willContinue) {
			goingTo = newPlace;
			continueGo(newPlace);
		}
	}

	private void continueGo(Place newPlace) {
		if (newPlace instanceof DefaultPlace) {
			DefaultPlace place = (DefaultPlace) newPlace;

			if (place.isReadyToGo()) {
				finishGo(newPlace);
			} else {
				place.go();
			}
		} else {
			finishGo(newPlace);
		}
	}

	public void finishGo(Place newPlace) {
		goingTo = null;
		where = newPlace;
		eventBus.fireEvent(new PlaceChangeEvent(newPlace));
	}

	private boolean maybeGoTo(Place newPlace) {
		if (goingTo != null && goingTo == newPlace) {
			return true;
		}

		PlaceChangeRequestEvent willChange = new PlaceChangeRequestEvent(newPlace);
		eventBus.fireEvent(willChange);
		String warning = willChange.getWarning();

		if (warning == null || delegate.confirm(warning)) {
			return true;
		} else {
			// Keep url in sync when cancled
			History.newItem(placeHistoryMapper.getToken(getWhere()), false);
			return false;
		}
	}
}
