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
package org.jboss.wsf.stack.cxf.client.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.cxf.bus.spring.BusApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * A JBossWS version of @see{org.apache.cxf.bus.spring.BusApplicationContext} that
 * allows for getting the default bus configuration from the JBossWS
 * integration.
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-May-2010
 *
 */
public class JBossWSBusApplicationContext extends BusApplicationContext
{
   private static final String JBWS_INTEGRATION_CXF_CFG_FILE = "META-INF/cxf/jbossws-cxf.xml";

   private static final String JBWS_CXF_EXT_CFG_FILE = "classpath*:META-INF/cxf/cxf-extension-jbossws.xml";

   private static final String DEFAULT_CXF_EXT_CFG_FILE = "classpath*:META-INF/cxf/cxf.extension";

   private final boolean jbwsIncludeDefaults;
   private final boolean ready;

   public JBossWSBusApplicationContext(String[] cf, boolean include, ApplicationContext parent)
   {
      super(cf, false, parent);
      this.jbwsIncludeDefaults = include;
      this.ready = true;
      refresh();
   }
   
   public JBossWSBusApplicationContext(URL[] url, boolean include, ApplicationContext parent)
   {
      super(url, false, parent);
      this.jbwsIncludeDefaults = include;
      this.ready = true;
      refresh();
   }

   @Override
   protected Resource[] getConfigResources()
   {
      List<Resource> resources = new ArrayList<Resource>();
      if (ready)
      {
         if (jbwsIncludeDefaults)
         {
            try
            {
               PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread
                     .currentThread().getContextClassLoader());
               //jbossws-cxf.xml with our integration defaults for the Bus 
               Collections.addAll(resources, resolver.getResources(JBWS_INTEGRATION_CXF_CFG_FILE));
               //cxf-extensions-jbossws.xml, for customers' jbossws additions
               Collections.addAll(resources, resolver.getResources(JBWS_CXF_EXT_CFG_FILE));
               //CXF vanilla extensions, need to load them here as super skips defaults loading
               Resource[] exts = resolver.getResources(DEFAULT_CXF_EXT_CFG_FILE);
               for (Resource r : exts)
               {
                  InputStream is = r.getInputStream();
                  BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                  String line = rd.readLine();
                  while (line != null)
                  {
                     if (!"".equals(line))
                     {
                        resources.add(resolver.getResource(line));
                     }
                     line = rd.readLine();
                  }
                  is.close();
               }

            }
            catch (IOException ex)
            {
               // ignore  
            }
         }
         //recurse to super; this loads everything else the user specified
         Resource[] superResources = super.getConfigResources();
         if (superResources != null)
            Collections.addAll(resources, superResources);
      }
      return resources.isEmpty() ? null : (Resource[]) resources.toArray(new Resource[resources.size()]);
   }
}
