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
package org.jboss.wsf.stack.xfire;

//$Id$

import org.jboss.metadata.WebMetaData;
import org.jboss.metadata.web.ParamValue;
import org.jboss.metadata.web.Servlet;
import org.jboss.metadata.web.ParamValue.ParamType;
import org.jboss.wsf.spi.deployment.AbstractDeployer;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.stack.xfire.metadata.services.DDBeans;

/**
 * A deployer that modifies the web.xml meta data 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Mai-2007
 */
public class ModifyWebMetaDataDeployer extends AbstractDeployer
{
   private String servletClass;

   public String getServletClass()
   {
      return servletClass;
   }

   public void setServletClass(String servletClass)
   {
      this.servletClass = servletClass;
   }

   @Override
   public void create(Deployment dep)
   {
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

         for (Servlet servlet : webMetaData.getServlets())
         {
            String orgServletClass = servlet.getServletClass();

            // JSP
            if (orgServletClass == null || orgServletClass.length() == 0)
            {
               log.debug("Innore servlet class: " + orgServletClass);
               continue;
            }

            if (!isJavaxServlet(orgServletClass, dep.getClassLoader()))
            {
               servlet.setServletClass(servletClass);
            }
         }
      }
   }

   @Override
   public void destroy(Deployment dep)
   {
      DDBeans ddbeans = dep.getContext().getAttachment(DDBeans.class);
      if (ddbeans != null)
      {
         ddbeans.destroyFileURL();
      }
   }

   private boolean isJavaxServlet(String orgServletClass, ClassLoader loader)
   {
      boolean isServlet = false;
      if (loader != null)
      {
         try
         {
            Class servletClass = loader.loadClass(orgServletClass);
            isServlet = javax.servlet.Servlet.class.isAssignableFrom(servletClass);
            if (isServlet == true)
            {
               log.info("Ignore servlet: " + orgServletClass);
            }
         }
         catch (ClassNotFoundException e)
         {
            log.warn("Cannot load servlet class: " + orgServletClass);
         }
      }
      return isServlet;
   }
}