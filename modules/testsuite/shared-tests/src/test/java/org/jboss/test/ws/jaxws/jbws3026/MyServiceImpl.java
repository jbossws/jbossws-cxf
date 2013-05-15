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
package org.jboss.test.ws.jaxws.jbws3026;

import javax.ejb.EJB;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@SOAPBinding(style = Style.RPC)
@WebService(serviceName = "MyService", portName = "MyServicePort")
public class MyServiceImpl implements MyService
{

   @EJB(lookup="java:global/jaxws-jbws3026-ejb/MyBean!org.jboss.test.ws.jaxws.jbws3026.MyBeanRemote")
   MyBeanRemote bean;

    /**
     * Invoking method of injected bean 
     */
   public void useBean()
   {
      bean.myMethod();
   }

   /**
    * This method works if you just remove the @EJB-Injection
    */
   public void thisOneWorks()
   {
      MyBeanRemote bean = lookupBean();
      bean.myMethod();
   }

   /**
    * Looking up the bean by using JNDI
    *
    * @return
    */
   private MyBeanRemote lookupBean()
   {
      MyBeanRemote res = null;
      try
      {
         InitialContext ctx = new InitialContext();
         res= (MyBeanRemote) ctx.lookup("java:global/jaxws-jbws3026-ejb/MyBean!org.jboss.test.ws.jaxws.jbws3026.MyBeanRemote");
      }
      catch (NamingException e)
      {
         System.out.println("Something went wrong");
         throw new RuntimeException(e);
      }
      return res;
   }

}
