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

import java.util.List;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.profile.web.client.presenter.person.ProjectPersonService;
import com.tasktop.c2c.server.tasks.client.widgets.AbstractEditTaskDisplay;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.KeywordSuggestService;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.Task;

public abstract class AbstractEditTaskPresenter<EditTaskViewType extends AbstractEditTaskDisplay> extends
		AbstractTaskPresenter {

	protected final EditTaskViewType editTaskView;
	protected String projectIdentifier;
	protected RepositoryConfiguration repositoryConfiguration;
	protected Task task;
	protected boolean ownerSet = false;

	public AbstractEditTaskPresenter(EditTaskViewType view) {
		super(view.asWidget());
		editTaskView = view;
		addClickHandlers();
	}

	@Override
	protected void bind() {

		editTaskView.setSelf(ProfileGinjector.get.instance().getAppState().getSelf());

		KeywordSuggestService keywordService = new KeywordSuggestService(projectIdentifier);
		editTaskView.setKeywordService(keywordService);

		ProjectPersonService personService = new ProjectPersonService(projectIdentifier);
		editTaskView.setPersonService(personService);
		editTaskView.setProjectIdentifier(projectIdentifier);
		configure(repositoryConfiguration);
		ownerSet = false;
		fetchTask();
	}

	protected abstract void fetchTask();

	protected void productChanged(ValueChangeEvent<Product> event) {
		Product product = event.getValue();

		List<Component> components = repositoryConfiguration.getComponents(product);
		editTaskView.setComponents(components);
		editTaskView.setMilestones(repositoryConfiguration.getMilestones(product));
		Product product2 = repositoryConfiguration.getProducts().get(
				repositoryConfiguration.getProducts().indexOf(product));
		editTaskView.setSelectedMilestone(product2.getDefaultMilestone());
		editTaskView.clearFoundInReleases();

		componentChanged(components.get(0));
	}

	private void componentChanged(Component selected) {
		if (!ownerSet) {
			editTaskView.setAssignee(selected.getInitialOwner());
		}
	}

	protected void addClickHandlers() {
		editTaskView.addProductChangeHandler(new ValueChangeHandler<Product>() {

			@Override
			public void onValueChange(ValueChangeEvent<Product> event) {
				productChanged(event);
			}
		});

		editTaskView.addComponentChangeHandler(new ValueChangeHandler<Component>() {

			@Override
			public void onValueChange(ValueChangeEvent<Component> event) {
				componentChanged(event.getValue());
			}
		});
		editTaskView.addOwnerChangeHandler(new ValueChangeHandler<Person>() {
			@Override
			public void onValueChange(ValueChangeEvent<Person> event) {
				// User touched the owner widget, so we won't be automatically setting it to default component owner
				// anymore
				ownerSet = true;
			}
		});

		editTaskView.getCancelClickHandlers().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				doCancel();
			}
		});

		editTaskView.getSaveClickHandlers().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				doSaveTask();
			}
		});

	}

	protected abstract void doCancel();

	protected abstract void doSaveTask();

	public void configure(RepositoryConfiguration configuration) {
		editTaskView.setRepositoryConfiguration(configuration);
	}

	public void populateUi(Task task) {
		editTaskView.setComponents(repositoryConfiguration.getComponents(task.getProduct()));
		editTaskView.setMilestones(repositoryConfiguration.getMilestones(task.getProduct()));
	}

	protected void populateModel() {
		task = editTaskView.getValue(); // FIXME give this task??
	}

	@Override
	public String mayStop() {
		if (editTaskView.isDirty()) {
			return taskMessages.dirtyNavigateWarning();
		}
		return null;
	}
}
