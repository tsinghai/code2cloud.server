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
package com.tasktop.c2c.server.tasks.domain;

public enum CommentType {
	NORMAL, DUPLICATE, NEW_BY_VOTING, MOVED, ATTACHMENT, UNKNOWN;

	public static CommentType getCommentType(short type) {
		switch (type) {
		case 1:
			return NORMAL;
		case 2:
			return DUPLICATE;
		case 3:
			return NEW_BY_VOTING;
		case 4:
			return MOVED;
		case 5:
			return ATTACHMENT;
		default:
			return UNKNOWN;
		}
	}
}
