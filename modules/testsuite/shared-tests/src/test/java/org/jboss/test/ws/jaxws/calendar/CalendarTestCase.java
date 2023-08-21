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
package org.jboss.test.ws.jaxws.calendar;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Calendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test calendar type binding
 *
 * @author alessio.soldano@jboss.com
 * @since 22-Apr-2014
 */
@RunWith(Arquillian.class)
public class CalendarTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-calendar.war");
         archive
            .addManifest()
            .addPackages(false, new Filter<ArchivePath>() {
               @Override
               public boolean include(ArchivePath path)
               {
                  return !path.get().contains("TestCase");
               }
            }, "org.jboss.test.ws.jaxws.calendar");
      return archive;
   }

   @Test
   @RunAsClient
   public void testCalendar() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/EndpointService?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/calendar", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      CalendarEndpoint port = service.getPort(CalendarEndpoint.class);

      Calendar calendar = Calendar.getInstance();
      
      Calendar response = port.echoCalendar(calendar);
      assertEquals(calendar.getTimeInMillis(), response.getTimeInMillis());
   }

   @Test
   @RunAsClient
   public void testXMLGregorianCalendar() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/EndpointService?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/calendar", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      CalendarEndpoint port = service.getPort(CalendarEndpoint.class);

      DatatypeFactory calFactory = DatatypeFactory.newInstance();
      XMLGregorianCalendar calendar = calFactory.newXMLGregorianCalendar(2002, 4, 5, 0, 0, 0, 0, 0);
      
      Object response = port.echoXMLGregorianCalendar(calendar);
      assertEquals("2002-04-05T00:00:00.000Z", response.toString());
   }

   @Test
   @RunAsClient
   public void testEmptyCalendar() throws Exception {
      URL wsdlURL = new URL(baseURL + "/EndpointService?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/calendar", "EndpointService");
      Service service = Service.create(wsdlURL, qname);

      Dispatch<SOAPMessage> dispatch = service.createDispatch(new QName("http://org.jboss.ws/jaxws/calendar", "EndpointPort"), SOAPMessage.class, Mode.MESSAGE);

      String reqEnv = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:echoCalendar xmlns:ns2=\"http://org.jboss.ws/jaxws/calendar\"><arg0/></ns2:echoCalendar></soap:Body></soap:Envelope>";

      SOAPMessage reqMsg = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));
      SOAPMessage resMsg = dispatch.invoke(reqMsg);

      assertNotNull(resMsg);
      //TODO improve checks
   }
}
