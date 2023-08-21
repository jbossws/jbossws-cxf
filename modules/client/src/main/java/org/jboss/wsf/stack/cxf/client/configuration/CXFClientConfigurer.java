/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.wsf.stack.cxf.client.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.security.auth.callback.CallbackHandler;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Dispatch;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ConduitSelector;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.ws.common.configuration.ConfigHelper;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.spi.security.ClientConfigProvider;
import org.jboss.wsf.spi.security.JASPIAuthenticationProvider;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.i18n.Loggers;

import static org.jboss.wsf.spi.security.ClientConfigProvider.CLIENT_PROVIDER_CONFIGURED;
import static org.jboss.wsf.spi.security.ClientConfigProvider.CLIENT_HTTP_MECHANISM;
import static org.jboss.wsf.spi.security.ClientConfigProvider.CLIENT_PASSWORD;
import static org.jboss.wsf.spi.security.ClientConfigProvider.CLIENT_SSL_CONTEXT;
import static org.jboss.wsf.spi.security.ClientConfigProvider.CLIENT_USERNAME;
import static org.jboss.wsf.spi.security.ClientConfigProvider.CLIENT_WS_SECURITY_TYPE;
import static org.jboss.wsf.stack.cxf.i18n.Messages.MESSAGES;

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
      ClientConfig config = readConfig(configFile, configName, clazz, (BindingProvider) client);
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
      Map<String, Object> attachments = config.getAttachments();
      if (attachments != null && !attachments.isEmpty()) {
         cxfClient.getEndpoint().putAll(attachments);
      }

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

      if (props != null && config.getAttachments().get(CLIENT_PROVIDER_CONFIGURED) != null && config.getAttachments().get(CLIENT_PROVIDER_CONFIGURED).toString().equalsIgnoreCase("true")) {
         //config provider
         ClientConfigProvider clientConfigProvider = (ClientConfigProvider) ServiceLoader.loadService(
                 ClientConfigProvider.class.getName(), null, ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader());
         if (clientConfigProvider != null)
         {
            String endpointAddress = ((BindingProvider) client).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY).toString();
            enableClientConfigProvider(clientConfigProvider, cxfClient, endpointAddress);
         }
      }
   }
   
   public void setConfigProperties(Client client, Map<String, String> properties) {
      client.getEndpoint().putAll(properties);
      InterceptorUtils.addInterceptors(client, properties);
      FeatureUtils.addFeatures(client, client.getBus(), properties);
      PropertyReferenceUtils.createPropertyReference(properties, client.getBus().getProperties());
   }
   
   private void enableClientConfigProvider(ClientConfigProvider clientConfigProvider, Client cxfClient, String endpointAddress) {
      Map<String, Object> requestContext = cxfClient.getRequestContext();
      if (requestContext.get("com.sun.xml.ws.transport.https.client.SSLSocketFactory") == null)
      {
         setClientConfigProviderConduitSelector(cxfClient); // sets BASIC in conduit
      }
      else
      {
         setHttpBasicProperties(cxfClient);
      }

      if (cxfClient.getEndpoint().get(CLIENT_USERNAME) != null && cxfClient.getEndpoint().get(CLIENT_WS_SECURITY_TYPE) != null && cxfClient.getEndpoint().get(CLIENT_WS_SECURITY_TYPE).toString().equalsIgnoreCase("usernametoken"))
      {
         setUsernameTokenProperties(cxfClient);
      }
   }
   
   private void setHttpBasicProperties(Client cxfClient) {
      Endpoint cxfClientEndpoint = cxfClient.getEndpoint();
      if (cxfClientEndpoint.get(CLIENT_USERNAME) != null && ((cxfClientEndpoint.get(CLIENT_HTTP_MECHANISM) == null && cxfClientEndpoint.get(CLIENT_WS_SECURITY_TYPE) == null) ||
              cxfClientEndpoint.get(CLIENT_HTTP_MECHANISM).toString().equalsIgnoreCase("basic")))
      {
         Map<String, Object> requestContext = cxfClient.getRequestContext();
         if (requestContext.get(BindingProvider.USERNAME_PROPERTY) == null && requestContext.get(BindingProvider.PASSWORD_PROPERTY) == null)
         {
            requestContext.put(BindingProvider.USERNAME_PROPERTY, cxfClientEndpoint.get(CLIENT_USERNAME));
            requestContext.put(BindingProvider.PASSWORD_PROPERTY, cxfClientEndpoint.get(CLIENT_PASSWORD));
         }
      }
   }
   
   private void setUsernameTokenProperties(Client cxfClient) {
      Map<String, Object> requestContext = cxfClient.getRequestContext();
      if (requestContext.get(SecurityConstants.USERNAME) == null && requestContext.get(SecurityConstants.PASSWORD) == null &&
              requestContext.get(SecurityConstants.CALLBACK_HANDLER) == null) {
         requestContext.put(SecurityConstants.USERNAME, cxfClient.getEndpoint().get(CLIENT_USERNAME));
         requestContext.put(SecurityConstants.CALLBACK_HANDLER, (CallbackHandler) callbacks -> {
                    WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
                    pc.setPassword(cxfClient.getEndpoint().get(CLIENT_PASSWORD).toString());
                 }
         );
      }
   }
   
   private void setClientConfigProviderConduitSelector(Client cxfClient) {
      ConduitSelector clientConfigProviderHttpConduitSelector = new ClientConfigConduitSelector();
      clientConfigProviderHttpConduitSelector.setEndpoint(cxfClient.getEndpoint());
      cxfClient.setConduitSelector(clientConfigProviderHttpConduitSelector);
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
               InterceptorUtils.removeInterceptors(client.getInInterceptors(), (String)ep.get(p));
            } else if (Constants.CXF_OUT_INTERCEPTORS_PROP.equals(p)) {
               InterceptorUtils.removeInterceptors(client.getOutInterceptors(), (String)ep.get(p));
            } else if (Constants.CXF_IN_FAULT_INTERCEPTORS_PROP.equals(p)) {
               InterceptorUtils.removeInterceptors(client.getInFaultInterceptors(), (String)ep.get(p));
            } else if (Constants.CXF_OUT_FAULT_INTERCEPTORS_PROP.equals(p)) {
               InterceptorUtils.removeInterceptors(client.getOutFaultInterceptors(), (String)ep.get(p));
            } else if (Constants.CXF_FEATURES_PROP.equals(p)) {
               Loggers.ROOT_LOGGER.couldNoRemoveFeaturesOnClient((String)ep.get(p));
            }
            ep.remove(p);
         }
         ep.remove(JBOSSWS_CXF_CLIENT_CONF_PROPS);
      }
   }
}
