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
package com.tasktop.c2c.server.profile.domain.build;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildDetails implements Serializable {

	public enum BuildResult {

		SUCCESS("Success"), UNSTABLE("Unstable"), FAILURE("Failure"), NOT_BUILT("Not built yet"), ABORTED("Aborted");

		private final String friendlyName;

		private BuildResult(String friendlyName) {
			this.friendlyName = friendlyName;
		}

		/**
		 * @return the friendlyName
		 */
		public String getFriendlyName() {
			return friendlyName;
		}
	}

	private String url;
	private Long duration;
	private Long timestamp;
	private BuildResult result;
	private Integer number;
	private List<Object> actions;
	private List<BuildArtifact> artifacts;
	private Boolean building;

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setResult(BuildResult result) {
		this.result = result;
	}

	public BuildResult getResult() {
		return result;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public void setActions(List<Object> actions) {
		this.actions = actions;
	}

	public List<Object> getActions() {
		return actions;
	}

	public String getCause() {
		if (actions == null) {
			return null;
		}
		for (Object obj : actions) {
			if (obj instanceof Map) {
				Map map = (Map) obj;
				if (map.containsKey("causes")) {
					List list = (List) map.get("causes");
					for (Object obj2 : list) {
						if (obj2 instanceof Map) {
							String value = (String) ((Map) obj2).get("shortDescription");
							if (value != null) {
								return value;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Long getDuration() {
		return duration;
	}

	public Boolean getBuilding() {
		return building;
	}

	public void setBuilding(Boolean building) {
		this.building = building;
	}

	public List<BuildArtifact> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<BuildArtifact> artifacts) {
		this.artifacts = artifacts;
	}
}
