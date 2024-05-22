/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * SystemInformation includes information of aggregator device
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/27/2024
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemInformation {
	private String systemName;
	private String systemHostname;
	private String timezone;

	/**
	 * Retrieves {@link #systemName}
	 *
	 * @return value of {@link #systemName}
	 */
	public String getSystemName() {
		return systemName;
	}

	/**
	 * Sets {@link #systemName} value
	 *
	 * @param systemName new value of {@link #systemName}
	 */
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	/**
	 * Retrieves {@link #systemHostname}
	 *
	 * @return value of {@link #systemHostname}
	 */
	public String getSystemHostname() {
		return systemHostname;
	}

	/**
	 * Sets {@link #systemHostname} value
	 *
	 * @param systemHostname new value of {@link #systemHostname}
	 */
	public void setSystemHostname(String systemHostname) {
		this.systemHostname = systemHostname;
	}

	/**
	 * Retrieves {@link #timezone}
	 *
	 * @return value of {@link #timezone}
	 */
	public String getTimezone() {
		return timezone;
	}

	/**
	 * Sets {@link #timezone} value
	 *
	 * @param timezone new value of {@link #timezone}
	 */
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
}