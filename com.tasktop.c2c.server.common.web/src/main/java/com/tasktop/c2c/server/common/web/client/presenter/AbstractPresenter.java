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
package com.tasktop.c2c.server.common.web.client.presenter;


import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.common.web.client.notification.Notifier;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;

public abstract class AbstractPresenter extends AbstractActivity implements Presenter {

	private final IsWidget view;

	protected AbstractPresenter(IsWidget view) {
		this.view = view;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		go(panel);
	}

	@Override
	public void go(AcceptsOneWidget container) {
		show(container);
		bind();
	}

	public void show(AcceptsOneWidget container) {
		container.setWidget(view);
	}

	protected abstract void bind();

	public final EventBus getEventBus() {
		return CommonGinjector.get.instance().getEventBus();
	}

	public final Notifier getNotifier() {
		return CommonGinjector.get.instance().getNotifier();
	}
}
