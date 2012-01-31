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
package com.tasktop.c2c.server.common.web.client.widgets.chooser.person;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Person implements Serializable, Comparable<Person> {
	private String name;
	private String identity;
	private Integer id;

	public Person() {
	}

	public Person(String identity, String name) {
		this(identity, name, null);
	}

	public Person(String identity, String name, Integer id) {
		this.identity = identity;
		this.name = name;
		this.id = id;
	}

	/**
	 * the name of the person, usually 'First Last'
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * the identity of the person, usually their username
	 */
	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	@Override
	public String toString() {
		return toFullyQualifiedName();
	}

	public String toFullyQualifiedName() {
		return getName() + " (" + getIdentity() + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identity == null) ? 0 : identity.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Person other = (Person) obj;
		if (identity == null) {
			if (other.identity != null)
				return false;
		} else if (!identity.equals(other.identity))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int compareTo(Person o) {
		if (o == this) {
			return 0;
		}
		int i = getName().compareToIgnoreCase(o.getName());
		if (i == 0) {
			i = getName().compareTo(o.getName());
			if (i == 0) {
				i = getIdentity().compareToIgnoreCase(o.getIdentity());
			}
		}
		return i;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

}
