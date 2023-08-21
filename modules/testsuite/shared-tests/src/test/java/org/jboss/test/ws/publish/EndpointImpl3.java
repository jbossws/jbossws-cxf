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

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.ServiceMode;
import jakarta.xml.ws.WebServiceProvider;

import org.jboss.logging.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@WebServiceProvider(serviceName="EndpointService3",
      portName="EndpointPort3",
      targetNamespace = "http://publish.ws.test.jboss.org/",
      wsdlLocation = "./WEB-INF/wsdl/EndpointImpl3.xml")
@ServiceMode(value = Service.Mode.MESSAGE)
public class EndpointImpl3 implements Provider<SOAPMessage>
{
   // Provide logging
   private static Logger log = Logger.getLogger(EndpointImpl3.class);

   public SOAPMessage invoke(SOAPMessage request)
   {
      log.info("echo (3): " + request);
      try {
         SOAPBody sb = request.getSOAPBody();
         NodeList nl = sb.getElementsByTagName("arg0");
         if (nl.getLength() != 1) {
            throw new IllegalArgumentException("Unexpected input!");
         }
         Node ret = sb.getOwnerDocument().createElement("return");
         Node arg0 = nl.item(0);
         ret.appendChild(arg0.getFirstChild().cloneNode(true));
         Node parent = arg0.getParentNode();
         parent.removeChild(arg0);
         parent.appendChild(ret);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
      return request;
   }
}
