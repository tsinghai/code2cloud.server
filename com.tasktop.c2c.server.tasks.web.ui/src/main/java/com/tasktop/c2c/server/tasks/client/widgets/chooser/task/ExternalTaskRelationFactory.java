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
package com.tasktop.c2c.server.tasks.client.widgets.chooser.task;


import com.tasktop.c2c.server.common.web.client.widgets.chooser.AbstractValueChooser;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.AbstractValueComposite;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.ValueCompositeFactory;
import com.tasktop.c2c.server.tasks.domain.ExternalTaskRelation;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
public class ExternalTaskRelationFactory extends ValueCompositeFactory<ExternalTaskRelation> {

	@Override
	public AbstractValueComposite<ExternalTaskRelation> getComposite(
			AbstractValueChooser<ExternalTaskRelation> valueChooser, ExternalTaskRelation origin) {
		ExternalTaskRelationValueComposite valueComposite = new ExternalTaskRelationValueComposite(valueChooser);
		valueComposite.setValue(origin);
		valueComposite.initWidget();
		return valueComposite;
	}

}
