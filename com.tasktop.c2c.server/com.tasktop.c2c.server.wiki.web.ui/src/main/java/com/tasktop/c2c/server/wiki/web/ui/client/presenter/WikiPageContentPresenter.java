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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.navigation.Navigation;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.Message.Action;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiEditPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiHomePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiViewPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.view.WikiPageContentView;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.DeletePageAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.DeletePageResult;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RestorePageAction;
import com.tasktop.c2c.server.wiki.web.ui.shared.action.RestorePageResult;

public class WikiPageContentPresenter extends AbstractWikiPresenter implements SplittableActivity {
	private final RenderedWikiPageDisplay view;

	private String path;
	private Page page;
	private boolean enableEdit = true;
	private boolean enableDelete = true;

	private String elementId;
	boolean owner;
	boolean user;

	public WikiPageContentPresenter(RenderedWikiPageDisplay view) {
		super(view.getWidget());
		this.view = view;
		view.addDeleteClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				doDelete();
			}
		});
		view.addRestoreClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				doRestore();
			}
		});
	}

	public WikiPageContentPresenter() {
		this(new WikiPageContentView());
	}

	public void setPlace(Place p) {
		ProjectWikiViewPagePlace place = (ProjectWikiViewPagePlace) p;
		this.page = place.getPage();
		this.path = place.getPagePath();
		projectIdentifier = place.getProjectId();

		if (StringUtils.hasText(place.getAnchor())) {
			setElementId(place.getAnchor());
		}
		setIsOwner(AuthenticationHelper.isAdmin(place.getProjectId()));
		setIsUser(AuthenticationHelper.isCommitter(place.getProjectId()));
		setEnableEdit(place.isEnableEdit());
		setEnableDelete(place.isEnableDelete());

		if (page != null) {
			displayPage();
		} else {
			displayPathNotFound();
		}
	}

	public interface RenderedWikiPageDisplay {
		Widget getWidget();

		void setEnableEdit(boolean enableEdit);

		void setPage(String projectIdentifier, Page page);

		void setPageNotFound(String path);

		void addDeleteClickHandler(ClickHandler clickHandler);

		void setEnableMetadata(boolean metadata);

		void setEnableHeader(boolean enable);

		/**
		 * @param canEdit
		 */
		void setEnableDelete(boolean canEdit);

		/**
		 * @param historyToken
		 */
		void setCreateHref(String historyToken);

		/**
		 * @param clickHandler
		 */
		void addRestoreClickHandler(ClickHandler clickHandler);

		/**
		 * @param canRestore
		 */
		void setEnableRestore(boolean canRestore);
	}

	public void setPage(Page page) {
		this.page = page;
		this.path = page == null ? null : page.getPath();
	}

	public void setEnableEdit(boolean enableEdit) {
		this.enableEdit = enableEdit;
	}

	public void setEnableMetadata(boolean metadata) {
		view.setEnableMetadata(metadata);
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	@Override
	protected void bind() {
		showIdElement(); // Need this in bind because the view is not always attached in setPlace.
	}

	private void displayPage() {
		view.setPage(projectIdentifier, page);

		if (page.isDeleted()) {
			boolean canRestore;
			switch (page.getDeleteAccess()) {
			case OWNERS:
				canRestore = enableDelete && owner;
				break;
			case MEMBER_AND_OWNERS:
				canRestore = enableDelete && (owner || user);
				break;
			case ALL:
			default:
				canRestore = enableDelete;
				break;
			}
			view.setEnableRestore(canRestore);
			return;

		}
		view.setEnableRestore(false);

		boolean canEdit;
		switch (page.getEditAccess()) {
		case OWNERS:
			canEdit = enableEdit && owner;
			break;
		case MEMBER_AND_OWNERS:
			canEdit = enableEdit && (owner || user);
			break;
		case ALL:
		default:
			canEdit = enableEdit;
			break;
		}
		view.setEnableEdit(canEdit);

		boolean canDelete;
		switch (page.getDeleteAccess()) {
		case OWNERS:
			canDelete = enableDelete && owner;
			break;
		case MEMBER_AND_OWNERS:
			canDelete = enableDelete && (owner || user);
			break;
		case ALL:
		default:
			canDelete = enableDelete;
			break;
		}
		view.setEnableDelete(canDelete);

		if (elementId != null) {
			showIdElement();
		}
	}

	private void displayPathNotFound() {
		view.setPageNotFound(path);
		view.setEnableDelete(false);
		view.setEnableEdit(false);
		view.setCreateHref(ProjectWikiEditPagePlace.createPlaceForPath(projectIdentifier, path).getHref());
	}

	private void showIdElement() {
		if (view.getWidget().isAttached() && elementId != null) {
			Navigation.showIdElement(view.getWidget().getElement(), elementId);
		}
	}

	protected void doDelete() {
		final Page deleted = page;
		getDispatchService().execute(new DeletePageAction(getProjectIdentifier(), page.getId()),
				new AsyncCallbackSupport<DeletePageResult>() {
					@Override
					protected void success(DeletePageResult result) {
						Action undo = new Action("Undo",

						new Runnable() {

							@Override
							public void run() {
								restorePage(deleted);

							}
						});
						ProjectWikiHomePlace place = ProjectWikiHomePlace.createDefaultPlace(getProjectIdentifier());
						place.displayOnArrival(Message.createSuccessWithActionMessage("Page deleted", undo));
						place.go();
					}
				});
	}

	protected void doRestore() {
		restorePage(page);
	}

	protected void restorePage(final Page pageToRestore) {
		getDispatchService().execute(new RestorePageAction(getProjectIdentifier(), pageToRestore.getId()),
				new AsyncCallbackSupport<RestorePageResult>() {
					@Override
					protected void success(RestorePageResult result) {
						ProjectWikiViewPagePlace place = ProjectWikiViewPagePlace.createPlaceForPage(
								getProjectIdentifier(), pageToRestore.getPath());
						place.displayOnArrival(Message.createSuccessMessage("Page restored"));
						place.go();
					}
				});
	}

	/**
	 * @param b
	 */
	public void setEnableHeader(boolean enable) {
		view.setEnableHeader(enable);
	}

	/**
	 * @param admin
	 */
	public void setIsOwner(boolean admin) {
		this.owner = admin;
	}

	/**
	 * @param committer
	 */
	public void setIsUser(boolean committer) {
		this.user = committer;
	}

	/**
	 * @param enableDelete
	 *            the enableDelete to set
	 */
	public void setEnableDelete(boolean enableDelete) {
		this.enableDelete = enableDelete;
	}
}
