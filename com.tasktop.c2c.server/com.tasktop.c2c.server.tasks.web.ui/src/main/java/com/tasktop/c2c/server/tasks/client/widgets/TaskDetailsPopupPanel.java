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

import java.math.BigDecimal;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceChangeRequestEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.NumberLabel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.ValueLabel;
import com.tasktop.c2c.server.common.web.client.event.AppScrollEvent;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.common.web.client.widgets.time.TimePeriodRenderer;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Task;

/**
 * @author Clint Morgan (Tasktop Technologies Inc.)
 * 
 */
public class TaskDetailsPopupPanel extends DecoratedPopupPanel implements Editor<Task>, PlaceChangeEvent.Handler,
		PlaceChangeRequestEvent.Handler {

	private static final int MAX_DESCRIPTION_LEN = 50;

	interface Binder extends UiBinder<HTMLPanel, TaskDetailsPopupPanel> {
	}

	interface Driver extends SimpleBeanEditorDriver<Task, TaskDetailsPopupPanel> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);
	private static Driver driver = GWT.create(Driver.class);

	private static TaskDetailsPopupPanel instance;

	public static TaskDetailsPopupPanel getInstance() {
		if (instance == null) {
			instance = new TaskDetailsPopupPanel();
		}
		return instance;
	}

	@UiField
	Label taskType;

	@UiField
	NumberLabel<Integer> id;

	@UiField
	Label shortDescription;

	@UiField
	@Editor.Ignore
	AnchorElement taskAnchorElement;

	@UiField
	@Editor.Path("reporter.loginName")
	Label reporter;

	@UiField(provided = true)
	DateLabel creationDate = new DateLabel(DateTimeFormat.getFormat("mm/dd/yy"));

	@UiField
	@Editor.Ignore
	Anchor commentsAnchor;

	@UiField
	@Editor.Ignore
	DivElement priorityDivElement;

	@UiField
	@Editor.Ignore
	SpanElement resolvedStatusIconSpanElement;

	@UiField
	@Editor.Path("status.value")
	Label status;

	@UiField
	@Editor.Path("resolution.value")
	Label resolution;

	@UiField
	DivElement severityDivElement;

	@UiField
	@Editor.Ignore
	Label description;

	@UiField
	@Editor.Ignore
	Anchor moreDescriptionAnchor;

	@UiField
	@Editor.Path("product.name")
	Label productName;

	@UiField
	@Editor.Path("component.name")
	Label componentName;

	@UiField
	@Editor.Path("milestone.value")
	Label milestoneValue;

	@UiField
	@Editor.Path("iteration.value")
	Label iteration;

	@UiField(provided = true)
	ValueLabel<BigDecimal> estimatedTime = new ValueLabel<BigDecimal>(TimePeriodRenderer.HOUR_RENDERER);

	@UiField(provided = true)
	ValueLabel<List<Keyword>> keywords = new ValueLabel<List<Keyword>>(new AbstractRenderer<List<Keyword>>() {

		@Override
		public String render(List<Keyword> keywords) {
			String result = "";
			if (keywords != null) {
				for (Keyword k : keywords) {
					if (!result.isEmpty()) {
						result += ", ";
					}
					result += k.getName();
				}
			}
			return result;
		}
	});

	@UiField()
	@Editor.Path("assignee.realname")
	Label assignee;

	public TaskDetailsPopupPanel() {
		super(true, false); // Not modal so that mouseout events on the hover source element can cause this to hide.

		setWidget(uiBinder.createAndBindUi(this));
		driver.initialize(this);
		setStyleName("tasks");
		CommonGinjector.get.instance().getEventBus()
				.addHandler(AppScrollEvent.TYPE, new AppScrollEvent.AppScrollEventHandler() {
					@Override
					public void onScroll() {
						hide();
					}
				});
		CommonGinjector.get.instance().getEventBus().addHandler(PlaceChangeRequestEvent.TYPE, this);
		CommonGinjector.get.instance().getEventBus().addHandler(PlaceChangeEvent.TYPE, this);
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> stringValueChangeEvent) {
				hide();
			}
		});
		super.addDomHandler(new MouseOutHandler() {

			@Override
			public void onMouseOut(MouseOutEvent event) {
				hide();
			}
		}, MouseOutEvent.getType());
		super.addDomHandler(new ScrollHandler() {

			@Override
			public void onScroll(ScrollEvent event) {
				hide();
			}
		}, ScrollEvent.getType());
		super.addDomHandler(new MouseWheelHandler() {

			@Override
			public void onMouseWheel(MouseWheelEvent event) {
				hide();
			}
		}, MouseWheelEvent.getType());
	}

	@Override
	public void onPlaceChangeRequest(PlaceChangeRequestEvent event) {
		hide();
	}

	@Override
	public void onPlaceChange(PlaceChangeEvent event) {
		hide();
	}

	public void setTask(Task task) {
		driver.edit(task);

		String urlString = task.getUrl();
		if (urlString.indexOf("#") >= 0) {
			// If we have a hash, only keep the part after it.
			urlString = urlString.substring(urlString.indexOf("#"));
		}

		taskAnchorElement.setHref(urlString);
		commentsAnchor.setText(task.getComments().size() + " comments");
		commentsAnchor.setHref(urlString);

		UIObject.setVisible(resolvedStatusIconSpanElement, !task.getStatus().isOpen());

		String desc = task.getDescription();
		if (desc.length() > MAX_DESCRIPTION_LEN) {
			desc = desc.substring(0, MAX_DESCRIPTION_LEN);
			moreDescriptionAnchor.setVisible(true);
		} else {
			moreDescriptionAnchor.setVisible(false);
		}
		description.setText(desc);

		priorityDivElement.setClassName("priority left");
		switch (task.getPriority().getId()) {
		case 1:
			priorityDivElement.addClassName("five");
			break;
		case 2:
			priorityDivElement.addClassName("four");
			break;
		case 3:
		default:
			priorityDivElement.addClassName("three");
			break;
		case 4:
			priorityDivElement.addClassName("two");
			break;
		case 5:
			priorityDivElement.addClassName("one");
			break;
		}

		severityDivElement.setClassName("severity right");
		switch (task.getSeverity().getId()) {
		case 1:
		case 2:
			severityDivElement.addClassName("five");
			break;
		case 3:
			severityDivElement.addClassName("four");
			break;
		case 4:
		default:
			severityDivElement.addClassName("three");
			break;
		case 5:
			severityDivElement.addClassName("two");
			break;
		case 6:
			severityDivElement.addClassName("two");
			break;
		}

	}

	public void showAt(final int targetAbsoluteLeft, final int targetAbsoluteTop, final int targetOffsetWidth,
			final int targetOffsetHeight) {
		setPopupPositionAndShow(new PopupPanel.PositionCallback() {
			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {
				// There doesn't appear to be a reliable way to get the width and height of this popup from the element
				// itself, so I have to resort to hardcoded values (spent a couple of hours trying to get the right
				// values here - popup.getWidget().getElement().getClientHeight() returned the correct height, but the
				// width was way off
				int popupWidth = 297;
				int popupHeight = 350;
				int anchorRight = targetAbsoluteLeft + targetOffsetWidth;
				int anchorBottom = targetAbsoluteTop + targetOffsetHeight;
				int windowRight = Window.getClientWidth() + Window.getScrollLeft();
				int windowBottom = Window.getScrollTop() + Window.getClientHeight();

				// By default, set our left and top to be just below the Anchor's bottom right corner.
				int popupLeft = anchorRight;
				int popupTop = anchorBottom;

				if ((popupLeft + popupWidth) > windowRight) {
					// If there's not enough space to the right, then make sure our popup is touching the
					// right edge of the screen
					popupLeft = windowRight - popupWidth;
				}

				if ((popupTop + popupHeight) > windowBottom) {
					// If there's not enough space at the bottom, then make sure our popup is touching the
					// bottom edge of the screen
					popupTop = windowBottom - popupHeight;
				}
				setPopupPosition(popupLeft, popupTop);
			}
		});
	}
}
