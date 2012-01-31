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

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellList.Style;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.common.web.client.widgets.Pager;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.place.ProjectHomePlace;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;
import com.tasktop.c2c.server.profile.web.ui.client.event.WatchProjectClickHandler;
import com.tasktop.c2c.server.profile.web.ui.client.place.NewProjectPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectDashboardPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.IProjectDiscoryView;

public class ProjectDiscoveryView extends AbstractComposite implements IProjectDiscoryView {

	interface Binder extends UiBinder<Widget, ProjectDiscoveryView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	private static ProjectDiscoveryView instance = null;

	public static ProjectDiscoveryView getInstance() {
		if (instance == null) {
			instance = new ProjectDiscoveryView();
		}
		return instance;
	}

	@UiField
	DivElement filterDivElement;
	@UiField
	Anchor publicFilterAnchor;
	@UiField
	Anchor watcherFilterAnchor;
	@UiField
	Anchor memberFilterAnchor;
	@UiField
	Anchor ownerFilterAnchor;
	@UiField
	Anchor allFilterAnchor;
	private Anchor[] filterAnchors;

	@UiField
	AnchorElement createAnchorElement;

	@UiField
	Panel projectsPanel;
	@UiField
	Panel projectPopup;
	@UiField
	Panel projectPopupWrapper;
	@UiField
	SimpleActivityView activityView;
	@UiField
	Anchor projectTitle;
	@UiField
	Anchor watchLink;
	@UiField
	ParagraphElement projectDesc;
	@UiField
	ProjectIconPanel iconPanel;

	@UiField
	Anchor closeLink;

	@UiField
	public Pager pager;

	private CellList<Project> projectList;
	private SingleSelectionModel<Project> model;
	private Presenter presenter;

	private ProjectDiscoveryView() {
		initWidget(uiBinder.createAndBindUi(this));

		projectList = new CellList<Project>(new ProjectCell(), new NoCellStyle());
		model = new SingleSelectionModel<Project>();

		// Hide the options menu - we never want to show it here.
		iconPanel.setOptionsVisible(false);

		model.addSelectionChangeHandler(new Handler() {

			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				updateSelectedProject(model.getSelectedObject());
			}
		});

		projectList.setSelectionModel(model);
		projectsPanel.add(projectList);
		pager.setDisplay(projectList);
		pager.itemLabel.setText("projects:");
		createAnchorElement.setHref(NewProjectPlace.createPlace().getHref());
		setupFilterAnchors();
	}

	/**
	 * 
	 */
	private void setupFilterAnchors() {
		filterAnchors = new Anchor[] { publicFilterAnchor, watcherFilterAnchor, memberFilterAnchor, ownerFilterAnchor,
				allFilterAnchor };
		publicFilterAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				selectedFilterChanged(ProjectRelationship.PUBLIC);

			}
		});
		watcherFilterAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				selectedFilterChanged(ProjectRelationship.WATCHER);

			}
		});
		memberFilterAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				selectedFilterChanged(ProjectRelationship.MEMBER);

			}
		});
		ownerFilterAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				selectedFilterChanged(ProjectRelationship.OWNER);

			}
		});
		allFilterAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				selectedFilterChanged(ProjectRelationship.ALL);

			}
		});
	}

	// No cell styles. This appears to be the only way to get the CellList to not plug in a bunch of default, undesired
	// style info.
	private class NoCellStyle implements CellList.Style, CellList.Resources {
		@Override
		public String cellListEvenItem() {
			return "ZorkNarf";
		}

		@Override
		public String cellListKeyboardSelectedItem() {
			return "ZorkNarf";
		}

		@Override
		public String cellListOddItem() {
			return "ZorkNarf";
		}

		@Override
		public String cellListSelectedItem() {
			return "ZorkNarf";
		}

		@Override
		public String cellListWidget() {
			return "ZorkNarf";
		}

		@Override
		public String getName() {
			return "NoStyle";
		}

		@Override
		public boolean ensureInjected() {
			return true;
		}

		@Override
		public String getText() {
			return "";
		}

		@Override
		public ImageResource cellListSelectedBackground() {
			return null;
		}

		@Override
		public Style cellListStyle() {
			return this;
		}
	}

	private void updateSelectedProject(final Project newProject) {

		// Clear out any previous activity data now that we've selected a new project.
		activityView.clear();

		if (newProject == null) {
			return;
		}

		// REVIEW This should be in the presenter
		// First, start our server-side async call for activities so that it gets going.
		ProfileEntryPoint
				.getInstance()
				.getProfileService()
				.getShortActivityList(newProject.getIdentifier(),
						new AsyncCallbackSupport<List<ProjectActivity>>(new OperationMessage("Loading Activity")) {
							@Override
							protected void success(List<ProjectActivity> result) {
								// Only render this activity stream if it's associated with the current project - the
								// user could have changed their selection while waiting for results to come back
								String curProjId = model.getSelectedObject() == null ? null : model.getSelectedObject()
										.getIdentifier();
								if (newProject.getIdentifier().equals(curProjId)) {
									activityView.renderActivity(result);
								}
							}
						});

		// Set up our icon panel
		iconPanel.setProject(newProject);
		iconPanel.activateAllIcons();

		// Make our popup visible.
		projectPopup.setVisible(true);
		projectTitle.setText(newProject.getName());
		projectTitle.setHref(ProjectHomePlace.createPlace(newProject.getIdentifier()).getHref());
		projectDesc.setInnerText(newProject.getDescription());

		setupWatchButton(newProject);
	}

	private static final String ACTIVE_STYLE = "active";

	private HandlerRegistration clickHandlerReg = null;

	@UiHandler("closeLink")
	void closePopup(ClickEvent e) {
		projectPopup.setVisible(false);
		// Deselect the current object.
		if (model.getSelectedObject() != null) {
			projectList.getSelectionModel().setSelected(model.getSelectedObject(), false);
		}
	}

	private void setupWatchButton(final Project newProject) {

		// Clear our old handler, if it exists.
		if (clickHandlerReg != null) {
			clickHandlerReg.removeHandler();
			clickHandlerReg = null;
		}

		// Also clear out the previous styles on this button, if they exist
		String[] curStyles = watchLink.getStyleName().split(" ");
		for (String style : curStyles) {
			if ("watch".equals(style) || "watching".equals(style)) {
				watchLink.removeStyleName(style);
			}
		}

		if (AuthenticationHelper.isAnonymous() || !newProject.getPublic()) {
			// Can't watch if you're anonymous.
			watchLink.setVisible(false);
			return;
		} else {
			// Make sure this is visible.
			watchLink.setVisible(true);
		}

		// Check to see if I'm currently watching this project.
		boolean isWatching = AuthenticationHelper.isWatching(newProject.getIdentifier());

		if (isWatching) {
			watchLink.addStyleName("watching");
			watchLink.setHTML("<span></span>Watching");
			watchLink.setEnabled(false);
		} else {
			watchLink.addStyleName("watch");
			watchLink.setHTML("<span></span>Watch");
			watchLink.setEnabled(true);

			// If we're not watching this project, set up our watch handler now.
			clickHandlerReg = watchLink.addClickHandler(new WatchProjectClickHandler(newProject) {

				@Override
				protected void onWatchSuccess(Project project) {
					// Reconfig our watch button, so it's now rendered correctly.
					setupWatchButton(project);
				}
			});
		}
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
		update();
	}

	private void update() {
		UIObject.setVisible(filterDivElement, !AuthenticationHelper.isAnonymous());
		UIObject.setVisible(createAnchorElement, !AuthenticationHelper.isAnonymous());
		setSelectedFilter(presenter.getProjectRelationship());
		setCurrentQuery(presenter.getCurrentQuery());
		// this.projectList.setRowData(presenter.getCurrentResult().getResultPage());
	}

	/**
	 * @param currentQuery
	 */
	private void setCurrentQuery(String currentQuery) {
		if (currentQuery != null) {

		}
	}

	interface PageTemplate extends SafeHtmlTemplates {

		@Template("<div class=\"discover-project {5}\">{1}<div class=\"project-heading\"><div class=\"info-graphic right\"><span>Show Details</span></div><h2>{0}</h2><div class=\"clear\"></div><div class=\"project-activity\"><span class=\"timestamp left\">{4}</span>{2}<div class=\"clear\"></div></div><div class=\"clear\"></div></div><p class=\"project-description\">{3}</p></div>")
		SafeHtml createProjectCell(SafeHtml title, SafeHtml pointer, SafeHtml commitWatchLabels, SafeHtml desc,
				SafeHtml dashboardLink, String activeClass);

		@Template("<div class=\"pointer-holder\"><div class=\"pointer\"></div></div>")
		SafeHtml createPointer();

		@Template("<span class=\"misc-icon watchers\"><span></span>{0}</span><span class=\"misc-icon left\"><span></span>{1}</span>")
		SafeHtml createCommitterAndWatcherLabels(int numWatchers, int numCommitters);
	}

	private static final PageTemplate TEMPLATE = GWT.create(PageTemplate.class);

	private final class ProjectCell extends AbstractCell<Project> {

		@Override
		public void render(Context context, Project value, SafeHtmlBuilder sb) {
			// Checking for null value and bailing out as recommended at
			// https://code.google.com/webtoolkit/doc/trunk/DevGuideUiCellWidgets.html#custom-cell
			if (value == null) {
				return;
			}

			// First, get the values we will care about during rendering. Make sure these are rendered safe to avoid
			// XSS.
			SafeHtml safeDesc = SafeHtmlUtils.fromString(value.getDescription());

			boolean isSelected = projectList.getSelectionModel().isSelected(value);

			// Only generate a pointer if this item is selected
			SafeHtml pointer = SafeHtmlUtils.EMPTY_SAFE_HTML;
			if (isSelected) {
				// pointer = TEMPLATE.createPointer();
			}

			// TODO

			// FIXME hook up watcher and member counts
			SafeHtml commiterWatcherLabel = TEMPLATE.createCommitterAndWatcherLabels(value.getNumWatchers(),
					value.getNumCommiters());

			String projectLinkClass = "misc-icon";

			if (AuthenticationHelper.isWatching(value.getIdentifier())) {
				projectLinkClass += " watcher";
			} else if (AuthenticationHelper.isCommitter(value.getIdentifier())) {
				projectLinkClass += " commiter";
			}
			String projectLink = "<a href=\"" + ProjectHomePlace.createPlace(value.getIdentifier()).getHref()
					+ "\" class=\"misc-icon\">" + value.getName() + "</a>";

			SafeHtml projectLinkHtml = new SafeHtmlBuilder().appendHtmlConstant(projectLink).toSafeHtml();

			Anchor dashboardLink = new Anchor("View Project Dashboard", ProjectDashboardPlace.createPlace(
					value.getIdentifier()).getHref());
			SafeHtml dashboardLinkHtml = new SafeHtmlBuilder().appendHtmlConstant(dashboardLink.toString())
					.toSafeHtml();

			// Now, spit out HTML to render this row.
			sb.append(TEMPLATE.createProjectCell(projectLinkHtml, pointer, commiterWatcherLabel, safeDesc,
					dashboardLinkHtml, isSelected ? ACTIVE_STYLE : ""));
		}
	}

	private void selectedFilterChanged(ProjectRelationship projectRelationship) {
		closePopup(null);
		setSelectedFilter(projectRelationship);
		presenter.setProjectRelationship(projectRelationship); // Triggers RPC
	}

	private void setSelectedFilter(ProjectRelationship projectRelationship) {
		if (projectRelationship == null) {
			updateFilterStyle(null);
			return;
		}
		switch (projectRelationship) {
		case ALL:
			updateFilterStyle(allFilterAnchor);
			break;
		case MEMBER:
			updateFilterStyle(memberFilterAnchor);
			break;
		case OWNER:
			updateFilterStyle(ownerFilterAnchor);
			break;
		case PUBLIC:
			updateFilterStyle(publicFilterAnchor);
			break;
		case WATCHER:
			updateFilterStyle(watcherFilterAnchor);
			break;
		}
	}

	private void updateFilterStyle(Anchor selectedFilterAnchor) {
		for (Anchor a : filterAnchors) {
			if (a.equals(selectedFilterAnchor)) {
				a.getElement().addClassName("selected");
			} else {
				a.getElement().removeClassName("selected");
			}
		}
	}

	public HasData<Project> getProjectsDisplay() {
		return projectList;
	}
}
