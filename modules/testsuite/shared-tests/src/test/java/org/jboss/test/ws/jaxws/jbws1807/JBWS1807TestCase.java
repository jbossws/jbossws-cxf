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
package org.jboss.test.ws.jaxws.jbws1807;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.http.HTTPBinding;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

/**
 * HTTP bindings for Provider
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1807
 *
 * @author Thomas.Diesler@jboss.com
 * @since 09-Oct-2007
 */
@RunWith(Arquillian.class)
public class JBWS1807TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1807.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.helper.DOMWriter.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1807.ProviderImpl.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1807/WEB-INF/wsdl/provider.wsdl"), "wsdl/provider.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1807/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      Element wsdl = DOMUtils.parse(wsdlURL.openStream());
      assertNotNull(wsdl);
   }

   @Test
   @RunAsClient
   public void testProviderDispatch() throws Exception
   {
      String targetNS = "http://ws.com/";
      QName serviceName = new QName(targetNS, "Provider");
      QName portName = new QName(targetNS, "ProviderPort");

      Service service = Service.create(serviceName);
      service.addPort(portName, HTTPBinding.HTTP_BINDING, baseURL.toString());

      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      Source resPayload = dispatch.invoke(new DOMSource(DOMUtils.parse("<ns2:input xmlns:ns2='http://ws.com/'><arg0>hello</arg0></ns2:input>")));

      Element docElement = DOMUtils.sourceToElement(resPayload);
      Element response = ((Element)DOMUtils.getChildElements(docElement, "return").next());
      assertEquals("hello", response.getTextContent());
   }
}
