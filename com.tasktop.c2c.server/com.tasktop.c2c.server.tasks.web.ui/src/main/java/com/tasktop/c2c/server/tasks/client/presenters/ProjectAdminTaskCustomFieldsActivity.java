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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.notification.OperationMessage;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminCustomFieldsPlace;
import com.tasktop.c2c.server.tasks.client.widgets.admin.customfields.CustomFieldsAdminView;
import com.tasktop.c2c.server.tasks.client.widgets.admin.customfields.ICustomFieldsAdminView;
import com.tasktop.c2c.server.tasks.domain.CustomFieldValue;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;
import com.tasktop.c2c.server.tasks.shared.action.CreateCustomFieldAction;
import com.tasktop.c2c.server.tasks.shared.action.CreateCustomFieldResult;
import com.tasktop.c2c.server.tasks.shared.action.DeleteCustomFieldAction;
import com.tasktop.c2c.server.tasks.shared.action.DeleteCustomFieldResult;
import com.tasktop.c2c.server.tasks.shared.action.UpdateCustomFieldAction;
import com.tasktop.c2c.server.tasks.shared.action.UpdateCustomFieldResult;

public class ProjectAdminTaskCustomFieldsActivity extends AbstractTaskPresenter implements
		ICustomFieldsAdminView.Presenter {

	private ICustomFieldsAdminView view;
	private List<FieldDescriptor> fields;

	public ProjectAdminTaskCustomFieldsActivity() {
		this(CustomFieldsAdminView.getInstance());
	}

	/**
	 * @param instance
	 */
	public ProjectAdminTaskCustomFieldsActivity(ICustomFieldsAdminView view) {
		super(view);
		this.view = view;
	}

	public void setPlace(Place aPlace) {
		ProjectAdminCustomFieldsPlace place = (ProjectAdminCustomFieldsPlace) aPlace;
		this.fields = place.getRepositoryConfiguration().getCustomFields();
		this.projectIdentifier = place.getProjectIdentifer();
		updateView();

	}

	private void updateView() {
		view.setPresenterAndUpdateDisplay(this);
	}

	@Override
	protected void bind() {
		//
	}

	@Override
	public List<FieldDescriptor> getCustomFields() {
		return fields;
	}

	@Override
	public void createCustomField(FieldDescriptor newField) {
		getDispatchService().execute(new CreateCustomFieldAction(projectIdentifier, newField),
				new AsyncCallbackSupport<CreateCustomFieldResult>(OperationMessage.create("Creating field")) {

					@Override
					protected void success(CreateCustomFieldResult result) {
						fields.add(result.get());
						getNotifier().displayMessage(Message.createSuccessMessage("Field Created"));
						updateView();
					}
				});
	}

	@Override
	public void updateCustomField(FieldDescriptor newField) {
		getDispatchService().execute(new UpdateCustomFieldAction(projectIdentifier, newField),
				new AsyncCallbackSupport<UpdateCustomFieldResult>(OperationMessage.create("Updating field")) {

					@Override
					protected void success(UpdateCustomFieldResult result) {
						int idx = fields.indexOf(result.get());
						fields.remove(idx);
						fields.add(idx, result.get());
						getNotifier().displayMessage(Message.createSuccessMessage("Field Updated"));
						updateView();
					}
				});
	}

	@Override
	public void deleteCustomField(FieldDescriptor customField) {
		getDispatchService().execute(new DeleteCustomFieldAction(projectIdentifier, customField.getId()),
				new AsyncCallbackSupport<DeleteCustomFieldResult>(OperationMessage.create("Deleteing field")) {

					@Override
					protected void success(DeleteCustomFieldResult result) {
						int idx = -1;
						int i = 0;
						for (FieldDescriptor f : fields) {
							if (f.getId().equals(result.get())) {
								idx = i;
								break;
							}
							i++;
						}
						fields.remove(idx);
						getNotifier().displayMessage(Message.createSuccessMessage("Field Deleted"));
						updateView();
					}
				});

	}

	@Override
	public void edit(FieldDescriptor selectedObject) {
		view.editField(selectedObject);
	}

	@Override
	public void newCustomField() {
		FieldDescriptor newField = new FieldDescriptor();
		newField.setFieldType(FieldType.TEXT);
		newField.setAvailableForNewTasks(true);
		newField.setObsolete(false);
		view.newField(newField);
	}

	@Override
	public void cancelNew() {
		updateView();
	}

	@Override
	public void cancelEdit() {
		updateView();

	}

	@Override
	public List<CustomFieldValue> getNewCustomFieldsValues() {
		List<CustomFieldValue> values = new ArrayList<CustomFieldValue>(1);
		values.add(new CustomFieldValue());
		values.get(0).setSortkey((short) 0);
		values.get(0).setIsActive(true);
		values.get(0).setValue("---");
		return values;
	}
}
