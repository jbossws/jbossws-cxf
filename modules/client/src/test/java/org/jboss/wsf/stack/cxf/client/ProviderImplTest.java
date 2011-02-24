/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.spi.Provider;

import junit.framework.TestCase;

import org.jboss.wsf.stack.cxf.client.ProviderImpl.DelegateClassLoader;
import org.jboss.wsf.stack.cxf.client.ProviderImpl.DelegateEndpointImpl;
import org.w3c.dom.Element;

/**
 * A test case for jbossws-cxf javax.xml.ws.spi.Provider implementation
 * 
 * @author alessio.soldano@jboss.com
 * @since 24-Feb-2011
 * 
 */
public class ProviderImplTest extends TestCase
{
   public void testGetProvider()
   {
      //just check we get the jbossws-cxf provider impl when the default maven tccl is set
      Provider providerImpl = Provider.provider();
      assertTrue(providerImpl instanceof ProviderImpl);
   }
   
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
      public TestClassLoader()
      {
         super(null);
      }
      
      @Override
      public Class<?> loadClass(final String className) throws ClassNotFoundException
      {
         throw new ClassNotFoundException("TestClassLoader does not load anything!");
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
