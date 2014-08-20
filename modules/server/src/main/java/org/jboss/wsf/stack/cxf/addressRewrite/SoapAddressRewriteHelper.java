/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.addressRewrite;

import static org.jboss.wsf.stack.cxf.Loggers.ADDRESS_REWRITE_LOGGER;

import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.stack.cxf.client.Constants;

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
    * @param serverConfig   The current ServerConfig
    * @return               The rewritten soap:address to be used in the wsdl
    */
   public static String getRewrittenPublishedEndpointUrl(String wsdlAddress, String epAddress, ServerConfig serverConfig, Map<String, String> props) {
      if (wsdlAddress == null) {
         return null;
      }
      if (isRewriteRequired(serverConfig, wsdlAddress))
      {
         final String origUriScheme = getUriScheme(wsdlAddress); //will be https if the user wants a https address in the wsdl
         final String newUriScheme = getUriScheme(epAddress); //will be https if the user set confidential transport for the endpoint
         return rewriteSoapAddress(serverConfig, wsdlAddress, epAddress, rewriteUriScheme(origUriScheme, newUriScheme, serverConfig, props));
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
    * @param serverConfig   The current ServerConfig
    * @return
    */
   public static String getRewrittenPublishedEndpointUrl(String address, ServerConfig serverConfig, Map<String, String> props)
   {
      try
      {
         if (isPathRewriteRequired(serverConfig) || isSchemeRewriteRequired(serverConfig, props)) {
            final URL url = new URL(address);
            final String uriScheme = rewriteUriScheme(getUriScheme(address), null, serverConfig, props);
            final String port = getDotPortNumber(uriScheme, serverConfig);
            final StringBuilder builder = new StringBuilder();
            builder.append(uriScheme);
            builder.append("://");
            builder.append(url.getHost());
            builder.append(port);
            final String path = url.getPath();
            builder.append(isPathRewriteRequired(serverConfig) ? SEDProcessor.newInstance(serverConfig.getWebServicePathRewriteRule()).processLine(path) : path);
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
      catch (Exception e)
      {
         ADDRESS_REWRITE_LOGGER.invalidAddressProvidedUseItWithoutRewriting(address, "");
         return address;
      }
   }

   public static boolean isAutoRewriteOn(ServerConfig serverConfig)
   {
      return serverConfig.isModifySOAPAddress() && ServerConfig.UNDEFINED_HOSTNAME.equals(serverConfig.getWebServiceHost());
   }
   
   private static boolean isRewriteRequired(ServerConfig serverConfig, String address)
   {
      //check config prop forcing address rewrite
      if (serverConfig.isModifySOAPAddress())
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
      catch (Exception e)
      {
         return true;
      }
      return false;
   }
   
   /**
    * Rewrite the provided address according to the current server
    * configuration and always using the specified uriScheme.
    * 
    * @param origAddress    The source address
    * @param newAddress     The new (candidate) address
    * @param uriScheme      The uriScheme to use for rewrite
    * @return               The obtained address
    */
   private static String rewriteSoapAddress(ServerConfig serverConfig, String origAddress, String newAddress, String uriScheme)
   {
      try
      {
         URL url = new URL(newAddress);
         String path = url.getPath();
         String host = serverConfig.getWebServiceHost();
         String port = getDotPortNumber(uriScheme, serverConfig);

         StringBuilder sb = new StringBuilder(uriScheme);
         sb.append("://");
         sb.append(host);
         sb.append(port);
         
         if (isPathRewriteRequired(serverConfig)) {
             sb.append(SEDProcessor.newInstance(serverConfig.getWebServicePathRewriteRule()).processLine(path));
         }
         else
         {
             sb.append(path);
         }
         final String urlStr = sb.toString();
         
         ADDRESS_REWRITE_LOGGER.addressRewritten(origAddress, urlStr);
         return urlStr;
      }
      catch (Exception e)
      {
         ADDRESS_REWRITE_LOGGER.invalidAddressProvidedUseItWithoutRewriting(newAddress, origAddress);
         return origAddress;
      }
   }
   
   private static String getDotPortNumber(String uriScheme, ServerConfig serverConfig) {
      String port = "";
      if (HTTPS.equals(uriScheme))
      {
         int portNo = serverConfig.getWebServiceSecurePort();
         if (portNo != 443)
         {
            port = ":" + portNo;
         }
      }
      else
      {
         int portNo = serverConfig.getWebServicePort();
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
      catch (Exception e)
      {
         return HTTP;
      }
   }


   public static boolean isPathRewriteRequired(ServerConfig sc){
      if (!sc.isModifySOAPAddress()) {
         return false;
      }
      final String pathRewriteRule = sc.getWebServicePathRewriteRule();
      return pathRewriteRule != null && !pathRewriteRule.isEmpty();
   }
   
   public static boolean isSchemeRewriteRequired(ServerConfig sc, Map<String, String> props) {
      if (!sc.isModifySOAPAddress()) {
         return false;
      } //TODO also check modify soap address is enabled in wsmd
      return sc.getWebServiceUriScheme() != null || props.get(Constants.JBWS_CXF_WSDL_URI_SCHEME) != null;
   }
   
   private static String rewriteUriScheme(final String origUriScheme, final String newUriScheme, final ServerConfig serverConfig, final Map<String, String> props) {
      //1) if either of orig URI or new URI uses HTTPS, use HTTPS
      String uriScheme = (HTTPS.equals(origUriScheme) || HTTPS.equals(newUriScheme)) ? HTTPS : HTTP;
      //2) server configuration override
      final String serverUriScheme = serverConfig.getWebServiceUriScheme();
      if (serverUriScheme != null) {
         uriScheme = serverUriScheme;
      }
      //3) deployment configuration override
      final String mdUriScheme = props.get(Constants.JBWS_CXF_WSDL_URI_SCHEME);
      if (mdUriScheme != null) {
         uriScheme = mdUriScheme;
      }
      return uriScheme;
   }
}
