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
package com.tasktop.c2c.server.internal.wiki.server.domain;

import java.io.IOException;
import java.io.StringWriter;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineItem;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tasktop.c2c.server.common.service.wiki.MarkupLanguageUtil;
import com.tasktop.c2c.server.internal.wiki.server.WikiServiceConfiguration;
import com.tasktop.c2c.server.wiki.domain.PageOutline;
import com.tasktop.c2c.server.wiki.domain.PageOutlineItem;

/**
 * @author David Green (Tasktop Technologies Inc.)
 * @author Lucas Panjer (Tasktop Technologies Inc.)
 * 
 */
@Component
public class MarkupRenderer {

	private Map<String, MarkupLanguage> markupLanguageByName = new HashMap<String, MarkupLanguage>();

	private Map<String, String> markupLanguageToVersion = new HashMap<String, String>();

	@Autowired
	WikiServiceConfiguration serviceConfiguration;

	public MarkupRenderer() {
		register(MarkupLanguageUtil.createDefaultMarkupLanguage());

		for (Map.Entry<String, MarkupLanguage> languageEntry : markupLanguageByName.entrySet()) {
			markupLanguageToVersion.put(languageEntry.getKey(), computeVersion(languageEntry.getValue()));
		}
	}

	public void setServiceConfiguration(WikiServiceConfiguration serviceConfiguration) {
		this.serviceConfiguration = serviceConfiguration;
	}

	private void register(MarkupLanguage markupLanguage) {
		markupLanguageByName.put(markupLanguage.getName(), markupLanguage);
	}

	public void render(PageContent pageContent) {
		final Page page = pageContent.getPage();
		if (page == null || page.getId() == null) {
			throw new IllegalStateException();
		}
		final String markupLanguageVersion = markupLanguageToVersion.get(page.getMarkupLanguage());
		pageContent.setRendererVersion(markupLanguageVersion);
		pageContent.setRenderedContent(render(page, pageContent.getContent()));
	}

	public String render(Page page, String pageContent) {
		MarkupLanguage markupLanguage = markupLanguageByName.get(page.getMarkupLanguage());

		if (markupLanguage == null) {
			throw new IllegalStateException("Markup language not found: " + page.getMarkupLanguage());
		}
		// must clone: markup language cannot be shared between threads
		markupLanguage = markupLanguage.clone();

		StringWriter writer = new StringWriter();

		HtmlDocumentBuilder builder = new WikiHtmlDocumentBuilder(writer, page, serviceConfiguration);

		MarkupParser parser = new MarkupParser(markupLanguage);
		parser.setBuilder(builder);
		parser.parse(pageContent);
		return writer.toString();
	}

	/**
	 * indicate if the given page content is up to date with respect to the {@link PageContent#getRendererVersion()
	 * renderer version}.
	 */
	public boolean isUpToDate(PageContent pageContent) {
		final String markupLanguageVersion = markupLanguageToVersion.get(pageContent.getPage().getMarkupLanguage());
		return markupLanguageVersion != null && markupLanguageVersion.equals(pageContent.getRendererVersion());
	}

	private String computeVersion(MarkupLanguage language) {
		String wikiVersion = computeVersion(MarkupRenderer.class);
		String markupLanguageVersion = computeVersion(language.getClass());
		String wikitextVersion = computeVersion(MarkupLanguage.class);
		return wikiVersion + ";" + wikitextVersion + ";" + markupLanguageVersion;
	}

	private String computeVersion(Class<?> clazz) {
		String version = clazz.getPackage().getImplementationVersion();
		if (version == null || version.length() == 0) {
			String classResource = clazz.getName().replace(".", "/") + ".class";
			URL classResourceUrl = clazz.getClassLoader().getResource(classResource);
			if (classResourceUrl != null) {
				try {
					URLConnection urlConnection = classResourceUrl.openConnection();
					if (urlConnection instanceof JarURLConnection) {
						JarURLConnection jarConnection = (JarURLConnection) urlConnection;
						Manifest manifest = jarConnection.getManifest();
						if (manifest != null) {
							version = manifest.getMainAttributes().getValue("Bundle-Version");
						}
					}
				} catch (MalformedURLException e) {
					// ignore
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return version;
	}

	public Map<String, String> getMarkupLanguageToVersion() {
		return Collections.unmodifiableMap(markupLanguageToVersion);
	}

	public PageOutline renderOutline(PageContent pageContent) {
		final Page page = pageContent.getPage();
		if (page == null || page.getId() == null) {
			throw new IllegalStateException();
		}

		MarkupLanguage markupLanguage = markupLanguageByName.get(page.getMarkupLanguage());

		if (markupLanguage == null) {
			throw new IllegalStateException("Markup language not found: " + page.getMarkupLanguage());
		}
		// must clone: markup language cannot be shared between threads
		markupLanguage = markupLanguage.clone();

		OutlineParser parser = new OutlineParser(markupLanguage);

		OutlineItem rootOutlineItem = parser.parse(pageContent.getContent());

		PageOutline outline = new PageOutline();

		for (OutlineItem child : rootOutlineItem.getChildren()) {
			PageOutlineItem outlineItem = new PageOutlineItem();
			outlineItem.setLabel(child.getLabel());
			outlineItem.setId(child.getId());
			outlineItem.setUrl(serviceConfiguration.computeWebUrlForPage(page.getPath())
					+ WikiHtmlDocumentBuilder.HASHTAG_DELIMITER + child.getId());
			outline.addOutlineItem(outlineItem);
		}

		return outline;
	}
}
