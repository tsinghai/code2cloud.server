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

import java.util.List;


import com.google.gwt.user.client.ui.IsWidget;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.profile.web.client.ClientCallback;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

public interface IProjectAdminTasksView<T extends IProjectAdminTasksView.Presenter> extends IsWidget {

	void setPresenter(T presenter);

	public static interface Presenter {
	}

	public static interface ProjectAdminTasksPresenter extends ProjectAdminTasksDisplayPresenter,
			ProjectAdminTasksEditPresenter, ProjectAdminTasksMenuPresenter {
	}

	public static interface ProjectAdminTasksDisplayPresenter extends Presenter {
		void onDeleteProduct();

		void onEditProduct();

		Product getProduct();
	}

	public static interface ProjectAdminTasksEditPresenter extends Presenter {
		void onSaveProduct(ErrorCapableView errorView);

		void onEditCancel();

		boolean isEditing();

		Product getProduct();

		List<TaskUserProfile> getUsers();

		Milestone createNewTransientMilestone(List<Milestone> milestones);

		Component createNewTransientComponent(List<Component> components);

		void deleteComponent(Integer componentId, ClientCallback<Void> callback);

		void deleteMilestone(Integer milestoneId, ClientCallback<Void> callback);
	}

	public static interface ProjectAdminTasksMenuPresenter extends Presenter {
		List<Product> getProducts();

		void selectProduct(Integer productId);

		Product getProduct();

		void addProduct();

		boolean isEditing();
	}

}
