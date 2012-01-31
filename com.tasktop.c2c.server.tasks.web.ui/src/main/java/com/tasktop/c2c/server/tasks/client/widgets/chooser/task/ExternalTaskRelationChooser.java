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

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.user.client.ui.SuggestOracle;
import com.tasktop.c2c.server.common.web.client.widgets.chooser.MultiValueChooser;
import com.tasktop.c2c.server.tasks.domain.ExternalTaskRelation;

/**
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 * @see com.tasktop.client.associations.core.TaskRelationKind for valid values.
 * 
 */
public class ExternalTaskRelationChooser extends MultiValueChooser<ExternalTaskRelation> {

	/**
	 * Always create a {@link ExternalTaskRelation} of type link and kind web
	 */
	public ExternalTaskRelationChooser() {
		super(new SuggestOracle() {

			@Override
			public void requestSuggestions(Request request, Callback callback) {

				List<Suggestion> suggestions = new ArrayList<Suggestion>();
				suggestions.add(new ExternalTaskRelationSuggestion(new ExternalTaskRelation("link", "web", request
						.getQuery())));
				callback.onSuggestionsReady(request, new Response(suggestions));
			}
		});
	}

}
