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

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test case to test MTOM detection.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 30th March 2009
 * @see https://jira.jboss.org/jira/browse/JBWS-2259
 */
public class JBWS2259TestCase extends JBossWSTest
{

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2259TestCase.class, "jaxws-jbws2259.war");
   }

   public void testCall() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2259?wsdl");
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
