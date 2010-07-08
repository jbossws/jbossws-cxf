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
package org.jboss.test.ws.jaxws.cxf.logging;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Tests configuration of message exchange logging using API
 * 
 * @author alessio.soldano@jboss.com
 * @since 08-Jul-2010
 */
public class MessageLoggingTestCase extends JBossWSTest
{
   private String loggingFeatureEndpointURL = "http://" + getServerHost() + ":8080/jaxws-cxf-logging/LoggingFeatureEndpoint";
   private String loggingInterceptorsEndpointURL = "http://" + getServerHost() + ":8080/jaxws-cxf-logging/LoggingInterceptorsEndpoint";
   
   private LoggingEndpoint port;

   public static Test suite()
   {
      return new JBossWSTestSetup(MessageLoggingTestCase.class, "jaxws-cxf-logging.jar");
   }
   
   public void testLoggingFeature() throws Exception
   {
      URL wsdlURL = new URL(loggingFeatureEndpointURL + "?wsdl");
      QName serviceName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingFeatureService");
      Service service = Service.create(wsdlURL, serviceName);
      QName portQName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingFeatureEndpointPort");
      port = (LoggingEndpoint)service.getPort(portQName, LoggingEndpoint.class);
      
      //This is actually just a sample, the test does not actually assert the logs are written on server side for the exchanges message
      //The CXF @Feature on the endpoint ensures exchanged messages are written to the server log
      assertEquals("foo", port.echo("foo"));
   }

   public void testLoggingWithCustomInterceptors() throws Exception
   {
      URL wsdlURL = new URL(loggingInterceptorsEndpointURL + "?wsdl");
      QName serviceName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingInterceptorsService");
      Service service = Service.create(wsdlURL, serviceName);
      QName portQName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingInterceptorsEndpointPort");
      port = (LoggingEndpoint)service.getPort(portQName, LoggingEndpoint.class);
      assertEquals("foo", port.echo("foo"));
   }

   public void testClientLogging() throws Exception
   {
      URL wsdlURL = new URL(loggingFeatureEndpointURL + "?wsdl");
      QName serviceName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingFeatureService");
      
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         //install the a LoggingInInterceptor in the bus used for the client
         LoggingInInterceptor myLoggingInterceptor = new LoggingInInterceptor();
         OutputStream out = new ByteArrayOutputStream();
         myLoggingInterceptor.setPrintWriter(new PrintWriter(out, true));
         bus.getInInterceptors().add(myLoggingInterceptor);
         BusFactory.setDefaultBus(bus);
         
         Service service = Service.create(wsdlURL, serviceName);
         QName portQName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingFeatureEndpointPort");
         port = (LoggingEndpoint)service.getPort(portQName, LoggingEndpoint.class);
         String content = "foo";
         port.echo(content);
         String s = out.toString();
         assertTrue("'" + content + "' not found in captured message: \n" + s, s.contains(content));
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
}
