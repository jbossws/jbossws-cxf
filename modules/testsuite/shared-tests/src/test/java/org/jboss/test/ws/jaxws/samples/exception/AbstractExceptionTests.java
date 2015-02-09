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

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;

/**
 * Test JAX-WS exception handling
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
public abstract class AbstractExceptionTests extends JBossWSTest
{
   @Test
   @RunAsClient
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

   @Test
   @RunAsClient
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

   @Test
   @RunAsClient
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

   @Test
   @RunAsClient
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

   @Test
   @RunAsClient
   public void testSOAP12SoapFaultException() throws Exception
   {
      try
      {
         getSOAP12Helper().testSoapFaultException();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   @Test
   @RunAsClient
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
   
   abstract protected ExceptionHelper getHelper();
   
   abstract protected SOAP12ExceptionHelper getSOAP12Helper();
}
