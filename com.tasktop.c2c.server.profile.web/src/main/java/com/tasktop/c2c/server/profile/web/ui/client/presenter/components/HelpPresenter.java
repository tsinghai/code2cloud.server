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


import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.common.web.shared.NoSuchEntityException;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.AbstractProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.HelpView;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiViewPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.presenter.WikiPageContentPresenter;
import com.tasktop.c2c.server.wiki.web.ui.client.view.WikiPageContentView;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RetrievePageResult;

public class HelpPresenter extends AbstractProfilePresenter implements SplittableActivity {

	private String projectIdentifier = "code2cloud";
	private String pageIdentifier = "Help";
	private HelpView view;

	public HelpPresenter(HelpView view) {
		super(view);
		this.view = view;
	}

	public HelpPresenter() {
		this(new HelpView());
	}

	public void setPlace(Place p) {
		AppGinjector.get
				.instance()
				.getDispatchService()
				.execute(new RetrievePageAction(projectIdentifier, pageIdentifier, true),
						new AsyncCallbackSupport<RetrievePageResult>() {
							@Override
							public void success(RetrievePageResult result) {
								WikiPageContentPresenter contentPresenter = new WikiPageContentPresenter(
										new WikiPageContentView());
								contentPresenter.setPage(result.get());
								contentPresenter.setProjectIdentifier(projectIdentifier);
								contentPresenter.setEnableMetadata(false);
								Project fake = new Project();
								fake.setIdentifier(projectIdentifier);
								contentPresenter.go(view.wikiContentPanel);
								ProjectWikiViewPagePlace place = ProjectWikiViewPagePlace.createPlaceWithData(fake,
										result.get());
								place.setEnableEdit(false);
								place.setEnableDelete(false);

								contentPresenter.setPlace(place);
							}

							@Override
							public void onFailure(Throwable exception) {
								if (exception instanceof NoSuchEntityException) {
									// ignore, this expected if the project or page doesn't exist.
								} else {
									super.onFailure(exception);
								}
							}
						});
	}

	@Override
	protected void bind() {

	}
}
