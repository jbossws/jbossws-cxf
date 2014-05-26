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
package org.jboss.test.ws.jaxws.cxf.asyncclient;

import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

import junit.framework.Test;

/**
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class AsyncClientTestCase extends JBossWSTest
{
   private String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-cxf-asyncclient";

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(AsyncClientTestCase.class, "jaxws-cxf-asyncclient.war");
   }

   public void testAsycClienWithHCAddress() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws/cxf/asyncclient", "EndpointImplService");
      URL wsdlURL = new URL(endpointAddress + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Endpoint proxy = service.getPort(Endpoint.class);
      BindingProvider provider = (BindingProvider)proxy;
      Map<String, Object> requestContext = provider.getRequestContext();
      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "hc://" + endpointAddress);
      assertEquals("Echo:1000", proxy.echo(1000));
   }
   
}