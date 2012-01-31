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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.common.web.client.widgets.DynamicFormPanel;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.MultiValueChooser;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.PersonCompositeFactory;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.PersonSuggestOracle;
import com.tasktop.c2c.server.profile.web.client.presenter.person.ProjectPersonService;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.keywords.KeywordCompositeFactory;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.keywords.KeywordSuggestOracle;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.KeywordSuggestService;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;

public class TaskSearchView extends AbstractComposite implements TaskSearchDisplay {
	interface TaskSearchViewUiBinder extends UiBinder<Widget, TaskSearchView> {
	}

	private static TaskSearchViewUiBinder uiBinder = GWT.create(TaskSearchViewUiBinder.class);

	protected ProjectPersonService personService;
	protected KeywordSuggestService keywordService;
	private RepositoryConfiguration repositoryConfiguration;

	@UiField
	public Panel detailsContainer;
	@UiField
	public Button searchButton;
	@UiField
	public Button searchButton2;
	@UiField
	Label pageLabel;
	@UiField
	public Button saveButton;
	@UiField
	public Button cancelButton;
	@UiField
	public TextBox summaryDescriptionComment;
	@UiField
	public ListBox iteration;
	@UiField
	public ListBox type;
	@UiField
	public ListBox product;
	@UiField
	public ListBox severity;
	@UiField
	public ListBox milestone;
	@UiField
	public ListBox component;
	@UiField
	public ListBox priority;
	@UiField
	Panel tagsPanel;
	@UiField
	Label tagsLabel;
	@UiField
	public ListBox status;
	@UiField
	public ListBox resolution;
	@UiField
	public SimpleCheckBox creator;
	@UiField
	public SimpleCheckBox assignee;
	@UiField
	public SimpleCheckBox commenter;
	@UiField
	public SimpleCheckBox cc;
	@UiField
	public Panel peoplePanel;
	@UiField
	public Label nameLabel;
	@UiField
	public ListBox dateType;
	@UiField
	public DateBox dateStart;
	@UiField
	public DateBox dateEnd;

	@UiField
	public SimpleCheckBox summary;

	@UiField
	public SimpleCheckBox description;

	private Map<FieldDescriptor, Widget> fieldDescriptorToWidget = new LinkedHashMap<FieldDescriptor, Widget>();

	protected Person self;

	public TaskSearchView() {
		initWidget(uiBinder.createAndBindUi(this));
		hookDefaultButton(searchButton);

		product.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				onProductChanged();
			}
		});
		setEditMode(false);
	}

	void setEditMode(boolean edit) {
		searchButton.setVisible(!edit);
		searchButton2.setVisible(!edit);
		saveButton.setVisible(edit);
	}

	@Override
	public void setSelf(Person self) {
		this.self = self;
	}

	@Override
	public Widget getWidget() {
		return this;
	}

	@Override
	public void setSearchClickHandler(ClickHandler handler) {
		searchButton.addClickHandler(handler);
		searchButton2.addClickHandler(handler);
	}

	@Override
	public void setPersonService(ProjectPersonService personService) {
		this.personService = personService;
	}

	@Override
	public void setKeywordService(KeywordSuggestService keywordService) {
		this.keywordService = keywordService;
	}

	@Override
	public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
		this.repositoryConfiguration = repositoryConfiguration;
	}

	private List<String> computeIterationValues(List<Iteration> iterations) {
		List<String> result = new ArrayList<String>();
		for (Iteration iteration : iterations) {
			result.add(iteration.getValue());
		}
		return result;
	}

	@Override
	public void createUi() {
		detailsContainer.clear();

		WidgetUtil.configureReferenceValues(severity, repositoryConfiguration.getSeverities());
		WidgetUtil.configureDomainValues(product, repositoryConfiguration.getProducts(), true);
		WidgetUtil.configureDomainValues(component, repositoryConfiguration.getComponents(), true);
		WidgetUtil.configureReferenceValues(milestone, repositoryConfiguration.getMilestones());
		WidgetUtil.configureReferenceValues(priority, repositoryConfiguration.getPriorities());
		WidgetUtil.configureReferenceValues(status, repositoryConfiguration.getStatuses());
		WidgetUtil.configureReferenceValues(resolution, repositoryConfiguration.getResolutions());
		WidgetUtil.configureStringValues(type, repositoryConfiguration.getTaskTypes());
		WidgetUtil.configureStringValues(iteration, computeIterationValues(repositoryConfiguration.getIterations()));
		adjustVisibleItemCount(severity);
		adjustVisibleItemCount(product);
		adjustVisibleItemCount(component);
		adjustVisibleItemCount(milestone);
		adjustVisibleItemCount(priority);
		adjustVisibleItemCount(status);
		adjustVisibleItemCount(resolution);
		adjustVisibleItemCount(type);
		adjustVisibleItemCount(iteration);

		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.SEVERITY_FIELD, "Severity",
				FieldType.SINGLE_SELECT), severity);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.PRODUCT_NAME_FIELD, "Product",
				FieldType.SINGLE_SELECT), product);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.COMPONENT_NAME_FIELD, "Component",
				FieldType.SINGLE_SELECT), component);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.MILESTONE_FIELD, "Release",
				FieldType.SINGLE_SELECT), milestone);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.PRIORITY_FIELD, "Priority",
				FieldType.SINGLE_SELECT), priority);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.STATUS_FIELD, "Status",
				FieldType.SINGLE_SELECT), status);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.RESOLUTION_FIELD, "Resolution",
				FieldType.SINGLE_SELECT), resolution);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.TASK_TYPE_FIELD, "Type",
				FieldType.SINGLE_SELECT), type);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.ITERATION_FIELD, "Iteration",
				FieldType.SINGLE_SELECT), iteration);
		fieldDescriptorToWidget.put(new FieldDescriptor("summaryDescriptionComments", "Summary", FieldType.TEXT),
				summaryDescriptionComment);
		fieldDescriptorToWidget.put(
				new FieldDescriptor(TaskFieldConstants.ASSIGNEE_FIELD, "Owner", FieldType.CHECKBOX), assignee);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.REPORTER_FIELD, "Creator",
				FieldType.CHECKBOX), creator);
		fieldDescriptorToWidget
				.put(new FieldDescriptor(TaskFieldConstants.WATCHER_FIELD, "CC", FieldType.CHECKBOX), cc);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.COMMENT_AUTHOR_FIELD, "Commenter",
				FieldType.CHECKBOX), commenter);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.SUMMARY_FIELD, "Summary Checkbox",
				FieldType.CHECKBOX), summary);
		fieldDescriptorToWidget.put(new FieldDescriptor(TaskFieldConstants.DESCRIPTION_FIELD, "Description Checkbox",
				FieldType.CHECKBOX), description);

		MultiValueChooser<Person> personChooser = new MultiValueChooser<Person>(new PersonSuggestOracle(personService));
		personChooser.setSelf(self);
		personChooser.addStyleName("task-form");
		personChooser.addLabel(nameLabel);

		peoplePanel.clear();
		personChooser.setValueCompositeFactory(new PersonCompositeFactory());
		peoplePanel.add(personChooser);
		fieldDescriptorToWidget.put(new FieldDescriptor("person", "Person", FieldType.TEXT), personChooser);

		fieldDescriptorToWidget.put(new FieldDescriptor("dateType", "Date Type", FieldType.SINGLE_SELECT), dateType);
		fieldDescriptorToWidget.put(new FieldDescriptor("startDate", "Start Date", FieldType.TIMESTAMP), dateStart);
		fieldDescriptorToWidget.put(new FieldDescriptor("endDate", "End Date", FieldType.TIMESTAMP), dateEnd);

		tagsPanel.clear();
		MultiValueChooser<Keyword> tagChooser = new MultiValueChooser<Keyword>(new KeywordSuggestOracle(keywordService,
				false));
		tagChooser.setValueCompositeFactory(new KeywordCompositeFactory());
		tagChooser.addStyleName("task-form");
		tagChooser.addLabel(tagsLabel);
		tagsPanel.add(tagChooser);

		fieldDescriptorToWidget.put(new FieldDescriptor("tags", "Tags", FieldType.TEXT), tagChooser);

		DynamicFormPanel detailsPanel = new DynamicFormPanel();

		List<FieldDescriptor> customFields = repositoryConfiguration.getCustomFields();
		if (customFields != null && !customFields.isEmpty()) {
			int offset = 0;
			final int columnCount = 3;

			for (FieldDescriptor field : customFields) {
				if (field.getName().equals("task_relations")) {
					continue;
				}

				if (++offset > columnCount) {
					offset = 1;
					detailsPanel.newLine();
				}
				addField(detailsPanel, field);

			}
		}
		detailsContainer.add(detailsPanel);

		DefaultFormat defaultFormat = new DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
		dateStart.setFormat(defaultFormat);
		dateEnd.setFormat(defaultFormat);
	}

	private void adjustVisibleItemCount(ListBox listBox) {
		listBox.setVisibleItemCount(5);
	}

	private void addField(DynamicFormPanel detailsPanel, FieldDescriptor field) {
		Widget widget = createWidget(field, "");
		fieldDescriptorToWidget.put(field, widget);
		String labelText = field.getDescription();
		detailsPanel.add(new Label(labelText), widget, true);
	}

	private Widget createWidget(FieldDescriptor field, String fieldValue) {
		switch (field.getFieldType()) {
		case SINGLE_SELECT: {
			ListBox listBox = new ListBox(true);
			int index = 0;
			int selectedIndex = -1;
			for (String value : field.getValueStrings()) {
				listBox.addItem(value, value);
				if (value.equals(fieldValue)) {
					selectedIndex = index;
				}
				++index;
			}
			if (selectedIndex != -1) {
				listBox.setSelectedIndex(selectedIndex);
			}
			adjustVisibleItemCount(listBox);
			return listBox;
		}
		case TIMESTAMP: {
			DateBox dateBox = new DateBox();
			return dateBox;
		}
		case TASK_REFERENCE:
		case TEXT:
		case LONG_TEXT: {
			TextBox textBox = new TextBox();
			textBox.setText(fieldValue);
			return textBox;
		}
		}
		return new Label(fieldValue);
	}

	@Override
	public Map<FieldDescriptor, Widget> getFieldDescriptorToWidget() {
		return fieldDescriptorToWidget;
	}

	public void onProductChanged() {
		List<Product> selectedProducts = new ArrayList<Product>();
		for (int i = 0; i < product.getItemCount(); i++) {
			if (product.isItemSelected(i)) {
				String productName = product.getItemText(i);
				for (Product p : repositoryConfiguration.getProducts()) {
					if (p.getName().equals(productName)) {
						selectedProducts.add(p);
					}
				}
			}
		}

		List<Component> availableComponents = new ArrayList<Component>();
		List<Milestone> availableMilestones = new ArrayList<Milestone>();

		if (selectedProducts.isEmpty()) {
			availableComponents.addAll(repositoryConfiguration.getComponents());
			availableMilestones.addAll(repositoryConfiguration.getMilestones());
		} else {
			for (Product p : selectedProducts) {
				availableComponents.addAll(repositoryConfiguration.getComponents(p));
				availableMilestones.addAll(repositoryConfiguration.getMilestones(p));
			}
		}

		WidgetUtil.configureDomainValues(component, availableComponents, true);
		WidgetUtil.configureReferenceValues(milestone, availableMilestones);

	}

	@Override
	public void setEditQuery(SavedTaskQuery editQuery) {
		setEditMode(editQuery != null);
		pageLabel.setText(editQuery == null ? "Advanced Search" : "Edit Saved Search");
	}

	@Override
	public void setSaveClickHandler(ClickHandler handler) {
		saveButton.addClickHandler(handler);
	}

	@Override
	public void setCancelClickHandler(ClickHandler handler) {
		cancelButton.addClickHandler(handler);
	}

}
