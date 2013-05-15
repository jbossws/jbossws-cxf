/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2978;

import java.io.ByteArrayInputStream;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * JBWS2978TestCase.
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JBWS2978TestCase extends JBossWSTest
{

   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws2978";

   private final String requestMessage = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Header><To xmlns='http://www.w3.org/2005/08/addressing'>"
         + TARGET_ENDPOINT_ADDRESS
         + "</To><Action xmlns='http://www.w3.org/2005/08/addressing'>inputAction</Action>"
         + "<MessageID xmlns='http://www.w3.org/2005/08/addressing'>uuid:56d586f8-980c-48cf-982d-77a2f56e5c5b</MessageID>"
         + "<ReplyTo xmlns='http://www.w3.org/2005/08/addressing'><Address>http://www.w3.org/2005/08/addressing/anonymous</Address></ReplyTo>"
         + "</S:Header><S:Body><ns1:addNumbers xmlns:ns1='http://ws.jboss.org'><arg0>10</arg0><arg1>10</arg1></ns1:addNumbers></S:Body></S:Envelope>";

   public Service service = null;

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2978TestCase.class, "jaxws-jbws2978.war");
   }

   public void setUp() throws Exception
   {
      super.setUp();
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://ws.jboss.org", "AddNumbers");
      service = Service.create(wsdlURL, serviceName);
   }

   public void testCall() throws Exception
   {
      try
      {
         Dispatch dispatch = service.createDispatch(new QName("http://ws.jboss.org", "AddNumbersPort"), SOAPMessage.class ,
               Service.Mode.MESSAGE);
         SOAPMessage reqMsg = MessageFactory.newInstance().createMessage(null,
               new ByteArrayInputStream(requestMessage.getBytes()));
         BindingProvider bp = (BindingProvider)dispatch;
         java.util.Map<String, Object> requestContext = bp.getRequestContext();
         requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, "mismatchAction");
         dispatch.invoke(reqMsg);
         fail("Should throw SOAPFaultExceptoin");
      }
      catch (SOAPFaultException e)
      {
         assertEquals(true, e.getFault().getFaultCode().indexOf("ActionMismatch") > -1);
      }
   }
}