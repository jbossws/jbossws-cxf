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
package org.jboss.wsf.stack.cxf.deployment.jms;

import java.net.URL;

import org.jboss.wsf.spi.metadata.jms.JMSDescriptorProcessor;
import org.jboss.xb.binding.ObjectModelFactory;

/**
 * JMS descriptor processor implementation.
 * 
 * @author <a href="ropalka@redhat.com">Richard Opalka</a>
 */
public final class JMSDescriptorProcessorImpl implements JMSDescriptorProcessor
{
   private String descriptorName;
   private boolean isValidating;
   
   /* (non-Javadoc)
    * @see org.jboss.wsf.spi.metadata.DescriptorProcessor#getDescriptorName()
    */
   @Override
   public String getDescriptorName()
   {
      return this.descriptorName;
   }

   /**
    * Invoked via MC.
    * @param descriptorName
    */
   public void setDescriptorName(final String descriptorName)
   {
      this.descriptorName = descriptorName;
   }

   /* (non-Javadoc)
    * @see org.jboss.wsf.spi.metadata.DescriptorProcessor#getFactory(java.net.URL)
    */
   @Override
   public ObjectModelFactory getFactory(final URL url)
   {
      if (url == null)
         throw new IllegalArgumentException("URL cannot be null");
      
      return new JMSEndpointsFactory(url);
   }

   /* (non-Javadoc)
    * @see org.jboss.wsf.spi.metadata.DescriptorProcessor#isValidating()
    */
   @Override
   public boolean isValidating()
   {
      return this.isValidating;
   }

   /**
    * Invoked via MC.
    * @param isValidating
    */
   public void setValidating(final boolean isValidating)
   {
      this.isValidating = isValidating;
   }
}
