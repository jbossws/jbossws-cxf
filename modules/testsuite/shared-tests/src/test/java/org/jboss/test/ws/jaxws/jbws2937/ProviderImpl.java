/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
