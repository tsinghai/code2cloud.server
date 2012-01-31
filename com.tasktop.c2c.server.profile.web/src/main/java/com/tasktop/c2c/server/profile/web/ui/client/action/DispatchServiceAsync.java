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
package com.tasktop.c2c.server.profile.web.ui.client.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.customware.gwt.dispatch.client.AbstractDispatchAsync;
import net.customware.gwt.dispatch.client.DefaultExceptionHandler;
import net.customware.gwt.dispatch.client.ExceptionHandler;
import net.customware.gwt.dispatch.client.standard.StandardDispatchService;
import net.customware.gwt.dispatch.client.standard.StandardDispatchServiceAsync;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.BatchAction;
import net.customware.gwt.dispatch.shared.BatchResult;
import net.customware.gwt.dispatch.shared.Result;


import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tasktop.c2c.server.common.web.client.event.ClearCacheEvent;
import com.tasktop.c2c.server.common.web.client.event.ClearCacheEventHandler;
import com.tasktop.c2c.server.common.web.shared.CachableReadAction;
import com.tasktop.c2c.server.common.web.shared.WriteAction;
import com.tasktop.c2c.server.profile.web.shared.Credentials;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogonEvent;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogonEventHandler;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogoutEvent;
import com.tasktop.c2c.server.profile.web.ui.client.event.LogoutEventHandler;
import com.tasktop.c2c.server.profile.web.ui.client.gin.AppGinjector;

public class DispatchServiceAsync extends AbstractDispatchAsync {

	private static StandardDispatchServiceAsync realService = null;

	private static StandardDispatchServiceAsync getRealService() {
		// This hack is to work around gwt deferred binding bug
		if (realService == null) {
			realService = GWT.create(StandardDispatchService.class);
		}
		return realService;
	}

	private static int cacheCleanDelay = 1 * 60 * 1000;
	private static int maxCacheLifetime = 3 * 60 * 1000;

	private class CachedResult {
		private final Result result;
		private final long exirationTime;

		public CachedResult(Result result) {
			this.result = result;
			this.exirationTime = System.currentTimeMillis() + maxCacheLifetime;
		}

		public Result getResult() {
			return result;
		}

		public boolean isExpired() {
			return System.currentTimeMillis() > this.exirationTime;
		}
	}

	private class CacheCleaner implements RepeatingCommand {

		@Override
		public boolean execute() {
			Iterator<Entry<Action<?>, CachedResult>> it = cachedActions.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Action<?>, CachedResult> entry = it.next();
				if (entry.getValue().isExpired()) {
					it.remove();
				}
			}
			return true;
		}

	}

	private Map<Action<?>, CachedResult> cachedActions = new HashMap<Action<?>, CachedResult>();

	public DispatchServiceAsync(ExceptionHandler exceptionHandler) {
		super(exceptionHandler);

		AppGinjector.get.instance().getScheduler().scheduleFixedDelay(new CacheCleaner(), cacheCleanDelay);
		AppGinjector.get.instance().getEventBus().addHandler(LogoutEvent.TYPE, new LogoutEventHandler() {

			@Override
			public void onLogout() {
				clearCache();
			}
		});
		AppGinjector.get.instance().getEventBus().addHandler(LogonEvent.TYPE, new LogonEventHandler() {

			@Override
			public void onLogon(Credentials credentials) {
				clearCache();
			}
		});
		AppGinjector.get.instance().getEventBus().addHandler(ClearCacheEvent.TYPE, new ClearCacheEventHandler() {

			@Override
			public void doClearCache() {
				clearCache();
			}
		});
	}

	public DispatchServiceAsync() {
		this(new DefaultExceptionHandler());
	}

	public <A extends Action<R>, R extends Result> void execute(final A action, final AsyncCallback<R> callback) {
		R result = fromCache(action);
		if (result != null) {
			callback.onSuccess(result);
		} else {
			makeServiceRequest(action, cacheingCallback(action, callback));
		}
	}

	private <A extends Action<R>, R extends Result> R fromCache(A action) {
		if (action instanceof CachableReadAction) {
			CachedResult result = cachedActions.get(action);
			return (R) (result == null ? null : result.getResult());
		} else if (action instanceof BatchAction) {
			BatchAction ba = (BatchAction) action;
			List<Result> results = new ArrayList<Result>(ba.getActions().length);
			for (Action<?> batchAction : ba.getActions()) {
				Result batchResult = fromCache(batchAction);
				if (batchResult == null) {
					return null;
				}
				results.add(batchResult);
			}
			return (R) new BatchResult(results, Collections.EMPTY_LIST);
		}
		return null;
	}

	private void clearCache() {
		cachedActions.clear();
	}

	private <A extends Action<R>, R extends Result> void maintainCache(A action, R result) {
		if (action instanceof WriteAction) {
			clearCache();
		} else if (action instanceof BatchAction) {
			BatchAction batchAction = (BatchAction) action;
			BatchResult batchRestul = (BatchResult) result;
			for (int i = 0; i < batchAction.getActions().length; i++) {
				// FIXME caste is workaround
				maintainCache((A) batchAction.getActions()[i], (R) batchRestul.getResult(i));
			}
		} else if (action instanceof CachableReadAction) {
			cachedActions.put(action, new CachedResult(result));
		}
	}

	private <A extends Action<R>, R extends Result> AsyncCallback<R> cacheingCallback(final A action,
			final AsyncCallback<R> callback) {
		return new AsyncCallback<R>() {

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(R result) {
				maintainCache(action, result);
				callback.onSuccess(result);

			}
		};
	}

	private <A extends Action<R>, R extends Result> void makeServiceRequest(final A action,
			final AsyncCallback<R> callback) {
		final StandardDispatchServiceAsync serviceHack = getRealService();
		serviceHack.execute(action, new AsyncCallback<Result>() {
			public void onFailure(Throwable caught) {
				DispatchServiceAsync.this.onFailure(action, caught, callback);
			}

			public void onSuccess(Result result) {
				DispatchServiceAsync.this.onSuccess(action, (R) result, callback);
			}
		});
	}

}
