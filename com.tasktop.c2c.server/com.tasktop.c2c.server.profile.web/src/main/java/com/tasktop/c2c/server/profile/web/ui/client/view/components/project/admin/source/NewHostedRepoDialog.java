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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.project.admin.source;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.cloud.domain.ScmLocation;
import com.tasktop.c2c.server.cloud.domain.ScmType;
import com.tasktop.c2c.server.common.web.client.view.errors.ErrorCabableDialogBox;
import com.tasktop.c2c.server.profile.domain.scm.ScmRepository;

/**
 * @author Clint Morgan (Tasktop Technologies Inc.)
 */
public class NewHostedRepoDialog extends ErrorCabableDialogBox implements
		IProjectAdminSourceView<IProjectAdminSourceView.Presenter> {

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, NewHostedRepoDialog> {
	}

	private static NewHostedRepoDialog instance;

	public static NewHostedRepoDialog getInstance(Presenter presenter) {
		if (instance == null) {
			instance = new NewHostedRepoDialog();
		}
		instance.setPresenter(presenter);
		return instance;
	}

	@UiField
	TextBox repoName;
	@UiField
	Label repoBaseUrl;
	@UiField
	Label repoNameLabel;
	@UiField
	Button addButton;
	@UiField
	Button cancelButton;

	private String baseUrl;
	private Presenter presenter;

	private static final String PLACEHOLDER_TEXT = "NewRepositoryName";

	public NewHostedRepoDialog() {
		super(false, true);
		setText("Add Hosted Repository");
		setWidget(uiBinder.createAndBindUi(this));
		setAnimationEnabled(true); // Why not?
		setGlassEnabled(true);
		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				if (isShowing()) {
					center();
				}
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		// use KeyUp because it catches backspace, and has the updated value in the textBox in the callback
		repoName.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				updateUrlText(repoName.getText());
			}
		});

		repoName.getElement().setPropertyString("placeholder", PLACEHOLDER_TEXT);
	}

	private static final int MAX_REPO_URL_CHARS = 50;
	private static final String TRIM_CHARS = "...";

	private void updateUrlText(String repoName) {
		repoNameLabel.setText(repoName);

		// Trim the url if needed
		int endIndex = baseUrl.length();
		String trimChars = "";
		if (repoName.length() + baseUrl.length() > MAX_REPO_URL_CHARS) {
			int extraChars = (repoName.length() + baseUrl.length()) - MAX_REPO_URL_CHARS;
			endIndex = Math.max(baseUrl.length() - extraChars, 0);
			trimChars = TRIM_CHARS;
		}
		repoBaseUrl.setText(baseUrl.substring(0, endIndex) + trimChars);

	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void show() {
		super.show();
		repoName.setText(null);
		updateUrlText(PLACEHOLDER_TEXT);

	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@UiHandler("cancelButton")
	void onCancel(ClickEvent event) {
		hide();
	}

	@UiHandler("addButton")
	void onAdd(ClickEvent event) {
		ScmRepository newRepo = new ScmRepository();
		newRepo.setName(repoName.getText() + ".git");
		newRepo.setType(ScmType.GIT);
		newRepo.setScmLocation(ScmLocation.CODE2CLOUD);
		presenter.onCreateRepository(this, newRepo);
	}
}
