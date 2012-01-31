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

import static com.tasktop.c2c.server.tasks.client.widgets.presenter.person.PersonUtil.toPerson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.adapters.EditorSource;
import com.google.gwt.editor.client.adapters.ListEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.testing.PassthroughRenderer;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.common.web.client.view.CompositeClickHandlers;
import com.tasktop.c2c.server.common.web.client.widgets.DynamicFormPanel;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.MultiValueChooser;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.SingleValueChooser;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.StringSuggestion;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.StringValueCompositeFactory;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.PersonSuggestOracle;
import com.tasktop.c2c.server.common.web.client.widgets.time.TimePeriodBox;
import com.tasktop.c2c.server.profile.web.client.presenter.person.ProjectPersonService;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.keywords.KeywordCompositeFactory;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.keywords.KeywordSuggestOracle;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.KeywordSuggestService;
import com.tasktop.c2c.server.tasks.client.widgets.wiki.EditWikiPanel;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

public class AbstractEditTaskView extends AbstractComposite implements AbstractEditTaskDisplay, Editor<Task> {

	@UiField
	protected TextBox shortDescription;
	@UiField
	protected EditWikiPanel description;
	@UiField(provided = true)
	protected ValueListBox<String> taskType = new ValueListBox<String>(PassthroughRenderer.instance());
	@UiField(provided = true)
	protected ValueListBox<Priority> priority = new ValueListBox<Priority>(ReferenceValueRenderer.getInstance());
	@UiField(provided = true)
	protected ValueListBox<TaskSeverity> severity = new ValueListBox<TaskSeverity>(ReferenceValueRenderer.getInstance());
	@UiField
	protected Panel detailsContainer;
	@UiField
	protected Button saveTaskButton;
	@UiField
	protected Button cancelButton;
	@UiField
	protected Button saveTaskButton2;
	@UiField
	protected Button cancelButton2;
	@UiField
	@Path("")
	protected StatusEditorView statusEditor;

	@UiField(provided = true)
	protected MultiValueChooser<Keyword> keywords = new MultiValueChooser<Keyword>(new KeywordSuggestOracle(null));
	@UiField(provided = true)
	protected MultipleTaskProfileChooser watchers = new MultipleTaskProfileChooser(new PersonSuggestOracle(null));
	@UiField(provided = true)
	protected SingleTaskProfileChooser assignee = new SingleTaskProfileChooser(new PersonSuggestOracle(null));

	protected SingleValueChooser<String> foundInRelease = new SingleValueChooser<String>(new SuggestOracle() {

		@Override
		public void requestSuggestions(Request request, Callback callback) {
			List<Suggestion> suggestions = new ArrayList<Suggestion>();

			boolean foundExact = false;
			for (String release : product.getValue().getReleaseTags()) {
				if (release.equals(request.getQuery())) {
					foundExact = true;
				}

				if (request.getQuery() == null || release.startsWith(request.getQuery())) {
					suggestions.add(new StringSuggestion(release));
				}
			}

			if (!foundExact) {
				suggestions.add(new StringSuggestion(request.getQuery()));
			}

			callback.onSuggestionsReady(request, new Response(suggestions));
		}

		@Override
		public void requestDefaultSuggestions(Request request, Callback callback) {
			requestSuggestions(request, callback);
		}
	});

	@UiField
	@Ignore
	protected Label tagsLabel;
	@UiField
	@Ignore
	protected Label taskOwnerLabel;
	@UiField
	@Ignore
	protected Label watcherLabel;
	@Ignore
	protected Label foundInReleaseLabel = new Label("Found In");

	protected ValueListBox<Product> product = new ValueListBox<Product>(new AbstractRenderer<Product>() {

		@Override
		public String render(Product object) {
			return object.getName();
		}
	});

	protected ValueListBox<Milestone> milestone = new ValueListBox<Milestone>(ReferenceValueRenderer.getInstance());
	protected ValueListBox<Iteration> iteration = new ValueListBox<Iteration>(new AbstractRenderer<Iteration>() {

		@Override
		public String render(Iteration value) {
			return value.getValue();
		}
	});

	protected ValueListBox<Component> component = new ValueListBox<Component>(new AbstractRenderer<Component>() {
		@Override
		public String render(Component object) {
			return object.getName();
		}
	});
	protected TimePeriodBox estimatedTime = TimePeriodBox.getHourBoxWithDefaultsEntryToDays();

	private static class CustomFieldSource extends EditorSource<CustomFieldEditor> {
		@Override
		public CustomFieldEditor create(int index) {
			return new CustomFieldEditor();

		}

	}

	protected DateBox deadline = new DateBox(new DatePicker(), new Date(), new DateBox.DefaultFormat(
			DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM)));

	protected CustomFieldSource customFieldSource = new CustomFieldSource();

	interface Driver extends SimpleBeanEditorDriver<List<CustomField>, ListEditor<CustomField, CustomFieldEditor>> {
	}

	private Driver customFieldDriver = GWT.create(Driver.class);
	@Ignore
	protected ListEditor<CustomField, CustomFieldEditor> customFieldEditor = ListEditor.of(customFieldSource);
	private List<FieldDescriptor> customFieldDescriptors;

	protected Task task;
	protected Person self;
	protected TaskMessages taskMessages = GWT.create(TaskMessages.class);
	private boolean isEditing = false;

	public AbstractEditTaskView() {
		foundInRelease.addLabel(foundInReleaseLabel);
		foundInRelease.setStyleName("text long date left"); // FIXME styling
		foundInRelease.setValueCompositeFactory(new StringValueCompositeFactory());
		keywords.setValueCompositeFactory(new KeywordCompositeFactory());
	}

	@Override
	public void initWidget(Widget w) {
		super.initWidget(w);
		keywords.addLabel(tagsLabel);
		watchers.addLabel(watcherLabel);
		assignee.addLabel(taskOwnerLabel);
		new CompositeClickHandlers(cancelButton, cancelButton2).addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				isEditing = false;
			}
		});
		statusEditor.setStylesForInline();
	}

	@Override
	public Widget getWidget() {
		return this;
	}

	@Override
	public void setSelf(Person self) {
		this.self = self;
		watchers.setSelf(self);
		assignee.setSelf(self);
	}

	@Override
	public void setKeywordService(KeywordSuggestService keywordService) {
		((KeywordSuggestOracle) keywords.getSuggestBox().getSuggestOracle()).setSuggestionService(keywordService);
	}

	@Override
	public void setPersonService(ProjectPersonService personService) {
		watchers.setSuggestionService(personService);
		assignee.setSuggestionService(personService);
	}

	@Override
	public HasClickHandlers getSaveClickHandlers() {
		return new CompositeClickHandlers(saveTaskButton, saveTaskButton2);
	}

	@Override
	public HasClickHandlers getCancelClickHandlers() {
		return new CompositeClickHandlers(cancelButton, cancelButton2);
	}

	@Override
	public void addProductChangeHandler(ValueChangeHandler<Product> handler) {
		product.addValueChangeHandler(handler);
	}

	@Override
	public void addComponentChangeHandler(ValueChangeHandler<Component> handler) {
		component.addValueChangeHandler(handler);
	}

	@Override
	public void addOwnerChangeHandler(ValueChangeHandler<Person> handler) {
		assignee.addValueChangeHandler(handler);
	}

	@Override
	public void setComponents(List<Component> components) {
		configureValues(component, components);
	}

	@Override
	public void setMilestones(List<Milestone> milestones) {
		configureValues(milestone, milestones);
	}

	static <T> void configureValues(ValueListBox<T> box, List<T> values) {
		if (values != null && !values.isEmpty()) {
			box.setValue(values.get(0));
			box.setAcceptableValues(values);
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Task> handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Task getValue() {
		populateTask(task);
		return task;
	}

	@Override
	public void setValue(Task value) {
		setTask(value);
	}

	@Override
	public void setValue(Task value, boolean fireEvents) {
		setValue(value);
	}

	protected void setTask(Task task) {
		this.task = task;

		Person reporter = task.getId() == null ? self : toPerson(task.getReporter());

		detailsContainer.clear();

		DynamicFormPanel detailsPanel;
		int offset = 0;
		int columnCount = 3;
		detailsPanel = new DynamicFormPanel();
		detailsPanel.add("Product", product);
		detailsPanel.add("Component", component);
		detailsPanel.add("Release", milestone);
		detailsPanel.newLine();
		detailsPanel.add(foundInReleaseLabel, foundInRelease);
		detailsPanel.add("Iteration", iteration);
		detailsPanel.newLine();
		detailsPanel.add("Due Date", deadline);
		detailsPanel.add("Estimate", estimatedTime);
		detailsPanel.newLine();

		detailsPanel.newLine();

		if (shouldEditCustomFields()) {
			List<CustomField> customFields = new ArrayList<CustomField>(customFieldDescriptors.size());
			for (FieldDescriptor field : customFieldDescriptors) {
				if (field.getName().equals("task_relations")) {
					continue;
				}

				String fieldValue = task.getCustomFields().get(field.getName());
				customFields.add(new CustomField(field, fieldValue));

			}
			customFieldDriver.initialize(customFieldEditor);
			customFieldDriver.edit(customFields);

			for (CustomFieldEditor editor : customFieldEditor.getEditors()) {
				FieldDescriptor fieldDescriptor = editor.getFieldDescriptor();
				if (fieldDescriptor.getFieldType() == FieldType.LONG_TEXT) {
					detailsPanel.newLine();
					detailsPanel.addLabel(columnCount * 2, new Label(fieldDescriptor.getDescription()));
					detailsPanel.newLine();
					detailsPanel.add(columnCount * 2, editor.getWidget());
					detailsPanel.newLine();
					offset = 0;
				} else {
					if (++offset > columnCount) {
						offset = 0;
						detailsPanel.newLine();
					}
					detailsPanel.add(fieldDescriptor.getDescription(), editor.getWidget());
				}

			}
		}
		detailsContainer.add(detailsPanel);

		watchers.setOrigin(reporter);
		watchers.setUnremovableValues(reporter == null ? null : Collections.singletonList(reporter));
		assignee.setOrigin(reporter);
		isEditing = true;
		description.setEditMode();
	}

	protected void populateTask(Task task) {
		if (shouldEditCustomFields()) {
			List<CustomField> customFields = customFieldDriver.flush();
			task.setCustomFields(new HashMap<String, String>());
			for (CustomField cf : customFields) {
				task.getCustomFields().put(cf.getFieldDescriptor().getName(), cf.getValue());
			}
		}

		isEditing = false;
	}

	@Override
	public void setSelectedMilestone(Milestone selected) {
		milestone.setValue(selected);
	}

	@Override
	public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
		statusEditor.setRepositoryConfiguration(repositoryConfiguration);
		configureValues(priority, repositoryConfiguration.getPriorities());
		configureValues(taskType, repositoryConfiguration.getTaskTypes());
		configureValues(iteration, repositoryConfiguration.getActiveIterations());
		configureValues(product, repositoryConfiguration.getProducts());
		configureValues(severity, repositoryConfiguration.getSeverities());
		this.customFieldDescriptors = getRelevantCustomFields(repositoryConfiguration.getCustomFields());
	}

	protected List<FieldDescriptor> getRelevantCustomFields(List<FieldDescriptor> fieldDescriptors) {
		ArrayList<FieldDescriptor> customFields = new ArrayList<FieldDescriptor>(fieldDescriptors.size());
		for (FieldDescriptor field : fieldDescriptors) {
			if (!field.isObsolete()) {
				customFields.add(field);
			}
		}
		return customFields;
	}

	@Override
	public void clearFoundInReleases() {
		foundInRelease.setValue(null);
	}

	@Override
	public final boolean isDirty() {
		if (!isEditing) {
			return false;
		}
		return areEditorsDirty();
	}

	private boolean shouldEditCustomFields() {
		return customFieldDescriptors != null && !customFieldDescriptors.isEmpty();

	}

	protected boolean areEditorsDirty() {
		if (shouldEditCustomFields()) {
			return customFieldDriver.isDirty();
		} else {
			return false;
		}
	}

	@Override
	public void setAssignee(TaskUserProfile toSet) {
		assignee.setValue(toSet);
	}

	@Override
	public void setProjectIdentifier(String projectIdentifier) {
		description.setProjectId(projectIdentifier);
	}

}
