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
package org.jboss.test.ws.jaxws.samples.provider;

import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.ServiceMode;
import jakarta.xml.ws.WebServiceProvider;

import org.w3c.dom.NodeList;

/**
 * Test a Provider<SOAPMessage>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Jun-2006
 */
@WebServiceProvider(
      serviceName = "ProviderService",
      portName = "ProviderPort",
      targetNamespace = "http://org.jboss.ws/provider",
      wsdlLocation = "WEB-INF/wsdl/Provider.wsdl")
@ServiceMode(value = Service.Mode.MESSAGE)
public class ProviderBeanMessage implements Provider<SOAPMessage>
{
   public SOAPMessage invoke(SOAPMessage request)
   {
      try
      {
         SOAPHeader headers = request.getSOAPHeader();
         if (headers != null)
         {
            NodeList nl = headers.getElementsByTagNameNS("http://org.jboss.ws/foo", "returnNullResponse");
            if (nl != null && nl.getLength() > 0)
            {
               return null;
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      return request;
   }
}
