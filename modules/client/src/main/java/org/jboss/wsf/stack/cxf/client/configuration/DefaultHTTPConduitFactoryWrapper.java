/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitFactory;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.transport.http.URLConnectionHTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jboss.wsf.stack.cxf.client.Constants;

/**
 * The default wrapper of HTTPConduitFactory, which gets default configuration values from a
 * map. The configuration map can also be populated by system properties.
 * 
 * @author alessio.soldano@jboss.com
 * @since 1-Apr-2015
 */
public final class DefaultHTTPConduitFactoryWrapper extends AbstractHTTPConduitFactoryWrapper
{
   private static final Map<String, Object> defaultConfiguration;
   static {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put(Constants.CXF_CLIENT_ALLOW_CHUNKING, SecurityActions.getBoolean(Constants.CXF_CLIENT_ALLOW_CHUNKING, null));
      map.put(Constants.CXF_CLIENT_CHUNKING_THRESHOLD, SecurityActions.getInteger(Constants.CXF_CLIENT_CHUNKING_THRESHOLD, null));
      map.put(Constants.CXF_TLS_CLIENT_DISABLE_CN_CHECK, SecurityActions.getBoolean(Constants.CXF_TLS_CLIENT_DISABLE_CN_CHECK));
      map.put(Constants.CXF_CLIENT_CONNECTION_TIMEOUT, SecurityActions.getLong(Constants.CXF_CLIENT_CONNECTION_TIMEOUT, null));
      map.put(Constants.CXF_CLIENT_RECEIVE_TIMEOUT, SecurityActions.getLong(Constants.CXF_CLIENT_RECEIVE_TIMEOUT, null));
      map.put(Constants.CXF_CLIENT_CONNECTION, SecurityActions.getSystemProperty(Constants.CXF_CLIENT_CONNECTION, null));
      defaultConfiguration = Collections.unmodifiableMap(map);
   }
   
   private final Map<String, Object> configuration;
   
   public DefaultHTTPConduitFactoryWrapper(HTTPConduitFactory delegate)
   {
      super(delegate);
      this.configuration = defaultConfiguration;
   }
   
   public DefaultHTTPConduitFactoryWrapper(Map<String, Object> configuration, boolean useSystemDefault, HTTPConduitFactory delegate)
   {
      super(delegate);
      if (configuration == null) {
         throw new IllegalArgumentException();
      }
      if (useSystemDefault) {
         this.configuration = new HashMap<String, Object>();
         for (Entry<String, Object> e : defaultConfiguration.entrySet()) {
            final String key = e.getKey();
            this.configuration.put(key, e.getValue());
            final Object providedValue = configuration.get(key);
            if (providedValue != null) {
               this.configuration.put(key, providedValue);
            }
         }
      } else {
         this.configuration = configuration;
      }
   }

   protected HTTPConduit createNewConduit(HTTPTransportFactory f, Bus b, EndpointInfo localInfo,
         EndpointReferenceType target) throws IOException
   {
      return new URLConnectionHTTPConduit(b, localInfo, target);
   }

   protected void configureConduit(HTTPConduit conduit)
   {
      configureTLSClient(conduit);
      configureHTTPClientPolicy(conduit);
   }
   
   private void configureTLSClient(HTTPConduit conduit)
   {
      TLSClientParameters parameters = conduit.getTlsClientParameters();
      if (parameters == null) //don't do anything when user already provided a configuration
      {
         parameters = new TLSClientParameters();
         parameters.setUseHttpsURLConnectionDefaultSslSocketFactory(true);
         if (Boolean.TRUE.equals((Boolean)configuration.get(Constants.CXF_TLS_CLIENT_DISABLE_CN_CHECK))) {
            parameters.setDisableCNCheck(true);
         }
         conduit.setTlsClientParameters(parameters);
      }
   }
   
   private void configureHTTPClientPolicy(HTTPConduit conduit)
   {
      boolean set = false;

      final Boolean allowChunking = (Boolean)configuration.get(Constants.CXF_CLIENT_ALLOW_CHUNKING);
      set = set || (allowChunking != null);
      final Integer chunkingThreshold = (Integer)configuration.get(Constants.CXF_CLIENT_CHUNKING_THRESHOLD);
      set = set || (chunkingThreshold != null);
      final Long connectionTimeout = (Long)configuration.get(Constants.CXF_CLIENT_CONNECTION_TIMEOUT);
      set = set || (connectionTimeout != null);
      final Long receiveTimeout = (Long)configuration.get(Constants.CXF_CLIENT_RECEIVE_TIMEOUT);
      set = set || (receiveTimeout != null);
      final String connection = (String)configuration.get(Constants.CXF_CLIENT_CONNECTION);
      set = set || (connection != null);

      if (set)
      {
         HTTPClientPolicy httpClientPolicy = conduit.getClient();
         if (httpClientPolicy == null)
         {
            httpClientPolicy = new HTTPClientPolicy();
            conduit.setClient(httpClientPolicy);
         }
         if (allowChunking != null)
         {
            httpClientPolicy.setAllowChunking(allowChunking);
         }
         if (chunkingThreshold != null)
         {
            httpClientPolicy.setChunkingThreshold(chunkingThreshold);
         }
         if (connectionTimeout != null)
         {
            httpClientPolicy.setConnectionTimeout(connectionTimeout);
         }
         if (receiveTimeout != null)
         {
            httpClientPolicy.setReceiveTimeout(receiveTimeout);
         }
         if (connection != null)
         {
            httpClientPolicy.setConnection(ConnectionType.fromValue(connection));
         }
      }
   }
   
   public static void install(Bus bus)
   {
      HTTPConduitFactory delegate = bus.getExtension(HTTPConduitFactory.class);
      bus.setExtension(new DefaultHTTPConduitFactoryWrapper(delegate), HTTPConduitFactory.class);
   }
}
