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
package org.jboss.wsf.stack.cxf.interceptor;

import org.apache.cxf.frontend.WSDLGetUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;
import org.jboss.wsf.stack.cxf.interceptor.util.WSDLSoapAddressRewriteUtils;

/**
 * This inInterceptor registers a custom WSDLGetUtils which will preform the desired
 * soap:address rewrite
 *
 * @author rsearls@redhat.com
 * @author alessio.soldano@jboss.com
 * @since 19-May-2014
 */
public class WSDLSoapAddressRewriteInterceptor extends AbstractPhaseInterceptor<Message> {
   private final WSDLGetUtils wsdlGetUtils;

   public WSDLSoapAddressRewriteInterceptor(SOAPAddressRewriteMetadata sarm) {
      // this must run before WSDLGetInterceptor which is in Phase.READ
      super(Phase.POST_STREAM);
      this.wsdlGetUtils = new WSDLSoapAddressRewriteUtils(sarm);
   }

   public void handleMessage(Message message) throws Fault {
      message.put(WSDLGetUtils.class.getName(), wsdlGetUtils);
   }

}
