/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3593;

import static javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_MTOM_BINDING;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import jakarta.activation.DataHandler;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.interceptor.OutInterceptors;
import org.jboss.logging.Logger;

@WebService(name="Endpoint", targetNamespace="http://org.jboss.ws/jaxws/jbws3593")
@SOAPBinding(style = SOAPBinding.Style.RPC, parameterStyle = SOAPBinding.ParameterStyle.BARE)
@BindingType(SOAP11HTTP_MTOM_BINDING)
@OutInterceptors(interceptors = {"org.jboss.test.ws.jaxws.cxf.jbws3593.MTOMOutInterceptor"})
public class EndpointBean
{
   private static Logger log = Logger.getLogger(EndpointBean.class);
   
   @XmlMimeType("text/plain")
   public DataHandler namespace(@XmlMimeType("text/plain") DataHandler data)
   {
      try
      {
         String name = (String)getContent(data);
         String type = (String)data.getContentType();
         log.info("User " + name + " requested namespace with content type ["+ type +"]");

         return new DataHandler("Hello " + name, "text/plain");
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }
   }
   
   public List<String> echoStrings(List<String> l) {
      if (l != null) {
         for (String s : l) {
            log.info("String: " + s);
         }
      }
      return l;
   }

   protected Object getContent(DataHandler dh) throws IOException
   {
      Object content = dh.getContent();

      if (content instanceof InputStream)
      {
         try
         {
            BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)content));
            return br.readLine();
         }
         finally
         {
            ((InputStream)content).close();
         }
      }
      return content;
   }
}
