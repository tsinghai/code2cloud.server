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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.BatchAction;
import net.customware.gwt.dispatch.shared.BatchAction.OnException;
import net.customware.gwt.dispatch.shared.BatchResult;
import net.customware.gwt.dispatch.shared.DispatchException;
import net.customware.gwt.dispatch.shared.Result;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.profile.web.client.AppState;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.shared.UserInfo;
import com.tasktop.c2c.server.profile.web.shared.actions.GetUserInfoAction;
import com.tasktop.c2c.server.profile.web.shared.actions.GetUserInfoResult;

/**
 * Base classes for places that first make rpcs to authorize, then make the rest rpcs.
 * 
 * @author Clint Morgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractBatchFetchingPlace extends AbstractPlace implements DefaultPlace {

	protected boolean requiresUserInfo = true;
	protected boolean readyToGo = false;
	private BatchResult results;

	protected void setUserInfo(UserInfo ui) {
		ProfileGinjector.get.instance().getAppState().setCredentials(ui.getCredentials());
		ProfileGinjector.get.instance().getAppState().setHasPendingAgreements(ui.getHasPendingAgreements());
	}

	public void go() {
		if (message == null) {
			message = new Message(0, "Loading...", Message.MessageType.PROGRESS);
		}
		notifier.displayMessage(message);

		if (!readyToGo) {
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
		return readyToGo;
	}

	@Override
	public void reset() {
		readyToGo = false;
	}

	/** Subclasses should call this from the handlBatchResults method after they are ready to go. */
	protected void onPlaceDataFetched() {
		readyToGo = true;
		go();
	}

	@SuppressWarnings("unchecked")
	protected <T extends Result> T getResult(Class<T> resultClass) {
		for (Result r : results) {
			if (r != null && r.getClass().getName().equals(resultClass.getName())) {
				return (T) r;
			}
		}
		return null;
	}

	/** Override to handle the batch results. Don't forget to call super. */
	protected void handleBatchResults() {

	}

	protected void onResultsRecieved() {
		if (requiresUserInfo) {
			setUserInfo(getResult(GetUserInfoResult.class).get());
		}

		if (isNotAuthorized()) {
			onNotAuthorised();
			return;
		}

		if (!AuthenticationHelper.isAnonymous()
				&& !(this instanceof AgreementsPlace)
				&& (ProfileGinjector.get.instance().getAppState().hasPendingAgreements() == null || ProfileGinjector.get
						.instance().getAppState().hasPendingAgreements())) {
			AgreementsPlace.createPlace(this).go();
			return;
		}

		if (hasException(null)) {
			boolean shouldContinue = handleExceptionInResults();

			if (!shouldContinue) {
				return;
			}
		}

		handleBatchResults();
	}

	/**
	 * Called after we have fetched the results. The userInfo will be populated. Default impl just looks for auth
	 * exceptions in the results or a disabled account
	 * 
	 * @return
	 */
	protected boolean isNotAuthorized() {
		return hasException("InsufficientPermissionsException") || hasException("AccessDeniedException")
				|| (requiresUserInfo && AuthenticationHelper.isAccountDisabled());
	}

	/**
	 * Called if there is an exception in the results.
	 * 
	 * @return true to continue with place. false if we should abort
	 */
	protected boolean handleExceptionInResults() {
		notifier.displayMessage(Message.createErrorMessage("A server-side error has occured"));
		return false;
	}

	protected boolean hasException(String exceptionClassName) {
		for (Throwable t : results.getExceptions()) {
			DispatchException dispatchException = (DispatchException) t;
			if (dispatchException != null) {
				if (exceptionClassName == null
						|| (dispatchException.getCauseClassname() != null && dispatchException.getCauseClassname()
								.contains(exceptionClassName))) {
					return true;
				}
			}
		}
		return false;
	}

	private BatchAction createFetchAction() {
		List<Action<?>> actions = new ArrayList<Action<?>>();
		if (requiresUserInfo) {
			actions.add(new GetUserInfoAction());
		}
		addActions(actions);
		return new BatchAction(OnException.CONTINUE, actions.toArray(new Action<?>[actions.size()]));
	}

	/** Override to add more actions. Don't forget to call super. */
	protected void addActions(List<Action<?>> actions) {

	}

	protected final void fetchPlaceData() {

		BatchAction fetchAction = createFetchAction();

		AsyncCallback<BatchResult> fetchCallback = new AsyncCallbackSupport<BatchResult>() {

			@Override
			protected void success(BatchResult result) {
				results = result;
				onResultsRecieved();
			}
		};
		CommonGinjector.get.instance().getDispatchService().execute(fetchAction, fetchCallback);
	}

	protected List<String> getUserRolesForPlace() {
		AppState appState = ProfileGinjector.get.instance().getAppState();
		if (appState == null || appState.getCredentials() == null || appState.getCredentials().getRoles() == null) {
			return Collections.EMPTY_LIST;
		}
		return appState.getCredentials().getRoles();
	}
}
