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
import java.util.List;
import java.util.Map;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;
import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.jboss.ws.api.monitoring.Record;

/**
 * Out Interceptor to write json format endpoint request records. This interceptor is added to interceptorchain by 
 * @see org.jboss.wsf.stack.cxf.interceptor.EndpointRecordGetInterceptor
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class EndpointRecordGetOutInterceptor extends AbstractManagementInterceptor
{
   public static final EndpointRecordGetOutInterceptor INSTANCE = new EndpointRecordGetOutInterceptor();

   public EndpointRecordGetOutInterceptor()
   {
      super(Phase.PREPARE_SEND);
      this.addAfter(MessageSenderInterceptor.class.getName());
   }

   public void handleMessage(Message message) throws Fault
   {
      @SuppressWarnings("unchecked")
      Map<String, List<Record>> records = (Map<String, List<Record>>)message.get(EndpointRecordGetInterceptor.RECORDS);
      message.remove(EndpointRecordGetInterceptor.RECORDS);
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
         for (String group : records.keySet()) {
            mappedWriter.writeStartElement(group);
            for(Record record : records.get(group)) {
               mappedWriter.writeStartElement(record.getMessageType().toString());
               writeElement(mappedWriter, "data", record.getDate());
               writeElement(mappedWriter, "sender", record.getSourceHost());
               writeElement(mappedWriter, "receiver", record.getDestinationHost());
               writeElement(mappedWriter, "operation", record.getOperation());
               writeElement(mappedWriter, "envelope", record.getEnvelope());
               mappedWriter.writeStartElement("headers");
               for(String key : record.getHeaders().keySet()) {
                  writeElement(mappedWriter, key, record.getHeaders().get(key));
               }
               mappedWriter.writeEndElement();
               mappedWriter.writeEndElement();
            }
            mappedWriter.writeEndElement();
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

   private void writeElement(AbstractXMLStreamWriter writter, String key, Object value) throws Exception
   {
      writter.writeStartElement(key);
      if (value != null)
      {
         writter.writeCharacters(value.toString());
      }
      writter.writeEndElement();

   }
}
