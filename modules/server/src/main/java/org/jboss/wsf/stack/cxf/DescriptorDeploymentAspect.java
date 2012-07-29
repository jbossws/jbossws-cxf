/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.jboss.wsf.stack.cxf.metadata.services.DDEndpoint;

/**
 * A deployer that locates or generates cxf.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-May-2007
 */
public class DescriptorDeploymentAspect extends DeploymentAspect
{
   // provide logging
   private final Logger log = Logger.getLogger(DescriptorDeploymentAspect.class);

   private String invokerEJB3;
   private String invokerJSE;

   public void setInvokerEJB3(String invokerEJB3)
   {
      this.invokerEJB3 = invokerEJB3;
   }

   public void setInvokerJSE(String invokerJSE)
   {
      this.invokerJSE = invokerJSE;
   }
   
   @Override
   public void create(Deployment dep)
   {
		URL cxfURL = getCXFConfigFromDeployment(dep);
		if (cxfURL == null) {
			cxfURL = generateCXFConfigFromDeployment(dep);
		}
		putCXFConfigToDeployment(dep, cxfURL);
		checkCVE20122379(dep, cxfURL);
   }

   @Override
   public void destroy(Deployment dep)
   {
      DDBeans dd = dep.getAttachment(DDBeans.class);
      if (dd != null)
      {
         dd.destroyFileURL();
      }
   }
   
   
   /**
    * Looks for <b>jbossws-cxf.xml</b> in:
    * <ul>
    *   <li><b>META-INF</b> resource directory for EJB3 deployment</li>
    *   <li><b>WEB-INF</b> resource directory for POJO deployment</li>
    * </ul>
    * @param dep deployment where to look for resources
    * @return <b>jbossws-cxf.xml URL</b> or <b>null</b> if not found
    */
   private URL getCXFConfigFromDeployment(Deployment dep)
   {
      final String metadir = getMetaDir(dep);
      URL cxfURL = null;
      try
      {
         // get resource URL
         ArchiveDeployment archDep = (ArchiveDeployment)dep;
         cxfURL = archDep.getResourceResolver().resolve(metadir + "/jbossws-cxf.xml");
         log.info("JBossWS-CXF configuration found: " + cxfURL);
      }
      catch (IOException ignore)
      {
         // resource not found
      }
      
      return cxfURL;
   }
   
   private String getMetaDir(Deployment dep) {
      
      DeploymentType depType = dep.getType();
      if (depType == DeploymentType.JAXWS_EJB3)
      {
         // expected resource location for EJB3 deployments
         return "META-INF";
      }
      else if (depType == DeploymentType.JAXWS_JSE)
      {
         // expected resource location for POJO deployments
         return "WEB-INF";
      }
      else
      {
         // only POJO and EJB3 deployments are supported
         throw new IllegalStateException("Unsupported deployment type: " + depType);
      }
   }
   
   /**
    * Generated CXF descriptor from deployment
    * @param dep deployment
    * @return CXF descriptor URL
    */
   private URL generateCXFConfigFromDeployment(Deployment dep)
   {
      // Generate the jbossws-cxf.xml descriptor
      DeploymentType depType = dep.getType();
      
      DDBeans dd = new DDBeans();
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         String id = ep.getShortName();
         String address = ep.getAddress();
         String implementor = ep.getTargetBeanName();

         boolean mtomEnabled = isMtomEnabled(ep.getTargetBeanClass());

         DDEndpoint ddep = new DDEndpoint(id, address, implementor, mtomEnabled);

         if (depType == DeploymentType.JAXWS_EJB3)
         {
            ddep.setInvoker(invokerEJB3);
         }

         if (depType == DeploymentType.JAXWS_JSE)
         {
            ddep.setInvoker(invokerJSE);
         }

         log.info("Add " + ddep);
         dd.addEndpoint(ddep);
      }

      URL cxfURL = dd.createFileURL();
      log.info("JBossWS-CXF configuration generated: " + cxfURL);

      dep.addAttachment(DDBeans.class, dd);

      return cxfURL;
   }

   /**
    * Puts CXF config file to deployment property <b>org.jboss.ws.webapp.ContextParameterMap</b> map
    * @param dep deployment where to put
    * @param cxfURL to be put
    */
   @SuppressWarnings("unchecked")
   private void putCXFConfigToDeployment(Deployment dep, URL cxfURL)
   {
      // get property map
      String propKey = "org.jboss.ws.webapp.ContextParameterMap";
      Map<String, String> contextParams = (Map<String, String>)dep.getProperty(propKey);
      if (contextParams == null)
      {
         // if there's no associated map with the property create it now
         contextParams = new HashMap<String, String>();
         dep.setProperty(propKey, contextParams);
      }
      // put cxf config URL to the property map
      contextParams.put(CXFServletExt.PARAM_CXF_BEANS_URL, cxfURL.toExternalForm());
   }

   private static boolean isMtomEnabled(Class<?> beanClass)
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
   
   private void checkCVE20122379(Deployment dep, URL cxfURL)
   {
      try {
         Set<String> wsdlLocations = new HashSet<String>();
         //first check jbossws-cxf.xml
         Set<String> endpoints = checkAssertionsAndGet(cxfURL, "http://cxf.apache.org/jaxws", "endpoint", "implementor");
         ClassLoader cl = dep.getRuntimeClassLoader();
         if (cl == null) {
            cl = dep.getInitialClassLoader();
         }
         for (String ep : endpoints)
         {
            Class<?> clazz = cl.loadClass(ep);
            String wl = null;
            if (clazz.isAnnotationPresent(WebService.class)) {
               WebService wsa = clazz.getAnnotation(WebService.class);
               wl = wsa.wsdlLocation();
               String epIf = wsa.endpointInterface();
               if(epIf != null && !epIf.isEmpty()) {
                  Class<?> epIfClass = cl.loadClass(epIf);
                  WebService epIfWsa = epIfClass.getAnnotation(WebService.class);
                  if (epIfWsa != null && epIfWsa.wsdlLocation() != null && !epIfWsa.wsdlLocation().isEmpty()) {
                     wl = epIfWsa.wsdlLocation();
                  }
               }
            } else {
               WebServiceProvider wsp = clazz.getAnnotation(WebServiceProvider.class);
               wl = wsp.wsdlLocation();
            }
            if (wl != null && !wl.trim().isEmpty()) {
               wsdlLocations.add(wl);
            }
         }
         //then check wsdl files for contract first endpoints
         for (String w : wsdlLocations) {
            ArchiveDeployment archDep = (ArchiveDeployment)dep;
            URL wsdlURL = archDep.getResourceResolver().resolve(w);
            checkAssertionsAndGet(wsdlURL, null, null, null);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private Set<String> checkAssertionsAndGet(URL url, String searchNS, String searchLocalName, String searchAttributeName) throws Exception
   {
      if (log.isTraceEnabled()) {
         log.trace("* checking for CVE-2012-2379 possibly vulnerable assertions in " + url);
      }
      InputStream is = null;
      XMLStreamReader reader = null;
      Set<String> endpoints = new HashSet<String>();
      final boolean search = searchNS != null || searchLocalName != null || searchAttributeName != null;
      try
      {
         is = url.openStream();
         reader = StAXUtils.createXMLStreamReader(is);
         while (reader.hasNext())
         {
            switch (reader.next())
            {
               case START_ELEMENT:
               {
                  if (StAXUtils.match(reader, NAMESPACES, ASSERTIONS))
                  {
                     throw new RuntimeException("WS-Security Policy SupportingTokens not allowed due to known security vulnerability! URL: " + url);
                  }
                  else if (search && StAXUtils.match(reader, searchNS, searchLocalName))
                  {
                     endpoints.add(reader.getAttributeValue(null, searchAttributeName).trim());
                  }
               }
            }
         }
      }
      finally
      {
         try {
            reader.close();
         } catch (Exception e) {}
         try {
            is.close();
         } catch (Exception e) {}
      }
      return endpoints;
   }
   
   private static final String SP_NS_11 = "http://schemas.xmlsoap.org/ws/2005/02/securitypolicy";
   private static final String SP_NS_12 = "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702";
   private static final String SP_NS_13 = "http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802";
   private static final String SUPPORTING_TOKENS = "SupportingTokens";
   private static final String SIGNED_SUPPORTING_TOKENS = "SignedSupportingTokens";
   private static final String ENDORSING_SUPPORTING_TOKENS = "EndorsingSupportingTokens";
   private static final String SIGNED_ENDORSING_SUPPORTING_TOKENS = "SignedEndorsingSupportingTokens";
   private static final String SIGNED_ENCRYPTED_SUPPORTING_TOKENS = "SignedEncryptedSupportingTokens";
   private static final String ENCRYPTED_SUPPORTING_TOKENS = "EncryptedSupportingTokens";
   private static final String ENDORSING_ENCRYPTED_SUPPORTING_TOKENS = "EndorsingEncryptedSupportingTokens";
   private static final String SIGNED_ENDORSING_ENCRYPTED_SUPPORTING_TOKENS = "SignedEndorsingEncryptedSupportingTokens";
   private static String[] NAMESPACES = new String[3];
   private static String[] ASSERTIONS = new String[8];
   static {
      NAMESPACES[0] = SP_NS_11;
      NAMESPACES[1] = SP_NS_12;
      NAMESPACES[2] = SP_NS_13;
      ASSERTIONS[0] = SUPPORTING_TOKENS;
      ASSERTIONS[1] = SIGNED_SUPPORTING_TOKENS;
      ASSERTIONS[2] = ENDORSING_SUPPORTING_TOKENS;
      ASSERTIONS[3] = SIGNED_ENDORSING_SUPPORTING_TOKENS;
      ASSERTIONS[4] = SIGNED_ENCRYPTED_SUPPORTING_TOKENS;
      ASSERTIONS[5] = ENCRYPTED_SUPPORTING_TOKENS;
      ASSERTIONS[6] = ENDORSING_ENCRYPTED_SUPPORTING_TOKENS;
      ASSERTIONS[7] = SIGNED_ENDORSING_ENCRYPTED_SUPPORTING_TOKENS;
   }

}
