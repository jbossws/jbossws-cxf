/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.bus;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 05-Oct-2010
 *
 */
public class AbstractClient
{
   public static void testBusCreation() throws BusTestException
   {
      Bus initialDefaultBus = BusFactory.getDefaultBus(false);
      Bus initialThreadBus = BusFactory.getThreadDefaultBus(false);
      BusFactory factory = BusFactory.newInstance();
      Bus bus = factory.createBus();
      assert (bus != null);
      if (initialThreadBus == null) //if the thread bus was not set before, it should now be
      {
         checkThreadBus(bus);
      }
      checkDefaultBus(initialDefaultBus);
      BusFactory.setThreadDefaultBus(initialThreadBus);
      checkThreadBus(initialThreadBus);
      checkDefaultBus(initialDefaultBus);
   }
   
   public static void testSOAPConnection(String host) throws BusTestException, Exception
   {
      Bus initialDefaultBus = BusFactory.getDefaultBus(false);
      Bus initialThreadBus = BusFactory.getThreadDefaultBus(false);
      //first call... the thread bus is reused if not null, otherwise a new one is created
      performSOAPCall(getEndpointURL(host));
      checkDefaultBus(initialDefaultBus);
      if (initialThreadBus != null)
      {
         checkThreadBus(initialThreadBus);
      }
      else
      {
         initialThreadBus = BusFactory.getThreadDefaultBus(false);
      }
      //second call...
      performSOAPCall(getEndpointURL(host));
      checkThreadBus(initialThreadBus);
      checkDefaultBus(initialDefaultBus);
   }
   
   public static void testWebServiceRef(Endpoint port) throws BusTestException
   {
      Bus initialDefaultBus = BusFactory.getDefaultBus(false);
      Bus threadBus = BusFactory.getThreadDefaultBus(false);
      performInvocation(port); //does not change anything to the bus, as the port is already created when injecting serviceref
      checkDefaultBus(initialDefaultBus);
      checkThreadBus(threadBus);
      try
      {
         BusFactory.setThreadDefaultBus(null);
         performInvocation(port); //does not change anything to the bus, as the port is already created when injecting serviceref
         checkDefaultBus(initialDefaultBus);
         checkThreadBus(null);
         BusFactory factory = BusFactory.newInstance();
         Bus bus = factory.createBus(); //internally sets the thread bus
         performInvocation(port); //does not change anything to the bus, as the port is already created when injecting serviceref
         checkDefaultBus(initialDefaultBus);
         checkThreadBus(bus);
      }
      finally
      {
         BusFactory.setThreadDefaultBus(threadBus);
      }
   }
   
   public static void testWebServiceClient(String host) throws BusTestException, Exception
   {
      Bus initialDefaultBus = BusFactory.getDefaultBus(false);
      performInvocation(getEndpointURL(host));
      checkDefaultBus(initialDefaultBus);
      //check client usage does not rely on default bus when no thread bus is set
      Bus threadBus = BusFactory.getThreadDefaultBus(false);
      try
      {
         final String url = getEndpointURL(host);
         BusFactory.setThreadDefaultBus(null);
         performInvocation(url); //goes through ServiceDelegate which sets the thread bus if it's null
         Bus newThreadBus = BusFactory.getThreadDefaultBus(false);
         if (newThreadBus == initialDefaultBus)
         {
            throw new BusTestException("Thread bus set to former default bus " + initialDefaultBus + " instead of a new bus!"); 
         }
         else if (newThreadBus == threadBus)
         {
            throw new BusTestException("Thread bus set to former thread bus " + threadBus + " instead of a new bus!"); 
         }
         performInvocation(url);
         checkThreadBus(newThreadBus); //the thread bus is not changed as it's not null
         checkDefaultBus(initialDefaultBus);
      }
      finally
      {
         BusFactory.setThreadDefaultBus(threadBus);
      }
   }
   
   protected static String getEndpointURL(String host)
   {
      return "http://" + host + ":8080/jaxws-cxf-bus/EndpointService/Endpoint";
   }
   
   protected static void performSOAPCall(String endpointAddress) throws SOAPException, MalformedURLException
   {
      SOAPFactory soapFac = SOAPFactory.newInstance();
      MessageFactory msgFac = MessageFactory.newInstance();
      SOAPConnectionFactory conFac = SOAPConnectionFactory.newInstance();
      SOAPMessage msg = msgFac.createMessage();
      SOAPConnection con = conFac.createConnection();
      QName echo = new QName("http://org.jboss.ws/bus", "echo");
      SOAPElement element = soapFac.createElement(echo);
      element.addTextNode("John");
      msg.getSOAPBody().addChildElement(element);
      SOAPMessage response = con.call(msg, new URL(endpointAddress));
      assert (response != null);
   }
   
   protected static void performInvocation(Endpoint endpoint)
   {
      String result = endpoint.echo("Alessio");
      assert ("Alessio".equals(result));
   }
   
   protected static void performInvocation(String endpointUrl) throws MalformedURLException
   {
      URL wsdlURL = new URL(endpointUrl + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/bus", "EndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      QName portQName = new QName("http://org.jboss.ws/bus", "EndpointPort");
      Endpoint endpoint = (Endpoint) service.getPort(portQName, Endpoint.class);
      performInvocation(endpoint);
   }

   protected static void checkDefaultBus(Bus expectedDefaultBus) throws BusTestException
   {
      Bus bus = BusFactory.getDefaultBus(false);
      if (bus != expectedDefaultBus)
      {
         throw new BusTestException("Default bus set to " + bus + " instead of expected " + expectedDefaultBus);
      }
   }
   
   protected static void checkThreadBus(Bus expectedThreadBus) throws BusTestException
   {
      Bus bus = BusFactory.getThreadDefaultBus(false);
      if (bus != expectedThreadBus)
      {
         throw new BusTestException("Thread " + Thread.currentThread() + " associated with bus " + bus
               + " instead of expected bus " + expectedThreadBus);
      }
   }
}
