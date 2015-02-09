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
package org.jboss.test.ws.jaxws.samples.oneway;

import java.io.File;
import java.io.StringReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.ws.common.DOMWriter;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

/**
 * Test the JSR-181 annotation: javax.jws.Oneway
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
@RunWith(Arquillian.class)
public class OneWayTestCase extends JBossWSTest
{
   private static final String targetNS = "http://oneway.samples.jaxws.ws.test.jboss.org/";

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-oneway.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.oneway.PingEndpointImpl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/oneway/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testWebService() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/TestService?wsdl");
      QName serviceName = new QName(targetNS, "PingEndpointService");
      QName portName = new QName(targetNS, "PingEndpointPort");
      Service service = Service.create(wsdlURL, serviceName);
      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      
      String payload = "<ns1:ping xmlns:ns1='http://oneway.samples.jaxws.ws.test.jboss.org/'/>";
      dispatch.invokeOneWay(new StreamSource(new StringReader(payload)));

      //sleep 5 sec as invokeOneWay is supposed to be non-blocking subject to the capabilities of the underlying protocol
      Thread.sleep(5000);
 
      payload = "<ns1:feedback xmlns:ns1='http://oneway.samples.jaxws.ws.test.jboss.org/'/>";
      Source retObj = dispatch.invoke(new StreamSource(new StringReader(payload)));
      
      Element docElement = DOMUtils.sourceToElement(retObj);
      Element retElement = DOMUtils.getFirstChildElement(docElement);
      DOMWriter.printNode(retElement, false);
      assertEquals("return", retElement.getNodeName());
      assertEquals("result: ok", retElement.getTextContent());
   }
}
