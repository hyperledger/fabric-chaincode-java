/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.metrics;
import java.util.Properties;

/** Interface to be implemented to send metrics on the chaincode to the 'backend-of-choice'.
 * 
 * An instance of this will be created, and provided with the resources from which chaincode
 * specific metrics can be collected. (via the no-argument constructor).
 * 
 * The choice of when, where and what to collect etc are within the remit of the provider.
 * 
 * This is the effective call sequence.
 * 
 * MyMetricsProvider mmp = new MyMetricsProvider()
 * mmp.initalize(props_from_environment);
 * // short while later....
 * mmp.setTaskMetricsCollector(taskService);
 */
public interface MetricsProvider {

	/**
	 * Initialize method that is called immediately after creation.
	 *
	 */
	default void initialize(Properties props) {};

	/**
	 * Pass a reference to this task service for information gathering.  This is related
	 * specifically to the handling of tasks within the chaincode. i.e. how individual transactions
	 * are dispatched for execution.
	 *
	 * @param taskService
	 */
	default void setTaskMetricsCollector(TaskMetricsCollector taskService) {};

    

}
