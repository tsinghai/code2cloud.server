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
package com.tasktop.c2c.server.common.service.doc;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class DocumentElementContainer<Element extends AnnotatedElement> implements Iterable<Section> {

	private SortedMap<Section, SortedMap<String, Element>> sectionToTitledElements = new TreeMap<Section, SortedMap<String, Element>>(
			new SectionComparator());

	public void put(Element element) {
		SectionKey sectionKey = computeSectionKey(element);
		SortedMap<String, Element> elementsByTitle = sectionToTitledElements.get(sectionKey);
		if (elementsByTitle == null) {
			elementsByTitle = new TreeMap<String, Element>();
			sectionToTitledElements.put(sectionKey, elementsByTitle);
		}
		String title = computeTitle(element);
		elementsByTitle.put(title, element);
	}

	public Iterator<Section> iterator() {
		return sectionToTitledElements.keySet().iterator();
	}

	public SortedMap<String, Element> sectionElementByTitle(Section section) {
		return sectionToTitledElements.get(section);
	}

	protected String computeTitle(Element element) {
		Title title = element.getAnnotation(Title.class);
		if (title != null) {
			return title.value();
		} else if (element instanceof Method) {
			return ((Method) element).getName();
		} else if (element instanceof Class) {
			return ((Class<?>) element).getSimpleName();
		} else {
			throw new IllegalStateException();
		}
	}

	protected SectionKey computeSectionKey(Element element) {
		Section section = element.getAnnotation(Section.class);
		if (section == null) {
			section = new SectionKey("", 0);
		}
		return new SectionKey(section);
	}

	private static class SectionKey implements Section {
		private String value;
		private int order;

		public SectionKey(String value, int order) {
			this.value = value;
			this.order = order;
		}

		public SectionKey(Section section) {
			this(section.value(), section.order());
		}

		public Class<? extends Annotation> annotationType() {
			return Section.class;
		}

		public int order() {
			return order;
		}

		public String value() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + order;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SectionKey other = (SectionKey) obj;
			if (order != other.order)
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

	}
}
