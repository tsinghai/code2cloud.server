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
package com.tasktop.c2c.server.tasks.client.widgets.wiki;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.tasks.client.TaskResources;
import com.tasktop.c2c.server.tasks.shared.action.RenderWikiTextAction;
import com.tasktop.c2c.server.tasks.shared.action.RenderWikiTextResult;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class EditWikiPanel extends Composite implements HasValue<String>, IsEditor<Editor<String>> {

	interface Binder extends UiBinder<Widget, EditWikiPanel> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	private TextArea text = new TextArea();
	private WikiHTMLPanel html = new WikiHTMLPanel();

	@UiField(provided = true)
	protected ToggleButton toggleButton = new ToggleButton(new Image(TaskResources.resources.pencilIcon()));
	@UiField(provided = true)
	protected Image busyImage = new Image(ProfileGinjector.get.instance().getAppResources().throbber());
	@UiField
	protected HasOneWidget panel;
	@UiField
	protected Anchor helpAnchor;

	public EditWikiPanel() {
		TaskResources.resources.style().ensureInjected(); // TODO put in single place.
		initWidget(uiBinder.createAndBindUi(this));
		panel.setWidget(text);
		html.setStyleName("wikiContent");
		text.setStyleName("textarea");
		toggleButton.setValue(true);
		busyImage.setVisible(false);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return text.addValueChangeHandler(handler);
	}

	@Override
	public String getValue() {
		return text.getValue();
	}

	@Override
	public void setValue(String value) {
		setValue(value, true);
	}

	public void setEditMode() {
		toggleButton.setValue(true);
		panel.setWidget(text);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		text.setValue(value, fireEvents);
	}

	public void setRenderedValue(String html) {
		boolean isEditing = false;
		toggleButton.setValue(isEditing);
		helpAnchor.setVisible(isEditing);
		displayRenderedHtml(html);
	}

	protected String projectId;

	@UiHandler("toggleButton")
	protected void onToggleButton(ClickEvent e) {

		boolean isEditing = toggleButton.getValue();
		if (isEditing) {
			panel.setWidget(text);
		} else {
			preRender();
			renderWikiPreview(text.getValue());
		}
		helpAnchor.setVisible(isEditing);
	}

	public void addToggleListener(ValueChangeHandler<Boolean> handler) {
		toggleButton.addValueChangeHandler(handler);
	}

	protected void preRender() {
		toggleButton.setVisible(false);
		busyImage.setVisible(true);
	}

	protected void postRender() {
		toggleButton.setVisible(true);
		busyImage.setVisible(false);
	}

	protected void renderWikiPreview(String wikiText) {
		preRender();
		ProfileGinjector.get
				.instance()
				.getDispatchService()
				.execute(new RenderWikiTextAction(projectId, wikiText),
						new AsyncCallbackSupport<RenderWikiTextResult>() {

							@Override
							protected void success(RenderWikiTextResult result) {
								displayRenderedHtml(result.get());

							}
						});
	}

	protected void displayRenderedHtml(String htmlString) {
		postRender();
		html.setWikiHTML(htmlString);

		if (text.getOffsetWidth() != 0) {
			html.setWidth(text.getOffsetWidth() + "px");
		}
		panel.setWidget(html);
	}

	public TextArea getTextArea() {
		return text;
	}

	/**
	 * @param projectId
	 *            the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public void setVisibleLines(int lines) {
		text.setVisibleLines(lines);
	}

	private TakesValueEditor<String> editor = null;

	@Override
	public Editor<String> asEditor() {
		if (editor == null) {
			editor = TakesValueEditor.of(this);
		}
		return editor;
	}

	@UiHandler("helpAnchor")
	protected void onHelp(ClickEvent e) {
		WikiCheatSheetPopup.getInstance().center();
	}

}
