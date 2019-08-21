/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.metrics.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.hyperledger.fabric.metrics.Metrics.DefaultProvider;
import org.hyperledger.fabric.metrics.MetricsProvider;
import org.hyperledger.fabric.shim.impl.InnvocationTaskExecutor;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.exporter.PushGateway;
import io.prometheus.client.hotspot.BufferPoolsExports;
import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryAllocationExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.ThreadExports;
import io.prometheus.client.hotspot.VersionInfoExports;

public class PrometheusProvider extends DefaultProvider implements MetricsProvider {

	private CollectorRegistry registry;
	private PushGateway pg;
	private String id;
	private InnvocationTaskExecutor taskService;

	@Override
	public void setIdentifier(String id) {
		this.id = id;
	}

	@Override
	public void setInnvocationExecutor(InnvocationTaskExecutor taskService) {
		this.taskService = taskService;
	}

	private final static String PROMETHEUS_PORT = "PROMETHEUS_PORT";
	private final static String PROMETHEUS_HOST = "PROMETHEUS_HOST";

	class ThreadPoolExports extends Collector {
		@Override
		public List<MetricFamilySamples> collect() {
			List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
			if (PrometheusProvider.this.taskService != null) {

				mfs.add(new GaugeMetricFamily("tx_active_count", "Active Tasks",
						PrometheusProvider.this.taskService.getActiveCount()));
				mfs.add(new GaugeMetricFamily("tx_pool_size", "Active Tasks",
						PrometheusProvider.this.taskService.getPoolSize()));
				mfs.add(new GaugeMetricFamily("tx_core_pool_size", "Active Tasks",
						PrometheusProvider.this.taskService.getCorePoolSize()));
				mfs.add(new GaugeMetricFamily("tx_largest_pool_size", "Active Tasks",
						PrometheusProvider.this.taskService.getLargestPoolSize()));
				mfs.add(new GaugeMetricFamily("tx_max_pool_size", "Active Tasks",
						PrometheusProvider.this.taskService.getMaximumPoolSize()));
				mfs.add(new GaugeMetricFamily("tx_task_count", "Active Tasks",
						PrometheusProvider.this.taskService.getTaskCount()));
				mfs.add(new GaugeMetricFamily("tx_current_task_count", "Current Task Count",
						PrometheusProvider.this.taskService.getCurrentTaskCount()));
				mfs.add(new GaugeMetricFamily("tx_current_queue_depth", "Current Queue Depth",
						PrometheusProvider.this.taskService.getCurrentQueueCount()));
			}
			return mfs;
		}
	}

	public PrometheusProvider(Map<String, String> props) {
		this.registry = new CollectorRegistry();
		int port = 9091;
		String host = "pushGateway";
		if (props.containsKey(PROMETHEUS_PORT)) {
			port = Integer.parseInt((props.get(PROMETHEUS_PORT)));
		}

		if (props.containsKey(PROMETHEUS_HOST)) {
			host = (props.get(PROMETHEUS_HOST));
		}
		this.pg = new PushGateway(host + ":" + port);

		new ThreadPoolExports().register(registry);

		// setup the standard JVM ports etc.
		new StandardExports().register(registry);
		new MemoryPoolsExports().register(registry);
		new MemoryAllocationExports().register(registry);
		new BufferPoolsExports().register(registry);
		new GarbageCollectorExports().register(registry);
		new ThreadExports().register(registry);
		new ClassLoadingExports().register(registry);
		new VersionInfoExports().register(registry);

		Timer metricTimer = new Timer(true);
		metricTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				PrometheusProvider.this.pushMetrics();
			}
		}, 0, 5000);

	}

	protected final void pushMetrics() {

		try {
			String job = InetAddress.getLocalHost().getHostName() + "_" + id;
			// update from all the producers
			pg.pushAdd(registry, job);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
