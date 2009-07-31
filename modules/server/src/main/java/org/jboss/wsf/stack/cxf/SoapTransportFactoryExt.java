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
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Port;

import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.jboss.logging.Logger;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;

/**
 * A SoapTransportFactory extending @see org.apache.cxf.binding.soap.SoapTransportFactory.
 * It overrides the EndpointInfo creation method to allow for the soap:address extension
 * of the wsdl to be overwritten according to the JBossWS configuration.
 * 
 * @author alessio.soldano@jboss.com
 * @since 31-Jul-2009
 * 
 */
public class SoapTransportFactoryExt extends SoapTransportFactory
{
   private static Logger log = Logger.getLogger(SoapTransportFactoryExt.class); 
   private ServerConfig serverConfig;
   
   public EndpointInfo createEndpointInfo(ServiceInfo serviceInfo, BindingInfo b, Port port)
   {
      String transportURI = "http://schemas.xmlsoap.org/wsdl/soap/";
      if (b instanceof SoapBindingInfo)
      {
         SoapBindingInfo sbi = (SoapBindingInfo)b;
         transportURI = sbi.getTransportURI();
      }
      ServerConfig config = getServerConfig();
      EndpointInfo info = new CustomSoapEndpointInfo(serviceInfo, transportURI, config != null && config.isModifySOAPAddress());
      if (port != null)
      {
         List ees = port.getExtensibilityElements();
         for (Iterator itr = ees.iterator(); itr.hasNext();)
         {
            Object extensor = itr.next();

            if (SOAPBindingUtil.isSOAPAddress(extensor))
            {
               final SoapAddress sa = SOAPBindingUtil.getSoapAddress(extensor);

               info.addExtensor(sa);
               info.setAddress(sa.getLocationURI());
            }
            else
            {
               info.addExtensor(extensor);
            }
         }
      }
      return info;
   }
   
   private ServerConfig getServerConfig()
   {
      if (serverConfig == null)
      {
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         serverConfig = spiProvider.getSPI(ServerConfigFactory.class).getServerConfig();
      }
      return serverConfig;
   }
   
   /**
    * A custom EndpointInfo that updates the SoapAddress extension
    * coming from the wsdl definition according to the JBossWS
    * soap address rewrite rules.
    * 
    */
   private class CustomSoapEndpointInfo extends EndpointInfo
   {
      boolean alwaysModifyWsdl;
      SoapAddress saddress;

      CustomSoapEndpointInfo(ServiceInfo serv, String trans, boolean alwaysModifyWsdl)
      {
         super(serv, trans);
         this.alwaysModifyWsdl = alwaysModifyWsdl;
      }

      public void setAddress(String s)
      {
         boolean currentInvalid = isCurrentAddressInvalid();
         super.setAddress(s);
         if (alwaysModifyWsdl || currentInvalid)
         {
            log.info("Setting new address: " + s);
            if (saddress != null)
            {
               saddress.setLocationURI(s);
            }
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
      
      private boolean isCurrentAddressInvalid()
      {
         String address = super.getAddress();
         if (address != null)
         {
            try
            {
               new URL(address);
            }
            catch (MalformedURLException e)
            {
               log.info("Forcing rewrite of invalid address: " + address);
               return true;
            }
         }
         return false;
      }
   }
   
}
