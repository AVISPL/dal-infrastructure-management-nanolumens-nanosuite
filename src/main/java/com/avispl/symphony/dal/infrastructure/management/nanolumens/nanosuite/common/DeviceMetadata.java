/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DeviceMetadata class represents the meta data pertain to aggregated device
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/31/2024
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceMetadata {
	@JsonProperty("subsystemId")
	private String subsystemId;
	@JsonProperty("novastarPort")
	private String	novastarPort;
	@JsonProperty("novastarChain")
	private String novastarChain;
	@JsonProperty("novastarScreen")
	private String	novastarScreen;
	@JsonProperty("novastarSender")
	private String	novastarSender;
	@JsonProperty("novastarController")
	private String	novastarController;
	@JsonProperty("novastarScreenName")
	private String novastarScreenName;
	@JsonProperty("novastarControllerName")
	private String novastarControllerName;
	@JsonProperty("subsystemName")
	private String subsystemName;

	/**
	 * Retrieves {@link #subsystemId}
	 *
	 * @return value of {@link #subsystemId}
	 */
	public String getSubsystemId() {
		return subsystemId;
	}

	/**
	 * Sets {@link #subsystemId} value
	 *
	 * @param subsystemId new value of {@link #subsystemId}
	 */
	public void setSubsystemId(String subsystemId) {
		this.subsystemId = subsystemId;
	}

	/**
	 * Retrieves {@link #novastarPort}
	 *
	 * @return value of {@link #novastarPort}
	 */
	public String getNovastarPort() {
		return novastarPort;
	}

	/**
	 * Sets {@link #novastarPort} value
	 *
	 * @param novastarPort new value of {@link #novastarPort}
	 */
	public void setNovastarPort(String novastarPort) {
		this.novastarPort = novastarPort;
	}

	/**
	 * Retrieves {@link #novastarChain}
	 *
	 * @return value of {@link #novastarChain}
	 */
	public String getNovastarChain() {
		return novastarChain;
	}

	/**
	 * Sets {@link #novastarChain} value
	 *
	 * @param novastarChain new value of {@link #novastarChain}
	 */
	public void setNovastarChain(String novastarChain) {
		this.novastarChain = novastarChain;
	}

	/**
	 * Retrieves {@link #novastarScreen}
	 *
	 * @return value of {@link #novastarScreen}
	 */
	public String getNovastarScreen() {
		return novastarScreen;
	}

	/**
	 * Sets {@link #novastarScreen} value
	 *
	 * @param novastarScreen new value of {@link #novastarScreen}
	 */
	public void setNovastarScreen(String novastarScreen) {
		this.novastarScreen = novastarScreen;
	}

	/**
	 * Retrieves {@link #novastarSender}
	 *
	 * @return value of {@link #novastarSender}
	 */
	public String getNovastarSender() {
		return novastarSender;
	}

	/**
	 * Sets {@link #novastarSender} value
	 *
	 * @param novastarSender new value of {@link #novastarSender}
	 */
	public void setNovastarSender(String novastarSender) {
		this.novastarSender = novastarSender;
	}

	/**
	 * Retrieves {@link #novastarController}
	 *
	 * @return value of {@link #novastarController}
	 */
	public String getNovastarController() {
		return novastarController;
	}

	/**
	 * Sets {@link #novastarController} value
	 *
	 * @param novastarController new value of {@link #novastarController}
	 */
	public void setNovastarController(String novastarController) {
		this.novastarController = novastarController;
	}

	/**
	 * Retrieves {@link #novastarScreenName}
	 *
	 * @return value of {@link #novastarScreenName}
	 */
	public String getNovastarScreenName() {
		return novastarScreenName;
	}

	/**
	 * Sets {@link #novastarScreenName} value
	 *
	 * @param novastarScreenName new value of {@link #novastarScreenName}
	 */
	public void setNovastarScreenName(String novastarScreenName) {
		this.novastarScreenName = novastarScreenName;
	}

	/**
	 * Retrieves {@link #novastarControllerName}
	 *
	 * @return value of {@link #novastarControllerName}
	 */
	public String getNovastarControllerName() {
		return novastarControllerName;
	}

	/**
	 * Sets {@link #novastarControllerName} value
	 *
	 * @param novastarControllerName new value of {@link #novastarControllerName}
	 */
	public void setNovastarControllerName(String novastarControllerName) {
		this.novastarControllerName = novastarControllerName;
	}

	/**
	 * Retrieves {@link #subsystemName}
	 *
	 * @return value of {@link #subsystemName}
	 */
	public String getSubsystemName() {
		return subsystemName;
	}

	/**
	 * Sets {@link #subsystemName} value
	 *
	 * @param subsystemName new value of {@link #subsystemName}
	 */
	public void setSubsystemName(String subsystemName) {
		this.subsystemName = subsystemName;
	}
}