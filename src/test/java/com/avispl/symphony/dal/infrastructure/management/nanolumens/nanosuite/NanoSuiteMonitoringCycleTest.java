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

import com.avispl.symphony.api.dal.error.ResourceNotReachableException;

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
}
