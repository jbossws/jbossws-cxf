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

import java.net.InetAddress;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordFilter;
import org.jboss.ws.common.monitoring.AndFilter;
import org.jboss.ws.common.monitoring.HostFilter;
import org.jboss.ws.common.monitoring.NotFilter;
import org.jboss.ws.common.monitoring.OperationFilter;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;

/**
 * This tests the MemoryBufferRecorder
 * 
 * @author alessio.soldano@jboss.com
 * @since 7-Aug-2008
 */
public class MemoryBufferRecorderTestCase extends JBossWSTest
{
   private final String endpointURL = "http://" + getServerHost() + ":8080/management-recording/EndpointImpl";
   private final String targetNS = "http://recording.management.ws.test.jboss.org/";
   private String endpointObjectName;

   @Override
   protected void setUp() throws Exception
   {
      endpointObjectName = "jboss.ws:context=management-recording,endpoint=EndpointWithConfigImpl";
      JBossWSTestHelper.deploy("management-recording-as7.jar");
   }

   @Override
   protected void tearDown() throws Exception
   {
      JBossWSTestHelper.undeploy("management-recording-as7.jar");
   }
   
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


   @SuppressWarnings("unchecked")
   public void testGetRecordsByClientHost() throws Exception
   {
      Endpoint port = getPort();
      MBeanServerConnection server = getServer();
      ObjectName oname = new ObjectName(endpointObjectName + ",recordProcessor=MemoryBufferRecorder");
      setRecording(true);
      
      port.echo1("Test getRecordsByClientHost");

      // MemoryBufferRecorder stores source host as InetAddress IP format, eg. [::1] will be considered 0:0:0:0:0:0:0:1
      String host = InetAddress.getByName(getServerHost()).getHostAddress();

      Map<String, List<Record>> localhostRecords = (Map<String, List<Record>>)server.invoke(oname, "getRecordsByClientHost", new Object[] { host },
            new String[] { "java.lang.String" });
      Map<String, List<Record>> amazonRecords = (Map<String, List<Record>>)server.invoke(oname, "getRecordsByClientHost", new Object[] { "72.21.203.1" },
            new String[] { "java.lang.String" });
      assertTrue("No records for " + host, localhostRecords.size() > 0);
      assertTrue("There are records for 72.21.203.1", amazonRecords.size() == 0);
   }
   
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

      Map<String, List<Record>> allRecords = (Map<String, List<Record>>) server.invoke(oname, "getMatchingRecords", new Object[]{new RecordFilter[]{operationFilter}}, new String[]{filters.getClass().getName()});

      assertTrue("No records for hosts " + l + ", all records found: " + dumpInboundRecordsInfo(allRecords), stopRecords.size() > 0);
      assertEquals("There must be only 1 record for echo1 operation", 1, stopRecords.keySet().size() - startRecords.keySet().size());
   }

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
      URL wsdlURL = new URL(endpointURL + "?wsdl");
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
