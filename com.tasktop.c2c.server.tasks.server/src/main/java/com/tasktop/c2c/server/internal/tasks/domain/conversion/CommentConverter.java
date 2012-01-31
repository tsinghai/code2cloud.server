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
package com.tasktop.c2c.server.internal.tasks.domain.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.internal.tasks.service.CommentWikiRenderer;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.CommentType;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;

/**
 * @author Clint Morgan (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
@Component
public class CommentConverter implements ObjectConverter<Comment> {

	@Autowired
	private CommentWikiRenderer renderer;

	@Override
	public boolean supportsSource(Class<?> clazz) {
		return com.tasktop.c2c.server.internal.tasks.domain.Comment.class.isAssignableFrom(clazz);
	}

	@Override
	public void copy(Comment target, Object internalObject, DomainConverter converter, DomainConversionContext context) {
		com.tasktop.c2c.server.internal.tasks.domain.Comment source = (com.tasktop.c2c.server.internal.tasks.domain.Comment) internalObject;

		target.setId(source.getId());
		target.setCreationDate(source.getCreationTs());
		target.setCommentText(source.getThetext());
		target.setAuthor((TaskUserProfile) converter.convert(source.getProfile(), context));
		target.setCommentType(CommentType.getCommentType(source.getType()));
		target.setExtraData(source.getExtraData());
		target.setWikiRenderedText(renderer.render(source.getThetext()));
	}

	@Override
	public Class<Comment> getTargetClass() {
		return Comment.class;
	}

}
