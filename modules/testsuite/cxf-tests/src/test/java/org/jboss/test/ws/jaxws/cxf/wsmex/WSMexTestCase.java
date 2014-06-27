/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.wsmex;

import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.mex.MetadataExchange;
import org.apache.cxf.ws.mex.model._2004_09.Metadata;
import org.apache.cxf.ws.mex.model._2004_09.MetadataSection;
import org.jboss.ws.common.DOMWriter;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.w3c.dom.Node;

/**
 * Test WS-MetadataExchange
 * 
 * @author alessio.soldano@jboss.com
 * @since 10-May-2012
 */
public class WSMexTestCase extends JBossWSTest
{
   private String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-cxf-wsmex/EndpointService";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-cxf-wsmex.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.wsmex.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.wsmex.EndpointBean.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(WSMexTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testEndpoint() throws Exception
   {
      JaxWsProxyFactoryBean proxyFac = new JaxWsProxyFactoryBean();
      proxyFac.setAddress(endpointAddress);
      MetadataExchange exc = proxyFac.create(MetadataExchange.class);
      Metadata metadata = exc.get2004();

      assertNotNull(metadata);
      assertEquals(1, metadata.getMetadataSection().size());

      MetadataSection ms = metadata.getMetadataSection().get(0);
      assertEquals("http://schemas.xmlsoap.org/wsdl/", ms.getDialect());
      assertEquals("http://org.jboss.ws/cxf/wsmex", ms.getIdentifier());

      String wsdl = DOMWriter.printNode((Node)ms.getAny(), true);
      assertTrue(wsdl.contains("EndpointBeanServiceSoapBinding"));
   }
}
