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
package org.jboss.test.ws.jaxws.jbws2937;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.ServiceMode;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.xml.ws.http.HTTPBinding;

/**
 * JAXB based provider.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@WebServiceProvider
(
   serviceName = "ProviderService", 
   portName = "ProviderPort", 
   targetNamespace = "http://jboss.org/jbws2937", 
   wsdlLocation = "WEB-INF/wsdl/Endpoint.wsdl"
)
@BindingType(value = HTTPBinding.HTTP_BINDING)
@ServiceMode(value = Service.Mode.MESSAGE)
public class ProviderImpl implements Provider<Source>
{
   public Source invoke(final Source request)
   {
      try
      {
         JAXBContext jc = JAXBContext.newInstance(UserType.class);
         UserType user = (UserType)jc.createUnmarshaller().unmarshal(request);
         System.out.println("[string=" + user.getString() + ",qname=" + user.getQname() + "]");
         return new JAXBSource(jc, user);
      }
      catch (JAXBException e)
      {
         throw new WebServiceException(e);
      }
   }
}
