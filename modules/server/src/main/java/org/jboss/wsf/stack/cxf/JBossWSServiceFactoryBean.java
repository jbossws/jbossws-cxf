/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import java.io.IOException;

import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;
import org.jboss.logging.Logger;

/**
 * A service factory bean that performs additional operations
 * required by the JBossWS integration when creating a service.
 * 
 * @author alessio.soldano@jboss.com
 * @since 30-Mar-2010
 *
 */
public class JBossWSServiceFactoryBean extends JaxWsServiceFactoryBean
{
   private WSDLFilePublisher wsdlPublisher;
   
   @Override
   public Endpoint createEndpoint(EndpointInfo ei) throws EndpointException
   {
      Endpoint endpoint = super.createEndpoint(ei);
      Service service = endpoint.getService();
      // Publish wsdl after endpoint deployment, as required by JSR-109, section 8.2
      if (wsdlPublisher != null)
      {
         String wsdlLocation = isFromWsdl() ? getWsdlURL() : null;
         try
         {
            wsdlPublisher.publishWsdlFiles(service.getName(), wsdlLocation, BusFactory.getThreadDefaultBus(false), service.getServiceInfos());
         }
         catch (IOException ioe)
         {
            throw new RuntimeException("Error while publishing wsdl for service " + service.getName(), ioe);
         }
      }
      else
      {
         Logger.getLogger(JBossWSServiceFactoryBean.class).warn("WSDLPublisher not configured, unable to publish contract!");
      }
      return endpoint;
   }
   
   public void setWsdlPublisher(WSDLFilePublisher wsdlPublisher)
   {
      this.wsdlPublisher = wsdlPublisher;
   }
   
}
