/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3250;

import java.io.File;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.junit.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JBWS3250TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3250.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3250.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3250.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3250.MTOMRequest.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3250.MTOMResponse.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3250/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testMtomSawpFile() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      QName serviceName = new QName("http://ws.jboss.org/jbws3250", "TestEndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);
      SOAPBinding binding =(SOAPBinding)((BindingProvider)port).getBinding();
      binding.setMTOMEnabled(true);
      URL url = JBossWSTestHelper.getResourceURL("jaxws/jbws3250/wsf.png");
      URLDataSource urlDatasource = new URLDataSource(url);
      javax.activation.DataHandler dh = new DataHandler(urlDatasource);
      MTOMRequest request = new MTOMRequest();
      request.setContent(dh);
      request.setId("largeSize_mtom_request");
      MTOMResponse mtomResponse = port.echo(request);
      Assert.assertEquals("Response for requestID:largeSize_mtom_request", mtomResponse.getResponse());
      byte[] responseBytes = IOUtils.convertToBytes(mtomResponse.getContent());
      Assert.assertTrue(responseBytes.length > 65536);
   }

}
