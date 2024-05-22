/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common;

/**
 * Class containing constant values used in NanoSuite communication.
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/27/2024
 * @since 1.0.0
 */
public class NanoSuiteConstant {
		public static final String NONE = "None";
		public static final String METADATA = "metadata";
		public static final String HEALTHSTATE = "HealthState";
		public static final String PROFILE_TYPE = "ProfileType";
		public static final String SUBSYSTEM_NAME = "SubsystemName";
		public static final String SUBSYSTEM_ID = "SubsystemId";
		public static final String HASH = "#";
		public static final String ERROR = "error";
		public static final String ISSAC_SETTING_URL = "api/v1/settings";
		public static final int DEFAULT_NUMBER_THREAD = 8;
		public static final String FILTER_ASSET_URL = "api/v1/infra/assets?profileType=%s&metadata[novastarScreenName]=%s";
		public static final String SCREEN_ASSET_URL =  "api/v1/infra/assets?profileType=novastar_screen";
		public static final String NOVASTAR_SCREEN_NAME = "novastarScreenName";
		public static final String ISAAC_TOKEN = "isaac-token";
		public static final String NOVASTAR_SCREEN = "NovastarScreen";
}