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
package org.jboss.test.ws.jaxws.samples.mtom;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.apache.cxf.helpers.IOUtils;
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
import org.w3c.dom.NodeList;
/**
 * Client invoking web service using MTOM
 *
 */
@RunWith(Arquillian.class)
public final class MtomTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDep() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-mtom.war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.jaxws.samples.mtom.ServiceIface.class)
            .addClass(org.jboss.test.ws.jaxws.samples.mtom.ServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.mtom.jaxws.SayHello.class)
            .addClass(org.jboss.test.ws.jaxws.samples.mtom.jaxws.SayHelloResponse.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/mtom/WEB-INF/wsdl/MtomService.wsdl"), "wsdl/MtomService.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/mtom/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testMtomWithProxy() throws Exception
   {
      // construct proxy
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/mtom", "MtomService");
      URL wsdlURL = new URL(baseURL + "/jaxws-samples-mtom/MtomService" + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      // invoke method
      assertEquals("Hello World!", proxy.sayHello());
   }

   @Test
   @RunAsClient
   public void testMtomWithoutProxy() throws Exception
   {
      final String mtomPayload = "--uuid:b7a481a7-274a-42ed-8b84-9bb2280fb2e7\r\n"
                                 + "Content-Type: application/xop+xml; charset=UTF-8; type=\"text/xml\"\r\n"
                                 + "Content-Transfer-Encoding: binary\r\n"
                                 + "Content-ID: <root.message@cxf.apache.org>\r\n\r\n"
                                 + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>"
                                 + "<ns2:sayHello xmlns:ns2=\"http://www.jboss.org/jbossws/ws-extensions/mtom\" "
                                 + "xmlns:ns3=\"http://www.jboss.org/jbossws/ws-extensions/wsaddressing\"/></soap:Body></soap:Envelope>\r\n"
                                 + "--uuid:b7a481a7-274a-42ed-8b84-9bb2280fb2e7--";

      HttpURLConnection conn = (HttpURLConnection)new URL(baseURL + "/jaxws-samples-mtom/MtomService").openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("POST"); 
      conn.setRequestProperty("Content-Type", 
                              "multipart/related; type=\"application/xop+xml\"; boundary=\"uuid:b7a481a7-274a-42ed-8b84-9bb2280fb2e7\";" 
                              + " start=\"<root.message@cxf.apache.org>\"; start-info=\"text/xml\"");
      conn.setRequestProperty("Content-Length", Integer.toString(mtomPayload.length()));
      OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
      out.write(mtomPayload);
      out.flush();
      out.close();

      assertEquals(200, conn.getResponseCode());
      final InputStream is = conn.getInputStream();
      try {
         String response = IOUtils.readStringFromStream(is);
   
         assertTrue(response.contains("--uuid"));
         assertTrue(response.contains("<return>Hello World!</return>"));
      } finally {
         is.close();
      }
   }

   @Test
   @RunAsClient
   public void testMtomNotUsed() throws Exception
   {
      final String envelope = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>"
                              + "<ns2:sayHello xmlns:ns2=\"http://www.jboss.org/jbossws/ws-extensions/mtom\" "
                              + "xmlns:ns3=\"http://www.jboss.org/jbossws/ws-extensions/wsaddressing\"/></soap:Body></soap:Envelope>";

      HttpURLConnection conn = (HttpURLConnection)new URL(baseURL + "/jaxws-samples-mtom/MtomService").openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("POST"); 
      conn.setRequestProperty("Content-Type", "text/xml");
      conn.setRequestProperty("Content-Length", Integer.toString(envelope.length()));
      OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
      out.write(envelope);
      out.flush();
      out.close();

      assertEquals(500, conn.getResponseCode());
      Element responseEl = DOMUtils.parse(conn.getErrorStream());

      NodeList list = responseEl.getElementsByTagName("faultstring");
      String text = list.item(0).getTextContent();
      assertTrue(text.contains("These policy alternatives can not be satisfied: "));
      assertTrue(text.contains("{http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization}"
                    + "OptimizedMimeSerialization"));
   }   
}
