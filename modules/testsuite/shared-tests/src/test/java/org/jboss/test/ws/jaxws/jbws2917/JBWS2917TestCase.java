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
