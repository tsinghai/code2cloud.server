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
package com.tasktop.c2c.server.internal.wiki.server;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.internal.wiki.server.domain.MarkupRenderer;
import com.tasktop.c2c.server.internal.wiki.server.domain.PageContent;


@Service("internalWikiService")
@Qualifier("main")
@Transactional(rollbackFor = { Exception.class })
public class InternalWikiServiceBean extends AbstractJpaServiceBean implements InternalWikiService {

	private Logger log = LoggerFactory.getLogger(InternalWikiServiceBean.class.getName());

	@Autowired
	private MarkupRenderer markupRenderer;

	@Override
	public void updatePageContent() {
		for (Map.Entry<String, String> markupToVersionEntry : markupRenderer.getMarkupLanguageToVersion().entrySet()) {
			TypedQuery<PageContent> query = entityManager
					.createQuery(
							"select pc from "
									+ PageContent.class.getSimpleName()
									+ " pc where pc.page.markupLanguage = :language and (pc.rendererVersion is null or pc.rendererVersion != :version)",
							PageContent.class);
			query.setParameter("language", markupToVersionEntry.getKey());
			query.setParameter("version", markupToVersionEntry.getValue());
			for (PageContent content : query.getResultList()) {
				content.getPage();

				log.info(String.format("Updating page %s (%s) rendered version %s -> %s", content.getPage().getPath(),
						content.getId(), content.getRendererVersion(), markupToVersionEntry.getValue()));

				entityManager.detach(content.getPage());
				entityManager.detach(content);

				markupRenderer.render(content);

				Query updateQuery = entityManager.createQuery("update " + PageContent.class.getSimpleName()
						+ " pc set pc.renderedContent = :content, pc.rendererVersion = :version where pc.id = :id");
				updateQuery.setParameter("content", content.getRenderedContent());
				updateQuery.setParameter("version", content.getRendererVersion());
				updateQuery.setParameter("id", content.getId());

				updateQuery.executeUpdate();
			}
		}

	}
}
