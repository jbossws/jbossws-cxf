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
package org.jboss.wsf.stack.cxf.metadata.services;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

/**
 * DDJmsAddressBean.
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class DDJmsAddressBean
{
   private String portName;
   private Properties properties;
   private boolean isRequest = true;
   
   public DDJmsAddressBean(String portName, boolean isRequest, Properties properties)
   {
      this.portName = portName;
      this.isRequest = isRequest;
      this.properties = properties;
   }
   
   public void writeTo(Writer writer) throws IOException
   {
      if (isRequest) 
      {
        writer.write("<jms:destination name='" + portName + ".jms-destination'>");  
      } 
      else
      {
        writer.write("<jms:conduit name='" + portName + ".jms-conduit'>"); 
      }
      writer.write("<jms:address");
      for (String name : properties.stringPropertyNames()) 
      {
         writer.write(" " + name + "='" + properties.getProperty(name) + "'");
      }
      writer.write("/>");
      if (isRequest) 
      {
        writer.write("</jms:destination>");  
      } 
      else
      {
        writer.write("</jms:conduit>"); 
      }
   }

   public String toString()
   {
      StringWriter strWriter = new StringWriter();
      strWriter.write("JMSAddressing");
      strWriter.write("\n portName=" + portName);
      strWriter.write("\n isRequest=" + isRequest);
      strWriter.write("\n");
      properties.list(new java.io.PrintWriter(strWriter));
      return strWriter.toString();
   }
   

}
