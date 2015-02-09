/*
* JBoss, Home of Professional Open Source.
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2259;

import java.io.File;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test case to test MTOM detection.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 30th March 2009
 * @see https://jira.jboss.org/jira/browse/JBWS-2259
 */
@RunWith(Arquillian.class)
public class JBWS2259TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2259.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2259.CustomHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2259.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2259.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2259.Photo.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2259/META-INF/permissions.xml"), "permissions.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2259/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2259/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testCall() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      QName serviceName = new QName("http://ws.jboss.org/jbws2259", "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      BindingProvider bindingProvider = (BindingProvider)port;
      SOAPBinding soapBinding = (SOAPBinding)bindingProvider.getBinding();
      soapBinding.setMTOMEnabled(true);
      
      File sharkFile = getResourceFile("jaxws/jbws2259/attach.jpeg");
      DataSource ds = new FileDataSource(sharkFile);
      DataHandler handler = new DataHandler(ds);

      String expectedContentType = "image/jpeg";

      Photo p = new Photo();
      p.setCaption("JBWS2259 Smile :-)");
      p.setExpectedContentType(expectedContentType);
      p.setImage(handler);

      Photo reponse = port.echo(p);
      DataHandler dhResponse = reponse.getImage();

      String contentType = dhResponse.getContentType();
      assertEquals("content-type", expectedContentType, contentType);
   }

}
