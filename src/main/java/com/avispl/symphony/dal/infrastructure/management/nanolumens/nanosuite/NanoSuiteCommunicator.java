/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite;

import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.security.auth.login.FailedLoginException;
import org.apache.commons.lang3.math.NumberUtils;

import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.error.CommandFailureException;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.monitor.aggregator.Aggregator;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common.AggregatedDeviceInfo;
import com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common.DeviceMetric;
import com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common.HealthStateStatus;
import com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common.NanoSuiteConstant;
import com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common.PingMode;
import com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common.ProfileType;
import com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.common.SystemInformation;
import com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.metric.ReceiverMetric;
import com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.metric.ScreenMetric;
import com.avispl.symphony.dal.infrastructure.management.nanolumens.nanosuite.metric.SenderMetric;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * NanoSuiteCommunicator
 * Supported features are:
 * Monitoring Aggregator device:
 * <ul>
 *   <li>SystemName</li>
 *   <li>SystemHostname</li>
 *   <li>Timezone</li>
 * </ul>
 *
 * General Info Aggregated Device:
 * <ul>
 *   <li>deviceId</li>
 *   <li>deviceOnline</li>
 *   <li>deviceName</li>
 *   <li>deviceModel</li>
 *   </li>HealthState</li>
 *   <li>SubsystemName</li>
 *   <li>SubsystemId</li>
 * </ul>
 *
 *
 * @author Kevin / Symphony Dev Team<br>
 * Created on 3/26/2024
 * @since 1.0.0
 */
public class NanoSuiteCommunicator extends RestCommunicator implements Aggregator, Monitorable {

	/**
	 * Process that is running constantly and triggers collecting data from NanoSuite API endpoints, based on the given timeouts and thresholds.
	 *
	 * @author Kevin / Symphony Dev Team<br>
	 * @since 1.0.0
	 */
	class NanoSuiteDataLoader implements Runnable {
		private volatile boolean inProgress;
		private volatile boolean flag = false;

		public NanoSuiteDataLoader() {
			this.inProgress = true;
		}

		@Override
		public void run() {
			loop:
			while (inProgress) {
				try {
					TimeUnit.MICROSECONDS.sleep(500);
				} catch (InterruptedException e) {
					logger.info(String.format("Sleep for 0.5 second was interrupted with error message %s", e.getMessage()));
				}

				if (!inProgress) {
					break loop;
				}
				// next line will determine whether NanoSuite monitoring was paused
				updateAggregatorStatus();
				if (devicePaused) {
					continue loop;
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Fetching other than aggregated device list");
				}

				long currentTimestamp = System.currentTimeMillis();
				if (!flag && nextDevicesCollectionIterationTimestamp <= currentTimestamp) {
					populateDeviceDetails();
					flag = true;
				}

				while (nextDevicesCollectionIterationTimestamp > System.currentTimeMillis()) {
					try {
						TimeUnit.MILLISECONDS.sleep(1000);
					} catch (InterruptedException e) {
						logger.info(String.format("Sleep for 1 second was interrupted with error message %s", e.getMessage()));
					}
				}

				if (!inProgress) {
					break loop;
				}

				if (flag) {
					nextDevicesCollectionIterationTimestamp = System.currentTimeMillis() + 30000;
					flag = false;
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Finished collecting devices statistics cycle at " + new Date());
				}
			}
		}

		public void stop() {
			this.inProgress = false;
		}
	}

	/**
	 * Indicates whether a device is considered as paused.
	 * True by default so if the system is rebooted and the actual value is lost -> the device won't start stats
	 * collection unless the {@link NanoSuiteCommunicator#retrieveMultipleStatistics()} method is called which will change it
	 * to a correct value
	 */
	private volatile boolean devicePaused = true;

	/**
	 * We don't want the statistics to be collected constantly, because if there's not a big list of devices -
	 * new devices' statistics loop will be launched before the next monitoring iteration. To avoid that -
	 * this variable stores a timestamp which validates it, so when the devices' statistics is done collecting, variable
	 * is set to currentTime + 30s, at the same time, calling {@link #retrieveMultipleStatistics()} and updating the
	 */
	private long nextDevicesCollectionIterationTimestamp;

	/**
	 * This parameter holds timestamp of when we need to stop performing API calls
	 * It used when device stop retrieving statistic. Updated each time of called #retrieveMultipleStatistics
	 */
	private volatile long validRetrieveStatisticsTimestamp;

	/**
	 * Aggregator inactivity timeout. If the {@link NanoSuiteCommunicator#retrieveMultipleStatistics()}  method is not
	 * called during this period of time - device is considered to be paused, thus the Cloud API
	 * is not supposed to be called
	 */
	private static final long retrieveStatisticsTimeOut = 3 * 60 * 1000;

	/**
	 * Filter screen name
	 */
	private String screenNameFilter;

	/**
	 * Update the status of the device.
	 * The device is considered as paused if did not receive any retrieveMultipleStatistics()
	 * calls during {@link NanoSuiteCommunicator}
	 */
	private synchronized void updateAggregatorStatus() {
		devicePaused = validRetrieveStatisticsTimestamp < System.currentTimeMillis();
	}

	/**
	 * Uptime time stamp to valid one
	 */
	private synchronized void updateValidRetrieveStatisticsTimestamp() {
		validRetrieveStatisticsTimestamp = System.currentTimeMillis() + retrieveStatisticsTimeOut;
		updateAggregatorStatus();
	}

	/**
	 * A mapper for reading and writing JSON using Jackson library.
	 * ObjectMapper provides functionality for converting between Java objects and JSON.
	 * It can be used to serialize objects to JSON format, and deserialize JSON data to objects.
	 */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Executor that runs all the async operations, that is posting and
	 */
	private ExecutorService executorService;

	/**
	 * A private field that represents an instance of the NanoSuiteDataLoader class, which is responsible for loading device data for Nano Suite
	 */
	private NanoSuiteDataLoader deviceDataLoader;

	/**
	 * A private final ReentrantLock instance used to provide exclusive access to a shared resource
	 * that can be accessed by multiple threads concurrently. This lock allows multiple reentrant
	 * locks on the same shared resource by the same thread.
	 */
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/**
	 * Private variable representing the local extended statistics.
	 */
	private ExtendedStatistics localExtendedStatistics;

	/**
	 * List of aggregated device
	 */
	private final List<AggregatedDevice> aggregatedDeviceList = Collections.synchronizedList(new ArrayList<>());

	/**
	 * cache data for aggregated
	 */
	private final Map<String, Map<String,JsonNode>> cachedData = Collections.synchronizedMap(new HashMap<>());

	/**
	 * List of System Response
	 */
	private SystemInformation systemInformation = new SystemInformation();

	/**
	 * Ping mode
	 */
	private PingMode pingMode = PingMode.ICMP;

	/**
	 * Configurable property for historical properties, comma separated values kept as set locally
	 */
	private final Set<String> historicalProperties = new HashSet<>();

	/**
	 * Retrieves {@link #historicalProperties}
	 *
	 * @return value of {@link #historicalProperties}
	 */
	public String getHistoricalProperties() {
		return String.join(",", this.historicalProperties);
	}

	/**
	 * Sets {@link #historicalProperties} value
	 *
	 * @param historicalProperties new value of {@link #historicalProperties}
	 */
	public void setHistoricalProperties(String historicalProperties) {
		this.historicalProperties.clear();
		Arrays.asList(historicalProperties.split(",")).forEach(propertyName -> {
			this.historicalProperties.add(propertyName.trim());
		});
	}

	/**
	 * Number of thread
	 */
	private String numberThreads;

	/**
	 * Retrieves {@link #numberThreads}
	 *
	 * @return value of {@link #numberThreads}
	 */
	public String getNumberThreads() {
		return numberThreads;
	}

	/**
	 * Sets {@link #numberThreads} value
	 *
	 * @param numberThreads new value of {@link #numberThreads}
	 */
	public void setNumberThreads(String numberThreads) {
		this.numberThreads = numberThreads;
	}

	/**
	 * Constructs a new instance of NanoSuiteCommunicator.
	 */
	public NanoSuiteCommunicator() {
		this.setTrustAllCertificates(false);
	}

	/**
	 * Retrieves {@link #screenNameFilter}
	 *
	 * @return value of {@link #screenNameFilter}
	 */
	public String getScreenNameFilter() {
		return screenNameFilter;
	}

	/**
	 * Sets {@link #screenNameFilter} value
	 *
	 * @param screenNameFilter new value of {@link #screenNameFilter}
	 */
	public void setScreenNameFilter(String screenNameFilter) {
		this.screenNameFilter = screenNameFilter;
	}

	/**
	 * Retrieves {@link #pingMode}
	 *
	 * @return value of {@link #pingMode}
	 */
	public String getPingMode() {
		return pingMode.name();
	}

	/**
	 * Sets {@link #pingMode} value
	 *
	 * @param pingMode new value of {@link #pingMode}
	 */
	public void setPingMode(String pingMode) {
		this.pingMode = PingMode.ofString(pingMode);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 *
	 * Check for available devices before retrieving the value
	 * ping latency information to Symphony
	 */
	@Override
	public int ping() throws Exception {
		if (this.pingMode == PingMode.ICMP) {
			return super.ping();
		} else if (this.pingMode == PingMode.TCP) {
			if (isInitialized()) {
				long pingResultTotal = 0L;

				for (int i = 0; i < this.getPingAttempts(); i++) {
					long startTime = System.currentTimeMillis();

					try (Socket puSocketConnection = new Socket(this.host, this.getPort())) {
						puSocketConnection.setSoTimeout(this.getPingTimeout());
						if (puSocketConnection.isConnected()) {
							long pingResult = System.currentTimeMillis() - startTime;
							pingResultTotal += pingResult;
							if (this.logger.isTraceEnabled()) {
								this.logger.trace(String.format("PING OK: Attempt #%s to connect to %s on port %s succeeded in %s ms", i + 1, host, this.getPort(), pingResult));
							}
						} else {
							if (this.logger.isDebugEnabled()) {
								logger.debug(String.format("PING DISCONNECTED: Connection to %s did not succeed within the timeout period of %sms", host, this.getPingTimeout()));
							}
							return this.getPingTimeout();
						}
					} catch (SocketTimeoutException | ConnectException tex) {
						throw new SocketTimeoutException("Socket connection timed out");
					} catch (UnknownHostException tex) {
						throw new SocketTimeoutException("Socket connection timed out" + tex.getMessage());
					} catch (Exception e) {
						if (this.logger.isWarnEnabled()) {
							this.logger.warn(String.format("PING TIMEOUT: Connection to %s did not succeed, UNKNOWN ERROR %s: ", host, e.getMessage()));
						}
						return this.getPingTimeout();
					}
				}
				return Math.max(1, Math.toIntExact(pingResultTotal / this.getPingAttempts()));
			} else {
				throw new IllegalStateException("Cannot use device class without calling init() first");
			}
		} else {
			throw new IllegalArgumentException("Unknown PING Mode: " + pingMode);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		reentrantLock.lock();
		try {
			Map<String, String> statistics = new HashMap<>();
			ExtendedStatistics extendedStatistics = new ExtendedStatistics();
			retrieveSystemInfo();
			retrieveScreenAsset();
			populateSystemInfo(statistics);
			extendedStatistics.setStatistics(statistics);
			localExtendedStatistics = extendedStatistics;
		} finally {
			reentrantLock.unlock();
		}
		return Collections.singletonList(localExtendedStatistics);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics() throws Exception {
		if (executorService == null) {
			executorService = Executors.newFixedThreadPool(1);
			executorService.submit(deviceDataLoader = new NanoSuiteDataLoader());
		}
		nextDevicesCollectionIterationTimestamp = System.currentTimeMillis();
		updateValidRetrieveStatisticsTimestamp();
		for (Map.Entry<String, Map<String,JsonNode>> entry : cachedData.entrySet()) {
			if (entry.getValue().size() == 1) {
				return Collections.emptyList();
			}
		}
		return cloneAndPopulateAggregatedDeviceList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics(List<String> list) throws Exception {
		return retrieveMultipleStatistics().stream().filter(aggregatedDevice -> list.contains(aggregatedDevice.getDeviceId())).collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void authenticate() throws Exception {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalInit() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Internal init is called.");
		}
		executorService = Executors.newFixedThreadPool(1);
		executorService.submit(deviceDataLoader = new NanoSuiteDataLoader());
		super.internalInit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalDestroy() {
		if (logger.isDebugEnabled()) {
			logger.debug("Internal destroy is called.");
		}
		if (deviceDataLoader != null) {
			deviceDataLoader.stop();
			deviceDataLoader = null;
		}
		if (executorService != null) {
			executorService.shutdownNow();
			executorService = null;
		}
		if (localExtendedStatistics != null && localExtendedStatistics.getStatistics() != null) {
			localExtendedStatistics.getStatistics().clear();
		}
		nextDevicesCollectionIterationTimestamp = 0;
		aggregatedDeviceList.clear();
		cachedData.clear();
		super.internalDestroy();
	}

	/**
	 * {@inheritDoc}
	 * set API token into Header of Request
	 */
	@Override
	protected HttpHeaders putExtraRequestHeaders(HttpMethod httpMethod, String uri, HttpHeaders headers) throws Exception {
		if (!StringUtils.isNullOrEmpty(this.getPassword())) {
			headers.set(NanoSuiteConstant.ISAAC_TOKEN, this.getPassword());
		}
		return headers;
	}

	/**
	 * Populates system information into the provided stats map
	 * System information includes properties from the {@link SystemInformation} enumeration.
	 *
	 * @param stats The map to store system information properties.
	 */
	private void populateSystemInfo(Map<String, String> stats) {
		Map<String, Object> properties = convertObjectToMap(systemInformation);
		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			stats.put(entry.getKey(), checkNullOrEmptyValue(entry.getValue()));
		}
	}

	/**
	 * Populates device details using multiple threads.
	 * Retrieves aggregated data for each device in the device list concurrently.
	 */
	private void populateDeviceDetails() {
		int numberOfThreads = getDefaultNumberOfThread();
		ExecutorService executorServiceRetrieveAggregatedData = Executors.newFixedThreadPool(numberOfThreads);
		List<Future<?>> futures = new ArrayList<>();

		synchronized (cachedData) {
			for (Map.Entry<String, Map<String, JsonNode>> entry : cachedData.entrySet()) {
				Future<?> future = executorServiceRetrieveAggregatedData.submit(() -> retrieveDeviceAsset(entry.getKey()));
				futures.add(future);
			}
		}
		waitForFutures(futures, executorServiceRetrieveAggregatedData);
		executorServiceRetrieveAggregatedData.shutdown();
	}

	/**
	 * Retrieve list assets of specific device by send GET request to NanoSuite endpoint.
	 *
	 * @param deviceName name of the device
	 */
	private void retrieveDeviceAsset(String deviceName) {
		try {
			for (ProfileType profileType: ProfileType.values()) {
				if (!profileType.getName().equalsIgnoreCase(ProfileType.NOVASTAR_SCREEN.getName())) {
					String url = createFilterUrl(profileType, deviceName);
					JsonNode response = this.doGet(url, JsonNode.class);
					if (response != null && !StringUtils.isNullOrEmpty(response.toString()) && !NanoSuiteConstant.ERROR.contains(response.toString())) {
						Map<String, JsonNode> assets = new HashMap<>();
						assets.put(profileType.getValue(), response);
						updateCacheData(deviceName, assets);
					}
				}
			}
		} catch (Exception e) {
			logger.error(String.format("An error occurred when retrieving device asset %s", e.getMessage()), e);
		}
	}

	/**
	 * Get system information of NanoSuite by sending GET request to NanoSuite API endpoint.
	 *
	 * @throws FailedLoginException If there's an issue with the login credentials. This could happen if the password is incorrect.
	 * @throws ResourceNotReachableException If there's an error reaching the NanoSuite API or retrieving system information.
	 */
	private void retrieveSystemInfo() throws Exception {
		try {
			JsonNode response = this.doGet(NanoSuiteConstant.ISSAC_SETTING_URL, JsonNode.class);
			if (response != null && !response.has(NanoSuiteConstant.ERROR)) {
				systemInformation = objectMapper.treeToValue(response, SystemInformation.class);
			}
		} catch (FailedLoginException e) {
			throw new FailedLoginException("Error when login to system. Please check the credentials");
		} catch (CommandFailureException e) {
			throw new ResourceNotReachableException("An error occurred when retrieving the system information", e);
		} catch (Exception e) {
			logger.error(String.format("An error occurred when retrieving the system information %s", e.getMessage()), e);
		}
	}

	/**
	 * Retrieve list of screens to monitoring by send GET request to NanoSuite API endpoint.
	 *
	 * @throws FailedLoginException If there's an issue with the login credentials. This could happen if the password is incorrect.
	 * @throws ResourceNotReachableException If there's an error reaching the NanoSuite API or screen asset information.
	 */
	private void retrieveScreenAsset() throws Exception {
		try {
			String uri = StringUtils.isNullOrEmpty(this.screenNameFilter) ? NanoSuiteConstant.SCREEN_ASSET_URL : createFilterUrl(ProfileType.NOVASTAR_SCREEN, this.screenNameFilter);
			JsonNode response = this.doGet(uri, JsonNode.class);

			if (response != null && !StringUtils.isNullOrEmpty(response.toString()) && !NanoSuiteConstant.ERROR.contains(response.toString()) && response.isArray()) {
				List<JsonNode> devices = objectMapper.readerFor(new TypeReference<List<JsonNode>>(){}).readValue(response);
				if (devices == null || devices.isEmpty()) return;

				for (JsonNode device : devices) {
					JsonNode metadata = device.get(NanoSuiteConstant.METADATA);
					if (metadata == null) continue;

					String screenName = metadata.get(NanoSuiteConstant.NOVASTAR_SCREEN_NAME).asText();
					if (screenName == null) continue;

					Map<String, JsonNode> assets = new HashMap<>();
					assets.put(ProfileType.NOVASTAR_SCREEN.getValue(), objectMapper.createArrayNode().add(device));
					updateCacheData(screenName, assets);
				}
			}
		} catch (FailedLoginException e) {
			throw new FailedLoginException("Error when login to system. Please check the credentials");
		} catch (CommandFailureException e) {
			throw new ResourceNotReachableException("An error occurred when retrieving the screen asset information", e);
		} catch (Exception e) {
			logger.error(String.format("An error occurred when retrieving the screen asset information. %s", e.getMessage()), e);
		}
	}

	/**
	 * Update list assets of specific device
	 *
	 * @param deviceName name of the device
	 * @param value list assets of device
	 */
	private void updateCacheData(String deviceName, Map<String, JsonNode> value) {
		synchronized (cachedData) {
			Map<String, JsonNode> map = new HashMap<>();
			if (cachedData.containsKey(deviceName)) {
				map = cachedData.get(deviceName);
			}
			map.putAll(value);
			cachedData.put(deviceName, map);
		}
	}

	/**
	 * Waits for the completion of all futures in the provided list and then shuts down the executor service.
	 *
	 * @param futures The list of Future objects representing asynchronous tasks.
	 * @param executorService The ExecutorService to be shut down.
	 */
	private void waitForFutures(List<Future<?>> futures, ExecutorService executorService) {
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				logger.error("An exception occurred while waiting for a future to complete.", e);
			}
		}
		executorService.shutdown();
	}

	/**
	 * create a specific url for fetching asset type base on filter
	 */
	private String createFilterUrl(ProfileType profileType, String screenNameFilter) {
		 return String.format(NanoSuiteConstant.FILTER_ASSET_URL, profileType.getValue(), !StringUtils.isNullOrEmpty(this.screenNameFilter)? this.screenNameFilter : screenNameFilter);
	}

	/**
	 * Clones and populates a new list of aggregated devices with mapped monitoring properties.
	 *
	 * @return A new list of {@link AggregatedDevice} objects with mapped monitoring properties.
	 */
	private List<AggregatedDevice> cloneAndPopulateAggregatedDeviceList() {
		synchronized (cachedData) {
			aggregatedDeviceList.clear();
			cachedData.forEach((deviceName, info) -> {
				AggregatedDevice aggregatedDevice = new AggregatedDevice();
				aggregatedDevice.setDeviceModel(deviceName);
				aggregatedDevice.setDeviceName(deviceName);
				aggregatedDevice.setDeviceOnline(true);

				Map<String, String> stats = new HashMap<>();
				Map<String, String> dynamicStats = new HashMap<>();
				populateMonitoringProperties(stats, dynamicStats, info, aggregatedDevice);
				aggregatedDevice.setProperties(stats);
				aggregatedDevice.setDynamicStatistics(dynamicStats);
				aggregatedDeviceList.add(aggregatedDevice);
			});
		}
		return aggregatedDeviceList;
	}

	/**
	 * Populate monitoring properties including general device info, device assets.
	 *
	 * @param stats map to store monitor properties.
	 * @param dynamicStats map to store historical properties
	 * @param deviceInfos cache data to contain aggregated device information.
	 * @param aggregatedDevice aggregated device information.
	 */
	private void populateMonitoringProperties(Map<String, String> stats, Map<String, String> dynamicStats, Map<String,JsonNode> deviceInfos, AggregatedDevice aggregatedDevice) {
		try {
			for (Map.Entry<String, JsonNode> deviceInfo : deviceInfos.entrySet()) {
				JsonNode deviceNode = deviceInfo.getValue();

				if (deviceNode == null || !deviceNode.isArray()) continue;
				List<AggregatedDeviceInfo> devices = objectMapper.readerFor(new TypeReference<List<AggregatedDeviceInfo>>() {}).readValue(deviceNode);

				for (AggregatedDeviceInfo device: devices) {
					if (device != null) {
						// general info
						Map<String, Object> metadata = convertObjectToMap(device.getMetadata());
						aggregatedDevice.setDeviceId(metadata.get(NanoSuiteConstant.NOVASTAR_SCREEN).toString());
						stats.put(NanoSuiteConstant.SUBSYSTEM_NAME, checkNullOrEmptyValue(metadata.get(NanoSuiteConstant.SUBSYSTEM_NAME)));
						stats.put(NanoSuiteConstant.SUBSYSTEM_ID, checkNullOrEmptyValue(metadata.get(NanoSuiteConstant.SUBSYSTEM_ID)));

						// asset group
						List<DeviceMetric> metrics = device.getMetrics();
						String metricGroup = capitalizeFirstLetterOfEachWord(device.getDisplayName()).replace(",", "").concat(NanoSuiteConstant.HASH);
						ProfileType profileType = ProfileType.getByValue(device.getProfileType());
						if (metrics != null && !metrics.isEmpty() && profileType != null) {
							stats.put(metricGroup + NanoSuiteConstant.PROFILE_TYPE, checkNullOrEmptyValue(profileType.getName()));

							for (DeviceMetric metric : metrics) {
								switch (profileType) {
									case NOVASTAR_RECEIVER:
										ReceiverMetric receiverMetric = ReceiverMetric.getByValue(metric.getMetricType());
										String receiverAssetGroup = metricGroup + receiverMetric.getName();
										boolean isHistoricalProperty = !historicalProperties.isEmpty() && historicalProperties.contains(receiverMetric.getName());

										String value = checkNullOrEmptyValue(metric.getLastValue());
										if (isHistoricalProperty && !NanoSuiteConstant.NONE.equals(value)) {
											dynamicStats.put(receiverAssetGroup, roundDoubleValue(value));
										} else {
											stats.put(receiverAssetGroup, NumberUtils.isCreatable(value) ? roundDoubleValue(value): value);
										}
										break;
									case NOVASTAR_SENDER:
										SenderMetric senderMetric = SenderMetric.getByValue(metric.getMetricType());
										String senderAssetGroup = metricGroup + senderMetric.getName();
										stats.put(senderAssetGroup, checkNullOrEmptyValue(metric.getLastValue()));
										break;
									case NOVASTAR_SCREEN:
										String screenMetric = ScreenMetric.getByValue(metric.getMetricType());
										String screenAssetGroup = metricGroup + screenMetric;
										if (screenMetric != null) {
											String metricValue = checkNullOrEmptyValue(metric.getLastValue());
											if (NanoSuiteConstant.NONE.equals(metricValue)) {
												stats.put(screenAssetGroup, NanoSuiteConstant.NONE);
												break;
											}

											HealthStateStatus status = HealthStateStatus.getByValue(Integer.parseInt(metric.getLastValue()));
											stats.put(screenAssetGroup, status == null ? NanoSuiteConstant.NONE : checkNullOrEmptyValue(status.getName()));
										}
										break;
									default:
										logger.error("Unknown profile type: " + profileType);
										break;
								}
							}
						}

						// overall health state of asset group
						DeviceMetric overallHealthState = device.getOverallHealthState();
            if (overallHealthState!= null) {
							String lastValue = checkNullOrEmptyValue(overallHealthState.getLastValue());
							String group = metricGroup + NanoSuiteConstant.HEALTHSTATE;
							if (NanoSuiteConstant.NONE.equals(lastValue)) {
								stats.put(group, NanoSuiteConstant.NONE);
								continue;
							}

							HealthStateStatus lastHealth = HealthStateStatus.getByValue(Integer.parseInt(overallHealthState.getLastValue()));
							stats.put(group, lastHealth == null ? NanoSuiteConstant.NONE : checkNullOrEmptyValue(lastHealth.getName()));
            }
					}
				}
			}
		} catch (Exception e) {
			logger.error(String.format("An error occurred when populating monitoring properties %s", e.getMessage()), e);
		}
	}

	/**
	 * Check null value
	 *
	 * @param value the value is JsonNode value
	 * @return String / None if value is empty
	 */
	private String checkNullOrEmptyValue(Object value) {
		return value == null || StringUtils.isNullOrEmpty(String.valueOf(value)) ? NanoSuiteConstant.NONE : String.valueOf(value);
	}

	/**
	 * Convert the object to a map with key being the property name with a capital first letter and value being the property value
	 *
	 * @param obj need to convert to map
	 */
	public Map<String, Object> convertObjectToMap(Object obj) {
		Map<String, Object> map = new HashMap<>();
		for (Field field : obj.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				map.put(capitalizeFirstLetter(field.getName()), field.get(obj));
			} catch (Exception e) {
				map.put(capitalizeFirstLetter(field.getName()), null);
				logger.error("Failed to convert object " + obj.getClass().getName() + " with field " + field.getName() + " to map");
			}
		}
		return map;
	}

	/**
	 * Capitalizes the first letter of a given string.
	 *
	 * @param input The input string to be capitalized.
	 * @return a new string with the first letter capitalized, or the original string if it is null or empty.
	 */
	private String capitalizeFirstLetter(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	/**
	 * Capitalize the first letter of each word
	 *
	 * @param word the input string to be capitalized
	 */
	private String capitalizeFirstLetterOfEachWord(String word) {
		StringBuilder sb = new StringBuilder();
		String[] strs = word.split(" ");
		for (String str : strs) {
			sb.append(capitalizeFirstLetter(str));
		}
		return sb.toString();
	}

	/**
	 * Rounds a double value
	 *
	 * @param value the string representation of the double value to be rounded
	 * @return the rounded long value as a string, or the original value if it is "NONE" or cannot be parsed as a double
	 */
	private String roundDoubleValue(String value) {
		if (NanoSuiteConstant.NONE.equalsIgnoreCase(value)) {
			return value;
		}
		try {
			double doubleNumber = Double.parseDouble(value);
			return doubleNumber == (long) doubleNumber? String.valueOf((long) doubleNumber) :  String.valueOf(doubleNumber);
		} catch (NumberFormatException e) {
			return NanoSuiteConstant.NONE;
		}
	}

	/**
	 * Gets the default number of threads based on the provided input or a default constant value.
	 *
	 * @return The default number of threads.
	 */
	private int getDefaultNumberOfThread() {
		int result;
		try {
			if (StringUtils.isNullOrEmpty(numberThreads)) {
				result = NanoSuiteConstant.DEFAULT_NUMBER_THREAD;
			} else {
				result = Integer.parseInt(numberThreads);
				if (result < 0 || result >= NanoSuiteConstant.DEFAULT_NUMBER_THREAD) {
					result = NanoSuiteConstant.DEFAULT_NUMBER_THREAD;
				}
			}
		} catch (Exception e) {
			result = NanoSuiteConstant.DEFAULT_NUMBER_THREAD;
		}
		return result;
	}
}