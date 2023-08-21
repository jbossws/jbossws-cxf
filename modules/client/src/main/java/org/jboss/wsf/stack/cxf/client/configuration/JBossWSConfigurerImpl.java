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
package org.jboss.wsf.stack.cxf.client.configuration;

import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.extension.BusExtension;

/**
 * A CXF configurer that sets JBossWS stuff / customizations / properties etc. in CXF configurable beans
 *
 * @author alessio.soldano@jboss.com
 * @since 05-Oct-2009
 */
public class JBossWSConfigurerImpl implements JBossWSConfigurer, BusExtension
{
   private BeanCustomizer customizer;
   
   public JBossWSConfigurerImpl(BeanCustomizer customizer)
   {
      this.customizer = customizer;
   }

   @Override
   public void configureBean(Object beanInstance)
   {
      customConfigure(beanInstance);
   }

   @Override
   public void configureBean(String name, Object beanInstance)
   {
      customConfigure(beanInstance);
   }
   
   protected synchronized void customConfigure(Object beanInstance)
   {
      if (customizer != null)
      {
         customizer.customize(beanInstance);
      }
   }

   public BeanCustomizer getCustomizer()
   {
      return customizer;
   }

   public void setCustomizer(BeanCustomizer customizer)
   {
      this.customizer = customizer;
   }

   @Override
   public Class<?> getRegistrationType()
   {
      return Configurer.class;
   }
}
