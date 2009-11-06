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
package org.jboss.wsf.stack.cxf;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.endpoint.ServerRegistry;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.management.InstrumentationManager;
import org.apache.cxf.management.counters.CounterRepository;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.jms.AddressType;
import org.apache.cxf.transport.jms.JMSDestination;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.transport.servlet.ServletController;
import org.apache.cxf.transport.servlet.ServletTransportFactory;
import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.wsf.common.ObjectNameFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.binding.BindingCustomization;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSCXFConfigurer;
import org.jboss.wsf.stack.cxf.management.InstrumentationManagerExtImpl;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

/**
 * An extension to the CXF servlet
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 21-Apr-2007
 */
public class CXFServletExt extends CXFServlet
{
   public static final String PARAM_CXF_BEANS_URL = "jbossws.cxf.beans.url";
   public static final String ENABLE_CXF_MANAGEMENT = "enable.cxf.management";  
   public static final String JMS_NS = "http://cxf.apache.org/transports/jms";
   
   private static Logger log = Logger.getLogger(CXFServletExt.class);

   protected Endpoint endpoint;
   protected EndpointRegistry epRegistry;
   protected GenericApplicationContext childCtx;

   @Override
   public void init(ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);
   }

   @Override
   public ServletController createServletController(ServletConfig servletConfig)
   {
      ServletTransportFactory stf = (ServletTransportFactory)createServletTransportFactory();
      return new ServletControllerExt(stf, servletConfig, servletConfig.getServletContext(), bus);
   }

   @Override
   public void loadBus(ServletConfig servletConfig) throws ServletException
   {
      super.loadBus(servletConfig);

      ServletContext svCtx = getServletContext();
      ApplicationContext appCtx = (ApplicationContext)svCtx.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT");

      Bus bus = getBus();

      //register the InstrumentManagementImpl
      if (svCtx.getInitParameter(ENABLE_CXF_MANAGEMENT) != null && "true".equalsIgnoreCase((String)svCtx.getInitParameter(ENABLE_CXF_MANAGEMENT))) {
         registerInstrumentManger(bus);
      }

      //Install our SoapTransportFactory to allow for proper soap address rewrite
      DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
      SoapTransportFactory factory = new SoapTransportFactoryExt();
      factory.setBus(bus);
      dfm.registerDestinationFactory(Constants.NS_SOAP11, factory);
      dfm.registerDestinationFactory(Constants.NS_SOAP12, factory);

      //Init the Endpoint
      initEndpoint(servletConfig);

      //Load additional configurations
      loadAdditionalConfigExt(appCtx, servletConfig);      
      correctJmsEndpointAddress(endpoint, bus);
   }

   private void initEndpoint(ServletConfig servletConfig)
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      epRegistry = spiProvider.getSPI(EndpointRegistryFactory.class).getEndpointRegistry();

      ServletContext context = servletConfig.getServletContext();
      String contextPath = context.getContextPath();
      endpoint = initServiceEndpoint(contextPath);
      //Add customization check to use default CXF configurer
      BindingCustomization customization = endpoint.getAttachment(BindingCustomization.class);
      if (customization != null) {
         //Add extension to configure server beans according to JBossWS customizations
         JBossWSCXFConfigurer jbosswsConfigurer = new JBossWSCXFConfigurer(bus.getExtension(Configurer.class));
         jbosswsConfigurer.setBindingCustomization(endpoint.getAttachment(BindingCustomization.class));
         bus.setExtension(jbosswsConfigurer, Configurer.class);
      }
      context.setAttribute(ServletController.class.getName(), getController());
   }

   private void loadAdditionalConfigExt(ApplicationContext ctx, ServletConfig servletConfig) throws ServletException
   {
      //Load configuration 
      String location = servletConfig.getServletContext().getInitParameter(PARAM_CXF_BEANS_URL);
      if (location != null)
      {
         InputStream is;
         try
         {
            is = new URL(location).openStream();
         }
         catch (IOException e)
         {
            throw new ServletException(e);
         }

         childCtx = new GenericApplicationContext(ctx);
         XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(childCtx);
         reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
         reader.loadBeanDefinitions(new InputStreamResource(is, location));

         childCtx.refresh();
      }
   }

   @Override
   protected void invoke(HttpServletRequest req, HttpServletResponse res) throws ServletException
   {
      try
      {
         BusFactory.setThreadDefaultBus(getBus());
         EndpointAssociation.setEndpoint(endpoint);
         RequestHandler requestHandler = (RequestHandler)endpoint.getRequestHandler();
         requestHandler.handleHttpRequest(endpoint, req, res, getServletContext());
      }
      catch (IOException ioe)
      {
         throw new ServletException(ioe);
      }
      finally
      {
         EndpointAssociation.removeEndpoint();
         BusFactory.setThreadDefaultBus(null);
      }
   }

   @Override
   public void destroy()
   {
      if (childCtx != null)
         childCtx.destroy();

      super.destroy();
   }

   /** Initialize the service endpoint
    */
   private Endpoint initServiceEndpoint(String contextPath)
   {
      if (contextPath.startsWith("/"))
         contextPath = contextPath.substring(1);

      Endpoint endpoint = null;
      String servletName = getServletName();
      for (ObjectName sepId : epRegistry.getEndpoints())
      {
         String propContext = sepId.getKeyProperty(Endpoint.SEPID_PROPERTY_CONTEXT);
         String propEndpoint = sepId.getKeyProperty(Endpoint.SEPID_PROPERTY_ENDPOINT);
         if (servletName.equals(propEndpoint) && contextPath.equals(propContext))
         {
            endpoint = epRegistry.getEndpoint(sepId);
            break;
         }
      }

      if (endpoint == null)
      {
         ObjectName oname = ObjectNameFactory.create(Endpoint.SEPID_DOMAIN + ":" + Endpoint.SEPID_PROPERTY_CONTEXT + "=" + contextPath + ","
               + Endpoint.SEPID_PROPERTY_ENDPOINT + "=" + servletName);
         throw new WebServiceException("Cannot obtain endpoint for: " + oname);
      }

      return endpoint;
   }

   private void registerInstrumentManger(Bus bus) throws ServletException
   {
      InstrumentationManagerExtImpl instrumentationManagerImpl = new InstrumentationManagerExtImpl();
      instrumentationManagerImpl.setBus(bus);
      instrumentationManagerImpl.setEnabled(true);
      instrumentationManagerImpl.initMBeanServer();
      instrumentationManagerImpl.register();
      bus.setExtension(instrumentationManagerImpl, InstrumentationManager.class);

      //attach couterRepository
      CounterRepository couterRepository = new CounterRepository();
      couterRepository.setBus(bus);

      try
      {
         Method method = CounterRepository.class.getDeclaredMethod("registerInterceptorsToBus", new Class[] {});
         method.setAccessible(true);
         method.invoke(couterRepository, new Object[] {});
      }
      catch (Exception e)
      {
         throw new ServletException(e);
      }
   }
   
   private void correctJmsEndpointAddress(Endpoint endpoint, Bus bus) throws ServletException {
     for (Server server : bus.getExtension(ServerRegistry.class).getServers()) {
        if (server.getEndpoint() instanceof JaxWsEndpointImpl) {
           JaxWsEndpointImpl endpointImpl = (JaxWsEndpointImpl)server.getEndpoint();
         try
         {
            Field field = JaxWsEndpointImpl.class.getDeclaredField("implInfo");
            field.setAccessible(true);
            Object object = field.get(endpointImpl);
            if (object != null) {
               JaxWsImplementorInfo implementInfo = (JaxWsImplementorInfo)object;
               Class endpointClass = implementInfo.getImplementorClass();
               if (endpoint.getTargetBeanClass().getName().equals(endpointClass.getName()) 
                     && JMS_NS.equals(server.getEndpoint().getEndpointInfo().getTransportId())) {
                     //server.getDestination().getAddress()
                     AddressType address = server.getEndpoint().getEndpointInfo().getExtensor(AddressType.class);
                     if (address == null) 
                     {
                         //java first : the address is from jbossws-cxf.xml
                         JMSDestination jmsDestination = (JMSDestination)server.getDestination();
                         String url = "jms://" + jmsDestination.getJmsConfig().getTargetDestination();
                         if (jmsDestination.getJmsConfig().getReplyDestination() != null) {
                            url = url + "?replyToName=" + jmsDestination.getJmsConfig().getReplyDestination();
                            endpoint.setAddress(url);
                         }
          
                     } else 
                     {
                        //wsdl first: the address is from wsdl
                        String jmsURL = "jms://" + address.getJndiDestinationName();
                        if (address.getJndiReplyDestinationName() != null)
                        {
                           jmsURL = jmsURL + "?replyToName=" + address.getJndiReplyDestinationName();
                           endpoint.setAddress(jmsURL);
                        }
                     }
               }
                 
            }
         }
         catch (Exception e)
         {
            throw new ServletException(e);
         }           
        }
     }
   }
}
