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
package org.jboss.test.ws.jaxws.samples.serviceref;

import java.util.ArrayList;

import javax.naming.InitialContext;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPBinding;

public class ApplicationClient 
{
   public static void main(final String[] args) throws Exception
   {
      ArrayList<Endpoint> ports = new ArrayList<Endpoint>(2);
      try
      {
         InitialContext iniCtx = new InitialContext();
         ports.add((Endpoint)((Service)iniCtx.lookup("java:comp/env/service1")).getPort(Endpoint.class));
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/service2")).getEndpointPort());
      }
      catch (Exception ex)
      {
        throw new WebServiceException(ex);
      }
      System.out.println("TEST START");
      for (int i = 0; i < ports.size(); i++)
      {
         Endpoint port = (Endpoint)ports.get(i);

         BindingProvider bp = (BindingProvider)port;
         boolean mtomEnabled = ((SOAPBinding)bp.getBinding()).isMTOMEnabled();
         boolean expectedSetting = (i==0) ? false : true;

         if (mtomEnabled != expectedSetting)
            throw new WebServiceException("MTOM settings (enabled="+expectedSetting+") not overridden through service-ref");

         String inStr = args[0];
         String outStr = port.echo(inStr);
         if (inStr.equals(outStr) == false)
            throw new WebServiceException("Invalid echo return: " + inStr);
      }
      System.out.println("TEST END");
   }
}
