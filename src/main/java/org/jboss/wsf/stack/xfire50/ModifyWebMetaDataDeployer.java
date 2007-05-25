/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.wsf.stack.xfire50;

//$Id$

import org.jboss.metadata.WebMetaData;
import org.jboss.metadata.web.ParamValue;
import org.jboss.metadata.web.ParamValue.ParamType;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.stack.xfire.XFireConfigurableServletExt;
import org.jboss.wsf.stack.xfire.metadata.services.DDBeans;

/**
 * A deployer that modifies the web.xml meta data 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Mai-2007
 */
public class ModifyWebMetaDataDeployer extends org.jboss.wsf.container.jboss50.ModifyWebMetaDataDeployer
{
   @Override
   public void create(Deployment dep)
   {
      super.create(dep);
      
      WebMetaData webMetaData = dep.getContext().getAttachment(WebMetaData.class);
      if (webMetaData != null)
      {
         DDBeans ddbeans = dep.getContext().getAttachment(DDBeans.class);
         if (ddbeans == null)
            throw new IllegalStateException("Cannot obtain services.xml meta data");

         // Add the path to sun-jaxws.xml
         ParamValue ctxParam = new ParamValue();
         ctxParam.setType(ParamType.CONTEXT_PARAM);
         ctxParam.setName(XFireConfigurableServletExt.PARAM_XFIRE_SERVICES_URL);
         ctxParam.setValue(ddbeans.createFileURL().toExternalForm());
         webMetaData.addContextParam(ctxParam);
      }
   }

   @Override
   public void destroy(Deployment dep)
   {
      super.destroy(dep);
      
      DDBeans ddbeans = dep.getContext().getAttachment(DDBeans.class);
      if (ddbeans != null)
      {
         ddbeans.destroyFileURL();
      }
   }
}