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

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.jboss.test.helper.ClientHelper;
import org.jboss.test.ws.jaxws.samples.exception.client.ExceptionEndpoint;
import org.jboss.test.ws.jaxws.samples.exception.client.UserException;
import org.jboss.test.ws.jaxws.samples.exception.client.UserException_Exception;
import org.w3c.dom.Element;

public class SOAP12ExceptionHelper implements ClientHelper
{
   protected String targetEndpoint;
   protected String targetNS = "http://server.exception.samples.jaxws.ws.test.jboss.org/";
   
   public SOAP12ExceptionHelper(String targetEndpoint)
   {
      this.targetEndpoint = targetEndpoint;
   }
   
   public SOAP12ExceptionHelper()
   {
      //NOOP
   }
   
   protected ExceptionEndpoint getProxy() throws Exception
   {
      QName serviceName = new QName(targetNS, "SOAP12ExceptionEndpointImplService");
      URL wsdlURL = new URL(targetEndpoint + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(ExceptionEndpoint.class);
   }
   
   /*
    * 10.2.2.3
    *
    * faultcode (Subcode in SOAP 1.2, Code set to env:Receiver)
    *    1. SOAPFaultException.getFault().getFaultCodeAsQName()
    *    2. env:Server (Subcode omitted for SOAP 1.2).
    * faultstring (Reason/Text)
    *    1. SOAPFaultException.getFault().getFaultString()
    *    2. Exception.getMessage()
    *    3. Exception.toString()
    * faultactor (Role in SOAP 1.2)
    *    1. SOAPFaultException.getFault().getFaultActor()
    *    2. Empty
    * detail (Detail in SOAP 1.2)
    *    1. Serialized service specific exception (see WrapperException.getFaultInfo() in section 2.5)
    *    2. SOAPFaultException.getFault().getDetail()
    */
   public void testRuntimeException() throws Exception
   {
      try
      {
         getProxy().throwRuntimeException();
         fail("Expected SOAPFaultException");
      }
      catch (SOAPFaultException e)
      {
         String faultString = e.getFault().getFaultString();
         assertTrue(faultString.indexOf("oh no, a runtime exception occured.") >= 0);
      }
   }

   public void testSoapFaultException() throws Exception
   {
      try
      {
         getProxy().throwSoapFaultException();
         fail("Expected SOAPFaultException");
      }
      catch (SOAPFaultException e)
      {
         assertEquals("this is a fault string!", e.getFault().getFaultString());
         assertEquals("mr.actor", e.getFault().getFaultActor());
         assertEquals("VersionMismatch", e.getFault().getFaultCodeAsName().getLocalName());
         assertEquals("http://www.w3.org/2003/05/soap-envelope", e.getFault().getFaultCodeAsName().getURI());
         final QName subcode = (QName)e.getFault().getFaultSubcodes().next();
         assertEquals("http://ws.gss.redhat.com/", subcode.getNamespaceURI());
         assertEquals("NullPointerException", subcode.getLocalPart());
         assertEquals("test", ((Element)e.getFault().getDetail().getChildElements().next()).getLocalName());
         assertEquals("it", e.getFault().getFaultReasonLocales().next().toString());
      }
   }

   public void testApplicationException() throws Exception
   {
      try
      {
         getProxy().throwApplicationException();
         fail("Expected UserException");
      }
      catch (UserException_Exception e)
      {
         UserException ue = e.getFaultInfo();
         assertEquals("Some validation error", ue.getMessage());
         assertEquals("validation", ue.getErrorCategory());
         assertEquals(123, ue.getErrorCode());
      }
   }
   
   private static void fail(String s) throws Exception
   {
      throw new Exception(s);
   }
   
   private static void assertEquals(Object exp, Object actual) throws Exception
   {
      if (!(exp.equals(actual)))
      {
         throw new Exception("Expected #" + exp + "# but was #" + actual + "#");
      }
   }
   
   private static void assertTrue(boolean actual) throws Exception
   {
      if (!(actual))
      {
         throw new Exception("Expected true");
      }
   }

   @Override
   public void setTargetEndpoint(String address)
   {
      targetEndpoint = address;
   }
}
