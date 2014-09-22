/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.interceptor.util;

import javax.wsdl.Definition;

import org.apache.cxf.frontend.WSDLGetUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;
import org.jboss.wsf.stack.cxf.addressRewrite.SoapAddressRewriteHelper;

/**
 * This is an extension of the org.apache.cxf.frontend.WSDLGetUtils; currently this
 * is needed for properly setting the publishedEndpointURL in the code-first scenario
 * when a path rewrite rule is specified in the server configuration.
 * 
 * @author rsearls@redhat.com
 * @author alessio.soldano@jboss.com
 * 
 */
public class WSDLSoapAddressRewriteUtils extends WSDLGetUtils {
   
   private final SOAPAddressRewriteMetadata sarm;
   
   public WSDLSoapAddressRewriteUtils(SOAPAddressRewriteMetadata sarm) {
      super();
      this.sarm = sarm;
   }

   @Override
   public String getPublishableEndpointUrl(Definition def, String epurl,
                                           EndpointInfo endpointInfo){

      if (endpointInfo.getProperty(PUBLISHED_ENDPOINT_URL) != null) {
         epurl = String.valueOf(endpointInfo.getProperty(PUBLISHED_ENDPOINT_URL));
         updatePublishedEndpointUrl(epurl, def, endpointInfo.getName());
      } else {
         // When using replacement path, must set replacement path in the active url.
         if ((SoapAddressRewriteHelper.isPathRewriteRequired(sarm) || SoapAddressRewriteHelper.isSchemeRewriteRequired(sarm)) //TODO if we ended up here, the checks are perhaps not needed (otherwise this won't have been installed)
            && endpointInfo.getAddress().contains(ServerConfig.UNDEFINED_HOSTNAME)) {
            epurl = SoapAddressRewriteHelper.getRewrittenPublishedEndpointUrl(epurl, sarm);
            updatePublishedEndpointUrl(epurl, def, endpointInfo.getName());
         }
      }
      return epurl;
   }
}
