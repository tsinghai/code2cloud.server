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

import static com.tasktop.c2c.server.common.web.client.widgets.Format.stringValueDateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FieldSetElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.adapters.EditorSource;
import com.google.gwt.editor.client.adapters.ListEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.testing.PassthroughRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.tasktop.c2c.server.common.web.client.view.AbstractComposite;
import com.tasktop.c2c.server.common.web.client.view.Avatar;
import com.tasktop.c2c.server.common.web.client.widgets.Format;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.MultiValueChooser;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.SingleValueChooser;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.StringSuggestion;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.StringValueCompositeFactory;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.PersonSuggestOracle;
import com.tasktop.c2c.server.common.web.client.widgets.time.TimePeriodBox;
import com.tasktop.c2c.server.common.web.client.widgets.time.TimePeriodRenderer;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.presenter.person.ProjectPersonService;
import com.tasktop.c2c.server.tasks.client.TaskResources;
import com.tasktop.c2c.server.tasks.client.place.ProjectEditTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectNewTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskHistoryPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksPlace;
import com.tasktop.c2c.server.tasks.client.presenters.TaskPresenter;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.keywords.KeywordCompositeFactory;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.keywords.KeywordSuggestOracle;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.task.ParentTaskChooser;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.task.TaskCompositeFactory;
import com.tasktop.c2c.server.tasks.client.widgets.chooser.task.TaskSuggestOracle;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.KeywordSuggestService;
import com.tasktop.c2c.server.tasks.client.widgets.wiki.EditWikiPanel;
import com.tasktop.c2c.server.tasks.client.widgets.wiki.WikiHTMLPanel;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.ExternalTaskRelation;
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
import com.tasktop.c2c.server.tasks.domain.WorkLog;

public class TaskViewImpl extends AbstractComposite implements TaskView, Editor<Task> {

	private static final String NOT_SET = "Not set";
	private static TaskViewImpl instance = null;

	public static TaskViewImpl getInstance() {
		if (instance == null) {
			instance = new TaskViewImpl();
		}
		return instance;
	}

	interface Binder extends UiBinder<Widget, TaskViewImpl> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	abstract class InlineEditableField {
		private final HTML readOnlyField; // for anon users
		private final Anchor editAnchor;
		private final Widget editableField;

		public InlineEditableField(HTML readOnlyField, Anchor editAnchor, Widget editableField) {
			this(readOnlyField, editAnchor, editableField, editAnchor);
		}

		public InlineEditableField(HTML readOnlyField, Anchor editAnchor, Widget editableField,
				HasClickHandlers editTrigger) {
			this.readOnlyField = readOnlyField;
			this.editableField = editableField;
			this.editAnchor = editAnchor;

			if (editTrigger != null) {
				editTrigger.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						startInlineEdit(InlineEditableField.this);
					}
				});
			}
		}

		public void renderTask(Task t) {
			setTask(t);

			SafeHtml html = getHtml(t);
			UIObject.setVisible(editableField.getElement().getParentElement(), false);
			boolean anon = AuthenticationHelper.isAnonymous();
			if (readOnlyField != null) {
				readOnlyField.setVisible(anon);
			}
			if (getHideOnEditElement() != null) {
				UIObject.setVisible(getHideOnEditElement(), !anon);
			}

			if (html != null && anon && readOnlyField != null) {
				readOnlyField.setHTML(html);
			}

			if (!anon && editAnchor != null) {

				SafeHtml editHtml;

				if (html == null) {
					editHtml = visibleTriggerHtml();
					getEditTrigerElement(true).removeClassName(TaskResources.resources.style().inlineEditTrigger());
					getEditTrigerElement(false).removeClassName(TaskResources.resources.style().inlineEditTrigger());
					getEditTrigerElement(true).addClassName(TaskResources.resources.style().visibleTrigger());
				} else {
					editHtml = editTriggerHtml(html);
					getEditTrigerElement(true).removeClassName(TaskResources.resources.style().visibleTrigger());
					getEditTrigerElement(false).removeClassName(TaskResources.resources.style().visibleTrigger());
					getEditTrigerElement(false).addClassName(TaskResources.resources.style().inlineEditTrigger());
				}

				editAnchor.setHTML(editHtml);
			}

		}

		protected SafeHtml editTriggerHtml(SafeHtml html) {
			return template.editTriggerHtml(html, TaskResources.resources.pencilIcon().getSafeUri());
		}

		protected SafeHtml visibleTriggerHtml() {
			return template.imageHtml(TaskResources.resources.pencilIcon().getSafeUri());
		}

		protected Element getEditTrigerElement(boolean triggerVisible) {
			if (editAnchor == null) {
				return null;
			}
			if (triggerVisible) {
				return editAnchor.getElement();
			}
			return editAnchor.getElement().getParentElement();
		}

		protected Element getHideOnEditElement() {
			return getEditTrigerElement(false);
		}

		protected SafeHtml getHtml(Task t) {
			return SafeHtmlUtils.EMPTY_SAFE_HTML;
		}

		protected abstract void flushTask();

		protected void setTask(Task t) {
			// Empty if the editor does it all
		}

		public void cancelEdit() {
			renderTask(task);
		}

		public void startEdit() {
			if (getHideOnEditElement() != null) {
				UIObject.setVisible(getHideOnEditElement(), false);
			}
			if (editableField != null) {
				UIObject.setVisible(editableField.getElement().getParentElement(), true);

				ProfileGinjector.get.instance().getScheduler().scheduleDeferred(new ScheduledCommand() {

					@Override
					public void execute() {
						deferedStartEdit();
					}
				});

			}

		}

		protected void deferedStartEdit() {
			editableField.getElement().focus();
		}

		public void saveEdit() {
			flushTask();
			renderTask(task);
		}

	}

	interface Driver extends SimpleBeanEditorDriver<Task, TaskViewImpl> {

	}

	private Driver driver = GWT.create(Driver.class);

	@UiField(provided = true)
	ValueLabel<Integer> id = new ValueLabel<Integer>(new AbstractRenderer<Integer>() {

		@Override
		public String render(Integer object) {
			return object + ": ";
		}

	});
	@UiField
	Anchor editTaskTypeAnchor;
	@UiField(provided = true)
	ValueListBox<String> taskType = new ValueListBox<String>(PassthroughRenderer.instance());
	@Ignore
	@UiField
	HTML readOnlyTaskType;

	@UiField
	Anchor editSeverityAnchor;
	@UiField
	@Ignore
	HTML readOnlySeverity;
	@UiField(provided = true)
	ValueListBox<TaskSeverity> severity = new ValueListBox<TaskSeverity>(new AbstractRenderer<TaskSeverity>() {

		@Override
		public String render(TaskSeverity s) {
			return s.getValue();
		}

	});
	@UiField
	Anchor editPriorityAnchor;
	@UiField
	@Ignore
	HTML readOnlyPriority;
	@UiField(provided = true)
	ValueListBox<Priority> priority = new ValueListBox<Priority>(new AbstractRenderer<Priority>() {

		@Override
		public String render(Priority p) {
			return p.getValue();
		}

	});

	@UiField
	Anchor editEstimateAnchor;
	@UiField
	@Ignore
	HTML readOnlyEstimate;
	@UiField(provided = true)
	TimePeriodBox estimatedTime = TimePeriodBox.getHourBoxWithDefaultsEntryToDays();
	@UiField
	Anchor editTimeSpentAnchor;
	@UiField
	@Ignore
	HTML readOnlyTimeSpent;
	@UiField(provided = true)
	@Ignore
	TimePeriodBox timeSpent = TimePeriodBox.getDefaultHourBox();

	@UiField
	Anchor editProductAnchor;
	@UiField
	@Ignore
	HTML readOnlyProduct;
	@UiField(provided = true)
	ValueListBox<Product> product = new ValueListBox<Product>(new AbstractRenderer<Product>() {

		@Override
		public String render(Product object) {
			return object.getName();
		}
	});

	@UiField
	Anchor editComponentAnchor;
	@UiField
	@Ignore
	HTML readOnlyComponent;
	@UiField(provided = true)
	ValueListBox<Component> component = new ValueListBox<Component>(new AbstractRenderer<Component>() {
		@Override
		public String render(Component object) {
			return object.getName();
		}
	});

	@UiField
	Anchor editFoundInAnchor;
	@UiField
	@Ignore
	HTML readOnlyFoundIn;
	@UiField(provided = true)
	SingleValueChooser<String> foundInRelease = new SingleValueChooser<String>(new SuggestOracle() {

		@Override
		public void requestSuggestions(Request request, Callback callback) {
			List<Suggestion> suggestions = new ArrayList<Suggestion>();

			boolean foundExact = false;
			for (String release : repositoryConfiguration.getReleaseTags(product.getValue())) {
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
	Anchor editReleaseAnchor;
	@UiField
	@Ignore
	HTML readOnlyRelease;
	@UiField(provided = true)
	@SuppressWarnings("unchecked")
	ValueListBox<Milestone> milestone = new ValueListBox<Milestone>(ReferenceValueRenderer.getInstance());

	@UiField
	Anchor editIterationAnchor;
	@UiField(provided = true)
	ValueListBox<Iteration> iteration = new ValueListBox<Iteration>(new AbstractRenderer<Iteration>() {

		@Override
		public String render(Iteration object) {
			return object.getValue();
		}
	});

	@UiField
	@Ignore
	HTML readOnlyIteration;
	@UiField
	Anchor editDueDateAnchor;
	@UiField
	@Ignore
	HTML readOnlyDueDate;
	@UiField(provided = true)
	DateBox deadline = new DateBox(new DatePicker(), new Date(), new DateBox.DefaultFormat(Format.getDateFormat()));

	@UiField
	Anchor editTagsAnchor;
	@UiField
	@Ignore
	HTML readOnlyTags;
	@UiField(provided = true)
	MultiValueChooser<Keyword> keywords = new MultiValueChooser<Keyword>(new KeywordSuggestOracle(null));

	@UiField
	Anchor taskListLink;
	@UiField
	Anchor taskHistoryLink;
	@UiField
	SpanElement newSubTaskSpan;
	@UiField
	Anchor newSubTaskLink;
	@UiField
	Panel attachmentsPanel;

	@UiField
	Anchor editTaskAnchor;

	@UiField
	Anchor editSummaryAnchor;
	@Ignore
	@UiField
	HTML readOnlySummary;
	@UiField
	TextBox shortDescription;

	@UiField
	Image createdByImage;
	@UiField
	@Ignore
	Label createdBy;
	@UiField
	@Ignore
	Label creationDate;
	@UiField
	@Ignore
	Label updateDate;

	@UiField
	WikiHTMLPanel readOnlyDescription;
	@UiField
	EditWikiPanel description;

	@UiField
	DivElement editControlsDiv;
	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;

	@UiField
	Panel statusPanel;
	@UiField
	Anchor editStatusAnchor;
	@UiField
	@Ignore
	HTML readOnlyStatus;
	@UiField
	@Path("")
	StatusEditorView statusEditor;

	@UiField
	Anchor editOwnerAnchor;
	@UiField
	@Ignore
	HTML readOnlyOwner;
	@UiField(provided = true)
	SingleTaskProfileChooser assignee = new SingleTaskProfileChooser(new PersonSuggestOracle(null));;
	@UiField
	Anchor editCCAnchor;
	@UiField
	@Ignore
	HTML readOnlyCC;
	@UiField(provided = true)
	MultipleTaskProfileChooser watchers = new MultipleTaskProfileChooser(new PersonSuggestOracle(null));

	private TaskSuggestOracle taskOracle = new TaskSuggestOracle();

	@UiField
	Panel parentTasksPanel;
	@UiField
	Anchor editParentAnchor;
	@UiField(provided = true)
	protected ParentTaskChooser blocksTasks = new ParentTaskChooser(taskOracle);

	@UiField
	Panel subTasksPanel;
	@UiField
	Anchor editSubtasksAnchor;
	@UiField(provided = true)
	MultiValueChooser<Task> subTasks = new MultiValueChooser<Task>(taskOracle);
	@UiField
	FieldSetElement duplicatesElement;
	@UiField
	Panel duplicates;
	@UiField
	FieldSetElement externalDepsElement;
	@UiField
	Panel externalTaskRelations;
	@UiField
	CommentsPanel commentsPanel;
	@UiField
	Panel customFieldsPanel;

	private TaskPresenter presenter;

	private String projectIdentifier;
	private Task task;

	private static class CustomFieldSource extends EditorSource<CustomFieldEditor> {
		@Override
		public CustomFieldEditor create(int index) {
			return new CustomFieldEditor();

		}

	}

	protected CustomFieldSource customFieldSource = new CustomFieldSource();
	@Ignore
	protected ListEditor<CustomField, CustomFieldEditor> customFieldEditor = ListEditor.of(customFieldSource);

	interface CFDriver extends SimpleBeanEditorDriver<List<CustomField>, ListEditor<CustomField, CustomFieldEditor>> {
	}

	private CFDriver customFieldDriver = GWT.create(CFDriver.class);

	protected TaskMessages taskMessages = GWT.create(TaskMessages.class);

	private RepositoryConfiguration repositoryConfiguration;
	private List<InlineEditableField> inlineEditFields = new ArrayList<InlineEditableField>();
	private List<InlineEditableField> editingFields = new ArrayList<InlineEditableField>();
	private InlineEditableField lastEditedField;

	private TaskViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		initEditFields();
		driver.initialize(this);
		customFieldDriver.initialize(customFieldEditor);

		commentsPanel.postComment.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				postComment();
			}
		});
		newSubTaskSpan.appendChild(new Image(TaskResources.resources.newSubtaskIcon()).getElement());
		UIObject.setVisible(editControlsDiv, false);
		keywords.setValueCompositeFactory(new KeywordCompositeFactory());
		foundInRelease.setValueCompositeFactory(new StringValueCompositeFactory());
		subTasks.setValueCompositeFactory(new TaskCompositeFactory());
		blocksTasks.setValueCompositeFactory(new TaskCompositeFactory());
		editTaskAnchor.setHTML(template.iconText("Edit"));
	}

	private void initEditFields() {

		inlineEditFields.add(new InlineEditableField(readOnlyTaskType, editTaskTypeAnchor, taskType) {

			@Override
			public void flushTask() {
				presenter.saveTaskType(taskType.getValue());
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return SafeHtmlUtils.fromString((t.getTaskType()));
			}

			protected SafeHtml editTriggerHtml(SafeHtml html) {
				return html; // No place to layout with the icon
			}

		});

		inlineEditFields.add(new InlineEditableField(readOnlyPriority, editPriorityAnchor, priority) {

			@Override
			public void flushTask() {
				presenter.savePriority(priority.getValue());
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return SafeHtmlUtils.fromString((t.getPriority().getValue()));
			}

		});

		inlineEditFields.add(new InlineEditableField(readOnlySeverity, editSeverityAnchor, severity) {

			@Override
			public void flushTask() {
				presenter.saveSeverity(severity.getValue());
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return SafeHtmlUtils.fromString(t.getSeverity().getValue());
			}

		});

		inlineEditFields.add(new InlineEditableField(readOnlyStatus, editStatusAnchor, statusEditor) {

			@Override
			public void flushTask() {
				presenter.saveStatus(statusEditor.getSelectedStatus(), statusEditor.getSelectedResolution(),
						statusEditor.getDuplicateId());
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				statusPanel.clear();
				if (task.getStatus() != null) {
					if (task.getResolution() != null) {
						StringBuffer buf = new StringBuffer();

						buf.append(task.getResolution().toString());

						if (task.getResolution().isDuplicate() && task.getDuplicateOf() != null) {
							buf.append(" of ");
							Label label = new Label(buf.toString());
							statusPanel.add(label);

							statusPanel.add(createTaskAnchor(task.getDuplicateOf()));
						} else {
							Label label = new Label(buf.toString());
							statusPanel.add(label);
						}
					}
				}

				return SafeHtmlUtils.fromString(t.getStatus().getValue());
			}

			@Override
			public void startEdit() {
				super.startEdit();
				statusPanel.clear();
			}

		});

		final InlineEditableField descriptionEditableField = new InlineEditableField(null, null, description, null) {

			@Override
			public void flushTask() {
				presenter.saveDescription(description.getValue());
				task.setWikiRenderedDescription(null);

			}

			@Override
			public void setTask(Task t) {
				readOnlyDescription.setWikiHTML(task.getWikiRenderedDescription());
				if (task.getWikiRenderedDescription() != null) {
					description.setRenderedValue(task.getWikiRenderedDescription());
				}
			}

			public void renderTask(Task t) {
				boolean anon = AuthenticationHelper.isAnonymous();
				readOnlyDescription.setVisible(anon);
				description.setVisible(!anon);
				setTask(t);
			}

		};
		inlineEditFields.add(descriptionEditableField);
		description.getTextArea().setWidth("98%"); // FIXME
		description.addToggleListener(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue() && !editingFields.contains(descriptionEditableField)) {
					startInlineEdit(descriptionEditableField);
				}
			}
		});

		inlineEditFields.add(new InlineEditableField(readOnlySummary, editSummaryAnchor, shortDescription) {

			@Override
			public void flushTask() {
				presenter.saveShortDescription(shortDescription.getText());
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				boolean empty = task.getShortDescription() == null || task.getShortDescription().isEmpty();
				return SafeHtmlUtils.fromTrustedString(empty ? NOT_SET : task.getShortDescription());
			}

			protected SafeHtml editTriggerHtml(SafeHtml html) {
				return html; // No place to layout with the icon
			}
		});

		inlineEditFields.add(new InlineEditableField(readOnlyEstimate, editEstimateAnchor, estimatedTime) {

			@Override
			public void flushTask() {
				presenter.saveEstimate(estimatedTime.getValue());
			}

			@Override
			protected SafeHtml getHtml(Task t) {

				return formatThisAndSubtaskTime(t.getEstimatedTime(), t.getSumOfSubtasksEstimatedTime());
			}

		});

		inlineEditFields.add(new InlineEditableField(readOnlyOwner, editOwnerAnchor, assignee.asWidget()) {

			@Override
			public void flushTask() {
				presenter.saveOwner(assignee.getValue());
			}

			@Override
			protected void deferedStartEdit() {
				assignee.getWrappedChooser().getSuggestBox().showSuggestionList();
				assignee.getWrappedChooser().getSuggestBox().getElement().focus();
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return SafeHtmlUtils.fromString(t.getAssignee() == null ? NOT_SET : t.getAssignee().getRealname());
			}

		});

		inlineEditFields.add(new InlineEditableField(readOnlyTimeSpent, editTimeSpentAnchor, timeSpent) {

			@Override
			public void flushTask() {
				if (timeSpent.getValue() == null || timeSpent.getValue().signum() == 0) {
					// otherwise we get into a bad state
					return;
				}
				WorkLog workLog = new WorkLog();
				workLog.setHoursWorked(timeSpent.getValue());
				presenter.saveWorkLog(workLog);
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				BigDecimal sum = null;

				List<WorkLog> workLogs = t.getWorkLogs();
				if (workLogs != null && !workLogs.isEmpty()) {
					sum = BigDecimal.ZERO;
					for (WorkLog workLog : workLogs) {
						sum = sum.add(workLog.getHoursWorked());
					}
				}
				return formatThisAndSubtaskTime(sum, t.getSumOfSubtasksTimeSpent());
			}
		});

		inlineEditFields.add(new InlineEditableField(readOnlyCC, editCCAnchor, watchers.asWidget()) {

			@Override
			protected void flushTask() {
				presenter.saveCC(watchers.getValue());
			}

			@Override
			protected void deferedStartEdit() {
				watchers.getWrappedChooser().getSuggestBox().showSuggestionList();
				watchers.getWrappedChooser().getSuggestBox().getElement().focus();
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return getHtmlForPeople(t.getWatchers());
			}

		});

		inlineEditFields.add(new InlineEditableField(readOnlyTags, editTagsAnchor, keywords) {
			@Override
			protected void flushTask() {
				presenter.saveTags(keywords.getValues());
			}

			@Override
			protected void deferedStartEdit() {
				keywords.getSuggestBox().showSuggestionList();
				keywords.getSuggestBox().getElement().focus();
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return getHtmlForKeywords(t.getKeywords());

			}
		});

		inlineEditFields.add(new InlineEditableField(readOnlyIteration, editIterationAnchor, iteration) {

			@Override
			protected void flushTask() {
				presenter.saveIteration(iteration.getValue());

			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return SafeHtmlUtils.fromString(t.getIteration().getValue());

			}
		});

		inlineEditFields.add(new InlineEditableField(readOnlyDueDate, editDueDateAnchor, deadline) {

			@Override
			protected void flushTask() {
				presenter.saveDueDate(deadline.getValue());
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return SafeHtmlUtils.fromString(t.getDeadline() == null ? NOT_SET : Format.stringValueDate(t
						.getDeadline()));

			}
		});

		inlineEditFields.add(new InlineEditableField(null, editSubtasksAnchor, subTasks) {

			@Override
			protected void flushTask() {
				presenter.saveSubTasks(subTasks.getValue());
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				subTasksPanel.clear();
				List<Task> subTaskList = task.getSubTasks();
				if (subTaskList.size() > 0) {
					for (int i = 0; i < subTaskList.size(); i++) {
						subTasksPanel.add(createTaskAnchor(subTaskList.get(i)));
						if (i < (subTaskList.size() - 1)) {
							subTasksPanel.add(new Label(", "));
						}
					}
					subTasksPanel.add(editSubtasksAnchor);
					return null;
				} else {
					subTasksPanel.add(editSubtasksAnchor);
					return SafeHtmlUtils.fromTrustedString("None");
				}

			}

		});

		inlineEditFields.add(new InlineEditableField(null, editParentAnchor, blocksTasks) {

			@Override
			protected void flushTask() {
				presenter.saveBlocks(blocksTasks.getValue());
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				parentTasksPanel.clear();
				if (task.getBlocksTasks() != null && !task.getBlocksTasks().isEmpty()) {
					for (int i = 0; i < task.getBlocksTasks().size(); i++) {
						parentTasksPanel.add(createTaskAnchor(task.getBlocksTasks().get(i)));
						if (i < (t.getBlocksTasks().size() - 1)) {
							parentTasksPanel.add(new Label(", "));
						}
					}
					parentTasksPanel.add(editParentAnchor);
					return null;
				} else {
					parentTasksPanel.add(editParentAnchor);
					return SafeHtmlUtils.fromTrustedString("None");
				}

			}

		});

		initProductComopnentReleaseFoundIn();

	}

	private void initProductComopnentReleaseFoundIn() {
		inlineEditFields.add(new InlineEditableField(readOnlyProduct, editProductAnchor, product) {

			@Override
			protected void flushTask() {
				presenter.saveProduct(product.getValue());

			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return SafeHtmlUtils.fromString(t.getProduct().getName());

			}
		});

		inlineEditFields.add(new InlineEditableField(readOnlyComponent, editComponentAnchor, component) {

			@Override
			protected void flushTask() {
				presenter.saveComponent(component.getValue());
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return SafeHtmlUtils.fromString(t.getComponent().getName());

			}
		});
		inlineEditFields.add(new InlineEditableField(readOnlyFoundIn, editFoundInAnchor, foundInRelease) {

			@Override
			protected void flushTask() {
				presenter.saveFoundInRelease(foundInRelease.getValue());
			}

			@Override
			protected void deferedStartEdit() {
				foundInRelease.getSuggestBox().showSuggestionList();
				foundInRelease.getSuggestBox().getElement().focus();
			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return SafeHtmlUtils.fromString(t.getFoundInRelease() == null ? NOT_SET : t.getFoundInRelease());

			}
		});
		inlineEditFields.add(new InlineEditableField(readOnlyRelease, editReleaseAnchor, milestone) {

			@Override
			protected void flushTask() {
				presenter.saveRelease(milestone.getValue());

			}

			@Override
			protected SafeHtml getHtml(Task t) {
				return SafeHtmlUtils.fromString(t.getMilestone().getValue());

			}
		});

	}

	private static SafeHtml formatThisAndSubtaskTime(BigDecimal thisTime, BigDecimal subTasksTime) {
		if (thisTime == null && subTasksTime.signum() == 0) {
			return SafeHtmlUtils.fromTrustedString(NOT_SET);
		} else if (subTasksTime.signum() == 0) {
			return SafeHtmlUtils.fromTrustedString(TimePeriodRenderer.HOUR_RENDERER.render(thisTime));
		} else if (thisTime == null) {
			return SafeHtmlUtils
					.fromTrustedString("Subtasks: " + TimePeriodRenderer.HOUR_RENDERER.render(subTasksTime));
		} else {
			BigDecimal totalTime = thisTime == null ? subTasksTime : subTasksTime.add(thisTime);
			return template.timeBreakdown(TimePeriodRenderer.HOUR_RENDERER.render(totalTime),
					TimePeriodRenderer.HOUR_RENDERER.render(thisTime),
					TimePeriodRenderer.HOUR_RENDERER.render(subTasksTime));
		}
	}

	private static HtmlTemplates template = GWT.create(HtmlTemplates.class);

	static interface HtmlTemplates extends SafeHtmlTemplates {
		@Template("Total: {0} <br> This: {1} <br> Subtasks: {2}")
		SafeHtml timeBreakdown(String totalTime, String thisTaskTime, String subTaskTime);

		@Template("<div>{0}<img src=\"{1}\"/></div>")
		SafeHtml editTriggerHtml(SafeHtml safeHtml, SafeUri imageUri);

		@Template("<img src=\"{0}\"/>")
		SafeHtml imageHtml(SafeUri imageUri);

		@Template("<span></span>{0}")
		SafeHtml iconText(String text);
		//
		// @Template("{0}{1}<div class=\"date-info\"><div class=\"created left\"><span>Created by</span><span class=\"username\"><span class=\"avatar micro name-include\"><img src=\"{2}\"/>{3}</span><span>on {4}</span></span></div><div class=\"changed right\"><span>Updated on {5}</span></div><div class=\"clear\"></div>")
		// SafeHtml titleHtml(SafeHtml anchorHtml, String titleLabel, String avatarUrl, String reporterName,
		// String createDate, String modifyDate);
	}

	public void setProjectIdentifier(String projectIdentifier) {
		this.projectIdentifier = projectIdentifier;
		statusEditor.setProjectIdentifier(projectIdentifier);

		((KeywordSuggestOracle) keywords.getSuggestBox().getSuggestOracle())
				.setSuggestionService(new KeywordSuggestService(projectIdentifier));

		description.setProjectId(projectIdentifier);
		taskListLink.setHref(ProjectTasksPlace.createDefaultPlace(projectIdentifier).getHref());

		// Hide things which require a logged-in user
		boolean canEdit = !AuthenticationHelper.isAnonymous();
		newSubTaskLink.setVisible(canEdit);
		commentsPanel.setProjectIdentifier(projectIdentifier);
		taskOracle.setProjectIdentifier(projectIdentifier);

	}

	public void setTask(Task task) {
		this.task = task;

		editTaskAnchor.setHref(ProjectEditTaskPlace.createPlace(projectIdentifier, task.getId()).getHref());
		editTaskAnchor.setVisible(!AuthenticationHelper.isAnonymous());

		createdByImage.setUrl(Avatar.computeAvatarUrl(task.getReporter().getGravatarHash(), Avatar.Size.MICRO));
		createdBy.setText(task.getReporter().getLoginName());
		creationDate.setText(stringValueDateTime(task.getCreationDate()));
		updateDate.setText(stringValueDateTime(task.getModificationDate()));

		newSubTaskLink.setHref(ProjectNewTaskPlace.createNewSubtaskPlace(projectIdentifier, task.getId()).getHref());

		// Editable fields
		driver.edit(task);
		updateAcceptableValues();
		for (InlineEditableField f : inlineEditFields) {
			f.renderTask(task);
		}

		duplicates.clear();
		UIObject.setVisible(duplicatesElement, !task.getDuplicates().isEmpty());
		for (int i = 0; i < task.getDuplicates().size(); i++) {
			if (i != 0) {
				duplicates.add(new Label(", "));
			}
			duplicates.add(createTaskAnchor(task.getDuplicates().get(i)));
		}

		// external task relations
		externalTaskRelations.clear();
		List<ExternalTaskRelation> externalList = task.getExternalTaskRelations();
		UIObject.setVisible(externalDepsElement, !externalList.isEmpty());
		for (int i = 0; i < externalList.size(); i++) {
			externalTaskRelations.add(new Anchor(externalList.get(i).getUri(), externalList.get(i).getUri(), "new"));
			if (i < (externalList.size() - 1)) {
				externalTaskRelations.add(new Label(", "));
			}
		}

		// attachments
		setAttachments(task.getAttachments());
		updateCommentView(task);

		commentsPanel.setValue(task);

		taskHistoryLink.setHref(ProjectTaskHistoryPlace.createPlace(projectIdentifier, task.getId()).getHref());

		updateCustomFields(task);
	}

	private void updateCustomFields(Task task) {
		customFieldsPanel.clear();

		if (repositoryConfiguration.getCustomFields() == null || repositoryConfiguration.getCustomFields().isEmpty()) {
			return;
		}
		List<CustomField> customFields = new ArrayList<CustomField>(repositoryConfiguration.getCustomFields().size());

		for (FieldDescriptor field : repositoryConfiguration.getCustomFields()) {
			if (field.isObsolete()) {
				continue;
			}
			String fieldValue = task.getCustomFields().get(field.getName());
			customFields.add(new CustomField(field, fieldValue));

		}

		customFieldDriver.edit(customFields);

		List<InlineEditableField> customInlineEditFields = new ArrayList<InlineEditableField>();

		for (final CustomFieldEditor editor : customFieldEditor.getEditors()) {

			InlineEditableCustomField customFieldWidget = new InlineEditableCustomField(editor);
			customFieldsPanel.add(customFieldWidget);

			customInlineEditFields.add(new InlineEditableField(customFieldWidget.getReadOnlyField(), customFieldWidget
					.getEditFieldAnchor(), editor.getWidget()) {

				@Override
				protected void flushTask() {
					CustomField edited = editor.getValue();
					presenter.saveCustomField(edited.getFieldDescriptor().getName(), edited.getValue());
				}

				@Override
				protected SafeHtml getHtml(Task t) {
					String value = t.getCustomFields().get(editor.getFieldDescriptor().getName());
					final String displayValue;
					if (value == null || value.isEmpty()) {
						displayValue = NOT_SET;
					} else if (editor.getFieldDescriptor().getFieldType().equals(FieldType.TIMESTAMP)) {
						displayValue = stringValueDateTime(new Date(Long.parseLong(value)));
					} else {
						displayValue = value;
					}
					return SafeHtmlUtils.fromString(displayValue);
				}
			});

		}

		for (InlineEditableField f : customInlineEditFields) {
			f.renderTask(task);
		}

	}

	private SafeHtml getHtmlForKeywords(List<Keyword> keywords) {

		// Create strings from our keywords
		List<String> keywordNames = new ArrayList<String>(keywords.size());
		for (Keyword curKeyword : keywords) {
			keywordNames.add(curKeyword.getName());
		}

		return getHtmlForStrings(keywordNames);
	}

	private SafeHtml getHtmlForPeople(List<TaskUserProfile> people) {

		// Create strings from our keywords
		List<String> peopleNames = new ArrayList<String>(people.size());
		for (TaskUserProfile curPerson : people) {
			peopleNames.add(curPerson.getRealname());
		}

		return getHtmlForStrings(peopleNames);
	}

	private SafeHtml getHtmlForStrings(List<String> strings) {
		SafeHtmlBuilder shb = new SafeHtmlBuilder();

		for (int i = 0; i < strings.size(); i++) {
			// First, append this string as an escaped value to prevent XSS
			shb.appendEscaped(strings.get(i));

			// Then, if this isn't the last tag, append a <br/>
			if (i < (strings.size() - 1)) {
				shb.appendHtmlConstant("<br/>");
			}
		}

		// Check if we ended up with a blank string - if we did, send back a default
		if (strings.size() == 0) {
			shb.appendEscaped("None");
		}

		return shb.toSafeHtml();
	}

	private TaskAnchor createTaskAnchor(Task sourceTask) {
		Integer id = sourceTask.getId();
		String url = sourceTask.getUrl();
		TaskAnchor anchor = TaskAnchorManager.createAnchor(projectIdentifier, id, id.toString(), url);
		if (sourceTask.getStatus() != null && !sourceTask.getStatus().isOpen()) {
			anchor.addStyleName(TaskResources.resources.style().completeTask());
		}
		return anchor;
	}

	public void updateCommentView(Task task) {
		commentsPanel.setValue(task);
	}

	private void setAttachments(List<Attachment> attachments) {
		attachmentsPanel.clear();
		if (attachments.size() <= 0) {
			attachmentsPanel.add(new Label("None"));
			return;
		}

		for (int i = 0; i < attachments.size(); i++) {
			Attachment attachment = attachments.get(i);
			Panel attachmentPanel = new FlowPanel();
			attachmentPanel.setStyleName("file-uploaded");
			attachmentPanel.add(new HTML("<span class=\"file-icon\"/>"));
			attachmentPanel.add(new Anchor(attachment.getFilename(), attachment.getUrl()));
			// table.setHTML(i, 1, attachment.getDescription());
			attachmentPanel.add(new Label("   ("
					+ String.valueOf(Math.round(Float.valueOf(attachment.getByteSize()) / 1024)) + " KB)"));
			// PersonLabel personLabel = new PersonLabel();
			// personLabel.setPerson(toPerson(attachment.getSubmitter()));
			// personLabel.setAsSelf(toPerson(attachment.getSubmitter()).equals(currenUser));
			// table.setWidget(i, 2, personLabel);
			// table.setHTML(i, 3, stringValueDateTime(attachment.getCreationDate()));
			attachmentsPanel.add(attachmentPanel);
		}
	}

	@Override
	public void setSelf(Person self) {
		commentsPanel.setSelf(self);
	}

	/**
	 * @param presenter
	 *            the presenter to set
	 */
	public void setPresenter(TaskPresenter taskPresenter) {
		this.presenter = taskPresenter;
	}

	private void postComment() {
		presenter.postComment(commentsPanel.getText());
	}

	private void startInlineEdit(InlineEditableField field) {
		// Currently not supporting editing multiple fields in one go.
		for (InlineEditableField inEditField : editingFields) {
			if (driver.isDirty() || customFieldDriver.isDirty()) {
				// FIXME how to handle this?
				Window.alert("Save or cancel current edit first.");
				return;
			}
			inEditField.cancelEdit();
		}
		editingFields.clear();
		editingFields.add(field);
		adjustEditControls(field);
		field.startEdit();
	}

	private void adjustEditControls(InlineEditableField fieldOrNull) {
		boolean hide = fieldOrNull == null;
		editControlsDiv.removeFromParent();
		UIObject.setVisible(editControlsDiv, !hide);

		if (!hide) {
			InlineEditableField field = fieldOrNull;
			field.editableField.getElement().getParentElement().appendChild(editControlsDiv);

			// Seem we loose the clickhandlers with dom remove so must re-add each time.
			saveButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					onSave(event);
				}
			});

			cancelButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					onCancel(event);
				}
			});

		}
	}

	private void saveInlineEdit(InlineEditableField field) {
		field.saveEdit();
		lastEditedField = field;
		complteInlineEdit(field);
	}

	private void cancelInlineEdit(InlineEditableField field) {
		field.cancelEdit();
		complteInlineEdit(field);
	}

	private void complteInlineEdit(InlineEditableField field) {
		editingFields.remove(field);

		adjustEditControls(null);
	}

	protected void onSave(ClickEvent e) {
		for (InlineEditableField field : new ArrayList<InlineEditableField>(editingFields)) {
			saveInlineEdit(field);
		}
	}

	protected void onCancel(ClickEvent e) {
		for (InlineEditableField field : new ArrayList<InlineEditableField>(editingFields)) {
			cancelInlineEdit(field);
		}
		driver.edit(task); // Resets the editiable fields
		updateCustomFields(task);
	}

	@Override
	public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
		this.repositoryConfiguration = repositoryConfiguration;
		statusEditor.setRepositoryConfiguration(repositoryConfiguration);
	}

	private void updateAcceptableValues() {
		priority.setAcceptableValues(repositoryConfiguration.getPriorities());
		severity.setAcceptableValues(repositoryConfiguration.getSeverities());
		iteration.setAcceptableValues(repositoryConfiguration.getActiveIterations());
		product.setAcceptableValues(repositoryConfiguration.getProducts());
		component.setAcceptableValues(repositoryConfiguration.getComponents(product.getValue()));
		milestone.setAcceptableValues(repositoryConfiguration.getMilestones(product.getValue()));
		taskType.setAcceptableValues(repositoryConfiguration.getTaskTypes());
	}

	public void setPersonServices(ProjectPersonService personService, Person self) {
		assignee.setSuggestionService(personService);
		assignee.setSelf(self);
		// assignee.addLabel(ownerLabel); ??

		watchers.setSuggestionService(personService);
		watchers.setSelf(self);
		// watchers.addLabel(ccLabel);
	}

	@Override
	public void setCommentText(String text) {
		commentsPanel.setText(text);
	}

	@Override
	public boolean isDirty() {
		return commentsPanel.isDirty() || driver.isDirty() || customFieldDriver.isDirty();
	}

	@Override
	public void reEnterEditMode() {
		startInlineEdit(lastEditedField);
	}
}
