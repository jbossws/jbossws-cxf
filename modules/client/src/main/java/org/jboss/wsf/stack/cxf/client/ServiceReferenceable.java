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
package org.jboss.wsf.stack.cxf.client;

import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

import javax.naming.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/**
 * A JNDI reference to a javax.xml.ws.Service
 *
 * It holds the information to reconstrut the javax.xml.ws.Service
 * when the client does a JNDI lookup.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 06-Dec-2007
 */
public class ServiceReferenceable implements Referenceable
{
   public static final String SERVICE_REF_META_DATA = "SERVICE_REF_META_DATA";
   public static final String SERVICE_IMPL_CLASS = "SERVICE_CLASS_NAME";
   public static final String TARGET_CLASS_NAME = "TARGET_CLASS_NAME";

   private String serviceImplClass;
   private String targetClassName;
   private UnifiedServiceRefMetaData serviceRef;

   public ServiceReferenceable(String serviceImplClass, String targetClassName, UnifiedServiceRefMetaData serviceRef)
   {
      this.serviceImplClass = serviceImplClass;
      this.targetClassName = targetClassName;
      this.serviceRef = serviceRef;
   }

   /**
    * Retrieves the Reference of this object.
    *
    * @return The non-null Reference of this object.
    * @throws javax.naming.NamingException If a naming exception was encountered while retrieving the reference.
    */
   public Reference getReference() throws NamingException
   {
      Reference myRef = new Reference(ServiceReferenceable.class.getName(), ServiceObjectFactory.class.getName(), null);

      myRef.add(new StringRefAddr(SERVICE_IMPL_CLASS, serviceImplClass));
      myRef.add(new StringRefAddr(TARGET_CLASS_NAME, targetClassName));
      myRef.add(new BinaryRefAddr(SERVICE_REF_META_DATA, marshall(serviceRef)));

      return myRef;
   }

   private byte[] marshall(Object obj) throws NamingException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
      try
      {
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(obj);
         oos.close();
      }
      catch (IOException e)
      {
         throw new NamingException("Cannot marshall object, cause: " + e.toString());
      }
      return baos.toByteArray();
   }
}
