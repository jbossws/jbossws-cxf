/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2956;

import java.net.URL;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

public class JBWS2956TestCase extends JBossWSTest
{
   
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws2956";

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2956TestCase.class, "jaxws-jbws2956.war");
   }

   public void testCall() throws Exception
   {  
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      OnewayEndpointService service = new OnewayEndpointService(wsdlURL);
      OnewayEndpoint port = service.getOnewayEndpointPort();
      //there should be no exception threw
      port.echo("testJBWS2956");    
   }
}