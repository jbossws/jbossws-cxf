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
package org.jboss.wsf.stack.cxf.metadata;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.JavaUtils;
import org.jboss.wsf.spi.annotation.EndpointConfig;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.metadata.config.ConfigMetaDataParser;
import org.jboss.wsf.spi.metadata.config.ConfigRoot;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainsMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;
import org.jboss.wsf.spi.metadata.webservices.PortComponentMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebserviceDescriptionMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebservicesFactory;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.jboss.wsf.stack.cxf.metadata.services.DDEndpoint;

/**
 * Builds the DDBeans metadata used for generating the jboss-cxf.xml deployment descriptor
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Apr-2010
 *
 */
public class MetadataBuilder
{
   private static final Logger log = Logger.getLogger(MetadataBuilder.class);
   
   public MetadataBuilder()
   {
      
   }
   
   public DDBeans build(Deployment dep, String invokerEJB3, String invokerJSE)
   {
      DeploymentType depType = dep.getType();
      DDBeans dd = new DDBeans();
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         DDEndpoint ddep = createDDEndpoint(ep.getTargetBeanClass(), (ArchiveDeployment)dep, ep);

         if (depType == DeploymentType.JAXWS_EJB3)
         {
            ddep.setInvoker(invokerEJB3);
         }

         if (depType == DeploymentType.JAXWS_JSE)
         {
            ddep.setInvoker(invokerJSE);
         }

         processWSDDContribution(ddep, (ArchiveDeployment)dep);

         log.info("Add " + ddep);
         dd.addEndpoint(ddep);
      }
      return dd;
   }
   
   protected boolean isMtomEnabled(Class<?> beanClass)
   {
      BindingType bindingType = (BindingType)beanClass.getAnnotation(BindingType.class);
      MTOM mtom = (MTOM)beanClass.getAnnotation(MTOM.class);

      boolean mtomEnabled = mtom != null;
      if (!mtomEnabled && bindingType != null)
      {
         String binding = bindingType.value();
         mtomEnabled = binding.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING) || binding.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING);
      }
      
      return mtomEnabled;
   }
   
   private void processWSDDContribution(DDEndpoint endpoint, ArchiveDeployment dep)
   {
      WebservicesMetaData webservices = WebservicesFactory.loadFromVFSRoot(dep.getRootFile());
      if (webservices != null)
      {
         for (WebserviceDescriptionMetaData wsDesc : webservices.getWebserviceDescriptions())
         {
            for (PortComponentMetaData portComp : wsDesc.getPortComponents())
            {
               // We match portComp's by SEI first and portQName second
               // In the first case the portComp may override the portQName that derives from the annotation
               String portCompSEI = portComp.getServiceEndpointInterface();
               boolean doesMatch = portCompSEI != null ? portCompSEI.equals(endpoint.getEpClass().getName()) : false;
               if (!doesMatch)
               {
                  doesMatch = portComp.getWsdlPort().equals(endpoint.getPortName());
               }

               if (doesMatch)
               {
                  // PortQName overrides
                  if (portComp.getWsdlPort() != null)
                  {
                     if (log.isDebugEnabled())
                        log.debug("Override portName " + endpoint.getPortName() + " with " + portComp.getWsdlPort());
                     endpoint.setPortName(portComp.getWsdlPort());
                  }
                  //ServiceQName overrides
                  if (portComp.getWsdlService() != null)
                  {
                     if (log.isDebugEnabled())
                        log.debug("Override serviceName " + endpoint.getServiceName() + " with " + portComp.getWsdlService());
                     endpoint.setServiceName(portComp.getWsdlService());
                  }
                  
                  //HandlerChain contributions
                  UnifiedHandlerChainsMetaData chainWrapper = portComp.getHandlerChains();
                  if (chainWrapper != null)
                  {
                     endpoint.setHandlers(convertEndpointHandlers(chainWrapper.getHandlerChains()));
                  }

                  // MTOM settings
                  if (portComp.isMtomEnabled())
                  {
                     log.debug("Enabling MTOM");
                     endpoint.setMtomEnabled(true);
                     endpoint.setMtomThreshold(portComp.getMtomThreshold());
                  }
                  
                  //Addressing
                  if (portComp.isAddressingEnabled()) 
                  {
                      log.debug("Enabling Addressing");
                      endpoint.setAddressingEnabled(true);
                      endpoint.setAddressingRequired(portComp.isAddressingRequired());
                      endpoint.setAddressingResponses(portComp.getAddressingResponses());
                  }
                  //RespectBinding
                  if (portComp.isRespectBindingEnabled()) 
                  {
                      log.debug("RepectBinging...");
                      endpoint.setRespectBindingEnabled(true);
                  }
                  //wsdlLocation override
                  String wsdlFile = portComp.getWebserviceDescription().getWsdlFile();
                  if (wsdlFile != null)
                  {
                     if (log.isDebugEnabled())
                        log.debug("Override wsdlFile location with " + wsdlFile);
                     endpoint.setWsdlLocation(wsdlFile);
                  }
               }
            }
         }
      }
   }
   
   private List<String> convertEndpointHandlers(List<UnifiedHandlerChainMetaData> handlerChains)
   {
      List<String> handlers = new LinkedList<String>();
      if (handlerChains != null)
      {
         for (UnifiedHandlerChainMetaData handlerChain : handlerChains)
         {
            if (handlerChain.getPortNamePattern() != null || handlerChain.getProtocolBindings() != null
                  || handlerChain.getServiceNamePattern() != null)
            {
               log.warn("PortNamePattern, ServiceNamePattern and ProtocolBindings filters not supported; adding handlers anyway.");
            }
            for (UnifiedHandlerMetaData uhmd : handlerChain.getHandlers())
            {
               if (log.isDebugEnabled())
                  log.debug("Contribute handler from webservices.xml: " + uhmd.getHandlerName());
               if (uhmd.getInitParams() != null && !uhmd.getInitParams().isEmpty())
               {
                  log.warn("Init params not supported.");
               }
               handlers.add(uhmd.getHandlerClass());
            }
         }
      }
      return handlers;
   }
   
   protected DDEndpoint createDDEndpoint(Class<?> sepClass, ArchiveDeployment dep, Endpoint ep)
   {
      WebService anWebService = sepClass.getAnnotation(WebService.class);
      WebServiceProvider anWebServiceProvider = sepClass.getAnnotation(WebServiceProvider.class);
      
      Class<?> seiClass = null;
      String seiName;

      String name = (anWebService != null) ? anWebService.name() : "";
      if (name.length() == 0)
         name = JavaUtils.getJustClassName(sepClass);

      String serviceName = (anWebService != null) ? anWebService.serviceName() : anWebServiceProvider.serviceName();
      if (serviceName.length() == 0)
         serviceName = JavaUtils.getJustClassName(sepClass) + "Service";

      String serviceNS = (anWebService != null) ? anWebService.targetNamespace() : anWebServiceProvider.targetNamespace();
      if (serviceNS.length() == 0)
         serviceNS = getTypeNamespace(JavaUtils.getPackageName(sepClass));

      String portName = (anWebService != null) ? anWebService.portName() : anWebServiceProvider.portName();
      if (portName.length() == 0)
         portName = name + "Port";
      
      EndpointConfig epConfig = sepClass.getAnnotation(EndpointConfig.class);

      if (anWebService != null && anWebService.endpointInterface().length() > 0)
      {
         seiName = anWebService.endpointInterface();
         ClassLoader runtimeClassLoader = dep.getRuntimeClassLoader();
         if(null == runtimeClassLoader)
            throw new IllegalArgumentException("Runtime loader cannot be null");
         
         try
         {
            seiClass = runtimeClassLoader.loadClass(seiName);
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new RuntimeException("Cannot load service endpoint interface class!", cnfe);
         }
         WebService seiAnnotation = seiClass.getAnnotation(WebService.class);

         if (seiAnnotation == null)
            throw new RuntimeException("Interface does not have a @WebService annotation: " + seiName);

         if (seiAnnotation.portName().length() > 0 || seiAnnotation.serviceName().length() > 0 || seiAnnotation.endpointInterface().length() > 0)
            throw new RuntimeException("@WebService cannot have attribute 'portName', 'serviceName', 'endpointInterface' on: " + seiName);

         if (epConfig == null)
         {
            epConfig = seiClass.getAnnotation(EndpointConfig.class);
         }
      }
      
      DDEndpoint result = new DDEndpoint();
      
      result.setId(ep.getShortName());
      result.setAddress(ep.getAddress());
      result.setImplementor(ep.getTargetBeanName());
      result.setMtomEnabled(isMtomEnabled(ep.getTargetBeanClass()));
      result.setEpClass(seiClass != null ? seiClass : sepClass);
      result.setPortName(new QName(serviceNS, portName));
      result.setServiceName(new QName(serviceNS, serviceName));
      if (epConfig != null)
      {
         configureEndpoint(dep, result, epConfig.configFile(), epConfig.configName());
      }

      return result;
   }
   
   /**
    * Configures the endpoint definition according to the specified jaxws configuration
    * (provided through @EndpointConfig annotation). The specified config file is looked
    * for in the deployment. If it's not found, the specified config is searched in the
    * global server endpoint configurations.
    * 
    * @param ep
    * @param configFile
    * @param configName
    */
   private void configureEndpoint(ArchiveDeployment dep, DDEndpoint ep, String configFile, String configName)
   {
      UnifiedVirtualFile vf = null;
      try
      {
         vf = dep.getRootFile().findChild(configFile);
         ConfigRoot config = ConfigMetaDataParser.parse(vf.toURL());
         org.jboss.wsf.spi.metadata.config.EndpointConfig epConfig = config.getEndpointConfigByName(configName);
         ep.setPreHandlers(convertEndpointHandlers(epConfig.getPreHandlerChains()));
         ep.setPostHandlers(convertEndpointHandlers(epConfig.getPostHandlerChains()));
         ep.setProperties(epConfig.getProperties());
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not find " + configFile);
      }
      //TODO [JBWS-3286] use default endpoint configuration as a fallback
   }
   
   /**
    * Extracts the typeNS given the package name
    * Algorithm is based on the one specified in JAXWS v2.0 spec
    */
   private static String getTypeNamespace(String packageName)
   {
      StringBuilder sb = new StringBuilder("http://");

      //Generate tokens with '.' as delimiter
      StringTokenizer st = new StringTokenizer(packageName, ".");

      //Have a LIFO queue for the tokens
      Stack<String> stk = new Stack<String>();
      while (st != null && st.hasMoreTokens())
      {
         stk.push(st.nextToken());
      }

      String next;
      while (!stk.isEmpty() && (next = stk.pop()) != null)
      {
         if (sb.toString().equals("http://") == false)
            sb.append('.');
         sb.append(next);
      }

      // trailing slash
      sb.append('/');

      return sb.toString();
   }
   
}
