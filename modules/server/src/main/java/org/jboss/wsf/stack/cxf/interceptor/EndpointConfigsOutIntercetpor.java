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

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;
import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.stack.cxf.Loggers;

/**
 * Out Interceptor to write json format endpoint config set result. This interceptor is added to interceptorchain by 
 * @see org.jboss.wsf.stack.cxf.interceptor.EndpointsConfigsPutInterceptor
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class EndpointConfigsOutIntercetpor extends AbstractManagementInterceptor
{
   public static final EndpointConfigsOutIntercetpor INSTANCE = new EndpointConfigsOutIntercetpor();
   public static final String CONFIG_RESULT = EndpointConfigsOutIntercetpor.class.getName() + ".ConfigResult";

   public EndpointConfigsOutIntercetpor()
   {
      super(Phase.PREPARE_SEND);
      this.addAfter(MessageSenderInterceptor.class.getName());
   }

   public void handleMessage(Message message) throws Fault
   {
      String configResult = (String)message.get(CONFIG_RESULT);
      if (configResult == null)
      {
         return;
      }
      message.remove(CONFIG_RESULT);
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

         mappedWriter.writeStartElement("result");
         mappedWriter.writeCharacters(configResult);
         mappedWriter.writeEndElement();

         mappedWriter.writeEndDocument();
         out.flush();
      }
      catch (Exception e)
      {
         String endpointName = message.getExchange().get(Endpoint.class).getShortName();
         WSFException wsfException = org.jboss.wsf.stack.cxf.Messages.MESSAGES.unableToCreateEndpointResultElement(endpointName, e);
         Loggers.INTERCEPTOR_LOGGER.error(wsfException);
         message.put(Message.RESPONSE_CODE, 500);
         PrintWriter outWriter = new PrintWriter(new BufferedOutputStream(out));
         outWriter.write(wsfException.getLocalizedMessage());
         outWriter.flush();
      }
      finally
      {
         StaxUtils.close(mappedWriter);
      }

   }

}
