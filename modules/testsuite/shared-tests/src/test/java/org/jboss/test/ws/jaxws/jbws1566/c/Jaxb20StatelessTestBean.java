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
package org.jboss.test.ws.jaxws.jbws1566.c;

import java.rmi.RemoteException;

import javax.ejb.Stateless;
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
