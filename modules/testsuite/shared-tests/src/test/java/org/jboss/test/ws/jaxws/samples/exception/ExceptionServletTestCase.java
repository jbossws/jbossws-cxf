/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.exception;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

public class ExceptionServletTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(ExceptionServletTestCase.class, "jaxws-samples-exception-jse.war, jaxws-samples-exception-client.war");
   }
   
   public void testRuntimeException() throws Exception
   {
      assertEquals("1", runTestInContainer("testRuntimeException"));
   }

   public void testSoapFaultException() throws Exception
   {
      assertEquals("1", runTestInContainer("testSoapFaultException"));
   }

   public void testApplicationException() throws Exception
   {
      assertEquals("1", runTestInContainer("testApplicationException"));
   }
   
   public void testRuntimeExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testRuntimeException"));
   }

   public void testSoapFaultExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testSoapFaultException"));
   }

   public void testApplicationExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testApplicationException"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-samples-exception-client?path=/jaxws-samples-exception-jse/ExceptionEndpointService&method=" + test
            + "&helper=" + ExceptionHelper.class.getName());
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      return br.readLine();
   }
   
   private String runTestInContainerSOAP12(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-samples-exception-client?path=/jaxws-samples-exception-jse/SOAP12ExceptionEndpointService&method=" + test
            + "&helper=" + SOAP12ExceptionHelper.class.getName());
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      return br.readLine();
   }
}
