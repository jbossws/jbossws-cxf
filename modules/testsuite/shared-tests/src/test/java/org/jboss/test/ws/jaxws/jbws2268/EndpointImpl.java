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
package org.jboss.test.ws.jaxws.jbws2268;

import java.io.File;
import java.io.FileWriter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * Endpoint Implementation
 * @author richard.opalka@jboss.com
 */
@WebService(
   serviceName = "EndpointService",
   targetNamespace = "http://www.jboss.org/test/ws/jaxws/jbws2268",
   endpointInterface="org.jboss.test.ws.jaxws.jbws2268.EndpointInterface"
)
public class EndpointImpl
{

   private StringBuilder builder = new StringBuilder();
   private File file;
   
   public EndpointImpl()
   {
      super();
   }
   
   @PostConstruct
   protected void init()
   {
      this.builder.append("init() ");
   }
   
   @WebMethod
   public boolean setFile(String targetFile)
   {
      this.file = new File(targetFile);
      return this.file.exists();
   }
   
   @PreDestroy
   protected void destroy()
   {
      this.builder.append("destroy()");
      this.writeTestLog();
   }
   
   private void writeTestLog()
   {
      try
      {
         FileWriter fw = new FileWriter(this.file);
         fw.write(this.builder.toString());
         fw.close();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.getMessage(), e);
      }
   }

}
