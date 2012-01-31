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
package com.tasktop.c2c.server.common.web.client.notification;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.notification.Message.Action;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;

public class NotificationPanel extends Composite implements Notifier {

	interface NotificationPanelUiBinder extends UiBinder<HTMLPanel, NotificationPanel> {
	}

	private static NotificationPanelUiBinder ourUiBinder = GWT.create(NotificationPanelUiBinder.class);
	@UiField
	protected SimplePanel notificationContentPanel;
	@UiField
	protected Anchor actionButton;
	@UiField
	protected HTMLPanel mole;

	private Message currentlyDisplayed;

	public NotificationPanel() {
		initWidget(ourUiBinder.createAndBindUi(this));
		actionButton.setText("Close");
		actionButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		CommonGinjector.get.instance().getEventBus().addHandler(PlaceChangeEvent.TYPE, new PlaceChangeEvent.Handler() {
			public void onPlaceChange(PlaceChangeEvent event) {
				hide();
			}
		});
		setVisible(false);

	}

	private void hide() {
		setVisible(false);
	}

	private void show() {
		setVisible(true);
	}

	private void displayErrorMessage(String message) {
		if (message != null) {
			notificationContentPanel.setWidget(new Label(message));
			actionButton.setVisible(true);
			mole.removeStyleName("success-mole");
			mole.addStyleName("error-mole");
			show();
		} else {
			hide();
		}
	}

	private void displaySuccessMessage(Widget message) {
		if (message != null) {
			notificationContentPanel.setWidget(message);
			actionButton.setVisible(true);
			mole.removeStyleName("error-mole");
			mole.addStyleName("success-mole");
			show();
		} else {
			hide();
		}
	}

	private void displayProgressMessage(String message) {
		if (message != null) {
			notificationContentPanel.setWidget(new Label(message));
			actionButton.setVisible(false);
			mole.removeStyleName("success-mole");
			mole.removeStyleName("error-mole");
			show();
		} else {
			hide();
		}
	}

	@Override
	public void displayMessage(final Message messageToDisplay) {
		if (messageToDisplay == null) {
			currentlyDisplayed = null;
			hide();
			return;
		}
		currentlyDisplayed = messageToDisplay;
		if (messageToDisplay.getMessageType() == Message.MessageType.PROGRESS) {
			displayProgressMessage(messageToDisplay.getMainMessage());
		}
		if (messageToDisplay.getMessageType() == Message.MessageType.ERROR) {
			displayErrorMessage(messageToDisplay.getMainMessage());
		}
		if (messageToDisplay.getMessageType() == Message.MessageType.SUCCESS) {
			Panel panel = new FlowPanel();
			panel.add(new Label(messageToDisplay.getMainMessage()));
			for (final Action action : messageToDisplay.getActions()) {
				Anchor anchor = new Anchor(action.getMessage());
				anchor.setHref(null); // for style underline
				anchor.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						action.getHandler().run();
					}
				});
				anchor.addStyleName("mole-action");
				panel.add(anchor);
			}

			displaySuccessMessage(panel);
		}

		if (messageToDisplay.getDisplayFor() > 0 && !messageToDisplay.isScheduledForRemoval()) {
			Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
				@Override
				public boolean execute() {
					messageToDisplay.setScheduledForRemoval(true);
					if (currentlyDisplayed == messageToDisplay) {
						displayMessage(null);
					}
					return false;
				}
			}, messageToDisplay.getDisplayFor() * 1000);
		}
	}

	@Override
	public void removeMessage(Message message) {
		if (message == currentlyDisplayed) {
			displayMessage(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.common.web.client.notification.Notifier#clearMessages()
	 */
	@Override
	public void clearMessages() {
		hide();
	}

}
