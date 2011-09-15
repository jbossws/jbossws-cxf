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
package org.jboss.test.ws.jaxws.cxf.mixtype;

import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

public class MixedTypeTestCase extends JBossWSTest
{
   private final String endpointURL = "http://" + getServerHost() + ":8080/mixtype/ServiceOne/EndpointOne";
   private final String ejbEndpointURL = "http://" + getServerHost() + ":8080/mixtype/EJBServiceOne/EJBEndpointOne";

   private String targetNS = "http://org.jboss.ws.jaxws.cxf/mixtype";
   
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(MixedTypeTestCase.class, "jaxws-cxf-mixtype.war");
   }

   public void testEndpoint() throws Exception
   {
      
      URL wsdlOneURL = new URL(endpointURL + "?wsdl");
      QName serviceOneName = new QName(targetNS, "ServiceOne");
      Service serviceOne = Service.create(wsdlOneURL, serviceOneName);
      EndpointOne endpoint = (EndpointOne)serviceOne.getPort(new QName(targetNS, "EndpointOnePort"), EndpointOne.class);
      assertEquals("mixedType", endpoint.echo("mixedType"));
      assertEquals(1, endpoint.getCount());
   }
   
   public void testEJBEndpoint() throws Exception
   {
      URL wsdlOneURL = new URL(ejbEndpointURL + "?wsdl");
      QName serviceOneName = new QName(targetNS, "EJBServiceOne");
      Service serviceOne = Service.create(wsdlOneURL, serviceOneName);
      EndpointOne endpoint = (EndpointOne)serviceOne.getPort(new QName(targetNS, "EJBEndpointOnePort"), EndpointOne.class);
      assertEquals("mixedType", endpoint.echo("mixedType"));
      assertEquals(1, endpoint.getCount());
   }

 
}