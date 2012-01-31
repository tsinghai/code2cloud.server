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


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.common.web.client.view.Avatar;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.place.Section;
import com.tasktop.c2c.server.profile.web.client.place.SignInPlace;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;

public class HeaderView extends AbstractComposite {

	interface HeaderViewUiBinder extends UiBinder<Widget, HeaderView> {
	}

	private static HeaderViewUiBinder uiBinder = GWT.create(HeaderViewUiBinder.class);

	private static HeaderView instance = null;

	public static HeaderView getInstance() {
		if (instance == null) {
			instance = new HeaderView();
		}
		return instance;
	}

	@UiField
	ProjectIconPanel iconPanel;

	@UiField
	public Button searchButton;
	@UiField
	public TextBox search;

	// unauthenticated
	@UiField
	Anchor signIn;

	// authenticated
	@UiField
	Panel userMenu;
	@UiField(provided = true)
	public Image avatarImage = AvatarHolder.avatarImage;

	// page title
	@UiField
	Label title;

	@UiField
	Label ownerBadge;

	// breadcrumbs
	@UiField
	DivElement projectSpecificDiv;
	@UiField
	Panel breadcrumbNavigation;

	private HeaderView() {
		initWidget(uiBinder.createAndBindUi(this));

		signIn.setHref(SignInPlace.createPlace().getHref());

		// By default, hide the project specific stuff.
		setProject(null);
		search.getElement().setAttribute("placeholder", "Search");
		hookDefaultButton(searchButton);

	}

	public void setAuthenticated(boolean isAuthenticated) {
		// If this user is authenticated, then display the "Projects" link.
		userMenu.setVisible(isAuthenticated);
		signIn.setVisible(!isAuthenticated);

		if (isAuthenticated) {

			String username = ProfileEntryPoint.getInstance().getAppState().getCredentials().getProfile().getUsername();

			// Set the user's name as the title and alt text of the Gravatar image
			avatarImage.setTitle(username);
			avatarImage.setAltText(username);
		} else {
			// Blank out any existing text
			avatarImage.setTitle("");
			avatarImage.setAltText("");
		}
	}

	private UserMenuPopupPanel userMenuPopupPanel = null;

	private List<Breadcrumb> breadcrumbs;
	private String initialBorderWidthProperty;

	@UiHandler("userMenuClickArea")
	public void showMenu(ClickEvent e) {
		if (userMenuPopupPanel == null) {
			userMenuPopupPanel = new UserMenuPopupPanel();
			userMenuPopupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
				@Override
				public void onClose(CloseEvent<PopupPanel> customPopupPanelCloseEvent) {
					DOM.setStyleAttribute(userMenu.getElement(), "borderWidth", initialBorderWidthProperty);
				}
			});
			userMenuPopupPanel.addAutoHidePartner(userMenu.getElement());
			userMenuPopupPanel.removeStyleName("gwt-PopupPanel");
			userMenuPopupPanel.setStyleName("account-options");
		}

		if (userMenuPopupPanel.isShowing()) {
			userMenuPopupPanel.hide();
		} else {
			userMenuPopupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
				@Override
				public void setPosition(int offsetWidth, int offsetHeight) {
					int menuBottom = userMenu.getAbsoluteTop() + userMenu.getOffsetHeight();
					int menuRight = userMenu.getAbsoluteLeft() + userMenu.getOffsetWidth();
					// Due initial popup width detection being off, we must explicitly specify the width
					int popupLeft = menuRight - 102;
					// Store the old border property
					initialBorderWidthProperty = DOM.getStyleAttribute(userMenu.getElement(), "borderWidth");
					// Apply the new border property
					DOM.setStyleAttribute(userMenu.getElement(), "borderWidth", "1px 1px 0px 1px");
					// Set popup position taking into account the now reduce border params of the menu
					userMenuPopupPanel.setPopupPosition(popupLeft, menuBottom - 2);
				}
			});
		}
	}

	public void setProject(Project project) {
		if (project == null) {
			projectSpecificDiv.setClassName("hide-project-nav");
			this.ownerBadge.setText("");
		} else {
			projectSpecificDiv.removeClassName("hide-project-nav");
			iconPanel.setProject(project);

			if (AuthenticationHelper.isAdmin(project.getIdentifier())) {
				this.ownerBadge.setText("owner");
			}
		}
	}

	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	public void setBreadcrumbs(List<Breadcrumb> breadcrumbs) {
		this.breadcrumbs = breadcrumbs;
		breadcrumbNavigation.clear();
		boolean first = true;
		for (Breadcrumb breadcrumb : breadcrumbs) {
			if (first == false) {
				InlineHTML span = new InlineHTML();
				span.setStyleName("arrow");
				span.setText("/");
				breadcrumbNavigation.add(span);
			}
			Anchor link = new Anchor(breadcrumb.getLabel(), "#" + breadcrumb.getUri());
			link.setStyleName("crumb");
			breadcrumbNavigation.add(link);

			first = false;
		}

	}

	public void setSection(Section section) {
		iconPanel.setActiveIcon(section);
	}

	public void setPageTitle(String title) {
		this.title.setText(title);
	}

	public void setGravatarHash(String hash) {
		String avatar = Avatar.computeAvatarUrl(hash, Avatar.Size.SMALL);
		if (!avatar.equals(AvatarHolder.lastAvatarUrl)) {
			AvatarHolder.avatarImage.setUrl(avatar);
			AvatarHolder.lastAvatarUrl = avatar;
		}
	}

	private static class AvatarHolder {
		public static String lastAvatarUrl = null;
		public static Image avatarImage = new Image();
	}

}
