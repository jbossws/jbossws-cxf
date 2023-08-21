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
