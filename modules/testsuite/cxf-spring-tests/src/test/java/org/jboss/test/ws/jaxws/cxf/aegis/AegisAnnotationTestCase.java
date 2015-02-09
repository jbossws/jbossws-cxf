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
package org.jboss.test.ws.jaxws.cxf.aegis;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Map;

@RunWith(Arquillian.class)
public class AegisAnnotationTestCase extends JBossWSTest
{
   @Deployment(testable = false)
   public static WebArchive createWarDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class,"jaxws-aegis-annotation.war");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.cxf.impl\n")) //cxf impl required due to AegisDataBinding reference in endpoint impl
               .addClass(org.jboss.test.ws.jaxws.cxf.aegis.AegisAnnotationGroupQuery.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.aegis.AegisAnnotationGroupQueryImpl.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.aegis.Member.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/aegis/jaxws/annotation/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testAccessAnnotation() throws Exception
   {
      ClientProxyFactoryBean proxyFactory = new ClientProxyFactoryBean();
      proxyFactory.setDataBinding(new AegisDatabinding());
      proxyFactory.setServiceClass(AegisGroupQuery.class);
      proxyFactory.setAddress("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-aegis-annotation");
      AegisGroupQuery query = (AegisGroupQuery)proxyFactory.create();
      @SuppressWarnings("unchecked")
      Map<Integer, String> members =  query.getMembers();
      assertEquals(2, members.size());
      assertEquals(true, members.containsKey(2));
   }

}
