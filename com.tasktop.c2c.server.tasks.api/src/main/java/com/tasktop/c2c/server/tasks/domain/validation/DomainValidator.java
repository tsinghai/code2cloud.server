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
package com.tasktop.c2c.server.tasks.domain.validation;


import com.tasktop.c2c.server.common.service.validation.AbstractDomainValidator;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.WorkLog;

public class DomainValidator extends AbstractDomainValidator {

	{
		registerValidator(Task.class, new TaskValidator());
		registerValidator(Comment.class, new CommentValidator());
		registerValidator(WorkLog.class, new WorkLogValidator());
		registerValidator(Attachment.class, new AttachmentValidator());
		registerValidator(Product.class, new ProductValidator());
		registerValidator(Component.class, new ComponentValidator());
		registerValidator(Milestone.class, new MilestoneValidator());
		registerValidator(Keyword.class, new KeywordValidator());
		registerValidator(SavedTaskQuery.class, new SavedTaskQueryValidator());
		registerValidator(Iteration.class, new IterationValidator());
		registerValidator(FieldDescriptor.class, new FieldDescriptorValidator());
	}

}
