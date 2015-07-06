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

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;
import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.jboss.wsf.spi.management.EndpointMetrics;

/**
 * Out Interceptor to write json format endpoint metrics result. This interceptor is added to interceptorchain by 
 * @see org.jboss.wsf.stack.cxf.interceptor.EndpointMetricsGetInterceptor
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class EndpointMetricsGetOutInterceptor extends AbstractPhaseInterceptor<Message>
{
   public static final EndpointMetricsGetOutInterceptor INSTANCE = new EndpointMetricsGetOutInterceptor();

   public EndpointMetricsGetOutInterceptor()
   {
      super(Phase.PRE_STREAM);
      getAfter().add(StaxOutInterceptor.class.getName());
   }

   public void handleMessage(Message message) throws Fault
   {
      EndpointMetrics metrics = (EndpointMetrics)message.get(EndpointMetricsGetInterceptor.ENDPOINT_METRICS);
      message.remove(EndpointMetricsGetInterceptor.ENDPOINT_METRICS);
      OutputStream out = message.getContent(OutputStream.class);
      if (out == null)
      {
         return;
      }
      message.put(Message.CONTENT_TYPE, "text/xml");
      OutputStreamWriter writer = null;
      AbstractXMLStreamWriter mappedWriter = null;
      try
      {
         writer = new OutputStreamWriter(out, getEncoding(message));
         mappedWriter = new MappedXMLStreamWriter(new MappedNamespaceConvention(), writer);
         mappedWriter.writeStartDocument();
         writeElement(mappedWriter, "requestCount", metrics.getRequestCount());
         writeElement(mappedWriter, "responseCount", metrics.getResponseCount());
         writeElement(mappedWriter, "faultCount", metrics.getFaultCount());
         writeElement(mappedWriter, "totalProcessingTime", metrics.getTotalProcessingTime());
         writeElement(mappedWriter, "maxProcessingTime", metrics.getMaxProcessingTime());
         writeElement(mappedWriter, "minProcessingTime", metrics.getMinProcessingTime());
         writeElement(mappedWriter, "averageProcessingTime", metrics.getAverageProcessingTime());
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

   private void writeElement(AbstractXMLStreamWriter writter, String key, long value) throws Exception
   {
      writter.writeStartElement(key);
      writter.writeCharacters(String.valueOf(value));
      writter.writeEndElement();

   }
   private String getEncoding(Message message)
   {
      Exchange ex = message.getExchange();
      String encoding = (String)message.get(Message.ENCODING);
      if (encoding == null && ex.getInMessage() != null)
      {
         encoding = (String)ex.getInMessage().get(Message.ENCODING);
         message.put(Message.ENCODING, encoding);
      }

      if (encoding == null)
      {
         encoding = "UTF-8";
         message.put(Message.ENCODING, encoding);
      }
      return encoding;
   }

}
