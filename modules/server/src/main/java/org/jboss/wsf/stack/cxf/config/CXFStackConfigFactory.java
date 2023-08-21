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
package org.jboss.wsf.stack.cxf.config;

import static org.jboss.wsf.stack.cxf.i18n.Loggers.ROOT_LOGGER;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.cxf.Bus;
import org.apache.wss4j.common.crypto.WSProviderConfig;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.StackConfig;
import org.jboss.wsf.spi.management.StackConfigFactory;
import org.jboss.wsf.stack.cxf.addressRewrite.SoapAddressRewriteHelper;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-May-2009
 *
 */
public class CXFStackConfigFactory extends StackConfigFactory
{
   @Override
   public StackConfig getStackConfig()
   {
      return new CXFStackConfig();
   }
}

class CXFStackConfig implements StackConfig
{
   public CXFStackConfig()
   {
      final ClassLoader orig = getContextClassLoader();
      //try early configuration of xmlsec engine through WSS4J:
      //* to avoid doing this later when the TCCL won't have visibility over the xmlsec internals
      //* to make sure any ws client will also have full xmlsec functionalities setup (BC enabled, etc.)
      try
      {
         setContextClassLoader(ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader());
         WSProviderConfig.init(true, false, true);
      }
      catch (Exception e)
      {
         ROOT_LOGGER.couldNotInitSecurityEngine();
         ROOT_LOGGER.errorGettingWSSConfig(e);
      }
      finally
      {
         setContextClassLoader(orig);
      }
   }

   //hack the 2 methods below to make the logs show something like
   // "JBossWS 5.1.1.Final (Apache CXF 3.1.4)"
   
   public String getImplementationTitle()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("JBossWS ");
      sb.append(getClass().getPackage().getImplementationVersion());
      sb.append(" (Apache CXF ");
      sb.append(Bus.class.getPackage().getImplementationVersion());
      sb.append(")");
      return sb.toString();
   }

   public String getImplementationVersion()
   {
      return "";
   }
   
   /**
    * Get context classloader.
    * 
    * @return the current context classloader
    */
   private static ClassLoader getContextClassLoader()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
   }
   
   /**
    * Set context classloader.
    *
    * @param classLoader the classloader
    */
   private static void setContextClassLoader(final ClassLoader classLoader)
   {
      if (System.getSecurityManager() == null)
      {
         Thread.currentThread().setContextClassLoader(classLoader);
      }
      else
      {
         AccessController.doPrivileged(new PrivilegedAction<Object>()
         {
            public Object run()
            {
               Thread.currentThread().setContextClassLoader(classLoader);
               return null;
            }
         });
      }
   }

   @Override
   public void validatePathRewriteRule(String rule)
   {
      SoapAddressRewriteHelper.validatePathRewriteRule(rule);
   }
}