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
package org.jboss.wsf.stack.cxf.addressRewrite;

import static org.jboss.wsf.stack.cxf.i18n.Loggers.ADDRESS_REWRITE_LOGGER;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;

/**
 * Helper for rewriting soap:address in published wsdl
 * 
 * @author alessio.soldano@jboss.com
 * @author rsears@redhat.com
 * @since 30-Nov-2012
 */
public class SoapAddressRewriteHelper
{
   private static final String HTTP = "http";
   private static final String HTTPS = "https";
   
   /**
    * Rewrite and get address to be used for CXF published endpoint url prop (rewritten wsdl address)
    * 
    * @param wsdlAddress    The soap:address in the wsdl
    * @param epAddress      The address that has been computed for the endpoint
    * @param sarm           The deployment SOAPAddressRewriteMetadata
    * @return               The rewritten soap:address to be used in the wsdl
    */
   public static String getRewrittenPublishedEndpointUrl(String wsdlAddress, String epAddress, SOAPAddressRewriteMetadata sarm) {
      if (wsdlAddress == null) {
         return null;
      }
      if (isRewriteRequired(sarm, wsdlAddress))
      {
         final String origUriScheme = getUriScheme(wsdlAddress); //will be https if the user wants a https address in the wsdl
         final String newUriScheme = getUriScheme(epAddress); //will be https if the user set confidential transport for the endpoint
         return rewriteSoapAddress(sarm, wsdlAddress, epAddress, rewriteUriScheme(sarm, origUriScheme, newUriScheme));
      }
      else
      {
         return wsdlAddress;
      }
   }
   
   /**
    * Rewrite and get address to be used for CXF published endpoint url prop (rewritten wsdl address).
    * This method is to be used for code-first endpoints, when no wsdl is provided by the user.
    * 
    * @param address        The container computed endpoint address
    * @param sarm           The deployment SOAPAddressRewriteMetadata
    * @return
    */
   public static String getRewrittenPublishedEndpointUrl(String address, SOAPAddressRewriteMetadata sarm)
   {
      try
      {
         if (isPathRewriteRequired(sarm) || isSchemeRewriteRequired(sarm)) {
            final URL url = new URL(address);
            final String uriScheme = rewriteUriScheme(sarm, getUriScheme(address), null);
            final String port = getDotPortNumber(uriScheme, sarm);
            final StringBuilder builder = new StringBuilder();
            builder.append(uriScheme);
            builder.append("://");
            builder.append(url.getHost());
            builder.append(port);
            final String path = url.getPath();
            builder.append(isPathRewriteRequired(sarm) ? SEDProcessor.newInstance(sarm.getWebServicePathRewriteRule()).processLine(path) : path);
            final String newUrl = builder.toString();

            ADDRESS_REWRITE_LOGGER.addressRewritten(address, newUrl);
            return newUrl;
         }
         else
         {
            ADDRESS_REWRITE_LOGGER.rewriteNotRequired(address);
            return address;
         }
      }
      catch (MalformedURLException e)
      {
         ADDRESS_REWRITE_LOGGER.invalidAddressProvidedUseItWithoutRewriting(address, "");
         return address;
      }
   }
   
   public static void validatePathRewriteRule(String rule) {
      if (rule == null) {
         throw new IllegalArgumentException();
      }
      SEDProcessor.newInstance(rule);
   }

   public static boolean isAutoRewriteOn(SOAPAddressRewriteMetadata sarm)
   {
      return sarm.isModifySOAPAddress() && ServerConfig.UNDEFINED_HOSTNAME.equals(sarm.getWebServiceHost());
   }
   
   private static boolean isRewriteRequired(SOAPAddressRewriteMetadata sarm, String address)
   {
      //check config prop forcing address rewrite
      if (sarm.isModifySOAPAddress())
      {
         ADDRESS_REWRITE_LOGGER.addressRewriteRequiredBecauseOfServerConf(address);
         return true;
      }
      //check if the previous address is not valid
      if (isInvalidAddress(address))
      {
         ADDRESS_REWRITE_LOGGER.addressRewriteRequiredBecauseOfInvalidAddress(address);
         return true;
      }
      ADDRESS_REWRITE_LOGGER.rewriteNotRequired(address);
      return false;
   }
   
   private static boolean isInvalidAddress(String address)
   {
      if (address == null)
      {
         return true;
      }
      String s = address.trim();
      if (s.length() == 0 || s.contains("REPLACE_WITH_ACTUAL_URL"))
      {
         return true;
      }
      try
      {
         new URL(s);
      }
      catch (MalformedURLException e)
      {
         return true;
      }
      return false;
   }
   
   /**
    * Rewrite the provided address according to the current server
    * configuration and always using the specified uriScheme.
    * 
    * @param sarm           The deployment SOAPAddressRewriteMetadata
    * @param origAddress    The source address
    * @param newAddress     The new (candidate) address
    * @param uriScheme      The uriScheme to use for rewrite
    * @return               The obtained address
    */
   private static String rewriteSoapAddress(SOAPAddressRewriteMetadata sarm, String origAddress, String newAddress, String uriScheme)
   {
      try
      {
         URL url = new URL(newAddress);
         String path = url.getPath();
         String host = sarm.getWebServiceHost();
         String port = getDotPortNumber(uriScheme, sarm);

         StringBuilder sb = new StringBuilder(uriScheme);
         sb.append("://");
         sb.append(host);
         sb.append(port);
         
         if (isPathRewriteRequired(sarm)) {
             sb.append(SEDProcessor.newInstance(sarm.getWebServicePathRewriteRule()).processLine(path));
         }
         else
         {
             sb.append(path);
         }
         final String urlStr = sb.toString();
         
         ADDRESS_REWRITE_LOGGER.addressRewritten(origAddress, urlStr);
         return urlStr;
      }
      catch (MalformedURLException e)
      {
         ADDRESS_REWRITE_LOGGER.invalidAddressProvidedUseItWithoutRewriting(newAddress, origAddress);
         return origAddress;
      }
   }
   
   private static String getDotPortNumber(String uriScheme, SOAPAddressRewriteMetadata sarm) {
      String port = "";
      if (HTTPS.equals(uriScheme))
      {
         int portNo = sarm.getWebServiceSecurePort();
         if (portNo != 443)
         {
            port = ":" + portNo;
         }
      }
      else
      {
         int portNo = sarm.getWebServicePort();
         if (portNo != 80)
         {
            port = ":" + portNo;
         }
      }
      return port;
   }
   
   private static String getUriScheme(String address)
   {
      try
      {
         URI addrURI = new URI(address);
         String scheme = addrURI.getScheme();
         return scheme != null ? scheme : HTTP;
      }
      catch (URISyntaxException e)
      {
         return HTTP;
      }
   }


   public static boolean isPathRewriteRequired(SOAPAddressRewriteMetadata sarm){
      if (!sarm.isModifySOAPAddress()) {
         return false;
      }
      final String pathRewriteRule = sarm.getWebServicePathRewriteRule();
      return pathRewriteRule != null && !pathRewriteRule.isEmpty();
   }
   
   public static boolean isSchemeRewriteRequired(SOAPAddressRewriteMetadata sarm) {
      if (!sarm.isModifySOAPAddress()) {
         return false;
      }
      return sarm.getWebServiceUriScheme() != null;
   }
   
   private static String rewriteUriScheme(final SOAPAddressRewriteMetadata sarm, final String origUriScheme, final String newUriScheme) {
      //1) if either of orig URI or new URI uses HTTPS, use HTTPS
      String uriScheme = (HTTPS.equals(origUriScheme) || HTTPS.equals(newUriScheme)) ? HTTPS : HTTP;
      //2) server / deployment configuration override
      final String serverUriScheme = sarm.getWebServiceUriScheme();
      if (serverUriScheme != null) {
         uriScheme = serverUriScheme;
      }
      return uriScheme;
   }
}
