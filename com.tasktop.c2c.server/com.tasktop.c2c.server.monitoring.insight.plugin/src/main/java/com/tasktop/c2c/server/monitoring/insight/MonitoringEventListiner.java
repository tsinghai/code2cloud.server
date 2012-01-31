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
package com.tasktop.c2c.server.monitoring.insight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;

import com.springsource.insight.intercept.EventInterceptListener;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.TraceInterceptListener;
import com.springsource.insight.intercept.event.Event;
import com.springsource.insight.intercept.operation.BasicOperation;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.web.HttpOperation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.tasktop.c2c.server.monitoring.domain.ErrorEvent;
import com.tasktop.c2c.server.monitoring.domain.MonitoringEvent;
import com.tasktop.c2c.server.monitoring.domain.SlowResponseEvent;
import com.tasktop.c2c.server.monitoring.service.MonitoringService;

public class MonitoringEventListiner implements EventInterceptListener, TraceInterceptListener, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringEventListiner.class.getName());

	private long slowResponseThreshold = 500;

	private MonitoringService monitoringService;

	private void registerWithDispatcher() {
		InterceptConfiguration.getInstance().getDispatcher().register(MonitoringEventListiner.this);
	}

	public void handleEventDispatch(Event event) {
		// Not sure when these happen
		LOGGER.info("Code2Cloud sees event for :" + event.getResource().getApplicationName() + " :"
				+ event.getResource().getName());
	}

	public void handleTraceDispatch(Trace trace) {

		Operation rootFrameOperation = trace.getRootFrame().getOperation();
		LOGGER.info("processing trace of  [" + trace.getApplicationName() + ":" + trace.getLabel() + "]. Operation: ["
				+ rootFrameOperation.getType().getName() + "] took " + trace.getRange().getDurationMillis() + "ms.");

		if (isHttpError(trace)) {
			handleHttpError(trace);
		} else if (hasException(trace)) {
			handleException(trace);
		} else if (trace.getRange().getDurationMillis() > slowResponseThreshold) {
			handleSlowResponse(trace);
		}
	}

	private boolean isHttpError(Trace trace) {
		if (trace.getRootFrame().getOperation() instanceof HttpOperation) {
			HttpOperation op = (HttpOperation) trace.getRootFrame().getOperation();
			if (op.getResponseDetails().getStatusCode() >= 500) {
				return true;
			}
		}
		return false;
	}

	private void handleHttpError(Trace trace) {
		String exceptionString = findFirstException(trace.getRootFrame());
		LOGGER.info("got error at [" + trace.getApplicationName() + ":" + trace.getLabel() + "]. Operation: ["
				+ trace.getRootFrame().getOperation().getType().getName() + "] Exception: [" + exceptionString + "]");
		ErrorEvent event = new ErrorEvent();
		event.setExceptionString(exceptionString);
		HttpOperation op = (HttpOperation) trace.getRootFrame().getOperation();
		event.setEventDescription(op.getRequestDetails().getUri().toString() + " returned Status code "
				+ op.getResponseDetails().getStatusCode());
		injectTraceInfo(trace, event);
		monitoringService.processEvent(event);
	}

	private void handleException(Trace trace) {
		String exceptionString = findFirstException(trace.getRootFrame());

		LOGGER.info("got exceptiong during [" + trace.getApplicationName() + ":" + trace.getLabel() + "]. Operation: ["
				+ trace.getRootFrame().getOperation().getType().getName() + "] Exception: [" + exceptionString + "]");
		ErrorEvent event = new ErrorEvent();
		event.setExceptionString(exceptionString);
		event.setEventDescription("Exception during " + trace.getLabel());
		injectTraceInfo(trace, event);
		monitoringService.processEvent(event);
	}

	private boolean hasException(Trace trace) {
		return findFirstException(trace.getRootFrame()) != null;
	}

	private String findFirstException(Frame frame) {
		if (frame.getOperation() instanceof BasicOperation) {
			if (((BasicOperation) frame.getOperation()).getException() != null) {
				return ((BasicOperation) frame.getOperation()).getException();
			}
		}
		for (Frame child : frame.getChildren()) {
			String childEx = findFirstException(child);
			if (childEx != null) {
				return childEx;
			}
		}
		return null;
	}

	private void handleSlowResponse(Trace trace) {
		LOGGER.info("slow response at [" + trace.getApplicationName() + ":" + trace.getLabel() + "]. Operation: ["
				+ trace.getRootFrame().getOperation().getType().getName() + "]. Response time: ["
				+ trace.getRange().getDurationMillis() + "]ms");
		SlowResponseEvent event = new SlowResponseEvent();
		event.setDurationInMilliseconds(trace.getRange().getDurationMillis());
		injectTraceInfo(trace, event);
		event.setEventDescription(event.getEventLabel() + " took " + event.getDurationInMilliseconds() + "ms.");
		monitoringService.processEvent(event);
	}

	private void injectTraceInfo(Trace trace, MonitoringEvent event) {
		event.setEventApplication(trace.getApplicationName().getName());
		event.setEventLabel(trace.getLabel());
		event.setTraceString(trace.toString() + "\n" + traceString(trace.getRootFrame(), 1));
	}

	private String traceString(Frame frame, int depth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			sb.append("  ");
		}
		sb.append(frame.getOperation().getLabel()).append(" took ").append(frame.getRange().getDurationMillis())
				.append("ms.\n");

		for (Frame child : frame.getChildren()) {
			sb.append(traceString(child, depth + 1));
		}
		return sb.toString();
	}

	public void setMonitoringService(MonitoringService monitoringService) {
		this.monitoringService = monitoringService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		registerWithDispatcher();
	}

}
