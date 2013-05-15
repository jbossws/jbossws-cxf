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

import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Assert;
import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

public class JBWS3250TestCase extends JBossWSTest
{
   private String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws3250";

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS3250TestCase.class, "jaxws-jbws3250.war");
   }

   public void testMtomSawpFile() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
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
