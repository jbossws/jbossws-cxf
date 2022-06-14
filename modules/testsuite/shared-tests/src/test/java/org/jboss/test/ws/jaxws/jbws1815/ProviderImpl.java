/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws1815;

import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import javax.xml.namespace.QName;
import jakarta.xml.soap.Detail;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.ServiceMode;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.jboss.ws.api.annotation.WebContext;

/**
 * Test impl for http://jira.jboss.org/jira/browse/JBWS-1815
 *
 * @author alessio.soldano@jboss.com
 * @since 11-Oct-2007
 */
@Local
@Stateless
@WebServiceProvider(serviceName = "MyTestService",
                    portName = "MyTestPort",
                    targetNamespace = "http://www.my-company.it/ws/my-test",
                    wsdlLocation = "META-INF/wsdl/my-service.wsdl")
@WebContext(contextRoot = "/jaxws-jbws1815")
@ServiceMode(value = Service.Mode.MESSAGE)
public class ProviderImpl implements Provider<SOAPMessage>
{

   public SOAPMessage invoke(SOAPMessage requestSoapMessage)
   {
      SOAPFault theSOAPFault;
      try {
         theSOAPFault = SOAPFactory.newInstance().createFault();
         Detail soapFaultDetail = theSOAPFault.addDetail();
         SOAPElement myFaultElement = soapFaultDetail.addChildElement(new QName("http://www.my-company.it/ws/my-test", "MyWSException"));
         SOAPElement myMessageElement = myFaultElement.addChildElement(new QName("http://www.my-company.it/ws/my-test", "message"));
//         myMessageElement.setNodeValue("This is a faked error"); //wrong: myMessageElement is not a text node
         myMessageElement.setValue("This is a faked error"); //right: this creates a text node and gives it a text value
      } catch (SOAPException se) {
         se.printStackTrace();
         throw new RuntimeException("Something unexpected happened!");
      }
      throw new SOAPFaultException(theSOAPFault);
   }
}
