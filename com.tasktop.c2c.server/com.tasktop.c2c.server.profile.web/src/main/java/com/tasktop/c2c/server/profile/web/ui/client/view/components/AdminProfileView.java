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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.common.web.client.view.Avatar;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.AdminProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.resources.ProfileCellListResources;
import com.tasktop.c2c.server.profile.web.ui.client.resources.ProfileResources;

public class AdminProfileView extends AbstractComposite implements Editor<Profile> {

	interface Binder extends UiBinder<Widget, AdminProfileView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	private static AdminProfileView instance;

	public static AdminProfileView getInstance() {
		if (instance == null) {
			instance = new AdminProfileView();
		}
		return instance;
	}

	private static HtmlTemplates template = GWT.create(HtmlTemplates.class);

	static interface HtmlTemplates extends SafeHtmlTemplates {

		@Template("<div><img src=\"{0}\"></img></div>")
		SafeHtml profileListAvatar(String avatarUrl);

		@Template("<div><div>{0} {1} ({2})</div><div>{3}</div></div>")
		SafeHtml profileListInfo(String firstName, String lastName, String username, String email);

		@Template("<div class=\"{2}\">{0} {1}</div>")
		SafeHtml profileListItem(SafeHtml avatarDiv, SafeHtml infoDiv, String className);
	}

	private class ProfileCell extends AbstractCell<Profile> {

		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context, Profile value, SafeHtmlBuilder sb) {
			String avatarUrl = Avatar.computeAvatarUrl(value.getGravatarHash(), Avatar.Size.SMALL);
			sb.append(template.profileListItem(
					template.profileListAvatar(avatarUrl),
					template.profileListInfo(value.getFirstName(), value.getLastName(), value.getUsername(),
							value.getEmail()), ProfileResources.get.style().adminProfileCell()));
		}
	}

	@UiField
	@Ignore
	protected TextBox filterText;
	@UiField
	@Ignore
	protected CheckBox showDisabled;
	@UiField
	@Ignore
	protected CheckBox showNewsEmailOptOut;
	@UiField
	@Ignore
	protected CheckBox showServiceEmailOptOut;
	@UiField
	protected DisclosurePanel emailsPanel;
	@UiField
	@Ignore
	protected TextArea emailArea;

	@UiField(provided = true)
	protected CellList<Profile> profileList = new CellList<Profile>(new ProfileCell(), ProfileCellListResources.get);
	@UiField(provided = true)
	protected SimplePager pager = new SimplePager();
	private ListDataProvider<Profile> dataProvider = new ListDataProvider<Profile>();

	@UiField
	protected DivElement editProfileDiv;
	@UiField
	protected Label username;
	@UiField
	protected Label firstName;
	@UiField
	protected Label lastName;
	@UiField
	protected Label email;
	@UiField
	@Ignore
	protected Label status;
	@UiField
	protected Button toggleDisableButton;

	interface Driver extends SimpleBeanEditorDriver<Profile, AdminProfileView> {

	}

	private Driver driver = GWT.create(Driver.class);
	private AdminProfilePresenter presenter;

	public AdminProfileView() {
		ProfileResources.get.style().ensureInjected(); // FIXME, put in a common place
		initWidget(uiBinder.createAndBindUi(this));
		showDisabled.setValue(false);
		showNewsEmailOptOut.setValue(true);
		showServiceEmailOptOut.setValue(true);
		for (CheckBox box : Arrays.asList(showDisabled, showNewsEmailOptOut, showServiceEmailOptOut)) {
			box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					applyFilters();

				}
			});
		}
		filterText.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				applyFilters();

			}
		});
		// HACK. Otherwise the header classname interferes with header styles in main-sb.css
		emailsPanel.getHeader().getElement().getParentElement().setClassName("");
		driver.initialize(this);
		filterText.getElement().setAttribute("placeholder", "Search");
		UIObject.setVisible(editProfileDiv, false);
		setupList();
	}

	private void setupList() {
		profileList.setPageSize(10);
		profileList.setKeyboardPagingPolicy(KeyboardPagingPolicy.CHANGE_PAGE);
		profileList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

		final SingleSelectionModel<Profile> selectionModel = new SingleSelectionModel<Profile>();
		profileList.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				setSelectedProfile(selectionModel.getSelectedObject());
			}
		});

		dataProvider.addDataDisplay(profileList);
		pager.setDisplay(profileList);

	}

	private Profile editingProfile;

	/**
	 * @param selectedObject
	 */
	protected void setSelectedProfile(Profile selectedObject) {
		UIObject.setVisible(editProfileDiv, true);
		editingProfile = selectedObject;
		driver.edit(editingProfile);
		if (editingProfile.getAccountDisabled()) {
			status.setText("Disabled");
			toggleDisableButton.setText("Enable");
		} else {
			status.setText("Active");
			toggleDisableButton.setText("Disable");
		}

	}

	@UiHandler("toggleDisableButton")
	protected void onToggleDisable(ClickEvent e) {
		presenter.toggleAccountEnabled(editingProfile);
	}

	private List<Profile> fullList;

	public void setProfileList(List<Profile> list) {
		fullList = list;
		applyFilters();
		if (editingProfile != null) {
			int idx = list.indexOf(editingProfile);
			setSelectedProfile(list.get(idx));
		}
	}

	protected void updateEmailList() {
		StringBuilder sb = new StringBuilder();

		for (Profile curProfile : dataProvider.getList()) {
			sb.append("\"" + curProfile.getFirstName() + " " + curProfile.getLastName() + "\" <"
					+ curProfile.getEmail() + ">,\n");
		}

		emailArea.setText(sb.toString());
	}

	protected void applyFilters() {
		dataProvider.setList(applyFilters(fullList));
		profileList.setPageStart(0);
		updateEmailList();
	}

	// TODO push to presenter
	private List<Profile> applyFilters(List<Profile> list) {
		List<Profile> result = new ArrayList<Profile>(list.size());

		for (Profile p : list) {
			if (!showDisabled.getValue() && p.getAccountDisabled()) {
				continue;
			}
			if (!showNewsEmailOptOut.getValue() && p.getNotificationSettings() != null
					&& !p.getNotificationSettings().getEmailNewsAndEvents()) {
				continue;
			}
			if (!showServiceEmailOptOut.getValue() && p.getNotificationSettings() != null
					&& !p.getNotificationSettings().getEmailServiceAndMaintenance()) {
				continue;
			}

			String filterTextValue = filterText.getValue();
			if (filterTextValue != null && !filterTextValue.trim().isEmpty()) {
				filterTextValue = filterTextValue.trim().toLowerCase();
				boolean shouldFilter = true;
				for (String property : Arrays.asList(p.getUsername(), p.getFirstName(), p.getLastName(), p.getEmail())) {
					if (property != null && property.toLowerCase().contains(filterTextValue)) {
						shouldFilter = false;
						break;
					}
				}
				if (shouldFilter) {
					continue;
				}
			}
			result.add(p);
		}
		return result;
	}

	/**
	 * @param presenter
	 *            the presenter to set
	 */
	public void setPresenter(AdminProfilePresenter presenter) {
		this.presenter = presenter;
	}
}
