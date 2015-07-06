/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.interceptor;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;
import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

/**
 * Out Interceptor to get json format endpoint config info. This interceptor is only 
 * responds to get url like http://localhost:8080/context/wsendpoint/management?config
 *@author <a href="mailto:ema@redhat.com>Jim Ma</a>
 *
 */
public class EndpointConfigsGetOutIntercetpor extends AbstractManagementInterceptor
{
   public static final EndpointConfigsGetOutIntercetpor INSTANCE = new EndpointConfigsGetOutIntercetpor();

   public EndpointConfigsGetOutIntercetpor()
   {
      super(Phase.PREPARE_SEND);
      this.addAfter(MessageSenderInterceptor.class.getName());
   }

   public void handleMessage(Message message) throws Fault
   {
      @SuppressWarnings("unchecked")
      Map<String, String> configMaps = (Map<String, String>)message.get(EndpointConfigsGetInterceptor.ENDPOINT_CONFIGS);
      if (configMaps == null)
      {
         return;
      }
      message.remove(EndpointConfigsGetInterceptor.ENDPOINT_CONFIGS);
      OutputStream out = message.getContent(OutputStream.class);
      if (out == null)
      {
         return;
      }
      setContentType(message);
      OutputStreamWriter writer = null;
      AbstractXMLStreamWriter mappedWriter = null;
      try
      {
         writer = new OutputStreamWriter(out, getEncoding(message));
         mappedWriter = new MappedXMLStreamWriter(new MappedNamespaceConvention(), writer);
         mappedWriter.writeStartDocument();
         for (String key : configMaps.keySet())
         {
            String value = configMaps.get(key);
            if (StringUtils.isEmpty(value))
            {
               mappedWriter.writeEmptyElement(key);
            }
            else
            {
               mappedWriter.writeStartElement(key);
               mappedWriter.writeCharacters(configMaps.get(key));
               mappedWriter.writeEndElement();
            }
         }
         mappedWriter.writeEndDocument();
         out.flush();
      }
      catch (Exception e)
      {
         throw new Fault(e);
      }
      finally
      {
         StaxUtils.close(mappedWriter);
      }

   }
}
