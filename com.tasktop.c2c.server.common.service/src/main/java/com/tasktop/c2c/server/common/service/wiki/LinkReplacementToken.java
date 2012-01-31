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

import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElement;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElementProcessor;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class LinkReplacementToken extends PatternBasedElement {

	protected final String linkPattern;
	protected final String replacementString;

	public LinkReplacementToken(String linkPattern, String replacementString) {
		// First, make sure it compiles - this throws an exception if not.
		Pattern.compile(linkPattern);

		this.linkPattern = linkPattern;
		this.replacementString = replacementString;
	}

	@Override
	protected String getPattern(int groupOffset) {
		// Always return the same pattern.
		return linkPattern;
	}

	@Override
	protected int getPatternGroupCount() {
		// Only support 1 group in our links
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new PatternBasedElementProcessor() {
			public void emit() {
				// Do our custom formatting on the HREF, but make sure the original text is unmodified.
				getBuilder().link(String.format(replacementString, group(1)), group(0));
			};
		};
	}

}
