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

import java.util.Map;

import junit.framework.Test;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

public class AegisAnnotationTestCase extends JBossWSTest
{
   private String endpointURL = "http://" + getServerHost() + ":8080/jaxws-aegis-annotation";

   public static Test suite()
   {
      return new JBossWSTestSetup(AegisAnnotationTestCase.class, "jaxws-aegis-annotation.war");
   }

   public void testAccessAnnotation() throws Exception
   {
      ClientProxyFactoryBean proxyFactory = new ClientProxyFactoryBean();
      proxyFactory.setDataBinding(new AegisDatabinding());
      proxyFactory.setServiceClass(AegisGroupQuery.class);
      proxyFactory.setAddress(endpointURL);
      AegisGroupQuery query = (AegisGroupQuery)proxyFactory.create();
      Map<Integer, String> members =  query.getMembers();
      this.assertEquals(2, members.size());
      this.assertEquals(true, members.containsKey(2));
   }

}
