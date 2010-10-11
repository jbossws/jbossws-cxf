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
package org.jboss.test.ws.saaj;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.helpers.LoadingByteArrayOutputStream;

@WebService
(
   portName = "SaajServicePort",
   serviceName = "SaajService",
   wsdlLocation = "WEB-INF/wsdl/SaajService.wsdl",
   targetNamespace = "http://www.jboss.org/jbossws/saaj",
   endpointInterface = "org.jboss.test.ws.saaj.ServiceIface"
)
public class ServiceImpl implements ServiceIface
{
   @Resource
   private WebServiceContext context;
   
   public String sayHello()
   {
	  Map<String, List<String>> reqHeaders = CastUtils.cast(
		 (Map<?, ?>)context.getMessageContext().get(MessageContext.HTTP_REQUEST_HEADERS));
	  
	  boolean chunkedEncodingDisabled = reqHeaders.get("transfer-encoding-disabled") != null;
	  	  
	  List<String> transferEncHeader = reqHeaders.get("transfer-encoding");
	  
	  if (!chunkedEncodingDisabled)
	  {
         if (transferEncHeader == null || transferEncHeader.size() != 1)
         {
            throw new RuntimeException("Transfer-Encoding is missing");
         }
         if (!"chunked".equals(transferEncHeader.get(0))) 
         {
            throw new RuntimeException("Wrong Transfer-Encoding value");
         }
	  }
	  else 
	  {
		  if (transferEncHeader != null)
		  {
			  throw new RuntimeException("Unexpected Transfer-Encoding header");
		  }
		  Map<String, List<String>> respHeaders = CastUtils.cast(
		     (Map<?, ?>)context.getMessageContext().get(MessageContext.HTTP_RESPONSE_HEADERS));
	      if (respHeaders == null) {
	         respHeaders = new HashMap<String, List<String>>();
	         context.getMessageContext().put(MessageContext.HTTP_RESPONSE_HEADERS, respHeaders);
	      }
	      respHeaders.put("Transfer-Encoding-Disabled", Arrays.asList("true"));
	  }
	  
	  Map<String, DataHandler> dataHandlers = CastUtils.cast(
         (Map<?, ?>)context.getMessageContext().get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS));
	  
	  Map<String, DataHandler> outDataHandlers = CastUtils.cast(
	     (Map<?, ?>)context.getMessageContext().get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS));
  
	  int index = 0;
	  try 
	  {
         for (Map.Entry<String, DataHandler> entry : dataHandlers.entrySet()) 
         {
            InputStream is = entry.getValue().getInputStream();
            LoadingByteArrayOutputStream bous = new LoadingByteArrayOutputStream();
            IOUtils.copy(is, bous);
            String name =  Integer.toString(index++);
            DataHandler handler = new DataHandler(
        		 new InputStreamDataSource(bous.createInputStream(), "text/plain", name));
            outDataHandlers.put(name, handler);
         }
	  }
	  catch (Exception ex)
	  {
		  throw new RuntimeException(ex);
	  }
	   
      return "Hello World!";
   }
   
   public String greetMe()
   {
	  try 
	  {
	     Map<String, DataHandler> outDataHandlers = CastUtils.cast(
	        (Map<?, ?>)context.getMessageContext().get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS));
  
	     final char[] content = new char[16 * 1024];
	     Arrays.fill(content, 'A');
	       
	     DataHandler handler = new DataHandler(
            new InputStreamDataSource(new ByteArrayInputStream(new String(content).getBytes()), 
            		"text/plain", "1"));
         outDataHandlers.put("1", handler);
         
	  }
	  catch (Exception ex)
	  {
		  throw new RuntimeException(ex);
	  }
	   
      return "Greetings";
   }
   
}
