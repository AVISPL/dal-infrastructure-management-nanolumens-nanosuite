/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.metric;

/**
 * ScreenMetric class represents metric of novastar screen/display asset type
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/31/2024
 * @since 1.0.0
 */
public enum ScreenMetric {

	SENDER_GROUP("Sender"),
	RECEIVER_GROUP("Receiver");
	private String group;

	ScreenMetric(String group) {
		this.group = group;
	}

	/**
	 * Retrieves {@link #group}
	 *
	 * @return value of {@link #group}
	 */
	public String getGroup() {
		return group;
	}


	/**
	 * Retrieve group name of screen/display asset by value
	 */
	public static String getByValue(String value) {
		SenderMetric senderMetric = SenderMetric.getByValue(value);
		if (senderMetric != null) {
			return SENDER_GROUP.getGroup() + senderMetric.getName();
		}

		ReceiverMetric receiverMetric = ReceiverMetric.getByValue(value);
		if (receiverMetric!= null) {
			return RECEIVER_GROUP.getGroup() + receiverMetric.getName();
		}
		return null;
	}
}