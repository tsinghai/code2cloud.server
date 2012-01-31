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
package com.tasktop.c2c.server.common.web.tests.client;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class JRETestUtil {

	// Appends the GWT debug prefix to a gwtDebugId
	public static String getDebugId(String id) {
		return Constants.GWT_DEBUG_ID_PREFIX + id;
	}

	public static <T> Stubber onSuccess(T data) {
		return call(true, data, null);
	}

	public static <T> Stubber onFailure(Throwable throwable) {
		return call(false, null, throwable);
	}

	// Returns a method used to stub AsyncCallback classes. Relies on the fact that the AsyncCallback is always
	// the last parameter in a service method.
	public static <T> Stubber call(final boolean success, final T data, final Throwable throwable) {
		return Mockito.doAnswer(new Answer<T>() {
			@Override
			@SuppressWarnings("unchecked")
			public T answer(InvocationOnMock invocationOnMock) throws Throwable {
				final Object[] args = invocationOnMock.getArguments();
				if (success) {
					((AsyncCallback) args[args.length - 1]).onSuccess(data);
				} else {
					((AsyncCallback) args[args.length - 1]).onFailure(throwable);
				}
				return null;
			}
		});
	}

	// Returns a method used to stub AsyncCallback classes. Relies on the fact that the AsyncCallback is always
	// the last parameter in a service method.
	public static <T> Stubber callMultiple(final List<T> data) {
		final AtomicInteger currentIndex = new AtomicInteger(0);
		return Mockito.doAnswer(new Answer<T>() {
			@Override
			@SuppressWarnings("unchecked")
			public T answer(InvocationOnMock invocationOnMock) throws Throwable {
				final Object[] args = invocationOnMock.getArguments();
				int i = currentIndex.get();
				if (i >= data.size()) {
					i = 0;
					currentIndex.set(0);
				}
				currentIndex.incrementAndGet();
				((AsyncCallback) args[args.length - 1]).onSuccess(data.get(i));

				return null;
			}
		});
	}
}
