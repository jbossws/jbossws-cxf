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
package org.jboss.test.ws.jaxws.cxf.gzip;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Sep-2010
 *
 */
public class GZIPTestCase extends JBossWSTest
{
   private String gzipFeatureEndpointURL = "http://" + getServerHost() + ":8080/jaxws-cxf-gzip/HelloWorldService/HelloWorldImpl";
   
   private Helper helper;

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(GZIPTestCase.class, DeploymentArchives.SERVER);
   }
   
   private Helper getHelper()
   {
      if (helper == null)
      {
         helper = new Helper(gzipFeatureEndpointURL);
      }
      return helper;
   }
   
   public void testGZIPUsingFeatureOnBus() throws Exception
   {
      assertTrue(getHelper().testGZIPUsingFeatureOnBus());
   }
   
   public void testGZIPUsingFeatureOnClient() throws Exception
   {
      assertTrue(getHelper().testGZIPUsingFeatureOnClient());
   }
   
   public void testGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      assertTrue(getHelper().testGZIPServerSideOnlyInterceptorOnClient());
   }
   
   public void testFailureGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      assertTrue(getHelper().testFailureGZIPServerSideOnlyInterceptorOnClient());
   }
   
   public void testGZIPServerSideOnlyInterceptorsOnBus() throws Exception
   {
      assertTrue(getHelper().testGZIPServerSideOnlyInterceptorsOnBus());
   }

   public void testFailureGZIPServerSideOnlyInterceptorsOnBus() throws Exception
   {
      assertTrue(getHelper().testFailureGZIPServerSideOnlyInterceptorsOnBus());
   }
}
