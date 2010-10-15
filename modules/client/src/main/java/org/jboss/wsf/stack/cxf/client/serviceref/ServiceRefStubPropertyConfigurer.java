/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.jboss.logging.Logger;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedStubPropertyMetaData;

/**
 * A CXF configurer that sets the serviceref data in the JaxWsProxyFactoryBean
 *
 * @author alessio.soldano@jboss.com
 * @since 21-Jul-2009
 */
public class ServiceRefStubPropertyConfigurer implements Configurer
{
   private UnifiedServiceRefMetaData serviceRefMD;
   private Configurer delegate;

   public ServiceRefStubPropertyConfigurer(UnifiedServiceRefMetaData serviceRefMD, Configurer delegate)
   {
      this.serviceRefMD = serviceRefMD;
      this.delegate = delegate;
   }

   public void configureBean(Object beanInstance)
   {
      if (delegate != null)
      {
         delegate.configureBean(beanInstance);
      }
   }

   public void configureBean(String name, Object beanInstance)
   {
      if (name != null && beanInstance instanceof JaxWsProxyFactoryBean)
      {
         QName portQName = null;
         try
         {
            String portName = name.substring(0, name.indexOf(".jaxws-client.proxyFactory"));
            portQName = QName.valueOf(portName);
         }
         catch (Exception e)
         {
            Logger.getLogger(this.getClass()).warn("Unable to retrieve port QName from '" + name + "', trying matching port using endpoint interface name only.");
         }
         configureJaxWsProxyFactoryBean(portQName, (JaxWsProxyFactoryBean)beanInstance);
      }
      if (delegate != null)
      {
         delegate.configureBean(name, beanInstance);
      }
   }
   
   private synchronized void configureJaxWsProxyFactoryBean(QName portQName, JaxWsProxyFactoryBean proxyFactory)
   {
      Class<?> clazz = proxyFactory.getServiceClass();
      UnifiedPortComponentRefMetaData upcmd = serviceRefMD.getPortComponentRef(clazz != null ? clazz.getName() : null, portQName);
      if (upcmd != null)
      {
         setProperties(proxyFactory, upcmd);
         setMTOM((JaxWsServiceFactoryBean)proxyFactory.getServiceFactory(), upcmd);
      }
   }
   
   private void setMTOM(JaxWsServiceFactoryBean serviceFactoryBean, UnifiedPortComponentRefMetaData upcmd)
   {
      if (upcmd.getEnableMTOM())
      {
         List<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
         List<WebServiceFeature> prevFeatures = serviceFactoryBean.getWsFeatures();
         if (prevFeatures != null)
         {
            features.addAll(prevFeatures);
         }
         features.add(new MTOMFeature(true));
         serviceFactoryBean.setWsFeatures(features);
      }
   }
   
   private void setProperties(JaxWsProxyFactoryBean proxyFactory, UnifiedPortComponentRefMetaData upcmd)
   {
      Map<String, Object> properties = proxyFactory.getProperties();
      if (properties == null)
      {
         properties = new HashMap<String, Object>();
         proxyFactory.setProperties(properties);
      }
      for (UnifiedStubPropertyMetaData prop : upcmd.getStubProperties())
      {
         properties.put(prop.getPropName(), prop.getPropValue());
      }
   }
}
