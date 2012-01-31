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

import static com.tasktop.c2c.server.common.web.client.widgets.Format.stringValueDateTime;

import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.tasktop.c2c.server.common.web.client.view.Avatar;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.Person;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.person.PersonDetailPopupPanel;
import com.tasktop.c2c.server.tasks.client.widgets.presenter.person.PersonUtil;
import com.tasktop.c2c.server.tasks.client.widgets.wiki.WikiHTMLPanel;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.CommentType;
import com.tasktop.c2c.server.tasks.domain.Task;

public class TaskComment extends Composite {
	interface Binder extends UiBinder<Widget, TaskComment> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	public interface ReplyHandler {
		public void reply(int commentNumber, Comment comment);
	}

	public static class TextBoxReplyHandler implements ReplyHandler {
		private TextBoxBase textBox;

		public TextBoxReplyHandler(TextBoxBase textBox) {
			this.textBox = textBox;
		}

		public void reply(int commentNumber, Comment comment) {
			String existingText = getCommentText();
			if (existingText == null) {
				existingText = "";
			}
			String replyText = "(In reply to comment #" + commentNumber + ")\n";
			replyText += RegExp.compile("^", "gm").replace(comment.getCommentText(), "> ");

			String newText;
			int cursorPosition = getCommentCursorPosition();
			int newCursorPosition;
			if (cursorPosition >= 0 && cursorPosition < existingText.length()) {
				String firstPart = existingText.substring(0, cursorPosition);
				if (firstPart.length() > 0) {
					firstPart += "\n";
				}
				firstPart += replyText;
				firstPart += "\n\n";
				newText = firstPart + existingText.substring(cursorPosition);
				newCursorPosition = firstPart.length() - 1;
			} else {
				newText = existingText + replyText + "\n\n";
				newCursorPosition = newText.length() - 1;
			}
			updateCommentText(newText, newCursorPosition);
		}

		protected String getCommentText() {
			return textBox.getText();
		}

		protected int getCommentCursorPosition() {
			return textBox.getCursorPos();
		}

		protected void updateCommentText(String newText, int newCursorPosition) {
			textBox.setText(newText);
			textBox.setFocus(true);
			textBox.setCursorPos(newCursorPosition);
		}
	}

	@UiField
	protected Label commentNumber;

	@UiField
	protected Label commentDate;
	@UiField
	protected Anchor commentHeaderAnchor;
	@UiField
	protected WikiHTMLPanel commentText;

	@UiField
	protected Anchor commentReply;

	@UiField
	protected Panel replyPanel;

	@UiField
	protected Image avatarImage;

	public TaskComment(final Task task, final Comment comment, final int commentNumber, Person self, Person reporter,
			final ReplyHandler replyHandler, final boolean showWikiRenderedText) {
		initWidget(uiBinder.createAndBindUi(this));

		this.commentNumber.setText("Comment #" + String.valueOf(commentNumber));
		commentHeaderAnchor.setText(comment.getAuthor().getRealname());
		commentDate.setText(stringValueDateTime(comment.getCreationDate()));
		commentHeaderAnchor.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				PersonDetailPopupPanel popupPanel = new PersonDetailPopupPanel(PersonUtil.toPerson(comment.getAuthor()));
				popupPanel.showRelativeTo(commentHeaderAnchor);
			}
		});

		if (showWikiRenderedText) {
			commentText.setWikiHTML(comment.getWikiRenderedText());
		} else {
			String commentTextString = "";
			if (comment.getCommentType().equals(CommentType.ATTACHMENT)) {
				Attachment attachment = new Attachment();
				attachment.setId(Integer.valueOf(comment.getExtraData()));
				List<Attachment> attachments = task.getAttachments();
				attachment = attachments.get(attachments.indexOf(attachment));
				commentTextString += "Created attachment " + attachment.getId() + "\n" + attachment.getDescription()
						+ "\n";
			}
			commentTextString += comment.getCommentText();
			commentText.setWikiHTML(commentTextString);
		}

		if (replyHandler != null) {
			commentReply.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					replyHandler.reply(commentNumber, comment);
				}
			});
		} else {
			replyPanel.setVisible(false);
		}

		String avatarUrl = Avatar.computeAvatarUrl(comment.getAuthor().getGravatarHash(), Avatar.Size.SMALL);
		avatarImage.setUrl(avatarUrl);

		if (commentNumber % 2 == 0) {
			getWidget().getElement().addClassName("alt");
		}
	}
}
