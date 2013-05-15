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
package org.jboss.test.ws.jaxws.jbws3441;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-3441] Support CDI interceptors for POJO JAX-WS services
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class JBWS3441TestCase extends JBossWSTest
{
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS3441TestCase.class, "jaxws-jbws3441.war");
   }

   private EndpointIface getPojo() throws Exception
   {
      final URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws3441/POJOEndpointService?wsdl");
      final QName serviceName = new QName("http://org.jboss.test.ws/jbws3441", "POJOEndpointService");
      final Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(EndpointIface.class);
   }

   private EndpointIface getEjb3() throws Exception
   {
      final URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws3441/EJB3EndpointService/EJB3Endpoint?wsdl");
      final QName serviceName = new QName("http://org.jboss.test.ws/jbws3441", "EJB3EndpointService");
      final Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(EndpointIface.class);
   }

   public void testPojoCall() throws Exception
   {
      String message = "Hi";
      String response = getPojo().echo(message);
      assertEquals("Hi (including POJO interceptor)", response);
   }

   public void testEjb3Call() throws Exception
   {
      String message = "Hi";
      String response = getEjb3().echo(message);
      assertEquals("Hi (including EJB interceptor)", response);
   }
}
