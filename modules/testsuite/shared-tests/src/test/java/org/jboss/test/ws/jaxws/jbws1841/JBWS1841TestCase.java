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
package org.jboss.test.ws.jaxws.jbws1841;

import junit.framework.Test;

import org.jboss.wsf.test.CleanupOperation;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.net.URL;

/**
 * Serviceref through ejb3 deployment descriptor.
 *
 * http://jira.jboss.org/jira/browse/JBWS-1841
 *
 * @author Heiko.Braun@jboss.com
 */
public class JBWS1841TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws1841/EndpointService/EJB3Bean";

   private static EndpointInterface port;
   private static StatelessRemote remote;
   private static InitialContext ctx;
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1841TestCase.class, "jaxws-jbws1841.jar", new CleanupOperation() {
         @Override
         public void cleanUp() {
            port = null;
            remote = null;
            if (ctx != null)
            {
               final InitialContext c = ctx;
               ctx = null;
               try {
                  c.close();
               } catch (NamingException ne) {
                  throw new RuntimeException(ne);
               }
            }
         }
      });
   }

   protected void setUp() throws Exception
   {
      if (port == null)
      {
         URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
         QName serviceName = new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService");
         port = Service.create(wsdlURL, serviceName).getPort(EndpointInterface.class);

         ctx = getServerInitialContext();
         remote = (StatelessRemote)ctx.lookup("ejb:/jaxws-jbws1841//" + StatelessBean.class.getSimpleName() + "!" + StatelessRemote.class.getName());
      }
   }

   public void testDirectWSInvocation() throws Exception
   {
      String result = port.echo("DirectWSInvocation");
      assertEquals("DirectWSInvocation", result);
   }

   public void testEJBRelay1() throws Exception
   {
      String result = remote.echo1("Relay1");
      assertEquals("Relay1", result);
   }

   public void testEJBRelay2() throws Exception
   {
      String result = remote.echo2("Relay2");
      assertEquals("Relay2", result);
   }

   public void testEJBRelay3() throws Exception
   {
      String result = remote.echo3("Relay3");
      assertEquals("Relay3", result);
   }

   public void testEJBRelay4() throws Exception
   {
      String result = remote.echo4("Relay4");
      assertEquals("Relay4", result);
   }

}
