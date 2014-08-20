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
package org.jboss.wsf.stack.cxf.interceptor;

import java.util.Map;

import org.apache.cxf.frontend.WSDLGetUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
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

   public WSDLSoapAddressRewriteInterceptor(Map<String, String> props) {
      // this must run before WSDLGetInterceptor which is in Phase.READ
      super(Phase.POST_STREAM);
      this.wsdlGetUtils = new WSDLSoapAddressRewriteUtils(props);
   }

   public void handleMessage(Message message) throws Fault {
      message.setContextualProperty(WSDLGetUtils.class.getName(), wsdlGetUtils);
   }

}
