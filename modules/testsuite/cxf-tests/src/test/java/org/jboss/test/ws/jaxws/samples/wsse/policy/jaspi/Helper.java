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
package org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.cxf.ws.security.SecurityConstants;
import org.jboss.ws.api.configuration.ClientConfigUtil;
import org.jboss.wsf.stack.cxf.client.jaspi.module.SOAPClientAuthModule;
import org.jboss.wsf.test.ClientHelper;

public class Helper implements ClientHelper
{
   private String address;

   public boolean testJaspiClient() throws Exception
   {
      
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(address + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      ClientConfigUtil.setConfigProperties(proxy, "META-INF/jaxws-client-config.xml", "jaspiSecurityDomain");
      ((BindingProvider)proxy).getRequestContext().put(SecurityConstants.USERNAME, "kermit");
      ((BindingProvider)proxy).getRequestContext()
            .put(SecurityConstants.CALLBACK_HANDLER, "org.jboss.test.ws.jaxws.samples.wsse.policy.jaspi.UsernamePasswordCallback");
      
      proxy.sayHello();
      return SOAPClientAuthModule.log.equals("secureRequest");
   }

   public void setTargetEndpoint(String address)
   {
      this.address = address;

   }
}