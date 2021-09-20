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
package org.jboss.test.ws.jaxws.jbws3293;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.AsyncHandler;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Response;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.soap.SOAPBinding;

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
 * [JBWS-3293] tests dispatch creation without WSDL.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class JBWS3293DispatchTestCase extends JBossWSTest
{
   private final String targetNS = "http://org.jboss.ws/jaxws/jbws3293";
   private final String reqPayload = "<ns2:echo xmlns:ns2='" + targetNS + "'><String_1>Hello</String_1></ns2:echo>";
   private Exception handlerException;
   private boolean asyncHandlerCalled;

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3293.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3293.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3293.EndpointBean.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3293/WEB-INF/web.xml"));
      return archive;
   }

  @Test
  @RunAsClient
   public void testInvokeAsynch() throws Exception
   {
      Source reqObj = new StreamSource(new StringReader(reqPayload));
      Dispatch<Source> dispatch = createDispatch();
      Response<Source> response = dispatch.invokeAsync(reqObj);
      verifyResponse(response.get(3000, TimeUnit.MILLISECONDS));
   }

   @Test
   @RunAsClient
   public void testInvokeAsynchHandler() throws Exception
   {
      AsyncHandler<Source> handler = new AsyncHandler<Source>()
      {
         @Override
         public void handleResponse(Response<Source> response)
         {
            try
            {
               verifyResponse(response.get());
               asyncHandlerCalled = true;
            }
            catch (Exception ex)
            {
               handlerException = ex;
            }
         }
      };
      StreamSource reqObj = new StreamSource(new StringReader(reqPayload));
      Dispatch<Source> dispatch = createDispatch();
      Future<?> future = dispatch.invokeAsync(reqObj, handler);
      future.get(3000, TimeUnit.MILLISECONDS);

      if (handlerException != null)
         throw handlerException;

      assertTrue("Async handler called", asyncHandlerCalled);
   }

   private void installHandler(final Dispatch<Source> dispatch)
   {
       @SuppressWarnings("rawtypes")
      List<Handler> handlers = dispatch.getBinding().getHandlerChain();
       handlers.add(new SOAPHandler());
       dispatch.getBinding().setHandlerChain(handlers);
   }

   private Dispatch<Source> createDispatch() throws MalformedURLException
   {
      QName serviceName = new QName(targetNS, "EndpointBeanService");
      QName portName = new QName(targetNS, "EndpointPort");
      Service service = Service.create(serviceName);
      service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, baseURL.toString());
      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      installHandler(dispatch);
      return dispatch;
   }

   private void verifyResponse(Source result) throws IOException
   {
      Element docElement = DOMUtils.sourceToElement(result);
      Element retElement = DOMUtils.getFirstChildElement(docElement);
      assertEquals("result", retElement.getNodeName());
      assertEquals("Hello modified in handler", retElement.getTextContent());
   }

}
