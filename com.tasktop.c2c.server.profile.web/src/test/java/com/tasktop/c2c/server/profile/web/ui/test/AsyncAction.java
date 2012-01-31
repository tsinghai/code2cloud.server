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
package com.tasktop.c2c.server.profile.web.ui.test;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.junit.Assert;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsyncAction<T> implements Action {
	private AsyncCallback<T> callback;

	public void succeedGiving(T result) {
		checkCallback();
		callback.onSuccess(result);
	}

	public void fail(Throwable caught) {
		checkCallback();
		callback.onFailure(caught);
	}

	public void describeTo(Description description) {
		description.appendText("requests an async callback");
	}

	private void checkCallback() {
		if (callback == null) {
			Assert.fail("asynchronous action not scheduled");
		}
	}

	@SuppressWarnings("unchecked")
	public Object invoke(Invocation invocation) throws Throwable {
		callback = (AsyncCallback<T>) invocation.getParameter(invocation.getParameterCount() - 1);
		return null;
	}
}
