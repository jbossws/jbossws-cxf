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
package org.jboss.test.ws.jaxws.jbws2241;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2241] Testing url-pattern parameter in jboss.xml
 *
 * @author alessio.soldano@jboss.com
 * @since 29-Sep-2008
 */
public class JBWS2241TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2241TestCase.class, isTargetJBoss71() ? "jaxws-jbws2241-as71.jar" : "jaxws-jbws2241.jar", true);
   }

   private EndpointInterface getPort(String user, String pwd) throws MalformedURLException {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/contextRoot/urlPattern/test?wsdl");
      QName serviceName = new QName("http://jbws2241.jaxws.ws.test.jboss.org/", "EJB3BeanService");
      Service service = Service.create(wsdlURL, serviceName);
      EndpointInterface port = service.getPort(EndpointInterface.class);
      if (user != null) {
         ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user);
      }
      if (pwd != null) {
         ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pwd);
      }
      return port;
   }

   public void testInvocation() throws Exception
   {
      EndpointInterface port = getPort("kermit", "thefrog");
      String hello = port.hello("hello");
      assertEquals("hello", hello);
      
      port = getPort("kermit", "notthefrog");
      try {
         port.hello("hi");
         fail("Failure expected with wrong credentials");
      } catch (Exception e) {
         
      }
      
      port = getPort(null, null);
      try {
         port.hello("hi");
         fail("Failure expected without credentials");
      } catch (Exception e) {
         
      }
   }
}
