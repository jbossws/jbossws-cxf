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
package org.jboss.test.ws.jaxrpc.samples.exception;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test user exception propagation.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 23-Sep-2004
 */
public class ExceptionTestCase extends JBossWSTest
{
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/samples/exception";
   private static final String TARGET_ENDPOINT_URL = "http://" + getServerHost() + ":8080/jaxrpc-samples-exception";

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(ExceptionTestCase.class, "jaxrpc-samples-exception.war, jaxrpc-samples-exception-client.jar");
   }

   private ExceptionServiceInterface getPort() throws Exception
   {
      ServiceFactory serviceFactory = ServiceFactory.newInstance();
      Service service = serviceFactory.createService(new URL(TARGET_ENDPOINT_URL + "?wsdl"), new QName(TARGET_NAMESPACE, "ExceptionService"));
      return (ExceptionServiceInterface)service.getPort(new QName(TARGET_NAMESPACE, "ExceptionServiceInterfacePort"), ExceptionServiceInterface.class);
   }

   /** Test creation of a SOAPFault */
   public void testSOAPFault() throws Exception
   {
      MessageFactory msgfactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = msgfactory.createMessage();
      SOAPPart soapPart = soapMessage.getSOAPPart();
      SOAPEnvelope soapEnv = soapPart.getEnvelope();
      SOAPBody soapBody = soapEnv.getBody();
      SOAPFault soapFault = soapBody.addFault();
      Detail detail = soapFault.addDetail();
      Name name = soapEnv.createName("GetLastTradePrice", "WOMBAT", "http://www.wombat.org/trader");
      detail.addDetailEntry(name);

      QName faultCode = new QName("http://foo.bar", "faultCode");
      SOAPFaultException sfex = new SOAPFaultException(faultCode, "faultString", "faultActor", detail);
      assertEquals("faultString", sfex.getFaultString());
      assertEquals(faultCode, sfex.getFaultCode());
      assertEquals("faultActor", sfex.getFaultActor());
   }

   /** Test simple exception propagation */
   public void testException() throws Exception
   {
      ExceptionServiceInterface port = getPort();
      try
      {
         port.throwException();
         fail("Should have failed with UserException");
      }
      catch (UserException usrex)
      {
         // do nothing
      }
      catch (Exception e)
      {
         fail("Unexpected Exception: " + e);
      }
   }

   /** Test exception with message */
   public void testExceptionWithMessage() throws Exception
   {
      ExceptionServiceInterface port = getPort();

      String message = "Don't worry it's just a test";
      try
      {
         port.throwExceptionWithMessage(message);
         fail("Should have failed with UserException");
      }
      catch (UserMessageException usrex)
      {
         assertEquals(message, usrex.getMessage());
      }
      catch (Exception e)
      {
         fail("Unexpected Exception: " + e);
      }
   }

   /** Test a complex user exception */
   public void testComplexUserException() throws Exception
   {
      ExceptionServiceInterface port = getPort();

      String message = "Don't worry it's just a test";
      try
      {
         port.throwComplexUserException(message, 200);
         fail("Should have failed with ComplexUserException");
      }
      catch (ComplexUserException usrex)
      {
         assertEquals(message, usrex.getMessage());
         assertEquals(200, usrex.getErrorCode());
      }
      catch (Exception e)
      {
         fail("Unexpected Exception: " + e);
      }
   }

   /** Test a complex user exception that contains an array */
   public void testComplexUserArrayException() throws Exception
   {
      ExceptionServiceInterface port = getPort();

      String message = "Don't worry it's just a test";
      try
      {
         port.throwComplexUserArrayException(message, new int[] { 100, 200 });
         fail("Should have failed with ComplexUserArrayException");
      }
      catch (ComplexUserArrayException usrex)
      {
         assertEquals(message, usrex.getMessage());
         assertEquals(100, usrex.getErrorCodes()[0]);
         assertEquals(200, usrex.getErrorCodes()[1]);
      }
      catch (Exception e)
      {
         fail("Unexpected Exception: " + e);
      }
   }

   /** Test a fault message for a non existant operation */
   public void testNonExistantOperation() throws Exception
   {
      String reqEnv = "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" + " <env:Header/>" + " <env:Body>"
            + "  <ns1:nonExistantOperation xmlns:ns1='http://org.jboss.webservice/exception'/>" + " </env:Body>" + "</env:Envelope>";

      MessageFactory factory = MessageFactory.newInstance();
      SOAPMessage reqMsg = factory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      String targetAddress = "http://" + getServerHost() + ":8080/jaxrpc-samples-exception/ExceptionBean";
      SOAPMessage resMsg = con.call(reqMsg, targetAddress);

      SOAPFault soapFault = resMsg.getSOAPBody().getFault();
      assertNotNull("Expected SOAPFault", soapFault);

      String faultString = soapFault.getFaultString();
      assertTrue("Unexpected faultString: " + faultString, faultString.indexOf("nonExistantOperation") > 0);
   }

   /** Test a fault message for a non existant operation */
   public void testNonExistantOperationDII() throws Exception
   {
      ServiceFactory factory = ServiceFactory.newInstance();
      Service service = factory.createService(new QName(TARGET_NAMESPACE, "ExceptionService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "nonExistantOperation"));
      String targetAddress = "http://" + getServerHost() + ":8080/jaxrpc-samples-exception/ExceptionBean";
      call.setTargetEndpointAddress(targetAddress);

      try
      {
         call.invoke(new Object[] {});
         fail("Should have failed with RemoteException");
      }
      catch (RemoteException ex)
      {
         String faultString = ex.getMessage();
         assertTrue("Unexpected faultString: " + faultString, faultString.indexOf("nonExistantOperation") > 0);
      }
   }
}
