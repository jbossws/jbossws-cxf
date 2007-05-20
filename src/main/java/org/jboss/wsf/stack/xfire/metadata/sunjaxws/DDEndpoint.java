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
package org.jboss.wsf.stack.xfire.metadata.sunjaxws;

import java.io.IOException;
import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPBinding;

//$Id$

/**
 * Metadata model for sun-jaxws.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-May-2007
 */
public class DDEndpoint
{
   // Name of the endpoint
   private String name;
   // Primary wsdl file location in the WAR file. 
   private String wsdl;
   //QName of WSDL service. 
   private QName service;
   //QName of WSDL port. 
   private QName port;
   // Endpoint implementation class name. 
   private String implementation;
   // Should match <url-pattern> in web.xml 
   private String urlPattern;
   // Binding id defined in the JAX-WS API 
   private String binding;
   // Enables MTOM optimization.
   private boolean enableMTOM;
   // Optional handler chain
   private DDHandlerChain handlerChain;

   public DDEndpoint(String name, String impl, String urlPattern)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("name cannot be null");
      if (impl == null || impl.length() == 0)
         throw new IllegalArgumentException("implementation cannot be null");
      if (urlPattern == null || urlPattern.length() == 0)
         throw new IllegalArgumentException("urlPattern cannot be null");
      
      this.name = name;
      this.implementation = impl;
      this.urlPattern = urlPattern;
      this.binding = SOAPBinding.SOAP11HTTP_BINDING;
   }

   public String getName()
   {
      return name;
   }

   public String getImplementation()
   {
      return implementation;
   }

   public String getUrlPattern()
   {
      return urlPattern;
   }

   public String getBinding()
   {
      return binding;
   }

   public void setBinding(String binding)
   {
      if (!SOAPBinding.SOAP11HTTP_BINDING.equals(binding) && !SOAPBinding.SOAP12HTTP_BINDING.equals(binding))
         throw new IllegalArgumentException("Invalid binding: " + binding);

      this.binding = binding;
   }

   public boolean isEnableMTOM()
   {
      return enableMTOM;
   }

   public void setEnableMTOM(boolean enableMTOM)
   {
      this.enableMTOM = enableMTOM;
   }

   public QName getPort()
   {
      return port;
   }

   public void setPort(QName port)
   {
      this.port = port;
   }

   public QName getService()
   {
      return service;
   }

   public void setService(QName service)
   {
      this.service = service;
   }

   public String getWsdl()
   {
      return wsdl;
   }

   public void setWsdl(String wsdl)
   {
      this.wsdl = wsdl;
   }

   public DDHandlerChain getHandlerChain()
   {
      return handlerChain;
   }

   public void setHandlerChain(DDHandlerChain handlerChain)
   {
      this.handlerChain = handlerChain;
   }

   public void writeTo(Writer writer) throws IOException
   {
      writer.write("<endpoint");
      writer.write(" name='" + name + "'");
      writer.write(" implementation='" + implementation + "'");
      writer.write(" url-pattern='" + urlPattern + "'");
      if (service != null)
         writer.write(" service='" + service + "'");
      if (port != null)
         writer.write(" port='" + port + "'");
      if (wsdl != null)
         writer.write(" wsdl='" + wsdl + "'");
      if (binding != null)
         writer.write(" binding='" + binding + "'");
      writer.write(" enable-mtom='" + enableMTOM + "'");
      writer.write(">");
      
      if (handlerChain != null)
         handlerChain.writeTo(writer);
      
      writer.write("</endpoint>");
   }
   
   public String toString()
   {
      StringBuilder str = new StringBuilder("Endpoint");
      str.append("\n name=" + name);
      str.append("\n implementation=" + implementation);
      str.append("\n url-pattern=" + urlPattern);
      if (service != null)
         str.append("\n service=" + service);
      if (port != null)
         str.append("\n port=" + port);
      if (wsdl != null)
         str.append("\n wsdl=" + wsdl);
      if (binding != null)
         str.append("\n binding=" + binding);
      str.append("\n enable-mtom=" + enableMTOM);
      return str.toString();
   }
}