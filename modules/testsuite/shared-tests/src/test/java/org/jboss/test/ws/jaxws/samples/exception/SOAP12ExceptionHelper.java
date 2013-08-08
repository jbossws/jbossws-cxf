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

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

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
