/*
 * Copyright (c) 2026 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common.NanoSuiteConstant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * The Symphony monitoring call doesn't wait on the NanoSuite API, and a collection pass stops at the first timeout
 * (SYAL-2377). Before, getMultipleStatistics() sent 2 requests inline and swallowed timeouts, adding ~60 s to each
 * monitoring cycle, so the cycle drifted past the monitoring rate.
 */
@Tag("Mock")
public class NanoSuiteMonitoringCycleTest {

	/** Communicator whose GET requests time out, without a NanoSuite server. */
	static class TestCommunicator extends NanoSuiteCommunicator {
		final AtomicInteger getCalls = new AtomicInteger();

		TestCommunicator() throws java.io.IOException {
			super();
		}

		@Override
		protected <Response> Response doGet(String uri, Class<Response> responseClass) {
			getCalls.incrementAndGet();
			throw new ResourceNotReachableException("Cannot reach resource at " + uri + ": Read timed out");
		}
	}

	/** Communicator whose NanoSuite returns two screens, each with asset details. */
	static class TwoScreensCommunicator extends NanoSuiteCommunicator {
		private final ObjectMapper mapper = new ObjectMapper();

		TwoScreensCommunicator() throws java.io.IOException {
			super();
		}

		@Override
		@SuppressWarnings("unchecked")
		protected <Response> Response doGet(String uri, Class<Response> responseClass) {
			if (uri.equals(NanoSuiteConstant.ISSAC_SETTING_URL)) {
				return (Response) mapper.createObjectNode();
			}
			ArrayNode array = mapper.createArrayNode();
			if (uri.equals(NanoSuiteConstant.SCREEN_ASSET_URL)) {
				for (String name : new String[] { "Screen 1", "Screen 2" }) {
					array.addObject().putObject(NanoSuiteConstant.METADATA).put(NanoSuiteConstant.NOVASTAR_SCREEN_NAME, name);
				}
			} else {
				array.addObject();
			}
			return (Response) (JsonNode) array;
		}
	}

	private TestCommunicator communicator;

	@BeforeEach
	void setUp() throws Exception {
		communicator = new TestCommunicator();
		communicator.setHost("127.0.0.1");
	}

	private void runCollectionPass() throws Throwable {
		Method method = NanoSuiteCommunicator.class.getDeclaredMethod("collectData");
		method.setAccessible(true);
		try {
			method.invoke(communicator);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	@Test
	void testGetMultipleStatisticsSendsNoRequests() throws Exception {
		communicator.getMultipleStatistics();
		Assertions.assertEquals(0, communicator.getCalls.get(), "getMultipleStatistics must only read collected data");
	}

	@Test
	void testCollectionPassStopsAtFirstTimeout() throws Throwable {
		runCollectionPass();
		Assertions.assertEquals(1, communicator.getCalls.get(), "no further requests after a timeout");
	}

	@Test
	void testTimeoutReportedWithoutWaiting() throws Throwable {
		runCollectionPass();
		int calls = communicator.getCalls.get();
		Assertions.assertThrows(ResourceNotReachableException.class, () -> communicator.getMultipleStatistics());
		Assertions.assertEquals(calls, communicator.getCalls.get());
	}

	@Test
	void testMonitoredDevicesTotalCountsCollectedDevices() throws Throwable {
		TwoScreensCommunicator twoScreens = new TwoScreensCommunicator();
		twoScreens.setHost("127.0.0.1");
		Method method = NanoSuiteCommunicator.class.getDeclaredMethod("collectData");
		method.setAccessible(true);
		method.invoke(twoScreens);
		ExtendedStatistics stats = (ExtendedStatistics) twoScreens.getMultipleStatistics().get(0);
		Assertions.assertEquals("2", stats.getDynamicStatistics().get(NanoSuiteConstant.MONITORED_DEVICES_TOTAL),
				"total must match the collected devices before retrieveMultipleStatistics is called");
	}
}
