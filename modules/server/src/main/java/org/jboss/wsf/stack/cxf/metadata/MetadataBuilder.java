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
package org.jboss.wsf.stack.cxf.metadata;

import static org.jboss.wsf.stack.cxf.i18n.Loggers.METADATA_LOGGER;
import static org.jboss.wsf.stack.cxf.i18n.Messages.MESSAGES;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import jakarta.jws.WebService;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.xml.ws.soap.MTOM;
import jakarta.xml.ws.soap.SOAPBinding;

import org.jboss.ws.common.JavaUtils;
import org.jboss.ws.common.deployment.SOAPAddressWSDLParser;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.HttpEndpoint;
import org.jboss.wsf.spi.deployment.ResourceResolver;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainsMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;
import org.jboss.wsf.spi.metadata.webservices.PortComponentMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebserviceDescriptionMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;
import org.jboss.wsf.stack.cxf.JBossWSInvoker;
import org.jboss.wsf.stack.cxf.addressRewrite.SoapAddressRewriteHelper;
import org.jboss.wsf.stack.cxf.configuration.SysPropUtils;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.jboss.wsf.stack.cxf.metadata.services.DDEndpoint;

/**
 * Builds the DDBeans metadata
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Apr-2010
 *
 */
public class MetadataBuilder
{
   public MetadataBuilder()
   {
      
   }
   
   public DDBeans build(Deployment dep)
   {
      final SOAPAddressRewriteMetadata sarm = dep.getAttachment(SOAPAddressRewriteMetadata.class);
	  Map<QName, String> serviceNameAddressMap = new HashMap<QName, String>();
      Map<String, SOAPAddressWSDLParser> soapAddressWsdlParsers = new HashMap<String, SOAPAddressWSDLParser>();
      DDBeans dd = new DDBeans();
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         DDEndpoint ddep = createDDEndpoint(ep.getTargetBeanClass(), (ArchiveDeployment)dep, ep);

         if (ep instanceof HttpEndpoint)
         {
            ddep.setInvoker(JBossWSInvoker.class.getName());
         }
         processWSDDContribution(ddep, (ArchiveDeployment)dep);
         URL wsdlLocation = getWsdlLocationURL(ddep, ((ArchiveDeployment)dep).getResourceResolver());
         processAddressRewrite(ddep, wsdlLocation, sarm, soapAddressWsdlParsers);
         
         METADATA_LOGGER.addingServiceEndpointMetadata(METADATA_LOGGER.isDebugEnabled() ? ddep.toStringExtended() : ddep.toString());
         dd.addEndpoint(ddep);
         serviceNameAddressMap.put(ddep.getServiceName(), ddep.getAddress());
      }
      dep.setProperty("ServiceAddressMap", serviceNameAddressMap);
      return dd;
   }
   
   protected boolean isMtomEnabled(Class<?> beanClass)
   {
      BindingType bindingType = (BindingType)beanClass.getAnnotation(BindingType.class);
      MTOM mtom = (MTOM)beanClass.getAnnotation(MTOM.class);

      boolean mtomEnabled = mtom != null && mtom.enabled();
      if (!mtomEnabled && bindingType != null)
      {
         String binding = bindingType.value();
         mtomEnabled = binding.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING) || binding.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING);
      }
      
      return mtomEnabled;
   }
   
   private void processWSDDContribution(DDEndpoint endpoint, ArchiveDeployment dep)
   {
      WebservicesMetaData webservices = dep.getAttachment(WebservicesMetaData.class);
      if (webservices != null)
      {
         for (WebserviceDescriptionMetaData wsDesc : webservices.getWebserviceDescriptions())
         {
            for (PortComponentMetaData portComp : wsDesc.getPortComponents())
            {
               final String linkedId = portComp.getEjbLink() != null ? portComp.getEjbLink() : portComp.getServletLink();
               final String id = endpoint.getId();
               if (!id.equals(linkedId)) continue;
               
               // PortQName overrides
               if (portComp.getWsdlPort() != null) {
                  METADATA_LOGGER.overridePortName(id, endpoint.getPortName(), portComp.getWsdlPort());
                  endpoint.setPortName(portComp.getWsdlPort());
               }
               // ServiceQName overrides
               if (portComp.getWsdlService() != null) {
                  METADATA_LOGGER.overrideServiceName(id, endpoint.getServiceName(), portComp.getWsdlService());
                  endpoint.setServiceName(portComp.getWsdlService());
               }

               // HandlerChain contributions
               UnifiedHandlerChainsMetaData chainWrapper = portComp.getHandlerChains();
               if (chainWrapper != null) {
                  endpoint.setHandlers(convertEndpointHandlers(chainWrapper.getHandlerChains()));
               }

               // MTOM settings
               if (portComp.isMtomEnabled()) {
                  METADATA_LOGGER.enableMTOM(id);
                  endpoint.setMtomEnabled(true);
                  endpoint.setMtomThreshold(portComp.getMtomThreshold());
               }

               // Addressing
               if (portComp.isAddressingEnabled()) {
                  METADATA_LOGGER.enableAddressing(id);
                  endpoint.setAddressingEnabled(true);
                  endpoint.setAddressingRequired(portComp.isAddressingRequired());
                  endpoint.setAddressingResponses(portComp.getAddressingResponses());
               }
               // RespectBinding
               if (portComp.isRespectBindingEnabled()) {
                  METADATA_LOGGER.enableRespectBinding(id);
                  endpoint.setRespectBindingEnabled(true);
               }
               // wsdlLocation override
               String wsdlFile = portComp.getWebserviceDescription().getWsdlFile();
               if (wsdlFile != null) {
                  METADATA_LOGGER.overridingWsdlFileLocation(id, wsdlFile);
                  endpoint.setWsdlLocation(wsdlFile);
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
               METADATA_LOGGER.filtersNotSupported();
            }
            for (UnifiedHandlerMetaData uhmd : handlerChain.getHandlers())
            {
               if (uhmd.getInitParams() != null && !uhmd.getInitParams().isEmpty())
               {
                  METADATA_LOGGER.initParamsSupported(uhmd.getHandlerName());
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
      if (serviceName.length() == 0) {
         serviceName = JavaUtils.getJustClassName(sepClass) + "Service";
      }

      String serviceNS = (anWebService != null) ? anWebService.targetNamespace() : anWebServiceProvider.targetNamespace();
      if (serviceNS.length() == 0)
         serviceNS = getTypeNamespace(JavaUtils.getPackageName(sepClass));

      String portName = (anWebService != null) ? anWebService.portName() : anWebServiceProvider.portName();
      if (portName.length() == 0) {
         portName = name + "Port";
      }
      
      String annWsdlLocation;
      if (anWebService != null && anWebService.endpointInterface().length() > 0)
      {
         seiName = anWebService.endpointInterface();
         ClassLoader runtimeClassLoader = dep.getClassLoader();
         if(null == runtimeClassLoader)
            throw MESSAGES.runtimeLoaderCannotBeNull(dep);
         
         try
         {
            seiClass = runtimeClassLoader.loadClass(seiName);
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new RuntimeException(cnfe);
         }
         WebService seiAnnotation = seiClass.getAnnotation(WebService.class);

         if (seiAnnotation == null)
            throw MESSAGES.webserviceAnnotationNotFound(seiName);

         if (seiAnnotation.portName().length() > 0 || seiAnnotation.serviceName().length() > 0 || seiAnnotation.endpointInterface().length() > 0)
            throw MESSAGES.webserviceAnnotationSEIAttributes(seiName);

         annWsdlLocation = !"".equals(anWebService.wsdlLocation()) ? anWebService.wsdlLocation() : seiAnnotation.wsdlLocation();
      }
      else
      {
         annWsdlLocation = (anWebService != null) ? anWebService.wsdlLocation() : anWebServiceProvider.wsdlLocation();
      }
      
      DDEndpoint result = new DDEndpoint();
      
      result.setId(ep.getShortName());
      result.setAddress(SysPropUtils.expandSystemProperty(ep.getAddress()));
      result.setImplementor(ep.getTargetBeanName());
      result.setMtomEnabled(isMtomEnabled(ep.getTargetBeanClass()));
      result.setEpClass(seiClass != null ? seiClass : sepClass);
      result.setPortName(new QName(serviceNS, portName));
      result.setServiceName(new QName(serviceNS, serviceName));
      if (annWsdlLocation.length() > 0) {
         result.setAnnotationWsdlLocation(annWsdlLocation);
      }
      return result;
   }
   
   protected URL getWsdlLocationURL(DDEndpoint ddep, ResourceResolver resolver) {
      String wsdlLocation = ddep.getWsdlLocation();
      if (wsdlLocation == null) {
         wsdlLocation = ddep.getAnnotationWsdlLocation();
      }
      if (wsdlLocation != null) {
         URL wsdlURL = resolver.resolveFailSafe(wsdlLocation);
         if (wsdlURL != null) {
            return wsdlURL;
         } else {
            throw MESSAGES.couldNotFetchWSDLContract(ddep.getImplementor(), wsdlLocation);
         }
      } else {
         return null;
      }
   }
   
   protected void processAddressRewrite(DDEndpoint ddep, URL wsdlUrl, SOAPAddressRewriteMetadata sarm, Map<String, SOAPAddressWSDLParser> soapAddressWsdlParsers)
   {
      if (wsdlUrl != null) {
         SOAPAddressWSDLParser parser = getCurrentSOAPAddressWSDLParser(wsdlUrl, soapAddressWsdlParsers);
         //do not try rewriting addresses for not-http binding
         String wsdlAddress = parser.filterSoapAddress(ddep.getServiceName(), ddep.getPortName(), SOAPAddressWSDLParser.SOAP_HTTP_NS);

         String rewrittenWsdlAddress = SoapAddressRewriteHelper.getRewrittenPublishedEndpointUrl(wsdlAddress, ddep.getAddress(), sarm);
         //If "auto rewrite", leave "publishedEndpointUrl" unset so that CXF does not force host/port values for
         //wsdl imports and auto-rewrite them too; otherwise set the new address into "publishedEndpointUrl",
         //which causes CXF to override any address in the published wsdl.
         if (!SoapAddressRewriteHelper.isAutoRewriteOn(sarm)) {
            ddep.setPublishedEndpointUrl(rewrittenWsdlAddress);
         }
      } else {
         //same comment as above regarding auto rewrite...
         if (!SoapAddressRewriteHelper.isAutoRewriteOn(sarm)) {
            //force computed address for code first endpoints
            ddep.setPublishedEndpointUrl(SoapAddressRewriteHelper.getRewrittenPublishedEndpointUrl(ddep.getAddress(), sarm));
         }
      }
   }
   
   private SOAPAddressWSDLParser getCurrentSOAPAddressWSDLParser(URL wsdlUrl, Map<String, SOAPAddressWSDLParser> soapAddressWsdlParsers) {
      final String key = wsdlUrl.toString();
      SOAPAddressWSDLParser parser = soapAddressWsdlParsers.get(key);
      if (parser != null) {
         return parser;
      } else {
         parser = new SOAPAddressWSDLParser(wsdlUrl);
         soapAddressWsdlParsers.put(key, parser);
         return parser;
      }
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
