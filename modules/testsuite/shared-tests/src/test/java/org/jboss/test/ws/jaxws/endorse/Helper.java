/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.endorse;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.soap.SOAPBinding;
import jakarta.xml.ws.spi.Provider;

import org.apache.cxf.BusFactory;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 02-Jun-2010
 *
 */
public class Helper
{
   public static void verifyCXF()
   {
      //check BusFactory customization; this is required by the JBWS-CXF Configurer integration (HTTPConduit customization, JAXBIntros, ...)
      BusFactory factory = BusFactory.newInstance();
      if (!(factory instanceof JBossWSBusFactory))
         throw new RuntimeException("Expected " + JBossWSBusFactory.class + " but got " + (factory == null ? null : factory.getClass()));
      
      //check the Apache CXF JAXWS implementation is actually used
      Object obj = getImplementationObject();
      if (!obj.getClass().getName().contains("cxf"))
         throw new RuntimeException("JAXWS implementation is not properly selected or endorsed!");
   }
   
   private static Object getImplementationObject()
   {
      Service service = Service.create(new QName("dummyService"));
      Object obj = service.getHandlerResolver();
      if (obj == null)
      {
         service.addPort(new QName("dummyPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://dummy-address");
         obj = service.createDispatch(new QName("dummyPort"), Source.class, Mode.PAYLOAD);
      }
      return obj;
   }
   
   public static void verifyJaxWsSpiProvider(String expectedProviderClass)
   {
      Provider provider = Provider.provider();
      String clazz = provider.getClass().getName();
      if (!clazz.equals(expectedProviderClass)) {
         throw new RuntimeException("Expected " + expectedProviderClass + " but got " + clazz);
      }
   }
}
