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
package com.tasktop.c2c.server.tasks.client.widgets;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Timer;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public abstract class AbstractHoverCell<C> extends AbstractCell<C> {

	private int delay = 500;

	public interface HoverListener<C> {
		void onHover(Context context, C value, NativeEvent event, Element element);

		void cancelHover(NativeEvent event);
	}

	private Element element;
	private HoverListener<C> listener;
	private Context context;
	private C value;
	private NativeEvent event;

	private Timer t = new Timer() {

		@Override
		public void run() {
			if (listener != null) {
				listener.onHover(context, value, event, element);
			}
		}

	};

	public AbstractHoverCell() {
		super("mouseover", "mouseout", "click", "mousemove", "mousewheel");
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, C value, NativeEvent event, ValueUpdater<C> valueUpdater) {
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		this.event = event;

		if ("mouseover".equals(event.getType())) {
			this.value = value;
			this.context = context;
			this.element = parent;
			t.schedule(delay);
		} else if ("mouseout".equals(event.getType())) {
			t.cancel();
			if (listener != null) {
				listener.cancelHover(event);
			}
		} else if ("click".equals(event.getType())) {
			t.cancel();
			if (listener != null) {
				listener.cancelHover(event);
			}
		} else if ("mousewheel".equals(event.getType())) {
			t.cancel();
			if (listener != null) {
				listener.cancelHover(event);
			}
		}
	}

	/**
	 * @param listener
	 *            the listener to set
	 */
	public void setHoverListener(HoverListener<C> listener) {
		this.listener = listener;
	}
}
