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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jakarta.activation.DataHandler;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.soap.SOAPBinding;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test service endpoint capability to process inlined and optimized
 * requests transparently. Both client and service endpoint do have handlers in place.
 * This means that an additional transition to a conceptually inlined message (handler view)
 * will happen as well.
 *
 * <ul>
 * <li>Client and service endpoint have MTOM enabled (roundtrip)
 * <li>Client send inlined requests (MTOM disabled), service answers with an optimized response.
 * </ul>
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since 05.12.2006
 */
@Ignore(value="[JBWS-2561] XOP request not properly inlined")
@RunWith(Arquillian.class)
public class XOPHandlerTestCase extends XOPBase
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      return DeploymentArchive.createDeployment("handler");
   }

   @Before
   public void setup() throws Exception
   {
      QName serviceName = new QName("http://doclit.xop.samples.jaxws.ws.test.jboss.org/", "MTOMService");
      URL wsdlURL = new URL(baseURL + "bare?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      port = service.getPort(MTOMEndpoint.class);
      binding = (SOAPBinding)((BindingProvider)port).getBinding();

      @SuppressWarnings("rawtypes")
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.addAll(binding.getHandlerChain());
      handlerChain.add(new MTOMProtocolHandler());
      binding.setHandlerChain(handlerChain);
   }

    /**
    * Consumption of inlined data should will always result on 'application/octet-stream'
    * @throws Exception
    */
    @Test
    @RunAsClient
   public void testDataHandlerRoundtrip() throws Exception
   {
      getBinding().setMTOMEnabled(true);
      DataHandler dh = new DataHandler("Client Data", "text/plain");
      DHResponse response = getPort().echoDataHandler(new DHRequest(dh));
      assertNotNull(response);
      assertEquals("application/octet-stream", response.getDataHandler().getContentType());
      assertTrue("Wrong java type returned", response.getDataHandler().getContent() instanceof InputStream);
   }

   /**
    * Consumption of inlined data should will always result on 'application/octet-stream'
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testDataHandlerResponseOptimzed() throws Exception
   {
      getBinding().setMTOMEnabled(false);

      DataHandler dh = new DataHandler("Client data", "text/plain");
      DHResponse response = getPort().echoDataHandler(new DHRequest(dh));
      assertNotNull(response);
      assertEquals("application/octet-stream", response.getDataHandler().getContentType());
      assertTrue("Wrong java type returned", response.getDataHandler().getContent() instanceof InputStream);
   }
}
