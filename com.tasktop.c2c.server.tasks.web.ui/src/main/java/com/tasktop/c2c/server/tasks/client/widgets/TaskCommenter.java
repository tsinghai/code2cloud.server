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


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.Avatar;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.Task;

public class TaskCommenter extends Composite {
	interface Binder extends UiBinder<Widget, TaskCommenter> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	protected Image avatarImage;

	public TaskCommenter() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public TaskCommenter(final Task task, final Comment comment, final int commentNumber) {
		initWidget(uiBinder.createAndBindUi(this));

		String avatarUrl = Avatar.computeAvatarUrl(comment.getAuthor().getGravatarHash(), Avatar.Size.MICRO);
		avatarImage.setUrl(avatarUrl);
	}
}
