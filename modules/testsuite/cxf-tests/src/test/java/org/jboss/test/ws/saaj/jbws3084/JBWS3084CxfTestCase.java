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
package org.jboss.test.ws.saaj.jbws3084;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.stack.cxf.saaj.SOAPConnectionFactoryImpl;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3084] Enable control of chunked encoding when using SOAPConnection.
 *
 * @author sberyozk@redhat.com
 */
@RunWith(Arquillian.class)
public class JBWS3084CxfTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "cxf-saaj-soap-connection.war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.saaj.jbws3084.InputStreamDataSource.class)
            .addClass(org.jboss.test.ws.saaj.jbws3084.ServiceIface.class)
            .addClass(org.jboss.test.ws.saaj.jbws3084.ServiceImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/saaj/jbws3084/WEB-INF/wsdl/SaajService.wsdl"), "wsdl/SaajService.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/saaj/jbws3084/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testSoapConnectionFactoryType() throws Exception
   {
      SOAPConnectionFactory conFac = SOAPConnectionFactory.newInstance();
      assertEquals(SOAPConnectionFactoryImpl.class.getName(), conFac.getClass().getName());
   }

   @Test
   @RunAsClient
   public void testSoapConnectionGet() throws Exception
   {
      final String serviceURL = baseURL + "/greetMe";
      SOAPConnectionFactory conFac = SOAPConnectionFactory.newInstance();

      SOAPConnection con = conFac.createConnection();
      URL endpoint = new URL(serviceURL);
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage msg = msgFactory.createMessage();
      msg.getSOAPBody().addBodyElement(new QName("http://www.jboss.org/jbossws/saaj", "greetMe"));
      SOAPMessage response = con.call(msg, endpoint);
      QName greetMeResp = new QName("http://www.jboss.org/jbossws/saaj", "greetMeResponse");

      Iterator<?> sayHiRespIterator = response.getSOAPBody().getChildElements(greetMeResp);
      SOAPElement soapElement = (SOAPElement) sayHiRespIterator.next();
      assertNotNull(soapElement);

      assertEquals(1, response.countAttachments());
   }
}
