/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SystemInformation includes information of aggregator device
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 5/23/2024
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubSystemInformation {
	@JsonProperty("type")
	private String type;
	@JsonAlias("displayName")
	private String name;
	@JsonProperty("description")
	private String description;
	@JsonAlias("externalId")
	private String moduleId;
	@JsonAlias("isAlive")
	private String connectionStatus;
	@JsonProperty("lastContactedAt")
	private String lastContactedAt;

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@link #name} value
	 *
	 * @param name new value of {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieves {@link #type}
	 *
	 * @return value of {@link #type}
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets {@link #type} value
	 *
	 * @param type new value of {@link #type}
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Retrieves {@link #description}
	 *
	 * @return value of {@link #description}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets {@link #description} value
	 *
	 * @param description new value of {@link #description}
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Retrieves {@link #moduleId}
	 *
	 * @return value of {@link #moduleId}
	 */
	public String getModuleId() {
		return moduleId;
	}

	/**
	 * Sets {@link #moduleId} value
	 *
	 * @param moduleId new value of {@link #moduleId}
	 */
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	/**
	 * Retrieves {@link #connectionStatus}
	 *
	 * @return value of {@link #connectionStatus}
	 */
	public String getConnectionStatus() {
		return connectionStatus;
	}

	/**
	 * Sets {@link #connectionStatus} value
	 *
	 * @param connectionStatus new value of {@link #connectionStatus}
	 */
	public void setConnectionStatus(String connectionStatus) {
		this.connectionStatus = connectionStatus;
	}

	/**
	 * Retrieves {@link #lastContactedAt}
	 *
	 * @return value of {@link #lastContactedAt}
	 */
	public String getLastContactedAt() {
		return lastContactedAt;
	}

	/**
	 * Sets {@link #lastContactedAt} value
	 *
	 * @param lastContactedAt new value of {@link #lastContactedAt}
	 */
	public void setLastContactedAt(String lastContactedAt) {
		this.lastContactedAt = lastContactedAt;
	}
}
