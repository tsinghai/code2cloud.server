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

import java.util.List;
import java.util.ListIterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.profile.web.client.AuthenticationHelper;
import com.tasktop.c2c.server.tasks.client.widgets.TaskComment.ReplyHandler;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.PersonUtil;
import com.tasktop.c2c.server.tasks.client.widgets.wiki.EditWikiPanel;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.Task;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class CommentsPanel extends Composite {

	interface Binder extends UiBinder<Widget, CommentsPanel> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	protected Panel commentsPanel;
	@UiField
	protected EditWikiPanel comment;
	@UiField
	protected HTML commentsHeading;
	@UiField
	protected Label commentersMore;
	@UiField
	protected Panel commentersPanel;
	@UiField
	protected Element commentEditPanel;
	@UiField
	Button postComment;

	private ReplyHandler commentReplyHandler;
	private Person self;

	public CommentsPanel() {
		super.initWidget(uiBinder.createAndBindUi(this));
		this.commentReplyHandler = new TaskComment.TextBoxReplyHandler(comment.getTextArea());
		comment.getTextArea().getElement().setAttribute("placeholder", "Post a comment...");

	}

	public void setValue(Task task) {
		Person reporter = PersonUtil.toPerson(task.getReporter());

		comment.setValue("");
		commentsPanel.clear();
		for (Integer i = 0; i < task.getComments().size(); i++) {
			commentsPanel.add(new TaskComment(task, task.getComments().get(i), i + 1, self, reporter,
					commentReplyHandler, true));
		}

		int MAX_COMMENTERS = 5;

		commentsHeading.addStyleName("left");
		commentsHeading.setHTML("Comments <span> (" + task.getComments().size() + ")</span>");

		if (task.getComments().size() > MAX_COMMENTERS) {
			commentersMore.setText("...");
		} else {
			commentersMore.setText("");
		}
		commentersPanel.clear();
		List<Comment> commentList = task.getComments();
		int commentListSize = commentList.size();
		ListIterator<Comment> iter = commentList.listIterator(commentListSize);
		int idx = 0;
		while (iter.hasPrevious()) {
			Comment c = iter.previous();
			if (idx < MAX_COMMENTERS) {
				TaskCommenter tc = new TaskCommenter(task, c, commentListSize - idx);
				commentersPanel.add(tc);
				idx++;
			} else {
				break;
			}
		}

		boolean canEdit = !AuthenticationHelper.isAnonymous();
		UIObject.setVisible(commentEditPanel, canEdit);
		comment.setEditMode();
	}

	public void populateTask(Task task) {
		if (isDirty()) {
			task.addComment(comment.getValue());
		}
	}

	/**
	 * @param self
	 */
	public void setSelf(Person self) {
		this.self = self;
	}

	public boolean isDirty() {
		return comment.getValue() != null && comment.getValue().length() > 0;
	}

	public String getText() {
		return comment.getValue();
	}

	/**
	 * @param text
	 */
	public void setText(String text) {
		comment.setValue(text);
	}

	/**
	 * @param projectIdentifier
	 */
	public void setProjectIdentifier(String projectIdentifier) {
		comment.setProjectId(projectIdentifier);
	}

}
