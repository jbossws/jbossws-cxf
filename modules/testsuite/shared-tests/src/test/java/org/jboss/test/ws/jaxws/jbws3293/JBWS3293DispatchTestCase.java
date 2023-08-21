/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
