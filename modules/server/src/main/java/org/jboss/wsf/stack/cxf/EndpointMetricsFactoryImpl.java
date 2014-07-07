package org.jboss.wsf.stack.cxf;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.EndpointMetrics;
import org.jboss.wsf.spi.management.EndpointMetricsFactory;
public class EndpointMetricsFactoryImpl extends EndpointMetricsFactory {

	@Override
	public EndpointMetrics newEndpointMetrics(Endpoint endpoint) {
	    return new EndpointMetricsCXFAdapterImpl(endpoint);
	}
}
