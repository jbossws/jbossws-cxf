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
package org.jboss.test.ws.jaxws.samples.endpoint;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.RunAsClient;

/**
 * Tests JAXWS dynamic endpoint deployment in an JSE environment.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class EndpointTestCase extends JBossWSTest
{
   private static final int port = 8878;

   @Test
   @RunAsClient
   public void test() throws Exception
   {
      String publishURL1 = "http://" + getServerHost() + ":" + port + "/jaxws-endpoint1";
      Endpoint endpoint1 = publishEndpoint(new EndpointBean(), publishURL1);

      String publishURL2 = "http://" + getServerHost() + ":" + port + "/jaxws-endpoint2";
      Endpoint endpoint2 = publishEndpoint(new EndpointBean(), publishURL2);

      invokeEndpoint(publishURL1);
      invokeEndpoint(publishURL2);

      endpoint1.stop();
      endpoint2.stop();
   }

   private Endpoint publishEndpoint(EndpointBean epImpl, String publishURL)
   {
      Endpoint endpoint = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, epImpl);
      endpoint.publish(publishURL);
      return endpoint;
   }

   private void invokeEndpoint(String publishURL) throws Exception
   {
      URL wsdlURL = new URL(publishURL + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint/", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);

      // Invoke the endpoint
      String helloWorld = "Hello world!";
      assertEquals(0, port.getCount());
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
      assertEquals(1, port.getCount());
      port.echo(helloWorld);
      assertEquals(2, port.getCount());
   }
}
