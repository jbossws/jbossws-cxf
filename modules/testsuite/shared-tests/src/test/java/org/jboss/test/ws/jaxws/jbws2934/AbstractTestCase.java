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
package org.jboss.test.ws.jaxws.jbws2934;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.jboss.logging.Logger;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.jboss.arquillian.container.test.api.RunAsClient;

/**
 * [JBWS-2934] WebServiceContext implementation have to be ThreadLocal aware.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public abstract class AbstractTestCase extends JBossWSTest
{
   private static final int THREADS_COUNT = 20;
   private static final int REQUESTS_COUNT = 20;
   private final Endpoint[] proxies = new Endpoint[THREADS_COUNT];
   private final Thread[] threads = new Thread[THREADS_COUNT];
   private final TestJob[] jobs = new TestJob[THREADS_COUNT];
   private final Logger log = Logger.getLogger(this.getClass());
   
   protected abstract String getEndpointAddress();
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName("http://jboss.org/jbws2934", "EndpointService");
      URL wsdlURL = new URL(getEndpointAddress() + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      for (int i = 0; i < THREADS_COUNT; i++) {
         proxies[i] = service.getPort(Endpoint.class);
         //Make the request context threadsafe as we'll be setting the ENDPOINT_ADDRESS_PROPERTY in it;
         //see http://cxf.apache.org/faq.html#FAQ-AreJAX-WSclientproxiesthreadsafe? for more details
         ((BindingProvider)proxies[i]).getRequestContext().put("thread.local.request.context", "true");
      }
   }

   @Test
   @RunAsClient
   public void testEndpointConcurrently() throws Exception
   {
      setUp();
      boolean traceEnabled = log.isTraceEnabled();
      for (int i = 0; i < THREADS_COUNT; i++)
      {
         if (traceEnabled)
            log.debug("Creating thread " + (i + 1));
         jobs[i] = new TestJob(proxies[i], REQUESTS_COUNT, "TestJob" + i, getEndpointAddress());
         threads[i] = new Thread(jobs[i]);
      }
      for (int i = 0; i < THREADS_COUNT; i++)
      {
         if (traceEnabled)
            log.debug("Starting thread " + (i + 1));
         threads[i].start();
      }
      Exception e = null;
      for (int i = 0; i < THREADS_COUNT; i++)
      {
         if (traceEnabled)
            log.debug("Joining thread " + (i + 1));
         threads[i].join();
         if (e == null)
            e = jobs[i].getException();
      }
      if (e != null) throw e;
   }

   private static final class TestJob implements Runnable
   {
      private final String jobName;
      private final Endpoint proxy; 
      private final int countOfRequests;
      private volatile Exception exception;
      private final String endpointAddress; 
      private static final Logger log = Logger.getLogger(TestJob.class);

      TestJob(Endpoint proxy, int countOfRequests, String jobName, String endpointAddress)
      {
         this.proxy = proxy;
         this.countOfRequests = countOfRequests;
         this.jobName = jobName;
         this.endpointAddress = endpointAddress;
      }
      
      public void run()
      {
         try
         {
            boolean traceEnabled = log.isTraceEnabled();
            for (int i = 0; i < this.countOfRequests; i++)
            {
               this.setQueryParameter(proxy, i);
               int retVal = proxy.getQueryParameter(jobName);
               if (traceEnabled)
                  log.trace("Thread=" + this.jobName + ", iteration=" + i);
               if (retVal != (i + 1))
                  throw new RuntimeException("Thread=" + this.jobName + ", iteration=" + i + ", received=" + retVal);
            }
         }
         catch (Exception e)
         {
            log.error("Exception caught: " + e.getMessage());
            this.exception = e;
         }
      }
      
      private void setQueryParameter(Endpoint proxy, int value)
      {
         BindingProvider bp = (BindingProvider)proxy;
         String queryString = "?" + this.jobName + "=" + value;
         bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress + queryString);
      }
      
      Exception getException()
      {
         return this.exception;
      }
   }
}
