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
package com.tasktop.c2c.server.profile.web.client.place;

import java.util.List;

import net.customware.gwt.dispatch.shared.Action;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.tasktop.c2c.server.profile.domain.project.Agreement;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.profile.web.shared.actions.GetPendingAgreementsAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetPendingAgreementsResult;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class AgreementsPlace extends LoggedInPlace implements HeadingPlace, WindowTitlePlace {

	public static PageMapping Agreements = new PageMapping(new AgreementsPlace.Tokenizer(), "agreements");

	public static class Tokenizer implements PlaceTokenizer<AgreementsPlace> {

		@Override
		public AgreementsPlace getPlace(String token) {
			return AgreementsPlace.createPlace(ProjectsDiscoverPlace.createPlace());
		}

		@Override
		public String getToken(AgreementsPlace place) {
			return place.getToken();
		}
	}

	public static AgreementsPlace createPlace(DefaultPlace postAgreementsPlace) {
		return new AgreementsPlace(postAgreementsPlace);
	}

	private List<Agreement> agreements;
	private DefaultPlace postAgreementsPlace;

	private AgreementsPlace(DefaultPlace postAgreementsPlace) {
		if (postAgreementsPlace == null) {
			this.postAgreementsPlace = ProjectsDiscoverPlace.createPlace();
		} else {
			this.postAgreementsPlace = postAgreementsPlace;
		}
	}

	public DefaultPlace getPostAgreementsPlace() {
		return postAgreementsPlace;
	}

	@Override
	public String getHeading() {
		return "Legal Agreements";
	}

	public List<Agreement> getAgreements() {
		return agreements;
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		return Agreements.getUrl();
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetPendingAgreementsAction());
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		agreements = getResult(GetPendingAgreementsResult.class).get();
		onPlaceDataFetched();
	}

	@Override
	public String getWindowTitle() {
		return WindowTitleBuilder.createWindowTitle("Legal Agreements");
	}
}
