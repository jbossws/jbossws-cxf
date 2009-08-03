/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.jboss.logging.Logger;
import org.jboss.wsf.spi.management.ServerConfig;

/**
 * A custom EndpointInfo that updates the SoapAddress extension
 * coming from the wsdl definition according to the JBossWS
 * soap address rewrite rules.
 * 
 * @see org.apache.cxf.binding.soap.SoapTransportFactory.SoapEndpointInfo
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Aug-2009
 * 
 */
public class AddressRewritingEndpointInfo extends EndpointInfo
{
   private static Logger log = Logger.getLogger(AddressRewritingEndpointInfo.class);
   
   private ServerConfig serverConfig;
   private String transportGuarantee;
   SoapAddress saddress;

   AddressRewritingEndpointInfo(ServiceInfo serv, String trans, ServerConfig serverConfig, String transportGuarantee)
   {
      super(serv, trans);
      this.serverConfig = serverConfig;
      this.transportGuarantee = transportGuarantee;
   }

   /**
    * This is the method responsible for both setting the EndpointInfo address and
    * setting the soap:address in the wsdl.
    * While the former action is straightforward, the latter is performed according
    * to the JBossWS configuration: every time CXF updates the EndpointInfo address
    * (which usually happens twice) this makes sure a proper address is updated in
    * the wsdl.
    * 
    * {@inheritDoc}
    */
   public void setAddress(String s)
   {
      String previousAddress = super.getAddress();
      log.warn(previousAddress + " -> " + s + " ?");
      super.setAddress(s);
      boolean setNewAddress = false;
      if (previousAddress == null)
      {
         setNewAddress = true;
      }
      else if (isRewriteAllowed(s) && isRewriteRequired(s, previousAddress))
      {
         String uriScheme = getUriScheme(s);
         //we set https if the transport guarantee is CONFIDENTIAL or the previous address already used https
         //(if the original wsdl soap:address uses https we can't overwrite it with http)
         if (isConfidential() || "https".equalsIgnoreCase(getUriScheme(previousAddress)))
         {
            uriScheme = "https";
         }
         if (uriScheme == null)
         {
            uriScheme = "http";
         }
         //rewrite the candidate new address
         s = rewriteSoapAddress(s, uriScheme);
         setNewAddress = true;
      }
      if (setNewAddress && saddress != null)
      {
         log.info("New service endpoint address: " + s);
         saddress.setLocationURI(s);
      }
   }

   public void addExtensor(Object el)
   {
      super.addExtensor(el);
      if (el instanceof SoapAddress)
      {
         saddress = (SoapAddress)el;
      }
   }
   
   protected boolean isRewriteAllowed(String address)
   {
      //exclude non http addresses
      return (address != null && address.trim().toLowerCase().startsWith("http"));
   }
   
   
   protected boolean isRewriteRequired(String address, String previousAddress)
   {
      //check config prop forcing address rewrite
      if (serverConfig.isModifySOAPAddress())
      {
         log.warn("Rewrite required because of configuration");
         return true;
      }
      //check if the previous address is not valid
      if (isInvalidAddress(previousAddress))
      {
         log.warn("Rewrite required because of invalid url");
         return true;
      }
      String uriScheme = getUriScheme(previousAddress);
      //check if the address' scheme does not match the transport guarantee
      if ("http".equalsIgnoreCase(uriScheme) && isConfidential())
      {
         return true;
      }
      log.warn("Rewrite NOT required");
      return false;
   }
   
   protected boolean isInvalidAddress(String address)
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
   
   private boolean isConfidential()
   {
      return "CONFIDENTIAL".equals(transportGuarantee);
   }
   
   /**
    * Rewrite the provided address according to the current server
    * configuration and always using the specified uriScheme. 
    * 
    * @param s          The source address
    * @param uriScheme  The uriScheme to use for rewrite
    * @return           The obtained address
    */
   protected String rewriteSoapAddress(String s, String uriScheme)
   {
      try
      {
         URL url = new URL(s);
         String path = url.getPath();
         String host = serverConfig.getWebServiceHost();
         String port = "";
         if ("https".equals(uriScheme))
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
         String urlStr = uriScheme + "://" + host + port + path;
         log.info("Rewritten new candidate service endpoint address '" + s + "' to '" + urlStr + "'");
         return urlStr;
      }
      catch (Exception e)
      {
         log.info("Invalid url provided, using it without rewriting: " + s);
         return s;
      }
   }
   
   private static String getUriScheme(String address)
   {
      try
      {
         URI addrURI = new URI(address);
         String scheme = addrURI.getScheme();
         return scheme;
      }
      catch (Exception e)
      {
         return null;
      }
   }
}