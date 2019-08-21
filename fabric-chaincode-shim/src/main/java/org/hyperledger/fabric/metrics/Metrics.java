/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.metrics;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;


import org.hyperledger.fabric.shim.impl.InnvocationTaskExecutor;

public class Metrics {

    private static final String CHAINCODE_METRICS_ENABLED = "CHAINCODE_METRICS_ENABLED";
    private static final String CHAINCODE_METRICS_PROVIDER = "CHAINCODE_METRICS_PROVIDER";

    private static Logger logger = Logger.getLogger(Metrics.class.getName());

    private static MetricsProvider provider;

    public static MetricsProvider initialize(Map<String,String> props) {
        if ( Boolean.parseBoolean(props.get(CHAINCODE_METRICS_ENABLED))) {
            try {
                logger.info("Metrics enabled");
                if (props.containsKey(CHAINCODE_METRICS_PROVIDER)){
                    String providerClass = props.get(CHAINCODE_METRICS_PROVIDER);

                    @SuppressWarnings("unchecked")
					Class<MetricsProvider> clazz = (Class<MetricsProvider>) Class.forName(providerClass);
                    provider = (MetricsProvider) clazz.getConstructor(Map.class).newInstance(props);
                } else {
                    logger.info("No metrics provider given");
                    provider = new DefaultProvider();
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new RuntimeException("Unable to start metrics",e);
            }
        } else {
        	logger.info("Metrics disabled");
        	provider = new DefaultProvider();
        }

        // return blank do very litte, well nothing, object
        return provider;
    }

    public static MetricsProvider getProvider() {
        return provider;
    }

    static public class DefaultProvider implements MetricsProvider {

    	static Logger logger = Logger.getLogger(MetricsProvider.class.getName());
    	protected String id;

    	protected Map<String,Supplier<Integer>> intGauges = new HashMap<>();

    	public DefaultProvider() {

    	}

		@Override
		public void addGaugeProducer(String purpose, Supplier<Integer> supplier) {
			this.intGauges.putIfAbsent(purpose, supplier);
		}

		@Override
		public void setIdentifier(String id) {
			this.id = id;
		}

		@Override
		public void setInnvocationExecutor(InnvocationTaskExecutor taskService) {
			// TODO Auto-generated method stub

		}




    }

}
