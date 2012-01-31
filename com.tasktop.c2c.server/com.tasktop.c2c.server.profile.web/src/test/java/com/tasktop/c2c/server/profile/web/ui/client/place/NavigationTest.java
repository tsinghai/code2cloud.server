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
package com.tasktop.c2c.server.profile.web.ui.client.place;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.tasktop.c2c.server.common.web.client.navigation.PathMapping;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.Message.MessageType;
import com.tasktop.c2c.server.common.web.client.notification.Notifier;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.ui.client.ProfileEntryPoint;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class NavigationTest {

	private PlaceHistoryMapper historyMapper;
	private AppGinjector appGinjector;
	private Notifier notifier;

	@Before
	public void setUp() throws Exception {
		GWTMockUtilities.disarm();
		appGinjector = mock(AppGinjector.class, RETURNS_MOCKS);
		AppGinjector.get.override(appGinjector);

		historyMapper = new AppHistoryMapper();

		notifier = appGinjector.getNotifier();
		when(appGinjector.getNotifier()).thenReturn(notifier);
	}

	@BeforeClass
	public static void setupClass() {
		new ProfileEntryPoint(); // Registers page mappings
	}

	@Test
	public void testRoot() {
		Place place = historyMapper.getPlace("projects");
		Assert.assertEquals(ProjectsDiscoverPlace.class, place.getClass());
	}

	@Test
	public void testPageNotFound() {

		Place place = historyMapper.getPlace("badUrl");
		verify(notifier).displayMessage(argThat(new BaseMatcher<Message>() {

			@Override
			public boolean matches(Object arg) {
				Message m = (Message) arg;
				return m.getMessageType().equals(MessageType.ERROR);
			}

			@Override
			public void describeTo(Description arg0) {

			}
		}));
	}

	@Test
	public void testAllPageMappingsParse() {
		for (PageMapping pageMapping : PageMapping.getRegisteredMappings()) {
			Place lastPlace = null;
			for (PathMapping pathMapping : pageMapping.getPathMappings()) {
				String[] args = new String[pathMapping.getPath().getArgumentCount()];
				for (int i = 0; i < args.length; i++) {
					if (pathMapping.getPath().isArgumentInteger(i)) {
						args[i] = i + "";
					} else {
						args[i] = "arg" + (i + 1);
					}
				}
				String path = pathMapping.getPath().uri((Object[]) args);
				if (path.contains("?")) {
					path = path.substring(0, path.indexOf("?"));
				}
				System.out.println("Testing url: " + path);
				Place place = historyMapper.getPlace(path);
				Assert.assertNotNull(place);
				if (lastPlace != null) {
					Assert.assertEquals(lastPlace.getClass(), place.getClass());
				}

				if (place instanceof AppSectionPlace) {
					continue; // FIXME
				}

				String pathFromPlace = historyMapper.getToken(place);
				Assert.assertEquals(path, pathFromPlace);

				lastPlace = place;
			}
		}
	}
}
