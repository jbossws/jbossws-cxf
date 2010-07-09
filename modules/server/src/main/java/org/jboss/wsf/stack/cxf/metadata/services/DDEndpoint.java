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

import java.io.IOException;
import java.io.Writer;

import javax.xml.namespace.QName;

/**
 * Metadata model for cxf.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @author ropalka@redhat.com
 * @author alessio.soldano@jboss.com
 */
public class DDEndpoint
{
   //fields mapped to jboss-cxf.xml
   private String id;

   private String address;

   private String implementor;

   private String invoker;

   private boolean mtomEnabled;
   private String wsdlLocation;
   private QName portName;
   private QName serviceName;
   
   //additional fields
   private Class<?> epClass;
   
   private int counter = 0;

   public QName getPortName()
   {
      return portName;
   }

   public void setPortName(QName portName)
   {
      this.portName = portName;
   }

   public QName getServiceName()
   {
      return serviceName;
   }

   public void setServiceName(QName serviceName)
   {
      this.serviceName = serviceName;
   }
   
   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getAddress()
   {
      return address;
   }

   public void setAddress(String address)
   {
      this.address = address;
   }

   public String getImplementor()
   {
      return implementor;
   }

   public void setImplementor(String implementor)
   {
      this.implementor = implementor;
   }

   public String getWsdlLocation()
   {
      return wsdlLocation;
   }

   public void setWsdlLocation(String wsdlLocation)
   {
      this.wsdlLocation = wsdlLocation;
   }

   public Class<?> getEpClass()
   {
      return epClass;
   }

   public void setEpClass(Class<?> epClass)
   {
      this.epClass = epClass;
   }

   public String getInvoker()
   {
      return invoker;
   }

   public boolean isMtomEnabled()
   {
      return mtomEnabled;
   }

   public void setInvoker(String invoker)
   {
      this.invoker = invoker;
   }

   public void setMtomEnabled(boolean mtomEnabled)
   {
      this.mtomEnabled = mtomEnabled;
   }

   public void writeTo(Writer writer) throws IOException
   {
      writer.write("<jaxws:endpoint id='" + this.id + "'");
      writer.write(" address='" + this.address + "'");
      writer.write(" implementor='" + this.implementor + "'");
      if (this.serviceName != null)
      {
         this.writeQNameElementTo("serviceName", this.serviceName, writer);
      }
      if (this.portName != null)
      {
         this.writeQNameElementTo("endpointName", this.portName, writer);
      }
      if (this.wsdlLocation != null)
      {
         writer.write(" wsdlLocation='" + this.wsdlLocation + "'");
      }
      writer.write(">");

      if (this.mtomEnabled)
      {
         writer.write("<jaxws:binding>");
         writer.write("<soap:soapBinding mtomEnabled='" + this.mtomEnabled + "'/>");
         writer.write("</jaxws:binding>");
      }

      if (this.invoker != null)
      {
         writer.write("<jaxws:invoker><bean class='" + this.invoker + "'/></jaxws:invoker>");
      }

      writer.write("</jaxws:endpoint>");
   }
   
   private void writeQNameElementTo(String elementName, QName qname, Writer writer) throws IOException
   {
      String prefix = "ns" + counter++;
      writer.write(" " + elementName + "='" + prefix + ":" + qname.getLocalPart() + "'");
      writer.write(" xmlns:" + prefix + "='" + qname.getNamespaceURI() + "'");
   }

   public String toString()
   {
      StringBuilder str = new StringBuilder("Service");
      str.append("\n id=" + this.id);
      str.append("\n address=" + this.address);
      str.append("\n implementor=" + this.implementor);
      str.append("\n invoker=" + this.invoker);
      str.append("\n serviceName=" + this.serviceName);
      str.append("\n portName=" + this.portName);
      str.append("\n wsdlLocation=" + this.wsdlLocation);
      str.append("\n mtomEnabled=" + this.mtomEnabled);
      return str.toString();
   }
}
