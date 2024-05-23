/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.metric;

import java.util.Arrays;

/**
 * ReceiverMetric class represents metric of novastar receiver asset type
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/31/2024
 * @since 1.0.0
 */
public enum ReceiverMetric {
	BRIGHTNESS_BLUE("BrightnessBlue","novastar_receiver_brightness_blue","novastar_screen,receiver_brightness_blue"),
	BRIGHTNESS_GREEN("BrightnessGreen","novastar_receiver_brightness_green","novastar_screen_receiver_brightness_green"),
	BRIGHTNESS_RED("BrightnessRed","novastar_receiver_brightness_red","novastar_screen_receiver_brightness_red"),
	BRIGHTNESS_VRED("BrightnessVRed","novastar_receiver_brightness_vred","novastar_screen_receiver_brightness_vred"),
	BRIGHTNESS("Brightness","novastar_receiver_brightness","novastar_screen_receiver_brightness"),
	GAMMA("Gamma","novastar_receiver_gamma","novastar_screen_receiver_gamma"),
	MODEL("Model","novastar_receiver_model","novastar_screen_receiver_model"),
	TEMPERATURE("Temperature(C)","novastar_receiver_temperature","novastar_screen_receiver_temperature"),
	VERSION_FPGA("VersionFPGA","novastar_receiver_version_fpga","novastar_screen_receiver_version_fpga"),
	VERSION_MCU("VersionMCU","novastar_receiver_version_mcu","novastar_screen_receiver_version_mcu"),
	VERSION_SOFTWARE("VersionSoftware","novastar_receiver_version_software","novastar_screen_receiver_version_software"),
	VIDEO_BLACKOUT("VideoBlackout","novastar_receiver_video_blackout","novastar_screen_receiver_video_blackout"),
	VIDEO_FREEZE("VideoFreeze","novastar_receiver_video_freeze","novastar_screen_receiver_video_freeze"),
	VIDEO_MAPPING("VideoMapping","novastar_receiver_video_mapping","novastar_screen_receiver_video_mapping"),
	VIDEO_TEST("VideoTest","novastar_receiver_video_test","novastar_screen_receiver_video_test"),
	VOLTAGE("Voltage(V)","novastar_receiver_voltage","novastar_screen_receiver_voltage");

	private String name;
	private String value;
	private String screenValue;

	ReceiverMetric(String name, String value, String screenValue) {
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
	 * Retrieves {@link ReceiverMetric} by {@link #value}
	 *
	 * @param value {@link #value}
	 * @return {@link ReceiverMetric}
	 */
	 public static ReceiverMetric getByValue(String value) {
		 return Arrays.stream(values())
				 .filter(receiver -> receiver.getValue().equalsIgnoreCase(value) || receiver.getScreenValue().equalsIgnoreCase(value))
				 .findFirst()
				 .orElse(null);
	 }
}