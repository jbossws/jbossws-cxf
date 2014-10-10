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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.ws.Dispatch;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.jaxws.DispatchImpl;
import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.ws.common.configuration.ConfigHelper;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.spi.security.JASPIAuthenticationProvider;
import org.jboss.wsf.stack.cxf.Loggers;
import org.jboss.wsf.stack.cxf.client.Constants;

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
   public void setConfigProperties(Object client, String configFile, String configName) {
      Class<?> clazz = !(client instanceof Dispatch) ? client.getClass() : null;
      ClientConfig config = readConfig(configFile, configName, clazz);
      setConfigProperties(client, config);
   }
   
   protected void setConfigProperties(Object client, ClientConfig config) {
      Client cxfClient;
      if (client instanceof DispatchImpl<?>) {
         cxfClient = ((DispatchImpl<?>)client).getClient();
      } else {
         cxfClient = ClientProxy.getClient(client);
      }
      cleanupPreviousProps(cxfClient);
      Map<String, String> props = config.getProperties();
      if (props != null && !props.isEmpty()) {
         savePropList(cxfClient, props);
      }
      setConfigProperties(cxfClient, props);
      
      //config jaspi
      JASPIAuthenticationProvider japsiProvider = (JASPIAuthenticationProvider) ServiceLoader.loadService(
            JASPIAuthenticationProvider.class.getName(), null, ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader());
      if (japsiProvider != null)
      {
         japsiProvider.enableClientAuthentication(cxfClient, props);
      }
      else
      {
         Loggers.SECURITY_LOGGER.cannotFindJaspiClasses();
      }
   }
   
   public void setConfigProperties(Client client, Map<String, String> properties) {
      client.getEndpoint().putAll(properties);
      addInterceptors(client, properties);
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
            if (Constants.CXF_IN_INTERCEPTORS_PROP.equals(p)) {
               removeInterceptors(client.getInInterceptors(), (String)ep.get(p));
            } else if (Constants.CXF_OUT_INTERCEPTORS_PROP.equals(p)) {
               removeInterceptors(client.getOutInterceptors(), (String)ep.get(p));
            }
            ep.remove(p);
         }
         ep.remove(JBOSSWS_CXF_CLIENT_CONF_PROPS);
      }
   }
   
   public void addInterceptors(InterceptorProvider interceptorProvider, Map<String, String> properties) {
      final String inInterceptors = properties.get(Constants.CXF_IN_INTERCEPTORS_PROP);
      if (inInterceptors != null) {
         interceptorProvider.getInInterceptors().addAll(createInterceptors(inInterceptors));
      }
      final String outInterceptors = properties.get(Constants.CXF_OUT_INTERCEPTORS_PROP);
      if (outInterceptors != null) {
         interceptorProvider.getOutInterceptors().addAll(createInterceptors(outInterceptors));
      }
   }
   
   private void removeInterceptors(List<Interceptor<?>> interceptorsList, String interceptors) {
      Set<String> set = new HashSet<String>();
      StringTokenizer st = new StringTokenizer(interceptors, ", ", false);
      while (st.hasMoreTokens()) {
         set.add(st.nextToken());
      }
      List<Interceptor<?>> toBeRemoved = new ArrayList<Interceptor<?>>();
      for (Interceptor<?> itc : interceptorsList) {
         if (set.contains(itc.getClass().getName())) {
            toBeRemoved.add(itc);
         }
      }
      interceptorsList.removeAll(toBeRemoved);
   }
   
   private static List<Interceptor<?>> createInterceptors(String propValue) {
      List<Interceptor<?>> list = new ArrayList<Interceptor<?>>();
      StringTokenizer st = new StringTokenizer(propValue, ", ", false );
      while (st.hasMoreTokens()) {
         String itc = st.nextToken();
         Interceptor<?> interceptor = (Interceptor<?>)newInstance(itc);
         if (interceptor != null) {
            list.add(interceptor);
         }
      }
      return list;
   }
   
   private static Object newInstance(String className)
   {
      try
      {
         ClassLoader loader = new DelegateClassLoader(ClassLoaderProvider.getDefaultProvider()
               .getServerIntegrationClassLoader(), SecurityActions.getContextClassLoader());
         Class<?> clazz = SecurityActions.loadClass(loader, className);
         return clazz.newInstance();
      }
      catch (Exception e)
      {
         return null;
      }
   }
}
