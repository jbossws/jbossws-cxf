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
package org.jboss.test.ws.jaxws.smoke.tools;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@WebService(targetNamespace = "http://foo.bar.com/calculator")
public class CalculatorBean
{
   @WebMethod
   public int add(int a, int b)
   {
      return a+b;
   }

   @WebMethod
   public int subtract(int a, int b)
   {
      return a-b;
   }
   
   @WebMethod
   public Set<Integer> getKeys(HashMap<Integer, String> map) throws RuntimeException, RemoteException
   {
      if (map != null)
         return map.keySet();
      else
         return null;
   }
   
   @WebMethod
   @XmlList
   public List<String> processList(@XmlList List<String> list)
   {
      return list;
   }
   
   @WebMethod
   @XmlJavaTypeAdapter(CustomAdapter.class)
   public CustomBean processCustom(@XmlJavaTypeAdapter(CustomAdapter.class) CustomBean bean)
   {
      return bean;
   }
}
