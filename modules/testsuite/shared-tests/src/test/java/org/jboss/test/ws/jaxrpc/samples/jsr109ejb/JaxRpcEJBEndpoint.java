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
package org.jboss.test.ws.jaxrpc.samples.jsr109ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jboss.logging.Logger;

/**
 * An EJB service endpoint
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-Feb-2005
 */
public class JaxRpcEJBEndpoint implements SessionBean
{
   // Provide logging
   private static Logger log = Logger.getLogger(JaxRpcEJBEndpoint.class);

   private SessionContext context;

   public String echoString(String str1, String str2)
   {
      log.info("echoString: " + str1 + "," + str2);
      log.info("callerPricipal: " + context.getCallerPrincipal());
      return str1 + str2;
   }

   public SimpleUserType echoSimpleUserType(String str1, SimpleUserType usr)
   {
      log.info("echoSimpleUserType: " + str1 + "," + usr);
      log.info("callerPricipal: " + context.getCallerPrincipal());
      return usr;
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
