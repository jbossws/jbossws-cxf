/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3670;
import javax.jws.WebService;


@WebService(serviceName = "HelloWorldService", endpointInterface = "org.jboss.test.ws.jaxws.cxf.jbws3670.HelloWorldServiceI", targetNamespace = "http://jbossws.jboss.org/helloworld", portName = "HelloWorldServicePort")
public class HelloWorldService implements HelloWorldServiceI
{
   private TestBean testBean;
   
   public HelloWorldService () {
      
   }
   public HelloWorldService(TestBean testBean)
   {
      this.testBean = testBean;
   }

   @Override
   public String sayHello(String username)
   {  
      if (testBean == null) {
         return "";
      } 
      return "Hello " + username ;
   }
}
