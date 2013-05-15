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
package org.jboss.test.ws.jaxrpc.samples.serviceref;

import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.Service;

import org.jboss.logging.Logger;

public class EJBClient implements SessionBean
{
   // Provide logging
   private static Logger log = Logger.getLogger(EJBClient.class);

   private SessionContext context;

   public String echo(String inStr) throws RemoteException
   {
      log.info("echo: " + inStr);

      ArrayList ports = new ArrayList();
      try
      {
         InitialContext iniCtx = new InitialContext();
         ports.add((TestEndpoint)((Service)iniCtx.lookup("java:comp/env/service1")).getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/service2")).getTestEndpointPort());
      }
      catch (Exception ex)
      {
         log.error("Cannot add port", ex);
         throw new JAXRPCException(ex);
      }

      for (int i = 0; i < ports.size(); i++)
      {
         TestEndpoint port = (TestEndpoint)ports.get(i);
         String outStr = port.echo(inStr);
         if (inStr.equals(outStr) == false)
            throw new JAXRPCException("Invalid echo return: " + inStr);
      }

      return inStr;
   }

   // EJB Lifecycle ----------------------------------------------------------------------

   public void setSessionContext(SessionContext context) throws EJBException, RemoteException
   {
      this.context = context;
   }

   public void ejbCreate()
   {
   }

   public void ejbRemove()
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }
}
