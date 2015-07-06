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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.cxf.binding.soap.interceptor.EndpointSelectionInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.jboss.ws.api.monitoring.Record;
import org.jboss.ws.api.monitoring.RecordFilter;
import org.jboss.ws.api.monitoring.RecordProcessor;
import org.jboss.ws.common.monitoring.AndFilter;
import org.jboss.ws.common.monitoring.HostFilter;
import org.jboss.ws.common.monitoring.MemoryBufferRecorder;
import org.jboss.ws.common.monitoring.NotFilter;
import org.jboss.ws.common.monitoring.OperationFilter;
import org.jboss.ws.common.monitoring.OrFilter;
import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * Interceptor to get json format endpoint records. This interceptor is only 
 * responds to get url like http://localhost:8080/context/wsendpoint/management?records&query='sender is localhost'
 *@author <a href="mailto:ema@redhat.com>Jim Ma</a>
 *
 */
public class EndpointRecordGetInterceptor extends AbstractMangementInInterceptor
{

   public static final EndpointRecordGetInterceptor INSTANCE = new EndpointRecordGetInterceptor();
   public static final String RECORDS = EndpointRecordGetInterceptor.class.getName() + ".RECORDS";

   public static final Set<String> httpMethods = new HashSet<String>(4);
   private static final String QUERY_REGEX = "^(sender|operation)\\s(not|is)\\s(\\S*)+(\\s(and|or)\\s(sender|operation)\\s(not|is)\\s\\S*)?(\\s)*$";
   private Interceptor<Message> recordOutInteceptor = EndpointRecordGetOutInterceptor.INSTANCE;
   private static final String TRANSFORM_SKIP = "transform.skip";
   private static final Pattern pattern = Pattern.compile(QUERY_REGEX);
   static
   {
      httpMethods.add("GET");
   }

   public EndpointRecordGetInterceptor()
   {
      super(Phase.READ);
      getAfter().add(EndpointSelectionInterceptor.class.getName());
   }

   public EndpointRecordGetInterceptor(Interceptor<Message> outInterceptor)
   {
      this();
      // Let people override the EndpointConfigsGetOutIntercetpor 
      //configsOutInteceptor = outInterceptor;
   }

   public void handleMessage(Message message) throws Fault
   {
      if (!isAllowed(message))
      {
         return;
      }
      if (isRecognizedQuery(getQueryMap(message)))
      {
         String query = getQueryMap(message).get("query");
         Endpoint endpoint = message.getExchange().get(Endpoint.class);
         List<RecordProcessor> processors = endpoint.getRecordProcessors();
         try
         {
            if (query != null && !isValid(query = URLDecoder.decode(query, this.getEncoding(message)).toLowerCase()))
            {
               //TODO:return a better error message
               return;
            }
         }
         catch (UnsupportedEncodingException e)
         {
           throw new Fault(e);
         }
         MemoryBufferRecorder memRecorder = null;
         for (RecordProcessor processor : processors)
         {
            if (processor.getName().equals("MemoryBufferRecorder"))
            {
               memRecorder = (MemoryBufferRecorder)processor;
               break;
            }
         }
         if (memRecorder == null)
         {
            return;
         }

         List<RecordFilter> filterList = new ArrayList<RecordFilter>();
         if (query != null)
         {
            String[] elements = query.split("\\s(and|or)\\s");
            if (elements.length > 1)
            {
               RecordFilter left = getFilter(elements[0]);
               RecordFilter right = getFilter(elements[1]);
               if (query.contains("and"))
               {
                  filterList.add(new AndFilter(left, right));
               }
               if (query.contains("or"))
               {
                  filterList.add(new OrFilter(left, right));
               }
            }
            else
            {
               filterList.add(getFilter(elements[0]));
            }
         }
         Map<String, List<Record>> records = memRecorder.getMatchingRecords(filterList.toArray(new RecordFilter[] {}));
         Message mout = this.createOutMessage(message);
         mout.put(RECORDS, records);
         mout.put(Message.CONTENT_TYPE, "text/xml");
         // just remove the interceptor which should not be used
         cleanUpOutInterceptors(mout);
         // notice this is being added after the purge above, don't swap the order!

         mout.getInterceptorChain().add(recordOutInteceptor);
         message.getExchange().put(TRANSFORM_SKIP, Boolean.TRUE);
         // skip the service executor and goto the end of the chain.
         message.getInterceptorChain().doInterceptStartingAt(message, OutgoingChainInterceptor.class.getName());

      }
   }

   private RecordFilter getFilter(final String query)
   {
      String[] elements = query.split("\\s(is|not)\\s");
      RecordFilter result = null;
      if (elements[0].equals("sender"))
      {
         result = new HostFilter(elements[1], true);
      }
      else
      {
         result = new OperationFilter(elements[1]);
      }
      if (query.contains("not"))
      {
         result = new NotFilter(result);
      }
      return result;
   }

   private boolean isRecognizedQuery(final Map<String, String> map)
   {
      if (map.containsKey("records") && map.size() == 1)
      {
         return true;
      }
      if (map.size() == 2 && map.containsKey("records") && map.containsKey("query"))
      {
         return true;
      }
      return false;
   }

   @Override
   Set<String> getAllowedMethod()
   {
      return httpMethods;
   }

   private boolean isValid(final String query)
   {
      if (query == null || query.length() == 0)
      {
         return false;
      }
      return pattern.matcher(query).find();
   }
}
