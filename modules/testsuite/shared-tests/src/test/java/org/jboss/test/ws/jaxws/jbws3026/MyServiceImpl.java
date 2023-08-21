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
package org.jboss.test.ws.jaxws.jbws3026;

import jakarta.ejb.EJB;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.jws.soap.SOAPBinding.Style;
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
