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
package org.jboss.test.ws.publish;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.ws.common.utils.AddressUtils;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.metadata.webservices.PortComponentMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebserviceDescriptionMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;
import org.jboss.wsf.spi.publish.Context;
import org.jboss.wsf.spi.publish.EndpointPublisher;
import org.jboss.wsf.spi.publish.EndpointPublisherFactory;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 01-Apr-2011
 *
 */
@WebServlet(name = "EndpointPublishServlet", urlPatterns = "/*")
public class EndpointPublishServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      Context ctx = null;
      EndpointPublisher publisher = null;
      try
      {
         //deploy endpoints
         ClassLoader loader = ClassLoaderProvider.getDefaultProvider().getWebServiceSubsystemClassLoader();
         EndpointPublisherFactory factory = ServiceLoader.load(EndpointPublisherFactory.class, loader).iterator().next();
         publisher = factory.newEndpointPublisher("default-host");
         
         Map<String,String> map = new HashMap<String, String>();
         map.put("/pattern", "org.jboss.test.ws.publish.EndpointImpl");
         map.put("/pattern2", "org.jboss.test.ws.publish.EndpointImpl2");
         map.put("/pattern3", "org.jboss.test.ws.publish.EndpointImpl3");
         map.put("/pattern4", "org.jboss.test.ws.publish.EndpointImpl4");
         map.put("/pattern5", "org.jboss.test.ws.publish.EndpointImpl5");
         
         ctx = publisher.publish("ep-publish-test", Thread.currentThread().getContextClassLoader(), map, createMetaData());
         
         for (Endpoint ep : ctx.getEndpoints()) {
            System.out.println("State: " + ep.getState());
            System.out.println("Address: " + ep.getAddress());
            System.out.println("TargetBeanClass: " + ep.getTargetBeanClass());
         }
         
         //call endpoint
         String jbossBindAddress = toIPv6URLFormat(System.getProperty("jboss.bind.address", "localhost"));
         int port = req.getLocalPort();
         invoke(new URL("http://" + jbossBindAddress + ":" + port + "/ep-publish-test/pattern?wsdl"), new QName("http://publish.ws.test.jboss.org/", "EndpointService"));
         invoke(new URL("http://" + jbossBindAddress + ":" + port + "/ep-publish-test/pattern2?wsdl"), new QName("http://publish.ws.test.jboss.org/", "EndpointService2"));
         invoke(new URL("http://" + jbossBindAddress + ":" + port + "/ep-publish-test/pattern3?wsdl"), new QName("http://publish.ws.test.jboss.org/", "EndpointService3"));
         invoke(new URL("http://" + jbossBindAddress + ":" + port + "/ep-publish-test/pattern4?wsdl"), new QName("http://publish.ws.test.jboss.org/", "EndpointService4"));
         invoke(new URL("http://" + jbossBindAddress + ":" + port + "/ep-publish-test/pattern5?wsdl"), new QName("http://publish.ws.test.jboss.org/", "EndpointService5"));
         
         res.getWriter().print("1");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         res.getWriter().print(e.getMessage());
      }
      finally
      {
         if (ctx != null && publisher != null)
         {
            try
            {
               //undeploy endpoints
               publisher.destroy(ctx);
            }
            catch (Exception e)
            {
               e.printStackTrace();
               res.getWriter().print(e.getMessage());
            }
         }
      }
   }
   
   private static String toIPv6URLFormat(final String host)
   {
      boolean isIPv6URLFormatted = false;
      if (host.startsWith("[") && host.endsWith("]")) {
         isIPv6URLFormatted = true;
      }
      //return IPv6 URL formatted address
      if (isIPv6URLFormatted) {
         return host;
      } else {
         return AddressUtils.isValidIPv6Address(host) ? "[" + host + "]" : host;
      }
   }

   // See https://issues.jboss.org/browse/JBWS-3579
   //
   // There's one magic think in endpoint publish when using WebservicesMetaData.
   // Every URL pattern is transformed to link name using the following algorithm.
   // Note that in order to match WebservicesMetaData with particular endpoint,
   // either ejb-link or servlet-link have to be provided.
   // +-------------------------+----------------+----------------------+-------------------------------------------------------------------+
   // |      pattern URL        | endpoint class | generated link name  | comment                                                           |
   // +-------------------------+----------------+----------------------+-------------------------------------------------------------------+
   // | /*                      | foo.Bar        | foo.Bar              | used class name if pattern is wildcard                            |
   // | /some/pattern           | foo.Bar        | some.pattern         | used pattern url with dots instead of / and removed all wildcards |
   // | /some/complex/pattern/* | foo.Bar        | some.complex.pattern | used pattern url with dots instead of / and removed all wildcards |
   // +-------------------------+----------------+----------------------+-------------------------------------------------------------------+
   private WebservicesMetaData createMetaData() {
      PortComponentMetaData portComponent = new PortComponentMetaData("PortComponent4", //unique ID
            new QName("http://publish.ws.test.jboss.org/","EndpointPort4"),
            "org.jboss.test.ws.publish.EndpointImpl4",
            null, // if endpoint ^ was an EJB, user would have to use this parameter to set ejbLink
            "pattern4", // mandatory servlet link (because endpoint is POJO) - needed for proper matching of endpoint with WebservicesMD
            null, null,
            new QName("http://publish.ws.test.jboss.org/", "EndpointService4"),
            null, null);
      WebserviceDescriptionMetaData webserviceDescription = new WebserviceDescriptionMetaData(null, "WEB-INF/wsdl/EndpointImpl4.xml", null, portComponent);
      
      PortComponentMetaData portComponent2 = new PortComponentMetaData("PortComponent5", //unique ID
            new QName("http://publish.ws.test.jboss.org/","EndpointPort5"),
            "org.jboss.test.ws.publish.EndpointImpl5",
            null, // if endpoint ^ was an EJB, user would have to use this parameter to set ejbLink
            "pattern5", // mandatory servlet link (because endpoint is POJO) - needed for proper matching of endpoint with WebservicesMD
            null, null,
            new QName("http://publish.ws.test.jboss.org/", "EndpointService5"),
            null, null);
      WebserviceDescriptionMetaData webserviceDescription2 = new WebserviceDescriptionMetaData(null, "org/jboss/test/ws/publish/EndpointImpl5.xml", //test JBWS-3540
            null, portComponent2);
      
      WebservicesMetaData metadata = new WebservicesMetaData(null, new WebserviceDescriptionMetaData[]{webserviceDescription, webserviceDescription2});
      return metadata;
   }

   private static void invoke(URL wsdlURL, QName serviceName) throws Exception {
      Service service = Service.create(wsdlURL, serviceName);
      org.jboss.test.ws.publish.Endpoint port = service.getPort(org.jboss.test.ws.publish.Endpoint.class);
      String result = port.echo("Foo");
      if (!"Foo".equals(result))
      {
         throw new Exception("Expected 'Foo' but got '" + result + "'");
      }
   }
}
