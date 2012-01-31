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
package com.tasktop.c2c.server.common.service.wiki;

import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElementProcessor;
import org.springframework.tenancy.context.TenancyContextHolder;

/**
 * <p>
 * This Link replacer will inject the current project's tenant ID (e.g. 'cf-code') as the first argument in the
 * resulting replacement string, and will pass along the captured pattern group as the second. For instance, with the
 * following inputs:
 * </p>
 * 
 * <pre>
 * linkPattern = &quot;task (\\d+)&quot;;
 * replacementString = &quot;project/%1$s/task/%2$s&quot;;
 * tenant ID = &quot;cf-code&quot;;
 * input string = "task 1234"
 * </pre>
 * <p>
 * The resulting output will be:
 * </p>
 * <code>&lt;a href="project/cf-code/task/1234"&gt;task 1234&lt;/a&gt;</code>
 * 
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class TenantAwareLinkReplacementToken extends LinkReplacementToken {

	public TenantAwareLinkReplacementToken(String linkPattern, String replacementString) {
		super(linkPattern, replacementString);
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new PatternBasedElementProcessor() {
			public void emit() {
				// Get our tenant from the TenancyContext
				String projectId = String.valueOf(TenancyContextHolder.getContext().getTenant().getIdentity());

				// Do our custom formatting on the HREF, but make sure the original text is unmodified.
				getBuilder().link(String.format(replacementString, projectId, group(1)), group(0));
			};
		};
	}

}
