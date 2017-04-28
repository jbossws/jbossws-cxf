package org.jboss.wsf.stack.cxf.configuration;

import org.jboss.wsf.stack.cxf.deployment.EndpointImpl;

public interface JBossWSEndpointConfig
{
   public void config(EndpointImpl endpoint);
}
