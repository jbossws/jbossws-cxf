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
package org.jboss.wsf.stack.cxf.client.serviceref;

import java.net.URL;

import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.configuration.Configurer;
import org.jboss.ws.common.serviceref.AbstractServiceObjectFactoryJAXWS;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSSpringBusFactory;

/**
 * {@inheritDoc}
 *
 * @author Thomas.Diesler@jboss.com
 * @author Richard.Opalka@jboss.com
 * @author alessio.soldano@jboss.com
 */
public final class CXFServiceObjectFactoryJAXWS extends AbstractServiceObjectFactoryJAXWS
{
   private static final ThreadLocal<Bus> busAssociation = new ThreadLocal<Bus>();

   @Override
   protected void init(final UnifiedServiceRefMetaData serviceRefUMDM)
   {
      BusFactory.setThreadDefaultBus(null);
      busAssociation.set(this.createNewBus(serviceRefUMDM));
   }

   @Override
   protected void configure(final UnifiedServiceRefMetaData serviceRefUMDM, final Service service)
   {
      if (serviceRefUMDM.getHandlerChain() != null)
      {
         service.setHandlerResolver(new CXFHandlerResolverImpl(busAssociation.get(), serviceRefUMDM.getHandlerChain(),
               service.getClass()));
      }
   }

   @Override
   protected void destroy(final UnifiedServiceRefMetaData serviceRefUMDM)
   {
      busAssociation.set(null);
      BusFactory.setThreadDefaultBus(null);
   }

   private Bus createNewBus(final UnifiedServiceRefMetaData serviceRefMD)
   {
      final Bus bus;
      final URL cxfConfig = this.getCXFConfiguration(serviceRefMD.getVfsRoot());
      if (cxfConfig != null)
      {
         final SpringBusFactory busFactory = new JBossWSSpringBusFactory();
         bus = busFactory.createBus(cxfConfig);
         BusFactory.setThreadDefaultBus(bus);
      }
      else
      {
         Bus threadBus = BusFactory.getThreadDefaultBus(false);
         bus = threadBus != null ? threadBus : BusFactory.newInstance().createBus();
      }

      Configurer configurer = bus.getExtension(Configurer.class);
      bus.setExtension(new CXFServiceRefStubPropertyConfigurer(serviceRefMD, configurer), Configurer.class);

      return bus;
   }

   private URL getCXFConfiguration(final UnifiedVirtualFile vfsRoot)
   {
      URL url = null;
      try
      {
         url = vfsRoot.findChild("WEB-INF/jbossws-cxf.xml").toURL();
      }
      catch (Exception e)
      {
      }

      if (url == null)
      {
         try
         {
            url = vfsRoot.findChild("META-INF/jbossws-cxf.xml").toURL();
         }
         catch (Exception e)
         {
         }
      }
      return url;
   }
}
