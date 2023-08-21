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
package org.jboss.test.ws.jaxws.jbws1566.c;

import java.rmi.RemoteException;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

import org.jboss.test.ws.jaxws.jbws1566.a.TestEnumeration;
import org.jboss.test.ws.jaxws.jbws1566.b.BClass;
import org.jboss.test.ws.jaxws.jbws1566.b.BException;
import org.jboss.ws.api.annotation.WebContext;

@Stateless
@WebService(endpointInterface = "org.jboss.test.ws.jaxws.jbws1566.c.Jaxb20TestWSInterface", targetNamespace = "http://org.jboss.ws/samples/c", serviceName = "WebServiceTestService", portName = "WebServiceTestPort")
@WebContext(contextRoot = "/jaxwstest", urlPattern = "/Jaxb20StatelessTestBean/*", secureWSDLAccess = false)
public class Jaxb20StatelessTestBean implements Jaxb20TestWSInterface
{

   public TestEnumeration testMethod(BClass input) throws BException, RemoteException
   {
      System.out.println("Got input: " + input + ": a=" + input.getA() + ", b=" + input.getB());
      if (input.getA() == 0)
      {
         BException ex = new BException();
         ex.setAe(11);
         ex.setBe(13);
         throw ex;
      }
      return TestEnumeration.A;
   }
}
