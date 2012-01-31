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
package com.tasktop.c2c.server.tasks.client.presenters;

import java.util.List;


import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminKeywordsPlace;
import com.tasktop.c2c.server.tasks.client.widgets.admin.keywords.IProjectAdminKeywordsView;
import com.tasktop.c2c.server.tasks.client.widgets.admin.keywords.ProjectAdminKeywordsEditView;
import com.tasktop.c2c.server.tasks.client.widgets.admin.keywords.ProjectAdminKeywordsView;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.shared.action.DeleteKeywordAction;
import com.tasktop.c2c.server.tasks.shared.action.DeleteKeywordResult;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationAction;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationResult;
import com.tasktop.c2c.server.tasks.shared.action.UpdateKeywordAction;
import com.tasktop.c2c.server.tasks.shared.action.UpdateKeywordResult;

public class ProjectAdminTaskKeywordsActivity extends AbstractTaskPresenter implements
		IProjectAdminKeywordsView.ProjectAdminKeywordsPresenter {

	private String projectIdentifier;
	private List<Keyword> keywords;
	private Keyword selectedKeyword;
	private ProjectAdminKeywordsView view;
	private Keyword newKeyword = null;
	private boolean editing = false;

	public ProjectAdminTaskKeywordsActivity() {
		this(ProjectAdminKeywordsView.getInstance());
	}

	/**
	 * @param instance
	 */
	public ProjectAdminTaskKeywordsActivity(ProjectAdminKeywordsView view) {
		super(view);
		this.view = view;
	}

	public void setPlace(Place aPlace) {
		ProjectAdminKeywordsPlace place = (ProjectAdminKeywordsPlace) aPlace;
		this.keywords = place.getRepositoryConfiguration().getKeywords();
		this.projectIdentifier = place.getProjectIdentifer();

	}

	private void updateView() {
		// Get the latest keywords
		ProfileGinjector.get
				.instance()
				.getDispatchService()
				.execute(new GetRepositoryConfigurationAction(projectIdentifier),
						new AsyncCallbackSupport<GetRepositoryConfigurationResult>() {

							@Override
							protected void success(GetRepositoryConfigurationResult actionResult) {
								List<Keyword> result = actionResult.get().getKeywords();
								keywords = result;
								view.setPresenter(ProjectAdminTaskKeywordsActivity.this);
							}
						});
	}

	public void onSaveKeyword(final ErrorCapableView errorView) {
		if (selectedKeyword.getName() == null || selectedKeyword.getName().length() == 0
				|| selectedKeyword.getName().equals("")) {
			return;
		}
		final Keyword toSave = selectedKeyword;
		ProfileGinjector.get
				.instance()
				.getDispatchService()
				.execute(new UpdateKeywordAction(projectIdentifier, toSave),
						new AsyncCallbackSupport<UpdateKeywordResult>() {
							@Override
							protected void success(final UpdateKeywordResult result) {
								getKeywords().add(result.get()); // REVIEW duplicate???
								selectedKeyword = result.get();
								newKeyword = null;
								editing = false;
								updateView();
								ProfileGinjector.get.instance().getNotifier()
										.displayMessage(Message.createSuccessMessage("Tag saved."));
							}
						});
	}

	@Override
	public void onEditCancel() {
		if (selectedKeyword != null && selectedKeyword.getId() == null) {
			newKeyword = null;
			selectedKeyword = keywords.size() > 0 ? keywords.get(0) : null;
		}
		// Remove any errors that are showing, since we're leaving edit mode.
		ProjectAdminKeywordsEditView.getInstance().clearErrors();
		resetKeyword(false);
	}

	private void resetKeyword(final boolean editState) {
		ProfileGinjector.get
				.instance()
				.getDispatchService()
				.execute(new GetRepositoryConfigurationAction(projectIdentifier),
						new AsyncCallbackSupport<GetRepositoryConfigurationResult>() {

							@Override
							protected void success(GetRepositoryConfigurationResult actionResult) {
								List<Keyword> result = actionResult.get().getKeywords();
								Keyword selected = selectedKeyword;
								boolean selectedFound = selected == null;
								for (Keyword keyword : result) {
									if (!selectedFound && selected.getName() != null && selected.equals(keyword)) {
										selectedKeyword = keyword;
										selectedFound = true;
									}
									if (selectedFound) {
										break;
									}
								}
								editing = editState;
								updateView();
							}
						});
	}

	@Override
	public boolean isEditing() {
		return editing;
	}

	@Override
	public List<Keyword> getKeywords() {
		return keywords;
	}

	@Override
	public void selectKeyword(final Keyword keyword) {

		if (keyword.getId() == null) {
			selectedKeyword = newKeyword;
			editing = true;
			updateView();
			return;
		}

		// First, if we're selecting a new keyword then clear out any old messages.
		// ProfileEntryPoint.getInstance().getEventBus().fireEvent(new MessageNotificationEvent());

		for (Keyword skeyword : getKeywords()) {
			if (skeyword.equals(keyword)) {
				selectedKeyword = keyword;
			}
		}
		editing = false;
		updateView();
	}

	@Override
	public Keyword getSelectedKeyword() {
		if (selectedKeyword == null) {
			selectedKeyword = newKeyword = createNewKeyword();
		}
		return selectedKeyword;
	}

	@Override
	public void addKeyword() {
		selectedKeyword = newKeyword = createNewKeyword();
		editing = true;
		updateView();
	}

	private Keyword createNewKeyword() {
		return new Keyword();
	}

	@Override
	public void onDeleteKeyword() {

		if (!Window.confirm("Are you sure you want to delete this tag? This operation cannot be undone.")) {
			return;
		}

		ProfileGinjector.get.instance().getDispatchService()
				.execute(new DeleteKeywordAction(projectIdentifier, selectedKeyword.getId()),

				new AsyncCallbackSupport<DeleteKeywordResult>() {
					@Override
					protected void success(DeleteKeywordResult result) {
						selectedKeyword = keywords.size() > 0 ? keywords.get(0) : null;
						ProfileGinjector.get.instance().getNotifier()
								.displayMessage(Message.createSuccessMessage("Tag deleted."));
						updateView();
					}
				});
	}

	@Override
	public void onEditKeyword() {
		resetKeyword(true);
	}

	@Override
	protected void bind() {
		view.setPresenter(this);
	}

}
