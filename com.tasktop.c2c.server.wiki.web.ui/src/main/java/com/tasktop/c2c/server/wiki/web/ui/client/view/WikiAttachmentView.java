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
package com.tasktop.c2c.server.wiki.web.ui.client.view;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.Format;
import com.tasktop.c2c.server.wiki.domain.Attachment;

public class WikiAttachmentView extends Composite {

	interface Binder extends UiBinder<Widget, WikiAttachmentView> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Anchor name;
	@UiField
	Label size;
	@UiField
	Label modificationDate;
	@UiField
	Label user;

	public WikiAttachmentView(Attachment attachment) {
		initWidget(uiBinder.createAndBindUi(this));

		name.setText(attachment.getName());
		name.setHref(attachment.getUrl());
		size.setText(String.valueOf(Math.round(Float.valueOf(attachment.getSize()) / 1024)) + " kB");
		modificationDate.setText(Format.stringValueDateTime(attachment.getModificationDate()));
		user.setText(attachment.getLastAuthor().getName());
	}
}
