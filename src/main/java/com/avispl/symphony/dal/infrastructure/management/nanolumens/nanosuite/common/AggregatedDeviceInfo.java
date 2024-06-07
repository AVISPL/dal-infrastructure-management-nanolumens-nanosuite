/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AggregatedDeviceInfo class represents information pertain to aggregated device
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/31/2024
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregatedDeviceInfo {
	@JsonProperty("profileType")
	private String profileType;
	@JsonProperty("metadata")
	private DeviceMetadata metadata;
	@JsonProperty("metrics")
	private List<DeviceMetric> metrics;
	@JsonProperty("displayName")
	private String displayName;
	@JsonProperty("externalRef")
	private String externalRef;

	@JsonProperty("healthState")
	private DeviceMetric overallHealthState;

	/**
	 * Retrieves {@link #externalRef}
	 *
	 * @return value of {@link #externalRef}
	 */
	public String getExternalRef() {
		return externalRef;
	}

	/**
	 * Sets {@link #externalRef} value
	 *
	 * @param externalRef new value of {@link #externalRef}
	 */
	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}

	/**
	 * Retrieves {@link #displayName}
	 *
	 * @return value of {@link #displayName}
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets {@link #displayName} value
	 *
	 * @param displayName new value of {@link #displayName}
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Retrieves {@link #profileType}
	 *
	 * @return value of {@link #profileType}
	 */
	public String getProfileType() {
		return profileType;
	}

	/**
	 * Sets {@link #profileType} value
	 *
	 * @param profileType new value of {@link #profileType}
	 */
	public void setProfileType(String profileType) {
		this.profileType = profileType;
	}

	/**
	 * Retrieves {@link #metadata}
	 *
	 * @return value of {@link #metadata}
	 */
	public DeviceMetadata getMetadata() {
		return metadata;
	}

	/**
	 * Sets {@link #metadata} value
	 *
	 * @param metadata new value of {@link #metadata}
	 */
	public void setMetadata(DeviceMetadata metadata) {
		this.metadata = metadata;
	}

	/**
	 * Retrieves {@link #metrics}
	 *
	 * @return value of {@link #metrics}
	 */
	public List<DeviceMetric> getMetrics() {
		return metrics;
	}

	/**
	 * Sets {@link #metrics} value
	 *
	 * @param metrics new value of {@link #metrics}
	 */
	public void setMetrics(List<DeviceMetric> metrics) {
		this.metrics = metrics;
	}

	/**
	 * Retrieves {@link #overallHealthState}
	 *
	 * @return value of {@link #overallHealthState}
	 */
	public DeviceMetric getOverallHealthState() {
		return overallHealthState;
	}

	/**
	 * Sets {@link #overallHealthState} value
	 *
	 * @param overallHealthState new value of {@link #overallHealthState}
	 */
	public void setOverallHealthState(DeviceMetric overallHealthState) {
		this.overallHealthState = overallHealthState;
	}
}