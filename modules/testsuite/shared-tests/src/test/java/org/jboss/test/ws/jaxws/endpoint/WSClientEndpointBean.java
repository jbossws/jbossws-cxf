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
package org.jboss.test.ws.jaxws.endpoint;

import java.net.URL;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

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
