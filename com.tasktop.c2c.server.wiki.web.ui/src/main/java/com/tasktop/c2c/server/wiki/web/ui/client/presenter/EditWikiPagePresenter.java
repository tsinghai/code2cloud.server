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
package com.tasktop.c2c.server.wiki.web.ui.client.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.event.ClearCacheEvent;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.domain.Page.GroupAccess;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiEditPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiHomePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiViewPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.view.EditWikiPageView;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.CreatePageAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.CreatePageResult;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.ListAttachmentsAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.ListAttachmentsResult;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.UpdatePageAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.UpdatePageResult;

public class EditWikiPagePresenter extends AbstractWikiPresenter implements SplittableActivity {
	private final EditWikiPageDisplay view;

	private String path;
	private Page page;
	boolean isUser;
	boolean isAdmin;

	public EditWikiPagePresenter(final EditWikiPageDisplay view) {
		super(view.getWidget());
		this.view = view;
		view.addCancelClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				doCancel();
			}
		});
		view.addSaveClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				doSave(view.getPage());
			}
		});
		view.addAttachmentSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				attachmentsSubmitted(event);
			}
		});
	}

	public EditWikiPagePresenter() {
		this(new EditWikiPageView());
	}

	public interface EditWikiPageDisplay {
		Widget getWidget();

		void setPage(Page page);

		void setAttachments(List<Attachment> attachments);

		Page getPage();

		void addSaveClickHandler(ClickHandler clickHandler);

		void addCancelClickHandler(ClickHandler clickHandler);

		void addAttachmentSubmitCompleteHandler(SubmitCompleteHandler handler);

		void setAvailableAccessSettings(Collection<GroupAccess> availableAccessSettings);

		String getAttachmentFileName();

		void clearAttachementForm();

		void setProjectIdentifier(String projectIdentifier);

		/**
		 * @return
		 */
		boolean isDirty();

	}

	public void setPlace(Place p) {
		ProjectWikiEditPagePlace place = (ProjectWikiEditPagePlace) p;
		projectIdentifier = place.getProjectId();
		setAdmin(AuthenticationHelper.isAdmin(projectIdentifier));
		setUser(AuthenticationHelper.isCommitter(projectIdentifier));
		String path = place.getPath();
		if (!place.isNew()) {
			setPath(path);
			setPage(place.getPage());
		} else {
			Page newPage = new Page();
			newPage.setPath(path);
			newPage.setEditAccess(GroupAccess.ALL);
			newPage.setDeleteAccess(GroupAccess.ALL);
			setPage(newPage);
		}

		List<GroupAccess> availableAccessSettings = new ArrayList<Page.GroupAccess>();
		availableAccessSettings.add(GroupAccess.ALL);
		if (isUser) {
			availableAccessSettings.add(GroupAccess.MEMBER_AND_OWNERS);
		}
		if (isAdmin) {
			availableAccessSettings.add(GroupAccess.OWNERS);
		}
		view.setAvailableAccessSettings(availableAccessSettings);
		view.setPage(page);
		view.setAttachments(place.getAttachements());
		view.setProjectIdentifier(projectIdentifier);

	}

	public void setPath(String path) {
		this.path = path;
		this.page = null;
	}

	public void setPage(Page page) {
		this.page = page;
		this.path = page == null ? null : page.getPath();
	}

	@Override
	protected void bind() {

	}

	private void attachmentsSubmitted(SubmitCompleteEvent event) {
		JSONValue value = JSONParser.parseLenient(event.getResults());
		JSONValue uploadResultValue = value.isObject().get("uploadResult");
		JSONObject uploadResult = uploadResultValue == null ? null : value.isObject().get("uploadResult").isObject();
		if (uploadResult != null) {
			// get updated modification stamp and list of attachments
			JSONObject updatedPage = uploadResult.get("page") == null ? null : uploadResult.get("page").isObject();
			if (updatedPage != null) {
				JSONNumber number = updatedPage.get("modificationDate") == null ? null : updatedPage.get(
						"modificationDate").isNumber();
				if (number != null) {
					page.setModificationDate(new Date(new Long(number.toString())));
				}
			}
			ProfileGinjector.get
					.instance()
					.getNotifier()
					.displayMessage(
							Message.createSuccessMessage("Attachment \"" + view.getAttachmentFileName() + "\" uploaded"));
			getEventBus().fireEvent(new ClearCacheEvent());
			view.clearAttachementForm();
			// Ideally we would simply update with the results for the json content.
			fetchAttachments();
		} else {
			String message = "Error: Unexpected server response";
			JSONValue errorValue = value.isObject().get("error");
			JSONObject errorObject = errorValue == null ? null : errorValue.isObject();
			if (errorObject != null) {
				JSONString errorMessage = errorObject.get("message").isString();
				if (errorMessage != null) {
					message = errorMessage.stringValue();
				}
			}
			ProfileGinjector.get.instance().getNotifier().displayMessage(Message.createErrorMessage(message));
		}
	}

	private void fetchAttachments() {
		getDispatchService().execute(new ListAttachmentsAction(projectIdentifier, page.getId()),
				new AsyncCallbackSupport<ListAttachmentsResult>() {

					@Override
					protected void success(ListAttachmentsResult result) {
						view.setAttachments(result.get());
					}
				});
	}

	protected void doCancel() {
		if (page != null && page.getId() != null) {
			ProjectWikiViewPagePlace.createPlaceForPage(projectIdentifier, page.getPath()).go();
		} else {
			ProjectWikiHomePlace.createDefaultPlace(getProjectIdentifier()).go();
		}
	}

	protected void doSave(Page page) {
		if (page.getId() == null) {
			getDispatchService().execute(new CreatePageAction(getProjectIdentifier(), page),
					new AsyncCallbackSupport<CreatePageResult>(new OperationMessage("Creating Page...")) {
						@Override
						protected void success(CreatePageResult result) {
							Page savedPage = result.get();
							ProjectWikiViewPagePlace place = ProjectWikiViewPagePlace.createPlaceForPage(
									getProjectIdentifier(), savedPage.getPath());
							place.displayOnArrival(Message.createSuccessMessage("Page Created"));
							place.go();

						}
					});
		} else {
			getDispatchService().execute(new UpdatePageAction(getProjectIdentifier(), page),
					new AsyncCallbackSupport<UpdatePageResult>(new OperationMessage("Saving Page...")) {
						@Override
						protected void success(UpdatePageResult result) {
							Page savedPage = result.get();
							ProjectWikiViewPagePlace place = ProjectWikiViewPagePlace.createPlaceForPage(
									getProjectIdentifier(), savedPage.getPath());
							place.displayOnArrival(Message.createSuccessMessage("Page Saved"));
							place.go();
						}
					});
		}
	}

	/**
	 * @param isAdmin
	 *            the isAdmin to set
	 */
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	/**
	 * @param isUser
	 *            the isUser to set
	 */
	public void setUser(boolean isUser) {
		this.isUser = isUser;
	}

	@Override
	public String mayStop() {
		if (view.isDirty()) {
			return "There are unsaved changes. Are you sure you want to navigate away? "
					+ "Press ok to navigate away and loose unsaved changes, or cancel to stay on the current page.";
		}
		return null;
	}
}
