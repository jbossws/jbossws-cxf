/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.wsf.stack.cxf.cdi;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This wrapper allows additional metadata to be added during bootstrap.
 * 
 * @author Jozef Hartinger
 * 
 */
public class JaxrsAnnotatedType<TYPE> implements AnnotatedType<TYPE>
{

   private AnnotatedType<TYPE> delegate;
   private Set<Annotation> annotations = new HashSet<Annotation>();
   
   public JaxrsAnnotatedType(AnnotatedType<TYPE> delegate, Annotation scope)
   {
      this.delegate = delegate;
      this.annotations.addAll(delegate.getAnnotations());
      this.annotations.add(scope);
   }

   public Set<AnnotatedConstructor<TYPE>> getConstructors()
   {
      return delegate.getConstructors();
   }

   public Set<AnnotatedField<? super TYPE>> getFields()
   {
      return delegate.getFields();
   }

   public Class<TYPE> getJavaClass()
   {
      return delegate.getJavaClass();
   }

   public Set<AnnotatedMethod<? super TYPE>> getMethods()
   {
      return delegate.getMethods();
   }

   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      return delegate.getAnnotation(annotationType);
   }

   public Set<Annotation> getAnnotations()
   {
      return Collections.unmodifiableSet(annotations);
   }

   public Type getBaseType()
   {
      return delegate.getBaseType();
   }

   public Set<Type> getTypeClosure()
   {
      return delegate.getTypeClosure();
   }

   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return delegate.isAnnotationPresent(annotationType);
   }
}
