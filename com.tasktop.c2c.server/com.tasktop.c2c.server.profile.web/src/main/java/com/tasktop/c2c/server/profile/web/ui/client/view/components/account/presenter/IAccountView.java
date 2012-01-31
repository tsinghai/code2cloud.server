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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter;

import java.util.List;


import com.google.gwt.place.shared.Place;
import com.tasktop.c2c.server.common.web.client.view.ErrorCapableView;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKey;
import com.tasktop.c2c.server.profile.web.client.ClientCallback;

public interface IAccountView<P extends IAccountView.Presenter> {

	void setPresenter(P presenter);

	public static interface Presenter {
		void goTo(Place place);

		Place getWhere();

		Profile getProfile();
	}

	public static interface SshKeyPresenter extends Presenter {
		void saveSshKey(ErrorCapableView errorView);

		SshPublicKey getSelectedSshKey();

		void deleteSshKey(Long sshPublicKeyId);
	}

	public static interface AccountProfilePresenter extends Presenter {
		Profile getProfile();

		void saveProfile();

		void verifyEmail();
	}

	public static interface AccountAuthenticationPresenter extends SshKeyPresenter {
		List<SshPublicKey> getSshKeys();

		void saveChangedPassword(String oldPassword, String newPassword, String newPasswordConfirmation,
				ClientCallback<Boolean> callback);

		void selectSshKey(SshPublicKey sshPublicKey);
	}

	public static interface AccountPresenter extends AccountProfilePresenter, AccountAuthenticationPresenter,
			SshKeyPresenter {
	}
}
