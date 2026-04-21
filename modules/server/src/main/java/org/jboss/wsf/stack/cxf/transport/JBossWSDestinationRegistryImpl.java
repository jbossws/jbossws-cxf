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
package org.jboss.wsf.stack.cxf.transport;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.DestinationRegistryImpl;
import org.jboss.logging.Logger;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;
import org.jboss.wsf.stack.cxf.client.Constants;

import javax.management.ObjectName;

/**
 * A JBossWS version of the CXF DestinationRegistryImpl that registers destinations
 * with different key values.
 *
 * @author alessio.soldano@jboss.com
 * @author fburzigo@ibm.com
 * @since 23-Jan-2014
 *
 */
public class JBossWSDestinationRegistryImpl extends DestinationRegistryImpl
{
   private static final Logger LOGGER = Logger.getLogger(JBossWSDestinationRegistryImpl.class);

   /**
    * Return a real path value, removing the protocol, host and port
    * if specified.
    * 
    * @param path 
    * @return trimmed path
    */
   @Override
   public String getTrimmedPath(String path)
   {
      if (path == null)
      {
         return "/";
      }
      if (!path.startsWith("/"))
      {
         try
         {
            path = new URL(path).getPath();
         }
         catch (MalformedURLException ex)
         {
            // ignore
            Logger.getLogger(JBossWSDestinationRegistryImpl.class).trace(ex);
         }
         if (!path.startsWith("/")) {
            path = "/" + path;
         }
      }
      return path;
   }

   /**
    * Override to handle URL-encoded paths containing non-ASCII characters.
    * When a service name contains National Language Symbols (NLS), incoming
    * HTTP requests arrive with URL-encoded paths (e.g., "Caff%C3%A8"), but
    * destinations are registered with decoded names (e.g., "Caffè") by CXF.
    * This method attempts lookup with the original path first, then tries
    * with a decoded version if the first lookup fails.
    * 
    * @param path the path to look up
    * @param tryDecoding whether to attempt URL decoding if initial lookup fails
    * @return the destination for the path, or null if not found
    */
   @Override
   public AbstractHTTPDestination getDestinationForPath(String path, boolean tryDecoding) {

      // 1. System property provides global default (UTF-8 by default)
      LOGGER.debug("Checking the " + Constants.JBWS_CXF_URL_CHARSET + " system property...");
      String urlCharset = SecurityActions.getSystemProperty(Constants.JBWS_CXF_URL_CHARSET,
              StandardCharsets.UTF_8.toString());
      LOGGER.debug(Constants.JBWS_CXF_URL_CHARSET + " system property is set to " + urlCharset);

      // 2. Deployment metadata property can override on a per-deployment basis (from jboss-webservices.xml)
      LOGGER.debug("Checking the " + Constants.JBWS_CXF_URL_CHARSET + " deployment metadata property...");
      // Let's see whether an Endpoint instance is associated with the current thread, as a way to
      // decide whether the code is being executed within an HTTP request.
      final Endpoint endpoint = EndpointAssociation.getEndpoint();
      if (endpoint == null) {
         LOGGER.warn("Cannot find an endpoint for the current request and look up the " +
                 Constants.JBWS_CXF_URL_CHARSET + " deployment property");
      } else {
         final Deployment deployment = getDeploymentFromEndpoint(endpoint);
         final JBossWebservicesMetaData wsmd = deployment.getAttachment(JBossWebservicesMetaData.class);
         if (wsmd != null && wsmd.getProperties() != null) {
            final String metadataProp = wsmd.getProperties().get(Constants.JBWS_CXF_URL_CHARSET);
            if (metadataProp != null) {
               urlCharset = metadataProp;
            }
         }
      }
      LOGGER.debug(Constants.JBWS_CXF_URL_CHARSET + " deployment metadata property is set to " + urlCharset);

      // First attempt: try original lookup with encoded path (backward compatibility)
      LOGGER.debug("Attempting CXF destination lookup for " + (path == null ? "(null)" : path) +
              " with tryDecoding=" + tryDecoding + "...");
      AbstractHTTPDestination dest = super.getDestinationForPath(path, tryDecoding);
      LOGGER.debug("CXF destination lookup returned " + (dest == null ? "no matching destination" : dest.getPath()));

      // Second attempt: if not found and decoding is enabled, try with decoded path
      if (dest == null && tryDecoding && path != null) {
         LOGGER.debug("Decoding the URL-encoded path...");
          String sanitizedPath = null;
          try {
              sanitizedPath = decodeAndClean(path, urlCharset);
          } catch (UnsupportedEncodingException e) {
              throw new RuntimeException(e);
          }
          // Only retry if decoding actually changed the path
         if (!sanitizedPath.equals(path)) {
            // The URL path is now sanitized, so we pass "tryDecoding" as "false", to
            // avoid any further action.
            LOGGER.debug("Attempting CXF destination lookup with decoded path...");
            dest = super.getDestinationForPath(sanitizedPath, false);
            LOGGER.debug("CXF destination lookup returned " + (dest == null ? "no matching destination" : dest.getPath()));
         }
      }
      return dest;
   }

   /**
    * Gets the deployment instance associated with the current thread endpoint.
    * @param endpoint The {@link Endpoint} instance associated with the current thread.
    * @return A {@link Deployment} instance associated with the given endpoint.
    */
   private static Deployment getDeploymentFromEndpoint(final Endpoint endpoint) {
       return endpoint.getService().getDeployment();
   }

   /**
    * Decodes URL-encoded characters and normalizes path separators.
    *
    * @param path The raw incoming URI path
    * @return A decoded and normalized path string
    */
   private String decodeAndClean(final String path, final String urlEncodingCharset) throws UnsupportedEncodingException {

      if (path == null || path.isEmpty()) {
         return path;
      }

      String processedPath = path;

      // 1. Decode NLS and special characters (e.g., %C3%A0 -> à)
      // Using StandardCharsets.UTF_8 is the standard for Jakarta EE 10+
      processedPath = URLDecoder.decode(processedPath, urlEncodingCharset);

      // 2. Collapse multiple slashes into a single one, after decoding
      // This prevents mismatches caused by "//" in the URL
      processedPath = processedPath.replaceAll("/{2,}", "/");

      return processedPath;
   }


}
