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
package com.tasktop.c2c.server.common.web.client.widgets.chooser;

import java.util.List;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.common.web.client.widgets.HorizontalPanel;

/**
 * A generic oracle based chooser widget.
 * 
 * See {@link MultiValueChooser} and {@link SingleValueChooser} for variants.
 * 
 * @author David Green (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 * @param <T>
 */
public abstract class AbstractValueChooser<T> extends Composite {
	private List<T> originalValues;
	private List<T> unremovableValues;
	private T self;
	private T origin;

	private Panel valuePanel;
	private SuggestBox suggestBox;
	private ValueCompositeFactory<T> valueCompositeFactory;

	public static SuggestBox createSuggestBox(SuggestOracle oracle) {
		return createSuggestBox(oracle, true, true);
	}

	public static SuggestBox createSuggestBox(SuggestOracle oracle, final boolean hideOnScroll,
			final boolean hideOnResize) {
		final SuggestBoxProxy proxy = new SuggestBoxProxy();
		SuggestBox suggestBox = new SuggestBox(oracle, new TextBox(), new SuggestBox.DefaultSuggestionDisplay() {
			@Override
			protected PopupPanel createPopup() {
				final PopupPanel popupPanel = super.createPopup();
				CommonGinjector.get.instance().getEventBus().addHandler(ScrollEvent.getType(), new ScrollHandler() {
					@Override
					public void onScroll(ScrollEvent event) {
						if (popupPanel.isShowing()) {
							if (proxy.getSuggestBox() != null && !hideOnScroll) {
								popupPanel.showRelativeTo(proxy.getSuggestBox());
							} else {
								popupPanel.hide();
							}
						}
					}
				});
				Window.addResizeHandler(new ResizeHandler() {
					@Override
					public void onResize(ResizeEvent event) {
						if (popupPanel.isShowing()) {
							if (proxy.getSuggestBox() != null && !hideOnResize) {
								popupPanel.showRelativeTo(proxy.getSuggestBox());
							} else {
								popupPanel.hide();
							}
						}
					}
				});
				return popupPanel;
			}
		});
		proxy.setSuggestBox(suggestBox);
		return suggestBox;
	}

	public static class SuggestBoxProxy {

		private SuggestBox suggestBox;

		public SuggestBox getSuggestBox() {
			return suggestBox;
		}

		public void setSuggestBox(SuggestBox suggestBox) {
			this.suggestBox = suggestBox;
		}
	}

	protected AbstractValueChooser(SuggestOracle oracle) {
		this(createSuggestBox(oracle));
	}

	public AbstractValueChooser(SuggestBox suggestBox) {
		com.google.gwt.user.client.ui.HorizontalPanel contentPanel = new com.google.gwt.user.client.ui.HorizontalPanel();
		contentPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		contentPanel.addStyleName("peopleChooser");

		this.suggestBox = suggestBox;
		SelectionHandler<Suggestion> selectionHandler = new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				ValueSuggestion<T> suggestion = (ValueSuggestion<T>) event.getSelectedItem();
				if (!getValues().contains(suggestion.getValue())) {
					addValue(suggestion.getValue());
				} else {
					AbstractValueChooser.this.suggestBox.setValue("", false);
				}
			}

		};
		suggestBox.addSelectionHandler(selectionHandler);

		contentPanel.add(suggestBox);

		valuePanel = new HorizontalPanel();
		contentPanel.add(valuePanel);

		initWidget(contentPanel);
	}

	protected abstract void addValue(T value);

	public abstract void removeValue(Object value);

	protected abstract List<T> getValues();

	protected abstract void fireValueChanged();

	/**
	 * The original values, which is used to indicate values that are added.
	 */
	public List<T> getOriginalValues() {
		return originalValues;
	}

	public void setOriginalValues(List<T> originalValues) {
		this.originalValues = originalValues;
		updateUi();
	}

	/**
	 * The values that cannot be removed from the selection
	 */
	public List<T> getUnremovableValues() {
		return unremovableValues;
	}

	public void setUnremovableValues(List<T> unremovableValues) {
		this.unremovableValues = unremovableValues;
	}

	/**
	 * The person representing the current user, affects display. Useful for person values.
	 */
	public T getSelf() {
		return self;
	}

	public void setSelf(T self) {
		this.self = self;
	}

	/**
	 * The value representing the "origin", affects styled display and prevents removal.
	 */
	public T getOrigin() {
		return origin;
	}

	public void setOrigin(T origin) {
		this.origin = origin;
	}

	public void addLabel(Label label) {
		label.addStyleName("peopleChooserLabel");
		label.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				suggestBox.showSuggestionList();
			}
		});
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		updateUi();
	}

	protected void updateUi() {
		if (!isAttached()) {
			return;
		}
		valuePanel.clear();
		suggestBox.setValue("", false);
		List<T> values = getValues();
		if (values != null) {
			if (origin != null && values.contains(origin)) {
				AbstractValueComposite<T> valueComposite = getValueCompositeFactory().getComposite(this, origin);
				valuePanel.add(valueComposite);
			}
			for (T value : values) {
				if (origin != null && origin.equals(value)) {
					continue;
				}
				AbstractValueComposite<T> valueComposite = getValueCompositeFactory().getComposite(this, value);
				valuePanel.add(valueComposite);
			}
		}
	}

	public String getSuggestBoxValue() {
		return suggestBox.getValue();
	}

	public ValueCompositeFactory<T> getValueCompositeFactory() {
		return valueCompositeFactory;
	}

	public void setValueCompositeFactory(ValueCompositeFactory<T> valueCompositeFactory) {
		this.valueCompositeFactory = valueCompositeFactory;
	}

	public SuggestBox getSuggestBox() {
		return suggestBox;
	}
}
