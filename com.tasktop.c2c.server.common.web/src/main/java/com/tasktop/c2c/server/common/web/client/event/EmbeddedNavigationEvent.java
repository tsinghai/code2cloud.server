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
package com.tasktop.c2c.server.common.web.client.event;


import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.tasktop.c2c.server.common.web.client.navigation.PathMapping;
import com.tasktop.c2c.server.common.web.client.presenter.Presenter;

public class EmbeddedNavigationEvent extends GwtEvent<EmbeddedNavigationEventHandler> {
	public static Type<EmbeddedNavigationEventHandler> TYPE = new Type<EmbeddedNavigationEventHandler>();

	private final String navigation;

	public interface PresentationHandler {
		public void doPresentation();
	}

	private static class DefaultPresentationHandler implements PresentationHandler {

		private final HasOneWidget hasWidgets;
		private final Presenter presenter;

		public DefaultPresentationHandler(HasOneWidget hasWidgets, Presenter presenter) {
			this.hasWidgets = hasWidgets;
			this.presenter = presenter;
		}

		@Override
		public void doPresentation() {
			presenter.go(hasWidgets);
		}
	}

	private PresentationHandler presentationHandler;

	public EmbeddedNavigationEvent(HasOneWidget hasWidgets, Presenter presenter, PathMapping mapping, Object... args) {
		this(new DefaultPresentationHandler(hasWidgets, presenter), mapping, args);
	}

	public EmbeddedNavigationEvent(PresentationHandler presentationHandler, PathMapping mapping, Object... args) {
		this.presentationHandler = presentationHandler;
		navigation = mapping.uri(args);
	}

	public EmbeddedNavigationEvent(PresentationHandler presentationHandler, String uri) {
		this.presentationHandler = presentationHandler;
		navigation = uri;
	}

	@Override
	protected void dispatch(EmbeddedNavigationEventHandler handler) {
		handler.onNavigate(navigation);
		presentationHandler.doPresentation();
	}

	/**
	 * get the navigation target token
	 */
	public String getTargetHistoryToken() {
		return navigation;
	}

	@Override
	public Type<EmbeddedNavigationEventHandler> getAssociatedType() {
		return TYPE;
	}

}
