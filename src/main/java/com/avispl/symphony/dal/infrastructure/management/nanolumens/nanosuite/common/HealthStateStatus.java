/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common;

import java.util.Arrays;

/**
 * HealthState represents the health state of device
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/27/2024
 * @since 1.0.0
 */
public enum HealthStateStatus {
	HEALTHY("Healthy", 0),
	WARNING("Warning", 1),
	ERROR("Error", 2),
	UNKNOWN("Unknown", -1);

	private String name;
	private int value;

	HealthStateStatus(String name, int value) {
		this.name = name;
    this.value = value;
	}

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@link #value}
	 *
	 * @return value of {@link #value}
	 */
	public int getValue() {
		return value;
	}

	/**
   * Retrieves {@link HealthStateStatus} by {@link #value}
   *
   * @param value {@link #value}
   * @return {@link HealthStateStatus}
	 */
	 public static HealthStateStatus getByValue(int value) {
		 return Arrays.stream(values())
				 .filter(status -> status.getValue() == value)
				 .findFirst()
				 .orElse(null);
	 }
}