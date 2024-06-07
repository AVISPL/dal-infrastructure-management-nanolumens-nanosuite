/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.metric;

import java.util.Arrays;

/**
 * MetricMappingValue represents the value mapping for specific metric type
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 5/27/2024
 * @since 1.0.0
 */
public enum MetricMappingValue {
	VIDEO_BLACKOUT(ReceiverMetric.VIDEO_BLACKOUT.getName(), "Blackout", "Normal"),
	VIDEO_FREEZE(ReceiverMetric.VIDEO_FREEZE.getName(), "Freeze", "Normal"),
	VIDEO_MAPPING(ReceiverMetric.VIDEO_MAPPING.getName(), "Overlay active", "Normal"),
  DISPLAYPORT(SenderMetric.INPUT_VALID_DISPLAYPORT.getName(), "Healthy", "Invalid"),
	DVI1(SenderMetric.INPUT_VALID_DVI1.getName(), "Healthy", "Invalid"),
	DVI2(SenderMetric.INPUT_VALID_DVI2.getName(), "Healthy", "Invalid"),
	DVI3(SenderMetric.INPUT_VALID_DVI3.getName(), "Healthy", "Invalid"),
	DVI4(SenderMetric.INPUT_VALID_DVI4.getName(), "Healthy", "Invalid"),
	HDMI(SenderMetric.INPUT_VALID_HDMI.getName(), "Healthy", "Invalid"),
	SDI(SenderMetric.INPUT_VALID_SDI.getName(), "Healthy", "Invalid"),
	INPUT_VALID(SenderMetric.INPUT_VALID.getName(), "Healthy", "Invalid"),
	;

	private String name;
	private String enableValue;
	private String disableValue;

	MetricMappingValue(String name, String enableValue, String disableValue) {
		this.name = name;
		this.enableValue = enableValue;
		this.disableValue = disableValue;
	}

	/**
	 * Retrieves {@link #enableValue}
	 *
	 * @return value of {@link #enableValue}
	 */
	public String getEnableValue() {
		return enableValue;
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
	 * Retrieves {@link #disableValue}
	 *
	 * @return value of {@link #disableValue}
	 */
	public String getDisableValue() {
		return disableValue;
	}

	/**
	 * Retrieves {@link MetricMappingValue} by {@link #name}
	 *
	 * @param name {@link #name}
	 * @return {@link MetricMappingValue}
	 */
	public static MetricMappingValue getByName(String name) {
		return Arrays.stream(values())
				.filter(metric -> metric.getName().equalsIgnoreCase(name))
				.findFirst()
				.orElse(null);
	}
}
