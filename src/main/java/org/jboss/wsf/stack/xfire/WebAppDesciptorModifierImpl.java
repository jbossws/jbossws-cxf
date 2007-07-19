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

import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.WebAppDesciptorModifier;
import org.jboss.wsf.stack.xfire.metadata.services.DDBeans;

/**
 * Modifies web.xml for jbossws
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-May-2006
 */
public class WebAppDesciptorModifierImpl implements WebAppDesciptorModifier
{
   // logging support
   private static Logger log = Logger.getLogger(WebAppDesciptorModifierImpl.class);

   private String servletClass;

   public String getServletClass()
   {
      return servletClass;
   }

   public void setServletClass(String servletClass)
   {
      this.servletClass = servletClass;
   }

   public RewriteResults modifyDescriptor(Deployment dep, Document webXml) throws ClassNotFoundException
   {
      RewriteResults results = new RewriteResults();

      Element root = webXml.getRootElement();

      DDBeans ddbeans = dep.getContext().getAttachment(DDBeans.class);
      if (ddbeans == null)
         throw new IllegalStateException("Cannot obtain sun-jaxws meta data");

      Element contextParam = root.addElement("context-param");
      contextParam.addElement("param-name").addText(CXFServletExt.PARAM_CXF_BEANS_URL);
      contextParam.addElement("param-value").addText(ddbeans.createFileURL().toExternalForm());

      for (Iterator it = root.elementIterator("servlet"); it.hasNext();)
      {
         Element servlet = (Element)it.next();
         String linkName = servlet.element("servlet-name").getTextTrim();

         // find the servlet-class
         Element classElement = servlet.element("servlet-class");

         // JSP
         if (classElement == null)
            continue;

         String orgServletClassName = classElement.getTextTrim();

         // Get the servlet class
         Class orgServletClass = null;
         try
         {
            ClassLoader loader = dep.getInitialClassLoader();
            orgServletClass = loader.loadClass(orgServletClassName);
         }
         catch (ClassNotFoundException ex)
         {
            log.warn("Cannot load servlet class: " + orgServletClassName);
         }

         String targetBeanName = null;

         // Check if it is a real servlet that we can ignore
         if (orgServletClass != null && javax.servlet.Servlet.class.isAssignableFrom(orgServletClass))
         {
            log.info("Ignore servlet: " + orgServletClassName);
            continue;
         }
         else if (orgServletClassName.endsWith("Servlet"))
         {
            log.info("Ignore <servlet-class> that ends with 'Servlet': " + orgServletClassName);
            continue;
         }

         classElement.setText(servletClass);
         targetBeanName = orgServletClassName;

         if (targetBeanName == null)
            throw new IllegalStateException("Cannot obtain service endpoint bean for: " + linkName);

         // remember the target bean name
         results.sepTargetMap.put(linkName, targetBeanName);
      }

      return results;
   }
}