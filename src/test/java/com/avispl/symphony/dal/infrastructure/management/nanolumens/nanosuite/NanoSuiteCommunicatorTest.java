/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.security.auth.login.FailedLoginException;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;

/**
 * NanoSuiteCommunicatorTest
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 4/1/2024
 * @since 1.0.0
 */
class NanoSuiteCommunicatorTest {
	private ExtendedStatistics extendedStatistic;
	private NanoSuiteCommunicator nanoSuiteCommunicator;

	@BeforeEach
	void setUp() throws Exception {
		nanoSuiteCommunicator = new NanoSuiteCommunicator();
		nanoSuiteCommunicator.setTrustAllCertificates(true);
		nanoSuiteCommunicator.setHost("");
		nanoSuiteCommunicator.setLogin("");
		nanoSuiteCommunicator.setPassword("");
		nanoSuiteCommunicator.setPort(80);
		nanoSuiteCommunicator.init();
		nanoSuiteCommunicator.connect();
	}

	@AfterEach
	void destroy() throws Exception {
		nanoSuiteCommunicator.disconnect();
		nanoSuiteCommunicator.destroy();
	}

	/**
	 * Testing ping successfully with TCP mode
	 */
	@Test
	void testTCPPing() throws Exception {
		nanoSuiteCommunicator = new NanoSuiteCommunicator();

		nanoSuiteCommunicator.setTrustAllCertificates(true);
		nanoSuiteCommunicator.setProtocol("http");
		nanoSuiteCommunicator.setPort(80);
		nanoSuiteCommunicator.setHost("");
		nanoSuiteCommunicator.setPingMode("TCP");
		nanoSuiteCommunicator.setContentType("text/plain");
		nanoSuiteCommunicator.setPassword("");
		nanoSuiteCommunicator.authenticate();
		nanoSuiteCommunicator.init();
		assertDoesNotThrow(() -> nanoSuiteCommunicator.ping(), "Expected not throw IllegalStateException");
	}

	/**
	 * Testing ping successfully with ICMP mode
	 */
	@Test
	void testICMPPing() throws Exception {
		nanoSuiteCommunicator.destroy();
		nanoSuiteCommunicator = new NanoSuiteCommunicator();

		nanoSuiteCommunicator.setTrustAllCertificates(true);
		nanoSuiteCommunicator.setProtocol("http");
		nanoSuiteCommunicator.setPort(80);
		nanoSuiteCommunicator.setHost("");
		nanoSuiteCommunicator.setPingMode("ICMP");
		nanoSuiteCommunicator.setPassword("");
		nanoSuiteCommunicator.authenticate();
		nanoSuiteCommunicator.init();
		assertDoesNotThrow(() -> nanoSuiteCommunicator.ping(), "Expected not throw IllegalStateException");
	}

	/**
	 * Test case to verify ResourceNotReachableException exception should be thrown when unable to send request to nanosuite
	 *
	 */
	@Test
	void testFailedToSendCommand() throws Exception {
		nanoSuiteCommunicator.destroy();
		nanoSuiteCommunicator = new NanoSuiteCommunicator();

		nanoSuiteCommunicator.setTrustAllCertificates(false);
		nanoSuiteCommunicator.setProtocol("http");
		nanoSuiteCommunicator.setPort(80);
		nanoSuiteCommunicator.setHost("");
		nanoSuiteCommunicator.setPingMode("ICMP");
		nanoSuiteCommunicator.setPassword("incorrect-password");
		nanoSuiteCommunicator.authenticate();
		nanoSuiteCommunicator.init();
		assertThrows(ResourceNotReachableException.class, () -> nanoSuiteCommunicator.getMultipleStatistics(), "Expected throw Resource not reach exception");
	}

	/**
	 * Test case for getting aggregated data from NanoSuite.
	 */
	@Test
	void testGetAggregatorData() throws Exception {
		extendedStatistic = (ExtendedStatistics) nanoSuiteCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = extendedStatistic.getStatistics();
		Assert.assertEquals(3, statistics.size());
	}

	/**
	 * Test case for aggregated general information retrieved from NanoSuite.
	 * Verifies specific properties of a device.
	 */
	@Test
	void testAggregatedGeneralInfo() throws Exception {
		nanoSuiteCommunicator.getMultipleStatistics();
		nanoSuiteCommunicator.retrieveMultipleStatistics();
		Thread.sleep(10000);

		List<AggregatedDevice> aggregatedDeviceList = nanoSuiteCommunicator.retrieveMultipleStatistics();
		String deviceId = "408";
		Optional<AggregatedDevice> aggregatedDevice = aggregatedDeviceList.stream().filter(item -> item.getDeviceId().equals(deviceId)).findFirst();
		if (aggregatedDevice.isPresent()) {
			Map<String, String> stats = aggregatedDevice.get().getProperties();

			assertEquals("408", aggregatedDevice.get().getDeviceId());
			assertEquals("MCTRL4K", aggregatedDevice.get().getDeviceModel());
			assertEquals("MCTRL4K", aggregatedDevice.get().getDeviceName());
			assertEquals(true, aggregatedDevice.get().getDeviceOnline());
			assertEquals("56", stats.get("SubsystemId"));
			assertEquals("NanoSuite", stats.get("SubsystemName"));
		}
	}

	/**
	 * Test case for retrieving multiple statistics with historical properties set to "Voltage(V)".
	 */
	@Test
	void testMultipleStatisticWithVoltageHistorical() throws Exception {
		nanoSuiteCommunicator.setHistoricalProperties("Voltage(V)");
		nanoSuiteCommunicator.getMultipleStatistics();
		nanoSuiteCommunicator.retrieveMultipleStatistics();
		Thread.sleep(10000);

		List<AggregatedDevice> aggregatedDeviceList = nanoSuiteCommunicator.retrieveMultipleStatistics();
		for (AggregatedDevice aggregatedDevice : aggregatedDeviceList) {
			if (Objects.equals(aggregatedDevice.getDeviceId(), "408")) {
				assertEquals(8, aggregatedDevice.getDynamicStatistics().size());
			}

			if (Objects.equals(aggregatedDevice.getDeviceId(), "409")) {
				assertEquals(4, aggregatedDevice.getDynamicStatistics().size());
			}
		}
	}

	/**
	 * Test case for retrieving multiple statistics with historical properties set to "Temperature(C)".
	 */
	@Test
	void testMultipleStatisticWithTemperatureHistorical() throws Exception {
		nanoSuiteCommunicator.setHistoricalProperties("Temperature(C)");
		nanoSuiteCommunicator.getMultipleStatistics();
		nanoSuiteCommunicator.retrieveMultipleStatistics();
		Thread.sleep(10000);

		List<AggregatedDevice> aggregatedDeviceList = nanoSuiteCommunicator.retrieveMultipleStatistics();
		for (AggregatedDevice aggregatedDevice : aggregatedDeviceList) {
			if (Objects.equals(aggregatedDevice.getDeviceId(), "408")) {
				assertEquals(8, aggregatedDevice.getDynamicStatistics().size());
			}

			if (Objects.equals(aggregatedDevice.getDeviceId(), "409")) {
				assertEquals(4, aggregatedDevice.getDynamicStatistics().size());
			}
		}
	}

	/**
	 * Test case for retrieving multiple statistics with historical properties set to "Voltage(V), Temperature(C)".
	 */
	@Test
	void testMultipleStatisticWithHistorical() throws Exception {
		nanoSuiteCommunicator.setHistoricalProperties("Voltage(V), Temperature(C)");
		nanoSuiteCommunicator.getMultipleStatistics();
		nanoSuiteCommunicator.retrieveMultipleStatistics();
		Thread.sleep(10000);

		List<AggregatedDevice> aggregatedDeviceList = nanoSuiteCommunicator.retrieveMultipleStatistics();
		for (AggregatedDevice aggregatedDevice : aggregatedDeviceList) {
			if (Objects.equals(aggregatedDevice.getDeviceId(), "408")) {
				assertEquals(16, aggregatedDevice.getDynamicStatistics().size());
			}

			if (Objects.equals(aggregatedDevice.getDeviceId(), "409")) {
				assertEquals(8, aggregatedDevice.getDynamicStatistics().size());
			}
		}
	}

	/**
	 * Test filter by screen name
	 */
	@Test
	void testScreenNameFilter() throws Exception {
		nanoSuiteCommunicator.setScreenNameFilter("MSD600");
		nanoSuiteCommunicator.getMultipleStatistics();
		nanoSuiteCommunicator.retrieveMultipleStatistics();
		Thread.sleep(10000);
		List<AggregatedDevice> aggregatedDeviceList = nanoSuiteCommunicator.retrieveMultipleStatistics();
		assertEquals(1, aggregatedDeviceList.size());
		assertEquals("MSD600", aggregatedDeviceList.get(0).getDeviceName());
	}
}