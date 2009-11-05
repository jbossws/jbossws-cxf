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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.common.integration.WSConstants;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * A deployer to correct the jms endpoint address to unknown address.<br>
 * The final jms address will be set when servlet is initilized
 * @author <a herf="mailto:ema@redhat.com>Jim Ma</a>
 */
public class JMSEndpointAddressDeploymentAspect extends DeploymentAspect
{
   private static final String JMS_NS = "http://cxf.apache.org/transports/jms";
   private static final String UNKOWN_ADDRESS = "unkwon address";
   @SuppressWarnings("unchecked")
   @Override
   public void start(Deployment dep)
   {
      //get EndpointRegistry 
      Map<String, String> contextParams = (Map<String, String>)dep.getProperty(WSConstants.STACK_CONTEXT_PARAMS);
      String url = contextParams.get(CXFServletExt.PARAM_CXF_BEANS_URL);
      try
      {
         Document document = DOMUtils.getDocumentBuilder().parse(url);
         List<Element> elements = DOMUtils.getChildElementsAsList(document.getDocumentElement(), new QName("http://cxf.apache.org/jaxws","endpoint"));
         //TODO: parse the jms address from jbossws-cxf.xml and set endpoint address (java first)
         for(Element element: elements) {
            String transportId = DOMUtils.getAttributeValue(element, "transportId");
            if (JMS_NS.equals(transportId)) {
               String implementor = DOMUtils.getAttributeValue(element, "implementor");
               if (implementor != null) {
                  for (Endpoint endpoint : dep.getService().getEndpoints()) {
                     if (implementor.equals(endpoint.getTargetBeanClass().getName())) {
                        endpoint.setAddress(UNKOWN_ADDRESS);
                     }
                  }
               }
            }
         }
         
      }
      catch (Exception e)
      {
         log.warn("Failed to parse jbossws-cxf.xml", e);
      }  
   }
}
