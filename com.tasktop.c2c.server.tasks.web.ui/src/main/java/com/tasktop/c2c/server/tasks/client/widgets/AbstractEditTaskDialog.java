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
package com.tasktop.c2c.server.tasks.client.widgets;


import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.tasks.client.presenters.TaskPresenter;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.Task;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractEditTaskDialog extends DialogBox {
	protected RepositoryConfiguration repositoryConfiguration;
	protected TaskPresenter presenter;

	@UiField
	Button saveButton;

	public AbstractEditTaskDialog(String title) {
		super(true, true);
		super.setText(title);
		super.setAnimationEnabled(true);
		super.setGlassEnabled(false);
	}

	public abstract void setTask(Task t);

	public void setPresenter(TaskPresenter presenter) {
		this.presenter = presenter;
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent e) {
		this.hide();
	}

	@UiHandler("saveButton")
	public void onSave(ClickEvent e) {
		this.hide();
		save();
	}

	protected abstract void save();

	public void setRepositoryConfiguration(RepositoryConfiguration config) {
		this.repositoryConfiguration = config;
	}

	@Override
	public void show() {
		super.show();

		ProfileGinjector.get.instance().getScheduler().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
				getInitialField().getElement().focus();
			}
		});
	}

	abstract protected Widget getInitialField();

	protected void attachEnterForSubmit(HasKeyPressHandlers widget) {
		widget.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (KeyCodes.KEY_ENTER == event.getUnicodeCharCode()) {
					onSave(null);
				}
			}
		});
	}
}
