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
package org.jboss.test.ws.jaxws.jbws2486;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2486] POJO service should be shared
 *
 * @author richard.opalka@jboss.com
 */
public class JBWS2486TestCase extends JBossWSTest
{
   private String targetNS = "http://jbws2486.jaxws.ws.test.jboss.org/";
   private JBWS2486 proxy;

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2486TestCase.class, "jaxws-jbws2486.war");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "JBWS2486Service");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2486/JBWS2486Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      proxy = (JBWS2486)service.getPort(JBWS2486.class);
   }

   public void testIssue() throws Exception
   {
      String serviceInstanceId = proxy.getServiceInstanceId();
      for (int i = 1; i <= 10; i++)
      {
         assertEquals(proxy.getServiceInstanceId(), serviceInstanceId);
      }
   }

}
