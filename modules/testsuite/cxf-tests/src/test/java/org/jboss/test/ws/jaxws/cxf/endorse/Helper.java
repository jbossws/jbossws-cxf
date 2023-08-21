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
package org.jboss.test.ws.jaxws.cxf.endorse;

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
