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
package org.jboss.test.ws.jaxws.jbws2917;

import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.EndpointReference;

import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
/**
 * [JBWS-2917] We're using buggy xalan version causing namespaces issues
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public final class JBWS2917TestCase extends JBossWSTest
{
   @Test
   @RunAsClient
   public void testToString() throws Exception
   {
      String XML_SOURCE = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n" +
         "<EndpointReference xmlns='http://www.w3.org/2005/08/addressing'>\n" +
         "  <Address>http://localhost:8080/jaxws-endpointReference</Address>\n" +
         "  <Metadata\n" + 
         "    wsdli:wsdlLocation='http://org.jboss.ws/endpointReference http://localhost:8080/jaxws-endpointReference?wsdl'\n" +
         "    xmlns:wsdli='http://www.w3.org/ns/wsdl-instance'>\n" +
         "    <wsam:InterfaceName xmlns:wsam='http://www.w3.org/2005/08/addressing' xmlns:wsns='http://org.jboss.ws/endpointReference'>wsns:Endpoint</wsam:InterfaceName>\n" +
         "    <wsam:ServiceName xmlns:wsam='http://www.w3.org/2005/08/addressing' xmlns:wsns='http://org.jboss.ws/endpointReference' xmlns='' EndpointName='HelloPort'>wsns:EndpointService</wsam:ServiceName>\n" +
         "  </Metadata>\n" +
         "</EndpointReference>\n";
      
      assertTrue("lost xmlns:wsns namespace declaration", this.getXML(XML_SOURCE).indexOf("xmlns:wsns") != -1);
      StreamSource source = new StreamSource(new StringReader(XML_SOURCE));
      EndpointReference epRef = EndpointReference.readFrom(source);
      assertTrue("lost xmlns:wsns namespace declaration", epRef.toString().indexOf("xmlns:wsns") != -1);
   }
   
   private String getXML(final String s) throws Exception
   {
      return DOMUtils.node2String(DOMUtils.parse(s));
   }
}
