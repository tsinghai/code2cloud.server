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
import java.util.Collections;
import java.util.List;


import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.tasktop.c2c.server.common.web.client.notification.Message;
import com.tasktop.c2c.server.common.web.client.presenter.AsyncCallbackSupport;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.common.web.client.view.CommonGinjector;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.profile.web.client.ClientCallback;
import com.tasktop.c2c.server.profile.web.client.ProfileGinjector;
import com.tasktop.c2c.server.tasks.client.place.ProjectAdminProductsPlace;
import com.tasktop.c2c.server.tasks.client.widgets.admin.products.IProjectAdminTasksView;
import com.tasktop.c2c.server.tasks.client.widgets.admin.products.ProjectAdminTasksEditView;
import com.tasktop.c2c.server.tasks.client.widgets.admin.products.ProjectAdminTasksUtil;
import com.tasktop.c2c.server.tasks.client.widgets.admin.products.ProjectAdminTasksView;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.shared.action.CreateProductAction;
import com.tasktop.c2c.server.tasks.shared.action.CreateProductResult;
import com.tasktop.c2c.server.tasks.shared.action.DeleteComponentAction;
import com.tasktop.c2c.server.tasks.shared.action.DeleteComponentResult;
import com.tasktop.c2c.server.tasks.shared.action.DeleteMilestoneAction;
import com.tasktop.c2c.server.tasks.shared.action.DeleteMilestoneResult;
import com.tasktop.c2c.server.tasks.shared.action.DeleteProductAction;
import com.tasktop.c2c.server.tasks.shared.action.DeleteProductResult;
import com.tasktop.c2c.server.tasks.shared.action.GetProductsAction;
import com.tasktop.c2c.server.tasks.shared.action.GetProductsResult;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationAction;
import com.tasktop.c2c.server.tasks.shared.action.GetRepositoryConfigurationResult;
import com.tasktop.c2c.server.tasks.shared.action.UpdateProductTreeAction;
import com.tasktop.c2c.server.tasks.shared.action.UpdateProductTreeResult;

public class ProjectAdminTaskProductsActivity extends AbstractTaskPresenter implements
		IProjectAdminTasksView.ProjectAdminTasksPresenter, SplittableActivity {

	private boolean editing;
	private String projectIdentifier;
	private RepositoryConfiguration repositoryConfiguration;
	private Product selectedProduct;
	private ProjectAdminTasksView view;
	private Product newProduct = null;

	public ProjectAdminTaskProductsActivity() {
		this(ProjectAdminTasksView.getInstance());
	}

	/**
	 * @param instance
	 */
	public ProjectAdminTaskProductsActivity(ProjectAdminTasksView view) {
		super(view);
		this.view = view;
	}

	public void setPlace(Place p) {
		ProjectAdminProductsPlace place = (ProjectAdminProductsPlace) p;
		this.editing = false;
		this.projectIdentifier = place.getProjectIdentifer();
		this.repositoryConfiguration = place.getRepositoryConfiguration();
		updateView();
	}

	private void updateView() {
		// Reload the repository configuration to get latest data
		CommonGinjector.get
				.instance()
				.getDispatchService()
				.execute(new GetRepositoryConfigurationAction(projectIdentifier),
						new AsyncCallbackSupport<GetRepositoryConfigurationResult>() {
							@Override
							protected void success(GetRepositoryConfigurationResult result) {
								repositoryConfiguration = result.get();
								view.setPresenter(ProjectAdminTaskProductsActivity.this);
								// ProjectAdminMenu.getInstance().updateUrls(project);
							}
						});
	}

	@Override
	public void onSaveProduct(ErrorCapableView errorView) {
		// Make a copy of our product for saving, so we can mutate it and not affect our product under edit.
		Product toSave = ProjectAdminTasksUtil.duplicateProduct(selectedProduct);
		if (toSave.getId() == null || toSave.getId() == -1) {
			saveNewProduct(errorView);
			return;
		}

		// wipe out any temporary IDs we might have in the product tree.
		ProjectAdminTasksUtil.neutralizeTemporaryIds(toSave);

		CommonGinjector.get
				.instance()
				.getDispatchService()
				.execute(new UpdateProductTreeAction(projectIdentifier, toSave),
						new AsyncCallbackSupport<UpdateProductTreeResult>(errorView) {
							@Override
							protected void success(UpdateProductTreeResult result) {
								editing = false;

								// Remove any errors that are showing, since we're leaving edit mode.
								ProjectAdminTasksEditView.getInstance().clearErrors();

								CommonGinjector.get
										.instance()
										.getDispatchService()
										.execute(new GetProductsAction(projectIdentifier),
												new AsyncCallbackSupport<GetProductsResult>() {

													@Override
													protected void success(GetProductsResult result) {
														for (Product product : result.get()) {
															if (product.getId().equals(selectedProduct.getId())) {
																selectedProduct = product;
																break;
															}
														}
														repositoryConfiguration.setProducts(result.get());
														ProfileGinjector.get
																.instance()
																.getNotifier()
																.displayMessage(
																		Message.createSuccessMessage("Product saved"));
														updateView();
													}
												});
							}

							@Override
							public void onFailure(Throwable exception) {
								super.onFailure(exception);
								// If a failure occurs, reset the view
								ProjectAdminTasksEditView.getInstance().redraw();
							}
						});
	}

	public void saveNewProduct(final ErrorCapableView errorView) {
		if (selectedProduct.getName() == null || selectedProduct.getName().length() == 0
				|| selectedProduct.getName().equals("")) {
			return;
		}
		final Product toSave = selectedProduct;
		if (toSave.getId() < 0) {
			toSave.setId(null);
		}
		CommonGinjector.get
				.instance()
				.getDispatchService()
				.execute(new CreateProductAction(projectIdentifier, toSave),
						new AsyncCallbackSupport<CreateProductResult>() {
							@Override
							protected void success(final CreateProductResult result) {
								repositoryConfiguration.getProducts().add(result.get());
								selectedProduct = result.get();
								newProduct = null;
								editing = true;
								updateView();
								ProfileGinjector.get.instance().getNotifier()
										.displayMessage(Message.createSuccessMessage("Product saved"));
							}
						});
	}

	@Override
	public void onEditCancel() {
		if (selectedProduct.getId() == -1) {
			newProduct = null;
			selectedProduct = repositoryConfiguration.getDefaultProduct();
		}

		// Remove any errors that are showing, since we're leaving edit mode.
		ProjectAdminTasksEditView.getInstance().clearErrors();

		resetProduct(false);
	}

	private void resetProduct(final boolean editState) {
		CommonGinjector.get.instance().getDispatchService()
				.execute(new GetProductsAction(projectIdentifier), new AsyncCallbackSupport<GetProductsResult>() {
					@Override
					protected void success(GetProductsResult result) {
						repositoryConfiguration.setProducts(result.get());
						Product selected = selectedProduct;
						Product defaultProduct = repositoryConfiguration.getDefaultProduct();
						boolean selectedFound = selected == null;
						boolean defaultFound = defaultProduct == null;
						for (Product product : result.get()) {
							if (!selectedFound && selected.getId() != null && selected.getId().equals(product.getId())) {
								selectedProduct = product;
								selectedFound = true;
							}
							if (!defaultFound && defaultProduct.getId() != null
									&& defaultProduct.getId().equals(product.getId())) {
								repositoryConfiguration.setDefaultProduct(product);
								defaultFound = true;
							}
							if (selectedFound && defaultFound) {
								break;
							}
						}
						editing = editState;
						updateView();
					}
				});
	}

	@Override
	public boolean isEditing() {
		return editing || selectedProduct.getId() == -1;
	}

	@Override
	public List<Product> getProducts() {
		if (repositoryConfiguration.getProducts().size() == 0) {
			newProduct = createNewProduct();
		}
		ArrayList<Product> products = new ArrayList<Product>(repositoryConfiguration.getProducts());
		if (newProduct != null) {
			products.add(newProduct);
		}
		return products;
	}

	@Override
	public void selectProduct(final Integer productId) {

		// First, if we're selecting a new product then clear out any old messages.
		ProfileGinjector.get.instance().getNotifier().clearMessages();

		if (getProduct().getId() == null || productId == null) {
			selectedProduct = newProduct;
			editing = true;
			updateView();
			return;
		}

		// If the selected product is a new product that hasn't been saved then we need to return to editing it
		if (newProduct != null && productId.equals(newProduct.getId())) {
			editing = true;
			selectedProduct = newProduct;
			updateView();
			return;
		}

		if (getProduct().getId().equals(productId)) {
			return;
		}
		for (Product product : repositoryConfiguration.getProducts()) {
			if (product.getId().equals(productId)) {
				selectedProduct = product;
			}
		}
		editing = false;
		updateView();
	}

	@Override
	public Product getProduct() {
		if (selectedProduct == null) {
			selectedProduct = repositoryConfiguration.getDefaultProduct();
		}
		if (selectedProduct == null) {
			selectedProduct = newProduct = createNewProduct();
		}
		if (selectedProduct.getDefaultMilestone() == null) {
			if (selectedProduct.getMilestones().size() == 0) {
				Milestone milestone = createNewTransientMilestone(selectedProduct.getMilestones());
				selectedProduct.setDefaultMilestone(milestone);
			} else {
				selectedProduct.setDefaultMilestone(selectedProduct.getMilestones().get(0));
			}
		}
		if (selectedProduct.getMilestones().size() == 0) {
			selectedProduct.getMilestones().add(selectedProduct.getDefaultMilestone());
		}

		if (selectedProduct.getDefaultComponent() == null) {
			if (selectedProduct.getComponents().size() == 0) {
				selectedProduct.setDefaultComponent(createNewTransientComponent(selectedProduct.getComponents()));
			} else {
				selectedProduct.setDefaultComponent(selectedProduct.getComponents().get(0));
			}
		}
		if (selectedProduct.getComponents().size() == 0) {
			selectedProduct.getComponents().add(selectedProduct.getDefaultComponent());
		}

		Collections.sort(selectedProduct.getMilestones());

		return selectedProduct;
	}

	@Override
	public void addProduct() {
		onCreateProduct();
	}

	public void onCreateProduct() {
		selectedProduct = newProduct = createNewProduct();
		editing = true;
		updateView();
	}

	private String getUniqueProductName(String baseName) {
		String suffix = "";
		int i = 0;
		while (true) {
			boolean found = false;
			for (Product product : repositoryConfiguration.getProducts()) {
				if (product.getName().equals(baseName + suffix)) {
					found = true;
					break;
				}
			}
			if (found) {
				i = i + 1;
				suffix = " " + String.valueOf(i);
			} else {
				return baseName + suffix;
			}
		}
	}

	private Product createNewProduct() {
		Product selectedProduct = new Product();
		selectedProduct.setId(-1);
		selectedProduct.setName(getUniqueProductName("New Product"));
		selectedProduct.setComponents(new ArrayList<Component>());
		selectedProduct.setMilestones(new ArrayList<Milestone>());
		selectedProduct.setDefaultComponent(createNewTransientComponent(selectedProduct.getComponents()));
		selectedProduct.setDefaultMilestone(createNewTransientMilestone(selectedProduct.getMilestones()));
		selectedProduct.getComponents().add(selectedProduct.getDefaultComponent());
		selectedProduct.getMilestones().add(selectedProduct.getDefaultMilestone());
		return selectedProduct;
	}

	@Override
	public List<TaskUserProfile> getUsers() {
		return repositoryConfiguration.getUsers();
	}

	@Override
	public Milestone createNewTransientMilestone(List<Milestone> milestones) {
		Integer i = 0;
		for (Milestone milestone : milestones) {
			if (milestone.getId() < i) {
				i = milestone.getId();
			}
		}
		i = i - 1;
		Milestone newMilestone = new Milestone();
		newMilestone.setId(i);
		newMilestone.setSortkey(Integer.valueOf(milestones.size()).shortValue());
		return newMilestone;
	}

	@Override
	public Component createNewTransientComponent(List<Component> components) {
		Integer i = 0;
		for (Component component : components) {
			if (component.getId() < i) {
				i = component.getId();
			}
		}
		i = i - 1;
		Component newComponent = new Component();
		newComponent.setId(i);
		return newComponent;
	}

	// TODO: Because saving a Product doesn't remove any discarded Components an individual delete operation must be
	// performed. This is really bad - James Tyrrell
	@Override
	public void deleteComponent(final Integer componentId, final ClientCallback<Void> callback) {
		if (componentId < 0) {
			removeComponent(selectedProduct, componentId);
			callback.onReturn(null);
			return;
		}
		if (!Window.confirm("Are you sure you want to delete this component? This operation cannot be undone.")) {
			// Didn't get confirmation, so bail out.
			return;
		}
		final Product toModify = selectedProduct;
		ProfileGinjector.get
				.instance()
				.getDispatchService()
				.execute(new DeleteComponentAction(projectIdentifier, componentId),
						new AsyncCallbackSupport<DeleteComponentResult>() {
							@Override
							protected void success(DeleteComponentResult result) {
								removeComponent(toModify, componentId);
								callback.onReturn(null);
								ProfileGinjector.get.instance().getNotifier()
										.displayMessage(Message.createSuccessMessage("Component deleted"));
							}
						});
	}

	private void removeComponent(Product product, Integer componentId) {
		for (Component component : product.getComponents()) {
			if (component.getId().equals(componentId)) {
				product.getComponents().remove(component);
				break;
			}
		}
	}

	private void removeMilestone(Product product, Integer milestoneId) {
		for (Milestone milestone : product.getMilestones()) {
			if (milestone.getId().equals(milestoneId)) {
				product.getMilestones().remove(milestone);
				break;
			}
		}
	}

	// TODO: Because saving a Product doesn't remove any discarded Milestone an individual delete operation must be
	// performed. This is really bad - James Tyrrell
	@Override
	public void deleteMilestone(final Integer milestoneId, final ClientCallback<Void> callback) {
		if (milestoneId < 0) {
			removeMilestone(selectedProduct, milestoneId);
			callback.onReturn(null);
			return;
		}

		if (!Window.confirm("Are you sure you want to delete this release? This operation cannot be undone.")) {
			// Didn't get confirmation, so bail out.
			return;
		}

		final Product toModify = selectedProduct;
		ProfileGinjector.get
				.instance()
				.getDispatchService()
				.execute(new DeleteMilestoneAction(projectIdentifier, milestoneId),
						new AsyncCallbackSupport<DeleteMilestoneResult>() {
							@Override
							protected void success(DeleteMilestoneResult result) {
								removeMilestone(toModify, milestoneId);
								callback.onReturn(null);
								ProfileGinjector.get.instance().getNotifier()
										.displayMessage(Message.createSuccessMessage("Release deleted"));
							}
						});
	}

	@Override
	public void onDeleteProduct() {

		if (!Window.confirm("Are you sure you want to delete this product? This operation cannot be undone.")) {
			return;
		}

		ProfileGinjector.get
				.instance()
				.getDispatchService()
				.execute(new DeleteProductAction(projectIdentifier, selectedProduct.getId()),
						new AsyncCallbackSupport<DeleteProductResult>() {
							@Override
							protected void success(DeleteProductResult result) {
								repositoryConfiguration.getProducts().remove(selectedProduct);
								if (selectedProduct.getId().equals(repositoryConfiguration.getDefaultProduct().getId())) {
									if (repositoryConfiguration.getProducts().size() > 0) {
										repositoryConfiguration.setDefaultProduct(repositoryConfiguration.getProducts()
												.get(0));
										selectedProduct = repositoryConfiguration.getDefaultProduct();
									} else {
										selectedProduct = newProduct = createNewProduct();
									}
								} else {
									selectedProduct = repositoryConfiguration.getDefaultProduct();
								}
								ProfileGinjector.get.instance().getNotifier()
										.displayMessage(Message.createSuccessMessage("Product deleted"));
								updateView();
							}
						});
	}

	@Override
	public void onEditProduct() {
		resetProduct(true);
	}

	@Override
	protected void bind() {
		// TODO Auto-generated method stub

	}
}
