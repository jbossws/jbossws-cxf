package org.jboss.wsf.stack.cxf;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.cxf.management.counters.CounterRepository;
import org.apache.cxf.management.counters.ResponseTimeCounter;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.EndpointMetrics;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;

public class EndpointMetricsCXFAdapterImpl implements EndpointMetrics {
	private CounterRepository counterRepo;
	private ResponseTimeCounter counter = null;
	private ObjectName objectName = null;

	public EndpointMetricsCXFAdapterImpl(Endpoint endpoint) {
		BusHolder busHolder = endpoint.getService().getDeployment()
				.getAttachment(BusHolder.class);
		counterRepo = busHolder.getBus().getExtension(CounterRepository.class);
		try {
			objectName = new ObjectName(endpoint.getName().toString()
					+ ",metrics=EndpointMetrics");
		} catch (MalformedObjectNameException e) {
			// ignore
		}
		counter = (ResponseTimeCounter)counterRepo.createCounter(objectName);
		counter.reset();
		counterRepo.getCounters().put(objectName, counter);
	}

	@Override
	public long getMinProcessingTime() {
		return counter.getMinResponseTime().longValue();
	}

	@Override
	public long getMaxProcessingTime() {
		return counter.getMaxResponseTime().longValue();
	}

	@Override
	public long getAverageProcessingTime() {		
		return counter.getAvgResponseTime().longValue();
	}

	@Override
	public long getTotalProcessingTime() {
		return counter.getTotalHandlingTime().longValue();
	}

	@Override
	public long getRequestCount() {
		return counter.getNumInvocations().longValue();
	}

	@Override
	public long getFaultCount() {
		long totalFault = counter.getNumCheckedApplicationFaults().longValue()
				+ counter.getNumLogicalRuntimeFaults().longValue()
				+ counter.getNumRuntimeFaults().longValue()
				+ counter.getNumUnCheckedApplicationFaults().longValue();
		return totalFault;
	}

	@Override
	public long getResponseCount() {
		return counter.getNumInvocations().longValue();
	}

}
