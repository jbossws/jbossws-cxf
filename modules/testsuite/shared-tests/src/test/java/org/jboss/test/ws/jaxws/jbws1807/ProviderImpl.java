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
package org.jboss.test.ws.jaxws.jbws1807;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.ServiceMode;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.xml.ws.http.HTTPBinding;

import org.jboss.logging.Logger;
import org.jboss.test.helper.DOMWriter;
import org.jboss.ws.api.util.DOMUtils;
import org.w3c.dom.Element;

@WebServiceProvider(wsdlLocation = "WEB-INF/wsdl/provider.wsdl", portName = "ProviderPort", serviceName = "ProviderService", targetNamespace = "http://ws.com/")
@ServiceMode(value = Service.Mode.PAYLOAD)
@BindingType(value = HTTPBinding.HTTP_BINDING)
public class ProviderImpl implements Provider<Source>
{
   // provide logging
   private final static Logger log = Logger.getLogger(ProviderImpl.class);

   public Source invoke(Source source)
   {
      try
      {
         Element elem = DOMUtils.sourceToElement(source, getDocumentBuilder());
         String value = DOMUtils.getChildElements(elem, "arg0", true).next().getTextContent();
         String input = DOMWriter.printNode(elem, false);
         log.info("invoke: " + input);
         
         String reply = "<myns:reply xmlns:myns='http://ws.com/'><return>" + value + "</return></myns:reply>";
         return new StreamSource(new ByteArrayInputStream(reply.getBytes()));
      }
      catch (IOException ex)
      {
         throw new RuntimeException(ex);
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
