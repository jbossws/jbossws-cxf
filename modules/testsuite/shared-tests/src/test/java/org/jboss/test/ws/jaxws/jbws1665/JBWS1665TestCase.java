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
package org.jboss.test.ws.jaxws.jbws1665;

import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1665] incorrect wsdl generation
 *
 * http://jira.jboss.org/jira/browse/JBWS-1665
 */
@RunWith(Arquillian.class)
public class JBWS1665TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1665.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1665.CoordinateData.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1665.JBWS1665TestCase.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1665.PropertyData.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1665.TraceData.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1665.TracePollData.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1665.TrackingServiceBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1665.TrackingServiceInterface.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testWebService() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1665/TrackingService?wsdl");
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(wsdlURL.toString());
      assertNotNull(wsdlDefinition);
   }

}
