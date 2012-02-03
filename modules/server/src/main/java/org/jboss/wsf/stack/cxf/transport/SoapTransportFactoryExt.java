/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.transport;

import java.util.Iterator;
import java.util.List;

import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.binding.soap.jms.interceptor.SoapJMSConstants;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.util.SOAPBindingUtil;
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
   private ServerConfig serverConfig;

   @Override
   public EndpointInfo createEndpointInfo(ServiceInfo serviceInfo, BindingInfo b, List<?> ees)
   {
      String transportURI = "http://schemas.xmlsoap.org/wsdl/soap/";
      if (b instanceof SoapBindingInfo)
      {
         SoapBindingInfo sbi = (SoapBindingInfo) b;
         transportURI = sbi.getTransportURI();
      }
      ServerConfig config = getServerConfig();
      EndpointInfo info = new AddressRewritingEndpointInfo(serviceInfo, transportURI, config);

      if (ees != null)
      {
         for (@SuppressWarnings("rawtypes")Iterator itr = ees.iterator(); itr.hasNext();)
         {
            Object extensor = itr.next();

            if (SOAPBindingUtil.isSOAPAddress(extensor))
            {
               final SoapAddress sa = SOAPBindingUtil.getSoapAddress(extensor);

               info.addExtensor(sa);
               info.setAddress(sa.getLocationURI());
               if (isJMSSpecAddress(sa.getLocationURI()))
               {
                  info.setTransportId(SoapJMSConstants.SOAP_JMS_SPECIFICIATION_TRANSPORTID);
               }
            }
            else
            {
               info.addExtensor(extensor);
            }
         }
      }

      return info;
   }

   private boolean isJMSSpecAddress(String address)
   {
      return address != null && address.startsWith("jms:") && !"jms://".equals(address);
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
}
