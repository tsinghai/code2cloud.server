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
package com.tasktop.c2c.server.tasks.client.presenters;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.MultiValueChooser;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.profile.web.client.presenter.person.ProjectPersonService;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksPlace;
import com.tasktop.c2c.server.tasks.client.widgets.TaskSearchDisplay;
import com.tasktop.c2c.server.tasks.client.widgets.WidgetUtil;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.PersonUtil;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.KeywordSuggestService;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.shared.action.UpdateQueryAction;
import com.tasktop.c2c.server.tasks.shared.action.UpdateQueryResult;

public class TaskSearchPresenter extends AbstractTaskPresenter {

	private String projectIdentifier;
	private final TaskSearchDisplay taskSearchView;
	protected RepositoryConfiguration repositoryConfiguration;
	private SavedTaskQuery editQuery;

	public TaskSearchPresenter(TaskSearchDisplay taskSearchView) {
		super(taskSearchView.getWidget());
		this.taskSearchView = taskSearchView;
		taskSearchView.setSearchClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				doSearch();
			}
		});
		taskSearchView.setSaveClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				doUpdateQuery();
			}
		});
	}

	@Override
	protected void bind() {
		//
	}

	void setState(String projectIdentifier, RepositoryConfiguration repositoryConfiguration) {
		this.projectIdentifier = projectIdentifier;
		this.repositoryConfiguration = repositoryConfiguration;
		configure();
	}

	private void configure() {
		taskSearchView.setRepositoryConfiguration(repositoryConfiguration);
		taskSearchView.setSelf(getAppState().getSelf());
		ProjectPersonService personService = new ProjectPersonService(projectIdentifier);
		taskSearchView.setPersonService(personService);
		taskSearchView.setKeywordService(new KeywordSuggestService(projectIdentifier));

		taskSearchView.createUi();

		maybeSetEditQueryToView();
	}

	private void addPeopleCriteria(NaryCriteria criteria, String fieldName, MultiValueChooser<Person> chooser) {
		NaryCriteria people = new NaryCriteria();
		people.setOperator(Operator.OR);

		// No persons chosen yet, just use the value in the text box if not empty.
		if (chooser.getValues().size() < 1 && !chooser.getSuggestBoxValue().isEmpty()) {
			people.addSubCriteria(new ColumnCriteria(fieldName, Operator.EQUALS, querySafeValue(chooser
					.getSuggestBoxValue())));
		} else {
			for (Person person : chooser.getValues()) {
				people.addSubCriteria(new ColumnCriteria(fieldName, Operator.EQUALS, person.getIdentity()));
			}
		}
		if (people.hasSubCriteria()) {
			if (people.getSubCriteria().size() == 1) {
				criteria.addSubCriteria(people.getSubCriteria().get(0));
			} else {
				criteria.addSubCriteria(people);
			}
		}
	}

	private Criteria calculateCriteria() {
		NaryCriteria criteria = new NaryCriteria();
		criteria.setOperator(Operator.AND);

		Map<FieldDescriptor, Widget> descriptorToWidget = taskSearchView.getFieldDescriptorToWidget();
		@SuppressWarnings("unchecked")
		MultiValueChooser<Person> personChooser = (MultiValueChooser<Person>) descriptorToWidget
				.get(new FieldDescriptor("person"));

		@SuppressWarnings("unchecked")
		MultiValueChooser<Keyword> tagsChooser = (MultiValueChooser<Keyword>) descriptorToWidget
				.get(new FieldDescriptor("tags"));

		for (Entry<FieldDescriptor, Widget> entry : descriptorToWidget.entrySet()) {
			Widget widget = entry.getValue();
			if (widget instanceof MultiValueChooser<?>) {

				if (widget.equals(personChooser)) {
					NaryCriteria peopleCriteria = new NaryCriteria();
					peopleCriteria.setOperator(Operator.OR);

					SimpleCheckBox creatorBox = (SimpleCheckBox) descriptorToWidget.get(new FieldDescriptor(
							TaskFieldConstants.REPORTER_FIELD));
					if (creatorBox.getValue()) {
						addPeopleCriteria(peopleCriteria, TaskFieldConstants.REPORTER_FIELD, personChooser);
					}

					SimpleCheckBox ownerBox = ((SimpleCheckBox) descriptorToWidget.get(new FieldDescriptor(
							TaskFieldConstants.ASSIGNEE_FIELD)));
					if (ownerBox.getValue()) {
						addPeopleCriteria(peopleCriteria, TaskFieldConstants.ASSIGNEE_FIELD, personChooser);
					}

					SimpleCheckBox commenterBox = (SimpleCheckBox) descriptorToWidget.get(new FieldDescriptor(
							TaskFieldConstants.COMMENT_AUTHOR_FIELD));
					if (commenterBox.getValue()) {
						addPeopleCriteria(peopleCriteria, TaskFieldConstants.COMMENT_AUTHOR_FIELD, personChooser);
					}

					SimpleCheckBox watcherBox = (SimpleCheckBox) descriptorToWidget.get(new FieldDescriptor(
							TaskFieldConstants.WATCHER_FIELD));
					if (watcherBox.getValue()) {
						addPeopleCriteria(peopleCriteria, TaskFieldConstants.WATCHER_FIELD, personChooser);
					}

					if (peopleCriteria.hasSubCriteria()) {
						if (peopleCriteria.getSubCriteria().size() == 1) {
							criteria.addSubCriteria(peopleCriteria.getSubCriteria().get(0));
						} else {
							criteria.addSubCriteria(peopleCriteria);
						}
					}
				} else if (widget.equals(tagsChooser)) {
					NaryCriteria tagsCriteria = new NaryCriteria();
					tagsCriteria.setOperator(Operator.OR);
					for (Keyword k : tagsChooser.getValues()) {
						tagsCriteria.addSubCriteria(new ColumnCriteria(TaskFieldConstants.KEYWORDS_FIELD, k.getName()));
					}
					if (tagsCriteria.hasSubCriteria()) {
						if (tagsCriteria.getSubCriteria().size() == 1) {
							criteria.addSubCriteria(tagsCriteria.getSubCriteria().get(0));
						} else {
							criteria.addSubCriteria(tagsCriteria);
						}
					}
				} else {
					// other choosers
				}

			} else if (widget instanceof ListBox) {
				// don't include dateType in the queryString
				if (widget.equals(descriptorToWidget.get(new FieldDescriptor("dateType")))) {
					continue;
				}
				// all other ListBoxes
				ListBox listBox = (ListBox) widget;
				NaryCriteria values = new NaryCriteria();
				values.setOperator(Operator.OR);
				for (int index = 0; index < listBox.getItemCount(); ++index) {
					if (listBox.isItemSelected(index)) {
						String value = querySafeValue(listBox.getValue(index));
						values.addSubCriteria(new ColumnCriteria(entry.getKey().getName(), Operator.EQUALS, value));
					}
				}

				if (values.hasSubCriteria()) {
					if (values.getSubCriteria().size() == 1) {
						criteria.addSubCriteria(values.getSubCriteria().get(0));
					} else {
						criteria.addSubCriteria(values);
					}
				}

			} else if (widget instanceof TextBox) {
				TextBox summaryDescriptionCommentsTextBox = (TextBox) descriptorToWidget.get(new FieldDescriptor(
						"summaryDescriptionComments"));

				if (widget.equals(summaryDescriptionCommentsTextBox)) {
					String value = summaryDescriptionCommentsTextBox.getValue();
					if (value != null && value.trim().length() > 0) {
						value = value.trim();
						NaryCriteria summaryDescSearch = new NaryCriteria();
						summaryDescSearch.setOperator(Operator.OR);
						SimpleCheckBox summaryBox = (SimpleCheckBox) descriptorToWidget.get(new FieldDescriptor(
								TaskFieldConstants.SUMMARY_FIELD));
						SimpleCheckBox descriptionBox = (SimpleCheckBox) descriptorToWidget.get(new FieldDescriptor(
								TaskFieldConstants.DESCRIPTION_FIELD));
						if (summaryBox.getValue()) {
							summaryDescSearch.addSubCriteria(new ColumnCriteria(TaskFieldConstants.SUMMARY_FIELD,
									Operator.STRING_CONTAINS, value));
						}
						if (descriptionBox.getValue()) {
							summaryDescSearch.addSubCriteria(new ColumnCriteria(TaskFieldConstants.DESCRIPTION_FIELD,
									Operator.STRING_CONTAINS, value));
							summaryDescSearch.addSubCriteria(new ColumnCriteria(TaskFieldConstants.COMMENT_FIELD,
									Operator.STRING_CONTAINS, value));
						}
						if (summaryDescSearch.hasSubCriteria()) {
							if (summaryDescSearch.getSubCriteria().size() == 1) {
								criteria.addSubCriteria(summaryDescSearch.getSubCriteria().get(0));
							} else {
								criteria.addSubCriteria(summaryDescSearch);
							}
						}
					}
				}
				// All other text boxes
				else {
					TextBox text = (TextBox) widget;
					String value = querySafeValue(text.getText());
					if (value != null && value.trim().length() > 0) {
						value = value.trim();
						criteria.addSubCriteria(new ColumnCriteria(entry.getKey().getName(), Operator.STRING_CONTAINS,
								value));
					}
				}

			} else if (widget instanceof DateBox) {
				ListBox dateTypeListBox = (ListBox) descriptorToWidget.get(new FieldDescriptor("dateType"));
				String selectedDateType = dateTypeListBox.getValue(dateTypeListBox.getSelectedIndex());
				String dateTypeField = null;
				if (selectedDateType.equals("Created")) {
					dateTypeField = TaskFieldConstants.CREATION_TIME_FIELD;
				} else if (selectedDateType.equals("Updated")) {
					dateTypeField = TaskFieldConstants.LAST_UPDATE_FIELD;
				}
				if (dateTypeField != null) {
					DateBox startDateBox = (DateBox) descriptorToWidget.get(new FieldDescriptor("startDate"));
					DateBox endDateBox = (DateBox) descriptorToWidget.get(new FieldDescriptor("endDate"));
					if (widget.equals(startDateBox)) {
						if (startDateBox.getValue() != null) {
							Date value = startDateBox.getValue();
							criteria.addSubCriteria(new ColumnCriteria(dateTypeField, Operator.GREATER_THAN, value));
						}
					} else if (widget.equals(endDateBox)) {
						if (endDateBox.getValue() != null) {
							Date value = endDateBox.getValue();
							criteria.addSubCriteria(new ColumnCriteria(dateTypeField, Operator.LESS_THAN, value));
						}
					}
				}
			}
		}
		return criteria;
	}

	private void maybeSetEditQueryToView() {
		clearWidgets(editQuery == null);
		if (editQuery != null) {
			updateWidgetsWithCriteria(editQuery.getQueryCriteria());
		}
	}

	private void clearWidgets(boolean checkBoxValue) {
		for (Widget w : taskSearchView.getFieldDescriptorToWidget().values()) {
			if (w instanceof SimpleCheckBox) {
				((SimpleCheckBox) w).setValue(checkBoxValue);
			} else if (w instanceof TakesValue<?>) {
				((TakesValue<?>) w).setValue(null);
			} else if (w instanceof ListBox) {
				for (int i = 0; i < ((ListBox) w).getItemCount(); i++) {
					((ListBox) w).setItemSelected(i, false);
				}
			} else if (w instanceof MultiValueChooser) {
				((MultiValueChooser<?>) w).setValues(Collections.EMPTY_LIST);
			}
		}
	}

	protected void doSearch() {
		ProjectTasksPlace.createPlaceForCriteriaQuery(projectIdentifier, calculateCriteria().toString()).go();
	}

	private void updateWidgetsWithCriteria(Criteria crit) {
		if (crit instanceof ColumnCriteria) {
			ColumnCriteria columnCriteria = (ColumnCriteria) crit;
			String field = columnCriteria.getColumnName();

			// Date
			if (field.equals(TaskFieldConstants.LAST_UPDATE_FIELD)
					|| field.equals(TaskFieldConstants.CREATION_TIME_FIELD)) {
				ListBox dateTypeListBox = (ListBox) getWidgetForField("dateType");
				String listBoxValue = field.equals(TaskFieldConstants.LAST_UPDATE_FIELD) ? "Updated" : "Created";
				WidgetUtil.select(dateTypeListBox, listBoxValue);
				switch (columnCriteria.getOperator()) {
				case GREATER_THAN:
					DateBox startDateBox = (DateBox) getWidgetForField("startDate");
					startDateBox.setValue((Date) columnCriteria.getColumnValue());
					break;
				case LESS_THAN:
					DateBox endDateBox = (DateBox) getWidgetForField("endDate");
					endDateBox.setValue((Date) columnCriteria.getColumnValue());
					break;
				default:
					throw new UnsupportedOperationException("Can not parse date crit");

				}
				return;
			}
			// Keywords
			else if (field.endsWith(TaskFieldConstants.KEYWORDS_FIELD)) {
				MultiValueChooser<Keyword> keywordChooser = (MultiValueChooser<Keyword>) getWidgetForField("tags");
				Keyword keyword = null;
				for (Keyword k : repositoryConfiguration.getKeywords()) {
					if (k.getName().equals(columnCriteria.getColumnValue())) {
						keyword = k;
						break;
					}
				}
				keywordChooser.addValue(keyword, false);
				return;
			}
			// People
			else if (field.equals(TaskFieldConstants.REPORTER_FIELD) || field.equals(TaskFieldConstants.ASSIGNEE_FIELD)
					|| field.equals(TaskFieldConstants.COMMENT_AUTHOR_FIELD)
					|| field.equals(TaskFieldConstants.WATCHER_FIELD)) {
				TakesValue<Boolean> box = (TakesValue<Boolean>) getWidgetForField(field);
				box.setValue(true);

				MultiValueChooser<Person> personChooser = (MultiValueChooser<Person>) getWidgetForField("person");
				String personLogin = columnCriteria.getColumnValue().toString();
				Person person = new Person(personLogin, personLogin);
				for (TaskUserProfile taskPerson : repositoryConfiguration.getUsers()) {
					if (taskPerson.getLoginName().equals(personLogin)) {
						person = PersonUtil.toPerson(taskPerson);
						break;
					}
				}
				personChooser.addValue(person, false);
				return;
			}
			// Text
			else if (field.equals(TaskFieldConstants.SUMMARY_FIELD)
					|| field.equals(TaskFieldConstants.DESCRIPTION_FIELD)) {
				TakesValue<Boolean> box = (TakesValue<Boolean>) getWidgetForField(field);
				box.setValue(true);

				TextBox textBox = (TextBox) getWidgetForField("summaryDescriptionComments");
				textBox.setValue(columnCriteria.getColumnValue().toString());
				return;
			} else if (field.equals(TaskFieldConstants.COMMENT_FIELD)) {
				// Ignore this because it is part of the above
				return;
			}

			// Generic
			Widget value = getWidgetForField(field);
			if (value == null) {
				throw new UnsupportedOperationException("Can not parse, unknown field: " + field);
			}
			if (value instanceof TextBox) {
				((TextBox) value).setText(columnCriteria.getColumnValue().toString());
			} else if (value instanceof ListBox) {
				ListBox listBox = (ListBox) value;
				String colValue = columnCriteria.getColumnValue().toString();
				for (int i = 0; i < listBox.getItemCount(); i++) {
					if (listBox.getItemText(i).equals(colValue)) {
						listBox.setItemSelected(i, true);
						return;
					}
				}
			}
		} else if (crit instanceof NaryCriteria) {
			// FIXME/REVIEW this is not necessarly correct, not all crit can be represented in this UI
			NaryCriteria naryCriteria = (NaryCriteria) crit;
			for (Criteria sub : naryCriteria.getSubCriteria()) {
				updateWidgetsWithCriteria(sub);
			}
		}
	}

	private Widget getWidgetForField(String fieldName) {
		for (Entry<FieldDescriptor, Widget> e : taskSearchView.getFieldDescriptorToWidget().entrySet()) {
			if (e.getKey().getName().equals(fieldName)) {
				return e.getValue();
			}
		}
		return null;
	}

	private String querySafeValue(String text) {
		if (text == null) {
			return null;
		}
		// can't have '?' in our query string, see bug 1649
		return text.replace("?", "");
	}

	public void setEditQuery(SavedTaskQuery editQuery) {
		this.editQuery = editQuery;
		this.taskSearchView.setEditQuery(editQuery);

		if (this.repositoryConfiguration != null) {
			maybeSetEditQueryToView();
		} // Otherwise this happens after the view is configured
	}

	public void setEditCriteria(Criteria criteria) {
		this.editQuery = null;
		this.taskSearchView.setEditQuery(null);
		updateWidgetsWithCriteria(criteria);
	}

	// REVIEW consider pushing this up to TasksPresenter. Then we can just swap out some displays and avoid full reload
	private void doUpdateQuery() {
		editQuery.setQueryString(calculateCriteria().toQueryString());
		getDispatchService().execute(new UpdateQueryAction(projectIdentifier, editQuery),
				new AsyncCallbackSupport<UpdateQueryResult>(new OperationMessage("Saving")) {

					@Override
					protected void success(UpdateQueryResult actionResult) {
						ProjectTasksPlace place = ProjectTasksPlace.createPlaceForNamedQuery(projectIdentifier,
								editQuery.getName());
						place.displayOnArrival(Message.createSuccessMessage("Query Saved"));
						place.go();
					}
				});
	}

}
