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
package org.jboss.wsf.stack.cxf.client;

import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.Executor;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.spi.ServiceDelegate;

import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedStubPropertyMetaData;

/**
 * ServiceDelegate that propagates service ref stub properties
 *
 * @author richard.opalka@jboss.com
 */
public final class ServiceRefStubPropertyServiceDelegate extends ServiceDelegate
{

   private ServiceDelegate delegate;
   private UnifiedServiceRefMetaData serviceRefMD;
   
   ServiceRefStubPropertyServiceDelegate(ServiceDelegate delegate, UnifiedServiceRefMetaData serviceRefMD)
   {
      this.delegate = delegate;
      this.serviceRefMD = serviceRefMD;
   }

   private <T> T propagateProps(T proxy, Class<T> serviceEndpointInterface)
   {
      for (UnifiedPortComponentRefMetaData pcRef : serviceRefMD.getPortComponentRefs())
      {
         String sei = pcRef.getServiceEndpointInterface();
         if (sei != null && sei.equals(serviceEndpointInterface.getName()) && proxy instanceof BindingProvider)
         {
            BindingProvider bp = (BindingProvider)proxy;
            for (UnifiedStubPropertyMetaData prop : pcRef.getStubProperties())
            {
               bp.getRequestContext().put(prop.getPropName(), prop.getPropValue());
            }
         }
      }
      return proxy;
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#addPort(javax.xml.namespace.QName, java.lang.String, java.lang.String)
    */
   @Override
   public void addPort(QName portName, String bindingId, String endpointAddress)
   {
      this.delegate.addPort(portName, bindingId, endpointAddress);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#createDispatch(javax.xml.ws.EndpointReference, java.lang.Class, javax.xml.ws.Service.Mode, javax.xml.ws.WebServiceFeature[])
    */
   @Override
   public <T> Dispatch<T> createDispatch(EndpointReference endpointReference, Class<T> type, Mode mode, WebServiceFeature... features)
   {
      return this.delegate.createDispatch(endpointReference, type, mode, features);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#createDispatch(javax.xml.ws.EndpointReference, javax.xml.bind.JAXBContext, javax.xml.ws.Service.Mode, javax.xml.ws.WebServiceFeature[])
    */
   @Override
   public Dispatch<Object> createDispatch(EndpointReference endpointReference, JAXBContext context, Mode mode, WebServiceFeature... features)
   {
      return this.delegate.createDispatch(endpointReference, context, mode, features);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#createDispatch(javax.xml.namespace.QName, java.lang.Class, javax.xml.ws.Service.Mode, javax.xml.ws.WebServiceFeature[])
    */
   @Override
   public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode, WebServiceFeature... features)
   {
      return this.delegate.createDispatch(portName, type, mode, features);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#createDispatch(javax.xml.namespace.QName, java.lang.Class, javax.xml.ws.Service.Mode)
    */
   @Override
   public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode)
   {
      return this.delegate.createDispatch(portName, type, mode);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#createDispatch(javax.xml.namespace.QName, javax.xml.bind.JAXBContext, javax.xml.ws.Service.Mode, javax.xml.ws.WebServiceFeature[])
    */
   @Override
   public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Mode mode, WebServiceFeature... features)
   {
      return this.delegate.createDispatch(portName, context, mode, features);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#createDispatch(javax.xml.namespace.QName, javax.xml.bind.JAXBContext, javax.xml.ws.Service.Mode)
    */
   @Override
   public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Mode mode)
   {
      return this.delegate.createDispatch(portName, context, mode);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getExecutor()
    */
   @Override
   public Executor getExecutor()
   {
      return this.delegate.getExecutor();
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getHandlerResolver()
    */
   @Override
   public HandlerResolver getHandlerResolver()
   {
      return this.delegate.getHandlerResolver();
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getPort(java.lang.Class, javax.xml.ws.WebServiceFeature[])
    */
   @Override
   public <T> T getPort(Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      return this.propagateProps(this.delegate.getPort(serviceEndpointInterface, features), serviceEndpointInterface);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getPort(java.lang.Class)
    */
   @Override
   public <T> T getPort(Class<T> serviceEndpointInterface)
   {
      return this.propagateProps(this.delegate.getPort(serviceEndpointInterface), serviceEndpointInterface);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getPort(javax.xml.ws.EndpointReference, java.lang.Class, javax.xml.ws.WebServiceFeature[])
    */
   @Override
   public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      return this.propagateProps(this.delegate.getPort(endpointReference, serviceEndpointInterface, features), serviceEndpointInterface);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getPort(javax.xml.namespace.QName, java.lang.Class, javax.xml.ws.WebServiceFeature[])
    */
   @Override
   public <T> T getPort(QName portName, Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      return this.propagateProps(this.delegate.getPort(portName, serviceEndpointInterface, features), serviceEndpointInterface);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getPort(javax.xml.namespace.QName, java.lang.Class)
    */
   @Override
   public <T> T getPort(QName portName, Class<T> serviceEndpointInterface)
   {
      return this.propagateProps(this.delegate.getPort(portName, serviceEndpointInterface), serviceEndpointInterface);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getPorts()
    */
   @Override
   public Iterator<QName> getPorts()
   {
      return this.delegate.getPorts();
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getServiceName()
    */
   @Override
   public QName getServiceName()
   {
      return this.delegate.getServiceName();
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#getWSDLDocumentLocation()
    */
   @Override
   public URL getWSDLDocumentLocation()
   {
      return this.delegate.getWSDLDocumentLocation();
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#setExecutor(java.util.concurrent.Executor)
    */
   @Override
   public void setExecutor(Executor executor)
   {
      this.delegate.setExecutor(executor);
   }

   /* (non-Javadoc)
    * @see javax.xml.ws.spi.ServiceDelegate#setHandlerResolver(javax.xml.ws.handler.HandlerResolver)
    */
   @Override
   public void setHandlerResolver(HandlerResolver handlerResolver)
   {
      this.delegate.setHandlerResolver(handlerResolver);
   }

}
