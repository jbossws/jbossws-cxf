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
package org.jboss.test.ws.jaxws.samples.jmsendpoints.jmstransport;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cxf.transport.jms.spec.JMSSpecConstants;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.common.ObjectNameFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * The test for cxf jms transport packaged in a jar file and deployed with jbossws-cxf.xml
 *
 * @author <a href=mailto:ema@redhat.com> Jim Ma </a>
 */
public class JMSEndpointsTestCase extends JBossWSTest
{
   private static boolean waitForResponse;

   public static Test suite() throws Exception
   {
      if (isHornetQAvailable()) {
         return new JBossWSTestSetup(JMSEndpointsTestCase.class, "hornetq-jmsendpoints-as6.sar, jaxws-samples-jmsendpoints-as6.jar");
      } else {
         return new TestSuite();
      }
   }

   public static boolean isHornetQAvailable()
   {
      try
      {
         ObjectName oname = ObjectNameFactory.create("jboss.system:type=Server");
         String jbossVersion = (String)getServer().getAttribute(oname, "VersionNumber");
         return JBossWSTestHelper.isTargetJBoss6() && !jbossVersion.contains("M2");
      }
      catch (Exception e)
      {
         return false;
      }
   }

   public void testMessagingClient() throws Exception
   {
      String reqMessage =
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<env:Body>" +
           "<ns1:getContactInfo xmlns:ns1='http://org.jboss.ws/samples/jmstransport'>" +
            "<arg0>mafia</arg0>" +
           "</ns1:getContactInfo>" +
          "</env:Body>" +
         "</env:Envelope>";

      String resMessage =
         "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>" + 
          "<soap:Body>" +
          "<ns1:getContactInfoResponse xmlns:ns1='http://org.jboss.ws/samples/jmstransport'>" +
          "<return>The &apos;mafia&apos; boss is currently out of office, please call again.</return>" +
          "</ns1:getContactInfoResponse>" +
          "</soap:Body>" +
          "</soap:Envelope>";

      InitialContext context = new InitialContext();
      QueueConnectionFactory connectionFactory = (QueueConnectionFactory)context.lookup("ConnectionFactory");
      Queue reqQueue = (Queue)context.lookup("queue/RequestQueue");
      Queue resQueue = (Queue)context.lookup("queue/ResponseQueue");

      QueueConnection con = connectionFactory.createQueueConnection();
      QueueSession session = con.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      QueueReceiver receiver = session.createReceiver(resQueue);
      ResponseListener responseListener = new ResponseListener();
      receiver.setMessageListener(responseListener);
      con.start();

      TextMessage message = session.createTextMessage(reqMessage);
      message.setJMSReplyTo(resQueue);
      message.setStringProperty(JMSSpecConstants.CONTENTTYPE_FIELD, "text/xml");
      message.setStringProperty(JMSSpecConstants.REQUESTURI_FIELD, "/foo");

      waitForResponse = true;

      QueueSender sender = session.createSender(reqQueue);
      sender.send(message);
      sender.close();

      int timeout = 5;
      while (waitForResponse && timeout > 0)
      {
         Thread.sleep(1000);
         timeout = timeout -1;
      }

      assertNotNull("Expected response message", responseListener.resMessage);
      assertEquals(DOMUtils.parse(resMessage), DOMUtils.parse(responseListener.resMessage));

      sender.close();
      receiver.close();
      con.stop();
      session.close();
      con.close();
   }

   public static class ResponseListener implements MessageListener
   {
      public String resMessage;

      public void onMessage(Message msg)
      {
         TextMessage textMessage = (TextMessage)msg;
         try
         {
            resMessage = textMessage.getText();
            waitForResponse = false;
         }
         catch (Throwable t)
         {
            t.printStackTrace();
         }
      }
   }
}
