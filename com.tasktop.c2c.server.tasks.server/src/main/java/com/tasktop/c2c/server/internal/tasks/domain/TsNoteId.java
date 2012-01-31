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
package com.tasktop.c2c.server.internal.tasks.domain;

// Generated May 26, 2010 11:31:55 AM by Hibernate Tools 3.3.0.GA

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * TsNoteId generated by hbm2java
 */
@Embeddable
@SuppressWarnings("serial")
public class TsNoteId implements java.io.Serializable {

	private int jobid;
	private String notekey;
	private byte[] value;

	public TsNoteId() {
	}

	public TsNoteId(int jobid) {
		this.jobid = jobid;
	}

	public TsNoteId(int jobid, String notekey, byte[] value) {
		this.jobid = jobid;
		this.notekey = notekey;
		this.value = value;
	}

	@Column(name = "jobid", nullable = false)
	public int getJobid() {
		return this.jobid;
	}

	public void setJobid(int jobid) {
		this.jobid = jobid;
	}

	@Column(name = "notekey")
	public String getNotekey() {
		return this.notekey;
	}

	public void setNotekey(String notekey) {
		this.notekey = notekey;
	}

	@Column(name = "value")
	public byte[] getValue() {
		return this.value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof TsNoteId))
			return false;
		TsNoteId castOther = (TsNoteId) other;

		return (this.getJobid() == castOther.getJobid())
				&& ((this.getNotekey() == castOther.getNotekey()) || (this.getNotekey() != null
						&& castOther.getNotekey() != null && this.getNotekey().equals(castOther.getNotekey())))
				&& ((this.getValue() == castOther.getValue()) || (this.getValue() != null
						&& castOther.getValue() != null && Arrays.equals(this.getValue(), castOther.getValue())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getJobid();
		result = 37 * result + (getNotekey() == null ? 0 : this.getNotekey().hashCode());
		result = 37 * result + (getValue() == null ? 0 : Arrays.hashCode(this.getValue()));
		return result;
	}

}
