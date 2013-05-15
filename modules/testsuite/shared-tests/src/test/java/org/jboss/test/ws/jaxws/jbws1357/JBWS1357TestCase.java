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
package org.jboss.test.ws.jaxws.jbws1357;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1357] JAXWSDeployerJSE is not handling jsp servlet defs correctly
 * 
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
public class JBWS1357TestCase extends JBossWSTest
{
   private String targetNS = "http://jbws1357.jaxws.ws.test.jboss.org/";
   private JBWS1357 proxy;

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1357TestCase.class, "jaxws-jbws1357.war");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "JBWS1357Service");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1357/JBWS1357Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      proxy = (JBWS1357)service.getPort(JBWS1357.class);
   }

   public void testEcho() throws Exception
   {
      assertEquals("hi there", proxy.echo("hi there"));
   }

   public void testJSP() throws Exception
   {
      URL jsp = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1357/hello.jsp");
      HttpURLConnection conn = (HttpURLConnection) jsp.openConnection();
      assertEquals(conn.getResponseCode(), 200);
   }
}
