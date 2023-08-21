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
package org.jboss.test.ws.jaxws.endpoint;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Tests Endpoint deployment in J2EE environment.
 *
 * @author <a href="mailto:tdiesler@redhat.com">Thomas Diesler</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@SuppressWarnings("serial")
public class EndpointServlet extends HttpServlet
{
   private Endpoint endpoint1;
   private Endpoint endpoint2;
   private String hostName;
   private static final String TEST_ELEMENT = "<fabrikam:CustomerKey xmlns:fabrikam='http://example.com/fabrikam'>123456789</fabrikam:CustomerKey>";
   
   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      
      endpoint1 = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, new EndpointBean());
      hostName = System.getProperty("jboss.bind.address", "localhost");
      hostName = (!hostName.startsWith("[") && hostName.indexOf(":") != -1) ? "[" + hostName + "]" : hostName;
      endpoint1.publish("http://" + hostName + ":8082/jaxws-endpoint");
      endpoint2 = Endpoint.publish("http://" + hostName + ":8082/jaxws-endpoint2/endpoint/long/path", new EndpointBean());
   }
   
   @Override
   public void destroy()
   {
      endpoint1.stop();
      endpoint2.stop();
      
      super.destroy();
   }

   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      // Create the port
//    URL wsdlURL = getServletContext().getResource("/WEB-INF/wsdl/TestService.wsdl");
      URL wsdlURL = new URL("http://" + hostName + ":8082/jaxws-endpoint?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);

      // Invoke the endpoint
      String param = req.getParameter("param");
      String retStr = port.echo(param);
      
      //Test epr
      DocumentBuilder builder = getDocumentBuilder();
      assertEndpointReference(endpoint1.getEndpointReference(DOMUtils.parse(TEST_ELEMENT, builder)), TEST_ELEMENT, builder);
      assertEndpointReference(endpoint1.getEndpointReference(W3CEndpointReference.class, (Element[])null), null, builder);

      // Return the result
      PrintWriter pw = new PrintWriter(res.getWriter());
      pw.print(retStr);
      pw.close();
   }
   
   private void assertEndpointReference(EndpointReference epr, String refPar, DocumentBuilder builder) throws IOException
   {
      Logger.getLogger(this.getClass()).info("epr: "+epr);
      assert(W3CEndpointReference.class.getName().equals(epr.getClass().getName()));
      Element endpointReference = DOMUtils.parse(epr.toString(), builder);
      assert("EndpointReference".equals(endpointReference.getNodeName()));
      assert("http://www.w3.org/2005/08/addressing".equals(endpointReference.getAttribute("xmlns")));
      NodeList addresses = endpointReference.getElementsByTagName("Address");
      assert(addresses.getLength() == 1);
      assert(("http://" + hostName + ":8082/jaxws-endpoint").equals(addresses.item(0).getFirstChild().getNodeValue()));
      if (refPar != null)
      {
    	 Element refEle = DOMUtils.parse(refPar, builder);
    	 NodeList nodeList = endpointReference.getElementsByTagNameNS(refEle.getNamespaceURI(), refEle.getLocalName());
    	 assert(nodeList.getLength() == 1);
    	 assert(refEle.getTextContent().equals(nodeList.item(0).getTextContent()));   	 
      }
   }
   
   private DocumentBuilder getDocumentBuilder()
   {
      DocumentBuilderFactory factory = null;
      try
      {
         factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setNamespaceAware(true);
         factory.setExpandEntityReferences(false);
         factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         return builder;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to create document builder", e);
      }
   }
}
