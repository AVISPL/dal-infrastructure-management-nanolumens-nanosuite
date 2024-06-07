/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common;

import java.util.Arrays;
import java.util.Objects;

/**
 * Ping mode - ICMP vs TCP
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 1/4/2024
 * @since 1.0.0
 */
public enum PingMode {
	ICMP("ICMP"), TCP("TCP");

	private String mode;

	PingMode(String mode) {
		this.mode = mode;
	}

	/**
	 * Retrieve {@link PingMode} instance based on the text value of the mode
	 *
	 * @param mode name of the mode to retrieve
	 * @return instance of {@link PingMode}
	 */
	public static PingMode ofString(String mode) {
		return Arrays.stream(values())
				.filter(pingMode -> Objects.equals(mode, pingMode.mode))
				.findFirst()
				.orElse(ICMP);
	}
}