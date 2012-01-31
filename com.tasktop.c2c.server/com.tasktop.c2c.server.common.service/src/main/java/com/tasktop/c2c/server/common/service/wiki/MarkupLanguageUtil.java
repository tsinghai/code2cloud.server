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

import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguageConfiguration;
import org.eclipse.mylyn.wikitext.core.parser.markup.token.ImpliedHyperlinkReplacementToken;
import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class MarkupLanguageUtil {

	private MarkupLanguageUtil() {
		// No instantiation of this class.
	}

	public static MarkupLanguage createDefaultMarkupLanguage() {
		MarkupLanguageConfiguration configuration = new MarkupLanguageConfiguration();
		configuration.setEscapingHtmlAndXml(true);

		// Be relatively liberal with our email detection regex.
		LinkReplacementToken emailReplacer = new LinkReplacementToken(
				"([^@\\s]+@(?:[^\\.\\@\\s]+\\.)+\\p{Alpha}{2,6})", "mailto:%s");

		// Wire up our task detection.
		TenantAwareLinkReplacementToken taskLinkReplacer = new TenantAwareLinkReplacementToken(
				"(?:(?:(?:[Dd][Uu][Pp][Ll][Ii][Cc][Aa][Tt][Ee] [Oo][Ff])|(?:[Tt][Aa][Ss][Kk])|(?:[Ff][Ee][Aa][Tt][Uu][Rr][Ee])|(?:[Dd][Ee][Ff][Ee][Cc][Tt])|(?:[Bb][Uu][Gg]))[ \t]*#?[ \t]*(\\d+))",
				"#projects/%1$s/task/%2$s");

		configuration.getPhraseModifiers().add(emailReplacer);
		configuration.getPhraseModifiers().add(taskLinkReplacer);
		configuration.getPhraseModifiers().add(new ImpliedHyperlinkReplacementToken());

		// Create and wire up our markup language.
		MarkupLanguage markupLanguage = new TextileLanguage();
		markupLanguage.configure(configuration);

		return markupLanguage;
	}
}
