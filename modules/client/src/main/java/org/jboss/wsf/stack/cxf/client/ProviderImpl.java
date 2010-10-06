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
package org.jboss.wsf.stack.cxf.client;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.ServiceDelegate;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.jaxws.ServiceImpl;
import org.jboss.logging.Logger;

/**
 * A custom javax.xml.ws.spi.Provider implementation
 * extending the CXF one while adding few customizations
 * 
 * @author alessio.soldano@jboss.com
 * @since 27-Aug-2010
 *
 */
public class ProviderImpl extends org.apache.cxf.jaxws22.spi.ProviderImpl
{
   @Override
   protected org.apache.cxf.jaxws.EndpointImpl createEndpointImpl(Bus bus, String bindingId, Object implementor,
         WebServiceFeature... features)
   {
      Boolean db = (Boolean)bus.getProperty(Constants.DEPLOYMENT_BUS);
      if (db != null && db)
      {
         Logger.getLogger(ProviderImpl.class).info(
               "Cannot use the bus associated to the current deployment for starting a new endpoint, creating a new bus...");
         bus = BusFactory.newInstance().createBus();
      }
      return super.createEndpointImpl(bus, bindingId, implementor, features);
   }
   
   @SuppressWarnings("rawtypes")
   @Override
   public ServiceDelegate createServiceDelegate(URL url, QName qname, Class cls)
   {
      //we override this method to prevent using the default bus when the current
      //thread is not already associated to a bus. In those situations we create
      //a new bus from scratch instead and link that to the thread.
      Bus bus = BusFactory.getThreadDefaultBus(false);
      if (bus == null)
      {
         bus = BusFactory.newInstance().createBus(); //this also set thread local bus internally as it's not set yet
      }
      return new ServiceImpl(bus, url, qname, cls);
   }

   @SuppressWarnings("rawtypes")
   @Override
   public ServiceDelegate createServiceDelegate(URL wsdlDocumentLocation, QName serviceName, Class serviceClass,
         WebServiceFeature... features)
   {
      //we override this method to prevent using the default bus when the current
      //thread is not already associated to a bus. In those situations we create
      //a new bus from scratch instead and link that to the thread.
      Bus bus = BusFactory.getThreadDefaultBus(false);
      if (bus == null)
      {
         bus = BusFactory.newInstance().createBus(); //this also set thread local bus internally as it's not set yet 
      }
      return super.createServiceDelegate(wsdlDocumentLocation, serviceName, serviceClass, features);
   }
}
