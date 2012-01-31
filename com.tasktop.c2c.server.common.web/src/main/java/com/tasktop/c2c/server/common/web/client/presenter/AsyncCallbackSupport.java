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

import java.util.List;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.Notifier;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;

public abstract class AsyncCallbackSupport<T> implements AsyncCallback<T> {

	public interface ErrorHandler {
		List<String> getErrors(Throwable exception);
	}

	private static ErrorHandler errorHandler;

	public static void setErrorHandler(ErrorHandler handler) {
		errorHandler = handler;
	}

	private OperationMessage operationMessage;
	private ErrorCapableView overrideErrorView;
	private Notifier notifier = CommonGinjector.get.instance().getNotifier();

	public AsyncCallbackSupport() {
		this(null, null);
	}

	public AsyncCallbackSupport(OperationMessage operationMessage) {
		this(operationMessage, null);
	}

	public AsyncCallbackSupport(ErrorCapableView overrideView) {
		this(null, overrideView);
	}

	public AsyncCallbackSupport(OperationMessage operationMessage, ErrorCapableView overrideView) {
		this.operationMessage = operationMessage;
		this.overrideErrorView = overrideView;
		operationStart();
	}

	private void operationStart() {
		if (operationMessage != null) {
			notifier.displayMessage(operationMessage.getProgressMessage());
		}
	}

	private void operationEnd() {
		if (operationMessage != null) {
			notifier.displayMessage(operationMessage.getSuccessMessage());
		}
	}

	@Override
	public final void onSuccess(T result) {
		operationEnd();
		success(result);
	}

	protected abstract void success(T result);

	@Override
	public void onFailure(Throwable exception) {
		if (overrideErrorView != null) {
			overrideErrorView.displayErrors(getErrors(exception));
		} else {
			String errorMsg = StringUtils.concatenate(getErrors(exception));
			Message message = new Message(0, errorMsg, Message.MessageType.ERROR);
			notifier.displayMessage(message);
		}
	}

	protected List<String> getErrors(Throwable exception) {
		return errorHandler.getErrors(exception);
	}
}
