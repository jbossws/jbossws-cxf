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
package org.jboss.test.ws.jaxws.calendar;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Calendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test calendar type binding
 *
 * @author alessio.soldano@jboss.com
 * @since 22-Apr-2014
 */
public class CalendarTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-calendar/EndpointService";

   public static Test suite()
   {
      return new JBossWSTestSetup(CalendarTestCase.class, "jaxws-calendar.war");
   }

   public void testCalendar() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/calendar", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      CalendarEndpoint port = service.getPort(CalendarEndpoint.class);

      Calendar calendar = Calendar.getInstance();
      
      Calendar response = port.echoCalendar(calendar);
      assertEquals(calendar.getTimeInMillis(), response.getTimeInMillis());
   }

   public void testXMLGregorianCalendar() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/calendar", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      CalendarEndpoint port = service.getPort(CalendarEndpoint.class);

      DatatypeFactory calFactory = DatatypeFactory.newInstance();
      XMLGregorianCalendar calendar = calFactory.newXMLGregorianCalendar(2002, 4, 5, 0, 0, 0, 0, 0);
      
      Object response = port.echoXMLGregorianCalendar(calendar);
      assertEquals("2002-04-05T00:00:00.000Z", response.toString());
   }
   
   public void testEmptyCalendar() throws Exception {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
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
