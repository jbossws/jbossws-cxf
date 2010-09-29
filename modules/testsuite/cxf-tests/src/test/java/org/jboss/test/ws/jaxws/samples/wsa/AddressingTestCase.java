/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsa;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.AddressingFeature;

import junit.framework.Test;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Client invoking web service using WS-Addressing
 *
 * @author richard.opalka@jboss.com
 */
public final class AddressingTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsa/AddressingService";
   
   public static Test suite()
   {
      return new JBossWSTestSetup(AddressingTestCase.class, "jaxws-samples-wsa.war");
   }

   /**
    * This tests the invocation using the local copy of the service contract; that does not have any ws-addressing
    * policy, so the addressing feature needs to be explicitly provided.
    * 
    * @throws Exception
    */
   public void testUsingLocalContract() throws Exception
   {
      // construct proxy
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsaddressing", "AddressingService");
      URL wsdlURL = getResourceURL("jaxws/samples/wsa/WEB-INF/wsdl/AddressingService.wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class, new AddressingFeature());
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL);
      // invoke method
      assertEquals("Hello World!", proxy.sayHello());
   }
   
   /**
    * This tests the invocation using the service contract published by the endpoint. That should have the
    * ws-addressing policy in it, hence no need to explicitly configure addressing, the policy engine takes care of that.
    * 
    * @throws Exception
    */
   public void testUsingContractFromDeployedEndpoint() throws Exception
   {
      // construct proxy
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsaddressing", "AddressingService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      // invoke method
      assertEquals("Hello World!", proxy.sayHello());
   }
   
}
