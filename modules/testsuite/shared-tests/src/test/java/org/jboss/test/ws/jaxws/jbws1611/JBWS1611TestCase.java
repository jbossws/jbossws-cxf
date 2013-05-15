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
package org.jboss.test.ws.jaxws.jbws1611;

import java.io.StringReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import junit.framework.Test;

import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.w3c.dom.Element;

/**
 * [JBWS-1611] SOAPAction is not sent in dispatch requests
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1611 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Jun-2007
 */
public class JBWS1611TestCase extends JBossWSTest
{
   private static final String targetNS = "http://jbws1611.jaxws.ws.test.jboss.org/";

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1611TestCase.class, "jaxws-jbws1611.war");
   }

   public void testWebService() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1611?wsdl");
      QName serviceName = new QName(targetNS, "PingEndpointService");
      QName portName = new QName(targetNS, "PingEndpointPort");
      Service service = Service.create(wsdlURL, serviceName);
      Dispatch dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);

      dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
      dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "uri:placeBuyOrder");
      
      String payload = "<ns1:ping xmlns:ns1='" + targetNS + "'/>";
      Source retObj = (Source)dispatch.invoke(new StreamSource(new StringReader(payload)));
      
      Element docElement = DOMUtils.sourceToElement(retObj);
      Element retElement = DOMUtils.getFirstChildElement(docElement);
      assertEquals("return", retElement.getLocalName());
      assertEquals("\"uri:placeBuyOrder\"", retElement.getFirstChild().getNodeValue());
   }
}
