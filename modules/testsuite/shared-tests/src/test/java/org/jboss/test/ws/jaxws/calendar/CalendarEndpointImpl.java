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

import java.util.Calendar;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "http://org.jboss.ws/jaxws/calendar",
            name = "CalendarEndpoint",
            portName = "EndpointPort",
            serviceName = "EndpointService",
            endpointInterface = "org.jboss.test.ws.jaxws.calendar.CalendarEndpoint")
public class CalendarEndpointImpl implements CalendarEndpoint
{

   @Override
   @WebResult(name = "return", targetNamespace = "")
   @RequestWrapper(localName = "echoXMLGregorianCalendar", targetNamespace = "http://org.jboss.ws/jaxws/calendar", className = "org.jboss.test.ws.jaxws.calendar.EchoXMLGregorianCalendar")
   @WebMethod
   @ResponseWrapper(localName = "echoXMLGregorianCalendarResponse", targetNamespace = "http://org.jboss.ws/jaxws/calendar", className = "org.jboss.test.ws.jaxws.calendar.EchoXMLGregorianCalendarResponse")
   public Object echoXMLGregorianCalendar(@WebParam(name = "arg0", targetNamespace = "") Object arg0)
   {
      return arg0;
   }

   @Override
   @WebResult(name = "return", targetNamespace = "")
   @RequestWrapper(localName = "echoCalendar", targetNamespace = "http://org.jboss.ws/jaxws/calendar", className = "org.jboss.test.ws.jaxws.calendar.EchoCalendar")
   @WebMethod
   @ResponseWrapper(localName = "echoCalendarResponse", targetNamespace = "http://org.jboss.ws/jaxws/calendar", className = "org.jboss.test.ws.jaxws.calendar.EchoCalendarResponse")
   public Calendar echoCalendar(@WebParam(name = "arg0", targetNamespace = "") Calendar arg0)
   {
      return arg0;
   }

}
