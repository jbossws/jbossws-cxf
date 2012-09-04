/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.wsf.stack.cxf.client.configuration;

import java.util.Map;
import java.util.Set;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.jboss.ws.common.configuration.ConfigHelper;
import org.jboss.wsf.spi.metadata.config.ClientConfig;

/**
 * CXF extension of common ClientConfigurer
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-Jul-2012
 *
 */
public class CXFClientConfigurer extends ConfigHelper
{
   private static final String JBOSSWS_CXF_CLIENT_CONF_PROPS = "jbossws.cxf.client.conf.props";
   
   @Override
   public void setConfigProperties(Object proxy, String configFile, String configName) {
      ClientConfig config = readConfig(configFile, configName);
      Client client = ClientProxy.getClient(proxy);
      cleanupPreviousProps(client);
      Map<String, String> props = config.getProperties();
      if (props != null && !props.isEmpty()) {
         savePropList(client, props);
      }
      setConfigProperties(client, props);
   }
   
   public void setConfigProperties(Client client, Map<String, String> properties) {
      client.getEndpoint().putAll(properties);
   }
   
   private void savePropList(Client client, Map<String, String> props) {
      final Set<String> keys = props.keySet();
      client.getEndpoint().put(JBOSSWS_CXF_CLIENT_CONF_PROPS, (String[])keys.toArray(new String[keys.size()]));
   }
   
   private void cleanupPreviousProps(Client client) {
      Endpoint ep = client.getEndpoint();
      String[] previousProps = (String[])ep.get(JBOSSWS_CXF_CLIENT_CONF_PROPS);
      if (previousProps != null) {
         for (String p : previousProps) {
            ep.remove(p);
         }
         ep.remove(JBOSSWS_CXF_CLIENT_CONF_PROPS);
      }
   }
}
