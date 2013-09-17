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
package org.jboss.test.ws.jaxws.samples.exception;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test JAX-WS exception handling
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
public class ExceptionTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(ExceptionTestCase.class, "jaxws-samples-exception-jse.war");
   }

   public void testRuntimeException() throws Exception
   {
      try
      {
         getHelper().testRuntimeException();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void testSoapFaultException() throws Exception
   {
      try
      {
         getHelper().testSoapFaultException();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void testApplicationException() throws Exception
   {
      try
      {
         getHelper().testApplicationException();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }
   
   public void testSOAP12RuntimeException() throws Exception
   {
      try
      {
         getSOAP12Helper().testRuntimeException();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void testSOAP12SoapFaultException() throws Exception
   {
      System.out.println("FIXME: [JBWS-3680] SOAP 1.2 fault reason xml:lang attribute not properly parsed on client side");
      /*try
      {
         getSOAP12Helper().testSoapFaultException();
      }
      catch (Exception e)
      {
         fail(e);
      }*/
   }

   public void testSOAP12ApplicationException() throws Exception
   {
      try
      {
         getSOAP12Helper().testApplicationException();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }
   
   private static void fail(Exception e) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(bos);
      e.printStackTrace(ps);
      fail(bos.toString());
      ps.close();
   }
   
   protected ExceptionHelper getHelper()
   {
      return new ExceptionHelper("http://" + getServerHost() + ":8080/jaxws-samples-exception-jse/ExceptionEndpointService");
   }
   
   protected SOAP12ExceptionHelper getSOAP12Helper()
   {
      return new SOAP12ExceptionHelper("http://" + getServerHost() + ":8080/jaxws-samples-exception-jse/SOAP12ExceptionEndpointService");
   }
}
