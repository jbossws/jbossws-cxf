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
package org.jboss.test.ws.management.recording;

import java.net.URL;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.ws.api.monitoring.RecordProcessor;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;

/**
 * This test case shows how to dynamically add a custom record processor
 * to a given endpoint.
 * 
 * @author alessio.soldano@jboss.com
 * @since 6-Aug-2008
 */
public class CustomRecordProcessorTestCase extends JBossWSTest
{
   private String endpointURL = "http://" + getServerHost() + ":8080/management-recording/EndpointImpl";
   private String targetNS = "http://recording.management.ws.test.jboss.org/";
   private String endpointObjectName;

   protected void setUp() throws Exception
   {
      endpointObjectName = "jboss.ws:context=management-recording,endpoint=EndpointWithConfigImpl";
      JBossWSTestHelper.deploy("management-recording-as7.jar");
   }
   
   protected void tearDown() throws Exception
   {
      JBossWSTestHelper.undeploy("management-recording-as7.jar");
   }

   public void testAddCustomProcessor() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);
      System.out.println("FIXME: [JBWS-3330] RMI class loader disabled / CNFE with remote classloader");
//      addCustomProcessor();
//      Object retObj = port.echo1("Hello");
//      assertEquals("Hello", retObj);
//      checkCustomProcessorJob();
   }
   
   private void addCustomProcessor() throws Exception
   {
      ObjectName oname = new ObjectName(endpointObjectName);
      ExtManagedProcessor myProcessor = new ExtManagedProcessor();
      myProcessor.setName("myExtProcessor");
      myProcessor.setRecording(true);
      myProcessor.setAttribute("Attribute value");
      myProcessor.setExtAttribute("ExtAttribute value");
      getServer().invoke(oname, "addRecordProcessor", new Object[] { myProcessor }, new String[] { RecordProcessor.class.getName() });
   }
   
   private void checkCustomProcessorJob() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName oname = new ObjectName(endpointObjectName + ",recordProcessor=myExtProcessor");
      assertEquals(true, server.getAttribute(oname, "Recording"));
      assertEquals(1, server.getAttribute(oname, "Size"));
      assertEquals("Attribute value", server.getAttribute(oname, "Attribute"));
   }
}
