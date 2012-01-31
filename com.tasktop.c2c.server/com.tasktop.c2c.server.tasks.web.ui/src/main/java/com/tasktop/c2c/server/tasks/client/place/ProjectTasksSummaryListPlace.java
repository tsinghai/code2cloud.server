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
package com.tasktop.c2c.server.tasks.client.place;

import java.util.LinkedHashMap;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.user.client.ui.Hyperlink;
import com.tasktop.c2c.server.common.web.client.navigation.Args;
import com.tasktop.c2c.server.common.web.client.navigation.Path;
import com.tasktop.c2c.server.common.web.client.util.StringUtils;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.web.client.navigation.PageMapping;
import com.tasktop.c2c.server.profile.web.client.place.Breadcrumb;
import com.tasktop.c2c.server.profile.web.client.util.WindowTitleBuilder;
import com.tasktop.c2c.server.profile.web.shared.actions.GetProjectResult;
import com.tasktop.c2c.server.tasks.client.TaskPageMappings;
import com.tasktop.c2c.server.tasks.client.presenters.TasksSummaryListPresenter.ListScope;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.shared.action.GetProductsAction;
import com.tasktop.c2c.server.tasks.shared.action.GetProductsResult;

/**
 * @author Ryan Slobojan (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class ProjectTasksSummaryListPlace extends AbstractProjectTaskBatchingPlace {

	public static final String RELEASE_SUFFIX = "/releases";
	public static final String COMPONENT_SUFFIX = "/components";
	public static final String PRODUCT = "productId";

	public Hyperlink createSummaryLink(String projectIdentifier, String linkLabel, Product product,
			Component component, Milestone milestone) {
		// If our link label is empty, bail on processing since the hyperlink won't be visible anyways
		if (!StringUtils.hasText(linkLabel) || linkLabel.equals("null")) {
			return new Hyperlink("", "");
		}

		String targetHistoryToken = null;

		if (product == null) {
			// If there's no product, the rest don't matter
			targetHistoryToken = ProjectTasksSummaryListPlace.createDefaultListPlace(projectIdentifier).getPrefix();
		} else if (component == null) {
			// If we're at this point, milestone may still be specified - check for it now.
			if (milestone == null) {
				// There's only a product
				targetHistoryToken = ProjectTasksSummaryListPlace.createProductListPlace(projectIdentifier,
						product.getId()).getPrefix();
			} else {
				// There's both a product and a milestone
				targetHistoryToken = ProjectTasksSummaryPlace.createPlaceForRelease(projectIdentifier, product.getId(),
						milestone.getValue()).getPrefix();
			}
		} else if (milestone == null) {
			// If we're at this point, component and product are specified
			targetHistoryToken = ProjectTasksSummaryPlace.createPlaceForComponent(projectIdentifier, product.getId(),
					component.getId()).getPrefix();
		} else {
			// If we're at this point, all 3 are specified
			targetHistoryToken = ProjectTasksSummaryPlace.createPlaceForComponentAndRelease(projectIdentifier,
					product.getId(), component.getId(), milestone.getValue()).getPrefix();
		}
		return new Hyperlink(linkLabel, targetHistoryToken);
	}

	public static class Tokenizer implements PlaceTokenizer<ProjectTasksSummaryListPlace> {

		@Override
		public ProjectTasksSummaryListPlace getPlace(String token) {
			Args pathArgs = PageMapping.getPathArgsForUrl(token);

			String projectIdentifier = pathArgs.getString(Path.PROJECT_ID);
			Integer productId = pathArgs.getInteger(PRODUCT);

			// Check to see which list we're looking for - it could be product, component or release.
			if (token.endsWith(COMPONENT_SUFFIX)) {

				// We have a component summary list.
				return createComponentListPlace(projectIdentifier, productId);
			} else if (token.endsWith(RELEASE_SUFFIX)) {

				// We have a release summary list.
				return createMilestoneListPlace(projectIdentifier, productId);
			} else if (productId != null) {

				// We have a product summary list
				return createProductListPlace(projectIdentifier, productId);
			} else {

				// If we had none of the above, then we want a full summary.
				return createDefaultListPlace(projectIdentifier);
			}
		}

		@Override
		public String getToken(ProjectTasksSummaryListPlace place) {
			return place.getToken();
		}
	}

	public static ProjectTasksSummaryListPlace createDefaultListPlace(String projectIdentifier) {
		return new ProjectTasksSummaryListPlace(projectIdentifier, null, ListScope.None);
	}

	public static ProjectTasksSummaryListPlace createProductListPlace(String projectIdentifier, Integer productId) {
		return new ProjectTasksSummaryListPlace(projectIdentifier, productId, ListScope.Product);
	}

	public static ProjectTasksSummaryListPlace createMilestoneListPlace(String projectIdentifier, Integer productId) {
		return new ProjectTasksSummaryListPlace(projectIdentifier, productId, ListScope.Milestone);
	}

	public static ProjectTasksSummaryListPlace createComponentListPlace(String projectIdentifier, Integer productId) {
		return new ProjectTasksSummaryListPlace(projectIdentifier, productId, ListScope.Component);
	}

	private Integer productId;
	private ListScope scope;
	private List<Product> products;
	private Project project;
	private List<Breadcrumb> breadcrumbs;

	private ProjectTasksSummaryListPlace(String projectId, Integer productId, ListScope scope) {
		super(projectId);
		this.productId = productId;
		this.scope = scope;
	}

	public Integer getProductId() {
		return productId;
	}

	public ListScope getScope() {
		return scope;
	}

	public List<Product> getProducts() {
		return products;
	}

	@Override
	public String getToken() {
		return "";
	}

	@Override
	public String getPrefix() {
		LinkedHashMap<String, String> tokenMap = new LinkedHashMap<String, String>();

		tokenMap.put(Path.PROJECT_ID, projectId);

		if (scope != ListScope.None) {
			// Every list has a product ID except for the unscoped one.
			tokenMap.put(PRODUCT, String.valueOf(productId));
		}

		String url = TaskPageMappings.ProjectTaskSummaryList.getUrlForNamedArgs(tokenMap);

		switch (scope) {
		case Component:
			url += COMPONENT_SUFFIX;
			break;
		case Milestone:
			url += RELEASE_SUFFIX;
			break;
		}

		return url;
	}

	@Override
	protected void addActions(List<Action<?>> actions) {
		super.addActions(actions);
		actions.add(new GetProductsAction(projectId));
	}

	@Override
	protected void handleBatchResults() {
		super.handleBatchResults();
		project = getResult(GetProjectResult.class).get();
		products = getResult(GetProductsResult.class).get();
		createBreadcrumbs(project);
		onPlaceDataFetched();
	}

	@Override
	public String getHeading() {
		return project.getName();
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	private void createBreadcrumbs(Project project) {
		breadcrumbs = Breadcrumb.getProjectSpecficBreadcrumbs(project);
		breadcrumbs.add(new Breadcrumb(ProjectTasksPlace.createDefaultPlace(project.getIdentifier()).getHistoryToken(),
				"Tasks"));
	}

	/**
	 * @return the project
	 */
	public Project getProject() {
		return project;
	}

	@Override
	public String getWindowTitle() {
		return WindowTitleBuilder.createWindowTitle("Task Summary", project.getName());
	}
}
