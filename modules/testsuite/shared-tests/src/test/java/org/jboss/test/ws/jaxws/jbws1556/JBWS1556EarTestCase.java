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
package org.jboss.test.ws.jaxws.jbws1556;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.CleanupOperation;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1556] @WebWservice does not work with class isolation
 *
 * http://jira.jboss.org/jira/browse/JBWS-1556
 *
 * @author Thomas.Diesler@jboss.com
 * @since 15-Jun-2007
 */
public class JBWS1556EarTestCase extends JBossWSTest
{
   private static EndpointInterface port;
   
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1556EarTestCase.class, "jaxws-jbws1556.ear", new CleanupOperation() {
         @Override
         public void cleanUp() {
            port = null;
         }
      });
   }

   public void setUp() throws MalformedURLException
   {
      if (port == null)
      {
         URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1556/EJB3Bean?wsdl");
         QName serviceName = new QName("http://jbws1556.jaxws.ws.test.jboss.org/", "EJB3BeanService");
         Service service = Service.create(wsdlURL, serviceName);
         port = service.getPort(EndpointInterface.class);
      }
   }

   public void testSimpleAccess() throws Exception
   {
      String hello = port.helloSimple("hello");
      assertEquals("hello", hello);
   }

   public void testComplexAccess() throws Exception
   {
      UserType req = new UserType("hello");
      UserType res = port.helloComplex(req);
      assertEquals(req, res);
   }
}
