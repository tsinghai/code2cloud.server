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
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminIterationsPlace;
import com.tasktop.c2c.server.tasks.client.widgets.admin.iterations.IIterationsAdminView;
import com.tasktop.c2c.server.tasks.client.widgets.admin.iterations.IterationsAdminView;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.shared.action.CreateIterationAction;
import com.tasktop.c2c.server.tasks.shared.action.CreateIterationResult;
import com.tasktop.c2c.server.tasks.shared.action.UpdateIterationAction;
import com.tasktop.c2c.server.tasks.shared.action.UpdateIterationResult;

public class ProjectAdminTaskIterationsActivity extends AbstractTaskPresenter implements IIterationsAdminView.Presenter {

	private String projectIdentifier;
	private List<Iteration> iterations;
	private IIterationsAdminView view;

	public ProjectAdminTaskIterationsActivity() {
		// this(ProjectAdminIterationsView.getInstance());
		this(IterationsAdminView.getInstance());
	}

	/**
	 * @param instance
	 */
	public ProjectAdminTaskIterationsActivity(IIterationsAdminView view) {
		super(view);
		this.view = view;
	}

	public void setPlace(Place aPlace) {
		ProjectAdminIterationsPlace place = (ProjectAdminIterationsPlace) aPlace;
		this.iterations = place.getRepositoryConfiguration().getIterations();
		this.projectIdentifier = place.getProjectIdentifer();
		updateView();

	}

	private void updateView() {
		view.setPresenterAndUpdateDisplay(this);
	}

	@Override
	public void saveIteration(final Iteration toSave) {

		ProfileGinjector.get
				.instance()
				.getDispatchService()
				.execute(new UpdateIterationAction(projectIdentifier, toSave),
						new AsyncCallbackSupport<UpdateIterationResult>() {
							@Override
							protected void success(final UpdateIterationResult result) {
								int oldIdx = iterations.indexOf(result.get());
								iterations.remove(oldIdx);
								iterations.add(oldIdx, result.get());

								updateView();
								ProfileGinjector.get.instance().getNotifier()
										.displayMessage(Message.createSuccessMessage("Iteration saved."));
							}
						});
	}

	private void createIteration(final Iteration toSave) {

		ProfileGinjector.get
				.instance()
				.getDispatchService()
				.execute(new CreateIterationAction(projectIdentifier, toSave),
						new AsyncCallbackSupport<CreateIterationResult>() {
							@Override
							protected void success(final CreateIterationResult result) {
								iterations.add(result.get());

								updateView();
								ProfileGinjector.get
										.instance()
										.getNotifier()
										.displayMessage(
												Message.createSuccessMessage("Created iteration "
														+ result.get().getValue() + "."));
							}
						});
	}

	@Override
	public List<Iteration> getIterations(boolean hideInActive) {
		if (hideInActive) {
			List<Iteration> active = new ArrayList<Iteration>(iterations.size());
			for (Iteration it : iterations) {
				if (it.getIsActive()) {
					active.add(it);
				}
			}
			return active;
		} else {
			return iterations;
		}
	}

	@Override
	protected void bind() {
		//
	}

	@Override
	public void newIteration() {
		Iteration newIteration = new Iteration();
		newIteration.setIsActive(true);
		newIteration.setValue(getNewIterationValue());
		createIteration(newIteration);
	}

	private final RegExp decimalRegexp = RegExp.compile("(\\d+)");

	private String getNewIterationValue() {
		String value = getFirstNewValueAttempt();
		int index = 1;
		while (!isValidValue(value)) {
			value = value + index++;
		}
		return value;
	}

	private boolean isValidValue(String value) {
		for (Iteration it : iterations) {
			if (it.getValue().equals(value)) {
				return false;
			}
		}
		return true;
	}

	private String getFirstNewValueAttempt() {
		if (!iterations.isEmpty()) {
			Iteration lastIteration = iterations.get(iterations.size() - 1);
			String lastValue = lastIteration.getValue();
			MatchResult match = decimalRegexp.exec(lastValue);
			if (match != null) {
				String dec = match.getGroup(1);
				Integer nextDec = Integer.parseInt(dec) + 1;
				return lastValue.replaceFirst(dec, nextDec + "");
			} else {
				return lastValue + "next"; // FIXME do better here.
			}
		} else {
			return "1";
		}
	}

}
