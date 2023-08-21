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
package org.jboss.test.ws.jaxws.jbws1666;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.spi.Provider;

public class TestClient
{
   public static final String REQ_STR = "Hello World!";

   public static String testPortAccess(String serverHost, int serverPort) throws Exception
   {
      URL wsdlURL = new URL("http://" + serverHost + ":" + serverPort + "/jaxws-jbws1666?wsdl");

      QName serviceName = new QName("http://org.jboss.ws/jbws1666", "TestEndpointImplService");
      Service service = Service.create(wsdlURL, serviceName);
      TestEndpoint port = (TestEndpoint)service.getPort(TestEndpoint.class);

      String resStr = port.echo(REQ_STR);
      return resStr;
   }

   public static void main(String[] args) throws Exception
   {
      String serverHost = args[0];
      String serverPort = args[1];
      String resStr = testPortAccess(serverHost, Integer.valueOf(serverPort));
      System.out.println(Provider.provider().getClass().getName() + ", " + resStr);
      
      //wait a bit before returning as the log processing can be aysnch, the test client
      //relies on the log contents and the log streams are closed by the system when the
      //process terminates
      Thread.sleep(1000);
   }
}
