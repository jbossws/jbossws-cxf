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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.logging.Logger;

public class ServletClient extends HttpServlet
{
   private static final long serialVersionUID = -5214608199882450292L;

   // Provide logging
   private static Logger log = Logger.getLogger(ServletClient.class);

   @WebServiceRef(name="service3")
   EndpointService injectedService = null;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      String inStr = req.getParameter("echo");
      log.info("doGet: " + inStr);

      List<Endpoint> ports = new ArrayList<Endpoint>();
      try
      {
         InitialContext iniCtx = new InitialContext();
         ports.add(((Service)iniCtx.lookup("java:comp/env/service1")).getPort(Endpoint.class));
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/service2")).getEndpointPort());
      }
      catch (Exception ex)
      {
         log.error("Cannot add port", ex);
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

      // Test the injected service as well
      Endpoint injectedPort = injectedService.getEndpointPort();
      String outStr = injectedPort.echo(" injected service");
      if (outStr.equals(" injected service") == false)
         throw new WebServiceException("Invalid echo return on injected service/port: " + inStr);

      res.getWriter().print(inStr);
   }
}
