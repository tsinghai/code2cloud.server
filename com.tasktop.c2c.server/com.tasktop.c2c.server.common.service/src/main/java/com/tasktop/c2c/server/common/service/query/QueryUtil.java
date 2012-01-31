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
package com.tasktop.c2c.server.common.service.query;

import java.util.Iterator;
import java.util.List;

import com.tasktop.c2c.server.common.service.domain.Region;


public class QueryUtil {

	/**
	 * Applies the region, if not null, to the given list. This will remove the results before and after the desired
	 * page. NOTE in general this should be done at the datastore level (e.g., JPA) if applicable.
	 * 
	 * @param result
	 * @param region
	 */
	public static void applyRegionToList(List<?> result, Region region) {
		if (region != null) {
			Iterator<?> it = result.iterator();
			int i = 0;
			while (it.hasNext()) {
				it.next();
				if (i < region.getOffset()) {
					it.remove();
				} else if (i >= region.getOffset() + region.getSize()) {
					it.remove();
				}
				i++;
			}
		}
	}
}
