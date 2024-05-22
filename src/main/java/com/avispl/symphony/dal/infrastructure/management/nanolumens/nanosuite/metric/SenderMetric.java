/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.metric;

import java.util.Arrays;

/**
 * SenderMetric class represents metric of novastar sender asset type
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/31/2024
 * @since 1.0.0
 */
public enum SenderMetric {
	INPUT_SOURCE("InputSource","novastar_sender_input_source","novastar_screen_sender_input_source"),
	INPUT_VALID_DISPLAYPORT("InputValidDisplayPort","novastar_sender_input_valid_displayport","novastar_screen_sender_input_valid_displayport"),
	INPUT_VALID_DVI1("InputValidDVI1","novastar_sender_input_valid_dvi1","novastar_screen_sender_input_valid_dvi1"),
	INPUT_VALID_DVI2("InputValidDVI2","novastar_sender_input_valid_dvi2","novastar_screen_sender_input_valid_dvi2"),
	INPUT_VALID_DVI3("InputValidDVI3","novastar_sender_input_valid_dvi3","novastar_screen_sender_input_valid_dvi3"),
	INPUT_VALID_DVI4("InputValidDVI4","novastar_sender_input_valid_dvi4","novastar_screen_sender_input_valid_dvi4"),
	INPUT_VALID_HDMI("InputValidHDMI","novastar_sender_input_valid_HDMI","novastar_screen_sender_input_valid_HDMI"),
	INPUT_VALID_SDI("InputValidSDI","novastar_sender_input_valid_sdi","novastar_screen_sender_input_valid_sdi"),
	INPUT_VALID("InputValid","novastar_sender_input_valid","novastar_screen_sender_input_valid"),
	MODEL("Model","novastar_sender_model","novastar_screen_sender_model"),
	SERIALNUMBER("SerialNumber","novastar_sender_serialnumber","novastar_screen_sender_serialnumber"),
	VERSION_FPGA("VersionFPGA","novastar_sender_version_fpga","novastar_screen_sender_version_fpga"),
	VERSION_MCU("VersionMCU","novastar_sender_version_mcu","novastar_screen_sender_version_mcu");

	private String name;
	private String value;
	private String screenValue;

	SenderMetric(String name, String value, String screenValue) {
		this.name = name;
		this.value = value;
		this.screenValue = screenValue;
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
	public String getValue() {
		return value;
	}

	/**
	 * Retrieves {@link #screenValue}
	 *
	 * @return value of {@link #screenValue}
	 */
	public String getScreenValue() {
		return screenValue;
	}

	/**
	 * Retrieves {@link SenderMetric} by {@link #value}
	 *
	 * @param value {@link #value}
	 * @return {@link SenderMetric}
	 */
	public static SenderMetric getByValue(String value) {
		return Arrays.stream(values())
				.filter(receiver -> receiver.getValue().equalsIgnoreCase(value) || receiver.getScreenValue().equalsIgnoreCase(value))
				.findFirst()
				.orElse(null);
	}


}