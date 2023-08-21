/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.wsf.stack.cxf.client;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.transform.Source;
import jakarta.xml.ws.Binding;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.spi.Provider;

import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.stack.cxf.client.ProviderImpl.DelegateEndpointImpl;
import org.junit.Test;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test case for jbossws-cxf jakarta.xml.ws.spi.Provider implementation
 * 
 * @author alessio.soldano@jboss.com
 * @since 24-Feb-2011
 * 
 */
public class ProviderImplTest
{
   @Test
   public void testGetProvider()
   {
      //just check we get the jbossws-cxf provider impl when the default maven tccl is set
      Provider providerImpl = Provider.provider();
      assertTrue(providerImpl instanceof ProviderImpl);
   }

   @Test
   public void testGetProviderWithCustomClassLoaderIsBackwardCompatibility()
   {
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      try
      {
         //overwrite the TCCL so that no Provider configuration is specified,
         //hence the JAXWS RI default is tried (given this test is run
         //out of container); this verifies the additions due to JBEE-75 and
         //JBWS-3223 are backward compatible.
         TestClassLoader cl = new TestClassLoader();
         Thread.currentThread().setContextClassLoader(cl);
         try
         {
            Provider.provider();
            fail("Exception due to class not found expected!");
         }
         catch (Exception e)
         {
            //check the default ProviderImpl was being looked up given no configuration was provided
            List<String> list = cl.getLoadClassRequests();
            assertTrue(list.contains("com.sun.xml.internal.ws.spi.ProviderImpl"));
            assertEquals(1, list.size());
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(tccl);
      }
   }
   
   /**
    * This verifies that the ProviderImpl::checkAndFixContextClassLoader does not
    * affect the current TCCL when the ProviderImpl class can be loaded and
    * created an instance of by that classloader.
    */
   @Test
   public void testCheckAndFixContextClassLoaderWithDefaultMavenClassLoader()
   {
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      try
      {
         assertNotNull(tccl);
         assertFalse(ProviderImpl.checkAndFixContextClassLoader(tccl));
         verifyClassLoaderCanLoadProviderImpl(tccl);
         assertEquals(tccl, Thread.currentThread().getContextClassLoader());
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(tccl);
      }
   }
   
   /**
    * This verifies that the ProviderImpl::checkAndFixContextClassLoader sets
    * a new DelegateClassLoader (able to load ProviderImpl) as TCCL when the
    * TCCL is not set.
    */
   @Test
   public void testCheckAndFixContextClassLoaderWithNullClassLoader()
   {
      ClassLoader orig = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(null);
         ClassLoader tccl = Thread.currentThread().getContextClassLoader();
         assertTrue(ProviderImpl.checkAndFixContextClassLoader(tccl));
         ClassLoader modifiedTccl = Thread.currentThread().getContextClassLoader();
         assertNotNull(modifiedTccl);
         assertTrue(modifiedTccl instanceof DelegateClassLoader);
         verifyClassLoaderCanLoadProviderImpl(modifiedTccl);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(orig);
      }
   }
   
   /**
    * This verifies that the ProviderImpl::checkAndFixContextClassLoader sets
    * a new DelegateClassLoader (able to load ProviderImpl) as TCCL when the
    * ProviderImpl class can't be loaded and created an instance of by that TCCL
    */
   @Test
   public void testCheckAndFixContextClassLoaderWithTestClassLoader()
   {
      ClassLoader orig = Thread.currentThread().getContextClassLoader();
      try
      {
         ClassLoader testCl = new TestClassLoader();
         assertFalse(canLoad(testCl, ProviderImpl.class.getName()));
         Thread.currentThread().setContextClassLoader(testCl);
         assertTrue(ProviderImpl.checkAndFixContextClassLoader(testCl));
         ClassLoader modifiedTccl = Thread.currentThread().getContextClassLoader();
         assertNotNull(modifiedTccl);
         assertTrue(modifiedTccl instanceof DelegateClassLoader);
         verifyClassLoaderCanLoadProviderImpl(modifiedTccl);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(orig);
      }
   }
   
   /**
    * This verifies that DelegateEndpointImpl delegates to the Apache
    * CXF EndpointImpl after having properly setup the TCCL so that it
    * can load and create instances of the ProviderImpl class.
    */
   @Test
   public void testEndpointImplPublishCorrectlySetsTCCL()
   {
      ClassLoader orig = Thread.currentThread().getContextClassLoader();
      try
      {
         Endpoint ep1 = new DelegateEndpointImpl(new TestEndpoint());
         ep1.publish(new Integer(1)); //just to ensure the publish(Object arg) is used
         ep1.publish("foo");
         Thread.currentThread().setContextClassLoader(new TestClassLoader());
         Endpoint ep2 = new DelegateEndpointImpl(new TestEndpoint());
         ep2.publish(new Integer(1)); //just to ensure the publish(Object arg) is used
         ep2.publish("foo");
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(orig);
      }
   }
   
   private static boolean canLoad(ClassLoader cl, String className)
   {
      try
      {
         cl.loadClass(className);
         return true;
      }
      catch (Exception e)
      {
         return false;
      }
   }
   
   private static void verifyClassLoaderCanLoadProviderImpl(ClassLoader cl)
   {
      final String impl = ProviderImpl.class.getName();
      boolean canLoad = canLoad(cl, impl);
      assertTrue("ClassLoader " + cl + " was not able to load " + impl + "!", canLoad);
   }
   
   //------ Test classes ------
   
   private static final class TestClassLoader extends ClassLoader
   {
      private List<String> loadClassRequests = new LinkedList<String>();
      
      public TestClassLoader()
      {
         super(null);
      }
      
      @Override
      public Class<?> loadClass(final String className) throws ClassNotFoundException
      {
         loadClassRequests.add(className);
         throw new ClassNotFoundException("TestClassLoader does not load anything!");
      }
      
      public List<String> getLoadClassRequests()
      {
         return this.loadClassRequests;
      }
   }
   
   private static final class TestEndpoint extends Endpoint
   {
      @Override
      public Binding getBinding()
      {
         return null;
      }

      @Override
      public Object getImplementor()
      {
         return null;
      }

      @Override
      public void publish(String address)
      {
         verifyClassLoaderCanLoadProviderImpl(Thread.currentThread().getContextClassLoader());
      }

      @Override
      public void publish(Object serverContext)
      {
         verifyClassLoaderCanLoadProviderImpl(Thread.currentThread().getContextClassLoader());
      }

      @Override
      public void stop()
      {
      }

      @Override
      public boolean isPublished()
      {
         return false;
      }

      @Override
      public List<Source> getMetadata()
      {
         return null;
      }

      @Override
      public void setMetadata(List<Source> metadata)
      {
      }

      @Override
      public Executor getExecutor()
      {
         return null;
      }

      @Override
      public void setExecutor(Executor executor)
      {
      }

      @Override
      public Map<String, Object> getProperties()
      {
         return null;
      }

      @Override
      public void setProperties(Map<String, Object> properties)
      {
      }

      @Override
      public EndpointReference getEndpointReference(Element... referenceParameters)
      {
         return null;
      }

      @Override
      public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters)
      {
         return null;
      }
   }
}
