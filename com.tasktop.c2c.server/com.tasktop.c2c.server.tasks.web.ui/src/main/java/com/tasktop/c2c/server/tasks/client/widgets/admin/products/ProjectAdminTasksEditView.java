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
package com.tasktop.c2c.server.tasks.client.widgets.admin.products;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.adapters.EditorSource;
import com.google.gwt.editor.client.adapters.ListEditor;
import com.google.gwt.editor.client.adapters.SimpleEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.common.web.client.view.CellTableResources;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.profile.web.client.ClientCallback;
import com.tasktop.c2c.server.profile.web.client.CustomActionCell;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.TextBoxCell;
import com.tasktop.c2c.server.profile.web.client.ValidatingTextBox;
import com.tasktop.c2c.server.profile.web.client.ValidationUtils;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

public class ProjectAdminTasksEditView extends Composite implements Editor<Product>,
		IProjectAdminTasksView<IProjectAdminTasksView.ProjectAdminTasksEditPresenter>, ErrorCapableView {
	interface ProjectAdminTasksEditViewUiBinder extends UiBinder<HTMLPanel, ProjectAdminTasksEditView> {
	}

	interface Driver extends SimpleBeanEditorDriver<Product, ProjectAdminTasksEditView> {
	}

	class ComponentEditor implements Editor<Component> {
		SimpleEditor<String> nameEditor = SimpleEditor.of();
		SimpleEditor<String> descriptionEditor = SimpleEditor.of();
		SimpleEditor<TaskUserProfile> initialOwnerEditor = SimpleEditor.of();
	}

	class MilestoneEditor implements Editor<Milestone> {
		SimpleEditor<String> valueEditor = SimpleEditor.of();
		SimpleEditor<Short> sortkeyEditor = SimpleEditor.of();
	}

	private static ProjectAdminTasksEditView instance;
	private static Driver driver = GWT.create(Driver.class);

	public static ProjectAdminTasksEditView getInstance() {
		if (instance == null) {
			instance = new ProjectAdminTasksEditView();
		}
		return instance;
	}

	private static ProjectAdminTasksEditViewUiBinder ourUiBinder = GWT.create(ProjectAdminTasksEditViewUiBinder.class);
	@UiField
	Button lowerCancelButton;
	@UiField
	Button lowerSaveButton;
	@UiField
	Button upperSaveButton;
	@UiField
	Button upperCancelButton;
	@UiField
	@Path("name")
	ValidatingTextBox productName;
	@UiField
	@Path("description")
	TextArea productDescription;
	@UiField(provided = true)
	@Path("defaultMilestone")
	ValueListBox<Milestone> productDefaultRelease = new ValueListBox<Milestone>(new Renderer<Milestone>() {

		@Override
		public String render(Milestone object) {
			if (object == null) {
				return "";
			}
			return object.getValue();
		}

		@Override
		public void render(Milestone object, Appendable appendable) throws IOException {
			appendable.append(render(object));
		}
	}, new ProvidesKey<Milestone>() {
		@Override
		public Object getKey(Milestone item) {
			return item.getId();
		}
	});

	@UiField(provided = true)
	CellTable<Component> componentsTable;

	@UiField(provided = true)
	CellTable<Milestone> milestoneTable;
	@UiField
	HTMLPanel releasesSection;
	@UiField
	HTMLPanel componentsSection;
	@UiField
	HTMLPanel defaultReleaseSection;

	@Path("components")
	ListEditor<Component, ComponentEditor> componentsEditor;

	@Path("milestones")
	ListEditor<Milestone, MilestoneEditor> milestonesEditor;

	private ProjectAdminTasksEditPresenter presenter;

	private static Template template = GWT.create(Template.class);

	private List<String> users = new ArrayList<String>();
	private ListDataProvider<Component> componentListDataProvider = new ListDataProvider<Component>();
	private Integer lastProductSelectedId = null;

	private ProjectAdminTasksEditView() {
		createComponentsTable();
		createReleasesTable();
		initWidget(ourUiBinder.createAndBindUi(this));
		productName.setErrorMap(errorTable);
		componentsEditor = ListEditor.of(new EditorSource<ComponentEditor>() {
			@Override
			public ComponentEditor create(int index) {
				return new ComponentEditor();
			}

		});
		milestonesEditor = ListEditor.of(new EditorSource<MilestoneEditor>() {
			@Override
			public MilestoneEditor create(int index) {
				return new MilestoneEditor();
			}
		});
		driver.initialize(this);
	}

	public void setPresenter(ProjectAdminTasksEditPresenter presenter) {
		this.presenter = presenter;
		if (lastProductSelectedId == null) {
			lastProductSelectedId = presenter.getProduct().getId();
		}
		if (!lastProductSelectedId.equals(presenter.getProduct().getId())) {
			clearErrors();
		}
		if (presenter.getProduct().getId() < 0) {
			defaultReleaseSection.setVisible(false);
			componentsSection.setVisible(false);
			releasesSection.setVisible(false);
		} else {
			defaultReleaseSection.setVisible(true);
			componentsSection.setVisible(true);
			releasesSection.setVisible(true);
		}
		productName.setDomainObject(presenter.getProduct());
		driver.edit(presenter.getProduct());
		// In order to maintain the list if we choose not to save the product we have to duplicate it
		componentsTable.setRowData(componentsEditor.getList());
		milestoneTable.setRowData(milestonesEditor.getList());
		users = new ArrayList<String>();
		users.add("Select an Owner");
		for (TaskUserProfile user : presenter.getUsers()) {
			users.add(user.getRealname());
		}

		redraw();
	}

	public void redraw() {
		// Make sure we have the latest data.
		productDefaultRelease.setAcceptableValues(presenter.getProduct().getMilestones());
		componentsTable.setRowData(componentsEditor.getList());
		milestoneTable.setRowData(milestonesEditor.getList());

		productName.redraw();
		componentsTable.redraw();
		milestoneTable.redraw();
	}

	// This is used to keep track of any validation errors which are received from the underlying service.
	private Map<String, String> errorTable = new HashMap<String, String>();

	@Override
	public void clearErrors() {
		// Clear out our error map, and then refresh the UI to remove all of the error messages.
		errorTable.clear();
	}

	@Override
	public void displayError(String message) {
		this.displayErrors(Collections.singletonList(message));
	}

	@Override
	public void displayErrors(List<String> messages) {

		// First, clear our our existing errors.
		clearErrors();

		// Keep track of our unhandled error messages so we can deal with them later.
		List<String> unhandledMessages = new ArrayList<String>();

		for (String curMsg : messages) {

			// Check if we have a custom error message
			if (ValidationUtils.isCustomErrorMessage(curMsg)) {
				ValidationUtils.addErrorToMap(curMsg, errorTable);
			} else {
				// Doesn't fit the pattern.
				unhandledMessages.add(curMsg);
			}
		}

		// Kick our product name box to get it to render in case there are errors.
		redraw();

		if (unhandledMessages.size() > 0) {
			// If some of these messages weren't our custom errors, pass them through for the standard rendering
			// treatment.
			ProfileGinjector.get.instance().getNotifier()
					.displayMessage(Message.createErrorMessage(StringUtils.concatenate(unhandledMessages)));
		}
	}

	private void createReleasesTable() {
		milestoneTable = new CellTable<Milestone>(10, resources);
		milestoneTable.setWidth("320px", true);
		CustomActionCell<String> moveReleaseUpCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						return SafeHtmlUtils.fromSafeConstant("<span class=\"order-control\"><a class=\"up\"/></span>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(Cell.Context object) {
						int index = object.getIndex();
						if (index > 0) {
							milestonesEditor.getList().get(index).setSortkey((short) (index - 1));
							milestonesEditor.getList().get(index - 1).setSortkey((short) index);
							Collections.sort(milestonesEditor.getList());
							milestoneTable.setRowData(milestonesEditor.getList());
						}
					}
				});
		Column<Milestone, String> moveReleaseUpColumn = new Column<Milestone, String>(moveReleaseUpCell) {

			@Override
			public String getValue(Milestone object) {
				return null;
			}

			@Override
			public void render(Cell.Context context, Milestone object, SafeHtmlBuilder sb) {
				if (context.getIndex() != 0) {
					super.render(context, object, sb);
				}
			}
		};
		milestoneTable.addColumn(moveReleaseUpColumn);
		milestoneTable.setColumnWidth(moveReleaseUpColumn, 22, Unit.PX);

		CustomActionCell<String> moveReleaseDownCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						return SafeHtmlUtils.fromSafeConstant("<span class=\"order-control\"><a class=\"down\"/></span>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(Cell.Context object) {
						Short index = (short) object.getIndex();
						if (index < milestonesEditor.getList().size() - 1) {
							milestonesEditor.getList().get(index).setSortkey((short) (index + 1));
							milestonesEditor.getList().get(index + 1).setSortkey(index);
							Collections.sort(milestonesEditor.getList());
							milestoneTable.setRowData(milestonesEditor.getList());
						}
					}
				});
		Column<Milestone, String> moveReleaseDownColumn = new Column<Milestone, String>(moveReleaseDownCell) {

			@Override
			public String getValue(Milestone object) {
				return null;
			}

			@Override
			public void render(Cell.Context context, Milestone object, SafeHtmlBuilder sb) {
				if (context.getIndex() != milestonesEditor.getList().size() - 1) {
					super.render(context, object, sb);
				}
			}
		};
		milestoneTable.addColumn(moveReleaseDownColumn);
		milestoneTable.setColumnWidth(moveReleaseDownColumn, 22, Unit.PX);

		CustomActionCell<String> moveReleaseToTopCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						return SafeHtmlUtils.fromSafeConstant("<span class=\"order-control\"><a class=\"top\"/></span>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(Cell.Context object) {
						int index = object.getIndex();
						Milestone topRelease = milestonesEditor.getList().get(index);
						milestonesEditor.getList().remove(topRelease);
						milestonesEditor.getList().add(0, topRelease);
						int i = 0;
						for (Milestone milestone : milestonesEditor.getList()) {
							milestone.setSortkey((short) i);
							i++;
						}
						Collections.sort(milestonesEditor.getList());
						milestoneTable.setRowData(milestonesEditor.getList());
					}
				});
		Column<Milestone, String> moveReleaseToTopColumn = new Column<Milestone, String>(moveReleaseToTopCell) {

			@Override
			public String getValue(Milestone object) {
				return null;
			}

			@Override
			public void render(Cell.Context context, Milestone object, SafeHtmlBuilder sb) {
				if (context.getIndex() != 0) {
					super.render(context, object, sb);
				}
			}
		};
		milestoneTable.addColumn(moveReleaseToTopColumn);
		milestoneTable.setColumnWidth(moveReleaseToTopColumn, 22, Unit.PX);

		Column<Milestone, String> releaseVersionColumn = addColumn(new TextBoxCell(new TextBoxCell.TemplateDelegate() {
			@Override
			public SafeHtml getHtml(Context context, String string) {
				SafeHtml releaseVerHtml = template.releaseVersion(string);
				String fieldName = "value";

				if (ValidationUtils.hasError(context.getKey(), fieldName, errorTable)) {
					String errorMsg = ValidationUtils.getErrorMessage(context.getKey(), fieldName, errorTable);

					return template.errorWrapper(releaseVerHtml, SafeHtmlUtils.fromString(errorMsg));
				} else {
					return releaseVerHtml;
				}
			}
		}), new GetValue<Milestone, String>() {
			@Override
			public String getValue(Milestone object) {
				return milestonesEditor.getEditors().get(milestonesEditor.getList().indexOf(object)).valueEditor
						.getValue();
			}
		});
		releaseVersionColumn.setFieldUpdater(new FieldUpdater<Milestone, String>() {
			@Override
			public void update(int index, Milestone object, String value) {
				Milestone currentDefaultMilestone = productDefaultRelease.getValue();
				for (Milestone milestone : presenter.getProduct().getMilestones()) {
					if (milestone.getId().equals(object.getId())) {
						milestone.setValue(value);
						break;
					}
				}
				for (Milestone milestone : presenter.getProduct().getMilestones()) {
					if (milestone.getId().equals(currentDefaultMilestone.getId())) {
						productDefaultRelease.asEditor().getValue().setValue(value);
						break;
					}
				}

				milestonesEditor.getEditors().get(index).valueEditor.setValue(value);
				productDefaultRelease.setAcceptableValues(presenter.getProduct().getMilestones());
			}
		});
		milestoneTable.addColumn(releaseVersionColumn);
		milestoneTable.setColumnWidth(releaseVersionColumn, 250, Unit.PX);

		CustomActionCell<String> removeReleaseCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						final Milestone referenced = milestonesEditor.getList().get(context.getIndex());
						if (referenced != null && "---".equals(referenced.getValue())) {
							return SafeHtmlUtils.fromSafeConstant("<a class=\"delete-disabled\"><span/></a>");
						}
						return SafeHtmlUtils.fromSafeConstant("<a class=\"misc-icon cancel\"><span/></a>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(final Cell.Context object) {
						final Milestone toRemove = milestonesEditor.getList().get(object.getIndex());
						if (toRemove == null) {
							return;
						}
						if ("---".equals(toRemove.getValue())) {
							return;
						}
						presenter.deleteMilestone(toRemove.getId(), new ClientCallback<Void>() {
							@Override
							public void onReturn(Void aVoid) {
								for (Milestone milestone : presenter.getProduct().getMilestones()) {
									if (milestone.getId().equals(toRemove.getId())) {
										presenter.getProduct().getMilestones().remove(milestone);
										break;
									}
								}
								productDefaultRelease.setAcceptableValues(presenter.getProduct().getMilestones());
								milestonesEditor.getList().remove(toRemove);
								milestoneTable.setRowData(milestonesEditor.getList());
							}
						});
					}
				});
		Column<Milestone, String> removeReleaseColumn = new Column<Milestone, String>(removeReleaseCell) {

			@Override
			public String getValue(Milestone object) {
				return null;
			}

			@Override
			public void render(Cell.Context context, Milestone object, SafeHtmlBuilder sb) {
				if (milestoneTable.getRowCount() == 1) {
					sb.appendHtmlConstant("<a><span/></a>");
				} else {
					super.render(context, object, sb);
				}
			}
		};
		milestoneTable.addColumn(removeReleaseColumn);
		milestoneTable.setColumnWidth(removeReleaseColumn, 30, Unit.PX);

		CustomActionCell<String> addReleaseCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						return SafeHtmlUtils.fromSafeConstant("<a class=\"misc-icon add right\"><span/></a>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(Cell.Context object) {
						Milestone newMilestone = presenter.createNewTransientMilestone(milestonesEditor.getList());
						milestonesEditor.getList().add(newMilestone);
						milestoneTable.setRowData(milestonesEditor.getList());
						presenter.getProduct().getMilestones()
								.add(ProjectAdminTasksUtil.duplicateMilestone(newMilestone));
						productDefaultRelease.setAcceptableValues(presenter.getProduct().getMilestones());
					}
				});
		Column<Milestone, String> addReleaseColumn = new Column<Milestone, String>(addReleaseCell) {
			@Override
			public String getValue(Milestone object) {
				return null;
			}

			@Override
			public void render(Cell.Context context, Milestone object, SafeHtmlBuilder sb) {
				if (context.getIndex() == milestoneTable.getRowCount() - 1) {
					super.render(context, object, sb);
				} else {
					sb.appendHtmlConstant("<a><span/></a>");
				}
			}
		};
		milestoneTable.addColumn(addReleaseColumn);
		milestoneTable.setColumnWidth(addReleaseColumn, 30, Unit.PX);
	}

	private void createComponentsTable() {
		componentsTable = new CellTable<Component>(10, resources);
		componentListDataProvider.addDataDisplay(componentsTable);
		componentsTable.setWidth("540px", true);
		Column<Component, String> nameColumn = addColumn(new TextBoxCell(new TextBoxCell.TemplateDelegate() {
			@Override
			public SafeHtml getHtml(Context context, String string) {

				SafeHtml componentNameHtml = template.componentName(string);
				String fieldName = "name";

				if (ValidationUtils.hasError(context.getKey(), fieldName, errorTable)) {
					String errorMsg = ValidationUtils.getErrorMessage(context.getKey(), fieldName, errorTable);

					return template.errorWrapper(componentNameHtml, SafeHtmlUtils.fromString(errorMsg));
				} else {
					return componentNameHtml;
				}
			}
		}), new GetValue<Component, String>() {
			@Override
			public String getValue(Component object) {
				return componentsEditor.getEditors().get(componentsEditor.getList().indexOf(object)).nameEditor
						.getValue();
			}
		});
		nameColumn.setFieldUpdater(new FieldUpdater<Component, String>() {
			@Override
			public void update(int index, Component object, String value) {
				componentsEditor.getEditors().get(index).nameEditor.setValue(value);
			}
		});
		componentsTable.addColumn(nameColumn, SafeHtmlUtils.fromSafeConstant("<h4>Name</h4>"));
		componentsTable.setColumnWidth(nameColumn, 250, Unit.PX);
		Column<Component, String> descriptionColumn = addColumn(new TextBoxCell(new TextBoxCell.TemplateDelegate() {
			@Override
			public SafeHtml getHtml(Context context, String string) {
				SafeHtml componentDescHtml = template.componentDescription(string);
				String fieldName = "description";

				if (ValidationUtils.hasError(context.getKey(), fieldName, errorTable)) {
					String errorMsg = ValidationUtils.getErrorMessage(context.getKey(), fieldName, errorTable);

					return template.errorWrapper(componentDescHtml, SafeHtmlUtils.fromString(errorMsg));
				} else {
					return componentDescHtml;
				}
			}
		}), new GetValue<Component, String>() {
			@Override
			public String getValue(Component object) {
				return componentsEditor.getEditors().get(componentsEditor.getList().indexOf(object)).descriptionEditor
						.getValue();
			}
		});
		descriptionColumn.setFieldUpdater(new FieldUpdater<Component, String>() {
			@Override
			public void update(int index, Component object, String value) {
				componentsEditor.getEditors().get(index).descriptionEditor.setValue(value);
			}
		});
		componentsTable.addColumn(descriptionColumn, SafeHtmlUtils.fromSafeConstant("<h4>Description</h4>"));
		componentsTable.setColumnWidth(descriptionColumn, 250, Unit.PX);
		Column<Component, String> personColumn = addColumn(new CustomSelectionCell(users) {

			@Override
			public void render(Context context, String value, SafeHtmlBuilder sb) {
				setOptions(users);
				super.render(context, value, sb);
			}
		}, new GetValue<Component, String>() {
			@Override
			public String getValue(Component object) {
				TaskUserProfile value = componentsEditor.getEditors().get(componentsEditor.getList().indexOf(object)).initialOwnerEditor
						.getValue();
				if (value == null) {
					return null;
				}
				return value.getRealname();
			}
		});
		personColumn.setFieldUpdater(new FieldUpdater<Component, String>() {
			@Override
			public void update(int index, Component object, String value) {
				boolean foundUser = false;
				for (TaskUserProfile user : presenter.getUsers()) {
					if (user.getRealname().equals(value)) {
						componentsEditor.getEditors().get(index).initialOwnerEditor.setValue(user);
						foundUser = true;
						break;
					}
				}
				if (!foundUser) {
					componentsEditor.getEditors().get(index).initialOwnerEditor.setValue(null);
				}
			}
		});

		componentsTable.addColumn(personColumn, SafeHtmlUtils.fromSafeConstant("<h4>Owner</h4>"));
		componentsTable.setColumnWidth(personColumn, 210, Unit.PX);

		CustomActionCell<String> removeComponentCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						return SafeHtmlUtils.fromSafeConstant("<a class=\"misc-icon cancel\"><span/></a>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(final Cell.Context object) {
						presenter.deleteComponent(componentsEditor.getList().get(object.getIndex()).getId(),
								new ClientCallback<Void>() {
									@Override
									public void onReturn(Void aVoid) {
										componentsEditor.getList().remove(object.getIndex());
										componentsTable.setRowData(componentsEditor.getList());
									}
								});
					}
				});
		Column<Component, String> removeComponentColumn = new Column<Component, String>(removeComponentCell) {

			@Override
			public String getValue(Component object) {
				return null;
			}

			@Override
			public void render(Cell.Context context, Component object, SafeHtmlBuilder sb) {
				if (componentsTable.getRowCount() == 1) {
					sb.appendHtmlConstant("<a><span/></a>");
				} else {
					super.render(context, object, sb);
				}
			}
		};
		componentsTable.addColumn(removeComponentColumn);
		componentsTable.setColumnWidth(removeComponentColumn, 30, Unit.PX);

		CustomActionCell<String> addComponentCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						return SafeHtmlUtils.fromSafeConstant("<a class=\"misc-icon add right\"><span/></a>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(Cell.Context object) {
						componentsEditor.getList().add(
								presenter.createNewTransientComponent(componentsEditor.getList()));
						componentsTable.setRowData(componentsEditor.getList());
					}
				});
		Column<Component, String> addComponentColumn = new Column<Component, String>(addComponentCell) {
			@Override
			public String getValue(Component object) {
				return null;
			}

			@Override
			public void render(Cell.Context context, Component object, SafeHtmlBuilder sb) {
				if (context.getIndex() == componentsTable.getRowCount() - 1) {
					super.render(context, object, sb);
				} else {
					sb.appendHtmlConstant("<a><span/></a>");
				}
			}
		};
		componentsTable.addColumn(addComponentColumn);
		componentsTable.setColumnWidth(addComponentColumn, 25, Unit.PX);
	}

	@UiHandler({ "upperSaveButton", "lowerSaveButton" })
	void onSave(ClickEvent event) {
		if (presenter != null) {
			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
				@Override
				public void execute() {
					// Push any changes from the editors into the object tree.
					driver.flush();

					presenter.onSaveProduct(ProjectAdminTasksEditView.this);
				}
			});
		}
	}

	@UiHandler({ "upperCancelButton", "lowerCancelButton" })
	void onEditCancel(ClickEvent event) {
		if (presenter != null) {
			presenter.onEditCancel();
			clearErrors();
		}
	}

	private <T, C> Column<T, C> addColumn(Cell<C> cell, final GetValue<T, C> getter) {
		return new Column<T, C>(cell) {
			@Override
			public C getValue(T object) {
				return getter.getValue(object);
			}
		};
	}

	private static interface GetValue<T, V> {
		V getValue(T object);
	}

	static interface Template extends SafeHtmlTemplates {
		@Template("<input class=\"text\" name=\"component-name[]\" type=\"text\" placeholder=\"Name\" value=\"{0}\"/>")
		SafeHtml componentName(String name);

		@Template("<input class=\"text\" name=\"component-description[]\" type=\"text\" placeholder=\"Description\" value=\"{0}\"/>")
		SafeHtml componentDescription(String description);

		@Template("<input class=\"text\" name=\"releases[]\" type=\"text\" placeholder=\"Release Version\" value=\"{0}\"/>")
		SafeHtml releaseVersion(String description);

		@Template("<div class=\"errorLabelWrapper\">{0}<div class=\"errorLabel\">{1}</div></div>")
		SafeHtml errorWrapper(SafeHtml itemToBeWrapped, SafeHtml errorMessage);
	}

	CellTableResources resources = CellTableResources.get.resources;

}
