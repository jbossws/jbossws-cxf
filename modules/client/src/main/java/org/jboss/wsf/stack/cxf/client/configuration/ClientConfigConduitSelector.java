/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat Middleware LLC, and individual contributors
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

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.UpfrontConduitSelector;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.http.HTTPConduit;

import javax.net.ssl.SSLContext;
import javax.xml.ws.BindingProvider;

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
