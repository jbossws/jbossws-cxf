/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.interceptor.util;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingOperationInfo;

/**
 * Removable feature class to enable/disable schema validation for endpoint
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class RemovableSchemaValidatonFeature extends RemovableFeature
{

   public RemovableSchemaValidatonFeature()
   {
      super(new JBossWSSchemaValidationFeature());
   }
   
   public RemovableSchemaValidatonFeature(JBossWSSchemaValidationFeature schemaValidationFeature)
   {
      super(schemaValidationFeature);
   }

   @Override
   public void remove(Endpoint endpoint)
   {
      for (BindingOperationInfo bop : endpoint.getEndpointInfo().getBinding().getOperations())
      {
         bop.getOperationInfo().removeProperty(Message.SCHEMA_VALIDATION_TYPE);
      }
   }

}
