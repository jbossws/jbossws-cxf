package org.jboss.wsf.stack.cxf.configuration;

import org.jboss.wsf.stack.cxf.deployment.EndpointImpl;

public interface JBossWSEndpointConfigure
{
   public void config(EndpointImpl endpoint);
}
