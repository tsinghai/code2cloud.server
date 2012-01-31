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

import com.google.gwt.place.impl.AbstractPlaceHistoryMapper;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.AbstractPreAuthorizingPlace;
import com.tasktop.c2c.server.profile.web.client.place.DefaultPlace;
import com.tasktop.c2c.server.profile.web.shared.UserInfo;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * @author jtyrrell
 * 
 */
public class AppHistoryMapper extends AbstractPlaceHistoryMapper<Place> {

	@Override
	protected PrefixAndToken getPrefixAndToken(Place newPlace) {
		if (newPlace instanceof DefaultPlace) {
			return new CustomPrefixAndToken(((DefaultPlace) newPlace).getPrefix(), ((DefaultPlace) newPlace).getToken());
		}
		return null;
	}

	public static class CustomPrefixAndToken extends PrefixAndToken {

		public CustomPrefixAndToken(String prefix, String token) {
			super(prefix, token);
		}

		@Override
		public String toString() {
			if (StringUtils.hasText(token)) {
				return ((prefix.length() == 0) ? token : prefix + ":" + token);
			} else {
				return prefix;
			}
		}
	}

	@Override
	protected PlaceTokenizer<?> getTokenizer(String prefix) {
		return null;
	}

	public Place getPlace(String token) {

		// First, try to get our tokenizer from the new handler
		Place retPlace = PageMapping.getPlaceForUrl(token);

		if (retPlace != null) {

			if (retPlace instanceof AbstractPreAuthorizingPlace) {
				reloadUserCredentials();
			}

			return retPlace;
		}

		AppGinjector.get.instance().getNotifier()
				.displayMessage(Message.createErrorMessage("Error: Unexpected error (HTTP 404)"));
		return AppGinjector.get.instance().getPlaceController().getWhere();
	}

	private void reloadUserCredentials() {
		AppGinjector.get.instance().getProfileService().getCurrentUserInfo(new AsyncCallbackSupport<UserInfo>() {

			@Override
			public void success(UserInfo result) {
				// FIXME batch this up
				AppGinjector.get.instance().getAppState().setCredentials(result.getCredentials());
				AppGinjector.get.instance().getAppState().setHasPendingAgreements(result.getHasPendingAgreements());
			}
		});
	}
}
