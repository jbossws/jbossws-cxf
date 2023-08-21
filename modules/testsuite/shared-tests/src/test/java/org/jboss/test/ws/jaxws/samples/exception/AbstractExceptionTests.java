/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
