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
package com.tasktop.c2c.server.profile.web.ui.client.view.components.account.authentication;


import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.tasktop.c2c.server.common.web.client.view.CellTableResources;
import com.tasktop.c2c.server.profile.domain.project.SshPublicKey;
import com.tasktop.c2c.server.profile.web.client.ClientCallback;
import com.tasktop.c2c.server.profile.web.client.CustomActionCell;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.IAccountView;

public class AuthenticationView extends Composite implements IAccountView<IAccountView.AccountAuthenticationPresenter> {

	interface AuthenticationViewUiBinder extends UiBinder<HTMLPanel, AuthenticationView> {
	}

	interface Template extends SafeHtmlTemplates {

		@Template("<div class=\"left lock misc-icon my-ssh-key\"><span></span><a style=\"cursor:pointer\">{0}</a></div>")
		SafeHtml sshKeyName(String name);
	}

	private static AuthenticationView instance;

	public static AuthenticationView getInstance() {
		if (instance == null) {
			instance = new AuthenticationView();
		}
		return instance;
	}

	private static AuthenticationViewUiBinder ourUiBinder = GWT.create(AuthenticationViewUiBinder.class);
	private static Template template = GWT.create(Template.class);

	@UiField
	PasswordTextBox oldPasswordField;
	@UiField
	PasswordTextBox newPasswordField;
	@UiField
	PasswordTextBox confirmNewPasswordField;
	@UiField
	Button cancelChangePasswordButton;
	@UiField
	Button saveChangePasswordButton;
	@UiField
	Anchor linkGitHubButton;
	@UiField
	Anchor addSshKeyButton;
	@UiField
	Anchor changePasswordAnchor;
	@UiField
	HTMLPanel changePasswordPanel;
	@UiField(provided = true)
	public FormPanel githubForm;

	@UiField(provided = true)
	CellTable<SshPublicKey> sshKeyTable;

	private AccountAuthenticationPresenter presenter;

	public AuthenticationView() {
		createSshKeyTable();
		// Give our Github form a target of "_self" - that will ensure that it replaces the current page when the
		// redirect to GitHub happens (which is what we want).
		githubForm = new FormPanel(new NamedFrame("_self"));
		initWidget(ourUiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(AccountAuthenticationPresenter presenter) {
		this.presenter = presenter;
		if (presenter.getProfile().getGithubUsername() != null
				&& !presenter.getProfile().getGithubUsername().trim().isEmpty()) {
			// There's a GitHub username, change the form to allow for delete of the link.
			((Panel) this.githubForm.getWidget()).add(new Hidden("_method", "DELETE"));
			linkGitHubButton.setText("Remove GitHub link for " + presenter.getProfile().getGithubUsername());
		}
		sshKeyTable.setRowData(presenter.getSshKeys());
		resetPasswords();
		EditSshKeyDialog.getInstance().hide();
	}

	private void resetPasswords() {
		oldPasswordField.setText(null);
		newPasswordField.setText(null);
		confirmNewPasswordField.setText(null);
	}

	private void createSshKeyTable() {
		sshKeyTable = new CellTable<SshPublicKey>(10, CellTableResources.get.resources);
		sshKeyTable.setTableLayoutFixed(true);
		CustomActionCell<String> nameCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						return template.sshKeyName(presenter.getSshKeys().get(context.getIndex()).getName());
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(Cell.Context context) {
						SshPublicKey toEdit = presenter.getSshKeys().get(context.getIndex());
						presenter.selectSshKey(toEdit);
						EditSshKeyDialog.getInstance().setPresenter(presenter);
					}
				});
		Column<SshPublicKey, String> nameColumn = new Column<SshPublicKey, String>(nameCell) {
			@Override
			public String getValue(SshPublicKey object) {
				return object.getName();
			}
		};
		sshKeyTable.addColumn(nameColumn);
		sshKeyTable.setColumnWidth(nameColumn, 200, Style.Unit.PX);
		CustomActionCell<String> removeKeyCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						return SafeHtmlUtils.fromSafeConstant("<div class=\"left\"><a style=\"cursor:pointer\" class=\" red-link\">Remove</a></div>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(Cell.Context context) {
						SshPublicKey toDelete = presenter.getSshKeys().get(context.getIndex());
						presenter.selectSshKey(toDelete);
						DeleteSshKeyDialog.getInstance().setPresenter(presenter);
					}
				});
		sshKeyTable.addColumn(new Column<SshPublicKey, String>(removeKeyCell) {
			@Override
			public String getValue(SshPublicKey object) {
				return null;
			}
		});
	}

	@UiHandler("addSshKeyButton")
	void addSshKey(ClickEvent event) {
		EditSshKeyDialog.getInstance();
		SshPublicKey sshPublicKey = new SshPublicKey();
		presenter.selectSshKey(sshPublicKey);
		EditSshKeyDialog.getInstance().setPresenter(presenter);
	}

	@UiHandler("linkGitHubButton")
	void onLinkGitHub(ClickEvent event) {
		githubForm.submit();
	}

	@UiHandler("changePasswordAnchor")
	void onChangePassword(ClickEvent event) {
		changePasswordPanel.setVisible(true);
	}

	@UiHandler("cancelChangePasswordButton")
	void onCancelChangePassword(ClickEvent event) {
		changePasswordPanel.setVisible(false);
		resetPasswords();
	}

	@UiHandler("saveChangePasswordButton")
	void onSaveChangedPassword(ClickEvent event) {
		presenter.saveChangedPassword(oldPasswordField.getText(), newPasswordField.getText(),
				confirmNewPasswordField.getText(), new ClientCallback<Boolean>() {
					@Override
					public void onReturn(Boolean success) {
						changePasswordPanel.setVisible(!success);
					}
				});
	}
}
