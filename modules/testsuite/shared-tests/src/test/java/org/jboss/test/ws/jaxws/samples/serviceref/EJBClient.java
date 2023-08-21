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
package org.jboss.test.ws.jaxws.samples.serviceref;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPBinding;

import org.jboss.logging.Logger;

@Remote(EJBRemote.class)
@Stateless
public class EJBClient
{
   // Provide logging
   private static Logger log = Logger.getLogger(EJBClient.class);

   public String echo(String inStr) throws RemoteException
   {
      log.info("echo: " + inStr);

      List<Endpoint> ports = new ArrayList<Endpoint>(2);

      try
      {
         InitialContext iniCtx = new InitialContext();
         ports.add(((Service)iniCtx.lookup("java:comp/env/service1")).getPort(Endpoint.class));
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/service2")).getEndpointPort());
      }
      catch (Exception ex)
      {
        throw new WebServiceException(ex);
      }

      for (int i = 0; i < ports.size(); i++)
      {
         Endpoint port = ports.get(i);

         BindingProvider bp = (BindingProvider)port;
         @SuppressWarnings("unused")
         boolean mtomEnabled = ((SOAPBinding)bp.getBinding()).isMTOMEnabled();
         //boolean expectedSetting = (i==0) ? false : true;

         //if(mtomEnabled != expectedSetting)
         //   throw new WebServiceException("MTOM settings (enabled="+expectedSetting+") not overridden through service-ref" );

         String outStr = port.echo(inStr);
         if (inStr.equals(outStr) == false)
            throw new WebServiceException("Invalid echo return: " + inStr);
      }

      return inStr;
   }
}
