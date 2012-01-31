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

import net.customware.gwt.dispatch.shared.ActionException;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.common.web.shared.NoSuchEntityException;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskAction;
import com.tasktop.c2c.server.tasks.shared.action.GetTaskResult;

public class TaskAnchorManager {

	private TaskAnchor target;
	private TaskDetailsPopupPanel popup = TaskDetailsPopupPanel.getInstance();

	private final Timer timer = new Timer() {

		@Override
		public void run() {
			// Display our popup
			showPopup();
		}
	};

	private final Timer fetchTimer = new Timer() {
		@Override
		public void run() {
			fetchTask(false);
		}
	};

	private final MouseOverHandler mouseOverHandler = new MouseOverHandler() {

		@Override
		public void onMouseOver(MouseOverEvent event) {
			if (target != event.getSource()) {
				hidePopup();
			}
			target = (TaskAnchor) event.getSource();
			if (target.getTask() != null) {
				timer.schedule(500);
			} else {
				fetchTimer.schedule(200);
			}
		}
	};

	private final MouseOutHandler mouseOutHandler = new MouseOutHandler() {

		@Override
		public void onMouseOut(MouseOutEvent event) {
			// Cancel our timer, in case it hasn't fired yet. If it has, the dialog will now be showing and this
			// will have no effect.
			timer.cancel();
			fetchTimer.cancel();

			// Explicitly do *NOT* hide the popup, as it makes the popup almost impossible to use (the second the mouse
			// moves off the link, it disappears)
			// hidePopup();
		}
	};

	private final ClickHandler clickHandler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			// Cancel our timer, in case it hasn't fired yet. If it has, the dialog will now be showing and this
			// will have no effect.
			timer.cancel();
			fetchTimer.cancel();
			hidePopup();
		}
	};

	private static final TaskAnchorManager INSTANCE = new TaskAnchorManager();

	public static TaskAnchor createAnchor(Task task) {
		return new TaskAnchor(task);
	}

	private TaskAnchorManager() {
	}

	public static TaskAnchor createAnchor(String projIdentifier, int taskId, String text, String href) {
		TaskAnchor anchor = new TaskAnchor(projIdentifier, taskId, text, href);
		anchor.addMouseOverHandler(INSTANCE.mouseOverHandler);
		anchor.addMouseOutHandler(INSTANCE.mouseOutHandler);
		anchor.addClickHandler(INSTANCE.clickHandler);
		return anchor;
	}

	private void showPopup() {
		if (target == null) {
			hidePopup();
			return;
		}
		fetchTask(true);
	}

	private void fetchTask(final boolean displayPopup) {
		if (target == null) {
			return;
		}
		final long startTime = System.currentTimeMillis();
		CommonGinjector.get
				.instance()
				.getDispatchService()
				.execute(new GetTaskAction(target.getProjectIdentifier(), target.getTaskId()),
						new AsyncCallbackSupport<GetTaskResult>() {
							@Override
							protected void success(GetTaskResult actionResult) {
								Task result = actionResult.get();
								// Display our popup with this task's data
								if (displayPopup) {
									displayPopupForTask(result);
								} else {
									long fetchDuration = System.currentTimeMillis() - startTime;
									int delay = 300;
									if (fetchDuration >= delay) {
										displayPopupForTask(result);
									} else {
										timer.schedule((int) (delay - fetchDuration));
									}
								}
							}

							@Override
							public void onFailure(Throwable exception) {
								if (exception instanceof ActionException
										&& ((ActionException) exception).getCauseClassname().equals(
												NoSuchEntityException.class.getName())) {
									// Do nothing, this is semi-expected (e.g. if a user puts in "task 100" and no task
									// 100 exists yet) - we don't want to display an error of any kind, we just want to
									// not display the popup.
								} else {
									super.onFailure(exception);
								}
							}
						});
	}

	private void hidePopup() {
		target = null;
		// If our popup is visible, hide it.
		popup.hide();
	}

	private void displayPopupForTask(Task task) {
		if (target == null) {
			hidePopup();
			return;
		}
		popup.setTask(task);
		popup.showAt(target.getAbsoluteLeft(), target.getAbsoluteTop(), target.getOffsetWidth(),
				target.getOffsetHeight());

	}
}
