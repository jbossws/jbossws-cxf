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
package org.jboss.test.ws.jaxws.samples.httpbinding;

import org.jboss.ws.api.util.DOMUtils;
import org.w3c.dom.Element;

import jakarta.jws.HandlerChain;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.*;
import jakarta.xml.ws.http.HTTPBinding;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Test a Provider<Source>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Jun-2006
 */

@WebServiceProvider(serviceName = "ProviderService", portName = "ProviderPort", targetNamespace = "http://org.jboss.ws/httpbinding", wsdlLocation = "WEB-INF/wsdl/HttpBinding.wsdl")

@BindingType(value = HTTPBinding.HTTP_BINDING)
@ServiceMode(value = Service.Mode.MESSAGE)
@HandlerChain(file = "httpbinding-handlers.xml")
public class ProviderBeanPayload implements Provider<Source>
{
   public Source invoke(Source req)
   {
      try
      {
         Transformer transformer = TransformerFactory.newInstance().newTransformer();
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         transformer.setOutputProperty(OutputKeys.METHOD, "xml");
         OutputStream out = new ByteArrayOutputStream();
         StreamResult streamResult = new StreamResult();
         streamResult.setOutputStream(out);
         transformer.transform(req, streamResult);
         String xmlReq = streamResult.getOutputStream().toString();

         verifyRequest(xmlReq);

         return new StreamSource(new ByteArrayInputStream(xmlReq.getBytes()));
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new WebServiceException(e);
      }
   }

   private void verifyRequest(String xml) throws IOException
   {
      Element was = DOMUtils.parse(xml, getDocumentBuilder());

      if(!"somePayload".equals(was.getLocalName())
        || !"http://org.jboss.ws/httpbinding".equals(was.getNamespaceURI())
        || !"Hello:InboundLogicalHandler".equals( DOMUtils.getTextContent(was)))
      {
         throw new WebServiceException("Unexpected payload: " + xml);
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
