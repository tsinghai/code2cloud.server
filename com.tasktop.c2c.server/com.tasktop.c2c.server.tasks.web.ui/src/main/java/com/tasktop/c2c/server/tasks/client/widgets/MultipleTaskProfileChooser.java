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

import java.util.List;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.MultiValueChooser;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.PersonCompositeFactory;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.PersonSuggestOracle;
import com.tasktop.c2c.server.profile.web.client.presenter.person.ProjectPersonService;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.PersonUtil;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

public class MultipleTaskProfileChooser implements IsEditor<Editor<List<TaskUserProfile>>>, IsWidget,
		TakesValue<List<TaskUserProfile>> {

	private MultiValueChooser<Person> wrapped;

	public MultipleTaskProfileChooser(SuggestOracle suggest) {
		wrapped = new MultiValueChooser<Person>(suggest);
		wrapped.setValueCompositeFactory(new PersonCompositeFactory());
	}

	@Override
	public Widget asWidget() {
		return wrapped;
	}

	private Editor<List<TaskUserProfile>> editor = null;

	@Override
	public Editor<List<TaskUserProfile>> asEditor() {
		if (editor == null) {
			editor = TakesValueEditor.of(this);
		}
		return editor;
	}

	@Override
	public void setValue(List<TaskUserProfile> values) {
		List<Person> p = PersonUtil.toPeople(values);
		wrapped.setValue(p);
		wrapped.setOriginalValues(p);
	}

	@Override
	public List<TaskUserProfile> getValue() {
		return PersonUtil.toTaskUserProfiles(wrapped.getValue());
	}

	/**
	 * @param taskOwnerLabel
	 */
	public void addLabel(Label taskOwnerLabel) {
		wrapped.addLabel(taskOwnerLabel);
	}

	/**
	 * @param self
	 */
	public void setSelf(Person self) {
		wrapped.setSelf(self);
	}

	/**
	 * @param personService
	 */
	public void setSuggestionService(ProjectPersonService personService) {
		((PersonSuggestOracle) wrapped.getSuggestBox().getSuggestOracle()).setSuggestionService(personService);

	}

	/**
	 * @param handler
	 * @return
	 */
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Person>> handler) {
		return wrapped.addValueChangeHandler(handler);

	}

	/**
	 * @param reporter
	 */
	public void setOrigin(Person origin) {
		wrapped.setOrigin(origin);
	}

	public void setStyleName(String style) {
		wrapped.setStyleName(style);
	}

	/**
	 * @param list
	 */
	public void setUnremovableValues(List<Person> unremovableValues) {
		wrapped.setUnremovableValues(unremovableValues);
	}

	public MultiValueChooser<Person> getWrappedChooser() {
		return wrapped;
	}

}
