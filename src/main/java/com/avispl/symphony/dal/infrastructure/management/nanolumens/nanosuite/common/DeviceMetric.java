/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DeviceMetric class represents the metric info of aggregated device
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/31/2024
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceMetric {
	private String lastValue;
	private String metricType;
	private int healthState;
	private String displayName;

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
	 * Retrieves {@link #lastValue}
	 *
	 * @return value of {@link #lastValue}
	 */
	public String getLastValue() {
		return lastValue;
	}

	/**
	 * Sets {@link #lastValue} value
	 *
	 * @param lastValue new value of {@link #lastValue}
	 */
	public void setLastValue(String lastValue) {
		this.lastValue = lastValue;
	}

	/**
	 * Retrieves {@link #metricType}
	 *
	 * @return value of {@link #metricType}
	 */
	public String getMetricType() {
		return metricType;
	}

	/**
	 * Sets {@link #metricType} value
	 *
	 * @param metricType new value of {@link #metricType}
	 */
	public void setMetricType(String metricType) {
		this.metricType = metricType;
	}

	/**
	 * Retrieves {@link #healthState}
	 *
	 * @return value of {@link #healthState}
	 */
	public int getHealthState() {
		return healthState;
	}

	/**
	 * Sets {@link #healthState} value
	 *
	 * @param healthState new value of {@link #healthState}
	 */
	public void setHealthState(int healthState) {
		this.healthState = healthState;
	}
}