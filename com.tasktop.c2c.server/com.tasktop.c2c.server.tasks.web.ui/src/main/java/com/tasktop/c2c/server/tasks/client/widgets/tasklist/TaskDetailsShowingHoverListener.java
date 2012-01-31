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
package com.tasktop.c2c.server.tasks.client.widgets.tasklist;


import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Window;
import com.tasktop.c2c.server.tasks.client.widgets.TaskDetailsPopupPanel;
import com.tasktop.c2c.server.tasks.client.widgets.AbstractHoverCell.HoverListener;
import com.tasktop.c2c.server.tasks.domain.Task;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class TaskDetailsShowingHoverListener implements HoverListener<Task> {

	private static TaskDetailsShowingHoverListener instance;

	public static TaskDetailsShowingHoverListener getInstance() {
		if (instance == null) {
			instance = new TaskDetailsShowingHoverListener();
		}
		return instance;
	}

	private TaskDetailsShowingHoverListener() {
		// singleton
	}


	@Override
	public void onHover(Context context, Task value, NativeEvent event, Element element) {
		TaskDetailsPopupPanel.getInstance().setTask(value);
		TaskDetailsPopupPanel.getInstance().showAt(element.getAbsoluteLeft(), element.getAbsoluteTop(), element.getOffsetWidth(), element.getOffsetHeight());
	}

	private static final int X_SLOP = 10;
	private static final int Y_SLOP = 10;

	@Override
	public void cancelHover(NativeEvent event) {
		if ("mousewheel".equals(event.getType())) {
			TaskDetailsPopupPanel.getInstance().hide(true);
			return;
		}
		int popupLeft = TaskDetailsPopupPanel.getInstance().getWidget().getElement().getAbsoluteLeft();
		int popupRight = TaskDetailsPopupPanel.getInstance().getWidget().getElement().getAbsoluteRight();
		int popupTop = TaskDetailsPopupPanel.getInstance().getWidget().getElement().getAbsoluteTop();
		int popupBottom = TaskDetailsPopupPanel.getInstance().getWidget().getElement().getAbsoluteBottom();
		int eventX = event.getClientX();
		int eventY = event.getClientY();
		if (eventX + X_SLOP >= popupLeft && eventX - X_SLOP <= popupRight && eventY + Y_SLOP >= popupTop
				&& eventY - Y_SLOP <= popupBottom) {
			// into the popupu, ignore cancel
		} else {
			TaskDetailsPopupPanel.getInstance().hide(true);
		}
	}
}
