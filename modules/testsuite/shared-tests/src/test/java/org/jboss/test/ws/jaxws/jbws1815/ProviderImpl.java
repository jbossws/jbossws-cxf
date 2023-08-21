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
