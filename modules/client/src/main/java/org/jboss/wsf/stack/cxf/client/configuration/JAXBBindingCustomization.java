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
package org.jboss.wsf.stack.cxf.client.configuration;

import com.sun.xml.bind.api.JAXBRIContext;

public class JAXBBindingCustomization extends org.jboss.wsf.spi.binding.JAXBBindingCustomization
{
   private static final long serialVersionUID = -5922902727331798457L;

   // Use an alternative RuntimeAnnotationReader implementation
   public final static String ANNOTATION_READER = JAXBRIContext.ANNOTATION_READER;

   // Reassign the default namespace URI to something else at the runtime
   public final static String DEFAULT_NAMESPACE_REMAP = JAXBRIContext.DEFAULT_NAMESPACE_REMAP;

   // Enable the c14n marshalling support in the JAXBContext.
   public final static String CANONICALIZATION_SUPPORT = JAXBRIContext.CANONICALIZATION_SUPPORT;
}
