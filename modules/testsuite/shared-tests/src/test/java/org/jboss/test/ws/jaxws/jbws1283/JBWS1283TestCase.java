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
package org.jboss.test.ws.jaxws.jbws1283;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import jakarta.xml.soap.AttachmentPart;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.api.handler.GenericSOAPHandler;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1283] Attachment dropped on outbound messages if they have been added through a handler
 */
@Ignore(value="[JBWS-2480] Soap attachments are dropped on server response")
@RunWith(Arquillian.class)
public class JBWS1283TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1283.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1283.AttachmentHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1283.JBWS1283Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1283.JBWS1283EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1283.JBWS1283TestCase.VerifyAttachmentHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1283.JBWS1283TestCase.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws1283/jaxws-handlers-server.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testAttachmentResponse() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.test.ws/jbws1283", "JBWS1283Service");
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1283/JBWS1283Service/JBWS1283EndpointImpl?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      JBWS1283Endpoint port = service.getPort(JBWS1283Endpoint.class);
      
      // Add a client-side handler that verifes existence of the attachment
      BindingProvider bindingProvider = (BindingProvider)port;
      @SuppressWarnings("rawtypes")
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.add(new VerifyAttachmentHandler());
      bindingProvider.getBinding().setHandlerChain(handlerChain);

      port.requestAttachmentData();
   }

   // handler that verifies the attachment that have been added on the server-side
   static class VerifyAttachmentHandler extends GenericSOAPHandler<SOAPMessageContext>
   {
      @Override
      protected boolean handleInbound(SOAPMessageContext msgContext)
      {
         SOAPMessage soapMessage = msgContext.getMessage();
         Iterator<?> it = soapMessage.getAttachments();
         while(it.hasNext())
         {
            try
            {
               AttachmentPart attachment = (AttachmentPart)it.next();
               System.out.println("Recv " + attachment.getContentType() + " attachment:");
               System.out.println("'"+attachment.getContent()+"'");
               return true;
            }
            catch (SOAPException e)
            {
               throw new RuntimeException("Failed to access attachment data");
            }
         }

         throw new IllegalStateException("Missing attachment on the client side");
      }
   }
}
