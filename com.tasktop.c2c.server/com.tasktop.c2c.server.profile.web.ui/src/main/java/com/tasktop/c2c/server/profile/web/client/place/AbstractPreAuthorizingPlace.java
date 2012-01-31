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
package com.tasktop.c2c.server.profile.web.client.place;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import com.google.gwt.core.client.Scheduler;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;

/**
 * Base classes for places that first make rpcs to authorize, then make the rest rpcs. This is deprecated. Instead use
 * the batching pattern.
 * 
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractPreAuthorizingPlace extends AbstractPlace {

	boolean permissionGranted = false;
	boolean ready = false;
	boolean preparing = false;
	boolean seekingPermission = false;

	protected final Set<String> roles;

	protected Set<String> getRequiredRoles() {
		return roles;
	}

	protected AbstractPreAuthorizingPlace(Set<String> roles) {
		this.roles = roles;
	}

	protected AbstractPreAuthorizingPlace() {
		this(new HashSet<String>());
	}

	protected Set<String> roles() {
		return Collections.unmodifiableSet(roles);
	}

	protected abstract void fetchPlaceData();

	protected void setPermissionGranted(boolean permissionGranted) {
		this.permissionGranted = permissionGranted;
	}

	protected void reset() {
		permissionGranted = false;
		seekingPermission = false;
		preparing = false;
		ready = false;
	}

	public void go() {
		if (message == null) {
			message = new Message(0, "Loading...", Message.MessageType.PROGRESS);
		}
		notifier.displayMessage(message);
		// If permission has already been requested, but not yet granted, don't it request again
		if (seekingPermission && !hasParentsPermission()) {
			return;
		}
		// If we don't yet have parent's permission, request it
		if (!hasParentsPermission()) {
			seekingPermission = true;
			seekPermission();
			return;
		}
		// If data is already being fetched don't try to fetch it again
		if (preparing) {
			return;
		}
		// If we aren't ready try to prepare
		if (!ready) {
			preparing = true;
			fetchPlaceData();
			return;
		}
		ProfileGinjector.get.instance().getPlaceController().goTo(this);
		ProfileGinjector.get.instance().getScheduler().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				notifier.removeMessage(message);
				if (displayOnArrival != null) {
					notifier.displayMessage(displayOnArrival);
				}
			}
		});

	}

	public boolean isReadyToGo() {
		return ready;
	}

	protected void setReadyToGo(boolean ready) {
		this.ready = ready;
	}

	protected void seekPermission() {
		permissionGranted = true;
		seekingPermission = false;
		go();
	}

	protected boolean hasParentsPermission() {
		return permissionGranted;
	}

	protected final void onPlaceDataFetched() {
		ready = true;
		preparing = false;
		go();
	}
}
