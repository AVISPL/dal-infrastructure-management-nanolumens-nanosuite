/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common;

import java.util.Arrays;

/**
 * ProfileType class represents profile type of aggregated device
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/31/2024
 * @since 1.0.0
 */
public enum ProfileType {

	NOVASTAR_RECEIVER("Novastar Receiver", "novastar_receiver"),
	NOVASTAR_SENDER("Novastar Sender", "novastar_sender"),
	NOVASTAR_SCREEN("Novastar Screen", "novastar_screen");

	private String name;
	private String value;

	ProfileType(String name, String value) {
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
	public String getValue() {
		return value;
	}

	/**
   * Retrieves {@link ProfileType} by {@link #value}
   *
   * @param value {@link #value}
   * @return {@link ProfileType}
	 */
	public static ProfileType getByValue(String value) {
		return Arrays.stream(values())
				.filter(type -> type.getValue().equalsIgnoreCase(value))
				.findFirst()
				.orElse(null);
	}
}