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
package com.tasktop.c2c.server.profile.web.ui.client.view.components;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.profile.domain.project.Agreement;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.AgreementsPresenter;

public class AgreementsView extends AbstractComposite {

	private static AgreementsView instance = null;

	public static AgreementsView getInstance() {
		if (instance == null) {
			instance = new AgreementsView();
		}
		return instance;
	}

	interface AgreementsViewUiBinder extends UiBinder<Widget, AgreementsView> {
	}

	private static AgreementsViewUiBinder uiBinder = GWT.create(AgreementsViewUiBinder.class);

	private Agreement agreement;

	@UiField
	public Label title;
	@UiField
	public HTML text;
	@UiField
	public Button agreeButton;

	private AgreementsPresenter presenter;

	private AgreementsView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setAgreement(Agreement agreement) {
		this.agreement = agreement;
		updateUi();
	}

	private void updateUi() {
		title.setText(agreement.getTitle());
		text.setHTML(agreement.getText());
	}

	public Agreement getAgreement() {
		return agreement;
	}

	public void setPresenter(AgreementsPresenter presenter) {
		this.presenter = presenter;
	}

	@UiHandler("agreeButton")
	void onAgree(ClickEvent clickEvent) {
		if (presenter != null) {
			presenter.onAgree();
		}
	}
}
