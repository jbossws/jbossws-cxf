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
package org.jboss.test.ws.jaxws.cxf.jbws3060;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 11-Jun-2010
 */
public class JBWS3060ServletTestCase extends JBWS3060Tests
{
   private String endpointOneURL = "http://" + getServerHost() + ":8080/jaxws-cxf-jbws3060-jse/ServiceOne/EndpointOne";
   private String endpointTwoURL = "http://" + getServerHost() + ":8080/jaxws-cxf-jbws3060-jse/ServiceTwo/EndpointTwo";

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(JBWS3060ServletTestCase.class, "jaxws-cxf-jbws3060-jse.war");
   }
   
   @Override
   protected String getEndpointOneURL()
   {
      return endpointOneURL;
   }


   @Override
   protected String getEndpointTwoURL()
   {
      return endpointTwoURL;
   }
}
