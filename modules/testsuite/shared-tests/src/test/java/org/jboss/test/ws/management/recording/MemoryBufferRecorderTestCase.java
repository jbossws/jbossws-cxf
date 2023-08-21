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
package org.jboss.test.ws.management.recording;

import java.net.InetAddress;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordFilter;
import org.jboss.ws.common.monitoring.AndFilter;
import org.jboss.ws.common.monitoring.HostFilter;
import org.jboss.ws.common.monitoring.NotFilter;
import org.jboss.ws.common.monitoring.OperationFilter;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * This tests the MemoryBufferRecorder
 * 
 * @author alessio.soldano@jboss.com
 * @since 7-Aug-2008
 */
@RunWith(Arquillian.class)
public class MemoryBufferRecorderTestCase extends JBossWSTest
{
   private final String targetNS = "http://recording.management.ws.test.jboss.org/";
   private final String endpointObjectName = "jboss.ws:context=management-recording,endpoint=EndpointWithConfigImpl";

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "management-recording-as7.jar");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
         + "Dependencies: org.jboss.logging,org.jboss.ws.common\n")) //see https://docs.jboss.org/author/display/JBWS/Predefined+client+and+endpoint+configurations#Predefinedclientandendpointconfigurations-Handlersclassloading
         .addClass(org.jboss.test.ws.management.recording.Endpoint.class)
         .addClass(org.jboss.test.ws.management.recording.EndpointWithConfigImpl.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testRecording() throws Exception
   {
      Endpoint port = getPort();
      MBeanServerConnection server = getServer();
      ObjectName oname = new ObjectName(endpointObjectName + ",recordProcessor=MemoryBufferRecorder");
      int startSize = (Integer)server.getAttribute(oname, "Size");
      setRecording(true);
      port.echo1("Hello");
      port.echo1("Hello again");
      port.echo2("Hi");
      setRecording(false);
      port.echo2("Hi again");
      int endSize = (Integer)server.getAttribute(oname, "Size");
      assertEquals(3, endSize - startSize);
   }

   @Test
   @RunAsClient
   @SuppressWarnings("unchecked")
   public void testGetRecordsByOperation() throws Exception
   {
      Endpoint port = getPort();
      MBeanServerConnection server = getServer();
      ObjectName oname = new ObjectName(endpointObjectName + ",recordProcessor=MemoryBufferRecorder");
      setRecording(true);

      port.echo1("Test getRecordsByOperation");
      port.echo2("Test getRecordsByOperation");

      Map<String, List<Record>> echo1Records = (Map<String, List<Record>>)server.invoke(oname, "getRecordsByOperation", new Object[] { targetNS, "echo1" }, new String[] {
            "java.lang.String", "java.lang.String" });
      Map<String, List<Record>> echo2Records = (Map<String, List<Record>>)server.invoke(oname, "getRecordsByOperation", new Object[] { targetNS, "echo2" }, new String[] {
            "java.lang.String", "java.lang.String" });

      assertFalse(echo1Records.isEmpty());
      assertFalse(echo2Records.isEmpty());
      for (List<Record> list : echo1Records.values())
      {
         for (Record record : list)
         {
            QName qName = record.getOperation();
            assertEquals(targetNS, qName.getNamespaceURI());
            assertEquals("echo1", qName.getLocalPart());
            assertTrue(record.getEnvelope().contains("echo1"));
         }
      }
      for (List<Record> list : echo2Records.values())
      {
         for (Record record : list)
         {
            QName qName = record.getOperation();
            assertEquals(targetNS, qName.getNamespaceURI());
            assertEquals("echo2", qName.getLocalPart());
            assertTrue(record.getEnvelope().contains("echo2"));
         }
      }
   }

   @Test
   @RunAsClient
   @SuppressWarnings("unchecked")
   public void testGetRecordsByClientHost() throws Exception
   {
      Endpoint port = getPort();
      MBeanServerConnection server = getServer();
      ObjectName oname = new ObjectName(endpointObjectName + ",recordProcessor=MemoryBufferRecorder");
      setRecording(true);
      
      port.echo1("Test getRecordsByClientHost");
      
      // MemoryBufferRecorder stores source host host as InetAddress IP format, eg. [::1] will be stored as 0:0:0:0:0:0:0:1
      String host = InetAddress.getByName(getServerHost()).getHostAddress();
      
      Map<String, List<Record>> localhostRecords = (Map<String, List<Record>>)server.invoke(oname, "getRecordsByClientHost", new Object[] { host },
            new String[] { "java.lang.String" });
      Map<String, List<Record>> amazonRecords = (Map<String, List<Record>>)server.invoke(oname, "getRecordsByClientHost", new Object[] { "72.21.203.1" },
            new String[] { "java.lang.String" });
      
      assertTrue("No records for " + host, localhostRecords.size() > 0);
      assertTrue("There are records for 72.21.203.1", amazonRecords.size() == 0);
   }

   @Test
   @RunAsClient
   @SuppressWarnings("unchecked")
   public void testGetMatchingRecords() throws Exception
   {
      Endpoint port = getPort();
      MBeanServerConnection server = getServer();
      ObjectName oname = new ObjectName(endpointObjectName + ",recordProcessor=MemoryBufferRecorder");
      setRecording(true);

      OperationFilter operationFilter = new OperationFilter(new QName(targetNS, "echo1"));
      LinkedList<String> l = new LinkedList<String>();
      l.add(getServerHost().replace("127.0.0.1", "localhost"));
      HostFilter hostFilter = new HostFilter(l,false); //destination

      RecordFilter[] filters = new RecordFilter[] {operationFilter, hostFilter};
      Map<String, List<Record>> startRecords = (Map<String, List<Record>>)server.invoke(oname, "getMatchingRecords", new Object[] { filters }, new String[] { filters.getClass().getName() });

      port.echo1("Test getMatchingRecords");
      port.echo2("Test getMatchingRecords");

      Map<String, List<Record>> stopRecords = (Map<String, List<Record>>)server.invoke(oname, "getMatchingRecords", new Object[] { filters }, new String[] { filters.getClass().getName() });
      Map<String, List<Record>> allRecords = (Map<String, List<Record>>) server.invoke(oname, "getMatchingRecords",
              new Object[] { new RecordFilter[]{ operationFilter } }, new String[] { filters.getClass().getName() });
      assertTrue("No records for hosts " + l + ", all records found: " + dumpInboundRecordsInfo(allRecords), stopRecords.size() > 0);
      assertEquals("There must be only 1 record for echo1 operation", 1, stopRecords.keySet().size() - startRecords.keySet().size());
   }

   @Test
   @RunAsClient
   public void testAddRemoveFilter() throws Exception
   {
      Endpoint port = getPort();
      MBeanServerConnection server = getServer();
      ObjectName oname = new ObjectName(endpointObjectName + ",recordProcessor=MemoryBufferRecorder");
      setRecording(true);

      OperationFilter operationFilter = new OperationFilter(new QName(targetNS, "echo1"));

      //Adding Operation filter...
      server.invoke(oname, "addFilter", new Object[] { operationFilter }, new String[] { RecordFilter.class.getName() });
      int size = (Integer)server.getAttribute(oname, "Size");
      port.echo1("Test testAddRemoveFilter");
      port.echo2("Test testAddRemoveFilter");
      assertEquals(1, (Integer)server.getAttribute(oname, "Size") - size);

      //Clean filters...
      server.setAttribute(oname, new Attribute("Filters", new LinkedList<RecordFilter>()));
      size = (Integer)server.getAttribute(oname, "Size");
      port.echo1("Test testAddRemoveFilter");
      port.echo2("Test testAddRemoveFilter");
      assertEquals(2, (Integer)server.getAttribute(oname, "Size") - size);

      NotFilter notFilter = new NotFilter(operationFilter);
      AndFilter andFilter = new AndFilter(operationFilter, notFilter);

      //Adding And filter...
      server.invoke(oname, "addFilter", new Object[] { andFilter }, new String[] { RecordFilter.class.getName() });
      size = (Integer)server.getAttribute(oname, "Size");
      port.echo1("Test testAddRemoveFilter");
      port.echo2("Test testAddRemoveFilter");
      assertEquals(0, (Integer)server.getAttribute(oname, "Size") - size);

      //Clean filters...
      server.setAttribute(oname, new Attribute("Filters", new LinkedList<RecordFilter>()));
      size = (Integer)server.getAttribute(oname, "Size");
      port.echo1("Test testAddRemoveFilter");
      port.echo2("Test testAddRemoveFilter");
      assertEquals(2, (Integer)server.getAttribute(oname, "Size") - size);
   }

   private void setRecording(boolean recording) throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName oname = new ObjectName(endpointObjectName + ",recordProcessor=MemoryBufferRecorder");
      Attribute attribute = new Attribute("Recording", recording);
      server.setAttribute(oname, attribute);
      assertEquals(recording, server.getAttribute(oname, "Recording"));
   }

   private Endpoint getPort() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/management-recording/EndpointImpl?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(Endpoint.class);
   }

   private String dumpInboundRecordsInfo(Map<String, List<Record>> records) {
      StringBuilder sb = new StringBuilder();
      for (String key : records.keySet()) {
         sb.append(records.get(key)).append("\n");
         for (Record record : records.get(key)) {
            if(record.getMessageType() == Record.MessageType.INBOUND) {
               sb.append("  ").append(record.getGroupID()).append("\n");
               sb.append("    Source host: ").append(record.getSourceHost()).append("\n");
               sb.append("    Destination host: ").append(record.getDestinationHost()).append("\n");
            }
         }
      }
      return sb.toString();
   }

}
