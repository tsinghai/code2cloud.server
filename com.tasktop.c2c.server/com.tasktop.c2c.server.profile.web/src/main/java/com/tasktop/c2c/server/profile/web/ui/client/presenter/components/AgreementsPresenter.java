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
package com.tasktop.c2c.server.profile.web.ui.client.presenter.components;

import java.util.List;


import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.profile.domain.project.Agreement;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.place.AgreementsPlace;
import com.tasktop.c2c.server.profile.web.client.place.DefaultPlace;
import com.tasktop.c2c.server.profile.web.shared.actions.ApproveAgreementAction;
import com.tasktop.c2c.server.profile.web.shared.actions.ApproveAgreementResult;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.AgreementsView;

public class AgreementsPresenter extends AbstractProfilePresenter {

	private AgreementsView agreementsView;
	private List<Agreement> agreements;
	private DefaultPlace postAgreementsPlace;

	public AgreementsPresenter(AgreementsView agreementsView, AgreementsPlace place) {
		super(agreementsView);
		this.agreementsView = agreementsView;
		this.agreements = place.getAgreements();
		this.postAgreementsPlace = place.getPostAgreementsPlace();
		this.agreementsView.setPresenter(this);
		doShowNextAgreement();
	}

	@Override
	protected void bind() {
	}

	public void onAgree() {
		doAgree(agreementsView.getAgreement());
	}

	private void doAgree(Agreement agreement) {
		if (agreement == null) {
			postAgreementsPlace.go();
			return;
		}
		ProfileGinjector.get
				.instance()
				.getDispatchService()
				.execute(new ApproveAgreementAction(agreement.getId()),
						new AsyncCallbackSupport<ApproveAgreementResult>() {

							@Override
							protected void success(ApproveAgreementResult result) {
								doShowNextAgreement();
							}
						});
	}

	public void doShowNextAgreement() {
		if (agreements.size() > 0) {
			agreementsView.setAgreement(agreements.remove(0));
		} else { // 0 agreements remain
			getAppState().setHasPendingAgreements(false);
			postAgreementsPlace.go();
		}
	}
}
