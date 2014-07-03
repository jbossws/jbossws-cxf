/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.asynch;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * Asynchronous web services test case (both client and server side async)
 * 
 * @author alessio.soldano@jboss.com
 * @since 21-Jun-2012
 */
public class AsynchTestCase extends JBossWSTest
{
   private String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-samples-asynch";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-asynch.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.cxf\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.asynch.EndpointImpl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/asynch/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(AsynchTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testAsyncEndpoint() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws/cxf/samples/asynch", "EndpointImplService");
      URL wsdlURL = new URL(endpointAddress + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Endpoint proxy = service.getPort(Endpoint.class);
      final String user = "Kermit";
      //do something... then get the result
      assertEquals(user + " (ASYNC)", proxy.echoAsync(user).get());
   }
   
   public void testAsyncEndpointUsingHandler() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws/cxf/samples/asynch", "EndpointImplService");
      URL wsdlURL = new URL(endpointAddress + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Endpoint proxy = service.getPort(Endpoint.class);
      final String user = "Kermit";
      TestAsyncHandler handler = new TestAsyncHandler();
      Future<String> future = proxy.echoAsync(user, handler);
      //do something... then get the result when it's ready
      while (!future.isDone()) {
         Thread.sleep(100);
      }
      assertEquals(user + " (ASYNC)", handler.getResponse());
   }
   
   private class TestAsyncHandler implements AsyncHandler<String> {
      
      private String res;
      
      @Override
      public void handleResponse(Response<String> response) {
         try {
            res = response.get();
         } catch (Exception e) {
            e.printStackTrace();
            res = e.getMessage();
         }
      }
      
      public String getResponse() {
         return res;
      }
   }
}
