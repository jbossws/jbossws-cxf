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

import java.net.URL;

import jakarta.jws.WebService;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;

/**
 * An endpoint that publishes and invokes another endpoint using Endpoint.publish(..) API
 * 
 * @author alessio.soldano@jboss.com
 * @since 26-Aug-2010
 *
 */
@WebService(endpointInterface = "org.jboss.test.ws.jaxws.endpoint.WSClientEndpointInterface", targetNamespace = "http://org.jboss.ws/jaxws/endpoint", serviceName = "WSClientEndpointService")
public class WSClientEndpointBean implements WSClientEndpointInterface
{
   public String echo(String input, String serverHost, int port)
   {
      Endpoint ep = Endpoint.create(new EndpointBean());
      String publishUrl = "http://" + serverHost + ":" + port + "/foo/bar";
      ep.publish(publishUrl);

      try
      {
         QName qname = new QName("http://org.jboss.ws/jaxws/endpoint", "EndpointService");
         Service service = Service.create(new URL(publishUrl + "?wsdl"), qname);
         EndpointInterface proxy = (EndpointInterface) service.getPort(EndpointInterface.class);
         return proxy.echo(input);
      }
      catch (Exception e)
      {
         throw new WebServiceException(e);
      }
      finally
      {
         if (ep != null)
         {
            ep.stop();
         }
      }
   }
   
   public String echo2(String input)
   {
      Endpoint ep = Endpoint.create(new EndpointBean());
      if (ep.getBinding() == null || ep.getImplementor() == null)
      {
         throw new WebServiceException("null binding or implementor!");
      }
      return input;
   }
}
