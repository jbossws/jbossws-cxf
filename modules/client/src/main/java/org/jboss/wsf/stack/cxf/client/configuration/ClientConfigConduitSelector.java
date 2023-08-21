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

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.UpfrontConduitSelector;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.http.HTTPConduit;

import javax.net.ssl.SSLContext;
import jakarta.xml.ws.BindingProvider;

import static org.jboss.wsf.spi.security.ClientConfigProvider.CLIENT_HTTP_MECHANISM;
import static org.jboss.wsf.spi.security.ClientConfigProvider.CLIENT_PASSWORD;
import static org.jboss.wsf.spi.security.ClientConfigProvider.CLIENT_SSL_CONTEXT;
import static org.jboss.wsf.spi.security.ClientConfigProvider.CLIENT_USERNAME;
import static org.jboss.wsf.spi.security.ClientConfigProvider.CLIENT_WS_SECURITY_TYPE;

/**
 * Extension of UpfrontConduitSelector that uses properties set by {@link org.jboss.wsf.spi.security.ClientConfigProvider} to configure SSLContext and credentials
 *
 * @author dvilkola@redhat.com
 * @since 24-Jul-2019
 */
public class ClientConfigConduitSelector extends UpfrontConduitSelector {

   /**
    * Called when a Conduit is actually required.
    *
    * @param message
    * @return the Conduit to use for mediation of the message
    */
   @Override
   public Conduit selectConduit(Message message) {
      Conduit c = super.selectConduit(message);
      if (c instanceof HTTPConduit)
      {
         setAuthorizationPolicyCredentials(message, (HTTPConduit) c);
         if (((HTTPConduit) c).getTlsClientParameters() == null || ((HTTPConduit) c).getTlsClientParameters().getSslContext() == null)
         {
            TLSClientParameters params = ((HTTPConduit) c).getTlsClientParameters() == null ? new TLSClientParameters() : ((HTTPConduit) c).getTlsClientParameters();
            params.setSslContext((SSLContext) message.getContextualProperty(CLIENT_SSL_CONTEXT));
            params.setUseHttpsURLConnectionDefaultSslSocketFactory(false);
            ((HTTPConduit) c).setTlsClientParameters(params);
         }
      }
      return c;
   }

   private void setAuthorizationPolicyCredentials(Message message, HTTPConduit c) {
      if (message.getContextualProperty(CLIENT_USERNAME) != null &&
              ((message.getContextualProperty(CLIENT_HTTP_MECHANISM) == null && message.getContextualProperty(CLIENT_WS_SECURITY_TYPE) == null) ||
                      message.getContextualProperty(CLIENT_HTTP_MECHANISM) != null && message.getContextualProperty(CLIENT_HTTP_MECHANISM).toString().equalsIgnoreCase("basic"))) {
         String defaultUsername = (String) message.get(BindingProvider.USERNAME_PROPERTY);
         String defaultPassword = (String) message.get(BindingProvider.PASSWORD_PROPERTY);
         AuthorizationPolicy authorizationPolicy = new AuthorizationPolicy();
         authorizationPolicy.setUserName(defaultUsername == null ? message.getContextualProperty(CLIENT_USERNAME).toString() : defaultUsername);
         authorizationPolicy.setPassword(defaultPassword == null ? message.getContextualProperty(CLIENT_PASSWORD).toString() : defaultPassword);
         c.setAuthorization(authorizationPolicy);
      }
   }
}
