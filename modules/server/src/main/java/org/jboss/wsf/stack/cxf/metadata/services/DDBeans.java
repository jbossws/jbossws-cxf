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
package org.jboss.wsf.stack.cxf.metadata.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.wsf.spi.deployment.WSFDeploymentException;
import org.jboss.wsf.common.IOUtils;

/**
 * Metadata model for cxf.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-May-2007
 */
public class DDBeans
{
   // The Required services.
   private List<DDEndpoint> endpoints = new ArrayList<DDEndpoint>();
   // Optional additional beans.
   private List<DDBean> beans = new ArrayList<DDBean>();
   
   private List<DDJmsAddressBean> addressBeans = new ArrayList<DDJmsAddressBean>();
   
   // The derived temp file
   private File tmpFile;

   public List<DDEndpoint> getEndpoints()
   {
      return endpoints;
   }

   public void addEndpoint(DDEndpoint service)
   {
      endpoints.add(service);
   }
   
   public void addAddress(DDJmsAddressBean addressBean) 
   {
      addressBeans.add(addressBean);     
   }

   public List<DDBean> getBeans()
   {
      return beans;
   }

   public void addBean(DDBean bean)
   {
      beans.add(bean);
   }

   public URL createFileURL()
   {
      destroyFileURL();

      try
      {
         File tmpDir = IOUtils.createTempDirectory();
         tmpFile = File.createTempFile("jbossws-cxf", ".xml", tmpDir);
         Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile));
         writeTo(writer);
         writer.close();

         return tmpFile.toURI().toURL();
      }
      catch (IOException ex)
      {
         throw new WSFDeploymentException(ex);
      }
   }

   public void destroyFileURL()
   {
      if (tmpFile != null)
      {
         tmpFile.delete();
         tmpFile = null;
      }
   }

   public void writeTo(Writer writer) throws IOException
   {
      writer.write("<beans " +
            "xmlns='http://www.springframework.org/schema/beans' " +
            "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
            "xmlns:beans='http://www.springframework.org/schema/beans' " +
            "xmlns:jaxws='http://cxf.apache.org/jaxws' " +
            "xmlns:wsa='http://cxf.apache.org/ws/addressing' " +
            "xmlns:jms='http://cxf.apache.org/transports/jms' " +
            "xmlns:soap='http://cxf.apache.org/bindings/soap' " + 
            "xsi:schemaLocation='http://www.springframework.org/schema/beans " +
            "http://www.springframework.org/schema/beans/spring-beans.xsd " +
            "http://cxf.apache.org/transports/jms " + 
            "http://cxf.apache.org/schemas/configuration/jms.xsd " +
            "http://cxf.apache.org/jaxws " +
            "http://cxf.apache.org/schemas/jaxws.xsd'>");
      
      for (DDEndpoint endpoint : endpoints)
      {
         endpoint.writeTo(writer);
      }
      for (DDBean bean : beans)
      {
         bean.writeTo(writer);
      }
      
      for (DDJmsAddressBean bean : this.addressBeans) {
         bean.writeTo(writer);
      }
      writer.write("</beans>");
   }

}
