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
package org.jboss.wsf.stack.cxf.client.injection;

import java.io.InputStream;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.cxf.resource.ResourceResolver;

/**
 * A CXF ResourceResolver that tries looking up the specified resources in the JNDI
 *
 * @author alessio.soldano@jboss.com
 * @since 07-Apr-2014
 */
public class JBossWSResourceInjectionResolver implements ResourceResolver
{
   private static final JBossWSResourceInjectionResolver me = new JBossWSResourceInjectionResolver();
   
   private JBossWSResourceInjectionResolver() {
      //NOOP
   }

   public static JBossWSResourceInjectionResolver getInstance() {
      return me;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public <T> T resolve(String resourceName, Class<T> resourceType)
   {
      try {
         return (T)new InitialContext().lookup("java:comp/env/" + resourceName);
      } catch (NamingException ne) {
         return null;
      }
   }

   @Override
   public InputStream getAsStream(String name)
   {
      return null;
   }
   
}
