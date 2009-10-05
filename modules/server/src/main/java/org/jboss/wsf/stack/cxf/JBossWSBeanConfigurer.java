/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf;

import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.service.Service;
import org.jboss.wsf.spi.binding.BindingCustomization;
import org.jboss.wsf.spi.binding.JAXBBindingCustomization;
import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * A CXF configurer that sets JBossWS stuff / customizations / properties etc. in CXF configurable beans
 *
 * @author alessio.soldano@jboss.com
 * @since 05-Oct-2009
 */
public class JBossWSBeanConfigurer implements Configurer
{
   private Endpoint endpoint;
   private Configurer delegate;

   public JBossWSBeanConfigurer(Endpoint endpoint, Configurer delegate)
   {
      this.endpoint = endpoint;
      this.delegate = delegate;
   }

   public void configureBean(Object beanInstance)
   {
      if (beanInstance instanceof Service)
      {
         configureService((Service)beanInstance);
      }
      if (delegate != null)
      {
         delegate.configureBean(beanInstance);
      }
   }

   public void configureBean(String name, Object beanInstance)
   {
      if (beanInstance instanceof Service)
      {
         configureService((Service)beanInstance);
      }
      if (delegate != null)
      {
         delegate.configureBean(name, beanInstance);
      }
   }
   
   private synchronized void configureService(Service service)
   {
      //Configure binding customization
      BindingCustomization customization = endpoint.getAttachment(BindingCustomization.class);
      if (customization != null)
      {
         DataBinding db = service.getDataBinding();
         //JAXB
         if (customization instanceof JAXBBindingCustomization)
         {
            if (db != null && db instanceof JAXBDataBinding)
            {
               ((JAXBDataBinding)db).setContextProperties(customization);
            }
         }
         //add other binding customization here below
         
      }
      //add other configurations here below
      
   }

}
